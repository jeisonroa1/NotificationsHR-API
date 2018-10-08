package com.endava.hrapp.notifications.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.when;

public class AuthenticationAndUserCommunicationTest {

    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    AuthenticationAndUserCommunication authenticationAndUserCommunication;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(authenticationAndUserCommunication, "restTemplate", restTemplate);
    }

    @Test
    public void testGetUserInfoHappyPath() throws Exception {
        HashMap<String,String> expected = new LinkedHashMap<>();
        expected.put("username", "gperez");
        expected.put("name", "German");
        expected. put("role", "Recruiter");


        ResponseEntity<HashMap<String, String>> response = new ResponseEntity<>( expected,HttpStatus.OK);
        ReflectionTestUtils.setField(authenticationAndUserCommunication, "urlAuthUserMicroservice", "testURL");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                isA(ParameterizedTypeReference.class)
        )).thenReturn(response);


        HashMap<String, String> result = authenticationAndUserCommunication.getUserInfo("");
        Assert.assertEquals(result, expected);

    }
    @Test
    public void testGetUserInfoWhitBadToken() throws Exception {
        HashMap<String, String> expected = new HashMap<String, String>(){{ put("error", "authentication failure");}};

        ResponseEntity<HashMap<String, String>> response = new ResponseEntity<>( expected,HttpStatus.OK);
        ReflectionTestUtils.setField(authenticationAndUserCommunication, "urlAuthUserMicroservice", "testURL");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                isA(ParameterizedTypeReference.class)
        )).thenReturn(response);

        HashMap<String, String> result = authenticationAndUserCommunication.getUserInfo("");
        Assert.assertEquals(result, expected);

    }

}
