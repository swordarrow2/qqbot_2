package com.meng.bot.qq.modules;

import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.bot.qq.handler.group.INudgeEvent;
import com.meng.tools.sjf.SJFPathTool;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;

public class ChatCounter extends BaseModule implements IGroupMessageEvent, INudgeEvent {

    private Connection connection = null;
    private static final String Drivde = "org.sqlite.JDBC";

    /**
     * 获取指定群号的最近100条聊天记录
     *
     * @param groupId 群号
     * @return 包含最近100条聊天记录的List，每个元素是一个包含时间、QQ号和消息内容的字符串
     */
    public List<String> getRecent100ChatRecords(long groupId) {
        List<String> chatRecords = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    String.format("SELECT sendTime, qq, sentence FROM g%d ORDER BY sendTime DESC LIMIT 100", groupId)
            );
            while (resultSet.next()) {
                long sendTime = resultSet.getLong("sendTime");
                long qq = resultSet.getLong("qq");
                String sentence = resultSet.getString("sentence");
                String record = String.format("[%s] QQ:%d -> %s", new Date(sendTime), qq, sentence);
                chatRecords.add(record);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chatRecords;
    }

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
            } else if (content.equals("查看最近发言")) {
                BufferedImage chartImage = generateWeeklyChatChart(event.getGroup().getId());
                if (chartImage != null) {
                    sendMessage(event.getGroup(), botWrapper.toImage(bufferedImageToBytes(chartImage), event.getGroup()));
                }
            }
        } catch (Exception e) {
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
        return "包含:\n" + contains + "\n完全相同:\n" + equals;
    }

    @Nullable
    private String processCommand(GroupMessageEvent event, long gid) throws SQLException {
        GroupChatInfo groupInfo = getGroupInfo(gid);
        StringBuilder stringBuilder = new StringBuilder("你群活跃度:");
        stringBuilder.append("\n总发言数:").append(groupInfo.groupCount.sentence);
        ArrayList<Map.Entry<Long, CountBean>> list = new ArrayList<>(groupInfo.hashMap.entrySet());

        list.sort((o1, o2) -> {
            if (Objects.equals(o1.getValue().sentence, o2.getValue().sentence)) {
                return 0;
            }
            return o1.getValue().sentence > o2.getValue().sentence ? -1 : 1;
        });
        int countSentence = Math.min(list.size(), 10);
        for (int i = 0; i < countSentence; i++) {
            Map.Entry<Long, CountBean> obj = list.get(i);
            Long qq = obj.getKey();

            stringBuilder.append("\nNo.").append(i + 1).append(":").append(configManager.getNickName(gid, qq))
                    .append("(").append(qq).append(")").append("->").append(obj.getValue().sentence).append("条");
        }
        list.sort((o1, o2) -> {
            if (Objects.equals(o1.getValue().nudge, o2.getValue().nudge)) {
                return 0;
            }
            return o1.getValue().nudge > o2.getValue().nudge ? -1 : 1;
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

    public BufferedImage generateWeeklyChatChart(long groupId) {
        setChineseChartTheme();
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            Map<String, Integer> dailyCounts = getWeeklyChatCounts(groupId);
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(formatter);
                String dateKey = date.toString();
                int count = dailyCounts.getOrDefault(dateKey, 0);
                dataset.addValue(count, "发言数量", dateStr);
            }
            JFreeChart chart = ChartFactory.createBarChart(
                    "群 " + groupId + " 最近一周发言统计", // 图表标题
                    "日期",                            // X轴标签
                    "发言数量",                        // Y轴标签
                    dataset,                          // 数据集
                    PlotOrientation.VERTICAL,         // 方向
                    true,                            // 包含图例
                    true,                            // 显示工具提示
                    false                            // 不显示URL
            );
//            chart.setAntiAlias(true);
            customizeChart(chart);
            return chart.createBufferedImage(1440, 720);
        } catch (Exception e) {
            e.printStackTrace();
            sendGroupMessage(groupId, e.toString());
            return null;
        }
    }

    private Font getChineseFont() {
        String[] fontNames = {"Microsoft YaHei", "SimHei", "SimSun", "STSong", "STKaiti", "FangSong"};
        for (String fontName : fontNames) {
            try {
                Font font = new Font(fontName, Font.PLAIN, 12);
                if (font.getName().contains(fontName)) {
                    return font;
                }
            } catch (Exception e) {
                // 继续尝试下一个字体
            }
        }
        return new Font("Serif", Font.PLAIN, 12);
    }

    private void customizeChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // 设置柱状图颜色
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        renderer.setItemMargin(0.2);
        Font axisFont = getChineseFont();
        plot.getDomainAxis().setLabelFont(axisFont.deriveFont(Font.PLAIN, 14));
        plot.getRangeAxis().setLabelFont(axisFont.deriveFont(Font.PLAIN, 14));
        plot.getDomainAxis().setTickLabelFont(axisFont.deriveFont(Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(axisFont.deriveFont(Font.PLAIN, 12));
        Font legendFont = getChineseFont();
        chart.getLegend().setItemFont(legendFont.deriveFont(Font.PLAIN, 12));
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.CategoryItemLabelGenerator() {
            @Override
            public String generateRowLabel(CategoryDataset dataset, int row) {
                return null;
            }

            @Override
            public String generateColumnLabel(CategoryDataset dataset, int column) {
                return null;
            }

            @Override
            public String generateLabel(CategoryDataset dataset, int row, int column) {
                Number value = dataset.getValue(row, column);
                if (value != null) {
                    return String.valueOf(value.intValue());
                }
                return "";
            }
        });
        renderer.setDefaultItemLabelsVisible(true);
        Font valueFont = getChineseFont();
        renderer.setDefaultItemLabelFont(valueFont.deriveFont(Font.PLAIN, 11));
    }

    private void setChineseChartTheme() {
        Font chineseFont = null;
        try {
            String[] fontNames = {"Microsoft YaHei", "SimHei", "SimSun", "STSong"};
            for (String fontName : fontNames) {
                Font font = new Font(fontName, Font.PLAIN, 12);
                if (font.getName().contains(fontName)) {
                    chineseFont = font;
                    break;
                }
            }
        } catch (Exception e) {
            chineseFont = new Font("Serif", Font.PLAIN, 12);
        }
        StandardChartTheme chartTheme = new StandardChartTheme("CN");
        if (chineseFont != null) {
            chartTheme.setExtraLargeFont(chineseFont.deriveFont(Font.BOLD, 20));
            chartTheme.setLargeFont(chineseFont.deriveFont(Font.BOLD, 16));
            chartTheme.setRegularFont(chineseFont.deriveFont(Font.PLAIN, 14));
            chartTheme.setSmallFont(chineseFont.deriveFont(Font.PLAIN, 12));
        }
        ChartFactory.setChartTheme(chartTheme);
    }

    /**
     * 获取指定群组最近一周每天的发言数量
     *
     * @param groupId 群号
     * @return Map<日期字符串, 发言数量>
     */
    private Map<String, Integer> getWeeklyChatCounts(long groupId) throws SQLException {
        Map<String, Integer> dailyCounts = new HashMap<>();
        // 计算一周前的时间戳（毫秒）
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        String sql = String.format(
                "SELECT date(datetime(sendTime/1000, 'unixepoch')) as day, COUNT(*) as count " +
                        "FROM g%d " +
                        "WHERE sendTime >= %d " +
                        "GROUP BY day " +
                        "ORDER BY day",
                groupId, oneWeekAgo
        );
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String day = resultSet.getString("day");
                int count = resultSet.getInt("count");
                dailyCounts.put(day, count);
            }
        }
        return dailyCounts;
    }

    public byte[] bufferedImageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public BufferedImage bytesToBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }

    public ChatCounter(BotWrapper botHelper) {
        super(botHelper);
        try {
            Class.forName(Drivde);// 加载驱动,连接sqlite的jdbc
            connection = DriverManager.getConnection("jdbc:sqlite:" + SJFPathTool.getAppDirectory() + botWrapper.getId() + "groupRecord.db");//连接数据库,不存在则创建
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

    private static class GroupChatInfo {
        public CountBean groupCount;
        public HashMap<Long, CountBean> hashMap;

        public GroupChatInfo(CountBean groupCount, HashMap<Long, CountBean> hashMap) {
            this.groupCount = groupCount;
            this.hashMap = hashMap;
        }
    }

    private static class CountBean {
        int sentence = 0;
        int nudge = 0;
    }

}
