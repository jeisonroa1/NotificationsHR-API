package com.endava.hrapp.notifications.controllers;

import com.endava.hrapp.notifications.domain.Notification;
import com.endava.hrapp.notifications.domain.Process;
import com.endava.hrapp.notifications.domain.Rol;
import com.endava.hrapp.notifications.repositories.NotificationRepository;
import com.endava.hrapp.notifications.repositories.ProcessRepository;
import com.endava.hrapp.notifications.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/v1")
public class NotificationController {

    private NotificationRepository notificationRepository;
    private ProcessRepository processRepository;
    private DatabaseService databaseService;
    private OperationValidator validator;
    private ObjectMapper mapper;
    private JSONProcess jsonProcess;
    private AuthenticationAndUserCommunication authenticationAndUserCommunication;
    private Logger logger;

    @Autowired
    public NotificationController(NotificationRepository notificationRepository,ProcessRepository processRepository,
                                  DatabaseService databaseService,OperationValidator validator,ObjectMapper mapper,
                                  JSONProcess jsonProcess,AuthenticationAndUserCommunication authenticationAndUserCommunication) {
        this.notificationRepository = notificationRepository;
        this.processRepository = processRepository;
        this.databaseService = databaseService;
        this.validator = validator;
        this.mapper = mapper;
        this.jsonProcess = jsonProcess;
        this.authenticationAndUserCommunication = authenticationAndUserCommunication;
        this.logger= LoggerFactory.getLogger(NotificationController.class);
    }

    @GetMapping(path = "/notifications", produces = "application/json")
    public ResponseEntity<JsonNode> getNotifications(@RequestHeader(value = "Authorization") String token,
                                                     @RequestParam(name = "getDaily",required = false,defaultValue = "false") boolean getDaily) throws IOException {

        try {

            HashMap<String, String>  response = authenticationAndUserCommunication.getUserInfo(token);
            Optional<Boolean> isCorrect = validator.validateUserInfo(response);
            List<JsonNode> notificationsForUser = new ArrayList<>();

            if(isCorrect.isPresent())
                if(!isCorrect.get() )
                    return  ResponseEntity.status(401)
                            .body(mapper.readTree("{\"Message\":\"Authentication Failure\"}"));

            if(response.get("role").toLowerCase().equals(Rol.RECRUITER.toString().toLowerCase()))
                notificationsForUser = databaseService.getNotificationsForUser(response.get("username"), getDaily);
            else if(response.get("role").toLowerCase().equals(Rol.MANAGER.toString().toLowerCase()))
                notificationsForUser = databaseService.getNotificationsForManager(getDaily);
            return ResponseEntity.status(200)
                    .body(mapper.readTree("{\"notifications\":"+notificationsForUser+"}"));
        } catch (ServiceException e) {
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500).body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }


    @PostMapping(path = "/tickets/{ticket_id}/processes", consumes = "application/json",produces = "application/json")
    public ResponseEntity<JsonNode> insertProcess(@PathVariable("ticket_id") int ticketId,
                                        @RequestBody JsonNode requestBody,
                                        @RequestHeader(value = "Authorization") String token) throws IOException {
        try {
            HashMap<String, String>  response = authenticationAndUserCommunication.getUserInfo(token);
            Optional<Boolean> isValidUser = validator.validateUserInfo(response);

            if( isValidUser.isPresent())
                if(!isValidUser.get())
                    return  ResponseEntity.status(401)
                            .body(mapper.readTree("{\"Message\":\"Authentication Failure\"}"));
            String username=response.get("username");
            Optional<ResponseEntity<JsonNode>> result = checkValidationResult(validator.validateTicketId(ticketId, token),
                    404, "Ticket Not Found");
            if (!result.isPresent()) {
                result = checkValidationResult(validator.validateProcessJSON(requestBody, HttpMethod.POST, username, 0),
                        400, "Bad Input Parameter");
                if (!result.isPresent()) {
                    result = checkValidationResult(validator.validateNotExistingCandidate(
                            requestBody.get("id").asInt()), 409, "Redundant Info");
                    if (!result.isPresent()) {
                        result = Optional.of(ResponseEntity.ok().build());
                    }
                }
            }
            if (result.get().getStatusCode().is2xxSuccessful()) {
                Process p = jsonProcess.getProcessFromJSON(ticketId, requestBody);
                processRepository.save(p);
                return ResponseEntity.status(201).body(jsonProcess.getJSONFromProcess(p));
            } else {
                return result.get();
            }
        } catch (ServiceException e) {
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500)
                    .body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }

    @GetMapping(path = "/tickets/{ticket_id}/processes",produces = "application/json")
    public ResponseEntity<JsonNode> getProcessByCandidateName(@PathVariable("ticket_id") int ticketId,
                                                    @RequestHeader(value = "Authorization") String token,
                                                    @RequestParam(name = "candidate_name", required = false, defaultValue = "")
                                                            String candidateName) throws IOException {
        try {
            if (validator.validateName(candidateName)) {
                Optional<ResponseEntity<JsonNode>> result = checkValidationResult(validator.validateToken(token),
                        401, "Authentication Failure");
                if (!result.isPresent()) {
                    result = checkValidationResult(validator.validateTicketId(ticketId, token),
                            404, "Ticket Not Found");
                    if (!result.isPresent()) {
                        result = checkValidationResult(
                                validator.validateExistingProcessByCandidateName(candidateName, ticketId),
                                404, "Process Not Found");
                        if (!result.isPresent()) {
                            result = Optional.of((ResponseEntity.ok().build()));
                        }
                    }
                }
                if (result.get().getStatusCodeValue()==200) {
                    List<Process> processes=processRepository.selectProcessByCandidateName(candidateName,ticketId);
                    List<JsonNode> json=new LinkedList<>();
                    for(Process p:processes){ json.add(jsonProcess.getJSONFromProcess(p)); }
                    return ResponseEntity.status(200).body(mapper.readTree("{\"processes\":"+json+"}"));
                } else {
                    return result.get();
                }
            } else {
                return ResponseEntity.status(400)
                        .body(mapper.readTree("{\"Message\":\"Bad Input Parameter. The query parameter " +
                                "candidate_name isn't present or the name entered has an invalid format.\"}"));
            }
        } catch (ServiceException e) {
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500)
                    .body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }

    @GetMapping(path = "/tickets/{ticket_id}/processes/{process_id}", produces = "application/json")
    public ResponseEntity<JsonNode> getProcesses(@PathVariable("ticket_id") int ticketId,
                                       @PathVariable("process_id") int processId,
                                       @RequestHeader(value = "Authorization") String token) throws IOException {
        try {
            Optional<ResponseEntity<JsonNode>> result = checkValidationResult(validator.validateToken(token),
                    401, "Authentication Failure");
            if (!result.isPresent()) {
                result = checkValidationResult(validator.validateTicketId(ticketId, token),
                        404, "Ticket Not Found");
                if (!result.isPresent()) {
                    result = checkValidationResult(validator.validateExistingProcess(processId, ticketId),
                            404, "Process Not Found");
                    if (!result.isPresent()) {
                        result = Optional.of(ResponseEntity.ok().build());
                    }
                }
            }
            if (result.get().getStatusCode().is2xxSuccessful()) {
                Optional<Process> p = processRepository.findById(processId);
                if (p.isPresent()) {
                    return ResponseEntity.status(200).body(jsonProcess.getJSONFromProcess(p.get()));
                }
            }
            return result.get();
        } catch (ServiceException e) {
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500)
                    .body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }

    @PutMapping(path = "/tickets/{ticket_id}/processes/{process_id}", consumes = "application/json")
    public ResponseEntity<JsonNode> updateProcess(@PathVariable("ticket_id") int ticketId,
                                        @PathVariable("process_id") int processId,
                                        @RequestBody JsonNode json,
                                        @RequestHeader(value = "Authorization") String token) throws IOException {
        try{
            HashMap<String, String>  response = authenticationAndUserCommunication.getUserInfo(token);
            Optional<Boolean> isValidUser = validator.validateUserInfo(response);

            if( isValidUser.isPresent())
                if(!isValidUser.get())
                    return  ResponseEntity.status(401)
                            .body(mapper.readTree("{\"Message\":\"Authentication Failure\"}"));
            String username = response.get("username");
            Optional<ResponseEntity<JsonNode>> result = checkValidationResult(validator.validateTicketId(ticketId, token),
                    404, "Ticket Not Found");
            if (!result.isPresent()) {
                result = checkValidationResult(validator.validateExistingProcess(processId, ticketId),
                        404, "Process Not Found");
                if (!result.isPresent()) {
                    result = checkValidationResult(validator.validateProcessJSON(json, HttpMethod.PUT, username, processId),
                            400, "Bad Input Parameter");
                    if (!result.isPresent()) {
                        result = Optional.of(ResponseEntity.status(200).build());
                    }
                }
            }
            if (result.get().getStatusCode().is2xxSuccessful()) {
                Optional<Boolean> success = databaseService.updateProcess(processId, json);
                if(success.isPresent())
                    if (success.get())
                        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                    else
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            return result.get();
        }catch (ServiceException e){
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500).body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }

    @PutMapping(path = "/tickets/{id}", consumes = "application/json")
    public ResponseEntity<JsonNode> updateTicketInfo(@PathVariable("id") int ticketId,
                                           @RequestBody JsonNode json,
                                           @RequestHeader(value = "Authorization") String token) throws IOException {
        try{
            HashMap<String, String>  response = authenticationAndUserCommunication.getUserInfo(token);
            Optional<Boolean> isValidUser = validator.validateUserInfo(response);

            if( isValidUser.isPresent())
                if(!isValidUser.get())
                    return  ResponseEntity.status(401)
                            .body(mapper.readTree("{\"Message\":\"Authentication Failure\"}"));

            Optional<ResponseEntity<JsonNode>> result = checkValidationResult(validator.validateTicketId(ticketId, token),
                    404, "Ticket Not Found");
            if (!result.isPresent()) {
                result = checkValidationResult(validator.validateModifyTicketPropertiesJSON(json),
                        400, "Bad Input Parameter");
                if (!result.isPresent()) {
                    result = Optional.of(ResponseEntity.status(200).build());
                }
            }
            if (result.get().getStatusCode().is2xxSuccessful()) {
                Boolean isClosed = json.get("status").asText().equalsIgnoreCase("close");
                Optional<Boolean> success = databaseService.updateTicketProperties(ticketId, isClosed);
                if (success.isPresent())
                    if (success.get())
                        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                    else
                        return ResponseEntity.status(404)
                                .body(mapper.readTree("{\"Message\":\"Ticket Not Found\"}"));

            }
            return result.get();
        }catch (ServiceException e){
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500).body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }



    @PutMapping(path = "/notifications/{id}", consumes = "application/json")
    public ResponseEntity<JsonNode> updateNotificationReadingStatus(@PathVariable("id") int id,
                                                          @RequestBody JsonNode json,
                                                          @RequestHeader(value = "Authorization") String token) throws IOException {
        try {
            Optional<ResponseEntity<JsonNode>> result = checkValidationResult(validator.validateToken(token),
                    401, "Authentication Failure");
            if (!result.isPresent()) {
                result = checkValidationResult(validator.validateExistingNotification(id),
                        404, "Notification Not Found");
                if (!result.isPresent()) {
                    result = checkValidationResult(validator.validateReadStatusJson(json),
                            400, "Bad Input Parameter");
                    if (!result.isPresent()) {
                        result = Optional.of(ResponseEntity.ok().build());
                    }
                }
            }
            if (result.get().getStatusCode().is2xxSuccessful()) {
                Optional<Notification> response = notificationRepository.findById(id);
                if (response.isPresent()) {
                    Notification n = response.get();
                    n.setIsRead(json.get("is_read").asBoolean());
                    notificationRepository.save(n);
                    return ResponseEntity.status(204).build();
                } else {
                    return ResponseEntity.status(500)
                            .body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
                }

            } else {
                return result.get();
            }
        } catch (ServiceException e) {
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            return ResponseEntity.status(500).body(mapper.readTree("{\"Error\":\"Internal Server Error\"}"));
        }
    }

    private Optional<ResponseEntity<JsonNode>> checkValidationResult(HashMap<String,Boolean> result,
                                                           int code, String message) throws ServiceException {
        try {
            if (result.containsValue(false)) {
                return Optional.of(ResponseEntity.status(code)
                        .body(mapper.readTree("{\"Message\":" + "\"" + message + ". " +
                                result.keySet().toArray()[0]+"\"}")));
            }
            return Optional.empty();
        } catch (IOException e) {
            logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
            throw new ServiceException("", e.getCause());
        }
    }
}
