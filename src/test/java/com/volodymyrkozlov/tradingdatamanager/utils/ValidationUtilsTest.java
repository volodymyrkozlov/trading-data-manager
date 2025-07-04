package com.volodymyrkozlov.tradingdatamanager.utils;

import org.junit.jupiter.api.Test;

import static com.volodymyrkozlov.tradingdatamanager.utils.ValidationUtils.validateRequired;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {

    @Test
    void throwsExceptionIfObjectIsNull() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> validateRequired(null, "Test"));

        // then
        assertThat(exception.getMessage()).isEqualTo("Test is required");
    }
}
