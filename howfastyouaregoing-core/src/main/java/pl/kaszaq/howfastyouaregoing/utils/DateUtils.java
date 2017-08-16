package pl.kaszaq.howfastyouaregoing.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DateUtils {

    private static final DateTimeFormatter EXCEL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final static DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter MONTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static ZonedDateTime parseDate(String date) {
        ZonedDateTime createdDate = ZonedDateTime.parse(date, DATETIME_FORMATTER);
        return createdDate;
    }

    public static String printDateTimeExcel(ZonedDateTime date) {
        return date.format(EXCEL_DATETIME_FORMATTER);
    }

    public static String printDate(ZonedDateTime date) {
        return date.format(DATETIME_FORMATTER);
    }

    public static String printSimpleDate(ZonedDateTime date) {
        return date.format(SIMPLE_DATE_FORMATTER);
    }

    public static String printSimpleDate(LocalDate date) {
        return date.format(SIMPLE_DATE_FORMATTER);
    }

    public static String printMonthOnlyDate(ZonedDateTime date) {
        return date.format(MONTH_DATE_FORMATTER);
    }

    public static Collection<? extends LocalDate> getCollectionOfLocalDates(ZonedDateTime from, ZonedDateTime to) {
        return getCollectionOfLocalDates(from, to, Integer.MAX_VALUE);
    }

    public static Collection<? extends LocalDate> getCollectionOfLocalDates(ZonedDateTime from, ZonedDateTime to, int max) {
        //TODO if we used localdatetime (casted to system default time zone at loading of rest) this would not be required at all... blah!
        if (from == null) {
            System.out.println("Co jest grane?");
        }
        LocalDate fromLocalDate = LocalDate.from(from.withZoneSameInstant(ZoneId.systemDefault()));
        LocalDate toLocalDate = LocalDate.from(to.withZoneSameInstant(ZoneId.systemDefault()));
        List<LocalDate> dates = new ArrayList<>();
        int count = 0;
        while (!fromLocalDate.isAfter(toLocalDate) && count < max) {
            dates.add(fromLocalDate);
            fromLocalDate = fromLocalDate.plusDays(1);
            count++;
        }
        return dates;
    }

    public static Collection<? extends LocalDate> getCollectionOfLocalDatesBetweenDateExclusive(ZonedDateTime from, ZonedDateTime to) {
        LocalDate fromLocalDate = LocalDate.from(from.withZoneSameInstant(ZoneId.systemDefault())).plusDays(1);
        LocalDate toLocalDate = LocalDate.from(to.withZoneSameInstant(ZoneId.systemDefault())).minusDays(1);

        List<LocalDate> dates = new ArrayList<>();
        while (!fromLocalDate.isAfter(toLocalDate)) {
            dates.add(fromLocalDate);
            fromLocalDate = fromLocalDate.plusDays(1);
        }
        return dates;
    }
}
