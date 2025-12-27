package com.meng.bot.config;

import com.meng.bot.qq.Permission;
import com.meng.tools.normal.JSON;

import java.util.Objects;

public final class Person {

    public String name = "";
    public long qq;
    public int bid = 0;
    public int bLiveRoom = 0;
    public Config config = new Config();
    public Permission permission = Permission.Normal;

    public transient String liveUrl = "";
    public transient boolean lastStatus = false;
    public transient boolean needTip = false;

    private transient int hash = 0;

    public Person() {

    }

    public Person(String name, long qq, int bid) {
        this.name = name;
        this.qq = qq;
        this.bid = bid;
    }

    public boolean hasOwnerPermission() {
        return permission.value() >= Permission.Owner.value();
    }

    public boolean hasMasterPermission() {
        return permission.value() >= Permission.Master.value();
    }

    public boolean hasAdminPermission() {
        return permission.value() >= Permission.Admin.value();
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(name, qq, bid, bLiveRoom);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Person.class) {
            return false;
        }
        if (obj.hashCode() != hashCode()) {
            return false;
        }
        Person p = (Person) obj;
        return qq == p.qq && bid == p.bid && bLiveRoom == p.bLiveRoom && name.equals(p.name);
    }

    @Override
    public String toString() {
        return JSON.toJson(this);
    }

    public static final class Config {

        private static final int qa = 1 << 0;
        private static final int botOn = 1 << 1;

        private int flag = -1;

        public boolean isQaAllowOther() {
            return (flag & qa) != 0;
        }

        public void setQaAllowOther(boolean b) {
            if (b) {
                flag |= qa;
            } else {
                flag &= ~qa;
            }
        }

        public boolean isBotOn() {
            return (flag & botOn) != 0;
        }

        public void setBotOn(boolean b) {
            if (b) {
                flag |= botOn;
            } else {
                flag &= ~botOn;
            }
        }
    }
}
