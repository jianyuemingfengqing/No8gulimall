package com.learn.gmall.gateway.filters;

import com.learn.gmall.common.utils.IpUtils;
import com.learn.gmall.common.utils.JwtUtils;
import com.learn.gmall.gateway.config.JwtProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    private JwtProperties properties;

    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                // 获取request及response  名字与HttpServletRequest但是结构差不多
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                // 1.判断当前请求在不在拦截名单之中，如果不在则执行放行
                String curPath = request.getURI().getPath(); // 当前请求的路径
                List<String> paths = config.paths; // 拦截名单 yml中的配置
                // 如果拦截名单不为空，并且当前路径不在拦截名单之中则放行
                if (!CollectionUtils.isEmpty(paths) && !paths.stream().anyMatch(path -> curPath.startsWith(path))){
                    return chain.filter(exchange);
                }

                // 2.获取token：获取位置: 同步-cookie；异步-头信息
                String token = request.getHeaders().getFirst(properties.getToken()); // 从请求头信息中获取token
                if (StringUtils.isBlank(token)){
                    // 从cookie中尝试获取token
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();// 工具类是http中获取, 不能使用, 使用原生
                    if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(properties.getCookieName())){
                        // 不为空, 并且有我想要的cookie才获取

                        HttpCookie cookie = cookies.getFirst(properties.getCookieName()); // 防止重名
                        token = cookie.getValue();
                    }
                }

                // 3.判断token是否为空，如果为空则重定向到登录页面请求结束
                if (StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }

                try {
                    // 4.解析token，如果出现异常则重定向到登录页面请求结束
                    Map<String, Object> map = JwtUtils.getInfoFromToken(token, properties.getPublicKey());

                    // 5.获取载荷中的ip地址 和 当前请求的ip地址 比较，如果不一致说明可能是盗用则重定向到登录页面请求结束
                    String ip = map.get("ip").toString(); // 登录用户的ip地址
                    String curIp = IpUtils.getIpAddressAtGateway(request); // 当前请求的ip地址
                    if (!StringUtils.equals(ip, curIp)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        return response.setComplete();
                    }

                    // 6.把解析出来的用户信息传递给后续服务
                    request.mutate().header("userId", map.get("userId").toString())
                            .header("username", map.get("username").toString()).build();
                    exchange.mutate().request(request).build();

                    // 7.放行
                    return chain.filter(exchange);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }
            }
        };
    }

    @Data
    public static class PathConfig {
        private List<String> paths;
    }
}
