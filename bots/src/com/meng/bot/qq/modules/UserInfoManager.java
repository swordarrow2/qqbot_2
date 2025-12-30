package com.meng.bot.qq.modules;

import com.google.gson.annotations.SerializedName;
import com.meng.bot.annotation.BotData;
import com.meng.bot.qq.BaseModule;

import java.util.HashMap;

public class UserInfoManager extends BaseModule {
    private static UserInfoManager instance = null;

    public static UserInfoManager getInstance() {
        if (instance == null) {
            instance = new UserInfoManager();
            instance.load();
        }
        return instance;
    }

    @BotData("userinfo.json")
    private HashMap<Long, UserData> values = new HashMap<>();

    public void addCoins(long qq, int v) {
        getUserData(qq).coins += v;
        save();
    }

    public int getCoins(long qq) {
        return getUserData(qq).coins;
    }

    public void incQaCount(long qq) {
        getUserData(qq).qaCount++;
        save();
    }

    public void incQaRight(long qq) {
        getUserData(qq).qaRight++;
        save();
    }

    public boolean onSign(long qq) {
        UserData userData = getUserData(qq);
        if (userData.todaySigned) {
            return false;
        }
        userData.signedDays++;
        userData.todaySigned = true;
        if (userData.todaySigned && userData.yesterdaySigned) {
            userData.continuousSignedDays++;
        }
        userData.coins += (10 + userData.continuousSignedDays);
        save();
        return true;
    }

    public void onNewDay() {
        for (UserData ud : values.values()) {
            ud.yesterdaySigned = ud.todaySigned;
            if (ud.todaySigned) {
                ud.todaySigned = false;
            } else {
                ud.continuousSignedDays = 1;
            }
        }
        save();
    }

    public int getContinuousSignedDays(long qq) {
        return getUserData(qq).continuousSignedDays;
    }

    public UserData getUserData(long id) {
        if (values.containsKey(id)) {
            return values.get(id);
        }
        UserData ud = new UserData();
        ud.firstMeetTime = System.currentTimeMillis();
        values.put(id, ud);
        save();
        return ud;
    }

    @Override
    public String getModuleName() {
        return "UserInfoManager";
    }

    public static class UserData {
        @SerializedName("a")
        public int coins;
        @SerializedName("c")
        public int signedDays;
        @SerializedName("d")
        public boolean yesterdaySigned;
        @SerializedName("e")
        public boolean todaySigned;
        @SerializedName("f")
        public int continuousSignedDays = 0;
        @SerializedName("g")
        public long firstMeetTime;
        @SerializedName("i")
        public int qaCount;
        @SerializedName("j")
        public int qaRight;
    }
}
