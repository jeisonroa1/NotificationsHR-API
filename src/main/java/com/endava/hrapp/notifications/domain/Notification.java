package com.endava.hrapp.notifications.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Accessors(chain = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "body")
    private String body;

    @Column(name = "notification_date")
    private LocalDateTime notificationDate;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "recruiter_username")
    private String recruiterUsername;

    @OneToOne
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;
}
