package com.meng.bot.qq.handler.group;
import net.mamoe.mirai.event.events.NudgeEvent;

public interface INudgeEvent {
    public boolean onNudge(NudgeEvent event);  
}
