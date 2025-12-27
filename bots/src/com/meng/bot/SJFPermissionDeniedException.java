package com.meng.bot;

import net.mamoe.mirai.event.events.GroupMessageEvent;

public class SJFPermissionDeniedException extends RuntimeException {
    public GroupMessageEvent event;

    public SJFPermissionDeniedException(GroupMessageEvent event) {
        this.event = event;
    }
    
}
