package com.meng.tools.normal;

import com.meng.tools.sjf.SJFExecutors;
import com.meng.tools.sjf.SJFPathTool;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FileWatcherService {

    private static final FileWatcherService instance = new FileWatcherService();

    private WatchService watchService;
    private final HashMap<String, FileWatchedListener> listeners = new HashMap<>();
    private final HashSet<String> noActionOnceSet = new HashSet<>();

    public static FileWatcherService getInstance() {
        return instance;
    }

    public void addListener(String fileName, FileWatchedListener listener) {
        listeners.put(fileName, listener);
    }

    public void registNoActionOnce(String fileName) {
        noActionOnceSet.add(fileName);
    }

    private FileWatcherService() {
        Path path = Paths.get(SJFPathTool.getPersistentPath());
        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService,
                    /// 监听文件创建事件
                    StandardWatchEventKinds.ENTRY_CREATE,
                    /// 监听文件删除事件
                    StandardWatchEventKinds.ENTRY_DELETE,
                    /// 监听文件修改事件
                    StandardWatchEventKinds.ENTRY_MODIFY);

            SJFExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        watch();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void watch() throws InterruptedException {
        while (true) {
            WatchKey watchKey = watchService.take();
            List<WatchEvent<?>> watchEventList = watchKey.pollEvents();
            for (WatchEvent<?> watchEvent : watchEventList) {
                WatchEvent.Kind<?> kind = watchEvent.kind();

                WatchEvent<Path> curEvent = (WatchEvent<Path>) watchEvent;
                String fileName = curEvent.context().toFile().getName();
                if (noActionOnceSet.contains(fileName)) {
                    noActionOnceSet.remove(fileName);
                    continue;
                }
                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    System.out.printf("文件【%s】被修改，时间：%s%n", fileName, TimeFormater.getTime());
                    if (listeners.containsKey(fileName)) {
                        listeners.get(fileName).onModified(curEvent);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    System.out.printf("文件【%s】被创建，时间：%s%n", fileName, TimeFormater.getTime());
                    if (listeners.containsKey(fileName)) {
                        listeners.get(fileName).onCreated(curEvent);
                    }
                } else if (kind == StandardWatchEventKinds.OVERFLOW) {
                    System.out.printf("文件【%s】被丢弃，时间：%s%n", fileName, TimeFormater.getTime());
                    if (listeners.containsKey(fileName)) {
                        listeners.get(fileName).onOverflowed(curEvent);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    System.out.printf("文件【%s】被删除，时间：%s%n", fileName, TimeFormater.getTime());
                    if (listeners.containsKey(fileName)) {
                        listeners.get(fileName).onDeleted(curEvent);
                    }
                }
            }

            /**
             * WatchKey 有两个状态：
             * {@link sun.nio.fs.AbstractWatchKey.State.READY ready} 就绪状态：表示可以监听事件
             * {@link sun.nio.fs.AbstractWatchKey.State.SIGNALLED signalled} 有信息状态：表示已经监听到事件，不可以接续监听事件
             * 每次处理完事件后，必须调用 reset 方法重置 watchKey 的状态为 ready，否则 watchKey 无法继续监听事件
             */
            if (!watchKey.reset()) {
                break;
            }

        }
    }

    public interface FileWatchedListener {
        void onCreated(WatchEvent<Path> watchEvent);

        void onDeleted(WatchEvent<Path> watchEvent);

        void onModified(WatchEvent<Path> watchEvent);

        void onOverflowed(WatchEvent<Path> watchEvent);
    }
}

