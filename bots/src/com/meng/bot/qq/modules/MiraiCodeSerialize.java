package com.meng.bot.qq.modules;

import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;

import java.util.HashSet;

public class MiraiCodeSerialize extends BaseModule implements IGroupMessageEvent {

    private HashSet<Long> ready = new HashSet<>();

    public MiraiCodeSerialize(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public String getModuleName() {
        return "mirai码";
    }

    @Override
    @CommandDescribe(cmd = "rcode/tcode mirai码", note = "收发mirai码")
    public boolean onGroupMessage(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        long qq = event.getSender().getId();
        if (msg.equalsIgnoreCase("mtt")) {
            ready.add(qq);
            sendQuote(event, "等待中……");
            return true;
        } else if (ready.contains(qq)) {
            sendMessage(event.getGroup(), event.getMessage().serializeToMiraiCode());
            ready.remove(qq);
            return true;
        } else if (msg.startsWith("ttm")) {
            sendMessage(event.getGroup(), MiraiCode.deserializeMiraiCode(event.getMessage().get(1).toString().substring(6)));
            return true;
        }
        return false;
    }
}
