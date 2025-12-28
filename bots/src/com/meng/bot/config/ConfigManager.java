package com.meng.bot.config;

/**
 * @Description: 配置管理器
 * @author: 司徒灵羽
 **/

import com.meng.bot.annotation.BotData;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.Permission;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.Stranger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ConfigManager extends BaseModule {
    @BotData("ConfigManager.json")
    protected ConfigHolder configHolder = new ConfigHolder();

    public ConfigManager() {
    }

    public void setBotHelper(BotWrapper botHelper) {
        this.botWrapper = botHelper;
        this.moduleManager = botHelper.getModuleManager();
        load();
    }

    @Override
    public BaseModule load() {
        DataPersistenter.read(this);
        for (Person p : configHolder.person) {
            if (configHolder.admins.contains(p.qq)) {
                p.permission = Permission.Admin;
            }
            if (configHolder.masters.contains(p.qq)) {
                p.permission = Permission.Master;
            }
            if (configHolder.owner.contains(p.qq)) {
                p.permission = Permission.Owner;
            }
            switch (p.permission) {
                case Owner:
                    configHolder.owner.add(p.qq);
                case Master:
                    configHolder.masters.add(p.qq);
                case Admin:
                    configHolder.admins.add(p.qq);
                case Normal:
            }
        }
//        for (QQGroupConfig config : configHolder.groupCfgs.values()) {
//            config.setFunctionEnable(Functions.Repeater);
//        }
//        QQGroupConfig g1 = getGroupConfig(719324487);
//        if (g1 != null) {
//            g1.setFunctionEnable(Functions.Aphorism);
//        }
//        QQGroupConfig g2 = getGroupConfig(451195420);
//        if (g2 != null) {
//            g2.setFunctionEnable(Functions.Aphorism);
//        }
        save();
        return this;
    }

    @Override
    public BaseModule reload() {
        return load();
    }

    public QQGroupConfig getGroupConfig(Group group) {
        return getGroupConfig(group.getId());
    }

    public QQGroupConfig getGroupConfig(long group) {
        QQGroupConfig g = configHolder.groupCfgs.get(group);
        if (g == null) {
            g = new QQGroupConfig();
            g.init();
            configHolder.groupCfgs.put(group, g);
        }
        return g;
    }

    public void addBlockQQ(long qq) {
        configHolder.blockOnlyQQ.add(qq);
        save();
    }

    public void removeBlockQQ(long qq) {
        configHolder.blockOnlyQQ.remove(qq);
        save();
    }

    public boolean isBlockQQ(long qq) {
        return configHolder.blockOnlyQQ.contains(qq) || configHolder.blackQQ.contains(qq);
    }

    public boolean isBlockOnlyQQ(long qq) {
        return configHolder.blockOnlyQQ.contains(qq);
    }

    public void addBlackQQ(long qq) {
        configHolder.blackQQ.add(qq);
        save();
    }

    public void removeBlackQQ(long q) {
        configHolder.blackQQ.remove(q);
        save();
    }

    public boolean isBlackQQ(long qq) {
        return configHolder.blackQQ.contains(qq);
    }

    public void addBlackGroup(long group) {
        configHolder.blackGroup.add(group);
        save();
    }

    public void removeBlackGroup(long g) {
        configHolder.blackGroup.remove(g);
        save();
    }

    public boolean isBlackGroup(long qq) {
        return configHolder.blackGroup.contains(qq);
    }

    public void addBlockWord(String str) {
        configHolder.blockWord.add(str);
        save();
    }

    public void removeBlockWord(String str) {
        configHolder.blockWord.remove(str);
        save();
    }

    public boolean isBlockWord(String word) {
        for (String nrw : configHolder.blockWord) {
            if (word.contains(nrw)) {
                return true;
            }
        }
        return false;
    }

    public void add(Person pi) {
        configHolder.person.add(pi);
        save();
    }

    public Set<Person> getPersons() {
        return Collections.unmodifiableSet(configHolder.person);
    }

    public void remove(Person pi) {
        configHolder.person.remove(pi);
        save();
    }

    public Person getPersonFromQQ(long qq) {
        for (Person pi : configHolder.person) {
            if (pi.qq == qq) {
                return pi;
            }
        }
        return null;
    }

    public Person getPersonFromName(String name) {
        for (Person pi : configHolder.person) {
            if (pi.name.equals(name)) {
                return pi;
            }
        }
        return null;
    }

    public Person getPersonFromBid(long bid) {
        for (Person pi : configHolder.person) {
            if (pi.bid == bid) {
                return pi;
            }
        }
        return null;
    }

    public Person getPersonFromLiveId(long lid) {
        for (Person pi : configHolder.person) {
            if (pi.bLiveRoom == lid) {
                return pi;
            }
        }
        return null;
    }

    public void setWelcome(long fromGroup, String content) {
        if (content == null) {
            configHolder.welcomeMap.remove(fromGroup);
        } else {
            configHolder.welcomeMap.put(fromGroup, content);
        }
        save();
    }

    public String getWelcome(long fromGroup) {
        return configHolder.welcomeMap.get(fromGroup);
    }

    public Set<Person> getPersonbyCondition(Function<Person, Boolean> pms) {
        Set<Person> persons = new HashSet<Person>();
        for (Person p : configHolder.person) {
            if (pms.apply(p)) {
                persons.add(p);
            }
        }
        return persons;
    }

    public void setNickName(long qq, String nickname) {
        if (nickname != null) {
            configHolder.nicknameMap.put(qq, nickname);
        } else {
            configHolder.nicknameMap.remove(qq);
        }
        save();
    }

    public String getNickName(long group, long qq) {
        String nick = configHolder.nicknameMap.get(qq);
        if (nick != null) {
            return nick;
        }
        Person pi = getPersonFromQQ(qq);
        if (pi != null) {
            return pi.name;
        }
        NormalMember groupMember = botWrapper.getGroupMember(group, qq);
        if (groupMember != null) {
            return groupMember.getNameCard();
        }
        Stranger stranger = botWrapper.getStranger(qq);
        if (stranger != null) {
            return stranger.getNick();
        }
        return String.valueOf(qq);
    }

    @Override
    public String getModuleName() {
        return "ConfigManager";
    }

    public void addOtherBot(long toAdd) {
        configHolder.otherBots.add(toAdd);
    }

    public static class ConfigHolder {

        public HashMap<Long, QQGroupConfig> groupCfgs = new HashMap<>();
        public HashSet<Long> blockOnlyQQ = new HashSet<>();
        public HashSet<Long> blackQQ = new HashSet<>();
        public HashSet<Long> blackGroup = new HashSet<>();
        public HashSet<String> blockWord = new HashSet<>();

        public HashSet<Long> owner = new HashSet<>();
        public HashSet<Long> masters = new HashSet<>();
        public HashSet<Long> admins = new HashSet<>();
        public HashSet<Person> person = new HashSet<>();
        public HashSet<Long> otherBots = new HashSet<>();

        public HashMap<Long, String> nicknameMap = new HashMap<>();
        public HashMap<Long, String> welcomeMap = new HashMap<>();
    }
}
