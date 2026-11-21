package com.queueless.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "token_history")
public class TokenHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "token_id", nullable = false)
    private QueueToken token;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TokenStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by_user_id")
    private User actionBy;

    @Column(length = 255)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    public TokenHistory() {}

    public TokenHistory(QueueToken token, TokenStatus fromStatus, TokenStatus toStatus, User actionBy, String notes) {
        this.token = token;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actionBy = actionBy;
        this.notes = notes;
        this.timestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QueueToken getToken() { return token; }
    public void setToken(QueueToken token) { this.token = token; }

    public TokenStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(TokenStatus fromStatus) { this.fromStatus = fromStatus; }

    public TokenStatus getToStatus() { return toStatus; }
    public void setToStatus(TokenStatus toStatus) { this.toStatus = toStatus; }

    public User getActionBy() { return actionBy; }
    public void setActionBy(User actionBy) { this.actionBy = actionBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
