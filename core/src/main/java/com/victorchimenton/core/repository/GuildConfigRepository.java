package com.victorchimenton.core.repository;

import com.victorchimenton.core.model.GuildConfig;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildConfigRepository extends MongoRepository<GuildConfig, String> {
  Optional<GuildConfig> findByGuildId(String guildId);

  boolean existsByGuildId(String guildId);
}
