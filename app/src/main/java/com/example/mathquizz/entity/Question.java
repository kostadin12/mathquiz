package com.example.mathquizz.entity;

public class Question {
    private String difficulty;
    private String question;
    private String answer;

    public Question() {
    }

    public Question(String difficulty, String question, String answer) {
        this.difficulty = difficulty;
        this.question = question;
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
