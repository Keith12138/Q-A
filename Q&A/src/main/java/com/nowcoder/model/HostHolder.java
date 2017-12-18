package com.nowcoder.model;

import org.springframework.stereotype.Component;

/**
 * Created by hys on 2017/12/17.
 */
//用来存放拦截器判断的用户信息是谁
@Component
public class HostHolder {
    //通过线程本地变量来表示，实现每个线程对该变量都有一个拷贝，即多个用户同时请求时不会冲突（变量副本）
    private static ThreadLocal<User> users=new ThreadLocal<User>();

    public User getUser(){
        return users.get();
    }

    public  void setUser(User user){
        users.set(user);
    }

    public void clear(){
        users.remove();
    }
}
