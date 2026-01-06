package com.meng.bot;

import com.meng.api.touhou.THGameDataManager;
import com.meng.bot.qq.QqBotMain;

import java.util.Map;
import java.util.TreeMap;

public class Main {
    public static final String VERSION = "3.1.0";
    private static TreeMap<String, String> updateLog = new TreeMap<>() {{
        put(VERSION, "修正了命令权限错误的bug");
    }};

    public static String getUpdateLog() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("更新记录:");
        for (Map.Entry<String, String> entry : updateLog.entrySet()) {
            stringBuilder.append("\r\n").append(entry.getKey()).append(":").append(entry.getValue());
        }
        return stringBuilder.toString();
    }

    public static void main(String... args) {
        THGameDataManager.getThGameData();
        QqBotMain.getInstance().init();
    }
}
 
