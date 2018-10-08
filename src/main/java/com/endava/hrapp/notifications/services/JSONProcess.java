package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Process;
import com.endava.hrapp.notifications.domain.ProcessPhase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Service
public class JSONProcess {

    private DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Process getProcessFromJSON(int ticket_id, JsonNode json){
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00"));
        Process p=new Process();
        p.setId(json.get("id").asInt());
        p.setCandidateName(json.get("candidate_name").asText());
        p.setProcessPhase(json.get("process_phase").asText());
        for (ProcessPhase process : ProcessPhase.values()) {
            if (process.getPhaseName().equalsIgnoreCase(p.getProcessPhase())) {
                p.setProcessPhase(process.getPhaseName());
            }
        }
        p.setIsComment(json.get("is_comment").asBoolean());
        String actualTime=LocalDate.now().toString()+"T"+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        p.setLastUpdate(LocalDateTime.parse(actualTime));
        if (json.get("due_date").isTextual()) {
            p.setDueDate(LocalDateTime.parse(json.get("due_date").asText(), format));
        } else {
            p.setDueDate(null);
        }
        p.setTicketId(ticket_id);
        p.setIsClosed(json.get("is_closed").asBoolean());
        return p;
    }

    public JsonNode getJSONFromProcess(Process p) throws ServiceException {
        ObjectMapper mapper= new ObjectMapper();
        String json= "{\"id\": "+p.getId()+","+
                "\"candidate_name\": \""+p.getCandidateName()+"\","+
                "\"process_phase\": \""+p.getProcessPhase()+"\","+
                "\"is_comment\": "+p.getIsComment()+","+
                "\"last_update\": \""+p.getLastUpdate()+"\","+
                "\"due_date\": \""+p.getDueDate()+"\","+
                "\"ticket_id\": "+p.getTicketId()+","+
                "\"is_closed\": "+p.getIsClosed()+"}";
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            throw new ServiceException("Error while mapping Process to JSON",e.getCause());
        }

    }

    public Process updateProcess(Process p, JsonNode json){
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00"));
        p.setCandidateName(json.get("candidate_name").asText());
        p.setProcessPhase(json.get("process_phase").asText());
        for (ProcessPhase process : ProcessPhase.values()) {
            if (process.getPhaseName().equalsIgnoreCase(p.getProcessPhase())) {
                p.setProcessPhase(process.getPhaseName());
            }
        }
        p.setIsComment(json.get("is_comment").asBoolean());
        String actualTime=LocalDate.now().toString()+"T"+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        p.setLastUpdate(LocalDateTime.parse(actualTime));
        if (json.get("due_date").isTextual()) {
            p.setDueDate(LocalDateTime.parse(json.get("due_date").asText(), format));
        } else {
            p.setDueDate(null);
        }
        p.setIsClosed(json.get("is_closed").asBoolean());
        return p;
    }
}
