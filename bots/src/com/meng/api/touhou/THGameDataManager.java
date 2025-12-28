package com.meng.api.touhou;

import com.meng.api.touhou.gameData.TH06Data;
import com.meng.api.touhou.gameData.TH07Data;
import com.meng.api.touhou.gameData.TH08Data;
import com.meng.api.touhou.gameData.TH10Data;
import com.meng.api.touhou.gameData.TH11Data;
import com.meng.api.touhou.gameData.TH12Data;
import com.meng.api.touhou.gameData.TH13Data;
import com.meng.api.touhou.gameData.TH14Data;
import com.meng.api.touhou.gameData.TH15Data;
import com.meng.api.touhou.gameData.TH16Data;
import com.meng.api.touhou.gameData.TH17Data;
import com.meng.api.touhou.gameData.TH18Data;
import com.meng.api.touhou.gameData.THSSSData;
import com.meng.tools.sjf.SJFRandom;

import java.util.HashSet;
import java.util.function.Consumer;

public class THGameDataManager {

    public static String[] pl01 = {"别打砖块了，来飞机"};
    public static String[] pl02 = {"范围重视型", "高灵击伤害 平衡型", "威力重视型"};
    public static String[] pl03 = {"博丽灵梦", "魅魔", "雾雨魔理沙", "爱莲", "小兔姬", "卡娜·安娜贝拉尔", "朝仓理香子", "北白河千百合", "冈崎梦美"};
    public static String[] pl04 = {"博丽灵梦 诱导", "博丽灵梦 大范围", "雾雨魔理沙 激光", "雾雨魔理沙 高速射击"};
    public static String[] pl05 = {"博丽灵梦", "雾雨魔理沙", "魅魔", "幽香"};
    public static String[] pl09 = {"博丽灵梦", "雾雨魔理沙", "十六夜咲夜", "魂魄妖梦", "铃仙·优昙华院·因幡", "琪露诺", "莉莉卡·普莉兹姆利巴", "梅露兰·普莉兹姆利巴", "露娜萨·普莉兹姆利巴", "米斯蒂娅·萝蕾拉", "因幡帝", "射命丸文", "梅蒂欣·梅兰可莉", "风见幽香", "小野冢小町", "四季映姬·亚玛萨那度"};
    public static String[] plDiff = {"easy", "normal", "hard", "lunatic"};
    public static String[] neta = {"红lnb", "红lnm", "妖lnm", "妖lnn", "永lnm", "风lnm", "风lnn", "殿lnm", "船lnm", "船lnn", "庙lnm", "城lnm", "绀lnm", "璋lnn"};
    public static String[] wayToGoodEnd = {"红魔乡normal", "妖妖梦easy", "永夜抄6B", "风神录normal", "地灵殿normal", "星莲船normal", "神灵庙normal", "辉针城灵梦B", "辉针城魔理沙B", "辉针城咲夜B", "绀珠传no miss", "天空璋extra", "鬼形兽normal"};

    private static THGameData[] thGameData = null;

    public static THGameData[] getThGameData() {
        if (thGameData == null) {
            thGameData = new THGameData[]{
                    TH06Data.getInstance(),
                    TH07Data.getInstance(),
                    TH08Data.getInstance(),
                    TH10Data.getInstance(),
                    TH11Data.getInstance(),
                    TH12Data.getInstance(),
                    TH13Data.getInstance(),
                    TH14Data.getInstance(),
                    TH15Data.getInstance(),
                    TH16Data.getInstance(),
                    TH17Data.getInstance(),
                    TH18Data.getInstance(),
                    THSSSData.getInstance()
            };
        }
        return thGameData;
    }

    public static THCharacter getCharacter(String name) {
        for (THGameData data : thGameData) {
            THCharacter c = data.getCharacter(name);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    public static THSpell getSpellFromDiff(int diffFlag) {
        THSpell splc;
        while (true) {
            THGameData spellss = SJFRandom.randomSelect(thGameData);
            splc = SJFRandom.randomSelect(spellss.getSpellCards());
            if ((splc.difficult & diffFlag) != 0) {
                return splc;
            }
        }
    }

    public static THSpell[] getSpellFromNotDiff(int count, int diffFlag) {
        THSpell[] spshs = new THSpell[count];
        for (int i = 0; i < count; ++i) {
            THSpell splc;
            while (true) {
                THGameData spellss = SJFRandom.randomSelect(thGameData);
                splc = SJFRandom.randomSelect(spellss.getSpellCards());
                if ((splc.difficult & diffFlag) == 0) {
                    spshs[i] = splc;
                    break;
                }
            }
        }
        return spshs;
    }

    public static THSpell getTHSpell(String spellName) {
        for (THGameData scs : thGameData) {
            for (THSpell sc : scs.getSpellCards()) {
                if (sc.cnName.contains(spellName) || sc.jpName.contains(spellName)) {
                    return sc;
                }
            }
        }
        return null;
    }

    public static THSpell getTHSpell(String spellName, int diff) {
        for (THGameData scs : thGameData) {
            for (THSpell sc : scs.getSpellCards()) {
                if (sc.cnName.contains(spellName) && sc.difficult == diff) {
                    return sc;
                }
            }
        }
        return null;
    }

    public static HashSet<THSpell> getCharaTHSpell(final String name) {
        final HashSet<THSpell> hscs = new HashSet<>();
        forEachSpell(sc -> {
            if (sc.master.equals(name)) {
                hscs.add(sc);
            }
        });
        return hscs;
    }

    public static HashSet<THSpell> getCharaTHSpell(final String name, final int diff) {
        final HashSet<THSpell> hscs = new HashSet<>();
        forEachSpell(sc -> {
            if (sc.master.equals(name) && sc.difficult == diff) {
                hscs.add(sc);
            }
        });
        return hscs;
    }

    public static HashSet<THSpell> getCharaTHSpell(final String name, final String... spellExcept) {
        final HashSet<THSpell> hscs = new HashSet<>();
        forEachSpell(sc -> {
            if (!sc.master.equals(name)) {
                return;
            }
            for (String necx : spellExcept) {
                if (!sc.cnName.equals(necx)) {
                    hscs.add(sc);
                }
            }
        });
        return hscs;
    }

    public static String randomPlane(String game) {
        return switch (game) {
            case "东方灵异传", "th1", "th01" -> SJFRandom.randomSelect(pl01);
            case "东方封魔录", "th2", "th02" -> SJFRandom.randomSelect(pl02);
            case "东方梦时空", "th3", "th03" -> SJFRandom.randomSelect(pl03);
            case "东方幻想乡", "th4", "th04" -> SJFRandom.randomSelect(pl04);
            case "东方怪绮谈", "th5", "th05" -> SJFRandom.randomSelect(pl05);
            case "东方红魔乡", "th6", "th06", "tEoSD" ->
                    SJFRandom.randomSelect(TH06Data.getInstance().getPlayers()).randomType();
            case "东方妖妖梦", "th7", "th07", "PCB" ->
                    SJFRandom.randomSelect(TH07Data.getInstance().getPlayers()).randomType();
            case "东方永夜抄", "th8", "th08", "IN" ->
                    SJFRandom.randomSelect(TH08Data.getInstance().getPlayers()).randomType();
            case "东方花映塚", "东方花映冢", "th9", "th09", "PoFV" -> SJFRandom.randomSelect(pl09);
            case "东方风神录", "th10", "MoF" ->
                    SJFRandom.randomSelect(TH10Data.getInstance().getPlayers()).randomType();
            case "东方地灵殿", "th11" -> SJFRandom.randomSelect(TH11Data.getInstance().getPlayers()).randomType();
            case "东方星莲船", "th12", "UFO" ->
                    SJFRandom.randomSelect(TH12Data.getInstance().getPlayers()).randomType();
            case "东方神灵庙", "th13", "TD" -> SJFRandom.randomSelect(TH13Data.getInstance().getPlayers()).randomType();
            case "东方辉针城", "th14", "DDC" ->
                    SJFRandom.randomSelect(TH14Data.getInstance().getPlayers()).randomType();
            case "东方绀珠传", "th15", "LoLK" ->
                    SJFRandom.randomSelect(TH15Data.getInstance().getPlayers()).randomType();
            case "东方天空璋", "th16", "HSiFS" ->
                    SJFRandom.randomSelect(TH16Data.getInstance().getPlayers()).randomType();
            case "东方鬼形兽", "th17", "WBaWC" ->
                    SJFRandom.randomSelect(TH17Data.getInstance().getPlayers()).randomType();
            case "东方虹龙洞", "th18", "UM" -> SJFRandom.randomSelect(TH18Data.getInstance().getPlayers()).randomType();
            case "东方文花帖", "th9.5", "StB", "东方文花帖DS", "th12.5", "DS", "妖精大战争", "th12.8", "弹幕天邪鬼",
                 "th14.3", "ISC", "秘封噩梦日记", "th16.5", "VD" -> "就一个飞机你roll你🐴呢";
            default -> "只有2un飞机游戏";
        };
    }

    public static String randomGame(String pname, long fromQQ, boolean goodAt) {
        int gameNo = SJFRandom.randomInt(17) + 2;
        String gameName = null;
        String charaName = null;
        switch (gameNo) {
            case 2:
                gameName = "封魔录";
                charaName = SJFRandom.hashSelect(fromQQ + 2, pl02);
                break;
            case 3:
                gameName = "梦时空";
                charaName = SJFRandom.hashSelect(fromQQ + 2, pl03);
                break;
            case 4:
                gameName = "幻想乡";
                charaName = SJFRandom.hashSelect(fromQQ + 2, pl04);
                break;
            case 5:
                gameName = "怪绮谈";
                charaName = SJFRandom.hashSelect(fromQQ + 2, pl05);
                break;
            case 6:
                gameName = "红魔乡";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH06Data.getInstance().getPlayers()).randomType();
                break;
            case 7:
                gameName = "妖妖梦";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH07Data.getInstance().getPlayers()).randomType();
                break;
            case 8:
                gameName = "永夜抄";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH08Data.getInstance().getPlayers()).randomType();
                break;
            case 9:
                gameName = "花映塚";
                charaName = SJFRandom.hashSelect(fromQQ + 2, pl09);
                break;
            case 10:
                gameName = "风神录";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH10Data.getInstance().getPlayers()).randomType();
                break;
            case 11:
                gameName = "地灵殿";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH11Data.getInstance().getPlayers()).randomType();
                break;
            case 12:
                gameName = "星莲船";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH12Data.getInstance().getPlayers()).randomType();
                break;
            case 13:
                gameName = "神灵庙";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH13Data.getInstance().getPlayers()).randomType();
                break;
            case 14:
                gameName = "辉针城";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH14Data.getInstance().getPlayers()).randomType();
                break;
            case 15:
                gameName = "绀珠传";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH15Data.getInstance().getPlayers()).randomType();
                break;
            case 16:
                gameName = "天空璋";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH16Data.getInstance().getPlayers()).randomType();
                break;
            case 17:
                gameName = "鬼形兽";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH17Data.getInstance().getPlayers()).randomType();
                break;
            case 18:
                gameName = "虹龙洞";
                charaName = SJFRandom.hashSelect(fromQQ + 2, TH18Data.getInstance().getPlayers()).randomType();
                break;
            default:
                return "";
        }
        if (goodAt) {
            return String.format("%s今天宜用%s打%s", pname, charaName, gameName);
        } else {
            return String.format("忌用%s打%s", charaName, gameName);
        }
    }

    public static String generalTranslate(String text) {
        THGameData[] thGameData = THGameDataManager.getThGameData();
        if (text.equalsIgnoreCase("Phantasmagoria of Flower View")) {
            return "东方花映塚";
        }
        for (THGameData game : thGameData) {
            if (text.equals(game.getNameCN())) {
                return game.getNameEng();
            }
            if (text.equals(game.getNameEng())) {
                return game.getNameCN();
            }
            if (text.equals(game.getNameAbbr())) {
                return game.getNameFull();
            }
            for (THMusic mu : game.getMusics()) {
                String muCN = mu.getNameCN();
                String muEng = mu.getNameEng();
                if (muCN == null || muEng == null) {
                    continue;
                }
                if (text.equals(muCN)) {
                    return muEng;
                }
                if (text.equals(muEng)) {
                    return muCN;
                }
            }
            for (THSpell spell : game.getSpellCards()) {
                if (text.equals(spell.getNameEng()) || text.equals(spell.getNameAbbr())) {
                    return spell.cnName;
                }
                if (spell.cnName.contains(text)) {
                    return spell.jpName;
                }
                if (text.equals(spell.jpName)) {
                    return spell.cnName;
                }
                for (String nick : spell.nick) {
                    if (text.equals(nick)) {
                        return spell.jpName;
                    }
                }
            }
            StringBuilder nickBuilder = new StringBuilder();
            for (THCharacter chara : game.getCharacters()) {
                if (text.equals(chara.name)) {
                    nickBuilder.append(chara.nick).append(" ");
                }
                if (text.equals(chara.nick)) {
                    return chara.name;
                }
            }
            if (nickBuilder.length() > 0) {
                return nickBuilder.toString();
            }
        }
        return null;
    }

    public static String randomGE() {
        return SJFRandom.randomSelect(wayToGoodEnd);
    }

    public static String hashSelectGE(long seed) {
        return SJFRandom.hashSelect(seed, wayToGoodEnd);
    }

    public static String randomNeta() {
        return SJFRandom.randomSelect(neta);
    }

    public static String hashSelectNeta(long seed) {
        return SJFRandom.hashSelect(seed, neta);
    }

    public static THSpell randomSpell() {
        return SJFRandom.randomSelect(thGameData).randomSpell();
    }

    public static THMusic randomMusic() {
        return SJFRandom.randomSelect(thGameData).randomMusic();
    }

    public static THSpell hashRandomSpell(long fromQQ) {
        return SJFRandom.hashSelect(fromQQ, SJFRandom.hashSelect(fromQQ, thGameData).getSpellCards());
    }

    public static THCharacter hashRandomCharacter(long fromQQ) {
        return SJFRandom.hashSelect(fromQQ, SJFRandom.hashSelect(fromQQ, thGameData).getCharacters());
    }

    public static THMusic hashSelectMusic(long fromQQ) {
        return SJFRandom.hashSelect(fromQQ, SJFRandom.hashSelect(fromQQ, thGameData).getMusics());
    }

    public static void forEachCharacter(Consumer<THCharacter> consumer) {
        for (THGameData data : thGameData) {
            data.forEachCharacter(consumer);
        }
    }

    public static void forEachSpell(Consumer<THSpell> consumer) {
        for (THGameData data : thGameData) {
            data.forEachSpell(consumer);
        }
    }

    public static void forEachMusic(Consumer<THMusic> consumer) {
        for (THGameData data : thGameData) {
            data.forEachMusic(consumer);
        }
    }
}
