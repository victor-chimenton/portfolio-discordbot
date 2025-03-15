package com.victorchimenton.core.repository;

import com.victorchimenton.core.model.LevelRewardsConfig;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRewardsRepository extends MongoRepository<LevelRewardsConfig, String> {
  Optional<LevelRewardsConfig> findByGuildId(String guildId);

  boolean existsByGuildId(String guildId);
}
