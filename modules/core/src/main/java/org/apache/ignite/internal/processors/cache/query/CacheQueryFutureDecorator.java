package org.apache.ignite.internal.processors.cache.query;

import org.apache.ignite.IgniteCheckedException;
import org.jetbrains.annotations.Nullable;

public abstract class CacheQueryFutureDecorator<R> implements CacheQueryFuture<R> {
   private final CacheQueryFuture<R> future;

   public CacheQueryFutureDecorator(CacheQueryFuture<R> future) {
      this.future = future;
   }

   @Nullable
   @Override
   public R next() throws IgniteCheckedException {
      return future.next();
   }

   @Override
   public boolean isDone() {
      return future.isDone();
   }

   @Override
   public boolean cancel() throws IgniteCheckedException {
      return future.cancel();
   }
}
