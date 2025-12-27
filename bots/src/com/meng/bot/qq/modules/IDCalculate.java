package com.meng.bot.qq.modules;

import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.friend.IFriendMessageEvent;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.TextLexer;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class IDCalculate extends BaseModule implements IFriendMessageEvent, IGroupMessageEvent {

    private final int[] magics = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    public IDCalculate(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public String getModuleName() {
        return "检验计算";
    }

    @Override
    public boolean onFriendMessage(FriendMessageEvent event) {
        String result = calculate(event.getMessage().contentToString());
        if (result == null) {
            return false;
        }
        event.getSender().sendMessage(result);
        return true;
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        String result = calculate(event.getMessage().contentToString());
        if (result == null) {
            return false;
        }
        sendQuote(event, result);
        return true;
    }

    private String calculate(String msg) {
        if (msg.length() != 17) {
            return null;
        }
        char[] chars = msg.toCharArray();
        if (!TextLexer.isNumber(chars)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(msg);
        int sum = 0;
        for (int i = 0; i < chars.length; ++i) {
            sum += (chars[i] - '0') * magics[i];
        }
        int tmp = sum % 11;
        int finalResult = (12 - tmp) % 11;
        builder.append(finalResult == 10 ? "X" : finalResult);
        return builder.toString();
    }
}
