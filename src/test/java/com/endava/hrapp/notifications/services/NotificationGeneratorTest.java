package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Process;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class NotificationGeneratorTest {
    private NotificationGenerator notificationGenerator = new NotificationGenerator();
    private JSONProcessTest jsonProcessTest=new JSONProcessTest();

    @Test
    public void testGenerateNotificationsForTodayAgenda() throws Exception {
        Process p=jsonProcessTest.getFakeProcess();
        String notification="You have the Job Offer with Carlos Rodriguez for today at 09:00. " +
                "Do not forget to comment and update the process.";
        List<String> result = notificationGenerator.generateNotificationsForTodayAgenda(Collections.singletonList(p));
        Assert.assertEquals(notification, result.get(0));
    }

    @Test
    public void testGenerateNotificationsForPendingProcesses() throws Exception {
        Process p=jsonProcessTest.getFakeProcess();
        String notification="You had the Job Offer with Carlos Rodriguez on 2100-09-13 at 09:00 and did not comment or update his status." +
                " Please make a comment and update the candidate's status.";
        List<String> result = notificationGenerator.generateNotificationsForPendingProcesses(Collections.singletonList(p));
        Assert.assertEquals(notification, result.get(0));

        p.setIsComment(true);
        notification="You had the Job Offer with Carlos Rodriguez on 2100-09-13 at 09:00 and made a comment but didn't update his status. Please update the candidate's status.";
        result = notificationGenerator.generateNotificationsForPendingProcesses(Collections.singletonList(p));
        Assert.assertEquals(notification, result.get(0));

        p.setDueDate(null);
        notification="You added Carlos Rodriguez to the phase 'Job Offer' on 2018-10-02 but didn't add a due date of the process. " +
                "Please update the due date.";
        result = notificationGenerator.generateNotificationsForPendingProcesses(Collections.singletonList(p));
        Assert.assertEquals(notification, result.get(0));
    }
}