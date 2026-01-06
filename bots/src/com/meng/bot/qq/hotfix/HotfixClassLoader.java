package com.meng.bot.qq.hotfix;

import java.util.HashMap;
import java.util.Map;

public class HotfixClassLoader extends ClassLoader {
    private final Map<String, byte[]> classes = new HashMap<>();

    private static HotfixClassLoader instance;

    public static HotfixClassLoader getInstance() {
        if (instance == null) {
            instance = new HotfixClassLoader();
        }
        return instance;
    }

    private HotfixClassLoader() {
        super(getSystemClassLoader());
    }

    public void put(String className, byte[] code) {
        classes.put(className, code);
    }

    public byte[] getCode(String name) {
        return classes.get(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        byte[] classBytes = classes.get(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classes.get(name);
        if (classBytes == null) {
            throw new ClassNotFoundException();
        }
        Class<?> cl = defineClass(name, classBytes, 0, classBytes.length);
        if (cl == null) {
            throw new ClassNotFoundException();
        }
        return cl;
    }
}
