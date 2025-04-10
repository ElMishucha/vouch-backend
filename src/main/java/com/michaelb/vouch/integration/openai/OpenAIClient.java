package com.michaelb.vouch.integration.openai;

import com.google.gson.Gson;
import com.michaelb.vouch.integration.openai.model.ChatGPTResponse;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OpenAIClient {
    private final OkHttpClient client;
    private final Gson gson;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAIClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public String sendRequest(Object payload) {
        String json = gson.toJson(payload);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        String apiUrl = "https://api.openai.com/v1/chat/completions";
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                ChatGPTResponse chatGPTResponse = gson.fromJson(response.body().string(), ChatGPTResponse.class);
                return chatGPTResponse.choices.get(0).message.content;
            } else {
                System.err.println("Request failed: " + response.code() + " - " + response.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}