package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.controllers.NotificationController;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TicketsMicroServiceCommunication {

    private RestTemplate restTemplate;
    @Value("${urlTicketsMicroService}")
    private String baseURL;
    private Logger logger;
    @Autowired
    public TicketsMicroServiceCommunication(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.logger= LoggerFactory.getLogger(NotificationController.class);
    }


    public Optional<Boolean>  isTheOwnerTicket(int ticketId, String jwt) throws ServiceException{
        try{
            ParameterizedTypeReference<HashMap<String, String>> typeRef =
                    new ParameterizedTypeReference<HashMap<String, String>>() {};
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", jwt);
            HttpEntity entity = new HttpEntity(headers);

            ResponseEntity<HashMap<String, String>> response = restTemplate.exchange(
                    baseURL+"/"+ticketId+"/recruiter/verify",
                    HttpMethod.GET,
                    entity,
                    typeRef
            );

            if(response.getBody() != null && response.getStatusCodeValue() == 200 )
                if( response.getBody().keySet().contains("owner") )
                    if(response.getBody().get("owner").equals("true")){
                        return Optional.of(true);
            }
            return Optional.of(false);
        }catch (Exception e){
            if(e.getMessage().contains("404")) {
                logger.info("Ticket not found");
                return Optional.of(false);
            }
            else if(e.getMessage().contains("400")) {
                logger.info("Bad input Parameter");
                return Optional.of(false);
            }
            else if(e.getMessage().contains("401")) {
                logger.info("Invalid token");
                return Optional.of(false);
                }
            logger.error("Error while communicate with tickets micro service" + e.getMessage());
            throw new ServiceException("Error while communicate with tickets micro service",e.getCause());
        }
    }


    public Optional<HashMap<String, String>> getNameOfTicketOwner(int ticketId ) throws ServiceException {
        try{
            ParameterizedTypeReference<HashMap<String, String>> typeRef =
                    new ParameterizedTypeReference<HashMap<String, String>>() {};

            ResponseEntity<HashMap<String, String>> response = restTemplate.exchange(
                    baseURL+"/"+ticketId+"/recruiter",
                    HttpMethod.GET,
                    null,
                    typeRef
            );

            return Optional.of(new HashMap<String, String>(){{
                put("correct", "true");
                put("username", response.getBody().get("username"));
            }});
        }catch (Exception e){
            if(e.getMessage().contains("401")) {
                logger.info("Ticket not Found");
                return Optional.of(new HashMap<String, String>(){{
                    put("correct", "false");
                }});
            }
            logger.error(" Error while validate ticket owner " + e.getMessage());
            throw new ServiceException("Error while communicate with tickets micro service",e.getCause());
        }
    }

    public List<Integer> getListOfTicketsForUser(String username){
        try{
            JsonNode result=restTemplate.getForObject(baseURL+"/recruiter?username="+username,JsonNode.class);
            List<Integer> tickets=new ArrayList<>();
            if(result!=null){
                result.forEach(ticket->tickets.add(ticket.get("idTicket").asInt()));
            }
            return tickets;
        }catch (Exception e){
        logger.error("Error while obtain list of tickets for user " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
