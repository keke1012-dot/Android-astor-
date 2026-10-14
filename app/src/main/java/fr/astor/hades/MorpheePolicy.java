package fr.astor.hades;

import java.time.Duration;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Pure-Java policy for user-entered sleep sessions and optional breaks. */
final class MorpheePolicy {
    static final LocalTime BEDTIME = LocalTime.of(23, 0);
    static final LocalTime WAKE = LocalTime.of(9, 0);
    static final LocalTime PREPARE = LocalTime.of(22, 30);
    static final long BREAK_MINUTES = 60;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private MorpheePolicy() { }

    static LocalTime parseTime(String value) {
        if (value == null || !value.matches("[0-2][0-9]:[0-5][0-9]"))
            throw new IllegalArgumentException("Heure attendue : HH:mm");
        LocalTime result;
        try { result = LocalTime.parse(value, TIME); }
        catch (DateTimeException error) { throw new IllegalArgumentException("Heure invalide", error); }
        if (!result.format(TIME).equals(value))
            throw new IllegalArgumentException("Heure invalide");
        return result;
    }

    static LocalDate parseDate(String value) {
        if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException("Date attendue : AAAA-MM-JJ");
        try { return LocalDate.parse(value); }
        catch (DateTimeException error) { throw new IllegalArgumentException("Date invalide", error); }
    }

    /** Real elapsed minutes, accounting for DST. End <= start means next calendar day. */
    static long minutesSlept(String date, String start, String end, ZoneId zone) {
        LocalDate day = parseDate(date);
        LocalTime begin = parseTime(start), finish = parseTime(end);
        ZonedDateTime from = day.atTime(begin).atZone(zone);
        ZonedDateTime to = day.plusDays(!finish.isAfter(begin) ? 1 : 0)
                .atTime(finish).atZone(zone);
        long minutes = Duration.between(from.toInstant(), to.toInstant()).toMinutes();
        if (minutes < 1 || minutes > 20 * 60)
            throw new IllegalArgumentException("Durée hors limites (1 minute à 20 heures)");
        return minutes;
    }

    static ZonedDateTime nextOccurrence(ZonedDateTime now, LocalTime clock) {
        ZonedDateTime next = now.toLocalDate().atTime(clock).atZone(now.getZone());
        if (!next.isAfter(now)) next = now.toLocalDate().plusDays(1)
                .atTime(clock).atZone(now.getZone());
        return next;
    }

    static boolean pauseDue(long startedAtMillis, long nowMillis) {
        return startedAtMillis > 0 && nowMillis >= startedAtMillis
                && nowMillis - startedAtMillis >= BREAK_MINUTES * 60_000L;
    }
}
