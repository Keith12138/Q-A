package com.nowcoder.service;

import com.nowcoder.controller.LoginController;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hys on 2017/12/18.
 */

//敏感词过滤服务(DFA算法)
@Service
public class SensitiveService implements InitializingBean{

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    //重载的InitializingBean中的方法，在初始化的时候就会调用该方法生成敏感词库
    @Override
    public void afterPropertiesSet() throws Exception {
        try{
            InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader read=new InputStreamReader(is);
            BufferedReader bufferedReader=new BufferedReader(read);
            String lineTxt;
            while((lineTxt=bufferedReader.readLine())!=null){
                addWord(lineTxt.trim());
            }
            read.close();
        }catch (Exception e){
            logger.error("读取敏感词文件失败"+e.getMessage());
        }
    }


    //增加关键词
    private void addWord(String lineTxt){
        TrieNode tempNode=rootNode;
        for(int i=0;i<lineTxt.length();++i){
            Character c=lineTxt.charAt(i);

            //过滤掉特殊字符
            if(isSymbol(c)){
                continue;
            }
            TrieNode node=tempNode.getSubNode(c);

            if(node==null){
                node=new TrieNode();
                tempNode.addSubNode(c,node);
            }
            tempNode=node;
            if(i==lineTxt.length()-1){
                tempNode.setkeywordEnd(true);
            }
        }
    }

    private class TrieNode{
        //是不是关键词的结尾
        private boolean end=false;


        //当前节点下所有的子节点
        private Map<Character,TrieNode> subNodes=new HashMap<Character,TrieNode>();

        public void addSubNode(Character key,TrieNode node){
            subNodes.put(key,node);
        }

        TrieNode getSubNode(Character key){
            return subNodes.get(key);
        }

        boolean isKeyWordEnd(){
            return end;
        }

        void setkeywordEnd(boolean end){
            this.end=end;
        }
    }


    private TrieNode rootNode=new TrieNode();


    //判断字符是不是一些非法字符，以便过滤
    private boolean isSymbol(char c){
        int ic=(int) c;
        //东亚文字：0x2E80--0x9FFFF;
        return !CharUtils.isAsciiAlphanumeric(c)&&(ic<0x2E80||ic>0x9FFFF);
    }

    //过滤文本内容的方法
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return text;
        }

        StringBuilder result=new StringBuilder();
        String replacement="***";
        TrieNode tempNode= rootNode;
        int begin=0;
        int position=0;

        //一次查找内容中的敏感词
        while (position<text.length()){
            char c=text.charAt(position);

            //判断是不是一些非法的迷惑性字符,比如空格
            if(isSymbol(c)){
                if(tempNode==rootNode){
                    result.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }

            tempNode=tempNode.getSubNode(c);

            if(tempNode==null){
                result.append(text.charAt(begin));
                position=begin+1;
                begin=position;
                tempNode=rootNode;
            }else if (tempNode.isKeyWordEnd()){
                //发现敏感词
                result.append(replacement);
                position=position+1;
                begin=position;
                tempNode=rootNode;
            }else {
                ++position;
            }
        }
        result.append(text.substring(begin));
        return result.toString();
    }


    //测试主函数
    public static void main(String[] args){
        SensitiveService s=new SensitiveService();
        s.addWord("色情");
        s.addWord("赌博");
        System.out.print(s.filter("hi   你好 色 @ 情"));
    }


}
