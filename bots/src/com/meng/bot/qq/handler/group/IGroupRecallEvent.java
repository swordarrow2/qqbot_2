package com.meng.bot.qq.handler.group;

import net.mamoe.mirai.event.events.MessageRecallEvent;

public interface IGroupRecallEvent {
    public boolean onGroupRecall(MessageRecallEvent.GroupRecall event);
}
