package com.endava.hrapp.notifications.repositories;

import com.endava.hrapp.notifications.domain.Notification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {

    @Query("from Notification n where n.recruiterUsername=?1 ")
    Optional<List<Notification>> getNotificationsByUsername(String username);

    @Query(value = "SELECT * FROM notifications WHERE notification_date > CURDATE() AND notification_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY) and recruiter_username like  %?1%",  nativeQuery = true)
    Optional<List<Notification>> getNotificationsForDay(String username);

    @Query(value = "SELECT * FROM notifications WHERE notification_date > CURDATE() AND notification_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY)",  nativeQuery = true)
    Iterable<Notification> findAllDaily();

}
