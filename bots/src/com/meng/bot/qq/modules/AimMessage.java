package com.meng.bot.qq.modules;

import com.meng.bot.annotation.BotData;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: 司徒灵羽
 **/
public class AimMessage extends BaseModule implements IGroupMessageEvent {
    @BotData("AimMessage.json")
    public AimMessageHolder holder = new AimMessageHolder();

    public AimMessage(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent gme) {
        long qqId = gme.getSender().getId();
        long groupId = gme.getGroup().getId();
        if (!holder.delayList.isEmpty()) {
            Iterator<MessageWait> iter = holder.delayList.iterator();
            while (iter.hasNext()) {
                MessageWait mw = iter.next();
                if (mw.qq == qqId) {
                    if (mw.group == -1) {
                        sendGroupMessage(groupId, MiraiCode.deserializeMiraiCode(mw.content));
                        iter.remove();
                    } else if (mw.group == groupId) {
                        sendGroupMessage(groupId, MiraiCode.deserializeMiraiCode(mw.content));
                        iter.remove();
                    }
                }
            }
            save();
        }
        MessageWait mw = holder.delayMap.remove(qqId);
        if (mw != null) {
            if (mw.qq == qqId) {
                if (mw.group == -1) {
                    sendGroupMessage(groupId, MiraiCode.deserializeMiraiCode(mw.content));
                } else if (mw.group == groupId) {
                    sendGroupMessage(groupId, MiraiCode.deserializeMiraiCode(mw.content));
                }
                save();
            }
        }
        return false;
    }

    public void addTip(long InGroup, long toQQ, String msg) {
        holder.delayList.add(new MessageWait(InGroup, toQQ, msg));
        save();
    }

    public void addTip(long toQQ, String msg) {
        addTip(-1, toQQ, msg);
    }

    public void addTipSingleton(long InGroup, long toQQ, String msg) {
        holder.delayMap.put(toQQ, new MessageWait(InGroup, toQQ, msg));
        save();
    }

    public void addTipSingleton(long toQQ, String msg) {
        addTipSingleton(-1, toQQ, msg);
    }

    public static class AimMessageHolder {
        public ArrayList<MessageWait> delayList = new ArrayList<>();
        public ConcurrentHashMap<Long, MessageWait> delayMap = new ConcurrentHashMap<>();
    }

    private class MessageWait {
        public final long group;
        public final long qq;
        public final String content;

        public MessageWait(long inGroup, long toQQ, String msg) {
            group = inGroup;
            qq = toQQ;
            content = msg;
        }
    }
}
