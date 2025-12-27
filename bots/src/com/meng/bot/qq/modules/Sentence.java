package com.meng.bot.qq.modules;

public class Sentence{// extends BaseModule implements IGroupMessageEvent {
//    @BotData("sentence.json")
//
//    public Sentences sens = new Sentences();
//    /*
//     {l:[{s:"春宵一刻值千金",t:""},{s:"千金散尽还复来",t:""}]}
//     */
//    private File content;
//
//    public Sentence(BotHelper botHelper) {
//        super(botHelper);
//    }
//
//    @Override
//    public boolean onGroupMessage(GroupMessageEvent event) {
//        if (event.getMessage().contentToString().equals(".sentence")) {
//            sendMessage(event.getGroup(), SJFRandom.randomSelect(sens.sentences).toString());
//            return true;
//        }
//        return false;
//    }
//
//    public static class Sentences {
//        @SerializedName("l")
//        public ArrayList<SingleSentence> sentences = new ArrayList<>();
//    }
//
//    public static class SingleSentence {
//        @SerializedName("s")
//        public String text;
//        @SerializedName("t")
//        public String tag;
//
//        @Override
//        public String toString() {
//            return text;
//        }
//    }
}
