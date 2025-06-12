// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * An implementation of {@link LazyStringList} that wraps another {@link LazyStringList} such that
 * it cannot be modified via the wrapper.
 *
 * @author jonp@google.com (Jon Perlow)
 * @deprecated use {@link LazyStringArrayList#makeImmutable} instead.
 */
@Deprecated
public class UnmodifiableLazyStringList extends AbstractList<String>
    implements LazyStringList, RandomAccess {

  private final LazyStringList list;

  public UnmodifiableLazyStringList(LazyStringList list) {
    this.list = list;
  }

  public String get(int index) {
    return list.get(index);
  }

  public Object getRaw(int index) {
    return list.getRaw(index);
  }

  public int size() {
    return list.size();
  }

  public ByteString getByteString(int index) {
    return list.getByteString(index);
  }

  public void add(ByteString element) {
    throw new UnsupportedOperationException();
  }

  public void set(int index, ByteString element) {
    throw new UnsupportedOperationException();
  }

  public boolean addAllByteString(Collection<? extends ByteString> element) {
    throw new UnsupportedOperationException();
  }

  public byte[] getByteArray(int index) {
    return list.getByteArray(index);
  }

  public void add(byte[] element) {
    throw new UnsupportedOperationException();
  }

  public void set(int index, byte[] element) {
    throw new UnsupportedOperationException();
  }

  public boolean addAllByteArray(Collection<byte[]> element) {
    throw new UnsupportedOperationException();
  }

  public ListIterator<String> listIterator(final int index) {
    return new ListIterator<String>() {
      ListIterator<String> iter = list.listIterator(index);

      public boolean hasNext() {
        return iter.hasNext();
      }

      public String next() {
        return iter.next();
      }

      public boolean hasPrevious() {
        return iter.hasPrevious();
      }

      public String previous() {
        return iter.previous();
      }

      public int nextIndex() {
        return iter.nextIndex();
      }

      public int previousIndex() {
        return iter.previousIndex();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      public void set(String o) {
        throw new UnsupportedOperationException();
      }

      public void add(String o) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public Iterator<String> iterator() {
    return new Iterator<String>() {
      Iterator<String> iter = list.iterator();

      public boolean hasNext() {
        return iter.hasNext();
      }

      public String next() {
        return iter.next();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public List<?> getUnderlyingElements() {
    // The returned value is already unmodifiable.
    return list.getUnderlyingElements();
  }

  public void mergeFrom(LazyStringList other) {
    throw new UnsupportedOperationException();
  }

  public List<byte[]> asByteArrayList() {
    return Collections.unmodifiableList(list.asByteArrayList());
  }

  public List<ByteString> asByteStringList() {
    return Collections.unmodifiableList(list.asByteStringList());
  }

  public LazyStringList getUnmodifiableView() {
    return this;
  }
}
