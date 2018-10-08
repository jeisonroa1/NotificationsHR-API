package com.endava.hrapp.notifications.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class TicketsMicroServiceCommunicationTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Logger logger;
    @InjectMocks
    TicketsMicroServiceCommunication ticketsMicroServiceCommunication;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(ticketsMicroServiceCommunication, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(ticketsMicroServiceCommunication, "baseURL", "testURL");
    }

    @Test
    public void testIsTheOwnerTicketExpectedTrue() throws Exception {
        HashMap<String,String> expected = new HashMap<String, String>(){{
            put("owner", "true");
        }};

        ResponseEntity<HashMap<String, String>> response = new ResponseEntity<>( expected, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                isA(ParameterizedTypeReference.class)
        )).thenReturn(response);

        Optional<Boolean> result = ticketsMicroServiceCommunication.isTheOwnerTicket(0, "jwt");
        Assert.assertEquals(Optional.of(true), result);

    }

    @Test
    public void testIsTheOwnerTicketExpectedFalse() throws Exception {
        HashMap<String,String> expected = new HashMap<String, String>(){{
            put("owner", "false");
        }};

        ResponseEntity<HashMap<String, String>> response = new ResponseEntity<>( expected, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                isA(ParameterizedTypeReference.class)
        )).thenReturn(response);

        Optional<Boolean> result = ticketsMicroServiceCommunication.isTheOwnerTicket(0, "jwt");
        Assert.assertEquals(Optional.of(false), result);

    }

    @Test
    public void testGetNameOfTicketOwnerExpectedName() throws Exception {
        HashMap<String,String> expected = new HashMap<String, String>(){{
            put("username", "user");
        }};

        ResponseEntity<HashMap<String, String>> response = new ResponseEntity<>( expected, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                eq(null),
                isA(ParameterizedTypeReference.class)
        )).thenReturn(response);

        Optional<HashMap<String, String>> result = ticketsMicroServiceCommunication.getNameOfTicketOwner(0);
        Assert.assertEquals(expected.get("username"), result.get().get("username"));
    }
}
