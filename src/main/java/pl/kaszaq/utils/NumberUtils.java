package pl.kaszaq.utils;

import java.text.DecimalFormat;

/**
 *
 * @author michal.kasza
 */
public class NumberUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    static {
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
    }

    public static String prettyPrint(Double val) {
        return DECIMAL_FORMAT.format(val);
    }
}
