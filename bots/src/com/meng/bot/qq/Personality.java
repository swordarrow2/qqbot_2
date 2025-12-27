package com.meng.bot.qq;

import com.meng.api.mhy.Bh3Api;
import com.meng.tools.sjf.SJFPathTool;

import java.io.File;

public enum Personality {
    None(Tag.Other) {
        @Override
        public File getVoiceFolder() {
            return null;
        }
    },
    Seele_White(Tag.HonKai_3rd) {
        @Override
        public File getVoiceFolder() {
            return SJFPathTool.getAudioPath(Bh3Api.getEngName("幻海梦蝶").replaceAll("[^a-zA-Z0-9]", ""));
        }
    },
    Seele_Black(Tag.HonKai_3rd) {
        @Override
        public File getVoiceFolder() {
            return SJFPathTool.getAudioPath(Bh3Api.getEngName("魇夜星渊").replaceAll("[^a-zA-Z0-9]", ""));
        }
    },
    Seele_Mix(Tag.HonKai_3rd) {
        @Override
        public File getVoiceFolder() {
            return SJFPathTool.getAudioPath(Bh3Api.getEngName("彼岸双生").replaceAll("[^a-zA-Z0-9]", ""));
        }
    },
    Yakumo_Ran(Tag.TouHou) {
        @Override
        public File getVoiceFolder() {
            return null;
        }
    },
    Eternity_Larva(Tag.TouHou) {
        @Override
        public File getVoiceFolder() {
            return null;
        }
    },
    Flandre_Scalet(Tag.TouHou) {
        @Override
        public File getVoiceFolder() {
            return null;
        }
    },
    Kochiya_Sanae(Tag.TouHou) {
        @Override
        public File getVoiceFolder() {
            return null;
        }
    };


    private Personality(Tag tag) {
        this.tag = tag;
    }

    private Tag tag;

    public Tag getTag() {
        return tag;
    }

    public abstract File getVoiceFolder();

    public enum Tag {
        Other,
        TouHou,
        HonKai_3rd
    }
}
