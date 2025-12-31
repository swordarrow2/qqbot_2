package com.meng.tools.normal;

import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ImageFactory {
    private static volatile ImageFactory instance;

    private final Map<String, BufferedImage> templateCache = new ConcurrentHashMap<>();

    public static ImageFactory getInstance() {
        if (instance == null) {
            synchronized (ImageFactory.class) {
                if (instance == null) {
                    instance = new ImageFactory();
                }
            }
        }
        return instance;
    }

    private ImageFactory() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (String template : new String[]{
                "精神支柱.png",
                "神触.png",
                "小画家.png",
                "继续干活.png",
                "我永远喜欢.png",
                "发癫.png",
                "不可以见面.png",
                "在想.png",
                "抱紧.png"}) {
            executor.submit(() -> getTemplateImage(template));
        }
        executor.shutdown();
    }

    public BufferedImage generateGray(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);
        IntStream.range(0, pixels.length).parallel().forEach(i -> {
            int col = pixels[i];
            int alpha = col & 0xFF000000;
            int r = (col >> 16) & 0xFF;
            int g = (col >> 8) & 0xFF;
            int b = col & 0xFF;
            int gray = (r * 77 + g * 150 + b * 29 + 128) >> 8;
            pixels[i] = alpha | (gray << 16) | (gray << 8) | gray;
        });
        grayImage.setRGB(0, 0, width, height, pixels, 0, width);
        return grayImage;
    }

    public float[] RGBToYUV(int R, int G, int B) {
        return new float[]{
                0.299f * R + 0.587f * G + 0.114f * B,
                -0.147f * R - 0.289f * G + 0.436f * B,
                0.615f * R - 0.515f * G - 0.100f * B
        };
    }

    public int[] YUVToRGB(float Y, float U, float V) {
        return new int[]{
                ((int) (Y + 1.14f * V)),
                ((int) (Y - 0.39f * U - 0.58f * V)),
                ((int) (Y + 2.03f * U))
        };
    }

    private BufferedImage getTemplateImage(String name) {
        return templateCache.computeIfAbsent(name, key -> {
            try {
                return ImageIO.read(SJFPathTool.getBaseImagePath(key));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public BufferedImage generateJingShenZhiZhu(BufferedImage src) {
        BufferedImage des1 = scaleImage(generateRotateImage(src, 346), 190);
        Image im = getTemplateImage("精神支柱.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = b.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(im, 0, 0, null);
        graphics2D.drawImage(des1, -29, 30, null);
        return b;
    }

    public BufferedImage generateShenChu(BufferedImage src) {
        BufferedImage des1 = new BufferedImage(228, 228, BufferedImage.TYPE_INT_ARGB);
        des1.getGraphics().drawImage(src, 0, 0, 228, 228, null);
        Image im = getTemplateImage("神触.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        b.getGraphics().drawImage(im, 0, 0, null);
        b.getGraphics().drawImage(des1, 216, -20, null);
        return b;
    }

    public BufferedImage generateXiaoHuaJia(BufferedImage src) {
        BufferedImage des1 = scaleImage(src, 345);
        Image im = getTemplateImage("小画家.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        b.getGraphics().drawImage(des1, 73, 91, null);
        b.getGraphics().drawImage(im, 0, 0, null);
        return b;
    }

    public BufferedImage generateJiXuGanHuo(BufferedImage src) {
        BufferedImage des1 = scaleImage(generateRotateImage(src, 343), 400);
        Image im = getTemplateImage("继续干活.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        b.getGraphics().drawImage(des1, 30, 35, null);
        b.getGraphics().drawImage(im, 0, 0, null);
        return b;
    }

    public BufferedImage generateWoYongYuanXiHuan(BufferedImage src, String str) {
        BufferedImage transactedSrc = scaleImage(src, 400);
        Image im = getTemplateImage("我永远喜欢.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = b.createGraphics();
        graphics.drawImage(im, 0, 0, null);
        graphics.drawImage(transactedSrc, 15, 93, null);
        graphics.setColor(Color.black);
        Font ft1 = new Font("黑体", Font.PLAIN, 40);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setFont(ft1);
        graphics.drawString("我永远喜欢", 500, 500);
        graphics.drawString(str, 500, 560);
        return b;
    }

    public BufferedImage generateFaDian(BufferedImage src, String str) {
        BufferedImage transactedSrc = scaleImage(src, 90);
        Image im = getTemplateImage("发癫.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = b.createGraphics();
        graphics.drawImage(transactedSrc, 5, 17, null);
        graphics.drawImage(im, 0, 0, null);
        graphics.setColor(Color.black);
        Font ft1 = new Font("黑体", Font.PLAIN, 20);
        graphics.setFont(ft1);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (str.length() > 5) {
            str = str.substring(str.length() - 5);
        }
        graphics.drawString(str + "...嘿嘿....", 40, 192);
        graphics.drawString("我的" + str + "...", 40, 220);
        return b;
    }

    public BufferedImage generateBuKeYiJianMian(BufferedImage src) {
        BufferedImage transactedSrc = scaleImage(src, 430);
        Image im = getTemplateImage("不可以见面.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = b.getGraphics();
        graphics.drawImage(transactedSrc, 40, 100, null);
        graphics.drawImage(im, 0, 0, null);
        return b;
    }

    public BufferedImage generatePa(BufferedImage src, String dummy) {
        try {
            BufferedImage transactedSrc = scaleImage(src, 72);
            File file = SJFRandom.randomSelect(new File(SJFPathTool.getBaseImagePath() + "crawl/").listFiles());
            Image im = ImageIO.read(file);
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = b.getGraphics();
            graphics.drawImage(im, 0, 0, null);
            graphics.drawImage(transactedSrc, 17, 410, null);
            return b;
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateZaiXiang(BufferedImage src) {
        try {
            BufferedImage transactedSrc = scaleImage(src, 540);
            Image im = getTemplateImage("在想.png");
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = b.getGraphics();
            graphics.drawImage(transactedSrc, 530, 0, null);
            graphics.drawImage(im, 0, 0, null);
            return zoomByScale(b, 0.6);
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateBaojin(BufferedImage src) {
        BufferedImage transactedSrc = scaleImage(src, 180);
        Image im = getTemplateImage("抱紧.png");
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = b.getGraphics();
        graphics.drawImage(transactedSrc, 108, 200, null);
        graphics.drawImage(im, 0, 0, null);
        return b;
    }

    public BufferedImage generateMirror(BufferedImage srcImage, int flag) {
        int w = srcImage.getWidth();
        int h = srcImage.getHeight();
        int size = w * h;

        int[] rgb1 = new int[size];
        srcImage.getRGB(0, 0, w, h, rgb1, 0, w);
        int[] rgb2 = new int[size];

        switch (flag % 3) {
            case 0: // 左右镜像
                for (int y = 0; y < h; y++) {
                    int srcPos = y * w;
                    int destPos = srcPos + w - 1;
                    for (int x = 0; x < w; x++) {
                        rgb2[destPos - x] = rgb1[srcPos + x];
                    }
                }
                break;
            case 1: // 上下镜像
                for (int y = 0; y < h; y++) {
                    System.arraycopy(rgb1, y * w, rgb2, (h - 1 - y) * w, w);
                }
                break;
            case 2: // 上下半部分交换
                int halfH = h / 2;
                int halfSize = halfH * w;
                System.arraycopy(rgb1, 0, rgb2, halfSize, halfSize);
                System.arraycopy(rgb1, halfSize, rgb2, 0, halfSize);
                break;
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, w, h, rgb2, 0, w);
        return result;
    }

    public BufferedImage generateRotateImage(Image src, double angle) {
        BufferedImage srcImage;
        if (src instanceof BufferedImage) {
            srcImage = (BufferedImage) src;
        } else {
            srcImage = new BufferedImage(src.getWidth(null), src.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            srcImage.getGraphics().drawImage(src, 0, 0, null);
        }
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.floor(srcImage.getWidth() * cos + srcImage.getHeight() * sin);
        int newHeight = (int) Math.floor(srcImage.getHeight() * cos + srcImage.getWidth() * sin);
        BufferedImage result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform transform = new AffineTransform();
        transform.translate(newWidth / 2.0, newHeight / 2.0);
        transform.rotate(radians);
        transform.translate(-srcImage.getWidth() / 2.0, -srcImage.getHeight() / 2.0);
        g2d.setTransform(transform);
        g2d.drawImage(srcImage, 0, 0, null);
        g2d.dispose();
        return result;
    }

    public BufferedImage scaleImage(BufferedImage img, int newSize) {
        BufferedImage img2 = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = img2.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(img.getScaledInstance(newSize, newSize, Image.SCALE_SMOOTH), 0, 0, null);
        return img2;
    }

    public BufferedImage zoomByScale(BufferedImage img, double scale) throws IOException {
        int _width = (int) (scale * img.getWidth(null));
        int _height = (int) (scale * img.getHeight(null));
        Image _img = img.getScaledInstance(_width, _height, Image.SCALE_DEFAULT);
        BufferedImage image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(_img, 0, 0, null);
        graphics.dispose();
        return image;
    }
}