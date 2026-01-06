package com.meng.tools.normal;

import com.meng.tools.sjf.SJFExecutors;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;

public class FileWatcherService {

    private static final FileWatcherService instance = new FileWatcherService();
    private final HashMap<WatchService, FileWatchedListener> listeners = new HashMap<>();

    public static FileWatcherService getInstance() {
        return instance;
    }

    public void addListener(String strPath, FileWatchedListener listener) {
        try {
            Path path = Paths.get(strPath);
            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE, // 监听文件创建事件
                    StandardWatchEventKinds.ENTRY_DELETE, // 监听文件删除事件
                    StandardWatchEventKinds.ENTRY_MODIFY); // 监听文件修改事件
            listeners.put(watchService, listener);
            SJFExecutors.execute(() -> {
                try {
                    watch(watchService);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileWatcherService() {

    }

    private void watch(WatchService watchService) throws InterruptedException {
        while (true) {
            WatchKey watchKey = watchService.take();
            List<WatchEvent<?>> watchEventList = watchKey.pollEvents();
            for (WatchEvent<?> watchEvent : watchEventList) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                WatchEvent<Path> curEvent = (WatchEvent<Path>) watchEvent;
                try {
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        if (listeners.containsKey(watchService)) {
                            listeners.get(watchService).onModified(curEvent);
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (listeners.containsKey(watchService)) {
                            listeners.get(watchService).onCreated(curEvent);
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        if (listeners.containsKey(watchService)) {
                            listeners.get(watchService).onDeleted(curEvent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
    }
}

