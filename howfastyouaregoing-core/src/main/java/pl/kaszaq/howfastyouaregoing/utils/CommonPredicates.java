package pl.kaszaq.howfastyouaregoing.utils;

import java.util.function.Predicate;

public class CommonPredicates {

    public static <T> Predicate<T> alwaysTrue() {
        return cl -> true;
    }
}
