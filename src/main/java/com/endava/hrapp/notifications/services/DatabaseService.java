package com.endava.hrapp.notifications.services;

import com.endava.hrapp.notifications.domain.Notification;
import com.endava.hrapp.notifications.domain.Process;
import com.endava.hrapp.notifications.repositories.NotificationRepository;
import com.endava.hrapp.notifications.repositories.ProcessRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private JSONProcess jsonProcess;

    public List<JsonNode> getNotificationsForUser(String username, boolean daily) throws ServiceException {
        List<JsonNode> notificationsForUser = new ArrayList<>();
        Optional<List<Notification>> response;

        try {
            ObjectMapper mapper=new ObjectMapper();
            if( daily)
                response = this.notificationRepository.getNotificationsForDay(username);
            else
                response = this.notificationRepository.getNotificationsByUsername(username);

            if( response.isPresent()){
                List<Notification> notificationList = response.get();
                for (Notification n : notificationList) {
                    String json = "{\"id\":" + n.getId() + "," +
                            "\"candidate_id\": " + n.getProcess().getId() + "," +
                            "\"body\": \"" + n.getBody() + "\"," +
                            "\"notification_date\": \"" + n.getNotificationDate() + "\"," +
                            "\"is_read\": " + n.getIsRead() + "," +
                            "\"recruiter_username\": \"" + n.getRecruiterUsername() + "\"}";
                    notificationsForUser.add(mapper.readTree(json));
                }
            }
            return notificationsForUser;
        }catch (DataAccessResourceFailureException exception){
            throw new ServiceException("Data base communication error" , exception.getCause());
        }catch (IOException e){
            throw new ServiceException("Error while mapping notifications to JSON",e.getCause());
        }

    }

    public List<JsonNode> getNotificationsForManager(boolean daily) throws ServiceException {
        List<JsonNode> notificationsForManager = new ArrayList<>();

        try {
            ObjectMapper mapper=new ObjectMapper();
            Iterable<Notification> response;
            if(daily)
                response = this.notificationRepository.findAllDaily();
            else
                response = this.notificationRepository.findAll();
            for (Notification n : response) {
                String json = "{\"id\":" + n.getId() + "," +
                        "\"candidate_id\": " + n.getProcess().getId() + "," +
                        "\"body\": \"" + n.getBody() + "\"," +
                        "\"notification_date\": \"" + n.getNotificationDate() + "\"," +
                        "\"is_read\": " + n.getIsRead() + "," +
                        "\"recruiter_username\": \"" + n.getRecruiterUsername() + "\"}";
                notificationsForManager.add(mapper.readTree(json));
            }
            return notificationsForManager;
        }catch (DataAccessResourceFailureException exception){
            throw new ServiceException("Data base communication error" , exception.getCause());
        }catch (IOException e){
            throw new ServiceException("Error while mapping notifications to JSON",e.getCause());
        }
    }

    public  Optional<Boolean> updateProcess(int processId, JsonNode json) throws ServiceException {
        try{
            Optional<Process> p = processRepository.findById(processId);
            if (p.isPresent()) {
                jsonProcess.updateProcess(p.get(), json);
                processRepository.save(p.get());
                return Optional.of(true);
            }
            return Optional.of(false);
        }catch (DataAccessResourceFailureException exception){
            throw new ServiceException("Data base communication error" , exception.getCause());
        }
    }

    public Optional<Boolean> updateTicketProperties(int ticketId, Boolean isClose) throws ServiceException{
        try{
            Optional<List<Process>> listOfProcess = processRepository.findAllByTicketId(ticketId);
            if(listOfProcess.isPresent()){
                listOfProcess.get().stream().forEach( p -> p.setIsClosed(isClose));
                processRepository.saveAll(listOfProcess.get());
                return Optional.of(true);
            }else
                return Optional.of(false);
        }catch (DataAccessResourceFailureException exception) {
            throw new ServiceException("Data base communication error", exception.getCause());
        }
    }

    public List<Integer> getListOfTickets() throws ServiceException {
        try{
            List<Integer> ticketIds=new ArrayList<>();
            processRepository.findAll().forEach(process -> {
                if(!ticketIds.contains(process.getTicketId())){
                    ticketIds.add(process.getTicketId());
                }
            });
            return ticketIds;
        }catch (Exception e){
           throw new ServiceException("Error while obtaining tickets ids",e.getCause());
        }
    }

    public List<Process> getProcessesForToday(int ticketId) throws ServiceException {
        try{
            Optional<List<Process>> result=processRepository.selectProcessForToday(ticketId);
            return result.orElseGet(ArrayList::new);
        }catch (Exception e){
            throw new ServiceException("Error while obtaining process for today",e.getCause());
        }
    }

    public List<Process> getPendingProcesses(int ticketId) throws ServiceException {
        try{
            Optional<List<Process>> result=processRepository.selectPendingProcess(ticketId);
            return result.orElseGet(ArrayList::new);
        }catch (Exception e){
            throw new ServiceException("Error while obtaining pending processes",e.getCause());
        }
    }

    public void saveNotificationAboutProcesses(HashMap<Process, String> processes,
                                               String ticketOwner) throws ServiceException {
        try {
            List<String> notificationsAlreadyGenerated = new ArrayList<>();
            List<Notification> notificationsToSave = new ArrayList<>();
            if(!processes.isEmpty()) {
                notificationRepository.findAll().forEach(n ->
                        notificationsAlreadyGenerated.add(n.getBody())
                );
                processes.forEach((key, value) -> {
                    if (!notificationsAlreadyGenerated.contains(value)) {
                        Notification n = new Notification();
                        n.setBody(value);
                        n.setNotificationDate(LocalDateTime.now());
                        n.setIsRead(false);
                        n.setRecruiterUsername(ticketOwner);
                        n.setProcess(key);
                        notificationsToSave.add(n);
                    }
                });
                notificationRepository.saveAll(notificationsToSave);
            }
        } catch (Exception e) {
            throw new ServiceException("Error while build and save the notifications", e.getCause());
        }
    }

    public boolean timeCrossing(LocalDateTime date,List<Integer> tickets,int id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String dt=date.toLocalDate().toString()+" "+date.toLocalTime().format(formatter);
        return processRepository.findProcessByDate(dt,tickets,id).isPresent();
    }

    public List<Process> getNotScheduledProcess(int ticketId) throws ServiceException {
        try{
            Optional<List<Process>> result=processRepository.selectProcessNotScheduled(ticketId);
            return result.orElseGet(ArrayList::new);
        }catch (Exception e){
            throw new ServiceException("Error while obtaining processes not scheduled",e.getCause());
        }
    }
}
