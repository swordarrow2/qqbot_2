package com.meng.tools.sjf;

import com.meng.tools.normal.TextLexer;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CommandList {
    private final ArrayList<Message> cmds = new ArrayList<>();
    private int pointer;
    private final long group;
    private final long qq;
    private final String senderName;

    public CommandList(GroupMessageEvent event) {
        group = event.getGroup().getId();
        qq = event.getSender().getId();
        senderName = event.getSenderName();
        for (SingleMessage singleMessage : event.getMessage()) {
            if (singleMessage instanceof PlainText) {
                ArrayList<String> list = TextLexer.analyze(singleMessage.toString());
                for (String s : list) {
                    cmds.add(new PlainText(s));
                }
            } else {
                cmds.add(singleMessage);
            }
        }
    }

    public long getGroup() {
        return group;
    }

    public long getQq() {
        return qq;
    }

    public String getSenderName() {
        return senderName;
    }

    public boolean hasNext() {
        return pointer < cmds.size() - 1;
    }

    public Message next() {
        return cmds.get(pointer++);
    }

    public String nextString() {
        return cmds.get(pointer++).toString();
    }

    public int nextInt() {
        return Integer.parseInt(nextString());
    }

    public long nextLong() {
        return Long.parseLong(nextString());
    }

    public At nextAt() {
        return (At) cmds.get(pointer++);
    }

    public Image nextImage() {
        return (Image) cmds.get(pointer++);
    }

    public Audio nextAudio() {
        return (Audio) cmds.get(pointer++);
    }

    public void reset() {
        pointer = 0;
    }

    public <T extends Message> Class<T> getNextType() {
        return (Class<T>) cmds.get(pointer + 1).getClass();
    }
}
