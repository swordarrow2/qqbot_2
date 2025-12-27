package com.meng.bot.qq.modules;

import com.meng.api.touhou.thsss.replay.ThsssReplay;
import com.meng.api.touhou.thsss.replay.ThsssReplayAnalyzer;
import com.meng.api.touhou.zun.replay.ReplayDecoder;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMemberEvent;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.bot.qq.handler.group.IGroupRecallEvent;
import com.meng.tools.normal.*;
import com.meng.tools.sjf.SJFPathTool;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EventsHandlers extends BaseModule implements IGroupMessageEvent, IGroupRecallEvent, IGroupMemberEvent {

    public EventsHandlers(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        FileMessage msg = event.getMessage().get(FileMessage.Key);
        if (msg == null) {
            return false;
        }
        if (msg.getName().endsWith(".rpy")) {
            File touhouReplay = SJFPathTool.getReplayPath(System.currentTimeMillis() + msg.getName());
            byte[] fileBytes;
            try {
                fileBytes = Network.httpGetRaw(msg.toAbsoluteFile(event.getGroup()).getUrl());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                return false;
            }
            try {
                FileTool.saveFile(touhouReplay, fileBytes);
            } catch (IOException ignore) {
                return false;
            }
            onRpyFile(event.getGroup(), touhouReplay);
        }
        return false;
    }

    private void onRpyFile(Group group, File touhouReplay) {
        try {
            FileFormat.Content fc = FileFormat.getFileType(touhouReplay);
            if (fc.describe == FileFormat.FileType.rpy_thsss_replay) {
                ThsssReplay rpy = new ThsssReplay().load(touhouReplay);
                sendMessage(group, rpy.toString());
                botWrapper.upLoadGroupFile(group, ThsssReplayAnalyzer.analyze(rpy).getBytes(StandardCharsets.UTF_8), "/thsss_replay_keys.txt");
            } else {
                sendMessage(group, new ReplayDecoder().read(touhouReplay).toString());
            }
        } catch (IOException e) {
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
        }
    }

    @Override
    public boolean onGroupRecall(MessageRecallEvent.GroupRecall event) {
        MessageSource source = botMessageHandler.getSource(event);
        if (source == null) {
            return true;
        }
        MessageChain originalMessage = source.getOriginalMessage();
        sendGroupMessage(event.getGroup().getId(), new PlainText(String.valueOf(event.getOperator().getId())).plus("撤回了:"));
        sendGroupMessage(event.getGroup().getId(), originalMessage);

        Image img = originalMessage.get(Image.Key);

        if (img != null) {
            String url = botWrapper.getUrl(img);
            try {
                byte[] fileBytes = Network.httpGetRaw(url);
                File file = SJFPathTool.getRecallPath(Hash.getMd5Instance().calculate(fileBytes) + "." + FileFormat.getFileType(fileBytes));
                FileTool.saveFile(file, fileBytes);
            } catch (Exception e) {
                ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
            }
        }
        FlashImage fi = originalMessage.get(FlashImage.Key);
        if (fi != null) {
            String url = botWrapper.getUrl(fi.getImage());
            Network.downloadImage(url);
        }
        return false;
    }

    @Override
    public boolean onMemberJoinRequest(MemberJoinRequestEvent event) {
        sendGroupMessage(event.getGroupId(), "有人申请加群,管理员快看看吧");
        return false;
    }

    @Override
    public boolean onMemberJoin(MemberJoinEvent event) {
        long groupId = event.getGroup().getId();
        String welc = configManager.getWelcome(groupId);
        sendGroupMessage(groupId, welc == null ? "欢迎新人" : welc);
        return false;
    }

    @Override
    public boolean onMemberLeave(MemberLeaveEvent event) {
        sendGroupMessage(event.getGroup().getId(), event.getMember().getId() + "离开了");
        return false;
    }

    @Override
    public boolean onInviteBot(BotInvitedJoinGroupRequestEvent event) {
        return false;
    }

    @Override
    public boolean onCardChange(MemberCardChangeEvent event) {
        return false;
    }

    @Override
    public boolean onTitleChange(MemberSpecialTitleChangeEvent event) {
        return false;
    }

    @Override
    public boolean onPermissionChange(MemberPermissionChangeEvent event) {
        return false;
    }

    @Override
    public boolean onMemberMute(MemberMuteEvent event) {
        return false;
    }

    @Override
    public boolean onMemberUnmute(MemberUnmuteEvent event) {
        return false;
    }

}
