package com.endava.hrapp.notifications.services;

public class ServiceException extends Exception{

   public ServiceException(String message, Throwable cause) {
       super(message, cause);
   }

    public ServiceException(String message) {
        super(message);
    }
}