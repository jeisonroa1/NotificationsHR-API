package com.endava.hrapp.notifications.services;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

public class BusinessDaysServiceTest {

    private BusinessDaysService businessDaysService = new BusinessDaysService();

    @Test
    public void testIsBusinessDay() throws Exception {
        boolean result = businessDaysService.IsBusinessDay(LocalDate.of(2018, Month.OCTOBER, 2));
        Assert.assertTrue(result);

        result = businessDaysService.IsBusinessDay(LocalDate.of(2018, Month.OCTOBER, 15));
        Assert.assertFalse(result);

        result = businessDaysService.IsBusinessDay(LocalDate.of(2018, Month.OCTOBER, 6));
        Assert.assertFalse(result);

        result = businessDaysService.IsBusinessDay(LocalDate.of(2018, Month.OCTOBER, 7));
        Assert.assertFalse(result);
    }
}