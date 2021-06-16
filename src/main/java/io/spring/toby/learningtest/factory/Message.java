package io.spring.toby.learningtest.factory;

public class Message {
    private String text;

    // 외부에서 직접 객체 생성 제약
    private Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    // static method를 통한 객체 생성만 허용
    public static Message newMessage(String text) {
        return new Message(text);
    }
}
