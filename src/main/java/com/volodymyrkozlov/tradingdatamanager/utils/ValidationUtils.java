package com.volodymyrkozlov.tradingdatamanager.utils;

public final class ValidationUtils {
    private ValidationUtils() {

    }

    public static <T> void validateRequired(T object,
                                            String objectName) {
        if (object == null) {
            throw new IllegalArgumentException("%s is required".formatted(objectName));
        }
    }
}
