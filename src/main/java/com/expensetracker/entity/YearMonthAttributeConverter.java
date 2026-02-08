package com.expensetracker.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Date;
import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, Date> {

    @Override
    public Date convertToDatabaseColumn(YearMonth yearMonth) {
        if (yearMonth == null) return null;
        return Date.valueOf(yearMonth.atDay(1));  // Store first day
    }

    @Override
    public YearMonth convertToEntityAttribute(Date date) {
        if (date == null) return null;
        return YearMonth.from(date.toLocalDate());
    }
}
