package com.meng.bot.qq;

import com.meng.bot.config.ConfigManager;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.modules.ChatCounter;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.sjf.SJFExecutors;
import com.meng.tools.sjf.SJFRandom;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class BotMessageHandler extends SimpleListenerHost {

    private BotWrapper botWrapper;
    private ModuleManager moduleManager;
    private ConfigManager configManager;

    private LinkedList<MessageSource> sourceList = new LinkedList<>();
    private LinkedList<MessageEvent> msgEvents = new LinkedList<>();


    public BotMessageHandler() {
        SJFExecutors.executeAtFixedRate(() -> {
            sourceList.removeIf(messageSource -> (int) (System.currentTimeMillis() / 1000) - messageSource.getTime() > 1800);
            msgEvents.removeIf(messageEvent -> (int) (System.currentTimeMillis() / 1000) - messageEvent.getTime() > 1800);
        }, 2, 2, TimeUnit.MINUTES);
    }

    public void setBotWrapper(BotWrapper botWrapper) {
        this.botWrapper = botWrapper;
        this.moduleManager = botWrapper.getModuleManager();
        this.configManager = botWrapper.getConfigManager();
    }

    //群消息
    @NotNull
    @EventHandler()
    public ListeningStatus onReceive(final GroupMessageEvent event) {
        //    if(qqId == 1418780411L){
        //        throw new FishPoolGroupMasterAndProgrammerAndRbqLjyysEveryDayGenshinImpactAndMineSraftNotWriteThpthWeNeedFuckHerException("ljyys");
        //    }
        putMessage(event);
        moduleManager.onGroupMessage(event);
        return ListeningStatus.LISTENING;
    }

    //群消息同步
    @NotNull
    @EventHandler()
    public ListeningStatus onReceive(GroupMessageSyncEvent event) {
        GroupMessageEvent groupMessageEvent = new GroupMessageEvent(event.getSenderName(), event.getSender().getPermission(), event.getSender(), event.getMessage(), event.getTime());
        onReceive(groupMessageEvent);
        return ListeningStatus.LISTENING;
    }

    //好友消息
    @NotNull
    @EventHandler()
    public ListeningStatus onReceive(FriendMessageEvent event) {
        moduleManager.onFriendMessage(event);
        return ListeningStatus.LISTENING;
    }

    //群撤回: GroupRecall
    @NotNull
    @EventHandler
    public ListeningStatus onReceive(MessageRecallEvent.GroupRecall event) {
        if (! configManager.getGroupConfig(event.getGroup()).isFunctionEnabled(Functions.MessageRecallEvent_GroupRecall)) {
            return ListeningStatus.LISTENING;
        }
        moduleManager.onGroupRecall(event);
        return ListeningStatus.LISTENING;
    }

    @NotNull
    @EventHandler
    public ListeningStatus onReceive(BotLeaveEvent event) {
        return ListeningStatus.LISTENING;
    }

    //机器人被禁言: BotMuteEvent
    @NotNull
    @EventHandler
    public ListeningStatus onReceive(BotMuteEvent event) {
        if (! configManager.getGroupConfig(event.getGroup()).isFunctionEnabled(Functions.BotMuteEvent)) {
            return ListeningStatus.LISTENING;
        }
        long id = event.getGroup().getId();
        long id2 = event.getOperator().getId();
//        configManager.addBlack(id, id2);
//        event.getGroup().quit();
        sendGroupMessage(BotWrapper.yysGroup, String.format("在群%d中被%d禁言", id, id2));
        return ListeningStatus.LISTENING;
    }

    //成员已经加入群: MemberJoinEvent
    @NotNull
    @EventHandler
    public ListeningStatus onReceive(MemberJoinEvent event) {
        if (!configManager.getGroupConfig(event.getGroup()).isFunctionEnabled(Functions.MemberJoinEvent)) {
            return ListeningStatus.LISTENING;
        }
        moduleManager.onMemberJoin(event);
        return ListeningStatus.LISTENING;
    }

    //成员已经离开群: MemberLeaveEvent
    @NotNull
    @EventHandler
    public ListeningStatus onReceive(MemberLeaveEvent event) {
        if (!configManager.getGroupConfig(event.getGroup()).isFunctionEnabled(Functions.MemberLeaveEvent)) {
            return ListeningStatus.LISTENING;
        }
        moduleManager.onMemberLeave(event);
        return ListeningStatus.LISTENING;
    }

    //机器人被邀请加入群: BotInvitedJoinGroupRequestEvent
    @NotNull
    @EventHandler
    public ListeningStatus onReceive(BotInvitedJoinGroupRequestEvent event) {
        if (configManager.isBlackQQ(event.getInvitorId()) || configManager.isBlackGroup(event.getGroupId())) {
            event.ignore();
        } else {
            event.accept();
            moduleManager.getModule(ChatCounter.class).addGroup(event.getGroupId());
        }
        return ListeningStatus.LISTENING;
    }

    //一个账号请求添加机器人为好友: NewFriendRequestEvent
    @NotNull
    @EventHandler
    public ListeningStatus onReceive(NewFriendRequestEvent event) {
        event.accept();
        return ListeningStatus.LISTENING;
    }

    @NotNull
    @EventHandler
    public ListeningStatus onReceive(NudgeEvent event) {
        moduleManager.onNudge(event);
        return ListeningStatus.LISTENING;
    }

    public MessageSource sendGroupMessage(long groupId, Message msg) {
        if (botWrapper.sleeping || !configManager.getGroupConfig(groupId).isFunctionEnabled(Functions.GroupMessageEvent)) {
            return null;
        }
        MessageReceipt mr = botWrapper.getGroup(groupId).sendMessage(msg);
        putMessage(mr);
        moduleManager.getModule(ChatCounter.class).add(groupId, botWrapper.getId(), new MessageChainBuilder().append(msg).asMessageChain());
        return mr.getSource();
    }

    public MessageSource sendGroupMessage(long fromGroup, String msg) {
        return sendGroupMessage(fromGroup, new PlainText(msg));
    }

    public MessageSource sendGroupMessage(long fromGroup, String[] msg) {
        return sendGroupMessage(fromGroup, SJFRandom.randomSelect(msg));
    }

    public MessageSource sendGroupMessage(long fromGroup, ArrayList<String> msg) {
        return sendGroupMessage(fromGroup, msg.toArray(new String[0]));
    }

    public MessageSource sendMessage(Group group, String msg) {
        return sendGroupMessage(group.getId(), msg);
    }

    public MessageSource sendMessage(Group group, Message msg) {
        return sendGroupMessage(group.getId(), msg);
    }

    public MessageSource sendMessage(Group group, String[] msg) {
        return sendGroupMessage(group.getId(), SJFRandom.randomSelect(msg));
    }

    public MessageSource sendMessage(Group group, ArrayList<String> msg) {
        return sendGroupMessage(group.getId(), msg.toArray(new String[0]));
    }

    public MessageSource sendQuote(GroupMessageEvent gme, String msg) {
        return sendGroupMessage(gme.getGroup().getId(), new QuoteReply(gme.getSource()).plus(msg));
    }

    public MessageSource sendQuote(GroupMessageEvent gme, Message msg) {
        return sendGroupMessage(gme.getGroup().getId(), new QuoteReply(gme.getSource()).plus(msg));
    }

    @Override
    public void handleException(CoroutineContext context, Throwable exception) {
        ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), exception);
    }

    public void putMessage(MessageReceipt msgEvent) {
        sourceList.add(msgEvent.getSource());
        if (sourceList.size() > 1000) {
            sourceList.removeFirst();
        }
    }

    public void putMessage(MessageEvent msgEvent) {
        msgEvents.add(msgEvent);
        if (msgEvents.size() > 1000) {
            msgEvents.removeFirst();
        }
        sourceList.add(msgEvent.getSource());
        if (sourceList.size() > 1000) {
            sourceList.removeFirst();
        }
    }

    public MessageSource getSource(MessageRecallEvent event) {
        return getSource(event.getMessageIds());
    }

    public MessageSource getSource(int[] ids) {
        for (MessageSource source : sourceList) {
            if (arrayEquals(source.getIds(), ids)) {
                return source;
            }
        }
        return null;
    }


    public MessageEvent getEvent(MessageRecallEvent event) {
        return getEvent(event.getMessageIds());
    }

    public MessageEvent getEvent(int[] ids) {
        for (MessageEvent event : msgEvents) {
            if (arrayEquals(event.getSource().getIds(), ids)) {
                return event;
            }
        }
        return null;
    }

    public MessageEvent getEvent(MessageSource source) {
        return getEvent(source.getIds());
    }

    public void autoRecall(MessageSource source, int second) {
        MessageSource.recallIn(source, second * 1000);
    }

    public void autoRecall(MessageSource source) {
        MessageSource.recallIn(source, 60000);
    }

    public void autoRecall(int[] msgIds, int second) {
        MessageSource.recallIn(getSource(msgIds), second * 1000);
    }

    public void autoRecall(int[] msgIds) {
        MessageSource.recallIn(getSource(msgIds), 60000);
    }

    private boolean arrayEquals(int[] a1, int[] a2) {
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; ++i) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }
}
