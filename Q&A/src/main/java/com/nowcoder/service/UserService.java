package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by hys on 2017/12/16.
 */
@Service
public class UserService {
    @Autowired
    UserDAO userDAO;

    @Autowired
    LoginTicketDAO loginTicketDAO;

    public User selectByName(String name){
        return userDAO.selectByName(name);
    }


    //用户注册的服务
    public Map<String,Object> register(String username,String password){
        Map<String,Object> map=new HashMap<String,Object>();
        if(StringUtils.isBlank(username)){
            map.put("msg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        //判断数据库中是否已经存在该用户名
        User user=userDAO.selectByName(username);
        if(user!=null){
            map.put("msg","用户名已经被注册");
        }

        //如果符合条件可以进行注册，则把用户信息存入数据库
        user=new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));//随机生成一段盐存入数据库
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png",
                new Random().nextInt(1000)));//添加一个随机头像
        user.setPassword(WendaUtil.MD5(password + user.getSalt()));//存入密码加盐后的加密密文
        userDAO.addUser(user);


        //添加用户登录的状态信息
        String ticket=addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;

    }


    //用户登录的服务
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        //判断数据库中是否存在该用户名
        User user=userDAO.selectByName(username);
        if(user==null){
            map.put("msg","用户名不存在");
            return map;
        }

        //判断密码是否正确
        if(!WendaUtil.MD5(password+user.getSalt()).equals(user.getPassword())){
            map.put("msg","密码不正确");
            return map;
        }

        //添加用户登录的状态信息
        String ticket=addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;


    }


    private String addLoginTicket(int userId){
        LoginTicket ticket=new LoginTicket();
        ticket.setUserId(userId);
        Date date=new Date();
        date.setTime(date.getTime()+1000*3600*24);
        ticket.setExpired(date);
        ticket.setStatus(0);//有效状态
        ticket.setTicket(UUID.randomUUID().toString().replace("-",""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }


    public User getUser(int id){
        return userDAO.selectById(id);
    }

    //退出登录的服务
    public void logout(String ticket){
        loginTicketDAO.updateStatus(ticket,1);
    }
}
