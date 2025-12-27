package com.meng.api;

import com.meng.tools.normal.FileTool;
import com.meng.tools.normal.Hash;
import com.meng.tools.normal.Network;
import com.meng.tools.sjf.SJFPathTool;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LkaaApi {
    public static File generalVoice(String text) throws IOException {
        File voiceFile = SJFPathTool.getTTSPath(Hash.getMd5Instance().calculate(text.getBytes(StandardCharsets.UTF_8)) + ".mp3");
        if (!voiceFile.exists()) {
            byte[] voice = Network.httpGetRaw(Network.httpGet("http://lkaa.top/API/yuyin/api.php?msg=" + text + "&type=text"));
            FileTool.saveFile(voiceFile, voice);             
        }
        return voiceFile;
    }

    public static String generalTranslate(String text) throws IOException {
        return Network.httpGet("http://lkaa.top/API/qqfy/api.php?msg=" + text + "&type=male");
    }
}
