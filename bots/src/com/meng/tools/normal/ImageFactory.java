package com.meng.tools.normal;

import com.meng.tools.sjf.SJFPathTool;
import com.meng.tools.sjf.SJFRandom;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFactory {

    private static final ImageFactory instance = new ImageFactory();

    public static final int GENERATE_FLAG_FLIP = 0;
    public static final int GENERATE_FLAG_UPSIDE_DOWN = 1;
    public static final int GENERATE_FLAG_SEIJA = 2;

    public static ImageFactory getInstance() {
        return instance;
    }

    public BufferedImage generateGray(BufferedImage img) {
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int col = img.getRGB(i, j);
                int alpha = col & 0xFF000000;
                int R = (col & 0x00FF0000) >> 16;
                int G = (col & 0x0000FF00) >> 8;
                int B = (col & 0x000000FF);
                //  int Y = (int)(R * 0.299 + G * 0.587 + B * 0.114);
                int Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                int newColor = alpha | (Y << 16) | (Y << 8) | Y;
                img.setRGB(i, j, newColor);
            }
        }
        return img;
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

    public BufferedImage generateJingShenZhiZhu(BufferedImage src) {
        try {
            BufferedImage des1 = scaleImage(generateRotateImage(src, 346), 190);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("精神支柱.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2D = b.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.drawImage(im, 0, 0, null);
            graphics2D.drawImage(des1, -29, 30, null);

            return b;
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateShenChu(BufferedImage src) {
        try {
            BufferedImage des1 = new BufferedImage(228, 228, BufferedImage.TYPE_INT_ARGB);
            des1.getGraphics().drawImage(src, 0, 0, 228, 228, null);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("神触.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            b.getGraphics().drawImage(im, 0, 0, null);
            b.getGraphics().drawImage(des1, 216, -20, null);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage generateXiaoHuaJia(BufferedImage src) {
        try {
            BufferedImage des1 = scaleImage(src, 345);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("小画家.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            b.getGraphics().drawImage(des1, 73, 91, null);
            b.getGraphics().drawImage(im, 0, 0, null);

            return b;
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateJiXuGanHuo(BufferedImage src) {
        try {
            BufferedImage des1 = scaleImage(generateRotateImage(src, 343), 400);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("继续干活.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            b.getGraphics().drawImage(des1, 30, 35, null);
            b.getGraphics().drawImage(im, 0, 0, null);
            return b;
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateWoYongYuanXiHuan(BufferedImage src, String str) {
        try {
            BufferedImage transactedSrc = scaleImage(src, 400);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("我永远喜欢.png"));
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
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateFaDian(BufferedImage src, String str) {
        try {
            BufferedImage transactedSrc = scaleImage(src, 90);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("发癫.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = b.createGraphics();
            graphics.drawImage(transactedSrc, 5, 17, null);
            graphics.drawImage(im, 0, 0, null);
            graphics.setColor(Color.black);
            Font ft1 = new Font("黑体", Font.PLAIN, 20);
            graphics.setFont(ft1);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (str.length() > 3) {
                str = str.substring(str.length() - 3);
            }
            graphics.drawString(str + "...嘿嘿....", 40, 192);
            graphics.drawString("我的" + str + "...", 40, 220);
            return b;
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateBuKeYiJianMian(BufferedImage src) {
        try {
            BufferedImage transactedSrc = scaleImage(src, 430);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("不可以见面.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = b.getGraphics();
            graphics.drawImage(transactedSrc, 40, 100, null);
            graphics.drawImage(im, 0, 0, null);
            return b;
        } catch (IOException e) {
            return null;
        }
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
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("在想.png"));
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
        try {


            BufferedImage transactedSrc = scaleImage(src, 180);
            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("抱紧.png"));
            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = b.getGraphics();
            graphics.drawImage(transactedSrc, 108, 200, null);
            graphics.drawImage(im, 0, 0, null);
            return b;
        } catch (IOException e) {
            return null;
        }
    }

    public BufferedImage generateMirror(BufferedImage srcImage, int flag) {
        Image im = srcImage;
        int w = im.getWidth(null);
        int h = im.getHeight(null);
        int size = w * h;
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        b.getGraphics().drawImage(im.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        int[] rgb1 = b.getRGB(0, 0, w, h, new int[size], 0, w);
        int[] rgb2 = new int[size];
        switch (flag % 3) {
            case 0:
                for (int y = 0; y < h; ++y) {
                    int yw = y * w;
                    for (int x = 0; x < w; ++x) {
                        rgb2[(w - 1 - x) + yw] = rgb1[x + yw]; // 镜之国
                    }
                }
                break;
            case 1:
                for (int y = 0; y < h; y++) {
                    // 天地
                    if (w >= 0) {
                        System.arraycopy(rgb1, y * w, rgb2, (h - 1 - y) * w, w);
                    }
                }
                break;
            case 2:
                int halfH = h / 2;
                for (int y = 0; y < h; y++) {
                    // 天壤梦弓
                    if (w >= 0) {
                        System.arraycopy(rgb1, y * w, rgb2, (y < halfH ? y + halfH : y - halfH) * w, w);
                    }
                }
                break;
        }
        b.setRGB(0, 0, w, h, rgb2, 0, w);
        return b;
    }

    public BufferedImage generateRotateImage(Image src, int angel) {
        int srcWidth = src.getWidth(null);
        int srcHeight = src.getHeight(null);
        if (angel > 90) {
            if (angel / 90 % 2 == 1) {
                srcHeight = srcHeight ^ srcWidth;
                srcWidth = srcHeight ^ srcWidth;
                srcHeight = srcHeight ^ srcWidth;
            }
        }
        double r = Math.sqrt(srcHeight * srcHeight + srcWidth * srcWidth) / 2;
        double len = 2 * Math.sin(Math.toRadians(angel % 90) / 2) * r;
        double angelAlpha = (Math.PI - Math.toRadians(angel % 90)) / 2;
        double angelDaltaWidth = Math.atan((double) srcHeight / srcWidth);
        double angelDaltaHeight = Math.atan((double) srcWidth / srcHeight);
        int lenDaltaWidth = (int) (len * Math.cos(Math.PI - angelAlpha - angelDaltaWidth));
        int lenDaltaHeight = (int) (len * Math.cos(Math.PI - angelAlpha - angelDaltaHeight));
        int desWidth = srcWidth + lenDaltaWidth * 2;
        int desHeight = srcHeight + lenDaltaHeight * 2;
        BufferedImage res = new BufferedImage(desWidth, desHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = res.createGraphics();
        g2.translate((desWidth - srcWidth) / 2, (desHeight - srcHeight) / 2);
        g2.rotate(Math.toRadians(angel), srcWidth / 2, srcHeight / 2);
        g2.drawImage(src, null, null);
        return res;
    }

    public BufferedImage scaleImage(BufferedImage img, int newSize) {
        BufferedImage img2 = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = img2.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(img.getScaledInstance(newSize, newSize, Image.SCALE_SMOOTH), 0, 0, null);
        return img2;
    }

    public BufferedImage zoomByScale(BufferedImage img, double scale) throws IOException {

        //获取缩放后的长和宽
        int _width = (int) (scale * img.getWidth(null));
        int _height = (int) (scale * img.getHeight(null));
        //获取缩放后的Image对象
        Image _img = img.getScaledInstance(_width, _height, Image.SCALE_DEFAULT);
        //新建一个和Image对象相同大小的画布
        BufferedImage image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        //获取画笔
        Graphics2D graphics = image.createGraphics();
        //将Image对象画在画布上,最后一个参数,ImageObserver:接收有关 Image 信息通知的异步更新接口,没用到直接传空
        graphics.drawImage(_img, 0, 0, null);
        //释放资源
        graphics.dispose();
        //使用ImageIO的方法进行输出,记得关闭资源
        return image;
    }

}
