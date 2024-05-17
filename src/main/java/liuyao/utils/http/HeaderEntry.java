package liuyao.utils.http;

import java.util.Map;

public class HeaderEntry<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

    public HeaderEntry(K k, V v) {
        this.key = k;
        this.value = v;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        this.value = value;
        return this.value;
    }
}