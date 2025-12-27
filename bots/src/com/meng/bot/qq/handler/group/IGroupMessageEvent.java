package com.meng.bot.qq.handler.group;

import net.mamoe.mirai.event.events.GroupMessageEvent;

/**
 * @author: 清梦
 **/
public interface IGroupMessageEvent {
    public boolean onGroupMessage(GroupMessageEvent event);
}
