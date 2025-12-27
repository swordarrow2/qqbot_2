package com.meng.tools.normal;

import java.util.HashMap;
import java.util.Map;

public class Tools {

	public static Map<String, String> liveHead = new HashMap<>();
    public static Map<String, String> mainHead = new HashMap<>();

	public static final String DEFAULT_ENCODING = "UTF-8";

	static{
		liveHead.put("Host", "api.live.bilibili.com");
        liveHead.put("Accept", "application/json, text/javascript, */*; q = 0.01");
        liveHead.put("Content-Type", "application/x-www-form-urlencoded; charset = UTF-8");
        liveHead.put("Connection", "keep-alive");
        liveHead.put("Origin", "https://live.bilibili.com");

        mainHead.put("Host", "api.bilibili.com");
        mainHead.put("Accept", "application/json, text/javascript, */*; q = 0.01");
        mainHead.put("Content-Type", "application/x-www-form-urlencoded; charset = UTF-8");
        mainHead.put("Connection", "keep-alive");
        mainHead.put("Origin", "https://www.bilibili.com");
	}

	public static class ArrayTool {

		public static byte[] mergeArray(byte[]... arrays) {
			int allLen = 0;
			for (byte[] bs:arrays) {
				allLen += bs.length;
            }
            byte[] finalArray = new byte[allLen];
            int flag = 0;
            for (byte[] byteArray:arrays) {
                for (int i = 0;i < byteArray.length;++flag,++i) {
                    finalArray[flag] = byteArray[i];
                }
            }
            return finalArray;
        }
	}
}
