package com.meng.bot.qq.modules;

import com.meng.api.Character;
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
import java.util.HashSet;
import java.util.Map;

public class SetuManager extends BaseModule implements IGroupMessageEvent {

    public SetuManager(BotWrapper b) {
        super(b);
    }

    // 状态映射：发送者ID -> 状态
    // null: 不在状态
    // "waiting_for_character": 等待选择角色
    // 角色名: 等待接收图片
    protected Map<Long, String> stateMap = new HashMap<>();

    @Override
    public boolean onGroupMessage(GroupMessageEvent event) {
        long groupId = event.getGroup().getId();
        long senderId = event.getSender().getId();
        QQGroupConfig config = configManager.getGroupConfig(groupId);
        if (config == null || !config.isFunctionEnabled(Functions.ACGImages)) {
            return false;
        }
        String msg = event.getMessage().contentToString().trim();
        if (!msg.startsWith("添加") && msg.endsWith("色图") && msg.length() > 2) {
            return handleGetSetu(event, msg);
        }
        Person personFromQQ = configManager.getPersonFromQQ(senderId);
        if (personFromQQ == null || !personFromQQ.hasAdminPermission()) {
            return false;
        }
        if (msg.startsWith("添加色图名单 ")) {
            handleAddCharacterList(event, msg);
            return true;
        }
        return handleStateMachine(event, msg, senderId);
    }

    private boolean handleGetSetu(GroupMessageEvent event, String msg) {
        // 提取角色名，如"亚丝娜色图" -> "亚丝娜"
        String characterName = msg.substring(0, msg.length() - 2).trim();
        if (characterName.isEmpty()) {
            return false;
        }
        // 通过角色名获取标准角色名（如果需要）
        // final String standardName = Character.getInstance().getCharaterName(characterName);
        // if (standardName != null) {
        //     characterName = standardName;
        // }
        File[] folders = SJFPathTool.getR15Path("").listFiles(file -> file.isDirectory() && file.getName().contains(characterName));
        if (folders == null || folders.length == 0) {
            sendMessage(event, "未找到相关图片");
            return true;
        }
        // 收集所有图片文件
        HashSet<File> imageFiles = new HashSet<>();
        for (File folder : folders) {
            if (folder.isDirectory()) {
                imageFiles.addAll(FileTool.listAllFiles(folder));
            }
        }
        if (imageFiles.isEmpty()) {
            sendMessage(event, "未找到相关图片");
            return true;
        }
        // 随机选择一张图片并发送
        File selectedImage = SJFRandom.randomSelect(imageFiles);
        if (selectedImage != null && selectedImage.exists()) {
            sendMessage(event, botWrapper.toImage(selectedImage, event.getGroup()));
            return true;
        }
        sendMessage(event, "未找到相关图片");
        return true;
    }

    private void handleAddCharacterList(GroupMessageEvent event, String msg) {
        String characterName = msg.substring("添加色图名单 ".length()).trim();
        if (characterName.isEmpty()) {
            sendQuote(event, "角色名不能为空");
            return;
        }
        File characterDir = SJFPathTool.getR15Path(characterName);
        if (characterDir.exists()) {
            sendQuote(event, "角色已存在");
            return;
        }
        boolean created = characterDir.mkdirs();
        if (created) {
            sendQuote(event, "添加角色成功: " + characterName);
        } else {
            sendQuote(event, "添加角色失败");
        }
    }

    private boolean handleStateMachine(GroupMessageEvent event, String msg, long senderId) {
        String state = stateMap.get(senderId);
        if ("取消添加".equals(msg)) {
            if (state != null) {
                sendQuote(event, "已取消添加");
                stateMap.remove(senderId);
            }
            return true;
        }
        if (state == null) {
            if ("添加色图".equals(msg)) {
                sendQuote(event, "发送角色名以选择要添加图片的角色");
                stateMap.put(senderId, "waiting_for_character");
                return true;
            }
            return false;
        }
        // 状态：等待选择角色
        if ("waiting_for_character".equals(state)) {
            processCharacterSelection(event, msg, senderId);
            return true;
        }
        // 状态：等待接收图片（state为角色名）
        return processSaveImages(event, senderId, state);
    }

    private void processCharacterSelection(GroupMessageEvent event, String msg, long senderId) {
        // 获取标准角色名
        String characterName = Character.getInstance().getCharaterName(msg);
        if (characterName == null) {
            // 如果角色不存在，检查是否要创建新角色
            sendQuote(event, "角色\"" + msg + "\"不存在，是否创建？(回复\"是\"创建，其他取消)");
            stateMap.put(senderId, "confirm_create:" + msg);
            return;
        }

        // 检查角色文件夹是否存在
        File characterDir = SJFPathTool.getR15Path(characterName);
        if (!characterDir.exists()) {
            sendQuote(event, "角色文件夹不存在，是否创建？(回复\"是\"创建，其他取消)");
            stateMap.put(senderId, "confirm_create:" + characterName);
            return;
        }
        // 角色存在，进入等待图片状态
        stateMap.put(senderId, characterName);
        sendQuote(event, "请发送要为[" + characterName + "]添加的图片，或发送\"取消添加\"退出");
    }

    private boolean processSaveImages(GroupMessageEvent event, long senderId, String state) {
        // 检查是否是确认创建角色的状态
        if (state.startsWith("confirm_create:")) {
            return processCreateCharacterConfirmation(event, senderId, state);
        }
        // 正常保存图片状态
        String characterName = state;
        boolean hasImage = false;
        int savedCount = 0;
        long totalSize = 0;

        for (Message message : event.getMessage()) {
            if (message instanceof Image) {
                hasImage = true;
                try {
                    String imageUrl = botWrapper.getUrl(((Image) message));
                    byte[] imgData = Network.httpGetRaw(imageUrl);
                    if (imgData == null || imgData.length == 0) {
                        continue;
                    }
                    File characterDir = SJFPathTool.getR15Path(characterName);
                    if (!characterDir.exists()) {
                        characterDir.mkdirs();
                    }
                    FileTool.saveFile(new File(characterDir.getAbsolutePath() + File.separator + FileTool.getAutoFileName(imgData)), imgData);
                    savedCount++;
                    totalSize += imgData.length;
                } catch (IOException e) {
                    sendQuote(event, "保存图片失败: " + e.getMessage());
                    if (botWrapper.debug) {
                        e.printStackTrace();
                    }
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
                String sizeInfo;
                if (totalSize < 1024) {
                    sizeInfo = totalSize + "B";
                } else if (totalSize < 1024 * 1024) {
                    sizeInfo = String.format("%.1fKB", totalSize / 1024.0);
                } else {
                    sizeInfo = String.format("%.1fMB", totalSize / (1024.0 * 1024.0));
                }
                sendQuote(event, "成功为[" + characterName + "]保存了" + savedCount + "张图片，总计" + sizeInfo);
            } else {
                sendQuote(event, "未成功保存任何图片");
            }
        } else {
            sendQuote(event, "未检测到图片");
        }
        stateMap.remove(senderId);
        return true;
    }

    private boolean processCreateCharacterConfirmation(GroupMessageEvent event, long senderId, String state) {
        String msg = event.getMessage().contentToString().trim();
        String characterName = state.substring("confirm_create:".length());
        if ("是".equals(msg) || "yes".equalsIgnoreCase(msg) || "y".equalsIgnoreCase(msg)) {
            // 创建角色文件夹
            File characterDir = SJFPathTool.getR15Path(characterName);
            boolean created = characterDir.mkdirs();
            if (created) {
                sendQuote(event, "角色[" + characterName + "]创建成功");
                stateMap.put(senderId, characterName);
                sendQuote(event, "请发送要为[" + characterName + "]添加的图片，或发送\"取消添加\"退出");
            } else {
                sendQuote(event, "创建角色文件夹失败");
                stateMap.remove(senderId);
            }
        } else {
            sendQuote(event, "已取消创建角色");
            stateMap.remove(senderId);
        }
        return true;
    }
}