package com.meng.bot.qq.hotfix;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class ByteArrayJavaClass extends SimpleJavaFileObject {
    private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public ByteArrayJavaClass(String name) {
        super(URI.create("bytes:///" + name), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
        return stream;
    }

    public byte[] getBytes() {
        return stream.toByteArray();
    }
}
