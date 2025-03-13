package com.victorchimenton.core.util;

import com.victorchimenton.core.model.GuildConfig;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigUpdaterUtil {

  public <T> boolean updateIfChanged(
      GuildConfig config,
      String userId,
      String username,
      String fieldName,
      T currentValue,
      T newValue,
      BiPredicate<T, T> comparator,
      Consumer<T> setter) {

    if (comparator.test(currentValue, newValue)) {
      return false;
    }

    setter.accept(newValue);

    config.addEventLog(
        userId, username, fieldName, String.valueOf(currentValue), String.valueOf(newValue));

    log.debug(
        "Updated field '{}' from '{}' to '{}' by user '{}'",
        fieldName,
        currentValue,
        newValue,
        username);

    return true;
  }

  public static BiPredicate<String, String> safeStringEquals() {
    return (s1, s2) -> {
      if (s1 == null && s2 == null) return true;
      if (s1 == null) return s2.trim().isEmpty();
      if (s2 == null) return s1.trim().isEmpty();
      return s1.equals(s2);
    };
  }

  public static BiPredicate<Boolean, Boolean> booleanEquals() {
    return Objects::equals;
  }

  public static <T> BiPredicate<T, T> objectEquals() {
    return Objects::equals;
  }
}
