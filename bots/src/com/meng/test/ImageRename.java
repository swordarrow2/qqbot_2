package com.meng.test;

import com.meng.tools.normal.FileTool;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageRename {
    public static void main(String[] args) {
        List<File> sanae = FileTool.listAllFiles(new File("C:\\sanae_data\\image\\r15\\刻晴"));
        sanae.parallelStream().forEach(file -> {
            try {
                File nf = new File(file.getParent(), FileTool.getAutoFileName(FileTool.readBytes(file)));
                if (!nf.getAbsolutePath().equals(file.getAbsolutePath())) {
                    file.renameTo(nf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
