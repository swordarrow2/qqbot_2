package com.meng.api.touhou;
import com.meng.tools.sjf.SJFRandom;
import com.meng.bot.BasePojo;

public class THPlayer extends BasePojo {
    public String name;
    public String[] type;

    public THPlayer(String name) {
        this.name = name;
    }

    public THPlayer(String name, String... type) {
        this.name = name;
        this.type = type;
    }

    public String randomType() {
        if (type == null) {
            return name;
        }
        return name + "-" + SJFRandom.randomSelect(type);
    }
}
