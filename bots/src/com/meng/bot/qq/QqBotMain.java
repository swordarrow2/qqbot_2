package com.meng.bot.qq;

import com.google.gson.annotations.SerializedName;
import com.meng.bot.config.ConfigManager;
import com.meng.tools.normal.FileTool;
import com.meng.tools.normal.JSON;
import com.meng.tools.normal.TimeTask;
import com.meng.tools.sjf.SJFExecutors;
import com.meng.tools.sjf.SJFPathTool;
import kotlin.sequences.Sequence;
import net.mamoe.mirai.Bot;
import top.mrxiaom.overflow.BotBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class QqBotMain {

    private static QqBotMain instance;
    private static final HashMap<Bot, BotWrapper> botWrappers = new HashMap<>();
    private static boolean isLoaded = false;
    public static AccountInfo accountInfo;
    private static final ArrayList<Long> accountList = new ArrayList<>();

    public static QqBotMain getInstance() {
        if (instance != null) {
            return instance;
        }
        return instance = new QqBotMain();
    }

    public static ArrayList<Long> getAccountList() {
        return accountList;
    }


    public void init() {
        if (isLoaded) {
            return;
        }
        try {
            accountInfo = JSON.fromJson(FileTool.readString(new File(SJFPathTool.getAppDirectory() + "sjf.json")), AccountInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        AccountInfo.Account account = accountInfo.accounts.get(0);
        System.out.println("connect to:  " + account.wsuri);
        Bot bot = BotBuilder.positive(account.wsuri)
                .token(account.token)
                .connect();
        System.out.println("connected.");
        BotWrapper botWrapper = new BotWrapper();
        botWrapper.setBot(bot);

        ModuleManager moduleManager = new ModuleManager();
        BotMessageHandler botMessageHandler = new BotMessageHandler();
        ConfigManager configManager = new ConfigManager();

        botWrapper.setBotMessageHandler(botMessageHandler);
        botWrapper.setModuleManager(moduleManager);
        botWrapper.setConfigManager(configManager);

        botMessageHandler.setBotWrapper(botWrapper);
        configManager.setBotHelper(botWrapper);
        moduleManager.setBotHelper(botWrapper);


        bot.getEventChannel().registerListenerHost(botMessageHandler);
        bot.login();

        botWrappers.put(bot, botWrapper);
        SJFExecutors.execute(() -> bot.join());

        TimeTask.getInstance(botWrapper).addTask(0, 0, () -> FileTool.deleteFiles(SJFPathTool.getTempPath("")));
        SJFExecutors.execute(TimeTask.getInstance());
        isLoaded = true;

        //        SJFExecutors.executeAfterTime(new Runnable(){
//
//                @Override
//                public void run() {
//                    for (Group group:sbot.getGroups()) {
//                        if (group.getBotAsMember().getMuteTimeRemaining() > 0) {
//                            if (group.getId() == SBot.yysGroup) {
//                                continue;
//                            }
//                            group.quit();
//                            sbot.sendGroupMessage(SBot.yysGroup, "退出群" + group.getId());
//                        }
//                    }
//                }
//            }, 1, TimeUnit.MINUTES);  
    }

    public static BotWrapper getBotWrapper(Bot bot) {
        return botWrappers.get(bot);
    }

    public static List<Bot> getInstances() {
        return Bot.Companion.getInstances();
    }

    public static Sequence<Bot> getInstancesSequence() {
        return Bot.Companion.getInstancesSequence();
    }

    public static Bot getInstance(long qq) throws NoSuchElementException {
        return Bot.Companion.getInstance(qq);
    }

    public static Bot getInstanceOrNull(long qq) {
        return Bot.Companion.getInstanceOrNull(qq);
    }

    public static Bot findInstance(long qq) {
        return Bot.Companion.findInstance(qq);
    }

    public static class AccountInfo {
        public ArrayList<AccountInfo.Account> accounts;

        public static class Account {
            @SerializedName("a")
            public long account;
            @SerializedName("p")
            public String password;
            //          @SerializedName("t")
//          public Personality tag;
            @SerializedName("e")
            public boolean enable;
            @SerializedName("d")
            public boolean debug = false;
            @SerializedName("w")
            public String wsuri;
            @SerializedName("o")
            public String token;
        }
    }
}
