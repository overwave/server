package dev.overwave.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Structure<T> {
    private static final int CAPACITY = 100;

    private final List<T> list;
    private int cursor;

    public Structure() {
        list = new ArrayList<>(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            list.add(null);
        }
        cursor = 0;
    }

    public boolean contains(T t) {
        return list.contains(t);
    }

    public void add(T t) {
        list.set(cursor, t);
        cursor = (cursor + 1) % CAPACITY;
    }

    @Override
    public String toString() {
        return IntStream.range(0, CAPACITY)
                .map(i -> (i + cursor) % CAPACITY)
                .mapToObj(list::get)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining("\r\n"));
    }
}
