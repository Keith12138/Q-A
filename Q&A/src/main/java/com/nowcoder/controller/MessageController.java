package com.nowcoder.controller;

import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hys on 2017/12/19.
 */
@Controller
public class MessageController {
    @Autowired
    HostHolder hostHolder;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;


    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);


    @RequestMapping(path={"/msg/list"},method = {RequestMethod.GET})
    public String getConversationList(Model model){
        try {
            if(hostHolder.getUser()==null){
                return "redirect:/reglogin";
            }
            int localUserId=hostHolder.getUser().getId();
            List<ViewObject> conversations=new ArrayList<>();
            List<Message> conversationList=messageService.getConversationList(localUserId,0,10);
            for(Message msg :conversationList){
                ViewObject vo=new ViewObject();
                vo.set("conversation",msg);
                int targetId=msg.getFromId()==localUserId?msg.getToId():msg.getFromId();//获取消息对象的id
                User user=userService.getUser(targetId);
                vo.set("user",user);
                vo.set("unread",messageService.getConversationUnreadCount(localUserId,msg.getConversationId()));
                conversations.add(vo);
            }
            model.addAttribute("conversations",conversations);
        }catch (Exception e){
            logger.error("获取站内信列表失败"+e.getMessage());
        }

        return "letter";
    }



    @RequestMapping(path={"/msg/detail"},method = {RequestMethod.GET})
    public String getConversationDetail(Model model,@RequestParam("conversationId")String conversationId){
        try{
            List<Message> conversationList=messageService.getConversationDetail(conversationId,0,10);
            List<ViewObject> messages=new ArrayList<>();
            for(Message msg :conversationList){
                ViewObject vo=new ViewObject();
                vo.set("message",msg);
                User user=userService.getUser(msg.getFromId());
                if(user==null){
                    continue;
                }

                //改变消息的状态，由未读变成已读
                if(hostHolder.getUser().getId()==msg.getToId()){
                    messageService.updateHasRead(msg.getToId(),conversationId,1);
                }

                vo.set("user",user);
                vo.set("headUrl",user.getHeadUrl());
                vo.set("userId",user.getId());
                messages.add(vo);
            }
            model.addAttribute("messages",messages);
        }catch (Exception e){
            logger.error("获取详情失败"+e.getMessage());
        }
        return "letterDetail";
    }




    //发站内信消息的入口
    @RequestMapping(path={"/msg/addMessage"},method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName")String toName,
                             @RequestParam("content") String content){
        try {
            if(hostHolder.getUser()==null){
                return WendaUtil.getJSONString(999,"未登录");
            }
            User user=userService.selectByName(toName);
            if(user==null){
                return WendaUtil.getJSONString(1,"用户不存在");
            }

            Message message=new Message();
            message.setCreatedDate(new Date());
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            message.setContent(content);
            messageService.addMessage(message);

            return WendaUtil.getJSONString(0);

        }catch (Exception e){
            logger.error("发送消息失败"+e.getMessage());
            return WendaUtil.getJSONString(1,"发送消息失败");
        }
    }

}
