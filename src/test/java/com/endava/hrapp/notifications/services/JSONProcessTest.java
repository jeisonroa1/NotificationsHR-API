package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Process;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.internal.guava.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class JSONProcessTest {

    private JSONProcess jSONProcess = new JSONProcess();

    @Test
    public void testGetProcessFromJSON() throws Exception {
        Process result = jSONProcess.getProcessFromJSON(1, getFakeJson("jsonTestProcessCorrect.json"));
        Assert.assertEquals(1, result.getId());
        Assert.assertEquals("Carlos Rodriguez", result.getCandidateName());
        Assert.assertEquals("HR Interview", result.getProcessPhase());
        Assert.assertFalse(result.getIsComment());
        Assert.assertEquals(LocalDate.now(),result.getLastUpdate().toLocalDate());
        Assert.assertEquals(LocalTime.now().getHour(),result.getLastUpdate().toLocalTime().getHour());
        Assert.assertEquals(LocalDateTime.of(2100,9,13,9,0,0),result.getDueDate());
        Assert.assertFalse(result.getIsClosed());
        Assert.assertEquals(1,result.getTicketId());
    }

    @Test
    public void testGetJSONFromProcess() throws Exception {
        List<String> fields= Arrays.asList("id","candidate_name","process_phase","is_comment","last_update",
                "due_date","ticket_id","is_closed");
        Process testProcess= getFakeProcess();
        JsonNode result = jSONProcess.getJSONFromProcess(testProcess);
        Assert.assertEquals(Lists.newArrayList(result.fieldNames()), fields);
        Assert.assertEquals(1, result.get("id").asInt());
        Assert.assertEquals("Carlos Rodriguez", result.get("candidate_name").asText());
        Assert.assertEquals("Job Offer", result.get("process_phase").asText());
        Assert.assertFalse(result.get("is_comment").asBoolean());
        Assert.assertEquals(LocalDateTime.of(2018,10,2,12,0,0).toString(),result.get("last_update").asText());
        Assert.assertEquals(LocalDateTime.of(2100,9,13,9,0,0).toString(),result.get("due_date").asText());
        Assert.assertFalse(result.get("is_closed").asBoolean());
        Assert.assertEquals(1,result.get("ticket_id").asInt());

        boolean testPassed=false;
        try{
            testProcess.setProcessPhase("\"fake phase");
            jSONProcess.getJSONFromProcess(testProcess);
        }catch (ServiceException e){
            testPassed=true;
        }
        Assert.assertTrue(testPassed);
    }

    @Test
    public void testUpdateProcess() throws Exception {
        Process result = jSONProcess.updateProcess(getFakeProcess(), getFakeJson("jsonTestProcessPut.json"));
        Assert.assertEquals(1, result.getId());
        Assert.assertEquals("Lina Maria", result.getCandidateName());
        Assert.assertEquals("HR Interview", result.getProcessPhase());
        Assert.assertFalse(result.getIsComment());
        Assert.assertEquals(LocalDate.now(),result.getLastUpdate().toLocalDate());
        Assert.assertEquals(LocalTime.now().getHour(),result.getLastUpdate().toLocalTime().getHour());
        Assert.assertEquals(LocalDateTime.of(2018,9,26,16,30,0),result.getDueDate());
        Assert.assertFalse(result.getIsClosed());
        Assert.assertEquals(1,result.getTicketId());
    }

    private JsonNode getFakeJson(String fakeJson) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        URL resource = getClass().getClassLoader().getResource(fakeJson);
        return mapper.readTree(resource);
    }

    public Process getFakeProcess(){
        Process p = new Process();
        p.setId(1);
        p.setCandidateName("Carlos Rodriguez");
        p.setProcessPhase("Job Offer");
        p.setIsComment(false);
        p.setLastUpdate(LocalDateTime.of(2018,10,2,12,0,0));
        p.setDueDate(LocalDateTime.of(2100,9,13,9,0,0));
        p.setTicketId(1);
        p.setIsClosed(false);
        return p;
    }
}
