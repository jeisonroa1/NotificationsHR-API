package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@EnableScheduling
public class NotificationsScheduler {

    private BusinessDaysService businessDaysService;
    private DatabaseService databaseService;
    private TicketsMicroServiceCommunication tickets;
    private NotificationGenerator notificationGenerator;
    private MailService mailService;
    private AuthenticationAndUserCommunication authentication;
    private Logger logger;

    @Autowired
    public NotificationsScheduler(BusinessDaysService businessDaysService, DatabaseService databaseService,
                                  TicketsMicroServiceCommunication tickets,NotificationGenerator notificationGenerator,
                                  MailService mailService,AuthenticationAndUserCommunication authentication){
        this.businessDaysService=businessDaysService;
        this.databaseService=databaseService;
        this.tickets=tickets;
        this.notificationGenerator=notificationGenerator;
        this.mailService=mailService;
        this.logger= LoggerFactory.getLogger(NotificationsScheduler.class);
        this.authentication=authentication;
    }

    @Scheduled(cron = "${cron}")
    private void generateNotifications(){
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-5:00"));
        LocalDate actualDate=LocalDate.now();
        int today=actualDate.getDayOfWeek().getValue();
        LocalDate yesterday=actualDate.minusDays(1);
        boolean isBusinessDay=businessDaysService.IsBusinessDay(actualDate);
        boolean yesterdayWasHoliday=!businessDaysService.IsBusinessDay(yesterday);
        try {
            HashMap<String,List<String>> managerNotifications=new LinkedHashMap<>();
            List<Integer> ticketIds = databaseService.getListOfTickets();
            for (int id : ticketIds) {
                List<Process> processForToday = databaseService.getProcessesForToday(id);
                List<Process> pendingProcesses = databaseService.getPendingProcesses(id);
                pendingProcesses.addAll(databaseService.getNotScheduledProcess(id));
                List<String> notificationsForToday =
                        notificationGenerator.generateNotificationsForTodayAgenda(processForToday);
                List<String> notificationsForPendingProcesses =
                        notificationGenerator.generateNotificationsForPendingProcesses(pendingProcesses);
                Optional<HashMap<String, String>> result = tickets.getNameOfTicketOwner(id);
                 if(result.isPresent()) {
                     if(result.get().get("correct").equals("true")) {
                         String ticketOwner =result.get().get("username");
                                 databaseService.saveNotificationAboutProcesses(
                                         buildProcessNotificationHashMap(processForToday, notificationsForToday), ticketOwner);
                         databaseService.saveNotificationAboutProcesses(
                                 buildProcessNotificationHashMap(pendingProcesses, notificationsForPendingProcesses), ticketOwner);
                         managerNotifications.put(ticketOwner, notificationsForPendingProcesses);
                         if (isBusinessDay && (!notificationsForToday.isEmpty() || !notificationsForPendingProcesses.isEmpty())) {
                             mailService.sendEmailForRecruiter(ticketOwner + "@endava.com",
                                     notificationsForToday, notificationsForPendingProcesses);
                         }
                     }
                 }
            }
            if (isBusinessDay) {
                if ((today == 1 || (today == 2 && yesterdayWasHoliday)) && !managerNotifications.isEmpty()) {
                    List<String> managers=authentication.getManagersUsername();
                    for(String username:managers) {
                        mailService.sendEmailForManager(username+"@endava.com", managerNotifications);
                    }
                }
            }
            logger.info("The emails were sent correctly.");
        }catch (ServiceException e){
            logger.error(e.getMessage()+"."+e.getCause());
        }
    }

    private HashMap<Process,String> buildProcessNotificationHashMap(List<Process> processes,List<String> notifications){
        HashMap<Process,String> result=new LinkedHashMap<>();
        if (processes.size() == notifications.size()) {
            for (int i = 0; i < processes.size(); i++) {
                result.put(processes.get(i), notifications.get(i));
            }
        }
        return result;
    }
}
