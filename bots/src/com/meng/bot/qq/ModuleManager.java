package com.meng.bot.qq;

import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.ConfigManager;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.modules.UserInfoManager;
import com.meng.bot.qq.handler.friend.IFriendChangeEvent;
import com.meng.bot.qq.handler.friend.IFriendEvent;
import com.meng.bot.qq.handler.friend.IFriendMessageEvent;
import com.meng.bot.qq.handler.group.*;
import com.meng.bot.qq.modules.*;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.normal.TimeTask;
import net.mamoe.mirai.event.events.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 模块管理器
 * @author: 司徒灵羽
 **/

public class ModuleManager extends BaseModule implements IGroupEvent, INudgeEvent, IGroupRecallEvent, IFriendEvent {

    private final List<IGroupBotEvent> groupBotHandlers = new ArrayList<>();
    private final List<IGroupMessageEvent> groupMsgHandlers = new ArrayList<>();
    private final List<IGroupSettingEvent> groupSettingsHandlers = new ArrayList<>();
    private final List<IGroupMemberEvent> groupMemberHandlers = new ArrayList<>();
    private final List<INudgeEvent> nudgeHanderlers = new ArrayList<>();
    private final List<IGroupRecallEvent> groupRecallHandler = new ArrayList<>();
    private final List<IFriendMessageEvent> friendMsgHandlers = new ArrayList<>();
    private final List<IFriendChangeEvent> friendChangeHandlers = new ArrayList<>();

    private final List<BaseModule> all = new ArrayList<>();

    private final Map<String, Object> hotFix = new HashMap<>();

    public ModuleManager() {
    }

    public void setBotHelper(BotWrapper botWrapper) {
        this.botWrapper = botWrapper;
        botMessageHandler = botWrapper.getBotMessageHandler();
        configManager = botWrapper.getConfigManager();
        load();
    }

    public ModuleManager load() {
        load(ReflexCommand.class);
        load(MessageSaver.class);
        load(MtestMsg.class);
        load(ChatCounter.class);

        load(AdminMessage.class);

        load(MessageRefuse.class);

        // load(MusicRecongnition.class);
        load(BlackHistory.class);
        load(SetuManager.class);
        load(Report.class);

        load(IDCalculate.class);
        load(MiraiCodeSerialize.class);
        load(ImageProcess.class);
        load(ApiCaller.class);
        load(EventsHandlers.class);
        load(NumberProcess.class);
        load(Dice.class);
        load(CoinsManager.class);
        load(AimMessage.class);
        load(QuestionAndAnswer.class);
        load(WordsStock.class);
        //load(Sentence.class);
        load(RepeaterManager.class);
        return this;
    }


    public void hotfix(Object module) {
        hotFix.put(module.getClass().getName(), module);
    }

    public Object hotfixCancel(String className) {
        return hotFix.remove(className);
    }

    public <T extends BaseModule> T load(Class<T> cls) {
        try {
            Constructor<T> cos = cls.getDeclaredConstructor(BotWrapper.class);
            T module = cos.newInstance(botWrapper);
            all.add(module);
            if (module instanceof IGroupBotEvent) {
                groupBotHandlers.add((IGroupBotEvent) module);
            }
            if (module instanceof IGroupMessageEvent) {
                groupMsgHandlers.add((IGroupMessageEvent) module);
            }
            if (module instanceof IGroupSettingEvent) {
                groupSettingsHandlers.add((IGroupSettingEvent) module);
            }
            if (module instanceof IGroupMemberEvent) {
                groupMemberHandlers.add((IGroupMemberEvent) module);
            }
            if (module instanceof INudgeEvent) {
                nudgeHanderlers.add((INudgeEvent) module);
            }
            if (module instanceof IGroupRecallEvent) {
                groupRecallHandler.add((IGroupRecallEvent) module);
            }
            if (module instanceof IFriendMessageEvent) {
                friendMsgHandlers.add((IFriendMessageEvent) module);
            }
            if (module instanceof IFriendChangeEvent) {
                friendChangeHandlers.add((IFriendChangeEvent) module);
            }
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFunction() {
        StringBuilder builder = new StringBuilder();
        for (IGroupMessageEvent event : groupMsgHandlers) {
            Method method;
            try {
                method = event.getClass().getMethod("onGroupMessage", GroupMessageEvent.class);
            } catch (Exception e) {
                continue;
            }
            if (method.isAnnotationPresent(CommandDescribe.class)) {
                CommandDescribe cd = method.getAnnotation(CommandDescribe.class);
                builder.append(cd.cmd()).append(":").append(cd.note()).append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        long groupId = event.getGroup().getId();
        if (botWrapper.debug) {
            if (groupId != 666247478 && groupId != 927682440) {
                return true;
            }
        }
        for (IGroupMessageEvent m : groupMsgHandlers) {
            String name = m.getClass().getName();
            if (hotFix.containsKey(name)) {
                Object module = hotFix.get(name);
                if (module instanceof IGroupMessageEvent ie && ie.onGroupMessage(event)) {
                    return true;
                }
            } else if (m.onGroupMessage(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNudge(NudgeEvent event) {
        if (!configManager.getGroupConfig(event.getSubject().getId())
                .isFunctionEnabled(Functions.GroupMessageEvent)) {
            return false;
        }
        return nudgeHanderlers.stream().anyMatch(h -> h.onNudge(event));
    }

    @Override
    public boolean onGroupRecall(MessageRecallEvent.GroupRecall event) {
        if (!configManager.getGroupConfig(event.getGroup().getId())
                .isFunctionEnabled(Functions.GroupMessageEvent)) {
            return false;
        }
        return groupRecallHandler.stream().anyMatch(h -> h.onGroupRecall(event));
    }

    @Override
    public boolean onFriendMessage(FriendMessageEvent event) {
        return friendMsgHandlers.stream().anyMatch(h -> h.onFriendMessage(event));
    }

    @Override
    public boolean onFriendAdd(FriendAddEvent event) {
        return friendChangeHandlers.stream().anyMatch(h -> h.onFriendAdd(event));
    }

    @Override
    public boolean onRequestAddFriend(NewFriendRequestEvent event) {
        return friendChangeHandlers.stream().anyMatch(h -> h.onRequestAddFriend(event));
    }

    @Override
    public boolean onFriendDelete(FriendDeleteEvent event) {
        return friendChangeHandlers.stream().anyMatch(h -> h.onFriendDelete(event));
    }

    @Override
    public boolean onBotLeave(BotLeaveEvent event) {
        return groupBotHandlers.stream().anyMatch(h -> h.onBotLeave(event));
    }

    @Override
    public boolean onBotPermissionChange(BotGroupPermissionChangeEvent event) {
        return groupBotHandlers.stream().anyMatch(h -> h.onBotPermissionChange(event));
    }

    @Override
    public boolean onBotMute(BotMuteEvent event) {
        return groupBotHandlers.stream().anyMatch(h -> h.onBotMute(event));
    }

    @Override
    public boolean onBotUnmute(BotUnmuteEvent event) {
        return groupBotHandlers.stream().anyMatch(h -> h.onBotUnmute(event));
    }

    @Override
    public boolean onBotJoinGroup(BotJoinGroupEvent event) {
        return groupBotHandlers.stream().anyMatch(h -> h.onBotJoinGroup(event));
    }

    @Override
    public boolean onGroupSettingChange(GroupSettingChangeEvent event) {
        return groupSettingsHandlers.stream().anyMatch(h -> h.onGroupSettingChange(event));
    }

    @Override
    public boolean onGroupNameChange(GroupNameChangeEvent event) {
        return groupSettingsHandlers.stream().anyMatch(h -> h.onGroupNameChange(event));
    }

    @Override
    public boolean onGroupMuteAll(GroupMuteAllEvent event) {
        return groupSettingsHandlers.stream().anyMatch(h -> h.onGroupMuteAll(event));
    }

    @Override
    public boolean onGroupAllowAnonymousChange(GroupAllowAnonymousChatEvent event) {
        return groupSettingsHandlers.stream().anyMatch(h -> h.onGroupAllowAnonymousChange(event));
    }

    @Override
    public boolean onGroupAllowConfessTalkChange(GroupAllowConfessTalkEvent event) {
        return groupSettingsHandlers.stream().anyMatch(h -> h.onGroupAllowConfessTalkChange(event));
    }

    @Override
    public boolean onAllowInviteChange(GroupAllowMemberInviteEvent event) {
        return groupSettingsHandlers.stream().anyMatch(h -> h.onAllowInviteChange(event));
    }

    @Override
    public boolean onMemberJoinRequest(MemberJoinRequestEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onMemberJoinRequest(event));
    }

    @Override
    public boolean onMemberJoin(MemberJoinEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onMemberJoin(event));
    }

    @Override
    public boolean onMemberLeave(MemberLeaveEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onMemberLeave(event));
    }

    @Override
    public boolean onInviteBot(BotInvitedJoinGroupRequestEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onInviteBot(event));
    }

    @Override
    public boolean onCardChange(MemberCardChangeEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onCardChange(event));
    }

    @Override
    public boolean onTitleChange(MemberSpecialTitleChangeEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onTitleChange(event));
    }

    @Override
    public boolean onPermissionChange(MemberPermissionChangeEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onPermissionChange(event));
    }

    @Override
    public boolean onMemberMute(MemberMuteEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onMemberMute(event));
    }

    @Override
    public boolean onMemberUnmute(MemberUnmuteEvent event) {
        return groupMemberHandlers.stream().anyMatch(h -> h.onMemberUnmute(event));
    }

    public List<? extends BaseModule> getAllModules() {
        return all;
    }

    @SuppressWarnings("unchecked")
    public BaseModule getModule(String className) {
        try {
            return (BaseModule) getModule(Class.forName(className));
        } catch (ClassNotFoundException e) {
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getModule(Class<T> t) {
        if (t.getName().equals(BotMessageHandler.class.getName())) {
            return (T) botMessageHandler;
        } else if (t.getName().equals(ConfigManager.class.getName())) {
            return (T) configManager;
        } else if (t.getName().equals(UserInfoManager.class.getName())) {
            return (T) UserInfoManager.getInstance();
        } else if (t.getName().equals(TimeTask.class.getName())) {
            return (T) TimeTask.getInstance();
        }
        Object module = hotFix.get(t.getName());
        if (module != null) {
            return (T) module;
        }
        for (Object modules : all) {
            if (modules.getClass().getName().equals(t.getName())) {
                return (T) modules;
            }
        }
        return null;
    }
}
