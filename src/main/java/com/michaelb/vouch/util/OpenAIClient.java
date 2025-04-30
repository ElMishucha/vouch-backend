package com.michaelb.vouch.util;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OpenAIClient {
    private final OkHttpClient client;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAIClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String sendRequest(String payload) {
        RequestBody body = RequestBody.create(payload, MediaType.get("application/json; charset=utf-8"));

        String apiUrl = "https://api.openai.com/v1/responses";
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return extractText(responseBody);
            } else {
                System.err.println("Request failed: " + response.code() + " - " + response.body().string());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    String extractText(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        for (JsonElement element : root.getAsJsonArray("output")) {
            JsonObject object = element.getAsJsonObject();
            String type = object.get("type").getAsString();

            if (type.equals("message")) {
                for (JsonElement chunk : object.getAsJsonArray("content")) {
                    JsonObject chunkObject = chunk.getAsJsonObject();
                    if (chunkObject.get("type").getAsString().equals("output_text")) {
                        return chunkObject.get("text").getAsString();
                    }
                }
            }
        }

        return null;
    }
}