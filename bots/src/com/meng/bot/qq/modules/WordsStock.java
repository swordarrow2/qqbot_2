package com.meng.bot.qq.modules;

import com.google.gson.annotations.SerializedName;
import com.meng.api.LkaaApi;
import com.meng.bot.annotation.BotData;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.DataPersistenter;
import com.meng.bot.config.Functions;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.bot.qq.handler.group.INudgeEvent;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.message.data.Dice;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * @author: 司徒灵羽
 **/
public class WordsStock extends BaseModule implements IGroupMessageEvent, INudgeEvent {

    @BotData("dynamic_word_stock.json")
    private WordStock dictionary;

    private HashMap<String, Pattern> regexMap;

    public WordsStock(BotWrapper botHelper) {
        super(botHelper);
    }

    @Override
    @CommandDescribe(cmd = "-", note = "词库")
    public boolean onGroupMessage(GroupMessageEvent event) {
        if (!configManager.getGroupConfig(event.getGroup()).isFunctionEnabled(Functions.DynamicWordStock)) {
            return false;
        }
        return deal(event);
    }

    public boolean deal(GroupMessageEvent event) {
        String msg = event.getMessage().contentToString();
        long qq = event.getSender().getId();
        for (Map.Entry<String, Pattern> mapEntry : regexMap.entrySet()) {
            if (mapEntry.getValue().matcher(msg).find()) {
                ArrayList<WordsItem> list = dictionary.words.get(mapEntry.getKey());
                final GoodwillLevel gl = GoodwillLevel.getLevel(UserInfoManager.getInstance().getCoins(qq));

                WordsItem wordItem = SJFRandom.randomSelect(new ArrayList<>(list) {{
                    removeIf(entry1 -> !goodwillMatch(entry1, gl));
                }});
                if (wordItem.probability != 100) {
                    if (SJFRandom.randomInt(100) > wordItem.probability) {
                        return false;
                    }
                }
                MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                if (wordItem.isFlag(WordsItem.QUOTE)) {
                    messageChainBuilder.add(new QuoteReply(event.getSource()));
                } else if (wordItem.isFlag(WordsItem.AT)) {
                    messageChainBuilder.add(new At(qq));
                }
                Iterator<WordNode> iterator = wordItem.entryList.iterator();
                while (iterator.hasNext()) {
                    WordNode node = iterator.next();
                    try {
                        switch (node.type) {
                            case TEXT:
                                messageChainBuilder.add(node.content);
                                break;
                            case IMAGE:
                                try {
                                    messageChainBuilder.add(botWrapper.toImage(new File(SJFPathTool.getWordStockImagePath() + node.content), event.getGroup()));
                                } catch (Exception e) {
                                    ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                                }
                                break;
                            case QQ_NUMBER:
                                messageChainBuilder.add(String.valueOf(event.getSender().getId()));
                                break;
                            case QQ_NICK:
                                messageChainBuilder.add(event.getSenderName());
                                break;
                            case QQ_NICK_IN_GROUP:
                                messageChainBuilder.append(event.getSender().getNameCard());
                                break;
                            case GROUP_NUMBER:
                                messageChainBuilder.add(String.valueOf(event.getGroup().getId()));
                                break;
                            case GROUP_NAME:
                                messageChainBuilder.add(event.getGroup().getName());
                                break;
                            case RANDOM_INT:
                                int b = -1;
                                try {
                                    b = Integer.parseInt(node.content);
                                } catch (NumberFormatException ignore) {
                                }
                                ThreadLocalRandom current = ThreadLocalRandom.current();
                                messageChainBuilder.add(String.valueOf(b == -1 ? current.nextInt() : current.nextInt(b)));
                                break;
                            case RANDOM_FLOAT:
                                float scale = 1f;
                                try {
                                    scale = Float.parseFloat(node.content);
                                } catch (NumberFormatException ignore) {
                                }
                                messageChainBuilder.add(String.valueOf(ThreadLocalRandom.current().nextFloat() * scale));
                                break;
                            case HASH_RANDOM_INT:
                                int hi = -1;
                                try {
                                    hi = Integer.parseInt(node.content);
                                } catch (NumberFormatException ignore) {
                                }
                                messageChainBuilder.add(String.valueOf(SJFRandom.hashSelectInt(event.getSender().getId(), hi)));
                                break;
                            case HASH_RANDOM_FLOAT:
                                float rscale = 1f;
                                try {
                                    rscale = Float.parseFloat(node.content);
                                } catch (NumberFormatException ignore) {
                                }
                                messageChainBuilder.add(String.valueOf(SJFRandom.hashSelectFloat(event.getSender().getId(), rscale)));
                                break;
                            case IMAGE_FOLDER:
                                messageChainBuilder.add(botWrapper.toImage(SJFRandom.randomSelect(new File(SJFPathTool.getAppDirectory() + node.content).listFiles()), event.getGroup()));
                                break;
                            case VOICE:
                                messageChainBuilder.add(botWrapper.toAudio(new File(SJFPathTool.getAppDirectory() + node.content), event.getGroup()));
                                break;
                            case FLASH_IMAGE:
                                Image image = botWrapper.toImage(new File(SJFPathTool.getAppDirectory() + node.content), event.getGroup());
                                messageChainBuilder.append(new FlashImage(image));
                                break;
                            case NUDGE:
                                event.getSender().nudge().sendTo(event.getSubject());
                                break;
                            case DICE:
                                int dicei = 0;
                                try {
                                    dicei = Integer.parseInt(node.content);
                                } catch (NumberFormatException ignore) {
                                }
                                messageChainBuilder.append(new Dice(dicei == 0 ? SJFRandom.randomInt(6) + 1 : dicei));
                                break;
                            case CALL_METHOD:
                                int args = Integer.parseInt(node.content);
                                List<String> cmds = new ArrayList<>();
                                for (int i = 0; i < args; ++i) {
                                    cmds.add(iterator.next().content);
                                }
                                messageChainBuilder.add(moduleManager.getModule(ReflexCommand.class).invoke(cmds));
                                break;
                            case TTS:
                                messageChainBuilder.append(botWrapper.toAudio(LkaaApi.generalVoice(node.content), event.getGroup()));
                                break;
                            case RANDOM_AVATAR_IN_GROUP:
                                NormalMember member = SJFRandom.randomSelect(event.getGroup().getMembers().toArray(new NormalMember[0]));
                                messageChainBuilder.append(botWrapper.toImage(new URL(member.getAvatarUrl()), event.getGroup()));
                                break;
                            case SENDER_AVATAR:
                                messageChainBuilder.append(botWrapper.toImage(new URL(event.getSender().getAvatarUrl()), event.getGroup()));
                                break;
                            case GROUP_AVATAR:
                                messageChainBuilder.append(botWrapper.toImage(new URL(event.getGroup().getAvatarUrl()), event.getGroup()));
                                break;
                            case RAMDOM_MEMBER_IN_GROUP:
                                messageChainBuilder.append(String.valueOf(SJFRandom.randomSelect(event.getGroup().getMembers().toArray(new NormalMember[0])).getId()));
                                break;
                        }
                    } catch (Exception e) {
                        if (botWrapper.debug) {
                            e.printStackTrace();
                        }
                        ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                    }
                }
                int[] id = sendGroupMessage(event.getGroup().getId(), messageChainBuilder.asMessageChain()).getIds();
                if (wordItem.isFlag(WordsItem.AUTO_RECALL)) {
                    botMessageHandler.autoRecall(id);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNudge(NudgeEvent event) {
        if (event.getTarget().getId() != botWrapper.getId()) {
            return false;
        }
        if (event.getFrom().getId() == botWrapper.getId()) {
            return false;
        }
        if (dictionary.words.containsKey("onNudge")) {
            GroupMessageEvent groupMessageEvent =
                    new GroupMessageEvent(
                            event.getFrom().getNick(),
                            MemberPermission.MEMBER, botWrapper.getGroupMember(event.getSubject().getId(), event.getFrom().getId()),
                            new MessageChainBuilder().asMessageChain().plus("onNudge"),
                            (int) (System.currentTimeMillis() / 1000));
            onGroupMessage(groupMessageEvent);
            return true;
        }
        int flag = SJFRandom.randomInt(14);
        if (flag < 3) {
            try {
                File imageFile = botWrapper.getAvatarFile(event.getFrom());
                byte[] bytes = moduleManager.getModule(ImageProcess.class).randomTransaction(null, imageFile, event.getFrom());
                sendGroupMessage(event.getSubject().getId(), botWrapper.toImage(bytes, event.getSubject()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (flag < 11) {
            String[] nu = new String[]{"你群日常乱戳∠( ᐛ 」∠)_", "_(•̀ω•́ 」∠)_", "_(:３っ　)へ杰哥你又在戳哦", "(눈‸눈)你这戳一戳包熟吗", " (⊙o⊙)我家房子还蛮大的,可以好好戳",
                    "别戳啦、( ´◔‸◔`)", "戳一戳计数:114514", "戳一戳计数:1919810"};
            sendGroupMessage(event.getSubject().getId(), nu[flag - 3]);
        } else if (flag == 11) {
            sendGroupMessage(event.getSubject().getId(), "戳回去 ⸜(๑'ᵕ'๑)⸝⋆*");
            event.getFrom().nudge().sendTo(event.getSubject());
            return true;
        } else {
            File folder = botWrapper.personality.getVoiceFolder();
            if (folder.exists()) {
                Contact subject = event.getSubject();
                if (subject instanceof Group group) {
                    Audio audio = botWrapper.toAudio(SJFRandom.randomSelect(folder.listFiles()), group);
                    sendMessage(group, audio);
                }
            }
        }
        return false;
    }

    private boolean goodwillMatch(WordsItem entry, GoodwillLevel gl) {
        if (entry.isFlag(WordsItem.E) && gl == GoodwillLevel.EASY) {
            return true;
        }
        if (entry.isFlag(WordsItem.N) && gl == GoodwillLevel.NORMAL) {
            return true;
        }
        if (entry.isFlag(WordsItem.H) && gl == GoodwillLevel.HARD) {
            return true;
        }
        if (entry.isFlag(WordsItem.L) && gl == GoodwillLevel.LUNATIC) {
            return true;
        }
        return entry.isFlag(WordsItem.X) && gl == GoodwillLevel.EXTRA;
    }

    @Override
    public WordsStock load() {
        DataPersistenter.read(this);
        regexMap = new HashMap<>();
        for (String key : dictionary.words.keySet()) {
            regexMap.put(key, Pattern.compile(key));
        }
        return this;
    }

    public enum NodeType {
        TEXT,
        IMAGE,
        QQ_NUMBER,
        QQ_NICK,
        QQ_NICK_IN_GROUP,

        GROUP_NUMBER,
        GROUP_NAME,
        RANDOM_INT,
        RANDOM_FLOAT,
        HASH_RANDOM_INT,
        HASH_RANDOM_FLOAT,
        IMAGE_FOLDER,

        VOICE,
        FLASH_IMAGE,
        NUDGE,
        DICE,
        CALL_METHOD,
        TTS,
        RANDOM_AVATAR_IN_GROUP,
        SENDER_AVATAR,
        GROUP_AVATAR,
        RAMDOM_MEMBER_IN_GROUP,
    }

    public enum GoodwillLevel {
        ALL("十六夜"),
        EASY("新月级"),
        NORMAL("三日月级"),
        HARD("半月级"),
        LUNATIC("满月级"),
        EXTRA("暗月级");

        private String note = null;

        GoodwillLevel(String s) {
            note = s;
        }

        public static GoodwillLevel getLevel(int i) {
            if (i == -1) {
                return ALL;
            }
            if (i >= 0 && i < 1000) {
                return EASY;
            }
            if (i >= 1000 && i < 3000) {
                return NORMAL;
            }
            if (i >= 3000 && i < 6000) {
                return HARD;
            }
            if (i >= 6000 && i < 9961) {
                return LUNATIC;
            }
            if (i >= 9961) {
                return EXTRA;
            }
            throw new IllegalArgumentException();
        }
    }

    public static class WordStock {
        @SerializedName("w")
        public HashMap<String, ArrayList<WordsItem>> words = new HashMap<>();
    }

    private static class WordsItem {
        public static final int E = 1 << 8;
        public static final int N = 1 << 7;
        public static final int H = 1 << 6;
        public static final int L = 1 << 5;
        public static final int X = 1 << 4;

        public static final int AUTO_RECALL = 1 << 3;

        public static final int NORMAL = 1 << 2;
        public static final int AT = 1 << 1;
        public static final int QUOTE = 1 << 0;
        @SerializedName("p")
        public int probability = 100;

        //                                                 E N H L X  auto recall normal at quote
        // 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 | 0 0 0 0 0 |     0     |   0    0   0
        @SerializedName("f")
        public int flag = 0b111110100; // E | N | H | L | X | normal
        @SerializedName("e")
        public ArrayList<WordNode> entryList = new ArrayList<>();

        public void add(WordNode wordNode) {
            entryList.add(wordNode);
        }

        public void setFlagBit(int index) {
            flag |= index;
        }

        public void clearFlagBit(int index) {
            flag &= ~index;
        }

        public boolean isFlag(int index) {
            return (flag & index) != 0;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException();
        }

    }

    private static class WordNode {

        @SerializedName("t")
        public NodeType type;
        @SerializedName("c")
        public String content = "";
        // @SerializedName("m")
        // public int mode = 0;
        //0 : equals
        //1 : contains
        //2 : startWith
        //3 : endWith
        //4 : regex

        public WordNode() {
        }

        public WordNode(NodeType type) {
            this.type = type;
        }

        public WordNode(String content) {
            if (content != null) {
                this.content = content;
            }
            this.type = NodeType.TEXT;
        }

        public WordNode(String content, NodeType type) {
            if (content != null) {
                this.content = content;
            }
            this.type = type;
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException();
        }

    }

}
