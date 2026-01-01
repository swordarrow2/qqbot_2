package com.meng.bot.qq;

import com.google.gson.annotations.SerializedName;
import com.meng.bot.config.ConfigManager;
import com.meng.bot.qq.hotfix.HotfixClassLoader;
import com.meng.tools.normal.*;
import com.meng.tools.sjf.SJFExecutors;
import com.meng.tools.sjf.SJFPathTool;
import kotlin.sequences.Sequence;
import net.mamoe.mirai.Bot;
import top.mrxiaom.overflow.BotBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
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

        for (String fileName : new File(SJFPathTool.getHotFixPath()).list((dir, name) -> name.endsWith(".class"))) {
            try {
                if (loadClassFromDisk(fileName.substring(0, fileName.lastIndexOf(".class")), botWrapper, moduleManager)) {
                    System.out.println(fileName + "热修复成功");
                }
            } catch (Exception e) {
                System.out.println(fileName + "加载失败");
            }
        }

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
        FileWatcherService.getInstance().addListener(SJFPathTool.getPersistentPath(), new FileWatcherService.FileWatchedListener() {
            @Override
            public void onCreated(WatchEvent<Path> watchEvent) {
                String fileName = watchEvent.context().toFile().getName();
                System.out.printf("文件[%s]被创建，时间：%s%n", fileName, TimeFormater.getTime());
            }

            @Override
            public void onDeleted(WatchEvent<Path> watchEvent) {
                String fileName = watchEvent.context().toFile().getName();
                System.out.printf("文件[%s]被删除，时间：%s%n", fileName, TimeFormater.getTime());
            }

            @Override
            public void onModified(WatchEvent<Path> watchEvent) {
                String fileName = watchEvent.context().toFile().getName();
                System.out.printf("文件[%s]被修改，时间：%s%n", fileName, TimeFormater.getTime());
            }
        });
        FileWatcherService.getInstance().addListener(SJFPathTool.getHotFixPath(), new FileWatcherService.FileWatchedListener() {
            @Override
            public void onCreated(WatchEvent<Path> watchEvent) {
                String fileName = watchEvent.context().toFile().getName();
                System.out.printf("类[%s]被创建，加载热修复，时间：%s%n", fileName, TimeFormater.getTime());
                if (loadClassFromDisk(fileName.substring(0, fileName.lastIndexOf(".class")), botWrapper, moduleManager)) {
                    System.out.printf("类[%s]被创建，加载热修复成功，时间：%s%n", fileName, TimeFormater.getTime());
                }
            }

            @Override
            public void onDeleted(WatchEvent<Path> watchEvent) {
                String fileName = watchEvent.context().toFile().getName();
                System.out.printf("类[%s]被删除，取消热修复，时间：%s%n", fileName, TimeFormater.getTime());
                moduleManager.hotfixCancel("com.meng.bot.qq.modules." + fileName.substring(0, fileName.lastIndexOf(".class")));
            }

            @Override
            public void onModified(WatchEvent<Path> watchEvent) {
                String fileName = watchEvent.context().toFile().getName();
                System.out.printf("类[%s]被修改，加载热修复，时间：%s%n", fileName, TimeFormater.getTime());
                if (!loadClassFromDisk(fileName.substring(0, fileName.lastIndexOf(".class")), botWrapper, moduleManager)) {
                    System.out.printf("类[%s]被修改，加载热修复成功，时间：%s%n", fileName, TimeFormater.getTime());
                }
            }
        });
    }

    private boolean loadClassFromDisk(String className, BotWrapper botWrapper, ModuleManager moduleManager) {
        Class<?> nClass;
        Object module;
        try {
            HotfixClassLoader clsLd = new HotfixClassLoader(new HashMap<>());
            clsLd.put("com.meng.bot.qq.modules." + className, FileTool.readBytes(SJFPathTool.getHotFixPath() + className + ".class"));
            nClass = clsLd.loadClass("com.meng.bot.qq.modules." + className);
            Constructor<?> constructor = nClass.getDeclaredConstructor(BotWrapper.class);
            module = constructor.newInstance(botWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("类[%s]加载热修复失败，时间：%s%n", className, TimeFormater.getTime());
            return false;
        }
        try {
            Method methodLoad = nClass.getMethod("load");
            methodLoad.invoke(module);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("类[%s]加载热修复失败，时间：%s%n", className, TimeFormater.getTime());
            return false;
        }
        moduleManager.hotfix(module);
        return true;
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
