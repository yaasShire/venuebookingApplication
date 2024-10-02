package com.sporton.SportOn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyIncomeDTO {
    private String monthName;
    private BigDecimal totalIncome;
}