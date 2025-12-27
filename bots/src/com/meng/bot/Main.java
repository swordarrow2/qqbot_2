package com.meng.bot;

import com.meng.api.touhou.THGameDataManager;
import com.meng.bot.qq.QqBotMain;

public class Main {

    public static void main(String... args) {
        THGameDataManager.getThGameData();
        QqBotMain.getInstance().init();
    }
}
 
