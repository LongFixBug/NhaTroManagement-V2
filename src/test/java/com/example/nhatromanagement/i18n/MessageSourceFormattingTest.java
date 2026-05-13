package com.example.nhatromanagement.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageSourceFormattingTest {

    private final ResourceBundleMessageSource messageSource = createMessageSource();

    @Test
    void englishMeterReadingMessagesInterpolatePreviousValue() {
        String electricityMessage = messageSource.getMessage(
                "error.bill.elec.current.min",
                new Object[] { 123 },
                Locale.ENGLISH);
        String waterMessage = messageSource.getMessage(
                "error.bill.water.current.min",
                new Object[] { 56 },
                Locale.ENGLISH);

        assertTrue(electricityMessage.contains("123"));
        assertTrue(waterMessage.contains("56"));
    }

    @Test
    void vietnameseMeterReadingMessagesInterpolatePreviousValue() {
        Locale vietnamese = Locale.forLanguageTag("vi");
        String electricityMessage = messageSource.getMessage(
                "error.bill.elec.current.min",
                new Object[] { 123 },
                vietnamese);
        String waterMessage = messageSource.getMessage(
                "error.bill.water.current.min",
                new Object[] { 56 },
                vietnamese);

        assertTrue(electricityMessage.contains("123"));
        assertTrue(waterMessage.contains("56"));
    }

    private static ResourceBundleMessageSource createMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
