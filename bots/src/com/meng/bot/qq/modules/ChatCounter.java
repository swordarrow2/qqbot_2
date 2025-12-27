package com.meng.bot.qq.modules;

import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.bot.qq.handler.group.INudgeEvent;
import com.meng.tools.sjf.SJFPathTool;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.Nullable;

public class ChatCounter extends BaseModule implements IGroupMessageEvent, INudgeEvent {

    private Connection connection = null;
    private static final String Drivde = "org.sqlite.JDBC";

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        long gid = event.getGroup().getId();
        long qid = event.getSender().getId();
        String content = event.getMessage().contentToString();
        add(gid, qid, event.getMessage());
        try {
            if (Command.getCommand(content) == Command.getGroupInfo) {
                String result = processCommand(event, gid);
                if (result == null) {
                    return false;
                }
                sendMessage(event, result);
            } else if (content.startsWith("查看群统计 ")) {
                String text = content.substring("查看群统计 ".length());
                sendGroupMessage(gid, processCommand(gid, text));
            }
        } catch (SQLException e) {
            sendGroupMessage(gid, e.toString());
            e.printStackTrace();
        }
        return false;
    }

    private String processCommand(long group, String text) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(String.format("select sentence from g%d", group));
        int contains = 0;
        int equals = 0;
        while (resultSet.next()) {
            String string = resultSet.getString(1);
            if (string.equals(text)) {
                equals++;
            } else if (string.contains(text)) {
                contains++;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("包含:\n").append(contains).append("\n完全相同:\n").append(equals);
        return stringBuilder.toString();
    }

    @Nullable
    private String processCommand(GroupMessageEvent event, long gid) throws SQLException {
        GroupChatInfo groupInfo = getGroupInfo(gid);
        StringBuilder stringBuilder = new StringBuilder("你群活跃度:");
        stringBuilder.append("\n总发言数:").append(groupInfo.groupCount.sentence);
        ArrayList<Map.Entry<Long, CountBean>> list = new ArrayList<>(groupInfo.hashMap.entrySet());

        list.sort(new Comparator<Map.Entry<Long, CountBean>>() {
                @Override
                public int compare(Map.Entry<Long, CountBean> o1, Map.Entry<Long, CountBean> o2) {
                    if (Objects.equals(o1.getValue().sentence, o2.getValue().sentence)) {
                        return 0;
                    }
                    return o1.getValue().sentence > o2.getValue().sentence ? -1 : 1;
                }
            });
        int countSentence = Math.min(list.size(), 10);
        for (int i = 0; i < countSentence; i++) {
            Map.Entry<Long, CountBean> obj = list.get(i);
            Long qq = obj.getKey();
            NormalMember member = botWrapper.getGroupMember(gid, qq);
            stringBuilder.append("\nNo.").append(i + 1).append(":").append(member.getNick())
                .append("(").append(qq).append(")").append("-").append(obj.getValue().sentence).append("条");
        }
        list.sort(new Comparator<Map.Entry<Long, CountBean>>() {
                @Override
                public int compare(Map.Entry<Long, CountBean> o1, Map.Entry<Long, CountBean> o2) {
                    if (Objects.equals(o1.getValue().nudge, o2.getValue().nudge)) {
                        return 0;
                    }
                    return o1.getValue().nudge > o2.getValue().nudge ? -1 : 1;
                }
            });
        stringBuilder.append("\n总戳一戳:").append(groupInfo.groupCount.nudge);
        int countNudge = Math.min(list.size(), 5);
        for (int i = 0; i < countNudge; i++) {
            Map.Entry<Long, CountBean> obj = list.get(i);
            Long qq = obj.getKey();
            NormalMember member = botWrapper.getGroupMember(gid, qq);
            stringBuilder.append("\nNo.").append(i + 1).append(":").append(member.getNick())
                .append("(").append(qq).append(")").append("-").append(obj.getValue().nudge).append("次");
        }
        return stringBuilder.toString();
    }

    public ChatCounter(BotWrapper botHelper) {
        super(botHelper);
        try {
            Class.forName(Drivde);// 加载驱动,连接sqlite的jdbc
            connection = DriverManager.getConnection("jdbc:sqlite:" + SJFPathTool.getAppDirectory() +  botWrapper.getId() + "groupRecord.db");//连接数据库,不存在则创建
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void addGroup(long group) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format("create table if not exists g%d(id integer primary key ,sendTime bigint,qq bigint,sentence varchar(4000))", group));
            System.out.println("add " + group + " to table");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(long group, long qq, MessageChain message) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(String.format("insert into g%d values(null,%d,%d,'%s')", group, System.currentTimeMillis(), qq, message.serializeToMiraiCode()));
        } catch (SQLException e) {
            addGroup(group);
        }
    }

    @Override
    public boolean onNudge(NudgeEvent event) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(String.format("insert into g%d values(null,%d,%d,'%s')",
                                            event.getSubject().getId(),
                                            System.currentTimeMillis(),
                                            event.getFrom().getId(), event.toString()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private GroupChatInfo getGroupInfo(long group) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(String.format("select qq,sentence from g%d", group));
        HashMap<Long, CountBean> map = new HashMap<>();
        CountBean groupBean = new CountBean();
        while (resultSet.next()) {
            long qq = resultSet.getLong(1);
            CountBean cb = map.get(qq);
            if (cb == null) {
                map.put(qq, cb = new CountBean());
            }
            if (resultSet.getString(2).startsWith("NudgeEvent")) {
                cb.nudge++;
                groupBean.nudge++;
            } else {
                cb.sentence++;
                groupBean.sentence++;
            }
        }
//        ArrayList<Map.Entry<Long, Integer>> entryArrayList = new ArrayList<>(map.entrySet());
//        entryArrayList.sort(new Comparator<Map.Entry<Long, Integer>>() {
//            @Override
//            public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
//                if (Objects.equals(o1.getValue(), o2.getValue())) {
//                    return 0;
//                }
//                return o1.getValue() > o2.getValue() ? -1 : 1;
//            }
//        });
        return new GroupChatInfo(groupBean, map);
    }

    private NudgeBean analyzeNudge(String nu) {
        //NudgeEvent(from=NormalMember(1594703250), target=Bot(1975465607), subject=Group(666247478), action=戳了戳, suffix=)
        String regex = "[0-9]{5,12}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nu);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        try {
            return new NudgeBean(Long.parseLong(list.get(0)), Long.parseLong(list.get(1)), Long.parseLong(list.get(2)));
        } catch (Exception e) {
            return null;
        }
    }

    private static class NudgeBean {
        public final long group;
        public final long from;
        public final long target;

        public NudgeBean(long group, long from, long target) {
            this.group = group;
            this.from = from;
            this.target = target;
        }
    }

    private static class GroupChatInfo {
        public CountBean groupCount;
        public HashMap<Long, CountBean> hashMap;

        public GroupChatInfo(CountBean groupCount, HashMap<Long, CountBean> hashMap) {
            this.groupCount = groupCount;
            this.hashMap = hashMap;
        }
    }

    private class CountBean {
        int sentence = 0;
        int nudge = 0;
    }

}
