package com.victorchimenton.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "user_profiles")
@CompoundIndex(name = "guild_user_idx", def = "{'guildId': 1, 'userId': 1}", unique = true)
public class UserProfile {
  @Id private String id;

  private String guildId;
  private String userId;
  private String username;

  private long xp;
  private long level;
  private long messageCount;
  private long lastActiveTimestamp;

  private List<String> earnedRoleIds = new ArrayList<>();

  private Map<String, Integer> activityStats = new HashMap<>();

  public UserProfile(String guildId, String userId, String username) {
    this.guildId = guildId;
    this.userId = userId;
    this.username = username;
    this.xp = 0;
    this.level = 0;
    this.messageCount = 0;
    this.lastActiveTimestamp = System.currentTimeMillis();
  }

  public boolean addXp(long amount) {
    int oldLevel = (int) this.level;
    this.xp += amount;
    this.level = calculateLevel(this.xp);
    return this.level > oldLevel;
  }

  private int calculateLevel(long xp) {
    return (int) Math.floor(Math.sqrt(xp / 100.0));
  }

  public long getXpForNextLevel() {
    int nextLevel = (int) (this.level + 1);
    return (long) nextLevel * nextLevel * 100;
  }

  public int getLevelProgress() {
    long xpForCurrentLevel = (long) (this.level * this.level * 100);
    long xpForNextLevel = getXpForNextLevel();

    long xpInCurrentLevel = this.xp - xpForCurrentLevel;
    long xpRequiredForNextLevel = xpForNextLevel - xpForCurrentLevel;

    return (int) (xpInCurrentLevel * 100 / xpRequiredForNextLevel);
  }

  public void recordActivity(String activityType) {
    this.messageCount++;
    this.lastActiveTimestamp = System.currentTimeMillis();

    this.activityStats.compute(activityType, (k, v) -> (v == null) ? 1 : v + 1);
  }

  public boolean addEarnedRole(String roleId) {
    if (!this.earnedRoleIds.contains(roleId)) {
      this.earnedRoleIds.add(roleId);
      return true;
    }
    return false;
  }
}
