package com.endava.hrapp.notifications.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processes")
@Data
@Accessors(chain = true)
public class Process {

    @Id
    private int id;
    @Column(name = "candidate_name")
    private String candidateName;
    @Column(name = "process_phase")
    private String processPhase;
    @Column(name = "is_comment")
    private Boolean isComment;
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    @Column(name = "ticket_id")
    private int ticketId;
    @Column(name = "is_closed")
    private Boolean isClosed;
}
