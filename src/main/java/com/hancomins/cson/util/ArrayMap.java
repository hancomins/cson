package com.hancomins.cson.util;

import java.util.*;
import java.util.stream.Collectors;

public class ArrayMap<E> implements Map<Integer, E> {

    private final ArrayList<NumberIndexEntry<E>> list = new ArrayList<>();

    private int size = 0;

    public ArrayMap() {
    }

    public ArrayMap(int initialCapacity) {
        list.ensureCapacity(initialCapacity);
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if(!(key instanceof Integer)) {
            return false;
        }
        int index = (Integer) key;
        if(index < 0 | index > list.size()) {
            return false;
        }
        return list.get(index).getValue() != null;

    }

    @Override
    public boolean containsValue(Object value) {
        if(value == null) {
            return false;
        }
        for (NumberIndexEntry<E> keyValueEntry : list) {
            if(Objects.equals(keyValueEntry.getValue(), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public E get(Object key) {
        //noinspection DuplicatedCode
        if(!(key instanceof Integer)) {
            return null;
        }
        int index = (Integer) key;
        if(index < 0 || index >= list.size()) {
            return null;
        }
        NumberIndexEntry<E> entry = list.get(index);
        if(entry == null) {
            return null;
        }
        return entry.getValue();
    }

    @Override
    public E put(Integer key, E value) {
        if(key < 0) {
            return null;
        }
        if(key >= list.size()) {
            list.ensureCapacity(key + 1);
            for(int i = list.size(); i < key; i++) {
                list.add(null);
            }
            size++;
            list.add(new NumberIndexEntry<>(key, value));
            return null;
        }
        NumberIndexEntry<E> keyValueEntry = list.get(key);
        if(keyValueEntry == null) {
            size++;
            list.set(key, new NumberIndexEntry<>(key, value));
            return null;
        }
        E old = keyValueEntry.getValue();
        keyValueEntry.setValue(value);
        return old;
    }

    @Override
    public E remove(Object key) {
        //noinspection DuplicatedCode
        if(!(key instanceof Integer)) {
            return null;
        }
        int index = (Integer) key;
        if(index < 0 || index >= list.size()) {
            return null;
        }
        NumberIndexEntry<E> keyValueEntry = list.get(index);
        list.set(index, null);
        size--;
        return keyValueEntry.getValue();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends E> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        list.clear();
        size = 0;
    }

    @Override
    public Set<Integer> keySet() {
        return list.stream().filter(Objects::nonNull).map(NumberIndexEntry::getKey).collect(Collectors.toSet());
    }

    @Override
    public Collection<E> values() {
        return list.stream().filter(Objects::nonNull).map(NumberIndexEntry::getValue).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<Integer, E>> entrySet() {
        return list.stream().filter(Objects::nonNull).map(e -> (Entry<Integer, E>) e).collect(Collectors.toSet());
    }


    public static class NumberIndexEntry<V> implements Entry<Integer, V> {

        private final Integer key;
        private V value;

        public NumberIndexEntry(Integer key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
}
