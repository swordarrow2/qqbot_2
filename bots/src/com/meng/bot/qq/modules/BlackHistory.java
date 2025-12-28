package com.meng.bot.qq.modules;

import com.meng.bot.config.Functions;
import com.meng.bot.config.Person;
import com.meng.bot.config.QQGroupConfig;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.FileTool;
import com.meng.tools.normal.Network;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlackHistory extends BaseModule implements IGroupMessageEvent {

    public BlackHistory(BotWrapper b) {
        super(b);
    }

    // 状态映射：发送者ID -> 目标用户ID（null表示不在状态，0表示等待输入目标用户，>0表示已选择目标用户）
    protected Map<Long, Long> stateMap = new HashMap<>();

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        QQGroupConfig config = configManager.getGroupConfig(event.getGroup().getId());
        if (config == null || !config.isFunctionEnabled(Functions.BlackHistory)) {
            return false;
        }
        String msg = event.getMessage().contentToString();
        if (!msg.startsWith("添加") && msg.endsWith("迫害图") && msg.length() > 3) {
            handleGetImage(event, msg);
            return true;
        }
        final long sender = event.getSender().getId();
        Person personFromQQ = configManager.getPersonFromQQ(sender);
        if (personFromQQ == null || !personFromQQ.hasAdminPermission()) {
            return false;
        }
        return handleAddImage(event, msg, sender);
    }

    private boolean handleAddImage(GroupMessageEvent event, String msg, long sender) {
        Long state = stateMap.get(sender);
        // 如果在状态中
        if (state != null) {
            if ("取消".equals(msg)) {
                sendQuote(event, "已取消添加");
                stateMap.remove(sender);
                return true;
            }
            // 状态0：等待输入目标用户
            if (state == 0L) {
                processTargetUserInput(event, msg, sender);
                return true;
            }
            // 状态>0：接收并保存图片
            processSaveImages(event, sender, state);
            return true;
        }
        if ("添加迫害图".equals(msg)) {
            sendQuote(event, "发送QQ号码或@目标用户，或发送\"取消\"退出");
            stateMap.put(sender, 0L);
            return true;
        }

        return false;
    }

    private void processTargetUserInput(GroupMessageEvent event, String msg, long sender) {
        long target = botWrapper.getAt(event.getMessage());
        if (target <= 0) {
            try {
                target = Long.parseLong(msg);
            } catch (NumberFormatException e) {
                sendQuote(event, "QQ号格式错误，请发送纯数字QQ号或@目标用户，或发送\"取消\"退出");
                return;
            }
        }
        stateMap.put(sender, target);
        sendQuote(event, "请发送要为[" + target + "]添加的图片，或发送\"取消\"退出");
    }

    private void processSaveImages(GroupMessageEvent event, long sender, long targetUserId) {
        boolean hasImage = false;
        int totalSize = 0;
        int savedCount = 0;
        for (Message message : event.getMessage()) {
            if (message instanceof Image) {
                hasImage = true;
                try {
                    String imageUrl = botWrapper.getUrl(((Image) message));
                    byte[] imgData = Network.httpGetRaw(imageUrl);
                    if (imgData == null || imgData.length == 0) {
                        continue;
                    }
                    // 确保目标文件夹存在
                    File targetDir = SJFPathTool.getBlackHistoryPath(String.valueOf(targetUserId));
                    if (!targetDir.exists()) {
                        targetDir.mkdirs();
                    }
                    // 保存文件
                    FileTool.saveFile(new File(targetDir.getAbsolutePath() + File.separator + FileTool.getAutoFileName(imgData)), imgData);
                    savedCount++;
                    totalSize += imgData.length;
                } catch (Exception e) {
                    sendQuote(event, "处理图片时出错: " + e.getMessage());
                    if (botWrapper.debug) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (hasImage) {
            if (savedCount > 0) {
                String sizeInfo = String.format("%.1fKB", totalSize / 1024.0);
                sendQuote(event, "成功为[" + targetUserId + "]保存了" + savedCount + "张图片，总计" + sizeInfo);
            }
            stateMap.remove(sender);
        } else {
            sendQuote(event, "未检测到图片");
            stateMap.remove(sender);
        }
    }

    private void handleGetImage(GroupMessageEvent event, String msg) {
        Person person;
        long target = botWrapper.getAt(event.getMessage());
        if (target > 0) {
            person = configManager.getPersonFromQQ(target);
        } else {
            String prefix = msg.substring(0, msg.length() - 3).trim();
            person = configManager.getPersonFromName(prefix);
            if (person == null) {
                try {
                    long qq = Long.parseLong(prefix);
                    person = configManager.getPersonFromQQ(qq);
                } catch (NumberFormatException ignore) {
                }
            }
        }
        if (person == null) {
            sendQuote(event, "未找到该用户");
            return;
        }
        File targetDir = SJFPathTool.getBlackHistoryPath(String.valueOf(person.qq));
        if (!targetDir.exists()) {
            targetDir = SJFPathTool.getBlackHistoryPath(person.name);
        }
        File[] listFiles = targetDir.listFiles();

        if (listFiles == null || listFiles.length == 0) {
            sendQuote(event, "该用户暂无迫害图");
            return;
        }
        File selectedImage = SJFRandom.randomSelect(listFiles);
        if (selectedImage != null && selectedImage.exists()) {
            sendMessage(event, botWrapper.toImage(selectedImage, event.getGroup()));
        } else {
            sendQuote(event, "图片文件不存在或已损坏");
        }
    }
}