package com.meng.bot.config;

import com.meng.bot.annotation.BotData;
import com.meng.bot.qq.BaseModule;
import com.meng.tools.normal.ExceptionCatcher;
import com.meng.tools.normal.FileTool;
import com.meng.tools.normal.FileWatcherService;
import com.meng.tools.normal.JSON;
import com.meng.tools.sjf.SJFPathTool;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * @author: 司徒灵羽
 **/
public class DataPersistenter {

    private DataPersistenter() {

    }

    public synchronized static boolean save(BaseModule module) {
//        if (module instanceof ICommonData) {
//            ICommonData iCommonData = (ICommonData) module;
//            File file = SJFPathTool.getPersistentPath(module.getModuleName() + ".json");
//            try {
//                FileTool.saveFile(file, JSON.toJson(iCommonData.getData()).getBytes(StandardCharsets.UTF_8));
//            } catch (IOException e) {
//                ExceptionCatcher.getInstance().uncaughtException(e);
//            }
//        }

        Class<?> moduleClass = module.getClass();
        //Field[] fields = classObj.getFields();        //只能获取public
        Field[] fields = moduleClass.getDeclaredFields();  //public和private
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(BotData.class)) {
                BotData annotationField = field.getAnnotation(BotData.class);
                File file = SJFPathTool.getPersistentPath(annotationField.value());
                FileWatcherService.getInstance().registNoActionOnce(file.getName());
                try {
                    FileTool.saveFile(file, JSON.toJson(field.get(module)).getBytes(StandardCharsets.UTF_8));
                } catch (IOException | IllegalAccessException | IllegalArgumentException e) {
                    ExceptionCatcher.getInstance().uncaughtException(e);
                }
            }

        }
        return true;
    }

    public synchronized static <T> boolean read(BaseModule module) {
//        if (module instanceof ICommonData) {
//            ICommonData<T> iCommonData = (ICommonData) module;
//            File file = SJFPathTool.getPersistentPath(module.getModuleName() + ".json");
//            Type[] types = iCommonData.getClass().getGenericInterfaces();
//            Type actualType = null;
//            for (Type ta : types) {
//                ParameterizedType pt = (ParameterizedType) ta;
//                if (pt.getRawType() == ICommonData.class) {
//                    Type[] actualTypes = pt.getActualTypeArguments();
//                    actualType = actualTypes[0];
//                }
//            }
//            try {
//                if (file.exists()) {
//                    if (actualType != null) {
//                        String json = FileTool.readString(file);
//                        if (json.equals("null")) {
//                            iCommonData.setData((T) Class.forName(actualType.getTypeName()).getDeclaredConstructor().newInstance());
//                        } else {
//                            iCommonData.setData((T) JSON.fromJson(json, actualType));
//                        }
//                    }
//                } else {
//                    iCommonData.setData((T) Class.forName(actualType.getTypeName()).getDeclaredConstructor().newInstance());
//                }
//            } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }
        Class<?> moduleClass = module.getClass();
        //Field[] fields = classObj.getFields();        //只能获取public
        Field[] fields = moduleClass.getDeclaredFields();  //public和private
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(BotData.class)) {
                BotData annotationField = field.getAnnotation(BotData.class);
                try {
                    File file = SJFPathTool.getPersistentPath(annotationField.value());
                    if (!file.exists()) {
                        field.set(module, field.getType().getDeclaredConstructor().newInstance());
                        continue;
                    }
                    String json = FileTool.readString(file);
                    if (json == null || json.equals("null")) {
                        field.set(module, field.getType().getDeclaredConstructor().newInstance());
                    } else {
                        field.set(module, JSON.fromJson(json, field.getGenericType()));
                    }
                } catch (Exception e) {
                    ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        return true;
    }

//    public interface ICommonData<T> {
//        T getData();
//
//        void setData(T data);
//    }
}
