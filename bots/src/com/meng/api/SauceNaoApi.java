package com.meng.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import com.meng.bot.qq.BotWrapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

public class SauceNaoApi {

    public static SauceNaoResult getSauce(int database, String url) throws IOException {
        Connection.Response response = Jsoup.connect("https://saucenao.com/search.php?db=" + database).timeout(60000).data("url", url).method(Connection.Method.POST).execute();
        if (response.statusCode() != 200) {
            return null;
        }
        return new SauceNaoResult(Jsoup.parse(response.body()));
    }

    public static SauceNaoResult getSauce(int database, InputStream img) throws IOException {
        Connection.Response response = Jsoup.connect("https://saucenao.com/search.php")
                .userAgent(BotWrapper.userAgent)
                .timeout(60000)
                .header("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("accept-encoding","gzip, deflate, br")
                .header("accept-language","zh-CN,zh;q=0.9")
                .header("cache-control","max-age=0")
                .header("origin","https://saucenao.com")
                .header("referer","https://saucenao.com")
                .header("sec-fetch-dest","document")
                .header("sec-fetch-mode","navigate")
                .header("sec-fetch-site","same-origin")
                .header("sec-fetch-user","?1")
                .header("upgrade-insecure-requests","1")
                .header("Cookie","_ga=GA1.2.1741009638.1661679234; _gid=GA1.2.2017996702.1661679234; _pbjs_userid_consent_data=3524755945110770; _pubcid=2e237af0-d311-49c8-a92b-619f499e2be9; __gads=ID=3f0c9eea9842d324-22632e420ad600a4:T=1661679376:S=ALNI_Mby6SYgY7jl6W2Rn9io6Bmlkp-nBA; __gpi=UID=0000092b1b7c9cac:T=1661679376:RT=1661679376:S=ALNI_MaFruq22UT0k2MBYTq3-WkBiNLfFA; _im_vid=01GBHWW4DH43CZVNW6TX35HDFB; cto_bundle=lfaHfl9FbEN5NzcxR1d3U1E3dmFVR0REd2dSZlZMQ1V4MiUyRmtnZVhPYWw0WWN0S09GYUoxb2xIMVNaQ2hJTUtzTG9JZVd1N3lEUVVrbGozS2dRM1c1WXVlTlV1JTJGY3J2JTJCaVdNdVFWVkElMkJldVpzY0clMkZyN3R2Y1lkdzdQcHE0TjVQR2htMGU; _gat=1")
                .data("url","Paste Image URL")
                .data("file", "image.png", img)
                .method(Connection.Method.POST)
                .execute();
        if (response.statusCode() != 200) {
            return null;
        }
        return new SauceNaoResult(Jsoup.parse(response.body()));
    }

    public static SauceNaoResult getSauce(int database, File img) throws IOException {
        return getSauce(database, new FileInputStream(img));
    }

    public static SauceNaoResult getSauce(int database, byte[] img) throws IOException {
        return getSauce(database, new ByteArrayInputStream(img));
    }

    public static SauceNaoResult getSauce(File img) throws IOException {
        return getSauce(999, new FileInputStream(img));
    }

    public static SauceNaoResult getSauce(String url) throws IOException {
        return getSauce(999, url);
    }

    public static SauceNaoResult getSauce(byte[] img) throws IOException {
        return getSauce(999, img);
    }

    public static SauceNaoResult getSauce(InputStream img) throws IOException {
        return getSauce(999, img);
    }

    public static class SauceNaoResult {

        private final String CLASS_RESULT_CONTENT_COLUMN = "resultcontentcolumn";
        private final String CLASS_RESULT_IMAGE = "resultimage";
        private final String CLASS_RESULT_MATCH_INFO = "resultmatchinfo";
        private final String CLASS_RESULT_SIMILARITY_INFO = "resultsimilarityinfo";
        private final String CLASS_RESULT_TABLE = "resulttable";
        private final String CLASS_RESULT_TITLE = "resulttitle";
        private final String URL_LOOKUP_SUBSTRING = "https://saucenao.com/info.php?lookup_type=";

        private ArrayList<Result> mResults = new ArrayList<>();

        public SauceNaoResult(Document document) {
            for (Element result : document.getElementsByClass(CLASS_RESULT_TABLE)) {
                Element resultImage = result.getElementsByClass(CLASS_RESULT_IMAGE).first();
                Element resultMatchInfo = result.getElementsByClass(CLASS_RESULT_MATCH_INFO).first();
                Element resultTitle = result.getElementsByClass(CLASS_RESULT_TITLE).first();
                Elements resultContentColumns = result.getElementsByClass(CLASS_RESULT_CONTENT_COLUMN);
                Result newResult = new Result();
                newResult.loadSimilarityInfo(resultMatchInfo);
                newResult.loadThumbnail(resultImage);
                newResult.loadTitle(resultTitle);
                newResult.loadExtUrls(resultMatchInfo, resultContentColumns);
                newResult.loadColumns(resultContentColumns);
                mResults.add(newResult);
            }
        }

        public ArrayList<Result> getResults() {
            return mResults;
        }

        public class Result {
            public String mSimilarity;
            public String mThumbnail;
            public String mTitle;
            public ArrayList<String> mExtUrls = new ArrayList<>();
            public ArrayList<String> mColumns = new ArrayList<>();

            private void loadSimilarityInfo(Element resultMatchInfo) {
                try {
                    mSimilarity = resultMatchInfo.getElementsByClass(CLASS_RESULT_SIMILARITY_INFO).first().text();
                } catch (NullPointerException e) {
                    System.out.println("Unable to load similarity info");
                }
            }

            private void loadThumbnail(Element resultImage) {
                try {
                    Element img = resultImage.getElementsByTag("img").first();

                    if (img.hasAttr("data-src")) {
                        mThumbnail = img.attr("data-src");
                    } else if (img.hasAttr("src")) {
                        mThumbnail = img.attr("src");
                    }
                } catch (NullPointerException e) {
                    System.out.println("Unable to load thumbnail");
                }
            }

            private void loadTitle(Element resultTitle) {
                try {
                    mTitle = new HtmlToPlainText().getPlainText(resultTitle);
                } catch (NullPointerException e) {
                    System.out.println("Unable to load title");
                }
            }

            private void loadExtUrls(Element resultMatchInfo, Elements resultContentColumns) {
                try {
                    for (Element a : resultMatchInfo.getElementsByTag("a")) {
                        String href = a.attr("href");

                        if (!href.isEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                            mExtUrls.add(href);
                        }
                    }

                    for (Element resultContentColumn : resultContentColumns) {
                        for (Element a : resultContentColumn.getElementsByTag("a")) {
                            String href = a.attr("href");
                            if (!href.isEmpty() && !href.startsWith(URL_LOOKUP_SUBSTRING)) {
                                mExtUrls.add(href);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println("Unable to load external URLs");
                }
                Collections.sort(mExtUrls);
            }

            private void loadColumns(Elements resultContentColumns) {
                try {
                    for (Element resultContentColumn : resultContentColumns) {
                        mColumns.add(new HtmlToPlainText().getPlainText(resultContentColumn));
                    }
                } catch (NullPointerException e) {
                    System.out.println("Unable to load columns");
                }
            }
        }

        private class HtmlToPlainText {

            public String getPlainText(Element element) {
                FormattingVisitor formatter = new FormattingVisitor();
                NodeTraversor.traverse(formatter, element);

                return formatter.toString().trim();
            }

            private class FormattingVisitor implements NodeVisitor {
                private static final int mMaxWidth = 80;
                private int mWidth = 0;
                private StringBuilder mAccum = new StringBuilder();

                @Override
                public void head(Node node, int depth) {
                    String name = node.nodeName();
                    if (node instanceof TextNode) {
                        append(((TextNode) node).text());
                    } else if (name.equals("li")) {
                        append("\n * ");
                    } else if (name.equals("dt")) {
                        append("  ");
                    } else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr")) {
                        append("\n");
                    } else if (name.equals("strong")) {
                        append(" ");
                    }
                }

                // Hit when all of the node's children (if any) have been visited
                @Override
                public void tail(Node node, int depth) {
                    String name = node.nodeName();
                    if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) {
                        append("\n");
                    }
                }

                // Appends text to the string builder with a simple word wrap method
                private void append(String text) {
                    // Reset com.meng.counter if starts with a newline. only from formats above,
                    // not in natural text
                    if (text.startsWith("\n")) {
                        mWidth = 0;
                    }

                    // Don't accumulate long runs of empty spaces
                    if (text.equals(" ")
                            && (mAccum.length() == 0 || StringUtil.in(mAccum.substring(mAccum.length() - 1), " ", "\n"))) {
                        return;
                    }

                    // Won't fit, needs to wrap
                    if (text.length() + mWidth > mMaxWidth) {
                        String[] words = text.split("\\s+");

                        for (int i = 0; i < words.length; i++) {
                            String word = words[i];
                            boolean last = i == words.length - 1;
                            // Insert a space if not the last word
                            if (!last) {
                                word += " ";
                            }
                            // Wrap and reset com.meng.counter
                            if (word.length() + mWidth > mMaxWidth) {
                                mAccum.append("\n").append(word);
                                mWidth = word.length();
                            } else {
                                mAccum.append(word);
                                mWidth += word.length();
                            }
                        }
                    } else {
                        // Fits as is, without need to wrap text
                        mAccum.append(text);
                        mWidth += text.length();
                    }
                }

                @Override
                public String toString() {
                    return mAccum.toString();
                }
            }
        }
    }
}
