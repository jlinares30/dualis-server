package com.dualis.api.shared.time;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Month boundaries in the users' time zone (app.timezone, America/Lima by default), so a movement made
 * at 21:00 on the last day of the month in Lima is not counted in the next month (as it was with UTC).
 */
public final class MonthPeriod {

    public static final String DEFAULT_ZONE = "America/Lima";

    private MonthPeriod() {
    }

    public static ZoneId zone(String timezone) {
        return ZoneId.of(timezone != null && !timezone.isBlank() ? timezone : DEFAULT_ZONE);
    }

    public static YearMonth current(String timezone) {
        return YearMonth.now(zone(timezone));
    }

    /** First instant of the month (inclusive). */
    public static OffsetDateTime start(YearMonth month, String timezone) {
        return month.atDay(1).atStartOfDay(zone(timezone)).toOffsetDateTime();
    }

    /** Last instant of the month (inclusive). */
    public static OffsetDateTime end(YearMonth month, String timezone) {
        return month.plusMonths(1).atDay(1).atStartOfDay(zone(timezone)).toOffsetDateTime().minusNanos(1);
    }
}
