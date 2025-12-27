package com.meng.bot.qq.modules;

import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
import com.meng.bot.config.Person;
import com.meng.bot.config.QQGroupConfig;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.sjf.SJFExecutors;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;

import com.meng.bot.qq.handler.group.INudgeEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.MessageUtils;

/**
 * @author: 司徒灵羽
 **/
public class MessageRefuse extends BaseModule implements IGroupMessageEvent, INudgeEvent {

    public ConcurrentHashMap<Long, FireWallBean> msgMap = new ConcurrentHashMap<>();
    public HashSet<Long> unblocks = new HashSet<>();

    public MessageRefuse(BotWrapper botHelper) {
        super(botHelper);
    }

    public void registUnblock(long qq) {
        unblocks.add(qq);
    }

    public void registNormalBlock(long qq) {
        unblocks.remove(qq);
    }

    @Override
    public MessageRefuse load() {
        SJFExecutors.executeAtFixedRate(new Runnable() {

            @Override
            public void run() {
                for (FireWallBean mb : msgMap.values()) {
                    mb.lastSeconedMsgs = 0;
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
        return this;
    }

    @Override
    public boolean onNudge(NudgeEvent event) {
        long qqId = event.getFrom().getId();
        if (configManager.isBlackQQ(qqId)) {
            return true;
        }
        if (configManager.isBlockQQ(qqId)) {
            return true;
        }
        return false;
    }

    @Override
    @CommandDescribe(cmd = "-", note = "消息防护")
    public boolean onGroupMessage(GroupMessageEvent event) {
        long qqId = event.getSender().getId();
        long groupId = event.getGroup().getId();
        String msg = event.getMessage().contentToString();
        if (event.getSender().getId() == 3045126546L) {//过于吵闹的刷屏机器
            return true;
        }
        if (msg.startsWith(".bot")) {
            QQGroupConfig cfg = configManager.getGroupConfig(groupId);
            Person personFromQQ = configManager.getPersonFromQQ(qqId);
            if (qqId == botWrapper.getId() || (personFromQQ != null && personFromQQ.hasAdminPermission()) || botWrapper.getGroup(groupId).get(qqId).getPermission().getLevel() > 0) {
                if (msg.equals(".bot on")) {
                    cfg.setFunctionEnable(Functions.GroupMessageEvent);
                    sendQuote(event, "已启用本群响应");
                } else if (msg.equals(".bot off")) {
                    sendQuote(event, "已停用本群响应");
                    cfg.setFunctionDisable(Functions.GroupMessageEvent);
                }
                configManager.save();
                return true;
            }
        }
        if (!configManager.getGroupConfig(groupId).isFunctionEnabled(Functions.GroupMessageEvent)) {
            return true;
        }
        if (configManager.isBlackQQ(qqId)) {
            return true;
        }
        if (configManager.isBlockQQ(qqId) || configManager.isBlockWord(event.getMessage().contentToString())) {
            return true;
        }
        if (unblocks.contains(qqId)) {
            return false;
        }
        FireWallBean mtmb = msgMap.get(qqId);
        if (mtmb == null) {
            mtmb = new FireWallBean();
            msgMap.put(qqId, mtmb);
        }
        //发言间隔过短
        if (System.currentTimeMillis() - mtmb.lastSpeakTimeStamp < 500) {
            ++mtmb.timeSubLowTimes;
        } else {
            mtmb.timeSubLowTimes = 0;
        }
        if (mtmb.timeSubLowTimes > 5) {
            if (!mtmb.tiped) {
                mtmb.tiped = true;
                sendGroupMessage(groupId, "你说话真快");
            }
            return true;
        }
        //重复次数过多
        mtmb.lastSpeakTimeStamp = System.currentTimeMillis();
        if (BotWrapper.messageEquals(mtmb.lastMsg, event.getMessage())) {
            ++mtmb.repeatTime;
        } else {
            mtmb.repeatTime = 0;
        }
        if (mtmb.repeatTime > 5) {
            if (!mtmb.tiped) {
                mtmb.tiped = true;
                sendGroupMessage(groupId, "怎么又是这句话");
            }
            mtmb.lastMsg = event.getMessage();
            return true;
        }
        mtmb.lastMsg = event.getMessage();
        //一秒内消息过多
        ++mtmb.lastSeconedMsgs;
        if (mtmb.lastSeconedMsgs > 4) {
            if (!mtmb.tiped) {
                mtmb.tiped = true;
                sendGroupMessage(groupId, "你真稳");
            }
            return true;
        }
        mtmb.tiped = false;
        return false;
    }

    private class FireWallBean {
        public long qq;//qq
        public long lastSpeakTimeStamp;//最后一次发言时间
        public long timeSubLowTimes;//最后两次发言时间差过短次数
        public int repeatTime;//同一句话重复次数
        public int lastSeconedMsgs;//一秒内消息数量
        public MessageChain lastMsg = MessageUtils.emptyMessageChain();//最后一句话
        public boolean tiped = false;//刷屏提示
    }
}

