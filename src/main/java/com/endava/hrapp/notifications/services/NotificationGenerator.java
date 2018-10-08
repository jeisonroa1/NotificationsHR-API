package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Process;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationGenerator {

    public List<String> generateNotificationsForTodayAgenda(List<Process> processes) {
        List<String> notifications = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Process p : processes) {
            if (!p.getIsClosed()) {
                notifications.add("You have the " + p.getProcessPhase() +
                        " with " + p.getCandidateName() + " for today at " +
                        p.getDueDate().toLocalTime().format(formatter) + "." +
                        " Do not forget to comment and update the process.");
            }
        }
        return notifications;
    }

    public List<String> generateNotificationsForPendingProcesses(List<Process> processes) {
        List<String> notifications = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String message;
        for (Process p : processes) {
            if (!p.getIsClosed()) {
                if(p.getDueDate()==null){
                    message="You added "+p.getCandidateName()+" to the phase '"+p.getProcessPhase()+"'"+
                            " on "+p.getLastUpdate().toLocalDate()+" but didn't add a due date of the process. " +
                            "Please update the due date.";
                }else {
                    message = "You had the " + p.getProcessPhase() +
                            " with " + p.getCandidateName() + " on " +
                            p.getDueDate().toLocalDate() + " at " + p.getDueDate().toLocalTime().format(formatter);

                    if (p.getIsComment()) {
                        message += " and made a comment but didn't update his status. Please update the candidate's status.";
                    } else {
                        message += " and did not comment or update his status." +
                                " Please make a comment and update the candidate's status.";
                    }
                }
                notifications.add(message);
            }
        }
        return notifications;
    }

}
