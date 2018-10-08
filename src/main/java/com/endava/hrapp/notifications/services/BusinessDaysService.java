package com.endava.hrapp.notifications.services;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.TimeZone;

@Service
public class BusinessDaysService {

    public boolean IsBusinessDay(LocalDate date) {
        Locale locale = new Locale("ES", "CO");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00"));
        HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(locale));
        if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
            return false;
        } else {
            return !manager.isHoliday(date);
        }
    }
}
