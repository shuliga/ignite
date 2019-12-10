package org.apache.ignite.internal.processors.cache.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

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
      this.limit = actualLimit();
      this.isLimitDisabled = 0 >= this.limit;
      this.iterator = blockingIteratorImpl();
   }

   private int actualLimit() {
      int numberOfNodes = future.cctx.discovery().size() - 1;
      int limit = future.query().query().limit();

      return limit / numberOfNodes;
   }

   /**
    * Blocking iterator that waits while all items will be returned from nodes
    * @return Iterator<R>
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
