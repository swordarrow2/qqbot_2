package com.meng.bot.qq.modules;

import com.google.gson.annotations.SerializedName;
import com.meng.api.touhou.THGameDataManager;
import com.meng.api.touhou.THSpell;
import com.meng.bot.annotation.BotData;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;

/**
 * @author: 司徒灵羽
 **/
public class QuestionAndAnswer extends BaseModule implements IGroupMessageEvent {

    @BotData("qa.json")
    private ArrayList<QABean> qaList = new ArrayList<>();

    public HashMap<Long, QABean> onGoingQA = new HashMap<>();
    public static final int easy = 0;
    public static final int normal = 1;
    public static final int hard = 2;
    public static final int lunatic = 3;

    public static final int touhouBase = 1;
    public static final int _2unDanmakuIntNew = 2;
    public static final int _2unDanmakuAll = 3;
    public static final int _2unNotDanmaku = 4;
    public static final int _2unAll = 5;
    public static final int otherDanmaku = 6;
    public static final int luastg = 7;

    public QuestionAndAnswer(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    @CommandDescribe(cmd = ".qa/.qar", note = "问答")
    public boolean onGroupMessage(GroupMessageEvent gme) {
        if (!configManager.getGroupConfig(gme.getGroup()).isFunctionEnabled(Functions.QuestionAndAnswer)) {
            return false;
        }
        if (processQa(gme)) {
            return true;
        }
        if (processQar(gme)) {
            return true;
        }
        return false;
    }

    private boolean processQa(GroupMessageEvent event) {
        long qq = event.getSender().getId();
        QABean qaLast = onGoingQA.get(qq);
        if (qaLast != null && qaLast.fromQar) {
            return false;
        }
        String msg = event.getMessage().contentToString();
        if (qaLast != null && msg.equalsIgnoreCase(".qa")) {
            sendQuote(event, "你还没有回答");
            return true;
        }
        if (qaLast != null) {
            HashSet<Integer> userAnss = new HashSet<>();
            String[] usAnsStrs = msg.split(" ");
            for (String s : usAnsStrs) {
                try {
                    userAnss.add(Integer.parseInt(s) - 1);
                } catch (NumberFormatException ignore) {
                    //if not number,answer will never true 
                    sendQuote(event, "回答错误");
                    return true;
                }
            }
            if (qaLast.getTrueAns().containsAll(userAnss) && qaLast.getTrueAns().size() == userAnss.size()) {
                sendQuote(event, "回答正确");
                qaLast.incTrueTimes();
                UserInfoManager.getInstance().incQaRight(qq);
            } else {
                sendQuote(event, "回答错误");
            }
            onGoingQA.remove(qq);
            return true;
        }
        if (msg.equalsIgnoreCase(".qa")) {
            int randomInt = ThreadLocalRandom.current().nextInt(qaList.size());
            QABean qaNow = qaList.get(randomInt);
            qaNow.incShowTimes();
            UserInfoManager.getInstance().incQaCount(qq);
            StringBuilder sb = new StringBuilder().append("\n题目ID:").append(randomInt).append("\n");
            if (qaNow.getShowTimes() > 0) {
                sb.append(String.format("正确率:%.2f%%", ((float) qaNow.getTrueTimes()) / qaNow.getShowTimes() * 100));
                sb.append("\n");
            }
            if (qaNow.question.contains("(image)")) {
                sb.append(qaNow.question.replace("(image)", botWrapper.toImage(SJFPathTool.getQaImagePath(qaNow.getId() + ".jpg"), event.getGroup()).serializeToMiraiCode()));
            } else {
                sb.append(qaNow.question);
            }
            sb.append("\n");
            qaNow.shuffleAnswer();
            save();
            onGoingQA.put(qq, qaNow);
            int i = 1;
            for (String s : qaNow.answersToSelect) {
                if (s.equals("")) {
                    continue;
                }
                sb.append(i++).append(": ").append(s).append("\n");
            }
            sb.append("回答序号即可");
            if (qaNow.getTrueAns().size() > 1) {
                sb.append(",本题有多个选项(选项用空格隔开)");
            }
            sendQuote(event, MiraiCode.deserializeMiraiCode(sb.toString()));
            return true;
        }
        return false;
    }

    private boolean processQar(GroupMessageEvent gme) {
        String msg = gme.getMessage().contentToString();
        long qq = gme.getSender().getId();
        QABean onGoing = onGoingQA.get(qq);
        if (onGoing != null && msg.equalsIgnoreCase(".qar")) {
            sendQuote(gme, "你还没有回答");
            return true;
        }
        if (onGoing != null) {
            int userAnser = -1;
            try {
                userAnser = Integer.parseInt(msg) - 1;
            } catch (NumberFormatException ignore) {
                //if not number,answer will never true
            }
            if (onGoing.getTrueAns().contains(userAnser)) {
                UserInfoManager.getInstance().incQaRight(qq);
                sendQuote(gme, "回答正确");
            } else {
                sendQuote(gme, "回答错误");
            }
            onGoingQA.remove(qq);
            return true;
        }
        if (msg.equalsIgnoreCase(".qar")) {
            QABean qar = createQA();
            qar.shuffleAnswer();
            StringBuilder sb = new StringBuilder();
            sb.append(qar.question);
            int i = 1;
            for (String s : qar.answersToSelect) {
                if (s.equals("")) {
                    continue;
                }
                sb.append(i++).append(": ").append(s).append("\n");
            }
            sb.append("回答序号即可");
            UserInfoManager.getInstance().incQaCount(qq);
            onGoingQA.put(qq, qar);
            sendQuote(gme, sb.toString());
            return true;
        }
        return false;
    }

    private QABean createQA() {
        int diff = 1 << SJFRandom.randomInt(9);
        THSpell tHSpell = THGameDataManager.getSpellFromDiff(diff);
        THSpell[] sps = THGameDataManager.getSpellFromNotDiff(3, diff);
        QABean qa = new QABean();
        qa.fromQar = true;
        qa.answersToSelect.add(tHSpell.cnName);
        for (THSpell spc : sps) {
            qa.answersToSelect.add(spc.cnName);
        }
        qa.setTrueAns(0);
        qa.shuffleAnswer();
        StringBuilder sb = new StringBuilder();
        sb.append("以下符卡在");
        switch (diff) {
            case THSpell.Easy -> sb.append("easy难度");
            case THSpell.Normal -> sb.append("normal难度");
            case THSpell.Hard -> sb.append("hard难度");
            case THSpell.Lunatic -> sb.append("lunatic难度");
            case THSpell.Overdrive -> sb.append("overdrive难度");
            case THSpell.LastSpell -> sb.append("last spell");
            case THSpell.LastWord -> sb.append("lastword");
            case THSpell.Extra -> sb.append("extra关卡");
            case THSpell.Phantasm -> sb.append("phamtasm关卡");
            default -> {
                System.out.println(tHSpell.cnName);
                System.out.println(diff);
            }
        }
        sb.append("中出现的是:\n");
        qa.question = sb.toString();
        return qa;
    }

    public void addQuestion(long qq, QABean qa) {
        qaList.add(qa);
        onGoingQA.put(qq, qa);
    }

    public int getQaCount() {
        return qaList.size();
    }

    public List<QABean> getQas() {
        return Collections.unmodifiableList(qaList);
    }

    public static class QABean {
        @SerializedName("flag")
        private int flag = 0;
        //flag: id(16bit)                      
        //  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 | 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        @SerializedName("l")
        public int fileLength = 0;
        @SerializedName("q")
        public String question;
        @SerializedName("a")
        public ArrayList<String> answersToSelect = new ArrayList<>();
        @SerializedName("t")
        private int trueAnswerFlag;
        @SerializedName("r")
        public String reason;
        @SerializedName("showTimes")
        public int showTimes;
        @SerializedName("trueTimes")
        public int trueTimes;
        public transient boolean fromQar;

        public void incShowTimes() {
            ++showTimes;
        }

        public void incTrueTimes() {
            ++trueTimes;
        }

        public int getShowTimes() {
            return showTimes;
        }

        public int getTrueTimes() {
            return trueTimes;
        }

        public void shuffleAnswer() {
            int index1 = SJFRandom.randomInt(answersToSelect.size() - 1);
            int index2 = SJFRandom.randomInt(answersToSelect.size() - 1);
            boolean is1F = getBit(index1);
            setBit(index1, getBit(index2));
            setBit(index2, is1F);
            Collections.swap(answersToSelect, index1, index2);
        }

        private boolean getBit(int shift) {
            return (trueAnswerFlag & (1 << shift)) != 0;
        }

        private void setBit(int shift, boolean v) {
            if (v) {
                trueAnswerFlag |= (1 << shift);
            } else {
                trueAnswerFlag &= ~(1 << shift);
            }
        }

        public void setTrueFlag(int flag) {
            trueAnswerFlag = flag;
        }

        public int getTrueAnsFlag() {
            return trueAnswerFlag;
        }

        public void setTrueAns(int... ts) {
            trueAnswerFlag = 0;
            for (int i : ts) {
                trueAnswerFlag |= (1 << i);
            }
        }

        public HashSet<Integer> getTrueAns() {
            HashSet<Integer> intList = new HashSet<>(32);
            for (int i = 0; i < 32; ++i) {
                if ((trueAnswerFlag & (1 << i)) != 0) {
                    intList.add(i);
                }
            }
            return intList;
        }

        public void setFlag(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }

        public void setId(int id) {
            flag &= 0x0000ffff;
            flag |= (id << 16);
        }

        public int getId() {
            return (flag >> 16) & 0xff;
        }
    }
}

