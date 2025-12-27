package com.meng.tools.normal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileTool {

    public static String getAutoFileName(byte[] fileBytes) {
        return Hash.getMd5Instance().calculate(fileBytes).toUpperCase() + "." + FileFormat.getFileType(fileBytes);
    }

    public static List<File> listAllFiles(File file) {
        ArrayList<File> result = new ArrayList<>();
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                result.addAll(listAllFiles(f));
            }
            if (f.isFile()) {
                result.add(f);
            }
        }
        return result;
    }

    public static void deleteFiles(File folder) {
        File[] fs = folder.listFiles();
        if (fs != null && fs.length > 0) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    deleteFiles(f);
                    f.delete();
                } else {
                    f.delete();
                }
            }
        }
    }

    public static void fileCopy(String src, String des) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(des));
        int i = -1;
        byte[] bt = new byte[1024];
        while ((i = bis.read(bt)) != -1) {
            bos.write(bt, 0, i);
        }
        bis.close();
        bos.close();
    }

    public static String readString(String fileName) throws IOException {
        return readString(new File(fileName));
    }

    public static String readString(File f) throws IOException {
        long filelength = f.length();
        byte[] filecontent = new byte[(int) filelength];
        FileInputStream in = new FileInputStream(f);
        in.read(filecontent);
        in.close();
        return new String(filecontent, StandardCharsets.UTF_8);
    }

    public static byte[] readBytes(File f) throws IOException { 
        long filelength = f.length();
        byte[] filecontent = new byte[(int) filelength];
        FileInputStream in = new FileInputStream(f);
        in.read(filecontent);
        in.close();
        return filecontent;
    }

    public static byte[] readBytes(String path) throws IOException {
        return readBytes(new File(path));
    }

    public static File createFile(String path) throws IOException {
        return createFile(new File(path));
    }

    public static File createFile(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static void saveFile(File file, byte[] content) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
    }
}
