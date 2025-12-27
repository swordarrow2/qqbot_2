package com.meng.bot.qq.modules;

import com.meng.api.LkaaApi;
import com.meng.api.touhou.THGameDataManager;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.QQGroupConfig;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.TextLexer;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ApiCaller extends BaseModule implements IGroupMessageEvent {
    private List<File> setus = null;

    public ApiCaller(BotWrapper botHelper) {
        super(botHelper);
    }

    @CommandDescribe(cmd = "-", note = "多个调用器的缝合,用于调用代码在其他类的方法")
    @Override
    public boolean onGroupMessage(final GroupMessageEvent event) {
        QQGroupConfig groupConfig = configManager.getGroupConfig(event.getGroup());
        String message = event.getMessage().contentToString();

        if (message.charAt(0) != '.') {
            return false;
        }
        ArrayList<String> analyze = TextLexer.analyze(message);
        Iterator<String> iterator = analyze.iterator();
        iterator.next();
        Command command = Command.getCommand(iterator.next());
        if (command == null) {
            return false;
        }
        switch (command) {
            case noteUser:
                try {
                    long at = Long.parseLong(iterator.next()); // botHelper.getAt(event.getMessage());
                    if (at == -1) {
                        return true;
                    }
                    moduleManager.getModule(AimMessage.class).addTip(at, iterator.next());
                    sendMessage(event, "已添加");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case textToSpeech:
                try {
                    if (analyze.size() == 3) {
                        sendMessage(event, botWrapper.toAudio(LkaaApi.generalVoice(iterator.next()), event.getGroup()));
                    } else if (analyze.size() == 4) {
                        sendGroupMessage(Long.parseLong(iterator.next()), botWrapper.toAudio(LkaaApi.generalVoice(iterator.next()), event.getGroup()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case translate:
                String next = iterator.next();
                String translate = THGameDataManager.generalTranslate(next);
                sendMessage(event, translate == null ? "null" : translate);
                return true;


            default:
                return false;
        }
    }

}
