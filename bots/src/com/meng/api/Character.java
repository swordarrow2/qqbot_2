package com.meng.api;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.meng.bot.annotation.BotData;
import com.meng.bot.qq.BaseModule;

public class Character extends BaseModule {

    private static Character instance = null;

    public static Character getInstance() {
        if (instance == null) {
            instance = new Character();
            instance.load();
        }
        return instance;
    }

    @BotData("characters_map.json")
    public LinkedHashMap<String, HashSet<String>> characterMap = new LinkedHashMap<>();

    //    static {
//        for (String character : instance.characterMap.keySet()) {
//            File folder = SJFPathTool.getR15Path(character);
//            if (!folder.exists()) {
//                folder.mkdirs();
//            }
//        }
//    }
    private Character() {
    }

    public String getCharaterName(String src) {
        for (String t : characterMap.keySet()) {
            if (src.length() == 1) {
                if (t.equals(src)) {
                    return t;
                }
            } else {
                if (t.contains(src)) {
                    return t;
                }
            }

        }
        for (Map.Entry<String, HashSet<String>> entry : characterMap.entrySet()) {
            HashSet<String> value = entry.getValue();
            if (value.size() == 0) {
                continue;
            }
            for (String n : value) {
                if (n.contains(src)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
