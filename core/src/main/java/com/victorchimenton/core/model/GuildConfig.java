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
@Document(collection = "guild_config")
public class GuildConfig {
  @Id private String id;

  @Indexed(unique = true)
  private String guildId;

  /* General Settings */
  private String prefix = "$";
  private List<String> blacklistedCommandChannels = new ArrayList<>();
  private boolean deleteCommandsAfterExecution;

  /* Moderation Settings */
  private String autoRoleId;
  private String muteRoleId;
  private boolean spamControl;
  private boolean blockLinks;
  private boolean blockInvites;

  /* Welcome/Leave Settings */
  private WelcomeConfig welcomeConfig = new WelcomeConfig();
  private LeaveConfig leaveConfig = new LeaveConfig();

  /* Logs */
  private LogConfig logConfig = new LogConfig();

  /* AuditLog */
  private List<ConfigEventLog> eventLogs = new ArrayList<>();

  public GuildConfig(String guildId) {
    this.guildId = guildId;
  }

  public void addEventLog(
      String userId, String username, String field, Object oldValue, Object newValue) {
    ConfigEventLog log = new ConfigEventLog();
    log.setUserId(userId);
    log.setUsername(username);
    log.setField(field);
    log.setOldValue(oldValue != null ? oldValue.toString() : null);
    log.setNewValue(newValue != null ? newValue.toString() : null);
    log.setTimestamp(System.currentTimeMillis());

    if (this.eventLogs == null) {
      this.eventLogs = new ArrayList<>();
    }

    this.eventLogs.add(0, log);

    if (this.eventLogs.size() > 100) {
      this.eventLogs = new ArrayList<>(this.eventLogs.subList(0, 100));
    }
  }

  @Data
  public static class WelcomeConfig {
    private boolean enabled;
    private String message = "Welcome {user} to {server}!";
    private String channelId;
    private boolean sendDm;
    private String dmMessage = "Welcome {user} to {server}!";
  }

  @Data
  public static class LeaveConfig {
    private boolean enabled;
    private String message = "{user} left the server!";
    private String channelId;
  }

  @Data
  public static class LogConfig {
    private boolean enabled;
    private String channelId;
    private boolean logJoin = true;
    private boolean logLeave = true;
    private boolean logMessage = true;
    private boolean logCommand = true;
    private boolean logPunishment = true;
  }

  @Data
  public static class ConfigEventLog {
    private String userId;
    private String username;
    private String field;
    private String oldValue;
    private String newValue;
    private long timestamp;
  }
}
