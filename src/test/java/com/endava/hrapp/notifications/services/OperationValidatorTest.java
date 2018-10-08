package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.controllers.NotificationControllerTest;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class OperationValidatorTest {
    @Mock
    private ProcessRepository processRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private DatabaseService databaseService;
    @Mock
    private BusinessDaysService businessDaysService;
    @Mock
    private TicketsMicroServiceCommunication ticketsMicroServiceCommunication;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private OperationValidator operationValidator;
    private NotificationControllerTest notificationControllerTest;

    @Before
    public void setUp() {
        notificationControllerTest=new NotificationControllerTest();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(operationValidator, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(operationValidator, "urlAuthUserMicroService", "testURL");
    }

    @Test
    public void testValidateNotExistingCandidate() throws ServiceException {


        when(processRepository.findById(anyInt())).thenReturn(Optional.empty());
        HashMap<String,Boolean> result = operationValidator.validateNotExistingCandidate(1);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(processRepository.findById(anyInt())).thenReturn(Optional.of(new Process()));
        result = operationValidator.validateNotExistingCandidate(1);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        boolean testPassed = false;
        when(processRepository.findById(anyInt()))
                .thenThrow(new RuntimeException("fake Exception"));
        try {
            operationValidator.validateNotExistingCandidate(1);
        } catch (ServiceException e) {
            testPassed = true;
        }
        Assert.assertTrue(testPassed);
    }


    @Test
    public void testValidateProcessJSON() throws IOException, ServiceException {
        HashMap<String,Boolean> result =
                operationValidator.validateProcessJSON(getFakeJson(1), HttpMethod.POST,"",0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateProcessJSON(getFakeJson(2), HttpMethod.POST,"",0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateProcessJSON(getFakeJson(3), HttpMethod.POST,"",0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        when(ticketsMicroServiceCommunication.getListOfTicketsForUser(anyString())).thenReturn(Arrays.asList(1,2));
        when(databaseService.timeCrossing(any(LocalDateTime.class),any(),anyInt())).thenReturn(false);
        when(businessDaysService.IsBusinessDay(any(LocalDate.class))).thenReturn(true);
        result = operationValidator.validateProcessJSON(getFakeJson(4), HttpMethod.POST,"",0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(databaseService.timeCrossing(any(LocalDateTime.class),any(),anyInt())).thenReturn(true);
        when(businessDaysService.IsBusinessDay(any(LocalDate.class))).thenReturn(false);
        result = operationValidator.validateProcessJSON(getFakeJson(4), HttpMethod.POST,"",0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        when(databaseService.timeCrossing(any(LocalDateTime.class),any(),anyInt())).thenReturn(false);
        when(businessDaysService.IsBusinessDay(any(LocalDate.class))).thenReturn(false);
        result = operationValidator.validateProcessJSON(getFakeJson(4), HttpMethod.POST,"",0);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        boolean testPassed=false;
        try {
            operationValidator.validateProcessJSON(null, HttpMethod.POST,"",0);
        }catch(ServiceException e)
        {
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void validateExistingProcess() throws ServiceException {
        Process p=new Process();
        p.setTicketId(1);
        when(processRepository.findById(anyInt())).thenReturn(Optional.of(p));
        HashMap<String,Boolean> result=operationValidator.validateExistingProcess(1,1);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(processRepository.findById(anyInt())).thenReturn(Optional.empty());
        result=operationValidator.validateExistingProcess(1,1);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        boolean testPassed=false;
        try {
            when(processRepository.findById(anyInt())).thenThrow(new RuntimeException(""));
            operationValidator.validateExistingProcess(1,1);
        }catch(ServiceException e)
        {
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void testValidateProcessPhase(){
       boolean result=operationValidator.validaProcessPhase("");
       Assert.assertFalse(result);

        result=operationValidator.validaProcessPhase("Fake phase");
        Assert.assertFalse(result);

        result=operationValidator.validaProcessPhase("sourcing");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("hr interview");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("technical interview");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("final interview ");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("Final Interview To Be Scheduled");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("JO To Be Made");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("JO Rejected");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("Step 1 -Start Screening & Medical Check-up");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("Step 2 - JO Made");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("Step 3 -JO Accepted");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("Step 4 -Onboard");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase("Internal Candidate");
        Assert.assertTrue(result);

        result=operationValidator.validaProcessPhase(null);
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateName(){
        boolean result=operationValidator.validateName("");
        Assert.assertFalse(result);

        result=operationValidator.validateName("123 name");
        Assert.assertFalse(result);

        result=operationValidator.validateName("Carlos Rodríguez");
        Assert.assertTrue(result);

        result=operationValidator.validateName("Che Güevara");
        Assert.assertTrue(result);

        result=operationValidator.validateName("Ñoño");
        Assert.assertTrue(result);

    }

    @Test
    public void testValidateProcessByCandidateName() throws ServiceException {
        when(processRepository.findExistingProcessByCandidateName(anyString(),anyInt()))
                .thenReturn(Optional.of(Arrays.asList(1,2)));
        HashMap<String,Boolean> result= operationValidator.validateExistingProcessByCandidateName("",1);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(processRepository.findExistingProcessByCandidateName(anyString(),anyInt())).thenReturn(Optional.empty());
        result= operationValidator.validateExistingProcessByCandidateName("",1);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        boolean testPassed=false;
        try {
            when(processRepository.findExistingProcessByCandidateName(anyString(),anyInt()))
                    .thenThrow(new RuntimeException(""));
            operationValidator.validateExistingProcessByCandidateName("",1);
        }catch (ServiceException e){
            testPassed=true;
        }
        Assert.assertTrue(testPassed);

    }
    private JsonNode getFakeJson(int opc) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        URL resource;
        switch(opc){
            case 1: //return empty JSON
                return mapper.readTree("{}");
            case 2: //return JSON without all fields expecting
                return mapper.readTree("{\"candidate_name\":\"Juan Perez\"}");
            case 3: //return JSON with all the fields expecting but with incorrect type of values
                resource = getClass().getClassLoader().getResource("jsonTestProcessIncorrect.json");
                return mapper.readTree(resource);
            case 4: //return JSON expecting
                resource = getClass().getClassLoader().getResource("jsonTestProcessCorrect.json");
                return mapper.readTree(resource);
            default: //default empty JSON
                return mapper.readTree("{}");
        }
    }

    @Test
    public void testValidateExistingNotification() throws ServiceException {

        int id=1;

        when(notificationRepository.findById(anyInt())).thenReturn(notificationControllerTest.getFakeNotification());
        HashMap<String,Boolean> result = operationValidator.validateExistingNotification(id);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(notificationRepository.findById(anyInt())).thenReturn(Optional.empty());
        result = operationValidator.validateExistingNotification(id);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));


        boolean testPassed = false;
        when(notificationRepository.findById(anyInt())).thenThrow(new RuntimeException("fake Exception"));
        try {
            operationValidator.validateExistingNotification(id);
        } catch (ServiceException e) {
            testPassed = true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void testValidateReadStatusJSON() throws IOException, ServiceException {

        HashMap<String,Boolean> result = operationValidator.validateReadStatusJson(getFakeReadStatusJson(1));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateReadStatusJson(getFakeReadStatusJson(2));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateReadStatusJson(getFakeReadStatusJson(3));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateReadStatusJson(getFakeReadStatusJson(4));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        boolean testPassed=false;
        try {
            operationValidator.validateReadStatusJson(null);
        }catch(ServiceException e)
        {
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void testValidateModifyTicketPropertiesJSON() throws Exception{
        HashMap<String,Boolean> result = operationValidator.validateModifyTicketPropertiesJSON(getFakeJson(1));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateModifyTicketPropertiesJSON(getFakeJson(2));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateModifyTicketPropertiesJSON(getFakeJson(3));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        result = operationValidator.validateModifyTicketPropertiesJSON(getFakeReadStatusJson(5));
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        boolean testPassed=false;
        try {
            operationValidator.validateModifyTicketPropertiesJSON(null);
        }catch(ServiceException e)
        {
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    private JsonNode getFakeReadStatusJson(int opc) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        URL resource;
        switch(opc){
            case 1: //return empty JSON
                return mapper.readTree("{}");
            case 2: //return JSON without all fields expecting
                return mapper.readTree("{\"candidate_name\":\"Juan Perez\"}");
            case 3: //return JSON with all the fields expecting but with incorrect type of values
                resource = getClass().getClassLoader().getResource("jsonTestReadStatusIncorrect.json");
                return mapper.readTree(resource);
            case 4: //return JSON expecting
                resource = getClass().getClassLoader().getResource("jsonTestReadStatusCorrect.json");
                return mapper.readTree(resource);
            case 5: //return JSON expecting
                resource = getClass().getClassLoader().getResource("jsonTestPUTTicket.json");
                return mapper.readTree(resource);
            default: //default empty JSON
                return mapper.readTree("{}");
        }
    }

    @Test
    public void testValidateToken() throws ServiceException {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok().body("body"));
        HashMap<String,Boolean> result=operationValidator.validateToken("");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.status(401).body("body"));
        result=operationValidator.validateToken("");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        boolean testPassed=false;
        try{
            when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                    .thenThrow(new RuntimeException(""));
            operationValidator.validateToken("");
        }catch (ServiceException e){
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void testValidateTicketId() throws ServiceException {
        when(ticketsMicroServiceCommunication.isTheOwnerTicket(anyInt(),anyString())).thenReturn(Optional.of(true));
        HashMap<String,Boolean> result=operationValidator.validateTicketId(1,"");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(true));

        when(ticketsMicroServiceCommunication.isTheOwnerTicket(anyInt(),anyString())).thenReturn(Optional.of(false));
        result=operationValidator.validateTicketId(1,"");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.containsValue(false));

        boolean testPassed=false;
        try{
            when(ticketsMicroServiceCommunication.isTheOwnerTicket(anyInt(),anyString()))
                    .thenThrow(new RuntimeException(""));
            operationValidator.validateTicketId(1,"");
        }catch (ServiceException e){
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void testValidateUserInfo(){
        HashMap<String,String> fakeHashMap=new LinkedHashMap<>();
        fakeHashMap.put("key1","value1");
        fakeHashMap.put("key2","value2");
        Optional<Boolean> result= operationValidator.validateUserInfo(fakeHashMap);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(Optional.of(false),result);

        fakeHashMap.put("key3","value3");
        result= operationValidator.validateUserInfo(fakeHashMap);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(Optional.of(true),result);

        result= operationValidator.validateUserInfo(null);
        Assert.assertTrue(!result.isPresent());
        Assert.assertEquals(Optional.empty(),result);
    }
}