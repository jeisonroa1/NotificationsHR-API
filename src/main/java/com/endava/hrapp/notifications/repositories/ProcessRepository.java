package com.endava.hrapp.notifications.repositories;

import com.endava.hrapp.notifications.domain.Process;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends CrudRepository<Process, Integer> {

    @Query("select p.id from Process p where p.candidateName like %?1% and p.ticketId=?2")
    Optional<List<Integer>> findExistingProcessByCandidateName(String name, int ticketId);

    @Query("select p from Process p where p.candidateName like %?1% and p.ticketId=?2")
    List<Process> selectProcessByCandidateName(String name, int ticketId);

    @Query(value = "select * from processes where date_format(due_date,'%Y%m%d') = date_format(curdate(),'%Y%m%d') " +
            "and ticket_id=?1",nativeQuery = true)
    Optional<List<Process>> selectProcessForToday(int ticketId);

    @Query(value = "select * from processes where date_format(due_date,'%Y%m%d') < date_format(curdate(),'%Y%m%d') " +
            "and ticket_id=?1",nativeQuery = true)
    Optional<List<Process>> selectPendingProcess(int ticketId);
    
    @Query("select p from Process p where p.ticketId=?1")
    Optional<List<Process>> findAllByTicketId(int ticketId);

    @Query(value = "select * from processes where date_format(due_date,'%Y-%m-%d %H:%i')=?1 and ticket_id in (?2) and id!=?3",nativeQuery = true)
    Optional<Process> findProcessByDate(String date, List<Integer> tickets,int id);

    @Query(value = "select * from processes where due_date is null and ticket_id=?1",nativeQuery = true)
    Optional<List<Process>> selectProcessNotScheduled(int ticketId);
}
