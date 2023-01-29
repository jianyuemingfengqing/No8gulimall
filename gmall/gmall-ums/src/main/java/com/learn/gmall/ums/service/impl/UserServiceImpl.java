package com.learn.gmall.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.gmall.common.bean.PageParamVo;
import com.learn.gmall.common.bean.PageResultVo;
import com.learn.gmall.common.exception.AuthException;
import com.learn.gmall.ums.entity.UserEntity;
import com.learn.gmall.ums.mapper.UserMapper;
import com.learn.gmall.ums.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();

        // 判断类型
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("password", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }

        // 由于数据唯一, 所以可以使用 getone, getlist 等, 使用count只需要判断是否有这个数据就行, 比较方便
        return userMapper.selectCount(wrapper) == 0;

    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 校验短信验证码  根据手机号 在 redis中查询验证码, 然后比较
/*         String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + userEntity.getPhone());
         if (!StringUtils.equals(code, cacheCode)) {
             return false;
         }*/
        // 生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt); // md5 每次都是生成密文进行比较, 所以必须保存盐

        // 对密码加密 md5hex 64位加密 更加安全
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword()+ salt));

        // 新增用户   设置初始值, 创建时间等
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1L);
        userEntity.setStatus(1); // 状态 可用
        userEntity.setIntegration(0);
        userEntity.setGrowth(0);
        userEntity.setNickname(userEntity.getUsername()); // 有些网站是随机值, 有些是和用户名一致, 有些是用户名都一样

        // 添加到数据库
        boolean b = this.save(userEntity);

        // if(b){
        // 注册成功，删除redis中的记录
        // this.redisTemplate.delete(KEY_PREFIX + memberEntity.getPhone());
        // }
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {

        // 1.根据登录名查询用户信息（拿到盐）
/*         使用list 还是 one?
             有些账号是用别人的手机 或者 邮箱作为用户名, 此时会有多个数据, one会出错
             一般网站为了避免这种情况会在前端对用户名进行限制, 比如用户名不能带@ 防止邮箱, 不能全数字, 防止手机号
             项目做了限定, 所以使用getone
             */
        UserEntity userEntity = this.getOne(new QueryWrapper<UserEntity>()
                .eq("username", loginName)
                .or()
                .eq("phone", loginName)
                .or()
                .eq("email", loginName)
        );


        // 2.判断用户是否为空
        //错误类型太过详细, 会使黑客攻击更容易, 密码错误就表示账户正确, 降低黑客攻击门槛
        if (userEntity == null){
            throw new AuthException("账户或密码输入不正确！");
        }

        // 3.对密码加盐加密，并和数据库中的密码进行比较
        password = DigestUtils.md5Hex(password + userEntity.getSalt());

        // 4. 比较密码
        if (!StringUtils.equals(userEntity.getPassword(), password)){
            throw new AuthException("账户或密码输入不正确！");
        }

        // 5. 返回数据
        return userEntity;
    }
}