package com.meng.bot.qq.modules;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import com.meng.api.SauceNaoApi;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
import com.meng.bot.config.Person;
import com.meng.bot.qq.BaseModule;
import com.meng.bot.qq.BotWrapper;
import com.meng.bot.qq.command.Command;
import com.meng.bot.qq.command.SecondaryCommand;
import com.meng.bot.qq.handler.group.IGroupMessageEvent;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.normal.FileFormat;
import com.meng.tools.normal.ImageFactory;
import com.meng.tools.normal.SeijaImageFactory;
import com.meng.tools.sjf.SJFRandom;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ImageProcess extends BaseModule implements IGroupMessageEvent {

    public ImageProcess(BotWrapper botHelper) {
        super(botHelper);
    }

    private final ImageProcessNetwok network = new ImageProcessNetwok();
    private final ImageTransaction local = new ImageTransaction();

    public byte[] randomTransaction(GroupMessageEvent event, File imageFile, UserOrBot target) throws IOException {
        return local.randomTransaction(event, imageFile, target);
    }

    @Override
    public String getModuleName() {
        return "图片处理";
    }

    @Override
    @CommandDescribe(cmd = "-", note = "图片处理")
    public boolean onGroupMessage(GroupMessageEvent event) {
        if (!configManager.getGroupConfig(event.getGroup()).isFunctionEnabled(Functions.ImageProcess)) {
            return false;
        }
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        final MessageChain messageChain = event.getMessage();
        messageChainBuilder.addAll(new ArrayList<>(messageChain) {{
            removeIf(singleMessage -> !(singleMessage instanceof PlainText));
        }});
        SecondaryCommand command = Command.imageTransaction.getSecondaryCommand(messageChainBuilder.asMessageChain().contentToString());
        if (command == null && messageChain.get(Image.Key) == null) {
            return false;
        }
        QuoteReply quoteReply = messageChain.get(QuoteReply.Key);
        try {
            if (quoteReply != null) {
                GroupMessageEvent quotedEvent = (GroupMessageEvent) botMessageHandler.getEvent(quoteReply.getSource());
                if (local.onMessage(command, quotedEvent)) {
                    return true;
                }
                if (network.onMessage(command, quotedEvent)) {
                    return true;
                }
            }
            long atqq = botWrapper.getAt(messageChain);
            if (atqq == botWrapper.getId()) {
                File avatar = botWrapper.getAvatarFile(event.getSender());
                return local.generalImage(command, event, avatar, event.getSender());
            } else if (atqq != -1) {
                NormalMember groupMember = botWrapper.getGroupMember(event.getGroup().getId(), atqq);
                File avatar = botWrapper.getAvatarFile(groupMember);
                return local.generalImage(command, event, avatar, groupMember);
            } else {
                if (local.onMessage(command, event)) {
                    return true;
                }
                if (network.onMessage(command, event)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private class ImageProcessNetwok {
        private final ConcurrentHashMap<Long, BiConsumer<Image, GroupMessageEvent>> ready = new ConcurrentHashMap<>();
        private final Map<SecondaryCommand, BiConsumer<Image, GroupMessageEvent>> functionMap;

        private ImageProcessNetwok() {
            functionMap = Collections.unmodifiableMap(new HashMap<>() {
                {
                    put(SecondaryCommand.getImageUrl, (img, event) -> sendMessage(event, botWrapper.getUrl(img)));
                }
            });
        }

        public boolean onMessage(SecondaryCommand command, GroupMessageEvent event) {
            Image miraiImg = getImage(event);
            long senderId = event.getSender().getId();
            if (miraiImg != null && functionMap.containsKey(command)) {
                sendQuote(event, "正在识别……");
                functionMap.get(command).accept(miraiImg, event);
                ready.remove(senderId);
                return true;
            } else if (miraiImg == null && functionMap.containsKey(command)) {
                ready.put(senderId, functionMap.get(command));
                sendQuote(event, "发送一张图片吧");
                return true;
            } else if (miraiImg != null && ready.containsKey(senderId)) {
                sendQuote(event, "正在识别……");
                ready.get(senderId).accept(miraiImg, event);
                ready.remove(senderId);
                return true;
            }
            return false;
        }
    }

    private class ImageTransaction {

        private final ConcurrentHashMap<Long, BiFunction<BufferedImage, String, BufferedImage>> ready = new ConcurrentHashMap<>();
        private final Map<SecondaryCommand, BiFunction<BufferedImage, String, BufferedImage>> functions;

        private ImageTransaction() {
            functions = Collections.unmodifiableMap(new HashMap<>() {
                {
                    put(SecondaryCommand.imageToGray, (p1, s) -> ImageFactory.getInstance().generateGray(p1));
                    put(SecondaryCommand.imageRotate, (p1, s) -> ImageFactory.getInstance().generateRotateImage(p1, 90));
                    put(SecondaryCommand.imageUpsideDown, (p1, s) -> ImageFactory.getInstance().generateMirror(p1, 1));
                    put(SecondaryCommand.imageFlip, (p1, s) -> ImageFactory.getInstance().generateMirror(p1, 0));
                    put(SecondaryCommand.imageUpSeija, (p1, s) -> ImageFactory.getInstance().generateMirror(p1, 2));
                    put(SecondaryCommand.expression_jingShenZhiZhu, (p1, s) -> ImageFactory.getInstance().generateJingShenZhiZhu(p1));
                    put(SecondaryCommand.expression_shenChu, (p1, s) -> ImageFactory.getInstance().generateShenChu(p1));
                    put(SecondaryCommand.expression_xiaoHuaJia, (p1, s) -> ImageFactory.getInstance().generateXiaoHuaJia(p1));
                    put(SecondaryCommand.expression_JiXuGanHuo, (p1, s) -> ImageFactory.getInstance().generateJiXuGanHuo(p1));
                    put(SecondaryCommand.expression_BuKeYiJianMian, (p1, s) -> ImageFactory.getInstance().generateBuKeYiJianMian(p1));
                    put(SecondaryCommand.expression_ZaiXiang, (p1, s) -> ImageFactory.getInstance().generateZaiXiang(p1));
                    put(SecondaryCommand.expression_BaoJin, (p1, s) -> ImageFactory.getInstance().generateBaojin(p1));
                    put(SecondaryCommand.expression_WoYongYuanXiHuan, (bufferedImage, s) -> ImageFactory.getInstance().generateWoYongYuanXiHuan(bufferedImage, s));
                    put(SecondaryCommand.expression_FaDian, (bufferedImage, s) -> ImageFactory.getInstance().generateFaDian(bufferedImage, s));
                    put(SecondaryCommand.expression_Pa, (bufferedImage, s) -> ImageFactory.getInstance().generatePa(bufferedImage, s));
                }
            });
        }

        private byte[] randomTransaction(GroupMessageEvent event, File imageFile, UserOrBot target) throws IOException {
            SecondaryCommand command = SJFRandom.randomSelect(functions.keySet());
            BiFunction<BufferedImage, String, BufferedImage> trans = functions.get(command);
            String nick;
            if (event == null) {
                if (target != null) {
                    nick = configManager.getNickName(0, target.getId());
                } else {
                    nick = "";
                }
            } else {
                nick = configManager.getNickName(event.getGroup().getId(), target.getId());
            }
            if (!FileFormat.isFormat(imageFile, "gif")) {
                return generateStatic(imageFile, trans, nick);
            }
            if (command == SecondaryCommand.imageUpSeija) {
                return SeijaImageFactory.reverseGIF(imageFile, 2);
            }
            return generateDynamic(imageFile, trans, nick);
        }

        public boolean onMessage(SecondaryCommand command, GroupMessageEvent event) {
            Image miraiImg = getImage(event);
            try {
                return generalImage(command, event, botWrapper.downloadTempImage(miraiImg), event.getSender());
            } catch (IOException e) {
                if (botWrapper.debug) {
                    ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                }
            }
            return false;
        }

        private boolean generalImage(SecondaryCommand command, GroupMessageEvent event, File receivedImage, UserOrBot target) throws IOException {

            BiFunction<BufferedImage, String, BufferedImage> function = null;
            if (functions.containsKey(command)) {
                function = functions.get(command);
            } else if (ready.containsKey(target.getId())) {
                function = ready.remove(target.getId());
            }
            if (receivedImage == null && function != null) {
                ready.put(target.getId(), function);
                sendQuote(event, "发送一张图片吧");
                return true;
            }
            if (receivedImage != null) {
                boolean isGif = FileFormat.isFormat(receivedImage, "gif");
                String nick = configManager.getNickName(event.getGroup().getId(), target.getId());
                if (function != null) {
                    if (isGif && function == functions.get(SecondaryCommand.imageUpSeija)) {
                        sendMessage(event, botWrapper.toImage(SeijaImageFactory.reverseGIF(receivedImage, 2), event.getGroup()));
                    } else if (isGif) {
                        sendMessage(event, botWrapper.toImage(generateDynamic(receivedImage, function, nick), event.getGroup()));
                    } else {
                        sendMessage(event, botWrapper.toImage(generateStatic(receivedImage, function, nick), event.getGroup()));
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private byte[] generateStatic(File imageFile, BiFunction<BufferedImage, String, BufferedImage> function, String name) throws IOException {
        BufferedImage result = function.apply(ImageIO.read(imageFile), name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(result, "png", baos);
        return baos.toByteArray();
    }

    private byte[] generateDynamic(File imageFile, BiFunction<BufferedImage, String, BufferedImage> function, String name) throws FileNotFoundException {
        GifDecoder gifDecoder = new GifDecoder();
        FileInputStream fis = new FileInputStream(imageFile);
        int statusCode = gifDecoder.read(fis);
        if (statusCode != 0) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder localAnimatedGifEncoder = new AnimatedGifEncoder();
        localAnimatedGifEncoder.start(baos);//start
        localAnimatedGifEncoder.setRepeat(0);//设置生成gif的开始播放时间。0为立即开始播放
        for (int i = 0; i < gifDecoder.getFrameCount(); i++) {
            localAnimatedGifEncoder.setDelay(gifDecoder.getDelay(i));
            localAnimatedGifEncoder.addFrame(function.apply(gifDecoder.getFrame(i), name));
        }
        localAnimatedGifEncoder.finish();
        return baos.toByteArray();
    }

    private Image getImage(GroupMessageEvent event) {
        Image img = event.getMessage().get(Image.Key);
        if (img != null) {
            return img;
        }
        FlashImage fi = event.getMessage().get(FlashImage.Key);
        if (fi != null) {
            return fi.getImage();
        }
        return null;
    }
}
