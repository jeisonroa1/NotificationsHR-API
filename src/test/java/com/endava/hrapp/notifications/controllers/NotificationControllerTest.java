package com.endava.hrapp.notifications.controllers;

import com.endava.hrapp.notifications.domain.Notification;
import com.endava.hrapp.notifications.domain.Process;
import com.endava.hrapp.notifications.repositories.NotificationRepository;
import com.endava.hrapp.notifications.repositories.ProcessRepository;
import com.endava.hrapp.notifications.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

public class NotificationControllerTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private DatabaseService databaseService;
    @Mock
    private OperationValidator validator;
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private AuthenticationAndUserCommunication authenticationAndUserCommunication;
    @Mock
    private JSONProcess jsonProcess;
    @InjectMocks
    private NotificationController notificationController;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mapper = new ObjectMapper();
        notificationController = new NotificationController(notificationRepository, processRepository,
                databaseService, validator, mapper,
                jsonProcess, authenticationAndUserCommunication);
        ReflectionTestUtils.setField(notificationController, "validator", validator);
        ReflectionTestUtils.setField(notificationController, "databaseService", databaseService);
        ReflectionTestUtils.setField(notificationController, "authenticationAndUserCommunication", authenticationAndUserCommunication);
    }

    @Test
    public void testPOSTMethodExpectingCode401() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString()))
                .thenReturn(new HashMap<String,String>(){{put("username","");}});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(false));
        when(validator.validateProcessJSON(any(JsonNode.class), any(HttpMethod.class),anyString(),anyInt()))
                .thenReturn(new HashMap<>());
        when(validator.validateTicketId(anyInt(), anyString())).thenReturn(new HashMap<>());
        when(validator.validateNotExistingCandidate(anyInt())).thenReturn(new HashMap<>());
        ResponseEntity result =
                notificationController.insertProcess(1, getFakeJson("jsonTestProcessCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(401, result.getStatusCodeValue());
    }

    @Test
    public void testPOSTMethodExpectingCode400() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString()))
                .thenReturn(new HashMap<String,String>(){{put("username","");}});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(any(JsonNode.class), any(HttpMethod.class),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateTicketId(anyInt(), anyString())).thenReturn(new HashMap<>());
        when(validator.validateNotExistingCandidate(anyInt())).thenReturn(new HashMap<>());
        ResponseEntity result =
                notificationController.insertProcess(1, getFakeJson("jsonTestProcessCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(400, result.getStatusCodeValue());
    }

    @Test
    public void testPOSTMethodExpectingCode404() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString()))
                .thenReturn(new HashMap<String,String>(){{put("username","");}});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(any(JsonNode.class), any(HttpMethod.class),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateNotExistingCandidate(anyInt())).thenReturn(new HashMap<>());
        ResponseEntity result =
                notificationController.insertProcess(1, getFakeJson("jsonTestProcessCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testPOSTMethodExpectingCode409() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString()))
                .thenReturn(new HashMap<String,String>(){{put("username","");}});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(any(JsonNode.class), any(HttpMethod.class),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateNotExistingCandidate(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        ResponseEntity result =
                notificationController.insertProcess(1, getFakeJson("jsonTestProcessCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(409, result.getStatusCodeValue());
    }

    @Test
    public void testPOSTMethodExpectingCode201() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString()))
                .thenReturn(new HashMap<String,String>(){{put("username","");}});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(any(JsonNode.class), any(HttpMethod.class),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateNotExistingCandidate(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(jsonProcess.getProcessFromJSON(anyInt(), any(JsonNode.class))).thenReturn(new Process());
        when(jsonProcess.getJSONFromProcess(any(Process.class)))
                .thenReturn(getFakeJson("jsonTestProcessCorrect.json"));
        when(processRepository.save(any(Process.class))).thenReturn(new Process());
        ResponseEntity result =
                notificationController.insertProcess(1, getFakeJson("jsonTestProcessCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(201, result.getStatusCodeValue());
        Assert.assertEquals(getFakeJson("jsonTestProcessCorrect.json"), result.getBody());
    }

    @Test
    public void testGETMethodForProcessExpectingCode401() throws ServiceException, IOException {
        when(validator.validateToken(anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt())).thenReturn(new HashMap<>());
        ResponseEntity result = notificationController.getProcesses(1, 1, "fake Token");
        Assert.assertNotNull(result);
        Assert.assertEquals(401, result.getStatusCodeValue());
    }

    @Test
    public void testGETMethodForProcessExpectingCode404() throws ServiceException, IOException {
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt())).thenReturn(new HashMap<>());
        ResponseEntity result = notificationController.getProcesses(1, 1, "fake Token");
        Assert.assertNotNull(result);
        Assert.assertEquals(404, result.getStatusCodeValue());

        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        result = notificationController.getProcesses(1, 1, "fake Token");
        Assert.assertNotNull(result);
        Assert.assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testGETMethodForProcessExpectingCode200() throws ServiceException, IOException {
        Process p = getFakeProcess();
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(processRepository.findById(anyInt())).thenReturn(Optional.of(new Process()));
        when(jsonProcess.getJSONFromProcess(any(Process.class))).thenReturn(getFakeJson("jsonTestProcessCorrect.json"));
        ResponseEntity result = notificationController.getProcesses(1, 1, "fake Token");
        Assert.assertNotNull(result);
        Assert.assertEquals(200, result.getStatusCodeValue());
        Assert.assertEquals(getFakeJson("jsonTestProcessCorrect.json"), result.getBody());
    }

    @Test
    public void testGETMethodExpectingCode500() throws ServiceException, IOException {
        Exception e = new RuntimeException("");
        when(validator.validateToken(anyString())).thenThrow(new ServiceException("", e.getCause()));
        ResponseEntity result = notificationController.getProcesses(1, 1, "fake Token");
        Assert.assertNotNull(result);
        Assert.assertEquals(500, result.getStatusCodeValue());
    }

    @Test
    public void testGETProcessByCandidateName() throws IOException, ServiceException {
        ResponseEntity result = notificationController.getProcessByCandidateName(1, "", "");
        Assert.assertEquals(400, result.getStatusCodeValue());

        when(validator.validateName(anyString())).thenReturn(true);
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", false);
        }});
        result = notificationController.getProcessByCandidateName(1, "", "");
        Assert.assertEquals(401, result.getStatusCodeValue());

        when(validator.validateName(anyString())).thenReturn(true);
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        result = notificationController.getProcessByCandidateName(1, "", "");
        Assert.assertEquals(404, result.getStatusCodeValue());

        when(validator.validateName(anyString())).thenReturn(true);
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingProcessByCandidateName(anyString(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        result = notificationController.getProcessByCandidateName(1, "", "");
        Assert.assertEquals(404, result.getStatusCodeValue());

        when(validator.validateName(anyString())).thenReturn(true);
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingProcessByCandidateName(anyString(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(processRepository.selectProcessByCandidateName(anyString(), anyInt()))
                .thenReturn(Arrays.asList(new Process(), new Process()));
        when(jsonProcess.getJSONFromProcess(any())).thenReturn(getFakeJson("jsonTestProcessCorrect.json"));
        result = notificationController.getProcessByCandidateName(1, "", "");
        Assert.assertEquals(200, result.getStatusCodeValue());
        Assert.assertEquals(mapper.readTree("{\"processes\":" + Arrays.asList(getFakeJson("jsonTestProcessCorrect.json"),
                getFakeJson("jsonTestProcessCorrect.json"))+"}"), result.getBody());

        Exception e = new RuntimeException("");
        when(validator.validateName(anyString())).thenReturn(true);
        when(validator.validateToken(anyString())).thenThrow(new ServiceException("", e.getCause()));
        result = notificationController.getProcessByCandidateName(1, "", "");
        Assert.assertEquals(500, result.getStatusCodeValue());

    }

    private JsonNode getFakeJson(String fakeJson) throws IOException {
        URL resource = getClass().getClassLoader().getResource(fakeJson);
        return mapper.readTree(resource);
    }

    private Process getFakeProcess() {
        Process p = new Process();
        p.setId(1);
        p.setCandidateName("name");
        p.setProcessPhase("phase");
        p.setIsComment(false);
        p.setLastUpdate(LocalDateTime.now());
        p.setDueDate(LocalDateTime.of(2100,9,13,9,0,0));
        p.setTicketId(1);
        p.setIsClosed(false);
        return p;
    }

    @Test
    public void testGetNotificationsHappyPath() throws Exception {

        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(databaseService.getNotificationsForUser(anyString(), eq(false))).thenReturn(getFakeNotifications());
        ResponseEntity result = notificationController.getNotifications("", false);
        Assert.assertEquals(ResponseEntity.status(200)
                .body(mapper.readTree("{\"notifications\":" + getFakeNotifications() + "}")), result);
    }

    @Test
    public void testGetNotificationsBadToken() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<>());
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(false));
        ResponseEntity result = notificationController.getNotifications("token", false);
        System.out.println(result);
        Assert.assertEquals(ResponseEntity.status(401)
                .body(mapper.readTree("{\"Message\":\"Authentication Failure\"}")), result);

    }

    @Test
    public void testPUTMethodExpectingCode401() throws Exception {
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", false);
        }});
        when(validator.validateReadStatusJson(any(JsonNode.class))).thenReturn(new HashMap<>());
        when(validator.validateExistingNotification(anyInt())).thenReturn(new HashMap<>());
        ResponseEntity result =
                notificationController.updateNotificationReadingStatus(1, getFakeJson("jsonTestReadStatusCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(401, result.getStatusCodeValue());
    }

    @Test
    public void testPUTProcessMethodExpectingCode500() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<>());
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(any(JsonNode.class), any(HttpMethod.class),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{ put("", true); }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{ put("", true); }});
        when(validator.validateNotExistingCandidate(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{ put("", true); }});
        when(databaseService.updateProcess(anyInt(),any())).thenReturn(Optional.of(false));
        ResponseEntity result =
                notificationController.updateProcess(1, 1, getFakeJson("jsonTestProcessCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(500, result.getStatusCodeValue());
    }

    @Test
    public void testPUTMethodExpectingCode400() throws Exception {
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateExistingNotification(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateReadStatusJson(any(JsonNode.class)))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        ResponseEntity result =
                notificationController.updateNotificationReadingStatus(1, getFakeJson("jsonTestReadStatusCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(400, result.getStatusCodeValue());
    }

    @Test
    public void testPUTMethodExpectingCode404() throws Exception {
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateExistingNotification(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateReadStatusJson(any(JsonNode.class))).thenReturn(new HashMap<>());
        ResponseEntity result =
                notificationController.updateNotificationReadingStatus(1, getFakeJson("jsonTestReadStatusCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testPUTMethodExpectingCode204() throws Exception {
        when(validator.validateToken(anyString())).thenReturn(new HashMap<String, Boolean>() {{
            put("", true);
        }});
        when(validator.validateReadStatusJson(any(JsonNode.class)))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingNotification(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(notificationRepository.findById(anyInt())).thenReturn(getFakeNotification());
        ResponseEntity result =
                notificationController.updateNotificationReadingStatus(1, getFakeJson("jsonTestReadStatusCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(204, result.getStatusCodeValue());
    }

    @Test
    public void testPUTMethodExpectingCode500() throws Exception {
        when(validator.validateToken(anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateReadStatusJson(any(JsonNode.class)))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingNotification(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(notificationRepository.findById(anyInt())).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateNotificationReadingStatus(1, getFakeJson("jsonTestReadStatusCorrect.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(500, result.getStatusCodeValue());
    }

    @Test
    public void testPUTMethodExpectingCode500AfterException() throws Exception {
        Exception e = new RuntimeException("");
        when(validator.validateToken(anyString())).thenThrow(new ServiceException("Fake Exception", e.getCause()));
        when(validator.validateReadStatusJson(any(JsonNode.class)))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingNotification(anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(notificationRepository.findById(anyInt())).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateNotificationReadingStatus(1, getFakeJson("jsonTestReadStatusCorrect.json"), "");
        Assert.assertNotNull(result);
        System.out.println(result);
        Assert.assertEquals(500, result.getStatusCodeValue());
    }

    public Optional<Notification> getFakeNotification() {
        Notification n = new Notification();
        n.setIsRead(true);
        n.setBody("Body");
        n.setId(1);
        n.setRecruiterUsername("Hernando");
        n.setProcess(new Process());
        n.setNotificationDate(LocalDateTime.now());
        return Optional.of(n);
    }


    @Test
    public void testPUTProcessMethodExpectingCode400() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(eq(getFakeJson("jsonTestProcessPut.json")), eq(HttpMethod.PUT),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateTicketId(anyInt(), anyString())).thenReturn(new HashMap<>());
        when(validator.validateExistingProcess(anyInt(), anyInt())).thenReturn(new HashMap<>());
        when(databaseService.updateProcess(anyInt(), any(JsonNode.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateProcess(1, 1, getFakeJson("jsonTestProcessPut.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(400, result.getStatusCodeValue());
    }

    @Test
    public void testPUTProcessMethodExpectingCode401() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("Error", "authentication failure");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(false));
        when(validator.validateProcessJSON(eq(getFakeJson("jsonTestProcessPut.json")), eq(HttpMethod.PUT),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateTicketId(anyInt(), anyString())).thenReturn(new HashMap<>());
        when(validator.validateExistingProcess(anyInt(), anyInt())).thenReturn(new HashMap<>());
        when(databaseService.updateProcess(anyInt(), any(JsonNode.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateProcess(1, 1, getFakeJson("jsonTestProcessPut.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(401, result.getStatusCodeValue());
    }


    @Test
    public void testPUTProcessMethodExpectingCode404TicketNotFound() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(eq(getFakeJson("jsonTestProcessPut.json")), eq(HttpMethod.PUT),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(databaseService.updateProcess(anyInt(), any(JsonNode.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateProcess(1, 1, getFakeJson("jsonTestProcessPut.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testPUTProcessMethodExpectingCode404() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(eq(getFakeJson("jsonTestProcessPut.json")), eq(HttpMethod.PUT),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(databaseService.updateProcess(anyInt(), any(JsonNode.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateProcess(1, 1, getFakeJson("jsonTestProcessPut.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(404, result.getStatusCodeValue());
    }

    @Test
    public void testPUTProcessMethodExpectingCode204() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateProcessJSON(eq(getFakeJson("jsonTestProcessPut.json")), eq(HttpMethod.PUT),anyString(),anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateExistingProcess(anyInt(), anyInt()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(databaseService.updateProcess(anyInt(), any(JsonNode.class))).thenReturn(Optional.of(true));
        ResponseEntity result =
                notificationController.updateProcess(1, 1, getFakeJson("jsonTestProcessPut.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(204, result.getStatusCodeValue());
    }

    @Test
    public void testUpdateTicketByIdExpectingCode204() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateModifyTicketPropertiesJSON(getFakeJson("jsonTestPUTTicket.json")))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(databaseService.updateTicketProperties(anyInt(), any(Boolean.class))).thenReturn(Optional.of(true));
        ResponseEntity result =
                notificationController.updateTicketInfo(1, getFakeJson("jsonTestPUTTicket.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void testUpdateTicketByIdExpectingCode401() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(false));
        when(validator.validateModifyTicketPropertiesJSON(getFakeJson("jsonTestPUTTicket.json"))).thenReturn(new HashMap<>());
        when(validator.validateTicketId(anyInt(), anyString())).thenReturn(new HashMap<>());
        when(databaseService.updateTicketProperties(anyInt(), any(Boolean.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateTicketInfo(1, getFakeJson("jsonTestPUTTicket.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    public void testUpdateTicketByIdExpectingCode400() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("username", "gperez");
            put("name", "German");
            put("role", "Recruiter");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateModifyTicketPropertiesJSON(getFakeJson("jsonTestPUTTicket.json")))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(validator.validateTicketId(anyInt(), anyString())).thenReturn(new HashMap<>());
        when(databaseService.updateTicketProperties(anyInt(), any(Boolean.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateTicketInfo(1, getFakeJson("jsonTestPUTTicket.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testUpdateTicketByIdExpectingCode404() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("Error", "authentication failure");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateModifyTicketPropertiesJSON(getFakeJson("jsonTestPUTTicket.json")))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", false);
                }});
        when(databaseService.updateTicketProperties(anyInt(), any(Boolean.class))).thenReturn(Optional.empty());
        ResponseEntity result =
                notificationController.updateTicketInfo(1, getFakeJson("jsonTestPUTTicket.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testUpdateTicketByIdExpectingCodeValue404() throws Exception {
        when(authenticationAndUserCommunication.getUserInfo(anyString())).thenReturn(new HashMap<String, String>() {{
            put("Error", "authentication failure");
        }});
        when(validator.validateUserInfo(any())).thenReturn(Optional.of(true));
        when(validator.validateModifyTicketPropertiesJSON(getFakeJson("jsonTestPUTTicket.json")))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(validator.validateTicketId(anyInt(), anyString()))
                .thenReturn(new HashMap<String, Boolean>() {{
                    put("", true);
                }});
        when(databaseService.updateTicketProperties(anyInt(), any(Boolean.class))).thenReturn(Optional.of(false));
        ResponseEntity result =
                notificationController.updateTicketInfo(1, getFakeJson("jsonTestPUTTicket.json"), "");
        Assert.assertNotNull(result);
        Assert.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    private List<JsonNode> getFakeNotifications() throws IOException {
        List<JsonNode> list = new ArrayList<>();
        list.add(mapper.readTree("{\"candidate_id\": 1,\"body\": \"body1\",\"notification_date\": \"2018-09-10 10:00:00\",\"is_read\": false,\"recruiter_username\": \"recruiter1\"}"));
        list.add(mapper.readTree("{\"candidate_id\": 2,\"body\": \"body2\",\"notification_date\": \"2018-09-10 12:00:00\",\"is_read\": false,\"recruiter_username\": \"recruiter2\"}"));
        return list;

    }
}