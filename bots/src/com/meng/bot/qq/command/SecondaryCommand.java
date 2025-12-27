package com.meng.bot.qq.command;

public enum SecondaryCommand {
    dice_roll_plane("随机自机", "plane", "player", "pl"),

    dice_draw_spell("随机符卡", "spell"),
    dice_draw_neta("随机neta", "neta"),
    dice_draw_music("随机音乐", "music"),
    dice_draw_grandma("随机认奶奶", "grandma"),
    dice_draw_game("随机游戏", "game"),
    dice_draw_goodEnd("随机GE", "goodend"),
    dice_draw_ufo("随机ufo", "ufo"),
    dice_draw_all("all", "all"),

    music_test_easy("原曲认知easy", "e"),
    music_test_normal("原曲认知normal", "n"),
    music_test_hard("原曲认知hard", "h"),
    music_test_lunatic("原曲认知lunatic", "l"),

    searchPicture("搜索图片", "sp"),
    //    imageTag((CharSequence)"图片标签","tag"),
//    imagePorn((CharSequence)"图片色情程度","porn"),
//    imageOcr((CharSequence)"光学字符识别","ocr"),
    getImageUrl("图片url", "url"),
    //    getImageDeepDanbooruTag((CharSequence)"dtag","dtag"),
    imageToGray("生成灰度图", "灰度图", "灰阶图"),
    imageRotate("旋转图片180度", "图片旋转", "旋转图片"),
    imageUpsideDown("上下翻转图片", "上下翻转", "天下翻覆"),
    imageFlip("左右翻转图片", "左右翻转", "镜之国的弹幕", "镜之国"),
    imageUpSeija("天壤梦弓", "天壤梦弓"),
    expression_jingShenZhiZhu("表情包精神支柱", "精神支柱"),
    expression_shenChu("表情包神触", "神触"),
    expression_xiaoHuaJia("表情包小画家", "小画家"),
    expression_JiXuGanHuo("表情包继续干活", "继续干活"),
    expression_WoYongYuanXiHuan("表情包我永远喜欢", "我永远喜欢"),
    expression_FaDian("表情包发癫", "发癫"),
    expression_BuKeYiJianMian("表情包不可以见面", "不可以见面"),
    expression_Pa("表情包爬", "爬"),
    expression_ZaiXiang("表情包在想", "在想"),
    expression_BaoJin("表情包抱紧", "抱紧"),


    sign("签到", "签到"),
    getCoins("查看硬币数量", "查看硬币", "查询硬币"),
    addCoins("添加硬币", "添加硬币");

    SecondaryCommand(String note, String... cmd) {
        this.cmds = cmd;
        this.note = note;
    }

    public String[] getCmd() {
        return cmds;
    }

    public final String[] cmds;
    public final String note;
}
