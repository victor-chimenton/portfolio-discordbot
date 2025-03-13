package com.victorchimenton.core.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.victorchimenton.core.model.GuildConfig;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GuildConfigCacheService {
  private final Cache<String, Optional<GuildConfig>> guildConfigCache;

  public GuildConfigCacheService() {
    this.guildConfigCache =
        CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))
            .maximumSize(1000)
            .recordStats()
            .build();
  }

  public Optional<GuildConfig> getConfig(String guildId) {
    try {
      return guildConfigCache.getIfPresent(guildId);
    } catch (Exception ex) {
      log.warn("Failed to retrieve guild config from cache for guild: {}", guildId, ex);
      return Optional.empty();
    }
  }

  public void cacheConfig(String guildId, Optional<GuildConfig> config) {
    guildConfigCache.put(guildId, config);
  }

  public void invalidateConfig(String guildId) {
    guildConfigCache.invalidate(guildId);
  }

  public void invalidateAll() {
    guildConfigCache.invalidateAll();
  }

  public String getCacheStats() {
    var stats = guildConfigCache.stats();
    return String.format(
        "Cache Stats: Hits=%d, Misses=%d, Hit Rate=%.2f%%, Size=%d",
        stats.hitCount(), stats.missCount(), stats.hitRate() * 100, guildConfigCache.size());
  }
}
