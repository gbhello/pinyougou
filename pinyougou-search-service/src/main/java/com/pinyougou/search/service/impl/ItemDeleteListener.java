package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsId = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener监听到消息..."+goodsId);
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsId));
            System.out.println("成功删除索引中的记录");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
