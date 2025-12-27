package com.meng.tools.normal;

import com.meng.bot.SJFPermissionDeniedException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.meng.bot.qq.BotWrapper;
import com.meng.tools.sjf.SJFPathTool;
import com.meng.bot.qq.QqBotMain;

public class ExceptionCatcher implements Thread.UncaughtExceptionHandler {

    private static ExceptionCatcher mInstance = new ExceptionCatcher();

    private ExceptionCatcher() {
        Thread.setDefaultUncaughtExceptionHandler(this);
	}

    public static synchronized ExceptionCatcher getInstance() {
        if (null == mInstance) {
            mInstance = new ExceptionCatcher();
		}
        return mInstance;
	}

    public void uncaughtException(Throwable ex) {  
        uncaughtException(Thread.currentThread(), ex);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {  
        ex.printStackTrace();
        saveCrashInfo2File(ex);
    }

    public void catchException(BotWrapper botWrapper, Throwable exception) {
        if (exception instanceof SJFPermissionDeniedException) {
            SJFPermissionDeniedException pde = (SJFPermissionDeniedException)exception;
            botWrapper.getBotMessageHandler().sendQuote(pde.event, "Permission Denied.");
            return;
        }
        uncaughtException(Thread.currentThread(), exception);
    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
		}
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            File file = SJFPathTool.getCrashLog();
            FileTool.saveFile(file, sb.toString().replace("java", "jvav").getBytes(StandardCharsets.UTF_8));
            return file.getAbsolutePath();
		} catch (Exception e) {
            e.printStackTrace();
		}
        return null;
	}
}
