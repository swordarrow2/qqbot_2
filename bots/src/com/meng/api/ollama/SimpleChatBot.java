package com.meng.api.ollama;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.*;

public class SimpleChatBot {
    private static final String ENDPOINT = "http://localhost:11434/api/chat";
    private final String model;
    private final List<Map<String, String>> conversationHistory;
    private final long conversationId;

    public SimpleChatBot(String model, long conversationId) {
        this.model = model;
        this.conversationHistory = new ArrayList<>();
        this.conversationId = conversationId;
        // 添加系统提示
        addSystemMessage("你正在扮演一个科普助手，不要长篇大论但也不需要刻意压缩字数，只需要解释用户提问的东西，不需要额外的知识扩展");
    }

    public SimpleChatBot(String model, String systemPrompt, long conversationId) {
        this(model, conversationId);
        addSystemMessage(systemPrompt);
    }

    private void addSystemMessage(String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", "system");
        message.put("content", content);
        conversationHistory.add(message);
    }

    public String applyMessage(String userMessage) throws IOException {
        // 添加用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        conversationHistory.add(userMsg);

        // 准备请求
        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        for (Map<String, String> msg : conversationHistory) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.get("role"));
            msgObj.addProperty("content", msg.get("content"));
            messages.add(msgObj);
        }
        request.add("messages", messages);

        // 发送请求
        Connection.Response response = Jsoup.connect(ENDPOINT)
                .header("Content-Type", "application/json")
                .ignoreContentType(true)
                .requestBody(request.toString())
                .method(Connection.Method.POST)
                .timeout(180000)
                .execute();

        if (response.statusCode() != 200) {
            throw new IOException("HTTP错误: " + response.statusCode());
        }

        // 解析响应
        JsonObject jsonResponse = new JsonObject();
        jsonResponse = new com.google.gson.Gson().fromJson(response.body(), JsonObject.class);

        String assistantMessage = jsonResponse.getAsJsonObject("message").get("content").getAsString();

        // 添加助手回复到历史
        Map<String, String> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", assistantMessage);
        conversationHistory.add(assistantMsg);

        return assistantMessage;
    }

    public void clearHistory() {
        // 保留系统消息
        List<Map<String, String>> systemMessages = new ArrayList<>();
        for (Map<String, String> msg : conversationHistory) {
            if ("system".equals(msg.get("role"))) {
                systemMessages.add(msg);
            }
        }
        conversationHistory.clear();
        conversationHistory.addAll(systemMessages);
    }

    public List<Map<String, String>> getHistory() {
        return new ArrayList<>(conversationHistory);
    }

    public long getConversationId() {
        return conversationId;
    }

}