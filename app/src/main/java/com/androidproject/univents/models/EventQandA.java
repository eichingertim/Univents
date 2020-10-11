package com.androidproject.univents.models;

public class EventQandA {

    private String question;
    private String answer;
    private String eventQandAId;
    private String userQuestionedId;

    public EventQandA() {

    }

    public EventQandA(String question, String answer, String eventQandAId, String userQuestionedId) {
        this.question = question;
        this.answer = answer;
        this.eventQandAId = eventQandAId;
        this.userQuestionedId = userQuestionedId;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getEventQandAId() {
        return eventQandAId;
    }

    public String getUserQuestionedId() {
        return userQuestionedId;
    }

    public void setEventQandAId(String eventQandAId) {
        this.eventQandAId = eventQandAId;
    }
}
