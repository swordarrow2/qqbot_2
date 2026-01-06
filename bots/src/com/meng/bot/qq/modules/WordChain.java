package com.meng.bot.qq.modules;

import com.google.gson.annotations.SerializedName;
import com.meng.bot.annotation.BotData;
import com.meng.bot.config.DataPersistenter;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.FileWatcherService;
import com.meng.tools.normal.Pair;
import com.meng.tools.sjf.SJFPathTool;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WordChain extends BaseModule implements IGroupMessageEvent {
//    {
//        "sequence": [
//        {"sentence":[{"text":"我追着梦的光点"},{"text":"是因为有了勇气才不怕危险"},{"text":"你的爱没有上限"},{"text":"给我力量继续往前"}]},
//        {"sentence":[{"text":"力微任重久神疲"},{"text":"再竭衰庸定不支"},{"text":"苟利国家生死以"},{"text":"岂因祸福避趋之",} {"text":"谪居正是君恩厚"},{"text":"养拙刚于戍卒宜"},{"text":"戏与山妻谈故事"},{"text":"试吟断送老头皮"}]}
//  ]
//}

    @BotData("sentence_chain.json")
    public Chains chain = new Chains();

    public WordChain load() {
        DataPersistenter.read(this);
        return this;
    }

    private final Map<Long, Pair<Integer, Integer>> userStates = new HashMap<>();// Map<qq,Pair<sequenceIndex,sentenceIndex>>

    public WordChain(BotWrapper botWrapper) {
        super(botWrapper);
        load();
    }

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        String message = event.getMessage().contentToString().trim();
        long qq = event.getSender().getId();
        Pair<Integer, Integer> pair = userStates.get(qq);
        if (pair == null) {
            for (int i = 0; i < chain.sequence.size(); i++) {
                Sequence sequence = chain.get(i);
                if (sequence.get(0).text.equals(message)) {
                    pair = new Pair<>(i, 0);
                    userStates.put(qq, pair);
                    if (sequence.size() > 1) {
                        sendMessage(event.getGroup(), sequence.get(1).text);
                        pair.setSecond(2);
                        return true;
                    } else {
                        userStates.remove(qq);
                        return false;
                    }
                }
            }
            return false;
        }

        Sequence sequence = chain.get(pair.getFirst());
        int expectedIndex = pair.getSecond();
        if (expectedIndex >= sequence.size() || !message.equals(sequence.get(expectedIndex).text)) {
            userStates.remove(qq);
            return true;
        }
        int nextIndex = expectedIndex + 1;
        if (nextIndex >= sequence.size()) {
            userStates.remove(qq);
            return false;
        }
        sendMessage(event.getGroup(), sequence.get(nextIndex).text);
        int userNextIndex = nextIndex + 1;
        if (userNextIndex >= sequence.size()) {
            userStates.remove(qq);
            return false;
        }
        pair.setSecond(userNextIndex);
        return true;
    }

    public static class Chains implements Iterable<Sequence> {
        @SerializedName("sequence")
        public ArrayList<Sequence> sequence = new ArrayList<>();

        public Sequence get(int i) {
            return sequence.get(i);
        }

        @Override
        public @NotNull Iterator<Sequence> iterator() {
            return sequence.iterator();
        }
    }

    public static class Sequence implements Iterable<Sentence> {
        @SerializedName("sentence")
        public ArrayList<Sentence> sentence = new ArrayList<>();

        public Sentence get(int i) {
            return sentence.get(i);
        }

        public int size() {
            return sentence.size();
        }

        @Override
        public @NotNull Iterator<Sentence> iterator() {
            return sentence.iterator();
        }
    }

    public static class Sentence {
        @SerializedName("text")
        public String text;

        @Override
        public String toString() {
            return text;
        }
    }
}