package org.apache.ignite.internal.processors.cache.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * CacheQueryFutureRankedDecorator decorates CacheQueryFutureAdapter for collection results
 * in {@link PriorityQueue} which actually is implementation of Max Tree.
 *
 * Decorates {@link GridCacheQueryFutureAdapter#next()} using {@link BlockingIterator#await()}
 * Firstly a future completes an asynchronous task and results are collected to the queue
 * Then iterator slices the queue if limitIsDisabled is {@code true}
 * otherwise returns sorted queue in descending order
 * @param <K> - key
 * @param <V> - value
 * @param <R> - result
 */
public class CacheQueryFutureRankedDecorator<K, V, R> extends CacheQueryFutureDecorator<K, V, R> {
   private final GridCacheQueryFutureAdapter<K, V, R> future;
   private final BlockingIterator<R> iterator;
   private final Comparator<R> comparator;
   private final int limit;
   private boolean isLimitDisabled;

   public CacheQueryFutureRankedDecorator(GridCacheQueryFutureAdapter<K, V, R> future,
                                          @NotNull Comparator<R> comparator) {
      super(future);
      this.future = future;
      this.comparator = comparator;
      this.limit = future.query().query().limit();
      this.isLimitDisabled = 0 >= this.limit;
      this.iterator = blockingIteratorImpl();
   }

   /**
    * Blocking iterator that waits while all items will be returned from a future
    * @return Simple {@link Iterator}
    */
   private BlockingIterator<R> blockingIteratorImpl() {
      return new BlockingIterator<R>() {
         R next;
         int counter;
         boolean released;
         private final Queue<R> queue = new PriorityQueue<>(comparator.reversed());

         @Override
         public void await() {
            if (released) {
               return;
            }
            // Adds the counted value to priority queue (max tree)
            R val;
            while ((val = future.next()) != null) {
               queue.add(val);
            }
            released = true;
         }

         @Override
         public boolean hasNext() {
            // Polling item and check that it's not null
            // Also if actualLimit is exceeded iteration must be stopped
            next = queue.poll();
            return isLimitDisabled ? next != null : counter < limit && next != null;
         }

         @Override
         public R next() {
            counter++;
            return this.next;
         }
      };
   }

   @Nullable
   @Override
   public R next() {
      iterator.await();
      return iterator.hasNext() ? iterator.next() : null;
   }

   private interface BlockingIterator<E> extends Iterator<E> {
      void await();
   }
}
