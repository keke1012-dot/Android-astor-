package fr.astor.hades;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class MorpheePolicyTest {
    private static void eq(long expected, long actual) {
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }
    private static void invalid(Runnable runnable) {
        try { runnable.run(); throw new AssertionError("Should reject input"); }
        catch (IllegalArgumentException expected) { /* passed */ }
    }
    public static void main(String[] args) {
        ZoneId paris = ZoneId.of("Europe/Paris");
        eq(600, MorpheePolicy.minutesSlept("2026-10-13", "23:00", "09:00", paris));
        eq(90, MorpheePolicy.minutesSlept("2026-10-13", "13:00", "14:30", paris));
        eq(540, MorpheePolicy.minutesSlept("2026-10-13", "23:30", "08:30", paris));
        eq(540, MorpheePolicy.minutesSlept("2026-03-28", "23:00", "09:00", paris));
        eq(660, MorpheePolicy.minutesSlept("2026-10-24", "23:00", "09:00", paris));
        invalid(() -> MorpheePolicy.parseTime("25:00"));
        invalid(() -> MorpheePolicy.parseDate("2026-02-30"));
        invalid(() -> MorpheePolicy.minutesSlept("2026-10-13", "23:00", "23:00", paris));
        ZonedDateTime now = ZonedDateTime.of(2026, 10, 13, 22, 59, 0, 0, paris);
        if (!MorpheePolicy.nextOccurrence(now, LocalTime.of(23, 0)).toLocalDate()
                .equals(now.toLocalDate())) throw new AssertionError("today");
        if (!MorpheePolicy.nextOccurrence(now.plusMinutes(2), LocalTime.of(23, 0))
                .toLocalDate().equals(now.toLocalDate().plusDays(1)))
            throw new AssertionError("tomorrow");
        if (MorpheePolicy.pauseDue(1_000, 3_600_999)) throw new AssertionError("too early");
        if (!MorpheePolicy.pauseDue(1_000, 3_601_000)) throw new AssertionError("due");
        System.out.println("MORPHEE policy PASS");
    }
}
