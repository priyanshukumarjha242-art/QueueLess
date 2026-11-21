package com.queueless.dto;

public class TokenActionRequest {
    private String notes;

    public TokenActionRequest() {}
    public TokenActionRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
