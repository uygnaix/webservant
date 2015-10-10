package com.xyu.sys.shiro;

import com.xyu.common.utlis.Encoder;
import com.xyu.core.spring.SpringContextHolder;
import com.xyu.sys.config.Global;
import com.xyu.sys.user.bean.Permission;
import com.xyu.sys.user.bean.Role;
import com.xyu.sys.user.bean.User;
import com.xyu.sys.user.service.UserService;
import com.xyu.sys.user.service.impl.UserServiceImpl;
import com.xyu.sys.utils.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Xiang.Yu
 * Created by Xiang.Yu on 2015/10/5 0005.
 * @version 0.1.1
 */
@Service
public class UserRealm extends AuthorizingRealm{

    @Autowired
    private UserService userService;

    /**
     * 认证回调函数, 登录时调用
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
            User user = userService.findByUsername(token.getUsername());
        String pass=Encoder.encodeHex("123456".getBytes());
        if (user != null) {
                byte[] salt = Encoder.decodeHex(user.getPassword().substring(0, 16));
                return new SimpleAuthenticationInfo(new Principal(user),
                        user.getPassword().substring(16), ByteSource.Util.bytes(salt), getName());
            } else {
                return null;
            }

    }

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
        Principal principal = (Principal) getAvailablePrincipal(principals);
        User user = userService.findByUsername(principal.getUsername());
        if (user != null) {
            UserUtils.putCache("user", user);
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
//			List<Menu> list = UserUtils.getMenuList();
//			for (Menu menu : list) {
//				if (StringUtils.isNotBlank(menu.getPermission())) {
//					// 添加基于Permission的权限信息
//					for (String permission : StringUtils.split(
//							menu.getPermission(), ",")) {
//						info.addStringPermission(permission);
//					}
//				}
//			}
            // 更新登录IP和时间
//            getSystemService().updateUserLoginInfo(user.getId());
            return info;
        } else {
            return null;
        }
    }

    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
        if (!Global.isSSOLogin()) {
            super.assertCredentialsMatch(token, info);
        }
    };

    /**
     * 设定密码校验的Hash算法与迭代次数
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(Global.HASH_ALGORITHM);
        matcher.setHashIterations(Global.HASH_INTERATIONS);
        setCredentialsMatcher(matcher);
    }

    /**
     * 清空用户关联权限认证，待下次使用时重新加载
     */
    public void clearCachedAuthorizationInfo(String principal) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
        clearCachedAuthorizationInfo(principals);
    }

    /**
     * 清空所有关联认证
     */
    public void clearAllCachedAuthorizationInfo() {
        Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
        if (cache != null) {
            for (Object key : cache.keys()) {
                cache.remove(key);
            }
        }
    }

}