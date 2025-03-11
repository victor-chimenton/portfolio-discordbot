package com.victorchimenton.core.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AsyncOperationUtil {

  private AsyncOperationUtil() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  public static <T, R> CompletableFuture<R> executeAsync(
      T param, Function<T, R> operation, String successLogMessage, String errorLogMessage) {

    return CompletableFuture.supplyAsync(
            () -> {
              R result = operation.apply(param);
              log.debug(successLogMessage);
              return result;
            })
        .exceptionally(
            ex -> {
              log.error(errorLogMessage, ex);
              throw new CompletionException(ex);
            });
  }
}
