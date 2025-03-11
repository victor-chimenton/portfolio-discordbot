package com.victorchimenton.core.util;

import com.victorchimenton.core.model.GuildConfig;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class ConfigUpdaterUtil {

  public static <T> void updateIfChanged(
      GuildConfig config,
      String userId,
      String username,
      String fieldName,
      T currentValue,
      T newValue,
      BiPredicate<T, T> equalityCheck,
      Consumer<T> setter) {

    if (!equalityCheck.test(currentValue, newValue)) {
      config.addChangeLog(userId, username, fieldName, currentValue, newValue);
      setter.accept(newValue);
    }
  }

  public static BiPredicate<String, String> safeStringEquals() {
    return (s1, s2) -> (s1 == null && s2 == null) || (s1 != null && s1.equals(s2));
  }

  public static BiPredicate<Boolean, Boolean> booleanEquals() {
    return (b1, b2) -> b1 == b2;
  }

  public static <T> BiPredicate<T, T> objectEquals() {
    return Objects::equals;
  }
}
