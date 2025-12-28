package com.meng.bot.qq.modules;

import com.meng.api.touhou.THCharacter;
import com.meng.api.touhou.THGameDataManager;
import com.meng.api.touhou.THSpell;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.Personality;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.command.SecondaryCommand;
import com.meng.bot.qq.commonModules.UserInfoManager;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;
import com.meng.tools.normal.TextLexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

/**
 * @Description: 模拟骰子
 * @author: 司徒灵羽
 **/
public class Dice extends BaseModule implements IGroupMessageEvent {

    public Dice(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    public String getModuleName() {
        return "Dice";
    }

    @Override
    @CommandDescribe(cmd = "dice", note = "模仿Dice的行为")
    public boolean onGroupMessage(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        if (msg.length() < 2) {
            return false;
        }
        long groupId = event.getGroup().getId();
        if (!configManager.getGroupConfig(groupId).isFunctionEnabled(Functions.Dice)) {
            return false;
        }
        long qqId = event.getSender().getId();
        if (Command.getCommand(msg) == Command.Jrrp) {
            sendMessage(event.getGroup(), String.format("%s今天会在%s疮痍", configManager.getNickName(groupId, qqId), THGameDataManager.hashRandomSpell(qqId).cnName));
            return true;
        }
        if (msg.charAt(0) != '.') {
            return false;
        }
        Random random = ThreadLocalRandom.current();
        ArrayList<String> list = TextLexer.analyze(msg);
        Iterator<String> iterator = list.iterator();
        iterator.next();//.
        try {
            String senderNickName = configManager.getNickName(groupId, qqId);
            int flag = SJFRandom.hashSelectInt(0, 16);

            Command command = Command.getCommand(iterator.next());
            if (command == null) {
                return false;
            }
            switch (command) {
                case SignIn -> {
                    if (UserInfoManager.getInstance().onSign(qqId)) {
                        UserInfoManager.UserData gu = UserInfoManager.getInstance().getUserData(qqId);
                        String result = String.format("签到成功,获得%d个硬币(基础:10,连续签到:%d)", 10 + gu.continuousSignedDays, gu.continuousSignedDays);
                        sendMessage(event.getGroup(), result);
                    } else {
                        sendMessage(event.getGroup(), "你今天已经签到过啦");
                    }
                    return true;
                }
                case personInfo -> {
                    UserInfoManager.UserData ud = UserInfoManager.getInstance().getUserData(qqId);
                    sendMessage(event.getGroup(), String.format("累计签到%d天,连续签到%d天,信仰:%d,答题%d道,正确率%.2f%%", ud.signedDays, ud.continuousSignedDays, ud.coins, ud.qaCount, (((float) ud.qaRight) / ud.qaCount) * 100));
                    return true;
                }
                case dice_r -> {
                    sendMessage(event.getGroup(), String.format("%s投掷%s:D100 = %d", senderNickName, iterator.hasNext() ? iterator.next() : "", random.nextInt(100)));
                    return true;
                }
                case dice_ra -> {
                    String ras = iterator.next();
                    sendMessage(event.getGroup(), String.format("%s进行检定:D100 = %d/%s", senderNickName, random.nextInt(Integer.parseInt(ras)), ras));
                    return true;
                }
                case dice_li -> {
                    sendMessage(event.getGroup(), String.format("%s的疯狂发作-总结症状:\n1D10=%d\n症状: 狂躁：调查员患上一个新的狂躁症，在1D10=%d小时后恢复理智。在这次疯狂发作中，调查员将完全沉浸于其新的狂躁症状。这是否会被其他人理解（apparent to other people）则取决于守秘人和此调查员。\n1D100=%d\n具体狂躁症: 臆想症（Nosomania）：妄想自己正在被某种臆想出的疾病折磨。(KP也可以自行从狂躁症状表中选择其他症状)", senderNickName, random.nextInt(11), random.nextInt(11), random.nextInt(101)));
                    return true;
                }
                case dice_ti -> {
                    sendMessage(event.getGroup(), String.format("%s的疯狂发作-临时症状:\n1D10=%d\n症状: 逃避行为：调查员会用任何的手段试图逃离现在所处的位置，状态持续1D10=%d轮。", senderNickName, random.nextInt(11), random.nextInt(11)));
                    return true;
                }
                case dice_rd -> {
                    sendMessage(event.getGroup(), String.format("由于%s,%s骰出了: D100=%d", iterator.next(), senderNickName, random.nextInt(101)));
                    return true;
                }
                case dice_nn -> {
                    if (!iterator.hasNext()) {
                        configManager.setNickName(qqId, null);
                        sendMessage(event.getGroup(), "我以后会用你的QQ昵称称呼你");
                        return true;
                    }
                    String name = iterator.next();
                    if (name.length() > 30) {
                        sendMessage(event.getGroup(), "太长了,记不住");
                        return true;
                    }
                    configManager.setNickName(qqId, name);
                    sendMessage(event.getGroup(), "我以后会称呼你为" + name);
                    return true;
                }
                case dice_jrrp -> {
                    float fpro;
                    Personality.Tag tag = botWrapper.personality.getTag();
                    String nickName = configManager.getNickName(groupId, qqId);
                    if (tag == Personality.Tag.HonKai_3rd) {
                        sendMessage(event, String.format("%s今天深渊得分是%d", nickName, SJFRandom.hashSelectInt(qqId, 1001)));
                    } else if (tag == Personality.Tag.TouHou) {
                        if (flag == 0) {
                            fpro = 99.61f;
                        } else if (flag == 1) {
                            fpro = 97.60f;
                        } else if (flag == 2) {
                            fpro = 100.00f;
                        } else {
                            fpro = ((float) (SJFRandom.hashSelectInt(qqId, 10001))) / 100;
                        }
                        sendMessage(event, String.format("%s今天会在%.2f%%处疮痍", nickName, fpro));
                    } else {
                        sendMessage(event, String.format("%s今天的人品是%d", nickName, SJFRandom.hashSelectInt(qqId, 100)));
                    }
                    return true;
                }
                case dice_roll -> {
                    if (!iterator.hasNext()) {
                        sendMessage(event, Command.getCommandNote(Command.dice_roll));
                        return false;
                    }
                    SecondaryCommand diceRollSecondaryCommand = command.getSecondaryCommand(iterator.next());
                    if (diceRollSecondaryCommand == null) {
                        return false;
                    }
                    switch (diceRollSecondaryCommand) {
                        case dice_roll_plane:
                            sendMessage(event.getGroup(), THGameDataManager.randomPlane(iterator.next()));
                            return true;
                    }
                    return true;
                }
                case dice_draw -> {
                    if (!iterator.hasNext()) {
                        sendMessage(event, Command.getCommandNote(Command.dice_draw));
                        return false;
                    }
                    SecondaryCommand diceDrawSecondaryCommand = command.getSecondaryCommand(iterator.next());
                    if (diceDrawSecondaryCommand == null) {
                        return false;
                    }
                    diceDraw(event, qqId, random, list, iterator, senderNickName, flag, diceDrawSecondaryCommand);
                    return true;
                }
                case dice_spellInfo -> {
                    THSpell sc = THGameDataManager.getTHSpell(iterator.next());
                    if (sc == null) {
                        sendQuote(event, "没有找到这张符卡");
                        return true;
                    }
                    sendQuote(event, sc.getPs());
                    return true;
                }
                case dice_characterInfo -> {
                    THCharacter character = THGameDataManager.getCharacter(iterator.next());
                    if (character == null) {
                        sendQuote(event, "角色信息未填坑");
                        return true;
                    }
                    sendQuote(event, character.getCharaNick());
                    return true;
                }
            }
        } catch (Exception e) {
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
            sendMessage(event.getGroup(), "参数错误:" + e);
        }
        return false;
    }

    private void diceDraw(GroupMessageEvent gme, long qqId, Random random, ArrayList<String> list, Iterator<String> iterator, String pname, int flag, SecondaryCommand diceDrawSecondaryCommand) {
        switch (diceDrawSecondaryCommand) {
            case dice_draw_spell -> {
                if (list.size() == 3) {
                    sendMessage(gme.getGroup(), THGameDataManager.randomSpell().cnName);
                } else if (list.size() == 4) {
                    String spellName = iterator.next();
                    THSpell sc = THGameDataManager.getTHSpell(spellName);
                    if (sc == null) {
                        sendMessage(gme.getGroup(), "没有找到这张符卡");
                        return;
                    }
                    float allPro = ((float) (SJFRandom.hashSelectInt(qqId + spellName.hashCode()) % 10001)) / 100;
                    sendMessage(gme.getGroup(), "你今天" + sc.cnName + "的收率是" + allPro + "%");
                }
            }
            case dice_draw_neta ->
                    sendMessage(gme.getGroup(), String.format("%s今天宜打%s", pname, THGameDataManager.hashSelectNeta(qqId)));
            case dice_draw_music ->
                    sendMessage(gme.getGroup(), String.format("%s今天宜听%s", pname, THGameDataManager.hashSelectMusic(qqId).name));
            case dice_draw_grandma -> {
                if (SJFRandom.hashSelectInt(qqId, 16) == 0) {
                    sendMessage(gme.getGroup(), String.format("%s今天宜认八云紫当奶奶", pname));
                    return;
                }
                sendMessage(gme.getGroup(), String.format("%s今天宜认%s当奶奶", pname, THGameDataManager.hashRandomCharacter(qqId).name));
            }
            case dice_draw_game -> {
                String s = THGameDataManager.randomGame(pname, qqId, true) + "," + THGameDataManager.randomGame(pname, qqId + 1, false);
                sendMessage(gme.getGroup(), s);
            }
            case dice_draw_goodEnd -> sendMessage(gme.getGroup(), THGameDataManager.hashSelectGE(qqId));
            case dice_draw_ufo -> {
                int ufor = random.nextInt(10);
                if (ufor < 8) {
                    String[] fileName = {"blue.gif", "green.gif", "red.gif"};
                    MessageChainBuilder ufoMsgB = new MessageChainBuilder();
                    ThreadLocalRandom current = ThreadLocalRandom.current();
                    ufoMsgB.add(botWrapper.toImage(SJFPathTool.getUFOPath(fileName[current.nextInt(3)]), gme.getGroup()));
                    ufoMsgB.add(botWrapper.toImage(SJFPathTool.getUFOPath(fileName[current.nextInt(3)]), gme.getGroup()));
                    ufoMsgB.add(botWrapper.toImage(SJFPathTool.getUFOPath(fileName[current.nextInt(3)]), gme.getGroup()));
                    sendMessage(gme.getGroup(), ufoMsgB.asMessageChain());
                } else if (ufor == 8) {
                    sendMessage(gme.getGroup(), botWrapper.toImage(SJFPathTool.getUFOPath("/yellow.gif"), gme.getGroup()));
                } else {
                    sendMessage(gme.getGroup(), botWrapper.toImage(SJFPathTool.getUFOPath("colorful.gif"), gme.getGroup()));
                }
            }
            case dice_draw_all -> {
                String allStr = String.format("%s今天宜打%s", pname, THGameDataManager.hashSelectNeta(qqId));
                allStr += "\n";
                allStr += String.format("%s今天宜听%s", pname, THGameDataManager.hashSelectMusic(qqId).name);
                allStr += "\n";
                if (SJFRandom.hashSelectInt(qqId, 16) == 0) {
                    allStr += String.format("%s今天宜认八云紫当奶奶", pname);
                } else {
                    allStr += String.format("%s今天宜认%s当奶奶", pname, THGameDataManager.hashRandomCharacter(qqId).name);
                }
                allStr += "\n";
                allStr += THGameDataManager.randomGame(pname, qqId, true);
                allStr += ",";
                allStr += THGameDataManager.randomGame(pname, qqId + 1, false);
                allStr += "\n";
                float allPro;
                if (flag == 0) {
                    allPro = 99.61f;
                } else if (flag == 1) {
                    allPro = 97.60f;
                } else if (flag == 2) {
                    allPro = 100.00f;
                } else {
                    allPro = ((float) (SJFRandom.hashSelectInt(qqId) % 10001)) / 100;
                }
                allStr += String.format("%s今天会在%.2f%%处疮痍", pname, allPro);
                sendMessage(gme.getGroup(), allStr);
            }
            default -> sendMessage(gme.getGroup(), "可用.draw help查看帮助");
        }
    }
}
