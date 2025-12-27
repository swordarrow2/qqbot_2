package com.meng.api.mhy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.meng.tools.normal.*;
import com.meng.tools.sjf.SJFPathTool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public class Bh3Api {

    public static HashMap<String, String> nameTranslate = new HashMap<>();

    static {
        try {
            File file = SJFPathTool.getPersistentPath("bh3_names_translate.json");
            if (!file.exists()) {
                FileTool.saveFile(file, "{}".getBytes(StandardCharsets.UTF_8));
            }
            nameTranslate = JSON.fromJson(FileTool.readString(file), nameTranslate.getClass());
        } catch (IOException e) {
            ExceptionCatcher.getInstance().uncaughtException(e);
        }
    }

    public static HashMap<String, String> getCharacterFromMihoyo() throws IOException {
        HashMap<String, String> returnMap = new HashMap<>();
        File cache = SJFPathTool.getCachePath("bh3_main_cache.html");
        if (!cache.exists()) {
            FileTool.saveFile(cache, Network.httpGetRaw("https://bbs.mihoyo.com/bh3/wiki/channel/map/17/18?bbs_presentation_style=no_header"));
        }
        String html = FileTool.readString(cache);
        if (html == null) {
            return null;
        }
        Elements elements = Jsoup.parse(html).getElementsByClass("collection-avatar__item");
        for (Element element : elements) {
            String link = "https://bbs.mihoyo.com" + element.attr("href");
            Element element2 = element.getElementsByClass("collection-avatar__title").get(0);
            String name = element2.text();
            returnMap.put(name.replaceAll("[\\s\\p{C}]", "").replace(" ", ""), link);
        }
        return returnMap;
    }

    public static Element getDescribe(final String name) throws IOException {

        HashMap<String, String> characters = Bh3Api.getCharacterFromMihoyo();
        if (characters == null) {
            return null;
        }
        ArrayList<String> list = new ArrayList<>(characters.keySet());
        list.removeIf(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !s.contains(name);
            }
        });
        if (list.size() != 1) {
            return null;
        }
        String url = characters.get(list.get(0));
        if (url == null) {
            return null;
        }
        File cache = SJFPathTool.getCachePath("bh3_" + Hash.getMd5Instance().calculate(list.get(0)) + "_cache.html");
        if (!cache.exists()) {
            byte[] content = Network.httpGetRaw(url);
            if (content == null) {
                return null;
            }
            FileTool.saveFile(cache, content);
        }
        String html = FileTool.readString(cache);
        if (html == null) {
            return null;
        }
        return Jsoup.parse(html).getElementsByClass("obc-tmpl--col-l2r1").first();
    }

    public static String downloadAudio() throws IOException {

        int error = 0;
        long time = System.currentTimeMillis();
        int count = 0;

        HashMap<String, String> characters = Bh3Api.getCharacterFromMihoyo();
        assert characters != null;
        for (String name : characters.keySet()) {

            String url = characters.get(name);
            File cache = SJFPathTool.getCachePath("bh3_" + Hash.getMd5Instance().calculate(name) + "_cache.html");
            if (!cache.exists()) {
                FileTool.saveFile(cache, Network.httpGetRaw(url));
            }
            String html = FileTool.readString(cache);
            assert html != null;
            Element element = Jsoup.parse(html).getElementsByClass("obc-tmpl--col-l2r1").last();
            ArrayList<Pojos> pojos = JSON.fromJson(URLDecoder.decode(element.attr("data-data"), "utf-8"), new TypeToken<ArrayList<Pojos>>() {
            }.getType());

            String json = Objects.requireNonNull(getDescribe(name)).attr("data-data");
            JsonArray jsona = new JsonParser().parse(URLDecoder.decode(json, StandardCharsets.UTF_8.toString())).getAsJsonArray();
            String cEngName = null;
            String cCnName = null;
            for (int i = 0; i < jsona.size(); i++) {
                JsonObject jsonElement = jsona.get(i).getAsJsonObject();
                if ("basicIntroduction".equals(jsonElement.get("partKey").getAsString())) {
                    JsonObject characterMainObject = jsonElement.get("data").getAsJsonObject();
                    JsonObject nameObject = characterMainObject.get("mainFields").getAsJsonArray().get(0).getAsJsonObject();
                    cEngName = nameObject.get("valueR").getAsString();
                    cCnName = nameObject.get("valueL").getAsString();
                }
            }
            if (cEngName == null) {
                System.out.println("cEngName is null");
                continue;
            }
            assert pojos != null;
            for (Pojos p : pojos) {
                if (p.partKey.equals("voice")) {
                    for (Pojos.Data.Item item : p.data.items) {
                        nameTranslate.put(cCnName, cEngName);
                        File folder = SJFPathTool.getAudioPath(cEngName.replaceAll("[^a-zA-Z0-9]", ""));
                        File audioFile = new File(folder, item.content.replace("|", "").replace("?", "？").replace("!", "！") + ".wav");
                        if (!audioFile.exists() || audioFile.length() == 0) {
                            try {
                                FileTool.saveFile(audioFile, Network.httpGetRaw(item.audio));
                                count++;
                            } catch (IOException e) {
                                System.out.println("exception:" + cCnName + "  " + cEngName + "  " + audioFile.getName());
                                error++;
                            }
                            System.out.println("finish:" + cCnName + "  " + cEngName + "  " + audioFile.getName());
                        } else {
                            System.out.println("finish:" + cCnName + "  " + cEngName + "  " + audioFile.getName());
                        }
                    }
                }
            }
        }
        File file = SJFPathTool.getPersistentPath("bh3_names_translate.json");
        FileTool.saveFile(file, JSON.toJson(nameTranslate).getBytes(StandardCharsets.UTF_8));
        return String.format("count = %d, error = %d, time = %s", count, error, System.currentTimeMillis() - time);
    }

    public static String getEngName(String cnName) {
        ArrayList<String> list = new ArrayList<>();
        for (String k : nameTranslate.keySet()) {
            if (k.contains(cnName)) {
                list.add(k);
            }
        }
        if (list.size() != 1) {
            return null;
        }
        return nameTranslate.get(list.get(0));
    }

    public static class Pojos {
        public Data data;
        public String partKey;
        public String tmplKey;

        public class Data {
            public List<Item> items;
            @SerializedName("layout_")
            public String layout;

            public class Item {
                public String img;
                @SerializedName("name_")
                public String name;
                public String size;
                public String content;
                public String audio;
            }
        }

        @Override
        public String toString() {
            return "Pojos{" +
                    "data=" + data +
                    ", partKey='" + partKey + '\'' +
                    ", tmplKey='" + tmplKey + '\'' +
                    '}';
        }
    }
}
