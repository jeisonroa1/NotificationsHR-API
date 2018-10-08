package com.endava.hrapp.notifications.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Service
public class MailService {
    private String from;
    private String fromname;
    private String smtp_username;
    private String smtp_password;
    private String configSet;
    private String host;
    private int port ;
    private String subject;
    private Properties props;
    private Transport transport;
    private MimeMessage msg;

    public MailService() {
        from = "notificationsendava@gmail.com";
        fromname = "Notification Team";
        smtp_username = "AKIAJLDQAICOPVBIK3ZA";
        smtp_password = "AvSB2M6sowZeqIHftTT5UmPiqYPeE4w4K9sp2f2BwWhK";
        configSet = "ConfigSet";
        host = "email-smtp.us-east-1.amazonaws.com";
        port = 587; //465 OR 587
        subject = "Daily Notifications";
        props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
    }

    public void sendEmailForRecruiter(String to, List<String> notifications, List<String> pendingProcesses) throws ServiceException {
        String body = "<h1>Daily Notifications</h1>"+
                "<p>Here is the agenda for today and the reminders of the processes that you have pending:</p>"+
                "<h3>Agenda for today:</h3><ul>";
        if(notifications.isEmpty()){
            body+="<li>You don't have nothing for today.</li>";
        }else {
            for (String item : notifications) {
                body = body.concat("<li>" + item + "</li>");
            }
        }
        body=body.concat("</ul><h3>Pending Processes:</h3><ul>");
        if(pendingProcesses.isEmpty()){
            body+="<li>You don't have any pending process.</li>";
        }
        else {
            for (String item : pendingProcesses) {
                body = body.concat("<li>" + item + "</li>");
            }
        }
        body=body.concat("</ul>");
        send(body,to);
    }

    public void sendEmailForManager(String to, HashMap<String,List<String>> notifications) throws ServiceException {
        String body = "<h1>Daily Notifications</h1>"+
                "<p>Here is the list of notifications from recruiters with pending processes:</p><br>";
        final String[] aux = {""};
        notifications.forEach((key,value)->{
            String b="<h3>"+key.toUpperCase()+"'s Notifications:</h3><ul>";
            for(String item:value){
                b=b.concat("<li>"+item+"</li>");
            }
            b=b.concat("</ul><br>");
            aux[0] +=b;
        });
        body+=aux[0];
        send(body,to);
    }

    private void send(String body, String to) throws ServiceException {

        body="<html><head><meta charset=\"UTF-8\"></head>"+
               "<body>"+ body +"</body></html>";
        Session session = Session.getDefaultInstance(props);
        msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(from, fromname));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setHeader("X-SES-CONFIGURATION-SET", configSet);

            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(body, "text/html; charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);
            msg.setContent(multipart);

            transport = session.getTransport();
            transport.connect(host, smtp_username, smtp_password);
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("Fail internet address."+e.getMessage(), e.getCause());
        } catch (MessagingException ex) {
            Logger logger= LoggerFactory.getLogger(MailService.class);
            logger.error(ex.getMessage()+" ."+ex.getCause());
        }
    }
}
