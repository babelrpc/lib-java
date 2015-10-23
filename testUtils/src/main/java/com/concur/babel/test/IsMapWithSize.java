package com.concur.babel.test;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.Map;

import static org.hamcrest.core.IsEqual.equalTo;

public final class IsMapWithSize<K, V> extends FeatureMatcher<Map<? extends K, ? extends V>, Integer> {
    public IsMapWithSize(Matcher<? super Integer> sizeMatcher) {
        super(sizeMatcher, "a map with size", "map size");
    }

    @Override
    protected Integer featureValueOf(Map<? extends K, ? extends V> actual) {
        return actual.size();
    }

    @Factory
    public static <K, V> Matcher<Map<? extends K, ? extends V>> aMapWithSize(Matcher<? super Integer> sizeMatcher) {
        return new IsMapWithSize<K, V>(sizeMatcher);
    }

    @Factory
    public static <K, V> Matcher<Map<? extends K, ? extends V>> aMapWithSize(int size) {
        Matcher<? super Integer> matcher = equalTo(size);
        return IsMapWithSize.<K, V>aMapWithSize(matcher);
    }

    @Factory
    public static <K, V> Matcher<Map<? extends K, ? extends V>> anEmptyMap() {
        return IsMapWithSize.<K, V>aMapWithSize(equalTo(0));
    }
}
