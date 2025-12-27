package com.meng.bot.qq.modules;

import com.meng.api.ollama.SimpleChatBot;
import com.meng.bot.qq.BotWrapper;
import com.meng.tools.normal.CmdExecuter;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.sjf.SJFPathTool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Audio;

/**
 * @author: 司徒灵羽
 **/

public class MtestMsg extends BaseModule implements IGroupMessageEvent {

    private CmdExecuter ce;
    int step = 0;

    @Override
    public String getModuleName() {
        return "测试模块";
    }

    public MtestMsg(BotWrapper botHelper) {
        super(botHelper);
    }

    private boolean onAI = false;
    private HashMap<Long, SimpleChatBot> chatBotHashMap = new HashMap<>();

    @Override
    public boolean onGroupMessage(final GroupMessageEvent event) {
        long qqId = event.getSender().getId();
        String msg = event.getMessage().contentToString();

        if (qqId == 2856986197L) {
            if (msg.equals("test")) {
                sendMessage(event, "reply");
            }
            if (msg.equals("llm")) {
                onAI = true;
                sendMessage(event, "已启用AI");
                return true;
            }
            if (msg.equals("exit")) {
                onAI = false;
                sendMessage(event, "已停用AI");
                chatBotHashMap.remove(qqId);
                return true;
            }
            if (onAI) {
                SimpleChatBot bot = chatBotHashMap.get(qqId);
                if (bot == null) {
                    bot = new SimpleChatBot("qwen3:4b", qqId);
                    chatBotHashMap.put(qqId, bot);
                }

                try {
                    sendMessage(event, bot.applyMessage(msg));
                } catch (IOException e) {
                    sendMessage(event, e.toString());
                    e.printStackTrace();
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private void processText(GroupMessageEvent event) {
        try {
            File fileMp3 = SJFPathTool.getTTSPath("此生无悔入东方_来世愿生幻想乡.mp3");
            Audio ptt = botWrapper.toAudio(fileMp3, event.getGroup());
            if (ptt == null) {
                sendQuote(event, "生成失败");
            }
            sendQuote(event, ptt);
        } catch (Exception e) {
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
            sendQuote(event, e.toString());
        }
    }
}
