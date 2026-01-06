package com.meng.bot.qq.modules;

import com.meng.bot.Main;
import com.meng.bot.config.Functions;
import com.meng.bot.config.Person;
import com.meng.bot.config.QQGroupConfig;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.bot.qq.hotfix.HotfixClassLoader;
import com.meng.bot.qq.hotfix.SJFCompiler;
import com.meng.tools.normal.*;
import com.meng.tools.sjf.SJFExecutors;
import com.meng.tools.sjf.SJFPathTool;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.QuoteReply;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @Description: 管理员命令
 * @author: 司徒灵羽
 **/
public class AdminMessage extends BaseModule implements IGroupMessageEvent {

    public AdminMessage(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public String getModuleName() {
        return "管理指令";
    }

    @Override
//    @CommandDescribe(cmd = "AdminMessage", note = "主要给管理员用的指令")
    public boolean onGroupMessage(GroupMessageEvent event) {
        long qq = event.getSender().getId();
        long groupId = event.getGroup().getId();
        String msg = event.getMessage().contentToString();
        if (msg.charAt(0) != '.') {
            return false;
        }
        ArrayList<String> list = TextLexer.analyze(msg);
        Iterator<String> iter = list.iterator();
        iter.next();//.
        try {
            String first = iter.next();
            Command command = Command.getCommand(first);
            if (command == null) {
                return false;
            }
            if (command == Command.help) {
//                sendMessage(event.getGroup(), moduleManager.getFunction());
                sendMessage(event.getGroup(), "SJF bot v" + Main.VERSION);
                return true;
            }
            Person person = configManager.getPersonFromQQ(qq);
            //below bot admin and group admin forbid
            if ((person == null || !person.hasAdminPermission()) && botWrapper.getGroupMember(groupId, qq).getPermission().getLevel() == 0) {
                sendQuote(event, "权限不足");
                return false;
            }
            QuoteReply quoteReply = event.getMessage().get(QuoteReply.Key);
            if (quoteReply != null) {
                switch (command) {
                    case deleteCache -> {
                        File cache = SJFPathTool.getCachePath("bh3_main_cache.html");
                        cache.delete();
                        return true;
                    }
                    case recallMessageOnQuote -> {
                        botMessageHandler.autoRecall(quoteReply.getSource(), 1);
                        return true;
                    }
                    case saveOnQuote -> {
                        GroupMessageEvent quotedEvent = (GroupMessageEvent) botMessageHandler.getEvent(quoteReply.getSource());
                        Image miraiImg = quotedEvent.getMessage().get(Image.Key);
                        if (miraiImg == null) {
                            botMessageHandler.sendQuote(event, "保存失败");
                            return true;
                        }
                        String url = botWrapper.getUrl(miraiImg);
                        Network.downloadImage(url);
                        return true;
                    }
                }
            }
            switch (command) {
                case downloadPixivImage -> {
                    String id = iter.next();
                    File pixivImage = SJFPathTool.getPixivPath(id + ".png");
                    if (pixivImage.exists()) {
                        sendQuote(event, botWrapper.toImage(pixivImage, event.getGroup()));
                        return true;
                    }
                    byte[] result = Network.httpGetRaw("https://www.pixiv.cat/" + id + ".png");
                    if (result == null) {
                        sendQuote(event, "出现错误,id中有多张图片或不存在");
                    } else {
                        FileTool.saveFile(pixivImage, result);
                        sendQuote(event, botWrapper.toImage(pixivImage, event.getGroup()));
                    }
                    return true;
                }
                case exitGroup -> {
                    event.getGroup().quit();
                    return true;
                }
                case addUser -> {
                    try {
                        String qqStr = iter.next();
                        if (!qqStr.matches("\\d{5,11}")) {
                            sendQuote(event, "格式不正确");
                            return true;
                        }
                        long toAdd = Long.parseLong(qqStr);
                        if (toAdd < 10000 || toAdd > 9999999999L) {
                            sendQuote(event, "范围不正确");
                            return true;
                        }
                        Person newUser = configManager.getPersonFromQQ(toAdd);
                        if (newUser != null) {
                            sendQuote(event, "已存在，名称：" + newUser.name);
                            return true;
                        }
                        if (!iter.hasNext()) {
                            sendQuote(event, "参数错误，正确格式：addUser [qq] [name]");
                            return true;
                        }
                        newUser = new Person();
                        newUser.qq = toAdd;
                        newUser.name = iter.next();
                        if (iter.hasNext()) {
                            sendQuote(event, "参数错误，正确格式：addUser [qq] [name]");
                            return true;
                        }
                        configManager.addPerson(newUser);
                        configManager.save();
                        sendMessage(event.getGroup(), String.format("已将%d加入列表", toAdd));
                        return true;
                    } catch (Exception e) {
                        sendMessage(event.getGroup(), e.toString());
                    }
                }
                case addBotToBotList -> {
                    long toAdd = botWrapper.getAt(event.getMessage());
                    if (toAdd == -1) {
                        toAdd = Long.parseLong(iter.next());
                    }
                    configManager.addOtherBot(toAdd);
                    configManager.save();
                    sendMessage(event.getGroup(), String.format("已将%d加入bot列表", toAdd));
                    return true;
                }
                case findUserInBotGroups -> {
                    long parseLong = Long.parseLong(iter.next());
                    Set<Group> groups = botWrapper.findQQInAllGroup(parseLong);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(parseLong).append("在这些群中出现");
                    for (Group l : groups) {
                        stringBuilder.append("\n").append(l.getId()).append(l.getName());
                    }
                    botMessageHandler.sendMessage(event.getGroup(), stringBuilder.toString());
                    return true;
                }
                case welcomeSentence -> {
                    String wel = null;
                    if (iter.hasNext()) {
                        wel = iter.next();
                    }
                    configManager.setWelcome(groupId, wel);
                    configManager.save();
                    sendGroupMessage(groupId, "已设置为:" + wel);
                    return true;
                }
                case setGroupName -> event.getGroup().setName(iter.next());
                case setGroupCard -> {
                    if (list.size() == 3) {
                        botWrapper.setGroupCard(groupId, botWrapper.getId(), iter.next());
                    } else if (list.size() == 4) {
                        botWrapper.setGroupCard(groupId, Long.parseLong(iter.next()), iter.next());
                    }
                    return true;
                }
                case showSwitches -> {
                    if (list.size() == 2) {
                        StringBuilder sb = new StringBuilder("当前有:\n");
                        for (Functions function : Functions.values()) {
                            sb.append(function.getName()).append("\n");
                        }
                        sb.setLength(sb.length() - 1);
                        sendGroupMessage(event.getGroup().getId(), sb.toString());
                    } else if (list.size() == 3) {
                        Functions function = Functions.get(iter.next());
                        if (function != null) {
                            QQGroupConfig gcfg = configManager.getGroupConfig(groupId);
                            if (gcfg.isFunctionEnabled(function)) {
                                gcfg.setFunctionDisable(function);
                            } else {
                                gcfg.setFunctionEnable(function);
                            }
                            sendQuote(event, (gcfg.isFunctionEnabled(function) ? "已启用" : "已停用") + function);
                        } else {
                            sendQuote(event, "无此开关");
                        }
                        configManager.save();
                    }
                    return true;
                }
            }
            //below bot admin and group master forbid
            if ((person == null || !person.hasAdminPermission()) && botWrapper.getGroupMember(groupId, qq).getPermission().getLevel() != 2) {
                return false;
            }
            //TODO
            //below bot master forbid
            if (person == null || !person.hasMasterPermission()) {
                return false;
            }
            switch (command) {
                case broadcastInBotGroups -> {
                    String broadcast = iter.next();
                    HashSet<Group> hs = new HashSet<>();
                    Collection<Group> glist = botWrapper.getGroups();
                    for (Group g : glist) {
                        if (!configManager.getGroupConfig(g).isFunctionEnabled(Functions.GroupMessageEvent)) {
                            continue;
                        }
                        sendGroupMessage(g.getId(), broadcast);
                        hs.add(g);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    StringBuilder result = new StringBuilder("在以下群发送了广播:");
                    for (Group g : hs) {
                        result.append("\n").append(g.getId()).append(":").append(g.getName());
                    }
                    sendGroupMessage(groupId, result.toString());
                    return true;
                }
                case turnOffMain -> {
                    sendQuote(event, "disabled");
                    botWrapper.sleeping = true;
                    return true;
                }
                case turnOnMain -> {
                    botWrapper.sleeping = false;
                    sendQuote(event, "enabled");
                    return true;
                }
                case findPersonInfoInConfigJson -> {
                    String name = iter.next();
                    HashSet<Person> hashSet = new HashSet<>();
                    for (Person personInfo : configManager.getPersons()) {
                        if (personInfo.name != null && personInfo.name.contains(name)) {
                            hashSet.add(personInfo);
                        }
                        if (personInfo.qq != 0 && Long.toString(personInfo.qq).contains(name)) {
                            hashSet.add(personInfo);
                        }
                        if (personInfo.bid != 0 && String.valueOf(personInfo.bid).contains(name)) {
                            hashSet.add(personInfo);
                        }
                        if (personInfo.bLiveRoom != 0 && String.valueOf(personInfo.bLiveRoom).contains(name)) {
                            hashSet.add(personInfo);
                        }
                    }
                    sendGroupMessage(groupId, JSON.toJson(hashSet));
                    return true;
                }
                case threadInfo -> {
                    String s = "taskCount：" + SJFExecutors.getTaskCount() + "\n" +
                            "completedTaskCount：" + SJFExecutors.getCompletedTaskCount() + "\n" +
                            "largestPoolSize：" + SJFExecutors.getLargestPoolSize() + "\n" +
                            "poolSize：" + SJFExecutors.getPoolSize() + "\n" +
                            "activeCount：" + SJFExecutors.getActiveCount();
                    sendGroupMessage(groupId, s);
                    return true;
                }
                case jvmGc -> {
                    System.gc();
                    sendGroupMessage(groupId, "gc start");
                    return true;
                }
                case resend -> {
                    if (list.size() == 3) {
                        sendGroupMessage(groupId, iter.next());
                    } else if (list.size() == 4) {
                        sendGroupMessage(Long.parseLong(iter.next()), iter.next());
                    }
                    return true;
                }
                case setUserSpecialTitleInGroup -> {
                    botWrapper.setGroupSpecialTitle(groupId, Long.parseLong(iter.next()), iter.next());
                    return true;
                }
                case blockUser -> {
                    StringBuilder sb = new StringBuilder("屏蔽列表添加:");
                    while (iter.hasNext()) {
                        String nextqq = iter.next();
                        configManager.addBlackQQ(Long.parseLong(nextqq));
                        sb.append(nextqq).append(" ");
                        configManager.save();
                    }
                    sendGroupMessage(groupId, sb.toString());
                    return true;
                }
                case blackUser -> {
                    StringBuilder sb = new StringBuilder("屏蔽列表添加:");
                    while (iter.hasNext()) {
                        String nextqq = iter.next();
                        configManager.addBlackQQ(Long.parseLong(nextqq));
                        sb.append(nextqq).append(" ");
                    }
                    configManager.save();
                    sendGroupMessage(groupId, sb.toString());
                    return true;
                }
                case kickUserFromGroup -> {
                    long target = botWrapper.getAt(event.getMessage());
                    if (target == -1) {
                        target = Long.parseLong(iter.next());
                    }
                    NormalMember targetMember = botWrapper.getGroupMember(groupId, target);
                    if (targetMember == null) {
                        sendGroupMessage(groupId, "未找到该成员:" + target);
                        return true;
                    }
                    if (iter.hasNext()) {
                        targetMember.kick(iter.next());
                    } else {
                        targetMember.kick("");
                    }
                    return true;
                }
                case muteUser -> {
                    long muteTarget = botWrapper.getAt(event.getMessage());
                    if (muteTarget == -1) {
                        muteTarget = Long.parseLong(iter.next());
                    }
                    Member targetMember = botWrapper.getGroupMember(groupId, muteTarget);
                    if (targetMember != null) {
                        targetMember.mute(Integer.parseInt(iter.next()));
                    } else {
                        sendGroupMessage(groupId, "未找到该成员:" + muteTarget);
                    }
                    return true;
                }
            }
            //only owner
            if (!person.hasOwnerPermission()) {
                return false;
            }
            switch (command) {
                case hotFix -> {
                    String nane = iter.next();
                    String code = msg.substring(msg.indexOf(" ", 8));
                    HotfixClassLoader clsLd = HotfixClassLoader.getInstance();
                    SJFCompiler.generate(botWrapper, clsLd, nane, code);
                    Class<?> nClass = clsLd.loadClass(nane);
                    Constructor<?> constructor = nClass.getDeclaredConstructor(BotWrapper.class);
                    Object module = constructor.newInstance(botWrapper);
                    try {
                        Method methodLoad = nClass.getMethod("load");
                        methodLoad.invoke(module);
                    } catch (NoSuchMethodException e) {
                        sendMessage(event.getGroup(), e.toString());
                        return true;
                    }
                    moduleManager.hotfix(module);
                    sendMessage(event.getGroup(), nane + " loaded");
                    return true;
                }
                case hotfixCancel -> {
                    Object obj = moduleManager.hotfixCancel(iter.next());
                    sendQuote(event, obj != null ? "canceled" : "cancel failed");
                    return true;
                }
                case openAllSwitch -> {
                    Functions function = Functions.get(iter.next());
                    if (function == null) {
                        sendQuote(event, "无此开关");
                        return true;
                    }
                    for (Group group : botWrapper.getGroups()) {
                        configManager.getGroupConfig(group).setFunctionEnable(function);
                    }
                    sendQuote(event, function + "已启用");
                    return true;
                }
            }
        } catch (Exception e) {
            ExceptionCatcher.getInstance().catchException(botWrapper, e);
            sendGroupMessage(groupId, "参数错误:" + e);
        }
        return false;
    }
}
