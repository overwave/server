package dev.overwave.server;

import java.util.ArrayList;
import java.util.List;

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
}
