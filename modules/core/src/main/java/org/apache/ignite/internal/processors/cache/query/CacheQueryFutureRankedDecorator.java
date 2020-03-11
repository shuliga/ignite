package org.apache.ignite.internal.processors.cache.query;

import java.util.Collection;
import java.util.NoSuchElementException;
import org.apache.ignite.cluster.ClusterNode;
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
 * Decorates {@link GridCacheQueryFutureAdapter#next()} using {@link PagingSortedIterator#await()}
 * Firstly a future completes an asynchronous task and results are collected to the queue
 * Then iterator slices the queue if limitIsDisabled is {@code true}
 * otherwise returns sorted queue in descending order
 * @param <K> - key
 * @param <V> - value
 * @param <R> - result
 */
public class CacheQueryFutureRankedDecorator<K, V, R> extends CacheQueryFutureDecorator<K, V, R> {
   private final GridCacheQueryFutureAdapter<K, V, R> future;
   private final PagingSortedIterator<R> iterator;
   private final Comparator<R> comparator;
   private final int limit;
   private final int allPagesSize;
   private boolean isLimitDisabled;

   public CacheQueryFutureRankedDecorator(GridCacheQueryFutureAdapter<K, V, R> future,
                                          @NotNull Comparator<R> comparator) {
      super(future);
      this.future = future;
      this.comparator = comparator;
      this.allPagesSize = future.query().query().pageSize() * future.query().query().nodes().size();
      this.limit = future.query().query().limit();
      this.isLimitDisabled = 0 >= this.limit;
      this.iterator = pagingSortedIteratorImpl();
   }

   /**
    * Iterator that waits while first page items will be returned from all nodes.
    * All incoming items are sorted by provided comparator.
    * This is a Merge/Sort algorithm implementation that guarantee ordered response to be prepared as soon as possible.
    * @return Simple {@link Iterator}
    */
   private PagingSortedIterator<R> pagingSortedIteratorImpl() {
      return new PagingSortedIterator<R>() {
         R next;
         int counter;
         int allPagesCounter;
         boolean released;
         private final Queue<R> queue = new PriorityQueue<>(comparator.reversed());

         @Override
         public void await() {
            if (released) {
               return;
            }
            // Adds the counted value to priority queue (max tree)
            R val;
            while ((val = future.next()) != null && allPagesCounter++ <= allPagesSize) {
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
            allPagesCounter--;
            if (!isLimitDisabled && counter > limit)
               throw new NoSuchElementException("Cannot iterate over queue limit (" + limit + ")");
            return next;
         }
      };
   }

   @Nullable
   @Override
   public R next() {
      iterator.await();
      return iterator.hasNext() ? iterator.next() : null;
   }

   private interface PagingSortedIterator<E> extends Iterator<E> {
      void await();
   }
}
