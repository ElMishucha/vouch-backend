//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.michaelb.vouch.integration.openai.model;

import java.util.List;

public class ChatGPTResponse {
    public List<Choice> choices;

    public ChatGPTResponse() {
    }

    public static class Message {
        public String role;
        public String content;

        public Message() {
        }
    }

    public static class Choice {
        public Message message;

        public Choice() {
        }
    }
}
