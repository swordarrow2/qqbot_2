package com.meng.test;

import com.meng.tools.normal.ImageFactory;
import com.meng.tools.sjf.SJFPathTool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGeneralTest {
    public static void main(String[] args) {
        File imageFile = new File("C:\\Users\\Administrator\\Desktop",
                "IMG_20201215_185543.png");
        //       imageGeneralTest.analyzeNudge("NudgeEvent(from=NormalMember(1594703250), target=Bot(1975465607), subject=Group(666247478), action=戳了戳, suffix=)");
        try {

            BufferedImage bufferedImage = ImageIO.read(imageFile);


            BufferedImage result = ImageFactory.getInstance().zoomByScale(generateBaojin(bufferedImage), 1.5);

            JFrame frame = new ImageViewerFrame(result);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage generateBaojin(BufferedImage src) throws IOException {
        BufferedImage transactedSrc = scaleImage(src, 180);
        Image im = ImageIO.read(SJFPathTool.getBaseImagePath("抱紧.png"));
        BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = b.getGraphics();
        graphics.drawImage(transactedSrc, 108, 200, null);
        graphics.drawImage(im, 0, 0, null);
        return b;
    }
//
//
//    public BufferedImage generateFaDian(BufferedImage src, String str) {
//        try {
//            BufferedImage transactedSrc = scaleImage(src, 90);
//            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("发癫.png"));
//            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//            Graphics2D graphics = b.createGraphics();
//            graphics.drawImage(transactedSrc, 5, 17, null);
//            graphics.drawImage(im, 0, 0, null);
//            graphics.setColor(Color.black);
//            Font ft1 = new Font("黑体", Font.PLAIN, 20);
//            graphics.setFont(ft1);
//            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            if (str.length() > 3) {
//                str = str.substring(str.length() - 3);
//            }
//            graphics.drawString(str + "...嘿嘿....", 40, 192);
//            graphics.drawString("我的" + str + "...", 40, 220);
//            return b;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public BufferedImage generateBuKeYiJianMian(BufferedImage src, String str) {
//        try {
//            BufferedImage transactedSrc = scaleImage(src, 430);
//            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("不可以见面.png"));
//            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//            Graphics graphics = b.getGraphics();
//            graphics.drawImage(transactedSrc, 40, 100, null);
//            graphics.drawImage(im, 0, 0, null);
//            return b;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public BufferedImage generateWoYongYuanXiHuan(BufferedImage src, String str) {
//        try {
//            BufferedImage transactedSrc = scaleImage(src, 400);
//            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("我永远喜欢.png"));
//            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//            Graphics graphics = b.getGraphics();
//            graphics.drawImage(im, 0, 0, null);
//            graphics.drawImage(transactedSrc, 15, 93, null);
//            graphics.setColor(Color.black);
//            Font ft1 = new Font("黑体", Font.PLAIN, 40);
//            graphics.setFont(ft1);
//            graphics.drawString("我永远喜欢", 500, 500);
//            graphics.drawString(str, 500, 560);
//            return b;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public BufferedImage generateJingShenZhiZhu(BufferedImage src) {
//        try {
//            BufferedImage des1 = scaleImage(generateRotateImage(src, 346), 190);
//            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("精神支柱.png"));
//            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//            b.getGraphics().drawImage(im, 0, 0, null);
//            b.getGraphics().drawImage(des1, -29, 30, null);
//
//            return b;
//        } catch (IOException e) {
//            return null;
//        }
//    }
//
//    public BufferedImage generateShenChu(BufferedImage src) {
//        try {
//            BufferedImage des1 = new BufferedImage(228, 228, BufferedImage.TYPE_INT_ARGB);
//            des1.getGraphics().drawImage(src, 0, 0, 228, 228, null);
//            Image im = ImageIO.read(SJFPathTool.getBaseImagePath("神触.png"));
//            BufferedImage b = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//            b.getGraphics().drawImage(im, 0, 0, null);
//            b.getGraphics().drawImage(des1, 216, -20, null);
//            return b;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//

    public static BufferedImage generateRotateImage(Image src, int angel) {
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

    public static BufferedImage scaleImage(BufferedImage img, int newSize) {
        BufferedImage img2 = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_ARGB);
        img2.getGraphics().drawImage(img, 0, 0, newSize, newSize, null);
        return img2;
    }

    public static class ImageViewerFrame extends JFrame {
        private static final int DEFAULT_SIZE = 24;
        public static final int DEFAULT_WIDTH = 570;
        public static final int DEFAULT_HEIGHT = 400;

        public ImageViewerFrame(BufferedImage file) {
            setTitle("ImageViewer");
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            JLabel label = new JLabel();
            Container contentPane = getContentPane();
            contentPane.add(label, BorderLayout.CENTER);
            JPanel comboPanel = new JPanel();
            contentPane.add(comboPanel, BorderLayout.SOUTH);
            label.setIcon(new ImageIcon(file));
        }
    }
}
