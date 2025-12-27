package com.meng.bot.qq.modules;

import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.Network;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.FlashImage;

public class MessageSaver extends BaseModule implements IGroupMessageEvent {

    public MessageSaver(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        FlashImage fi = event.getMessage().get(FlashImage.Key);
        if (fi != null) {
            String url = botWrapper.getUrl(fi.getImage());
            Network.downloadImage(url);
        }
        return false;
    }
}
