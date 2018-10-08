package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.controllers.NotificationController;
import com.endava.hrapp.notifications.domain.Notification;
import com.endava.hrapp.notifications.domain.Process;
import com.endava.hrapp.notifications.repositories.NotificationRepository;
import com.endava.hrapp.notifications.repositories.ProcessRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

public class DatabaseServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private NotificationController notificationController;
    @Mock
    private JSONProcess jsonProcess;
    @InjectMocks
    private DatabaseService databaseService;




    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetNotificationsForUserHappyPath() throws Exception {
        List<Notification> list = new ArrayList<>();
        Notification notification = new Notification();
        notification.setId(1);
        notification.setBody("The body");
        notification.setIsRead(true);
        notification.setNotificationDate(LocalDateTime.parse("9999-12-11T23:59:59"));
        notification.setRecruiterUsername("gperez");
        notification.setProcess(new Process().setId(1));
        list.add(notification);

        List<JsonNode> result;
        when(notificationRepository.getNotificationsByUsername("gperez")).thenReturn(Optional.of(list));
        result = databaseService.getNotificationsForUser("gperez", false);
        Assert.assertEquals(getFakeArrayNotification(), result);
    }

    @Test
    public void testUpdateProcess() throws Exception{
        when(processRepository.findById(anyInt())).thenReturn( Optional.of(new Process(){{
            setIsClosed(false);
            setProcessPhase("HR Interview");
            setIsComment(false);
            setLastUpdate(LocalDateTime.parse("2018-09-25T18:30:00"));
            setDueDate(LocalDateTime.parse("2018-09-25T18:30:00"));
            setCandidateName("Lina Maria");

        }}));

        when(jsonProcess.updateProcess(any(Process.class), any(JsonNode.class))).thenReturn(new Process());

        when(processRepository.save(any(Process.class))).thenReturn(new Process(){{
            setIsClosed(false);
            setProcessPhase("HR Interview");
            setIsComment(false);
            setLastUpdate(LocalDateTime.parse("2018-09-25T18:30:00"));
            setDueDate(LocalDateTime.parse("2018-09-25T18:30:00"));
            setCandidateName("Jose Maria");
        }});
        Optional<Boolean> result = databaseService.updateProcess(eq(1),getFakeJson());
        Assert.assertEquals(true, result.get());
    }

    @Test
    public void testUpdateProcessNotFound() throws Exception{
        when(processRepository.findById(anyInt())).thenReturn( Optional.empty());
        Optional<Boolean> result = databaseService.updateProcess(eq(1),getFakeJson());
        Assert.assertEquals(false, result.get());
    }

    @Test
    public void testUpdateTicketPropertiesExpectedTrue() throws Exception{
        when(processRepository.findAllByTicketId(anyInt())).thenReturn(Optional.of(
                Arrays.asList(
                        new Process(){{
                            setIsClosed(false);
                        }}
                        )
                )
        );
        when(processRepository.saveAll(anyIterable())).thenReturn(anyIterable());
        Optional<Boolean> result = databaseService.updateTicketProperties(1, true);
        Assert.assertEquals(true, result.get());

    }

    @Test
    public void testUpdateTicketPropertiesExpectedFalse() throws Exception{
        when(processRepository.findAllByTicketId(anyInt())).thenReturn(Optional.empty() );
        when(processRepository.saveAll(anyIterable())).thenReturn(anyIterable());
        Optional<Boolean> result = databaseService.updateTicketProperties(1, true);
        Assert.assertEquals(false, result.get());

    }
    private JsonNode getFakeJson() throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        URL resource=getClass().getClassLoader().getResource("jsonTestProcessPut.json");
        return mapper.readTree(resource);
    }

    private List<JsonNode> getFakeArrayNotification() throws IOException {
        String json="{\"id\":1,\"candidate_id\":1,\"body\":\"The body\",\"notification_date\":\"9999-12-11T23:59:59\",\"is_read\":true,\"recruiter_username\":\"gperez\"}";
        ObjectMapper mapper=new ObjectMapper();
        return Collections.singletonList(mapper.readTree(json));
    }

}