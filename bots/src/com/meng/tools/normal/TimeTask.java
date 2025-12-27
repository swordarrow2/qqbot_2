package com.meng.tools.normal;


import com.meng.bot.config.Functions;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.QqBotMain;
import com.meng.bot.qq.commonModules.UserInfoManager;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.meng.tools.sjf.SJFExecutors;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

/**
 * @author: 司徒灵羽
 **/
public class TimeTask implements Runnable {

    private static TimeTask instance = new TimeTask();

    public static TimeTask getInstance() {
        return instance;
    }

    private final long groupYuTang = 617745343L;
    private final long groupDNFmoDao = 424838564L;
    private final long groupXueXi = 312342896L;
    private final long alice = 1326051907L;
    private final long YYS = 1418780411L;

    private HashSet<TaskBean> tasks = new HashSet<>();

    private String[] morning = new String[]{"早上好", "早安", "早", "大家早上好", "大家早上好啊..", "Good morning!"};
    private String[] evening = new String[]{"晚安", "大家晚安", "晚安....", "大家晚安....", "大家早点休息吧"};

    public TimeTask() {
        addTask(23, 0, new Runnable() {

            @Override
            public void run() {
                sendRegards(evening, true);
            }
        });
        addTask(6, 0, new Runnable() {

            @Override
            public void run() {
                sendRegards(morning, false);
            }
        });

        addTask(0, 0, new Runnable() {

            @Override
            public void run() {
                UserInfoManager.getInstance().onNewDay();
            }
        });
        SJFExecutors.executeAtFixedRate(new Runnable() {

            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                if (getTipHour(c)) {
//                    if (c.getActualMaximum(Calendar.DAY_OF_MONTH) == c.get(Calendar.DATE)) {
//                        sendGroupMessage(groupDNFmoDao, "最后一天莉，，，看看冒险团商店");
//                        sendGroupMessage(groupXueXi, "最后一天莉，，，看看冒险团商店");
//                    }
//                    if (c.get(Calendar.DAY_OF_WEEK) == 4) {
//                        sendGroupMessage(groupDNFmoDao, "星期三莉，，，看看成长胶囊");
//                        sendGroupMessage(groupXueXi, "星期三莉，，，看看成长胶囊");
//                    }
                }
            }
        }, 0, 1, TimeUnit.HOURS);
        SJFExecutors.executeAtFixedRate(new Runnable() {

            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                for (TaskBean ts : tasks) {
                    if ((ts.h == -1 || ts.h == c.get(Calendar.HOUR_OF_DAY)) && (ts.min == -1 || ts.min == c.get(Calendar.MINUTE))) {
                        SJFExecutors.execute(ts.r);
                    }
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void addTask(int onHour, int onMinute, Runnable r) {
        tasks.add(new TaskBean(onHour, onMinute, r));
    }

    @Override
    public void run() {
        for (TaskBean taskBean : tasks) {
            Calendar c = Calendar.getInstance();
            if (taskBean.h == c.get(Calendar.HOUR_OF_DAY) && taskBean.min == c.get(Calendar.MINUTE)) {
                SJFExecutors.execute(taskBean.r);
            }
        }
    }


    private void sendRegards(final String[] selections, final boolean sleep) {
        for (final Bot bot : Bot.Companion.getInstances()) {
            SJFExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    for (Group group : bot.getGroups()) {
                        BotWrapper botWrapper = QqBotMain.getBotWrapper(bot);
                        if (botWrapper.getConfigManager().getGroupConfig(group).isFunctionEnabled(Functions.GroupMessageEvent)) {
                            try {
                                //      group.sendMessage(SJFRandom.randomSelect(selections));
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ExceptionCatcher.getInstance().catchException(botWrapper, e);
                            }
                            botWrapper.sleeping = sleep;
                        }
                    }
                }
            });
        }
    }

    private boolean getTipHour(Calendar c) {
        return (c.get(Calendar.HOUR_OF_DAY) == 12 || c.get(Calendar.HOUR_OF_DAY) == 16 || c.get(Calendar.HOUR_OF_DAY) == 22);
    }

    private static class TaskBean {
        public int h;
        public int min;
        public Runnable r;

        public TaskBean(int h, int min, Runnable r) {
            this.h = h;
            this.min = min;
            this.r = r;
        }

        @Override
        public int hashCode() {
            return Objects.hash(h, min, r);
        }
    }
}
