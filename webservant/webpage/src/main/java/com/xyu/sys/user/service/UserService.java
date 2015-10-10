package com.xyu.sys.user.service;

import com.xyu.core.baseservice.BaseService;
import com.xyu.sys.user.bean.User;
import org.springframework.stereotype.Service;

/**
 * @author Xiang.Yu
 * Created by Administrator on 2015/10/5 0005.
 * @version 0.1.0
 *
 * 用户基础业务接口
 */
public interface UserService extends BaseService<User>{

    User findByUsername(String username);


}