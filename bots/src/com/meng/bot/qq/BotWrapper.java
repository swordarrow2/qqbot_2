package com.meng.bot.qq;

import com.meng.bot.config.ConfigManager;
import com.meng.tools.normal.FileFormat;
import com.meng.tools.normal.FileTool;
import com.meng.tools.normal.Hash;
import com.meng.tools.normal.Network;
import com.meng.tools.sjf.SJFPathTool;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class BotWrapper {
    public static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36 QIHU 360EE";
    public static final long mainGroup = 807242547L;
    public static final long yysGroup = 617745343L;

    private Bot bot;
    private BotMessageHandler botMessageHandler;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    public boolean sleeping = false;

    public boolean debug = false;
    public Personality personality = Personality.Yakumo_Ran;

    public BotWrapper() {
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public Stranger getStranger(long qq) {
        return bot.getStranger(qq);
    }

    public void setBotMessageHandler(BotMessageHandler botMessageHandler) {
        this.botMessageHandler = botMessageHandler;
    }

    public BotMessageHandler getBotMessageHandler() {
        return botMessageHandler;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public String findQQInAllGroup(long group, long findQQ) {
        Set<Group> groups = findQQInAllGroup(findQQ);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(findQQ).append("共同群：");
        for (Group l : groups) {
            stringBuilder.append("\n").append(l.getId()).append(l.getName());
        }
        return stringBuilder.toString();
    }

    public Set<Group> findQQInAllGroup(long findQQ) {
        ContactList<Group> groups = bot.getGroups();
        Set<Group> hashSet = new HashSet<>();
        for (Group group : groups) {
            if (group.getId() == 959615179L || group.getId() == 666247478L) {
                continue;
            }
            if (group.contains(findQQ)) {
                hashSet.add(group);
            }
        }
        return hashSet;
    }

    public File downloadTempImage(Image image) throws IOException {
        if (image == null) {
            return null;
        }
        return downloadTempImage(getUrl(image));
    }

    public File downloadTempImage(String url) throws IOException {
        if (url == null) {
            return null;
        }
        byte[] fileBytes = Network.httpGetRaw(url);
        File file = SJFPathTool.getTempPath(Hash.getMd5Instance().calculate(fileBytes) + "." + FileFormat.getFileType(fileBytes));
        FileTool.saveFile(file, fileBytes);
        return file;
    }

    public String getUrl(Image image) {
        return Mirai.getInstance().queryImageUrl(bot, image);
    }

    public Image toImage(File file, Contact contact) {
        return ExternalResource.Companion.uploadAsImage(file, contact);
    }

    public Image toImage(URL url, Contact contact) {
        try {
            return ExternalResource.Companion.uploadAsImage(url.openStream(), contact);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Image toImage(byte[] bytes, Contact contact) {
        return toImage(new ByteArrayInputStream(bytes), contact);
    }

    public Image toImage(InputStream inputStream, Contact contact) {
        return ExternalResource.Companion.uploadAsImage(inputStream, contact);
    }

    public File getAvatarFile(ContactOrBot contact) throws IOException {
        String avatarUrl = contact.getAvatarUrl();
        if (contact instanceof User) {
            int[] specs = {5, 4, 3};
            byte[] bytes;
            for (int spec : specs) {
                avatarUrl = "http://q2.qlogo.cn/headimg_dl?dst_uin=" + contact.getId() + "&spec=" + spec;
                bytes = Network.httpGetRaw(avatarUrl);
                if (bytes != null && bytes.length > 4096) {
                    File file = SJFPathTool.getAvatarPath(contact.getId() + ".png");
                    FileTool.saveFile(file, bytes);
                    return file;
                }
            }
            throw new IOException("Failed to get avatar after trying all spec parameters for user: " + contact.getId());
        } else {
            byte[] bytes = Network.httpGetRaw(avatarUrl);
            File file = SJFPathTool.getAvatarPath(contact.getId() + ".png");
            FileTool.saveFile(file, bytes);
            return file;
        }
    }

    public Audio toAudio(File file, Group group) {
        return group.uploadAudio(ExternalResource.Companion.create(file));
    }

    public Audio toAudio(InputStream inputStream, Group group) {
        try {
            return group.uploadAudio(ExternalResource.Companion.create(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AbsoluteFile upLoadGroupFile(Group group, byte[] file, String path) {
        return group.getFiles().uploadNewFile(path, ExternalResource.create(file));
    }

    public AbsoluteFile upLoadGroupFile(Group group, File file) {
        return upLoadGroupFile(group, file, "/" + file.getName());
    }

    public AbsoluteFile upLoadGroupFile(Group group, File file, String path) {
        return group.getFiles().uploadNewFile(path, ExternalResource.create(file));
    }

    public NormalMember getGroupMember(long gid, long qq) {
        return bot.getGroup(gid).get(qq);
    }

    public NormalMember getGroupMember(GroupMessageEvent event) {
        return bot.getGroup(event.getGroup().getId()).get(event.getSender().getId());
    }

    public boolean isAtme(MessageChain messageChain) {
        return getAt(messageChain) == bot.getId();
    }

    public long getAt(MessageChain msgc) {
        for (Message msg : msgc) {
            if (msg instanceof At) {
                return ((At) msg).getTarget();
            }
        }
        return -1;
    }

    public List<Long> getAts(MessageChain msg) {
        ArrayList<Long> al = new ArrayList<>();
        for (SingleMessage p1 : msg) {
            if (p1 instanceof At) {
                al.add(((At) p1).getTarget());
            }
        }
        return al;
    }

    public static boolean messageEquals(MessageChain mc1, MessageChain mc2) {
        int len = mc1.size();
        if (len != mc2.size()) {
            return false;
        }
        for (int i = 1; i < len; ++i) {
            if (!mc1.get(i).equals(mc2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void setGroupCard(long gid, long qq, String nick) {
        bot.getGroup(gid).get(qq).setNameCard(nick);
    }

    public void setGroupSpecialTitle(long gid, long qq, String title) {
        bot.getGroup(gid).get(qq).setSpecialTitle(title);
    }

    public long getId() {
        return bot.getId();
    }

    public Group getGroup(long groupId) {
        return bot.getGroup(groupId);
    }

    public Collection<Group> getGroups() {
        return bot.getGroups();
    }
}
