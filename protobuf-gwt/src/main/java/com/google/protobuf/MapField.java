// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import static com.google.protobuf.Internal.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Internal representation of map fields in generated messages.
 *
 * <p>This class supports accessing the map field as a {@link Map} to be used in generated API and
 * also supports accessing the field as a {@link List} to be used in reflection API. It keeps track
 * of where the data is currently stored and do necessary conversions between map and list.
 *
 * <p>This class is a protobuf implementation detail. Users shouldn't use this class directly.
 *
 * <p>THREAD-SAFETY NOTE: Read-only access is thread-safe. Users can call getMap() and getList()
 * concurrently in multiple threads. If write-access is needed, all access must be synchronized.
 */
public class MapField<K, V> extends MapFieldReflectionAccessor implements MutabilityOracle {

  /**
   * Indicates where the data of this map field is currently stored.
   *
   * <ul>
   *   <li>MAP: Data is stored in mapData.
   *   <li>LIST: Data is stored in listData.
   *   <li>BOTH: mapData and listData have the same data.
   * </ul>
   *
   * <p>When the map field is accessed (through generated API or reflection API), it will shift
   * between these 3 modes:
   *
   * <pre>
   *          <b>getMap()   getList()    getMutableMap()   getMutableList()</b>
   * <b>MAP</b>      MAP        BOTH         MAP               LIST
   * <b>LIST</b>     BOTH       LIST         MAP               LIST
   * <b>BOTH</b>     BOTH       BOTH         MAP               LIST
   * </pre>
   *
   * <p>As the map field changes its mode, the list/map reference returned in a previous method call
   * may be invalidated.
   */
  private enum StorageMode {
    MAP,
    LIST,
    BOTH
  }

  private volatile boolean isMutable;
  private volatile StorageMode mode;
  private MutabilityAwareMap<K, V> mapData;
  private List<Message> listData;

  // Convert between a map entry Message and a key-value pair.
  private static interface Converter<K, V> {
    Message convertKeyAndValueToMessage(K key, V value);

    void convertMessageToKeyAndValue(Message message, Map<K, V> map);

    Message getMessageDefaultInstance();
  }

  private static class ImmutableMessageConverter<K, V> implements Converter<K, V> {
    private final MapEntry<K, V> defaultEntry;

    public ImmutableMessageConverter(MapEntry<K, V> defaultEntry) {
      this.defaultEntry = defaultEntry;
    }

    public Message convertKeyAndValueToMessage(K key, V value) {
      return defaultEntry.newBuilderForType().setKey(key).setValue(value).buildPartial();
    }

    @SuppressWarnings("unchecked")
    public void convertMessageToKeyAndValue(Message message, Map<K, V> map) {
      MapEntry<K, V> entry = (MapEntry<K, V>) message;
      map.put(entry.getKey(), entry.getValue());
    }

    public Message getMessageDefaultInstance() {
      return defaultEntry;
    }
  }

  private final Converter<K, V> converter;

  private MapField(Converter<K, V> converter, StorageMode mode, Map<K, V> mapData) {
    this.converter = converter;
    this.isMutable = true;
    this.mode = mode;
    this.mapData = new MutabilityAwareMap<K, V>(this, mapData);
    this.listData = null;
  }

  private MapField(MapEntry<K, V> defaultEntry, StorageMode mode, Map<K, V> mapData) {
    this(new ImmutableMessageConverter<K, V>(defaultEntry), mode, mapData);
  }

  /** Returns an immutable empty MapField. */
  public static <K, V> MapField<K, V> emptyMapField(MapEntry<K, V> defaultEntry) {
    return new MapField<K, V>(defaultEntry, StorageMode.MAP, Collections.<K, V>emptyMap());
  }

  /** Creates a new mutable empty MapField. */
  public static <K, V> MapField<K, V> newMapField(MapEntry<K, V> defaultEntry) {
    return new MapField<K, V>(defaultEntry, StorageMode.MAP, new LinkedHashMap<K, V>());
  }

  private Message convertKeyAndValueToMessage(K key, V value) {
    return converter.convertKeyAndValueToMessage(key, value);
  }

  private void convertMessageToKeyAndValue(Message message, Map<K, V> map) {
    converter.convertMessageToKeyAndValue(message, map);
  }

  private List<Message> convertMapToList(MutabilityAwareMap<K, V> mapData) {
    List<Message> listData = new ArrayList<Message>();
    for (Map.Entry<K, V> entry : mapData.entrySet()) {
      listData.add(convertKeyAndValueToMessage(entry.getKey(), entry.getValue()));
    }
    return listData;
  }

  private MutabilityAwareMap<K, V> convertListToMap(List<Message> listData) {
    Map<K, V> mapData = new LinkedHashMap<K, V>();
    for (Message item : listData) {
      convertMessageToKeyAndValue(item, mapData);
    }
    return new MutabilityAwareMap<K, V>(this, mapData);
  }

  /** Returns the content of this MapField as a read-only Map. */
  public Map<K, V> getMap() {
    if (mode == StorageMode.LIST) {
      synchronized (this) {
        if (mode == StorageMode.LIST) {
          mapData = convertListToMap(listData);
          mode = StorageMode.BOTH;
        }
      }
    }
    return Collections.unmodifiableMap(mapData);
  }

  /** Gets a mutable Map view of this MapField. */
  public Map<K, V> getMutableMap() {
    if (mode != StorageMode.MAP) {
      if (mode == StorageMode.LIST) {
        mapData = convertListToMap(listData);
      }
      listData = null;
      mode = StorageMode.MAP;
    }
    return mapData;
  }

  public void mergeFrom(MapField<K, V> other) {
    getMutableMap().putAll(MapFieldLite.copy(other.getMap()));
  }

  public void clear() {
    mapData = new MutabilityAwareMap<K, V>(this, new LinkedHashMap<K, V>());
    mode = StorageMode.MAP;
  }

  @SuppressWarnings("unchecked")
  public boolean equals(
          Object object) {
    if (!(object instanceof MapField)) {
      return false;
    }
    MapField<K, V> other = (MapField<K, V>) object;
    return MapFieldLite.<K, V>equals(getMap(), other.getMap());
  }

  public int hashCode() {
    return MapFieldLite.<K, V>calculateHashCodeForMap(getMap());
  }

  /** Returns a deep copy of this MapField. */
  public MapField<K, V> copy() {
    return new MapField<K, V>(converter, StorageMode.MAP, MapFieldLite.copy(getMap()));
  }

  /** Gets the content of this MapField as a read-only List. */
  List<Message> getList() {
    if (mode == StorageMode.MAP) {
      synchronized (this) {
        if (mode == StorageMode.MAP) {
          listData = convertMapToList(mapData);
          mode = StorageMode.BOTH;
        }
      }
    }
    return Collections.unmodifiableList(listData);
  }

  /** Gets a mutable List view of this MapField. */
  List<Message> getMutableList() {
    if (mode != StorageMode.LIST) {
      if (mode == StorageMode.MAP) {
        listData = convertMapToList(mapData);
      }
      mapData = null;
      mode = StorageMode.LIST;
    }
    return listData;
  }

  /** Gets the default instance of the message stored in the list view of this map field. */
  Message getMapEntryMessageDefaultInstance() {
    return converter.getMessageDefaultInstance();
  }

  /**
   * Makes this list immutable. All subsequent modifications will throw an {@link
   * UnsupportedOperationException}.
   */
  public void makeImmutable() {
    isMutable = false;
  }

  /** Returns whether this field can be modified. */
  public boolean isMutable() {
    return isMutable;
  }

  /* (non-Javadoc)
   * @see com.google.protobuf.MutabilityOracle#ensureMutable()
   */
  public void ensureMutable() {
    if (!isMutable()) {
      throw new UnsupportedOperationException();
    }
  }

  /** An internal map that checks for mutability before delegating. */
  static class MutabilityAwareMap<K, V> implements Map<K, V> {
    private final MutabilityOracle mutabilityOracle;
    private final Map<K, V> delegate;

    MutabilityAwareMap(MutabilityOracle mutabilityOracle, Map<K, V> delegate) {
      this.mutabilityOracle = mutabilityOracle;
      this.delegate = delegate;
    }

    public int size() {
      return delegate.size();
    }

    public boolean isEmpty() {
      return delegate.isEmpty();
    }

    public boolean containsKey(Object key) {
      return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
      return delegate.containsValue(value);
    }

    public V get(Object key) {
      return delegate.get(key);
    }

    public V put(K key, V value) {
      mutabilityOracle.ensureMutable();
      checkNotNull(key);
      checkNotNull(value);
      return delegate.put(key, value);
    }

    public V remove(Object key) {
      mutabilityOracle.ensureMutable();
      return delegate.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
      mutabilityOracle.ensureMutable();
      for (K key : m.keySet()) {
        checkNotNull(key);
        checkNotNull(m.get(key));
      }
      delegate.putAll(m);
    }

    public void clear() {
      mutabilityOracle.ensureMutable();
      delegate.clear();
    }

    public Set<K> keySet() {
      return new MutabilityAwareSet<K>(mutabilityOracle, delegate.keySet());
    }

    public Collection<V> values() {
      return new MutabilityAwareCollection<V>(mutabilityOracle, delegate.values());
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
      return new MutabilityAwareSet<Entry<K, V>>(mutabilityOracle, delegate.entrySet());
    }

    public boolean equals(
            Object o) {
      return delegate.equals(o);
    }

    public int hashCode() {
      return delegate.hashCode();
    }

    public String toString() {
      return delegate.toString();
    }

    /** An internal collection that checks for mutability before delegating. */
    private static class MutabilityAwareCollection<E> implements Collection<E> {
      private final MutabilityOracle mutabilityOracle;
      private final Collection<E> delegate;

      MutabilityAwareCollection(MutabilityOracle mutabilityOracle, Collection<E> delegate) {
        this.mutabilityOracle = mutabilityOracle;
        this.delegate = delegate;
      }

      public int size() {
        return delegate.size();
      }

      public boolean isEmpty() {
        return delegate.isEmpty();
      }

      public boolean contains(Object o) {
        return delegate.contains(o);
      }

      public Iterator<E> iterator() {
        return new MutabilityAwareIterator<E>(mutabilityOracle, delegate.iterator());
      }

      public Object[] toArray() {
        return delegate.toArray();
      }

      public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
      }

      public boolean add(E e) {
        // Unsupported operation in the delegate.
        throw new UnsupportedOperationException();
      }

      public boolean remove(Object o) {
        mutabilityOracle.ensureMutable();
        return delegate.remove(o);
      }

      public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
      }

      public boolean addAll(Collection<? extends E> c) {
        // Unsupported operation in the delegate.
        throw new UnsupportedOperationException();
      }

      public boolean removeAll(Collection<?> c) {
        mutabilityOracle.ensureMutable();
        return delegate.removeAll(c);
      }

      public boolean retainAll(Collection<?> c) {
        mutabilityOracle.ensureMutable();
        return delegate.retainAll(c);
      }

      public void clear() {
        mutabilityOracle.ensureMutable();
        delegate.clear();
      }

      public boolean equals(
              Object o) {
        return delegate.equals(o);
      }

      public int hashCode() {
        return delegate.hashCode();
      }

      public String toString() {
        return delegate.toString();
      }
    }

    /** An internal set that checks for mutability before delegating. */
    private static class MutabilityAwareSet<E> implements Set<E> {
      private final MutabilityOracle mutabilityOracle;
      private final Set<E> delegate;

      MutabilityAwareSet(MutabilityOracle mutabilityOracle, Set<E> delegate) {
        this.mutabilityOracle = mutabilityOracle;
        this.delegate = delegate;
      }

      public int size() {
        return delegate.size();
      }

      public boolean isEmpty() {
        return delegate.isEmpty();
      }

      public boolean contains(Object o) {
        return delegate.contains(o);
      }

      public Iterator<E> iterator() {
        return new MutabilityAwareIterator<E>(mutabilityOracle, delegate.iterator());
      }

      public Object[] toArray() {
        return delegate.toArray();
      }

      public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
      }

      public boolean add(E e) {
        mutabilityOracle.ensureMutable();
        return delegate.add(e);
      }

      public boolean remove(Object o) {
        mutabilityOracle.ensureMutable();
        return delegate.remove(o);
      }

      public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
      }

      public boolean addAll(Collection<? extends E> c) {
        mutabilityOracle.ensureMutable();
        return delegate.addAll(c);
      }

      public boolean retainAll(Collection<?> c) {
        mutabilityOracle.ensureMutable();
        return delegate.retainAll(c);
      }

      public boolean removeAll(Collection<?> c) {
        mutabilityOracle.ensureMutable();
        return delegate.removeAll(c);
      }

      public void clear() {
        mutabilityOracle.ensureMutable();
        delegate.clear();
      }

      public boolean equals(
              Object o) {
        return delegate.equals(o);
      }

      public int hashCode() {
        return delegate.hashCode();
      }

      public String toString() {
        return delegate.toString();
      }
    }

    /** An internal iterator that checks for mutability before delegating. */
    private static class MutabilityAwareIterator<E> implements Iterator<E> {
      private final MutabilityOracle mutabilityOracle;
      private final Iterator<E> delegate;

      MutabilityAwareIterator(MutabilityOracle mutabilityOracle, Iterator<E> delegate) {
        this.mutabilityOracle = mutabilityOracle;
        this.delegate = delegate;
      }

      public boolean hasNext() {
        return delegate.hasNext();
      }

      public E next() {
        return delegate.next();
      }

      public void remove() {
        mutabilityOracle.ensureMutable();
        delegate.remove();
      }

      public boolean equals(
              Object obj) {
        return delegate.equals(obj);
      }

      public int hashCode() {
        return delegate.hashCode();
      }

      public String toString() {
        return delegate.toString();
      }
    }
  }
}
