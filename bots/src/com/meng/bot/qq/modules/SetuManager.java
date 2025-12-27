//package com.meng.bot.qq.modules;
//
//import com.meng.api.Character;
//import com.meng.bot.config.Functions;
//import com.meng.bot.config.Person;
//import com.meng.bot.config.QQGroupConfig;
//import com.meng.bot.qq.BotHelper;
//import com.meng.bot.qq.handler.group.IGroupMessageEvent;
//import com.meng.tools.normal.FileTool;
//import com.meng.tools.normal.Network;
//import com.meng.tools.sjf.SJFPathTool;
//import com.meng.tools.sjf.SJFRandom;
//import net.mamoe.mirai.event.events.GroupMessageEvent;
//import net.mamoe.mirai.message.data.Image;
//import net.mamoe.mirai.message.data.Message;
//
//import java.io.IOException;
//import java.util.function.BiConsumer;
//import java.io.File;
//import java.util.HashSet;
//import java.io.FileFilter;
//
//public class SetuManager extends StepCommandProcessor<String> implements IGroupMessageEvent {
//
//    public SetuManager(BotHelper b) {
//        super(b);
//    }
//
//    @Override
//    protected boolean preJudge(GroupMessageEvent event) {
//        QQGroupConfig config = configManager.getGroupConfig(event.getGroup().getId());
//        return config == null || !config.isFunctionEnabled(Functions.ACGImages);
//    }
//
//    @Override
//    public boolean onGroupMessage(GroupMessageEvent event) {
//        if (super.onGroupMessage(event)) {
//            return true;
//        }
//        String msg = event.getMessage().contentToString();
//        if (!msg.startsWith("添加") && msg.endsWith("色图")) {
//            final String charaterName = Character.getInstance().getCharaterName(msg.substring(0, msg.length() - 2));
//            if (charaterName == null) {
//                sendMessage(event, "未找到相关图片");
//                return true;
//            }
//            File[] folders = SJFPathTool.getR15Path("").listFiles(new FileFilter(){
//
//                    @Override
//                    public boolean accept(File p1) {
//                        return p1.getName().contains(charaterName);
//                    }
//                });
//            HashSet<File> hashset = new HashSet<>();
//            for (File file:folders) {
//                if (file.isDirectory()) {
//                    hashset.addAll(FileTool.listAllFiles(file));
//                }
//            }
//            if (hashset.size() == 0) {
//                return false;
//            }
//            sendMessage(event, botHelper.toImage(SJFRandom.randomSelect(hashset), event.getGroup()));
//            return true;
//        }
//
//        final long qq = event.getSender().getId();
//        Person personFromQQ = configManager.getPersonFromQQ(qq);
//        if (personFromQQ == null || !personFromQQ.hasAdminPermission()) {
//            return false;
//        }
//        if (msg.startsWith("添加色图名单 ")) {
//            String name = msg.substring("添加色图名单 ".length());
//            if (name.trim().length() == 0) {
//                sendQuote(event, "不能空白");
//                return true;
//            }
//            File file = SJFPathTool.getR15Path(name);
//            if (file.exists()) {
//                sendQuote(event, "已存在");
//            } else {
//                boolean mkdirs = file.mkdirs();
//                sendQuote(event, mkdirs ?"添加成功": "添加失败");
//                if (mkdirs) {
//                    Character.getInstance().characterMap.put(name, new HashSet<String>());
//                    Character.getInstance().save();
//                }
//            }
//            return true;
//        }
//        if (msg.equals("添加色图")) {
//            StepRunnable<String> stepRunnable = new StepRunnable<>();
//            addAction(qq, stepRunnable);
//            stepRunnable.addActions(new BiConsumer<GroupMessageEvent, StepRunnable<String>>() {
//                    @Override
//                    public void accept(GroupMessageEvent event, StepRunnable runnable) {
//                        moduleManager.getModule(MessageRefuse.class).registUnblock(qq);
//                        sendQuote(event, "发送人名或\"其他\"以选择是谁的图");
//                    }
//                }, new BiConsumer<GroupMessageEvent, StepRunnable<String>>() {
//
//                    @Override
//                    public void accept(GroupMessageEvent event, StepRunnable runnable) {
//                        String name = Character.getInstance().getCharaterName(event.getMessage().contentToString());
//                        if (name == null) {
//                            sendMessage(event, "未找到相关角色");
//                            cancel(event);
//                            return;
//                        }
//                        sendQuote(event, "发送图片为[" + name + "]添加图片,或发送取消添加以退出");
//                        runnable.extra = name;
//                    }
//                });
//            stepRunnable.setLoopPoint();
//            stepRunnable.addActions(new BiConsumer<GroupMessageEvent, StepRunnable<String>>() {
//
//                    @Override
//                    public void accept(GroupMessageEvent event, StepRunnable runnable) {
//                        int leng = 0;
//                        for (Message message : event.getMessage()) {
//                            if (message instanceof Image) {
//                                try {
//                                    byte[] img = Network.httpGetRaw(botHelper.getUrl(((Image) message)));
//                                    FileTool.saveFile(SJFPathTool.getR15Path(runnable.extra + "/" + FileTool.getAutoFileName(img)), img);
//                                    leng += img.length;
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                        if (leng == 0) {
//                            cancel(event);
//                        } else {
//                            sendQuote(event, "保存完成(" + (leng / 1024) + "KB)");
//                            runnable.gotoLoopPoint();
//                        }
//                    }
//                });
//            stepRunnable.run(event);
//            return true;
//        } else if (msg.equals("取消添加")) {
//            if (steps.containsKey(qq)) {
//                moduleManager.getModule(MessageRefuse.class).registNormalBlock(qq);
//                cancel(event);
//            }
//        }
//        return false;
//    }
//}
