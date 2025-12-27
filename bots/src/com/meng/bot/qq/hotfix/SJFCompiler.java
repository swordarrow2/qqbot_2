package com.meng.bot.qq.hotfix;

import com.meng.bot.qq.BotWrapper;

import javax.tools.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 司徒灵羽
 */

public class SJFCompiler {

    public static HotfixClassLoader generate(BotWrapper botHelper, HotfixClassLoader classloader, String className, String code) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final List<ByteArrayJavaClass> classFileObjects = new ArrayList<>();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager = new ForwardingJavaFileManager<JavaFileManager>(fileManager) {
            public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                ByteArrayJavaClass fileObject = new ByteArrayJavaClass(className);
                classFileObjects.add(fileObject);
                return fileObject;
            }
        };
        StringBuilderJavaSource source = new StringBuilderJavaSource(className);
        source.append(code);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(source));
        boolean result = task.call();
        StringBuilder builder = new StringBuilder(".");
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            builder.append(d.getKind()).append(":").append(d.getMessage(null)).append("\n");
        }
        botHelper.getBotMessageHandler().sendGroupMessage(BotWrapper.yysGroup, builder.toString());
        fileManager.close();
        if (!result) {
            System.out.println("compile failed");
            return classloader;
        }
        for (ByteArrayJavaClass cl : classFileObjects) {
            classloader.put(cl.getName().substring(1), cl.getBytes());
            System.out.println(cl.getName().substring(1));
        }
        return classloader;
    }
}
