package com.meng.tools.normal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSON {
    private static final Gson gson;

    static {
        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();
        gb.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        gson = gb.create();
    }

    public static <T> T fromJson(String json, Class<T> clz) {
        try {
            return (T) gson.fromJson(json, clz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            e.addSuppressed(new RuntimeException(json));
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
        }
        return null;
    }

    public static <T> T fromJson(String json, Type t) {
        try {
            return (T) gson.fromJson(json, t);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            e.addSuppressed(new RuntimeException(json));
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
        }
        return null;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static String formatJson(String content) {
        if (content == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int count = 0;
        boolean inString = false;
        while (index < content.length()) {
            char ch = content.charAt(index);
            if (ch == '"' && content.charAt(index - 1) != '\\') {
                inString = !inString;
            }
            if (ch == '{' || ch == '[') {
                sb.append(ch);
                sb.append('\n');
                count++;
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
            } else if (ch == '}' || ch == ']') {
                sb.append('\n');
                count--;
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
                sb.append(ch);
            } else if (ch == ',') {
                sb.append(ch);
                if (!inString) {
                    sb.append('\n');
                }
                for (int i = 0; i < count; i++) {
                    sb.append('\t');
                }
            } else {
                sb.append(ch);
            }
            index++;
        }
        return sb.toString();
    }

    /**
     * 把格式化的json紧凑
     *
     * @param content
     * @return
     */
    public static String compactJson(String content) {
        String regEx = "[\t\n]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(content);
        return m.replaceAll("").trim();
    }
}
