package pl.kaszaq.howfastyouaregoing.examples;

import java.text.DecimalFormat;

public class NumberUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    static {
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
    }

    public static String prettyPrint(Double val) {
        if (val == null) {
            return "";
        }
        return DECIMAL_FORMAT.format(val);
    }
}
