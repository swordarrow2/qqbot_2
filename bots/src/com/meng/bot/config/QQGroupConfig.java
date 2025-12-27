package com.meng.bot.config;

import java.util.EnumSet;
import java.util.HashSet;

/**
 * @author: 司徒灵羽
 **/
public class QQGroupConfig {
    public EnumSet<Functions> enabled = EnumSet.noneOf(Functions.class);
    public HashSet<String> tags = new HashSet<>();

    public void init() {
//        enabled.remove(Functions.GroupMessageEvent);
//        enabled.remove(Functions.DynamicWordStock);
//        enabled.remove(Functions.MessageRecallEvent_GroupRecall);
    }

    public QQGroupConfig() {
    }

    public void setFunctionEnable(Functions function) {
        enabled.add(function);
    }

    public void setFunctionDisable(Functions function) {
        enabled.remove(function);
    }

    public boolean isFunctionEnabled(Functions function) {
        return enabled.contains(function);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public boolean removeTag(String tag) {
        if (!tags.contains(tag)) {
            return false;
        }
        return tags.remove(tag);
    }
}
