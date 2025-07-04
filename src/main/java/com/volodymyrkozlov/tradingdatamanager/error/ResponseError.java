package com.volodymyrkozlov.tradingdatamanager.error;

import static com.volodymyrkozlov.tradingdatamanager.utils.ValidationUtils.validateRequired;

public record ResponseError(String message) {

    public ResponseError {
        validateRequired(message, "message");
    }
}
