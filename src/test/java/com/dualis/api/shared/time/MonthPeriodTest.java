package com.dualis.api.shared.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class MonthPeriodTest {

    @Test
    @DisplayName("A movement at 21:00 on Oct 31 in Lima belongs to October, not November")
    void limaEveningStaysInMonth() {
        YearMonth october = YearMonth.of(2026, 10);
        OffsetDateTime evening = OffsetDateTime.parse("2026-10-31T21:00:00-05:00"); // 02:00 UTC on Nov 1

        assertThat(evening.isAfter(MonthPeriod.start(october, "America/Lima"))).isTrue();
        assertThat(evening.isBefore(MonthPeriod.end(october, "America/Lima"))).isTrue();
        assertThat(evening.isBefore(MonthPeriod.start(YearMonth.of(2026, 11), "America/Lima"))).isTrue();
    }
}
