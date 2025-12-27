package com.meng.tools.normal;

import com.meng.bot.qq.BotWrapper;
import com.meng.tools.sjf.SJFPathTool;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 司徒灵羽
 */

public class Network {

    public static String httpPost(String url, String cookie, Map<String, String> headers, Object... params) {
        Connection connection = Jsoup.connect(url);
        connection.userAgent(BotWrapper.userAgent);
        if (headers != null) {
            connection.headers(headers);
        }
        if (cookie != null) {
            connection.cookies(cookieToMap(cookie));
        }
        connection.ignoreContentType(true).method(Connection.Method.POST);
        for (int i = 0; i < params.length; i += 2) {
            connection.data(String.valueOf(params[i]), String.valueOf(params[i + 1]));
        }
        Connection.Response response = null;
        try {
            response = connection.execute();
        } catch (IOException e) {
            e.printStackTrace();
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
            return null;
        }
        if (response.statusCode() != 200) {
            return String.valueOf(response.statusCode());
        }
        return response.body();
    }

    public static Map<String, String> cookieToMap(String value) {
        Map<String, String> map = new HashMap<>();
        String[] values = value.split("; ");
        for (String val : values) {
            String[] vals = val.split("=");
            if (vals.length == 2) {
                map.put(vals[0], vals[1]);
            } else if (vals.length == 1) {
                map.put(vals[0], "");
            }
        }
        return map;
    }

    public static String getRealUrl(String surl) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(surl).openConnection();
        conn.setInstanceFollowRedirects(false);
        return conn.getHeaderField("Location");
    }

    public static byte[] httpGetRaw(String url) throws IOException {
        return httpGetRaw(url, null, null);
    }

    public static byte[] httpGetRaw(String url, String cookie) throws IOException {
        return httpGetRaw(url, cookie, null);
    }

    public static byte[] httpGetRaw(String url, String cookie, String refer) throws IOException {
        Connection.Response response = null;
        Connection connection;
        connection = Jsoup.connect(url);
        connection.timeout(60000);
        if (cookie != null) {
            connection.cookies(cookieToMap(cookie));
        }
        if (refer != null) {
            connection.referrer(refer);
        }
//            connection.header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
//            connection.header("Accept-Language","zh-CN,zh;q=0.9");
//            connection.header("Accept-Encoding","gzip, deflate");
//            connection.header("Cache-Control","max-age=0");
//            connection.header("Connection","keep-alive");
//            connection.header("Host","q2.qlogo.cn");
//            connection.header("Upgrade-Insecure-Requests","1");
        connection.userAgent(BotWrapper.userAgent);
        connection.maxBodySize(1024 * 1024 * 10).ignoreContentType(true).method(Connection.Method.GET);
        response = connection.execute();
        if (response.statusCode() != 200) {
            System.out.println(response.statusCode());
        }
        return response.bodyAsBytes();
    }


    public static String httpGet(String url) throws IOException {
        return httpGet(url, null, null);
    }

    public static String httpGet(String url, String cookie) throws IOException {
        return httpGet(url, cookie, null);
    }

    public static String httpGet(String url, String cookie, String refer) throws IOException {
        Connection.Response response = null;
        Connection connection = Jsoup.connect(url);
        if (cookie != null) {
            connection.cookies(cookieToMap(cookie));
        }
        if (refer != null) {
            connection.referrer(refer);
        }
        connection.userAgent(BotWrapper.userAgent);
        connection.maxBodySize(1024 * 1024 * 10).ignoreContentType(true).method(Connection.Method.GET);
        response = connection.execute();
        if (response.statusCode() != 200) {
            System.out.println(String.valueOf(response.statusCode()));
        }
        return response.body();
    }

    public static void downloadImage(String url) {
        try {
            byte[] fileBytes = Network.httpGetRaw(url);
            File file = SJFPathTool.getFlashImagePath(Hash.getMd5Instance().calculate(fileBytes) + "." + FileFormat.getFileType(fileBytes));
            FileTool.saveFile(file, fileBytes);
        } catch (Exception e) {
            ExceptionCatcher.getInstance().uncaughtException(Thread.currentThread(), e);
        }
    }
}
