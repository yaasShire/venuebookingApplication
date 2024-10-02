package com.sporton.SportOn.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public interface MonthlyIncome {
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotalIncome();
}

