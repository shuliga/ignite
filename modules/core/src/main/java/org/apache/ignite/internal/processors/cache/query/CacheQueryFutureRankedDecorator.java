package org.apache.ignite.internal.processors.cache.query;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.lang.IgniteInClosure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CacheQueryFutureRankedDecorator<K, V, R> extends CacheQueryFutureDecorator<R> {
   private final GridCacheQueryFutureAdapter<K, V, R> future;
   private final Comparator<R> comparator;
   private final BlockingIterator<R> iterator;

   public CacheQueryFutureRankedDecorator(GridCacheQueryFutureAdapter<K, V, R> future,
                                          @NotNull Comparator<R> comparator) {
      super(future);
      this.future = future;
      this.comparator = comparator;
      this.iterator = blockingIteratorImpl();
   }

   /**
    * Blocking iterator that waits while all items will be returned from nodes
    * @return Iterator<R>
    */
   private BlockingIterator<R> blockingIteratorImpl() {
      final int limit = future.query().query().limit();
      return new BlockingIterator<R>() {
         R next;
         int counter;
         boolean released;
         Queue<R> queue = new PriorityQueue<>(comparator.reversed());

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
            // Also if limit is exceeded iteration must be stopped
            return counter < limit && (this.next = queue.poll()) != null;
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

   @Override
   public void close() throws Exception {
      future.close();
   }

   @Override
   public Collection<R> get() throws IgniteCheckedException {
      return future.get();
   }

   @Override
   public Collection<R> get(long timeout) throws IgniteCheckedException {
      return future.get(timeout);
   }

   @Override
   public Collection<R> get(long timeout, TimeUnit unit) throws IgniteCheckedException {
      return future.get(timeout, unit);
   }

   @Override
   public Collection<R> getUninterruptibly() throws IgniteCheckedException {
      return future.getUninterruptibly();
   }

   @Override
   public boolean isCancelled() {
      return future.isCancelled();
   }

   @Override
   public void listen(IgniteInClosure<? super IgniteInternalFuture<Collection<R>>> lsnr) {
      future.listen(lsnr);
   }

   @Override
   public <T> IgniteInternalFuture<T> chain(IgniteClosure<? super IgniteInternalFuture<Collection<R>>, T> doneCb) {
      return future.chain(doneCb);
   }

   @Override
   public <T> IgniteInternalFuture<T> chain(IgniteClosure<? super IgniteInternalFuture<Collection<R>>, T> doneCb,
                                            Executor exec) {
      return future.chain(doneCb, exec);
   }

   @Override
   public Throwable error() {
      return future.error();
   }

   @Override
   public Collection<R> result() {
      return future.result();
   }

   private interface BlockingIterator<E> extends Iterator<E> {
      void await();
   }
}
