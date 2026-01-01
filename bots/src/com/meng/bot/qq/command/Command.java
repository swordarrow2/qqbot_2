package com.meng.bot.qq.command;

import com.meng.bot.qq.Permission;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.util.function.Consumer;

public enum Command {
    help("使用说明", Permission.Normal, "帮助"),
    getGroupInfo("查看群聊天统计", Permission.Normal, "查看群统计"),
    //    HonKai3("查询崩坏三装甲信息",Permission.Normal,"bh3","崩坏3","崩坏三"),
    deleteCache("删除缓存", Permission.Normal, "refreshCache"),
    noteUser("提醒用户", Permission.Normal, "note"),
    textToSpeech("文字转语音", Permission.Normal, "tts"),
    translate("翻译至中文", Permission.Normal, "tr"),
    downloadPixivImage("下载Pixiv图片", Permission.Admin, "getPixiv"),
    addBotToBotList("将其他bot账号添加至数据库", Permission.Admin, "addBot"),
    addUser("将账号信息添加至数据库", Permission.Admin, "addUser"),
    findUserInBotGroups("在bot所在群中查找指定账号", Permission.Admin, "findInGroup"),
    welcomeSentence("设置加群欢迎词", Permission.Admin, "welcome"),
    setGroupName("设置群名称", Permission.Admin, "setGroupName"),
    setGroupCard("设置群昵称", Permission.Admin, "setGroupCard"),
    showSwitches("显示功能开关", Permission.Admin, "switch"),
    broadcastInBotGroups("在启用bot的群中发送广播", Permission.Master, "broadcast"),
    turnOffMain("打开主开关", Permission.Master, "start"),
    turnOnMain("关闭主开关", Permission.Master, "stop"),
    findPersonInfoInConfigJson("在bot的数据库中查找内容", Permission.Master, "findConfig"),
    threadInfo("查看线程池状态", Permission.Master, "threadInfo"),
    jvmGc("启动jvm垃圾回收", Permission.Master, "gc"),
    resend("转发消息", Permission.Master, "send"),
    setUserSpecialTitleInGroup("设置群头衔", Permission.Master, "setSpecial"),
    blockUser("账号添加至过滤列表", Permission.Master, "blockUser"),
    blackUser("账号添加至黑名单", Permission.Master, "blackUser"),
    kickUserFromGroup("账号踢出群", Permission.Master, "kick"),
    muteUser("禁言指定账号", Permission.Admin, "mute"),
    recallMessageOnQuote("撤回回复的消息", Permission.Admin, "recall"),
    saveOnQuote("保存回复的图片", Permission.Master, "save"),
    hotFix("热更新", Permission.Owner, "hotfix"),
    hotfixCancel("取消热更新", Permission.Owner, "hotfixCancel"),
    exitGroup("bot退群", Permission.Admin, "exitGroup", "dissmiss"),
    openAllSwitch("打开所有功能", Permission.Owner, "openAllSwitch"),
//    saveImage("保存图片", Permission.Owner, "saveImage"),
    AcgImages("二刺螈图片", Permission.Normal, "st"),
    //    AcgImagesFromFantasyZone("使用FantasyZone接口的二刺螈图片",Permission.Normal,"stf"),
    Jrrp("人品检测", Permission.Normal, "。jrrp"),
    SignIn("签到", Permission.Normal, "签到"),
    personInfo("查看自己的信息", Permission.Normal, "info"),
    dice_r("dice:r", Permission.Normal, "r"),
    dice_ra("dice:ra", Permission.Normal, "ra"),
    dice_li("dice:li", Permission.Normal, "li"),
    dice_ti("dice:ti", Permission.Normal, "ti"),
    dice_rd("dice:rd", Permission.Normal, "rd"),
    dice_nn("设置bot对自己的称呼", Permission.Normal, "nn"),
    dice_jrrp("人品检测", Permission.Normal, "jrrp"),

    dice_roll(Permission.Normal, "roll") {
        {
            secondaryCommands = new SecondaryCommand[]{
                    SecondaryCommand.dice_roll_plane
            };
        }
    },
    dice_draw(Permission.Normal, "draw") {
        {
            secondaryCommands = new SecondaryCommand[]{
                    SecondaryCommand.dice_draw_spell,
                    SecondaryCommand.dice_draw_neta,
                    SecondaryCommand.dice_draw_music,
                    SecondaryCommand.dice_draw_grandma,
                    SecondaryCommand.dice_draw_game,
                    SecondaryCommand.dice_draw_goodEnd,
                    SecondaryCommand.dice_draw_ufo,
                    SecondaryCommand.dice_draw_all
            };
        }
    },

    dice_spellInfo("车万符卡信息", Permission.Normal, "spellInfo"),
    dice_characterInfo("车万角色信息", Permission.Normal, "charaInfo"),

    imageTransaction(Permission.Normal, "") {
        {
            secondaryCommands = new SecondaryCommand[]{
//                    SecondaryCommand.searchPicture,
//                SecondaryCommand.imageTag,
//                SecondaryCommand.imagePorn,
//                SecondaryCommand.imageOcr,
                    SecondaryCommand.getImageUrl,
//                SecondaryCommand.getImageDeepDanbooruTag,
                    SecondaryCommand.imageToGray,
                    SecondaryCommand.imageRotate,
                    SecondaryCommand.imageUpsideDown,
                    SecondaryCommand.imageFlip,
                    SecondaryCommand.imageUpSeija,
                    SecondaryCommand.expression_jingShenZhiZhu,
                    SecondaryCommand.expression_shenChu,
                    SecondaryCommand.expression_xiaoHuaJia,
                    SecondaryCommand.expression_JiXuGanHuo,
                    SecondaryCommand.expression_WoYongYuanXiHuan,
                    SecondaryCommand.expression_FaDian,
                    SecondaryCommand.expression_BuKeYiJianMian,
                    SecondaryCommand.expression_Pa,
                    SecondaryCommand.expression_ZaiXiang
            };
        }
    },
    coinsManager(Permission.Normal, "") {
        {
            secondaryCommands = new SecondaryCommand[]{
                    SecondaryCommand.sign,
                    SecondaryCommand.getCoins,
                    SecondaryCommand.addCoins
            };
        }
    },
    miraiCodeToText("miraiCodeToText", Permission.Normal, "mtt"),
    textToMiraiCode("textToMiraiCode", Permission.Normal, "ttm"),
    touHouMusicTest(Permission.Normal, "原曲认知") {
        {
            secondaryCommands = new SecondaryCommand[]{
                    SecondaryCommand.music_test_easy,
                    SecondaryCommand.music_test_normal,
                    SecondaryCommand.music_test_hard,
                    SecondaryCommand.music_test_lunatic
            };
        }
    },
    intCalculate("int32计算", Permission.Normal, "int"),
    blackHistory("迫害图", Permission.Normal, "迫害图"),
    QuestionAndAnswer("问答", Permission.Normal, "qa"),
    QuestionAndAnswerRandom("自动生成题目的问答", Permission.Normal, "qar"),
    ReflexCommand("ReflexCommand", Permission.Normal, "invoke"),
    Ollama("ReflexCommand", Permission.Normal, "llm"),
    report("留言", Permission.Normal, "留言"),
    reportSee("留言查看", Permission.Master, "留言查看");

    public final Permission pms;
    Consumer<GroupMessageEvent> consumer;
    public final String[] cmds;
    public final String note;
    public SecondaryCommand[] secondaryCommands = null;

    Command(String... cmds) {
        this.cmds = cmds;
        this.note = null;
        this.pms = Permission.Normal;
    }

    Command(Permission pms, String... cmds) {
        this.cmds = cmds;
        this.pms = pms;
        this.note = null;
    }

    Command(String note, Permission pms, String... cmds) {
        this.cmds = cmds;
        this.pms = pms;
        this.note = note;
    }

    Command(String note, Permission pms, Consumer<GroupMessageEvent> consumer, String... cmds) {
        this.cmds = cmds;
        this.pms = pms;
        this.consumer = consumer;
        this.note = note;
    }

    void accept(GroupMessageEvent event) {
        consumer.accept(event);
    }

    public static String getCommandNote(Command command) {
        StringBuilder stringBuilder = new StringBuilder("子命令:");
        for (SecondaryCommand entry : command.secondaryCommands) {
            stringBuilder.append("\n");
            for (String s : entry.cmds) {
                stringBuilder.append(s).append(",");
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append(":").append(entry.note);
        }
        return stringBuilder.toString();
    }

    public static Command getCommand(String s) {
        String key = s.replaceAll("\\p{C}", "").trim();
        for (Command cmd : values()) {
            if (cmd.name().equals(key)) {
                return cmd;
            }
            for (String sub : cmd.cmds) {
                if (sub.equals(key)) {
                    return cmd;
                }
            }
        }
        return null;
    }

    public SecondaryCommand getSecondaryCommand(String s) {
        String key = s.replaceAll("\\p{C}", "").trim();
        for (SecondaryCommand cmd : SecondaryCommand.values()) {
            if (cmd.name().equals(key)) {
                return cmd;
            }
            for (String sub : cmd.cmds) {
                if (sub.equals(key)) {
                    return cmd;
                }
            }
        }
        return null;
    }

    public static String getCommandHelp() {
        StringBuilder builder = new StringBuilder();

        return builder.toString();
    }
}
