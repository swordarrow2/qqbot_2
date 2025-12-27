package com.meng.bot.qq;

import com.meng.bot.config.ConfigManager;
import com.meng.bot.config.DataPersistenter;
import com.meng.tools.sjf.SJFRandom;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: 司徒灵羽
 **/
public abstract class BaseModule {

    protected BotMessageHandler botMessageHandler;
    protected BotWrapper botWrapper;
    protected ConfigManager configManager;
    protected ModuleManager moduleManager;

    public BaseModule() {
    }

    public BaseModule(BotWrapper botWrapper) {
        this.botWrapper = botWrapper;
        botMessageHandler = botWrapper.getBotMessageHandler();
        configManager = botWrapper.getConfigManager();
        moduleManager = botWrapper.getModuleManager();
        load();
    }

    public void save() {
        DataPersistenter.save(this);
    }

    public BaseModule load() {
        DataPersistenter.read(this);
        return this;
    }

    public BaseModule reload() {
        System.out.println(getModuleName() + " reload...");
        return load();
    }

    public final MessageSource sendMessage(GroupMessageEvent event, Message msg) {
        return sendMessage(event.getGroup(), msg);
    }

    public final MessageSource sendMessage(GroupMessageEvent event, String msg) {
        return sendMessage(event.getGroup(), msg);
    }

    public final MessageSource sendGroupMessage(long fromGroup, Message msg) {
        return botMessageHandler.sendGroupMessage(fromGroup, msg);
    }

    public final MessageSource sendGroupMessage(long fromGroup, String msg) {
        return botMessageHandler.sendGroupMessage(fromGroup, new PlainText(msg));
    }

    public final MessageSource sendGroupMessage(long fromGroup, String[] msg) {
        return botMessageHandler.sendGroupMessage(fromGroup, SJFRandom.randomSelect(msg));
    }

    public final MessageSource sendGroupMessage(long fromGroup, ArrayList<String> msg) {
        return botMessageHandler.sendGroupMessage(fromGroup, msg.toArray(new String[0]));
    }

    public final MessageSource sendMessage(Group group, String msg) {
        return botMessageHandler.sendGroupMessage(group.getId(), msg);
    }

    public final MessageSource sendMessage(Group group, Message msg) {
        return botMessageHandler.sendGroupMessage(group.getId(), msg);
    }

    public final MessageSource sendMessage(Group group, String... msg) {
        return botMessageHandler.sendGroupMessage(group.getId(), SJFRandom.randomSelect(msg));
    }

    public final MessageSource sendMessage(Group group, List<String> msg) {
        return botMessageHandler.sendGroupMessage(group.getId(), msg.toArray(new String[0]));
    }

    public final MessageSource sendQuote(GroupMessageEvent gme, String msg) {
        return botMessageHandler.sendGroupMessage(gme.getGroup().getId(), new QuoteReply(gme.getSource()).plus(msg));
    }

    public final MessageSource sendQuote(GroupMessageEvent gme, Message msg) {
        return botMessageHandler.sendGroupMessage(gme.getGroup().getId(), new QuoteReply(gme.getSource()).plus(msg));
    }

    public String getModuleName() {
        return getClass().getSimpleName();
    }

}
