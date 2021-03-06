package org.infinispan.query.core.impl;

import java.util.NoSuchElementException;
import java.util.function.Function;

import org.infinispan.commons.util.CloseableIterator;

/**
 * A {@link CloseableIterator} decorator that can be sliced and have its elements transformed.
 *
 * @since 11.0
 * @param <S> Type of the original iterator
 * @param <T> Resulting type
 */
public class MappingIterator<S, T> implements CloseableIterator<T> {
   private final CloseableIterator<S> iterator;
   private final Function<? super S, ? extends T> mapper;

   private long skip = 0;
   private long max = -1;

   private S current;
   private long index;

   public MappingIterator(CloseableIterator<S> iterator, Function<? super S, ? extends T> mapper) {
      this.iterator = iterator;
      this.mapper = mapper;
   }

   @Override
   public boolean hasNext() {
      advance();
      return current != null && (max == -1 || index <= skip + max);
   }

   @Override
   public T next() {
      if (hasNext()) {
         T element = mapper.apply(current);
         current = null;
         return element;
      } else {
         throw new NoSuchElementException();
      }
   }

   private void advance() {
      if (current == null) {
         while (index < skip && iterator.hasNext()) {
            iterator.next();
            index++;
         }
         if (iterator.hasNext()) {
            current = iterator.next();
            index++;
         }
      }
   }

   public MappingIterator<S, T> skip(long n) {
      this.skip = n;
      return this;
   }

   public MappingIterator<S, T> limit(long max) {
      this.max = max;
      return this;
   }

   @Override
   public void close() {
      iterator.close();
   }

   @Override
   public void remove() {
      iterator.remove();
   }
}
