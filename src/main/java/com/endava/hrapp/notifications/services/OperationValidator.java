package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Notification;
import com.endava.hrapp.notifications.domain.Process;
import com.endava.hrapp.notifications.domain.ProcessPhase;
import com.endava.hrapp.notifications.repositories.NotificationRepository;
import com.endava.hrapp.notifications.repositories.ProcessRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.jersey.internal.guava.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OperationValidator {

    private RestTemplate restTemplate;
    private NotificationRepository notificationRepository;
    private ProcessRepository processRepository;
    private BusinessDaysService businessDaysService;
    private DatabaseService databaseService;
    private TicketsMicroServiceCommunication ticketsMicroServiceCommunication;
    @Value("${urlAuthUserMicroService}")
    private String urlAuthUserMicroService;

    @Autowired
    public OperationValidator(RestTemplate restTemplate,NotificationRepository notificationRepository,
                              ProcessRepository processRepository,BusinessDaysService businessDaysService,
                              DatabaseService databaseService, TicketsMicroServiceCommunication ticketsMicroServiceCommunication){
        this.restTemplate=restTemplate;
        this.notificationRepository=notificationRepository;
        this.processRepository=processRepository;
        this.businessDaysService=businessDaysService;
        this.databaseService=databaseService;
        this.ticketsMicroServiceCommunication = ticketsMicroServiceCommunication;
    }

    public HashMap<String,Boolean> validateToken(String token) throws ServiceException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity entity = new HttpEntity(headers);
            ResponseEntity<String> response = restTemplate.exchange(urlAuthUserMicroService, HttpMethod.GET,
                    entity, String.class);
            if(response.getStatusCodeValue()==200){
                return new HashMap<String,Boolean>(){{put("",true);}};
            }else{return  new HashMap<String,Boolean>(){{put("",false);}};}
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                return  new HashMap<String,Boolean>(){{put("",false);}};
            } else {
                throw new ServiceException("Exception when validate token", e.getCause());
            }
        }
    }

    public HashMap<String,Boolean> validateTicketId(int ticket_id, String token) throws ServiceException {
        try {
            Optional<Boolean> result=ticketsMicroServiceCommunication.isTheOwnerTicket(ticket_id, token);
            if(result.isPresent())
                if(result.get())
                    return new HashMap<String,Boolean>(){{put("",true);}};
            return new HashMap<String,Boolean>(){{put("",false);}};
        } catch (Exception e) {
            throw new ServiceException("Exception when validate ticket id "+e.getMessage(),e.getCause());
        }
    }

    public HashMap<String,Boolean> validateNotExistingCandidate(int process_id) throws ServiceException {
        try {
            if (processRepository.findById(process_id).isPresent()) {
                return  new HashMap<String,Boolean>(){
                    {put("The process with id="+process_id+" already exists",false);}};
            } else {
                return  new HashMap<String,Boolean>(){{put("",true);}};
            }
        } catch (Exception e) {
            throw new ServiceException("Exception when validate candidate existence",e.getCause());
        }
    }

    public HashMap<String,Boolean> validateExistingNotification(int id) throws ServiceException {
        try {
            Optional<Notification> result = notificationRepository.findById(id);
            if (result.isPresent()) { return new HashMap<String,Boolean>(){{put("",true);}};}
            else return new HashMap<String,Boolean>(){{put("",false);}};
        } catch (Exception e) {
            throw new ServiceException("Exception when validate notification existence",e.getCause());
        }
    }

    public HashMap<String,Boolean> validateExistingProcess(int process_id,int ticket_id) throws ServiceException {
        try {
            Optional<Process> p=processRepository.findById(process_id);
            if(p.isPresent()){
                if(p.get().getTicketId()==ticket_id){
                    return new HashMap<String,Boolean>(){{put("",true);}};
                }
            }
            return new HashMap<String,Boolean>(){{put("",false);}};
        } catch (Exception e) {
            throw new ServiceException("Exception when validate candidate existence",e.getCause());
        }
    }

    public HashMap<String,Boolean> validateExistingProcessByCandidateName(String candidateName,
                                                                    int ticketId) throws ServiceException {
        try {
            if (processRepository.findExistingProcessByCandidateName(candidateName, ticketId).isPresent()) {
                return new HashMap<String,Boolean>(){{put("",true);}};
            } else {
                return new HashMap<String,Boolean>(){{put("There aren't candidates that contains '"+candidateName+
                        "' in their name inside the ticket "+ticketId,false);}};
            }
        }catch (Exception e){
            throw new ServiceException("Exception while validate process by candidate's name",e.getCause());
        }
    }

    public Optional<Boolean> validateUserInfo(HashMap<String, String> body){
        try{
            if(body.size() < 3)
                return Optional.of(false);
            else
                return Optional.of(true);
        }catch (Exception e){
            return Optional.empty();
        }
    }

    public HashMap<String, Boolean> validateProcessJSON(JsonNode json, HttpMethod httpMethod, String username,int id) throws ServiceException {
        try {
            HashMap<String,Boolean> result=new LinkedHashMap<>();
            List<String> fields = new ArrayList<>();

            if(httpMethod.equals(HttpMethod.POST))
                fields = Arrays.asList("id","candidate_name", "process_phase", "is_comment",
                    "due_date", "is_closed");
            else if(httpMethod.equals(HttpMethod.PUT))
                fields = Arrays.asList("candidate_name", "process_phase", "is_comment",
                        "due_date", "is_closed");

            if (Lists.newArrayList(json.fieldNames()).equals(fields)) {
                boolean validData = json.get("candidate_name").isTextual() &&
                        json.get("process_phase").isTextual() &
                                json.get("is_comment").isBoolean() &
                                (json.get("due_date").isTextual() | json.get("due_date").isNull()) &
                                json.get("is_closed").isBoolean();

                if(httpMethod.equals(HttpMethod.POST))
                    validData = validData && json.get("id").isInt();

                if (validData) {
                    if(httpMethod.equals(HttpMethod.POST)){id=json.get("id").asInt();}
                    if (validaProcessPhase(json.get("process_phase").asText())) {
                        if (validateProcessDates(json,username,id)) {
                            if (validateName(json.get("candidate_name").asText())) {
                                result.put("",true);
                            }else{result.put("The candidate's name is wrong. The name can't have numbers or " +
                                    "characters like !#$%...",false);}
                        }else{result.put("The due date is wrong. This date can't be less than the current date or " +
                                "equal to the date of an appointment that has already scheduled. " +
                                "Also it can't be a date on weekends or holidays and must be within the hours 8-18." +
                                "The format required is yyyy-MM-ddTHH:mm:ss.",false);}
                    }else{result.put("The process phase is wrong.",false);}
                }else{result.put("The JSON's values aren't of the type required.",false);}
            }else{result.put("The JSON input doesn't have the fields required.",false);}
            return result;
        } catch (Exception e) {
            throw new ServiceException("Exception when validate JSON",e.getCause());
        }
    }

    boolean validaProcessPhase(String phaseName){
        for(ProcessPhase process:ProcessPhase.values()){
            if(process.getPhaseName().equalsIgnoreCase(phaseName)){
                return true;
            }
        }
        return false;
    }

    private boolean validateProcessDates(JsonNode json,String username,int id){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        try {
            List<Integer> tickets=ticketsMicroServiceCommunication.getListOfTicketsForUser(username);
            if(json.get("due_date").isNull()){return true;}
            TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00"));
            LocalDateTime actualTime=LocalDateTime.now();
            int hour=actualTime.getHour();
            LocalDateTime date = LocalDateTime.parse(json.get("due_date").asText(), format);
            if(date.toLocalDate().isBefore(actualTime.toLocalDate())){return false;}
            else if(date.toLocalDate().compareTo(actualTime.toLocalDate())==0 && date.getHour()<hour){return false;}
            else if(date.getHour()<8 || date.getHour()>18){return false;}
            else if(databaseService.timeCrossing(date,tickets,id)){return false;}
            else if(date.compareTo(actualTime) <= 0){ return false; }
            else{ return businessDaysService.IsBusinessDay(date.toLocalDate()); }
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean validateName(String name){
        return name.matches("\\p{javaAlphabetic}+&|^[ A-Za-záéíóúÁÉÍÓÚüñÑ]+$");
    }

    public HashMap<String,Boolean> validateReadStatusJson(JsonNode json) throws ServiceException {
        try {
            if (Lists.newArrayList(json.fieldNames()).equals(Collections.singletonList("is_read"))) {
                if (json.get("is_read").isBoolean()) {
                    return new HashMap<String,Boolean>(){{put("",true);}};
                }
            }
            return new HashMap<String,Boolean>(){{put("The field 'is_read' isn't present or the value is wrong.",false);}};
        } catch (Exception e) {
            throw new ServiceException("Exception when validate ReadStatus JSON", e.getCause());
        }
    }

    public HashMap<String,Boolean> validateModifyTicketPropertiesJSON(JsonNode json) throws ServiceException {
        try {
            if (Lists.newArrayList(json.fieldNames()).equals(Collections.singletonList("status"))) {
                if (json.get("status").isTextual())
                    if(Arrays.asList("close", "reopen").contains(json.get("status").asText().toLowerCase()))
                        return new HashMap<String,Boolean>(){{put("",true);}};
            }
            return new HashMap<String,Boolean>(){{put("The ticket status is wrong.",false);}};
        } catch (Exception e) {
            throw new ServiceException("Exception when validate JSON",e.getCause());
        }
    }
}
