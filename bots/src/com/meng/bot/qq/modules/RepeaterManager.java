package com.meng.bot.qq.modules;

import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.meng.tools.sjf.SJFExecutors;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

/**
 * @Description: 复读机
 * @author: 司徒灵羽
 **/
public class RepeaterManager extends BaseModule implements IGroupMessageEvent {

    private final HashMap<Long, BaseRepeater> repeaters = new HashMap<>();

    public RepeaterManager(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    @CommandDescribe(cmd = "-", note = "复读机")
    public boolean onGroupMessage(GroupMessageEvent event) {
        long groupId = event.getGroup().getId();
        if (!configManager.getGroupConfig(groupId).isFunctionEnabled(Functions.Repeater)) {
            return false;
        }
        BaseRepeater repeater = repeaters.get(groupId);
        if (repeater == null) {
            repeaters.put(groupId, repeater = new ReverseRepeater());
        }
        long sender = event.getSender().getId();
        return repeater.check(event.getGroup(), sender, event.getMessage());
    }

    private static abstract class BaseRepeater {
        private MessageChain lastMsgRecieved = MessageUtils.emptyMessageChain();
        private boolean lastStatus = false;

        private boolean check(Group group, long sender, MessageChain messageChain) {
            boolean flag = false;
            if (!lastStatus && BotWrapper.messageEquals(lastMsgRecieved, messageChain)) {
                flag = repeatStart(group, sender, messageChain);
            } else if (lastStatus && BotWrapper.messageEquals(lastMsgRecieved, messageChain)) {
                flag = repeatRunning(group, sender, messageChain);
            } else if (lastStatus && !BotWrapper.messageEquals(lastMsgRecieved, messageChain)) {
                flag = repeatEnd(group, sender, messageChain);
            }
            lastStatus = BotWrapper.messageEquals(lastMsgRecieved, messageChain);
            lastMsgRecieved = messageChain;
            return flag;
        }

        protected abstract boolean repeatEnd(Group group, long sender, MessageChain messageChain);

        protected abstract boolean repeatRunning(Group group, long sender, MessageChain messageChain);

        protected abstract boolean repeatStart(Group group, long sender, MessageChain messageChain);
    }

    private boolean isPureText(MessageChain messageChain) {
        for (Message msg : messageChain) {
            if (!(msg instanceof PlainText)) {
                return false;
            }
        }
        return true;
    }

    private class SimpleRepeater extends BaseRepeater {

        @Override
        protected boolean repeatEnd(Group group, long sender, MessageChain messageChain) {
            return false;
        }

        @Override
        protected boolean repeatRunning(Group group, long sender, MessageChain messageChain) {
            return false;
        }

        @Override
        protected boolean repeatStart(final Group group, long sender, final MessageChain messageChain) {
            SJFExecutors.execute(new Runnable() {

                @Override
                public void run() {
//                        String text;
//                        if (times % 4 == 0 && isText(messageChain)) {
//                            text = new StringBuilder(messageChain.contentToString()).reverse().toString();
//                            if (text.equals(messageChain.contentToString())) {
//                                text += " ";
//                            }
//                            sendGroupMessage(groupId, text);
//                            return;
//                        }
                    sendMessage(group, messageChain);
                }
            });
            return true;
        }
    }

    private class ReverseRepeater extends BaseRepeater {

        private int times = 0;

        @Override
        protected boolean repeatEnd(Group group, long sender, MessageChain messageChain) {
            return false;
        }

        @Override
        protected boolean repeatRunning(Group group, long sender, MessageChain messageChain) {
            return false;
        }

        @Override
        protected boolean repeatStart(final Group group, final long sender, final MessageChain messageChain) {
            SJFExecutors.execute(new Runnable() {

                @Override
                public void run() {
                    if (times++ % 4 == 0) {
                        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                        for (SingleMessage message : new ArrayList<SingleMessage>(messageChain) {
                            {
                                remove(0);
                                Collections.reverse(this);
                            }
                        }) {
                            if (message instanceof PlainText) {
                                String msg = message.contentToString();
                                String text = new StringBuilder(msg).reverse().toString();
                                messageChainBuilder.append(text.equals(msg) ? " " + text : text);
                            } else if (message instanceof Image) {
                                try {
                                    File imageFile = botWrapper.downloadTempImage((Image) message);
                                    byte[] bytes = moduleManager.getModule(ImageProcess.class).randomTransaction(imageFile);
                                    messageChainBuilder.append(botWrapper.toImage(bytes, group));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    messageChainBuilder.append(message);
                                }
                            } else {
                                messageChainBuilder.append(message);
                            }
                        }
                        sendMessage(group, messageChainBuilder.asMessageChain());
                    } else if (times++ % 5 == 0) {
                        try {
                            File imageFile = botWrapper.getAvatarFile(group.get(sender));
                            byte[] bytes = moduleManager.getModule(ImageProcess.class).randomTransaction(imageFile);
                            sendMessage(group, botWrapper.toImage(bytes, group));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sendMessage(group, messageChain);
                    }
                }
            });
            return true;
        }
    }
}

