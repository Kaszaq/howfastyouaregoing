package pl.kaszaq.utils;

import java.util.function.Predicate;

public class CommonPredicates {

    public static <T> Predicate<T> alwaysTrue() {
        return cl -> true;
    }
    
    public static <T> Predicate<T> not (Predicate<T> p) {
        return p.negate();
    }
}
