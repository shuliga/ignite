package org.apache.ignite.internal.processors.cache.query;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.IgniteInterruptedCheckedException;
import org.apache.ignite.internal.processors.cache.GridCacheContext;

import java.util.UUID;

/**
 * CacheQueryFutureDecorator
 * @param <K> key
 * @param <V> value
 * @param <R> result type
 */
public abstract class CacheQueryFutureDecorator<K, V, R> extends GridCacheQueryFutureAdapter<K, V, R> {
   private final GridCacheQueryFutureAdapter<K, V, R>  future;

   public CacheQueryFutureDecorator(GridCacheQueryFutureAdapter<K, V, R> future) {
      this.future = future;
   }

   protected CacheQueryFutureDecorator(GridCacheContext<K, V> cctx, GridCacheQueryBean qry, boolean loc,
                                       GridCacheQueryFutureAdapter<K, V, R> future) {
      super(cctx, qry, loc);
      this.future = future;
   }

   @Override
   public void awaitFirstPage() throws IgniteCheckedException {
      future.awaitFirstPage();
   }

   @Override
   protected boolean onPage(UUID nodeId, boolean last) {
      return future.onPage(nodeId, last);
   }

   @Override
   protected void loadPage() {
      future.loadPage();
   }

   @Override
   protected void loadAllPages() throws IgniteInterruptedCheckedException {
      future.loadAllPages();
   }

   @Override
   protected void cancelQuery() throws IgniteCheckedException {
      future.cancelQuery();
   }
}
