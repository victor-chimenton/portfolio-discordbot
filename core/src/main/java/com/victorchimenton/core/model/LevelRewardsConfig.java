package com.victorchimenton.core.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "level_rewards_config")
public class LevelRewardsConfig {
  @Id private String id;

  @Indexed(unique = true)
  private String guildId;

  private boolean enabled = true;
  private long baseXpPerMessage = 15;
  private long minXpPerMessage = 10;
  private long maxXpPerMessage = 25;
  private long xpCooldownSeconds = 60;
  private boolean announceLevel = true;
  private String announcementChannelId;

  private List<LevelReward> rewards = new ArrayList<>();

  private boolean stackRoles = false;

  public LevelRewardsConfig(String guildId) {
    this.guildId = guildId;
  }

  public List<LevelReward> getRewardsForLevel(int level) {
    return rewards.stream().filter(reward -> reward.getLevel() == level).toList();
  }

  public LevelReward findReward(int level, String roleId) {
    return rewards.stream()
        .filter(reward -> reward.getLevel() == level && reward.getRoleId().equals(roleId))
        .findFirst()
        .orElse(null);
  }

  public boolean addReward(int level, String roleId, String message) {
    if (findReward(level, roleId) == null) {
      LevelReward reward = new LevelReward();
      reward.setLevel(level);
      reward.setRoleId(roleId);
      reward.setMessage(message);
      return true;
    }
    return false;
  }

  public boolean removeReward(int level, String roleId) {
    LevelReward reward = findReward(level, roleId);
    if (reward != null) {
      rewards.remove(reward);
      return true;
    }
    return false;
  }

  @Data
  public static class LevelReward {
    private int level;
    private String roleId;
    private String message =
        "Congratulations {user}, you've reached level {level} and earned the {role} role!";
  }
}
