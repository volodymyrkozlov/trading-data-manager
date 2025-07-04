package com.volodymyrkozlov.tradingdatamanager.repository;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
