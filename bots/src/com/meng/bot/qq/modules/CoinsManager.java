package com.meng.bot.qq.modules;

import com.meng.api.touhou.THGameDataManager;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.command.SecondaryCommand;
import com.meng.bot.qq.commonModules.UserInfoManager;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.normal.TextLexer;

import java.util.ArrayList;
import java.util.Iterator;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import com.meng.bot.config.Person;

public class CoinsManager extends BaseModule implements IGroupMessageEvent {

    public CoinsManager(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public String getModuleName() {
        return "Dice";
    }

    @Override
    @CommandDescribe(cmd = "coinsManager", note = "管理硬币")
    public boolean onGroupMessage(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        if (msg.length() < 2) {
            return false;
        }
        long groupId = event.getGroup().getId();
        if (!configManager.getGroupConfig(groupId).isFunctionEnabled(Functions.Dice)) {
            return false;
        }
        long qqId = event.getSender().getId();
        UserInfoManager uf = UserInfoManager.getInstance();
        ArrayList<String> list = TextLexer.analyze(msg);
        Iterator<String> iterator = list.iterator();
        try {
            SecondaryCommand command = Command.coinsManager.getSecondaryCommand(iterator.next());
            if (command == null) {
                return false;
            }
            switch (command) {
                case sign -> {
                    if (UserInfoManager.getInstance().onSign(qqId)) {
                        int day = uf.getContinuousSignedDays(qqId);
                        String result = String.format("签到成功,获得%d个硬币(基础:10,连续签到:%d)", 10 + day, day);
                        sendMessage(event.getGroup(), result);
                    } else {
                        sendMessage(event.getGroup(), "你今天已经签到过啦");
                    }
                    return true;
                }
                case getCoins -> {
                    sendMessage(event, "你一共拥有" + uf.getCoins(qqId) + "个硬币");
                    return true;
                }
                case addCoins -> {
                    Person p = configManager.getPersonFromQQ(qqId);
                    if (p == null) {
                        return false;
                    }
                    if (!p.hasMasterPermission()) {
                        return false;
                    }
                    int count = 0;
                    if (iterator.hasNext()) {
                        try {
                            count = Integer.parseInt(iterator.next());
                        } catch (NumberFormatException e) {
                            sendMessage(event, "需要指定正确的数量");
                            return true;
                        }
                    }
                    long target = botWrapper.getAt(event.getMessage());
                    if (target == -1 && iterator.hasNext()) {
                        try {
                            target = Long.parseLong(iterator.next());
                        } catch (NumberFormatException ignore) {
                        }
                    }
                    if (target == -1) {
                        sendMessage(event, "需指定正确的用户");
                        return true;
                    }
                    UserInfoManager.getInstance().addCoins(target, count);
                    sendMessage(event, "已为" + target + "添加了" + count + "个硬币");
                    return true;
                }
            }
            return true;
        } catch (Exception e) {
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
            sendMessage(event.getGroup(), "参数错误:" + e.toString());
        }
        return false;
    }
}
