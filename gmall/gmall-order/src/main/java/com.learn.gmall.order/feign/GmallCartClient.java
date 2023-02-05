package com.learn.gmall.order.feign;

import com.learn.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
