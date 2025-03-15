package com.victorchimenton.core.repository;

import com.victorchimenton.core.model.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
  Optional<UserProfile> findByGuildIdUserId(String guildId, String userId);

  Page<UserProfile> findByGuildIdOrderByXpDesc(String guildId, Pageable pageable);

  List<UserProfile> findTop10ByGuildIdOrderByXpDesc(String guildId);

  long countByGuildId(String guildId);

  List<UserProfile> findByGuildIdAndLevel(String guildId, int level);
}
