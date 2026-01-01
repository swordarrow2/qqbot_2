package com.meng.tools.normal;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SeijaImageFactory {
    public static byte[] reverseGIF(File gifFile, int reverseFlag) throws FileNotFoundException {
        GifDecoder decoder = new GifDecoder();
        FileInputStream fis = new FileInputStream(gifFile);
        decoder.read(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        BufferedImage bg = decoder.getFrame(0);
//         int tw = bg.getWidth(null) - 1;
//         int th = bg.getHeight(null) - 1;
//         if (bg.getRGB(0, 0) == 0 &&
//         bg.getRGB(tw, 0) == 0 &&
//         bg.getRGB(0, th) == 0 &&
//         bg.getRGB(tw, th) == 0) {
//         encoder.setTransparent(new Color(0, 0, 0, 0));
//         }
        encoder.start(baos);// start
        encoder.setRepeat(0);// 设置生成gif的开始播放时间。0为立即开始播放
        float fa = (float) bg.getHeight() / (decoder.getFrameCount());
        switch (reverseFlag % 4) {
            case 0:// 镜之国
                bg = spell1(decoder.getFrame(0));
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    encoder.setDelay(decoder.getDelay(i));
                    encoder.addFrame(spell1(decoder.getFrame(i), bg));
                }
                break;
            case 1:// 天地
                bg = spell2(decoder.getFrame(0));
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    encoder.setDelay(decoder.getDelay(i));
                    encoder.addFrame(spell2(decoder.getFrame(i), bg));
                }
                break;
            case 2:// 天壤梦弓
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    encoder.setDelay(decoder.getDelay(i));
                    encoder.addFrame(spell3(decoder.getFrame(i), (int) (fa * (decoder.getFrameCount() - i)), bg));
                }
                break;
            case 3:// Reverse Hierarchy
                bg = spell4(decoder.getFrame(0));
                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    encoder.setDelay(decoder.getDelay(i));
                    encoder.addFrame(spell4(decoder.getFrame(i), bg));
                }
                break;
        }
        encoder.finish();
        return baos.toByteArray();
    }

    private static BufferedImage spell1(BufferedImage current, BufferedImage cache) {
        int w = current.getWidth(null);
        int h = current.getHeight(null);
        BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
        bmp.getGraphics().drawImage(cache.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = current.getRGB(x, y);
                bmp.setRGB(w - 1 - x, y, i == 0 ? 1 : i);// 镜之国
            }
        }
        cache.getGraphics().drawImage(bmp.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        return bmp;
    }

    private static BufferedImage spell1(BufferedImage current) {
        int w = current.getWidth(null);
        int h = current.getHeight(null);
        BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = current.getRGB(x, y);
                bmp.setRGB(w - 1 - x, y, i == 0 ? 1 : i);// 镜之国
            }
        }
        return bmp;
    }

    private static BufferedImage spell2(BufferedImage current, BufferedImage cache) {
        int w = current.getWidth(null);
        int h = current.getHeight(null);
        BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
        bmp.getGraphics().drawImage(cache.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = current.getRGB(x, y);
                bmp.setRGB(x, h - 1 - y, i == 0 ? 1 : i);// 天地
            }
        }
        cache.getGraphics().drawImage(bmp.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        return bmp;
    }

    private static BufferedImage spell2(BufferedImage current) {
        int w = current.getWidth(null);
        int h = current.getHeight(null);
        BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = current.getRGB(x, y);
                bmp.setRGB(x, h - 1 - y, i == 0 ? 1 : i);// 天地
            }
        }
        return bmp;
    }

    private static BufferedImage spell3(BufferedImage fg, int px, BufferedImage bg) {
        int bgW = bg.getWidth(null);
        int bgH = bg.getHeight(null);
        bg.getGraphics().drawImage(fg, 0, 0, null);
        BufferedImage resultImg = new BufferedImage(bgW, bgH, BufferedImage.TYPE_USHORT_565_RGB);
        spell3Process(bg, px, bgW, bgH, resultImg);
        return resultImg;
    }

    private static void spell3Process(BufferedImage bg, int px, int width, int height, BufferedImage bmp) {
        int newY;
        for (int y = 0; y < height; y++) {
            newY = y + px;
            for (int x = 0; x < width; x++) {
                int i = bg.getRGB(x, y);
                bmp.setRGB(x, newY < height ? newY : newY - height, i == 0 ? 1 : i);
            }
        }
    }

    private static BufferedImage spell4(BufferedImage current, BufferedImage cache) {
        int w = current.getWidth(null);
        int h = current.getHeight(null);
        BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
        bmp.getGraphics().drawImage(cache.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        spell4Process(current, w, h, bmp);
        cache.getGraphics().drawImage(bmp.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        return bmp;
    }

    private static BufferedImage spell4(BufferedImage current) {
        int w = current.getWidth(null);
        int h = current.getHeight(null);
        BufferedImage bmp = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_565_RGB);
        spell4Process(current, w, h, bmp);
        return bmp;
    }

    private static void spell4Process(BufferedImage current, int w, int h, BufferedImage bmp) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = current.getRGB(x, y);
                bmp.setRGB(w - 1 - x, h - 1 - y, i == 0 ? 1 : i);// Reverse_Hierarchy
            }
        }
    }
}
