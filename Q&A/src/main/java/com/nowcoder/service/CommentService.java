package com.nowcoder.service;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by hys on 2017/12/19.
 */
@Service
public class CommentService {
    @Autowired
    CommentDAO commentDAO;

    @Autowired
    SensitiveService sensitiveService;

    public List<Comment> getCommentsByEntity(int entityId,int entityType){
        return commentDAO.selectCommentByEntity(entityId,entityType);
    }

    public int addComment(Comment comment){
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//html标签过滤
        comment.setContent(sensitiveService.filter(comment.getContent()));//敏感词过滤

        return  commentDAO.addComment(comment)>0? comment.getId():0;
    }

    public int getCommentcount(int entityId,int entityType){
        return commentDAO.getCommentCount(entityId, entityType);
    }

    public void deleteComment(int entityId,int entityType){
        commentDAO.updateStatus(entityId,entityType,1);
    }


}
