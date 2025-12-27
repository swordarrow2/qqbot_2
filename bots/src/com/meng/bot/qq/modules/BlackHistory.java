package com.meng.bot.qq.modules;

import com.meng.bot.config.Functions;
import com.meng.bot.config.Person;
import com.meng.bot.config.QQGroupConfig;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.FileTool;
import com.meng.tools.normal.Network;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import net.mamoe.mirai.message.data.MessageChain;

public class BlackHistory extends StepCommandProcessor<Long> implements IGroupMessageEvent {

    public BlackHistory(BotWrapper b) {
        super(b);
    }

    @Override
    protected boolean preJudge(GroupMessageEvent event) {
        QQGroupConfig config = configManager.getGroupConfig(event.getGroup().getId());
        return config == null || !config.isFunctionEnabled(Functions.Aphorism);//TODO check function bug
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        if (super.onGroupMessage(event)) {
            return true;
        }
        String msg = event.getMessage().contentToString();
        if (!msg.startsWith("添加") && msg.endsWith("迫害图")) {
            long target = botWrapper.getAt(event.getMessage());
            String substring = msg.substring(0, msg.length() - 3);
            if (target != -1) {
                substring = target + "";
            }
            if (substring.equals("其他")) {
                substring = "-2";
            }
            File[] listFiles = SJFPathTool.getPlaneSentencePath(substring).listFiles();
            if (listFiles.length == 0) {
                return false;
            }
            sendMessage(event, botWrapper.toImage(SJFRandom.randomSelect(listFiles), event.getGroup()));
            return true;
        }
        final long qq = event.getSender().getId();
        Person personFromQQ = configManager.getPersonFromQQ(qq);
        if (personFromQQ == null || !personFromQQ.hasAdminPermission()) {
            return false;
        }
        if (msg.equals("添加迫害图")) {
            StepRunnable<Long> stepRunnable = new StepRunnable<>();
            addOnAction(qq, stepRunnable);
            stepRunnable.addActions(new BiConsumer<GroupMessageEvent, StepRunnable<Long>>() {
                @Override
                public void accept(GroupMessageEvent event1, StepRunnable<Long> p2) {
                    moduleManager.getModule(MessageRefuse.class).registUnblock(qq);
                    sendQuote(event1, "发送qq号码或at或\"其他\"以选择是谁的图\n");
                }
            }, new BiConsumer<GroupMessageEvent, StepRunnable<Long>>() {

                @Override
                public void accept(GroupMessageEvent event, StepRunnable runnable) {
                    MessageChain message = event.getMessage();
                    long target = botWrapper.getAt(message);
                    if (message.contentToString().equals("其他")) {
                        target = -2;
                    }
                    if (target == -1) {
                        try {
                            target = Long.parseLong(event.getMessage().contentToString());
                        } catch (NumberFormatException e) {
                            sendQuote(event, e.toString());
                            cancel(event);
                            return;
                        }
                    }
                    sendQuote(event, "发送图片为[" + (target != -2 ? target + "" : "其他") + "]添加图片,或发送取消添加以退出");
                    runnable.extra = target;
                }
            });
            stepRunnable.setLoopPoint();
            stepRunnable.addActions(new BiConsumer<GroupMessageEvent, StepRunnable<Long>>() {

                @Override
                public void accept(GroupMessageEvent event, StepRunnable runnable) {
                    int leng = 0;
                    for (Message message : event.getMessage()) {
                        if (message instanceof Image) {
                            byte[] img;
                            try {
                                img = Network.httpGetRaw(botWrapper.getUrl(((Image) message)));
                                FileTool.saveFile(SJFPathTool.getPlaneSentencePath((!runnable.extra.equals(-2L) ? runnable.extra : "其他") + "/" + FileTool.getAutoFileName(img)), img);
                            } catch (IOException e) {
                                sendQuote(event, e.toString());
                                e.printStackTrace();
                                continue;
                            }
                            leng += img.length;
                        }
                    }
                    if (leng == 0) {
                        cancel(event);
                    } else {
                        sendQuote(event, "保存完成(" + (leng / 1024) + "KB)");
                        runnable.gotoLoopPoint();
                    }
                }
            });
            stepRunnable.run(event);
            return true;
        } else if (msg.equals("取消添加")) {
            if (steps.containsKey(qq)) {
                moduleManager.getModule(MessageRefuse.class).registNormalBlock(qq);
                cancel(event);
            }
        }
        return false;
    }
}
