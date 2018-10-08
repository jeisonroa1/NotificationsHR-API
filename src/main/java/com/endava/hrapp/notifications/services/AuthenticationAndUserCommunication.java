package com.endava.hrapp.notifications.services;

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


@Service
public class AuthenticationAndUserCommunication {

    @Value("${urlAuthUserMicroService}")
    private String urlAuthUserMicroservice;

    private RestTemplate restTemplate;

    @Autowired
    public AuthenticationAndUserCommunication(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }
    public HashMap<String, String> getUserInfo(String jwt) throws ServiceException {
        HashMap<String, String> response = new HashMap<>();
        ParameterizedTypeReference<HashMap<String, String>> typeRef = new ParameterizedTypeReference<HashMap<String, String>>() {};
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwt);
        HttpEntity entity = new HttpEntity(headers);
        try {
            ResponseEntity<HashMap<String, String>> responseEntity = restTemplate.exchange(
                    urlAuthUserMicroservice,
                    HttpMethod.GET,
                    entity,
                    typeRef);
            if (responseEntity.getStatusCodeValue() ==  200) {
                response =responseEntity.getBody();
            } else if (responseEntity.getStatusCodeValue() == 500) {
                throw new ServiceException("Internal server error");
            }
            else {
                response.put("Error", "authentication failure");
            }
            return response;
        }catch ( Exception exception){
            if(exception.getMessage().contains("401 Unauthorized")){
                response.put("Error", "authentication failure");
                return response;
            }else
                throw new ServiceException(exception.getMessage(), exception.getCause() );
        }

    }

    public List<String> getManagersUsername() throws ServiceException {
        try{
            List<String> managers=new ArrayList<>();
            JsonNode result=restTemplate.getForObject(urlAuthUserMicroservice+"/managers",JsonNode.class);
            if(result != null )
                result.forEach(manager-> managers.add(manager.get("username").asText()));
            managers.remove("tbogota");
            return managers;
        }catch (Exception e){
            Logger logger=LoggerFactory.getLogger(AuthenticationAndUserCommunication.class);
            logger.error("Error while obtaining manager username's list");
            return new ArrayList<>();
        }
    }
}
