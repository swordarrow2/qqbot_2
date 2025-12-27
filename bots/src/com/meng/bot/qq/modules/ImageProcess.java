package com.meng.bot.qq.modules;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import com.meng.api.SauceNaoApi;
import com.meng.bot.annotation.CommandDescribe;
import com.meng.bot.config.Functions;
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
import java.util.function.Predicate;

public class ImageProcess extends BaseModule implements IGroupMessageEvent {

    public ImageProcess(BotWrapper botHelper) {
        super(botHelper);
    }

    private final ImageProcessNetwok network = new ImageProcessNetwok();
    private final ImageTransaction local = new ImageTransaction();

    public byte[] randomTransaction(File imageFile) throws IOException {
        return local.randomTransaction(imageFile);
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
        messageChainBuilder.addAll(new ArrayList<SingleMessage>(messageChain) {{
            removeIf(new Predicate<SingleMessage>() {
                @Override
                public boolean test(SingleMessage singleMessage) {
                    return !(singleMessage instanceof PlainText);
                }
            });
        }});
        SecondaryCommand command = Command.imageTransaction.getSecondaryCommand(messageChainBuilder.asMessageChain().contentToString());
        if (command == null && messageChain.get(Image.Key) == null) {
            return false;
        }
        QuoteReply quoteReply = messageChain.get(QuoteReply.Key);
        try {
            if (quoteReply != null) {
                GroupMessageEvent quotedEvent = (GroupMessageEvent) botMessageHandler.getEvent(quoteReply.getSource());
                if (local.onGroupMessage(command, event, quotedEvent)) {
                    return true;
                }
                if (network.onGroupMessage(command, event, quotedEvent)) {
                    return true;
                }
            } else {
                long atqq = botWrapper.getAt(messageChain);
                if (atqq == botWrapper.getId()) {
                    File avatar = botWrapper.getAvatarFile(event.getSender());
                    return local.generalImage(command, event, avatar, event.getSender());
                } else if (atqq != -1) {
                    NormalMember groupMember = botWrapper.getGroupMember(event.getGroup().getId(), atqq);
                    File avatar = botWrapper.getAvatarFile(groupMember);
                    return local.generalImage(command, event, avatar, groupMember);
                } else {
                    if (local.onGroupMessage(command, event, event)) {
                        return true;
                    }
                    if (network.onGroupMessage(command, event, event)) {
                        return true;
                    }
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
            functionMap = Collections.unmodifiableMap(new HashMap<SecondaryCommand, BiConsumer<Image, GroupMessageEvent>>() {
                {
                    put(SecondaryCommand.searchPicture, new BiConsumer<Image, GroupMessageEvent>() {

                        @Override
                        public void accept(Image simg, GroupMessageEvent event) {
                            try {
                                SauceNaoApi.SauceNaoResult mResults = SauceNaoApi.getSauce(new URL(botWrapper.getUrl(simg)).openStream());
                                if (mResults.getResults().size() < 1) {
                                    sendQuote(event, "没有相似度较高的图片");
                                    return;
                                }
                                MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                                SauceNaoApi.SauceNaoResult.Result tmpr = mResults.getResults().get(0);
                                String[] titleAndMetadata = tmpr.mTitle.split("\n", 2);
                                if (titleAndMetadata.length > 0) {
                                    messageChainBuilder.append(titleAndMetadata[0]).append("\n");
                                    if (titleAndMetadata.length == 2) {
                                        tmpr.mColumns.add(0, titleAndMetadata[1]);
                                    }
                                    for (String string : tmpr.mColumns) {
                                        messageChainBuilder.append(string).append("\n");
                                    }
                                }
                                String imgUrl = tmpr.mThumbnail;
                                if (tmpr.mExtUrls.size() == 2) {
                                    String extUrl = tmpr.mExtUrls.get(1);
                                    if (extUrl.contains("pixiv")) {
                                        imgUrl = "https://www.pixiv.cat/" + extUrl.substring(extUrl.indexOf("=") + 1) + ".png";
                                    }
                                    messageChainBuilder.append("图片&画师:").append(extUrl).append("\n");
                                    messageChainBuilder.append(tmpr.mExtUrls.get(0)).append("\n");
                                } else if (tmpr.mExtUrls.size() == 1) {
                                    String extUrl = tmpr.mExtUrls.get(0);
                                    if (extUrl.contains("pixiv")) {
                                        imgUrl = "https://www.pixiv.cat/" + extUrl.substring(extUrl.indexOf("=") + 1) + ".png";
                                    }
                                    messageChainBuilder.append("链接:").append(tmpr.mExtUrls.get(0)).append("\n");
                                }
                                if (!tmpr.mSimilarity.isEmpty()) {
                                    messageChainBuilder.append("相似度:").append(tmpr.mSimilarity).append("\n");
                                }
                                URL url1 = new URL(imgUrl);
                                Image element = botWrapper.toImage(url1, event.getGroup());
                                messageChainBuilder.add(element);
                                sendMessage(event.getGroup(), messageChainBuilder.asMessageChain());
                            } catch (IOException e) {
                                ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                                sendMessage(event, e.toString());
                            }
                        }
                    });

                    put(SecondaryCommand.getImageUrl, new BiConsumer<Image, GroupMessageEvent>() {

                        @Override
                        public void accept(Image img, GroupMessageEvent event) {
                            sendMessage(event, botWrapper.getUrl(img));
                        }
                    });

                }
            });
        }

        public boolean onGroupMessage(SecondaryCommand command, GroupMessageEvent event, GroupMessageEvent quotedEvent) {
            Image miraiImg = getImage(event, quotedEvent);
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

        private final ConcurrentHashMap<Long, Function<BufferedImage, BufferedImage>> ready = new ConcurrentHashMap<>();
        private final Map<SecondaryCommand, Function<BufferedImage, BufferedImage>> functions;
        private final Map<SecondaryCommand, BiFunction<BufferedImage, String, BufferedImage>> bifunctions;

        private ImageTransaction() {
            functions = Collections.unmodifiableMap(new HashMap<SecondaryCommand, Function<BufferedImage, BufferedImage>>() {
                {
                    put(SecondaryCommand.imageToGray, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateGray(p1);
                        }
                    });
                    put(SecondaryCommand.imageRotate, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateRotateImage(p1, 90);
                        }
                    });
                    put(SecondaryCommand.imageUpsideDown, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateMirror(p1, 1);
                        }
                    });
                    put(SecondaryCommand.imageFlip, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateMirror(p1, 0);
                        }
                    });
                    put(SecondaryCommand.imageUpSeija, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateMirror(p1, 2);
                        }
                    });
                    put(SecondaryCommand.expression_jingShenZhiZhu, new Function<BufferedImage, BufferedImage>() {
                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateJingShenZhiZhu(p1);
                        }
                    });
                    put(SecondaryCommand.expression_shenChu, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateShenChu(p1);
                        }
                    });
                    put(SecondaryCommand.expression_xiaoHuaJia, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateXiaoHuaJia(p1);
                        }
                    });
                    put(SecondaryCommand.expression_JiXuGanHuo, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateJiXuGanHuo(p1);
                        }
                    });
                    put(SecondaryCommand.expression_BuKeYiJianMian, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateBuKeYiJianMian(p1);
                        }
                    });
                    put(SecondaryCommand.expression_ZaiXiang, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateZaiXiang(p1);
                        }
                    });
                    put(SecondaryCommand.expression_BaoJin, new Function<BufferedImage, BufferedImage>() {

                        @Override
                        public BufferedImage apply(BufferedImage p1) {
                            return ImageFactory.getInstance().generateBaojin(p1);
                        }
                    });
                }
            });
            bifunctions = Collections.unmodifiableMap(new HashMap<SecondaryCommand, BiFunction<BufferedImage, String, BufferedImage>>() {{
                put(SecondaryCommand.expression_WoYongYuanXiHuan, new BiFunction<BufferedImage, String, BufferedImage>() {
                    @Override
                    public BufferedImage apply(BufferedImage bufferedImage, String s) {
                        return ImageFactory.getInstance().generateWoYongYuanXiHuan(bufferedImage, s);
                    }
                });
                put(SecondaryCommand.expression_FaDian, new BiFunction<BufferedImage, String, BufferedImage>() {
                    @Override
                    public BufferedImage apply(BufferedImage bufferedImage, String s) {
                        return ImageFactory.getInstance().generateFaDian(bufferedImage, s);
                    }
                });
                put(SecondaryCommand.expression_Pa, new BiFunction<BufferedImage, String, BufferedImage>() {
                    @Override
                    public BufferedImage apply(BufferedImage bufferedImage, String s) {
                        return ImageFactory.getInstance().generatePa(bufferedImage, s);
                    }
                });
            }});
        }

        private byte[] randomTransaction(File imageFile) throws IOException {
            SecondaryCommand command = SJFRandom.randomSelect(functions.keySet());
            Function<BufferedImage, BufferedImage> trans = functions.get(command);
            if (FileFormat.isFormat(imageFile, "gif")) {
                if (command == SecondaryCommand.imageUpSeija) {
                    return SeijaImageFactory.reverseGIF(imageFile, 2);
                } else {
                    return generateDynamic(imageFile, trans);
                }
            } else {
                return generateStatic(imageFile, trans);
            }
        }

        public boolean onGroupMessage(SecondaryCommand command, GroupMessageEvent event, GroupMessageEvent quotedEvent) {
            Image miraiImg = getImage(event, quotedEvent);
            try {
                return generalImage(command, event, miraiImg, event.getSender());
            } catch (IOException e) {
                if (botWrapper.debug) {
                    ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                }
            }
            return false;
        }

        private boolean generalImage(SecondaryCommand command, GroupMessageEvent event, Image miraiImg, UserOrBot target) throws IOException {
            return generalImage(command, event, botWrapper.downloadTempImage(miraiImg), target);
        }

        private boolean generalImage(SecondaryCommand command, GroupMessageEvent event, File receivedImage, UserOrBot target) throws IOException {

            if (receivedImage != null && functions.containsKey(command)) {
                ready.remove(target.getId());
                if (FileFormat.isFormat(receivedImage, "gif")) {
                    if (command == SecondaryCommand.imageUpSeija) {
                        sendMessage(event, botWrapper.toImage(SeijaImageFactory.reverseGIF(receivedImage, 2), event.getGroup()));
                    } else {
                        sendMessage(event, botWrapper.toImage(generateDynamic(receivedImage, functions.get(command)), event.getGroup()));
                    }
                } else {
                    sendMessage(event, botWrapper.toImage(generateStatic(receivedImage, functions.get(command)), event.getGroup()));
                }
                return true;
            } else if (receivedImage == null && functions.containsKey(command)) {
                ready.put(target.getId(), functions.get(command));
                sendQuote(event, "发送一张图片吧");
                return true;
            } else if (receivedImage != null && ready.containsKey(target.getId())) {
                Function<BufferedImage, BufferedImage> function = ready.remove(target.getId());
                if (FileFormat.isFormat(receivedImage, "gif")) {
                    if (function == functions.get(SecondaryCommand.imageUpSeija)) {
                        sendMessage(event, botWrapper.toImage(SeijaImageFactory.reverseGIF(receivedImage, 2), event.getGroup()));
                    } else {
                        sendMessage(event, botWrapper.toImage(generateDynamic(receivedImage, function), event.getGroup()));
                    }
                } else {
                    sendMessage(event, botWrapper.toImage(generateStatic(receivedImage, function), event.getGroup()));
                }
                return true;
            } else if (receivedImage != null && bifunctions.containsKey(command)) {
                if (FileFormat.isFormat(receivedImage, "gif")) {
                    sendMessage(event,
                            botWrapper.toImage(
                                    generateDynamic(receivedImage, bifunctions.get(command), target.getNick()),
                                    event.getGroup()));
                } else {
                    sendMessage(event,
                            botWrapper.toImage(
                                    generateStatic(receivedImage, bifunctions.get(command), target.getNick()),
                                    event.getGroup()));
                }
                return true;
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

    private byte[] generateStatic(File imageFile, Function<BufferedImage, BufferedImage> function) throws IOException {
        BufferedImage result = function.apply(ImageIO.read(imageFile));
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

    private byte[] generateDynamic(File imageFile, Function<BufferedImage, BufferedImage> function) throws FileNotFoundException {
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
            localAnimatedGifEncoder.addFrame(function.apply(gifDecoder.getFrame(i)));
        }
        localAnimatedGifEncoder.finish();
        return baos.toByteArray();
    }

    private Image getImage(GroupMessageEvent event, GroupMessageEvent quotedEvent) {
        Image miraiImg;
        if (quotedEvent == null) {
            miraiImg = event.getMessage().get(Image.Key);
            if (miraiImg == null) {
                FlashImage fi = event.getMessage().get(FlashImage.Key);
                if (fi != null) {
                    miraiImg = fi.getImage();
                }
            }
        } else {
            miraiImg = quotedEvent.getMessage().get(Image.Key);
            if (miraiImg == null) {
                FlashImage fi = quotedEvent.getMessage().get(FlashImage.Key);
                if (fi != null) {
                    miraiImg = fi.getImage();
                }
            }
        }
        return miraiImg;
    }

    private String switchTagName(String s) {
        switch (s) {
            case "normal":
                return "普通";
            case "hot":
                return "性感";
            case "porn":
                return "色情";
            case "female-genital":
                return "女性阴部";
            case "female-breast":
                return "女性胸部";
            case "male-genital":
                return "男性阴部";
            case "pubes":
                return "阴毛";
            case "anus":
                return "肛门";
            case "sex":
                return "性行为";
            case "normal_hot_porn":
                return "色情综合值";
            default:
                return null;
        }
    }
}
