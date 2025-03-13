package com.victorchimenton.core.service;

import com.victorchimenton.core.model.GuildConfig;
import com.victorchimenton.core.repository.GuildConfigRepository;
import com.victorchimenton.core.util.AsyncOperationUtil;
import com.victorchimenton.core.util.ConfigUpdaterUtil;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuildConfigService {
  private final GuildConfigRepository guildConfigRepository;

  public CompletableFuture<Optional<GuildConfig>> getConfigByGuildId(String guildId) {
    return AsyncOperationUtil.executeAsync(
        guildId,
        guildConfigRepository::findByGuildId,
        "Retrieved configuration for guild " + guildId,
        "Failed to retrieve configuration for guild " + guildId);
  }

  public CompletableFuture<GuildConfig> getOrCreateGuildConfig(String guildId) {
    return getConfigByGuildId(guildId)
        .thenCompose(
            optionalConfig ->
                optionalConfig
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(() -> saveGuildConfig(new GuildConfig(guildId))));
  }

  public CompletableFuture<GuildConfig> saveGuildConfig(GuildConfig config) {
    return AsyncOperationUtil.executeAsync(
        config,
        guildConfigRepository::save,
        "Saved configuration for guild " + config.getGuildId(),
        "Failed to save configuration for guild " + config.getGuildId());
  }

  private CompletableFuture<GuildConfig> updateGuildConfig(
      String guildId, BiConsumer<GuildConfig, ConfigUpdaterUtil> updater) {
    return getConfigByGuildId(guildId)
        .thenCompose(
            optionalConfig ->
                optionalConfig
                    .map(
                        config -> {
                          updater.accept(config, new ConfigUpdaterUtil());
                          return saveGuildConfig(config);
                        })
                    .orElseThrow(
                        () -> {
                          log.error("Configuration not found for guild: {}", guildId);
                          return new IllegalStateException(
                              "Configuration does not exist for guild: " + guildId);
                        }))
        .thenApply(
            updatedConfig -> {
              log.debug("Configuration updated for guild {}", guildId);
              return updatedConfig;
            })
        .exceptionally(
            ex -> {
              log.error("Error updating configuration for guild {}", guildId, ex);
              throw new CompletionException(ex);
            });
  }

  public CompletableFuture<List<GuildConfig.ConfigEventLog>> getGuildConfigEventLog(
      String guildId) {
    return getConfigByGuildId(guildId)
        .thenApply(
            optionalConfig -> optionalConfig.map(GuildConfig::getEventLogs).orElseGet(List::of))
        .exceptionally(
            ex -> {
              log.error("Error retrieving event logs for guild {}", guildId, ex);
              throw new CompletionException(ex);
            });
  }

  public CompletableFuture<GuildConfig> updateGeneralSettings(
      String guildId,
      String userId,
      String username,
      String prefix,
      List<String> blacklistedChannels,
      boolean deleteCommandsAfterExecution) {
    return updateGuildConfig(
        guildId,
        (config, updater) -> {
          updater.updateIfChanged(
              config,
              userId,
              username,
              "prefix",
              config.getPrefix(),
              prefix,
              ConfigUpdaterUtil.objectEquals(),
              config::setPrefix);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "blacklistedCommandChannels",
              config.getBlacklistedCommandChannels(),
              blacklistedChannels,
              ConfigUpdaterUtil.objectEquals(),
              config::setBlacklistedCommandChannels);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "deleteCommandsAfterExecution",
              config.isDeleteCommandsAfterExecution(),
              deleteCommandsAfterExecution,
              ConfigUpdaterUtil.booleanEquals(),
              config::setDeleteCommandsAfterExecution);
        });
  }

  public CompletableFuture<GuildConfig> updateModerationSettings(
      String guildId,
      String userId,
      String username,
      String autoRoleId,
      String muteRoleId,
      boolean spamControl,
      boolean blockLinks,
      boolean blockInvites) {
    return updateGuildConfig(
        guildId,
        (config, updater) -> {
          updater.updateIfChanged(
              config,
              userId,
              username,
              "autoRoleId",
              config.getAutoRoleId(),
              autoRoleId,
              ConfigUpdaterUtil.safeStringEquals(),
              config::setAutoRoleId);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "muteRoleId",
              config.getMuteRoleId(),
              muteRoleId,
              ConfigUpdaterUtil.safeStringEquals(),
              config::setMuteRoleId);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "spamControl",
              config.isSpamControl(),
              spamControl,
              ConfigUpdaterUtil.booleanEquals(),
              config::setSpamControl);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "blockLinks",
              config.isBlockLinks(),
              blockLinks,
              ConfigUpdaterUtil.booleanEquals(),
              config::setBlockLinks);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "blockInvites",
              config.isBlockInvites(),
              blockInvites,
              ConfigUpdaterUtil.booleanEquals(),
              config::setBlockInvites);
        });
  }

  public CompletableFuture<GuildConfig> updateWelcomeConfig(
      String guildId,
      String userId,
      String username,
      boolean enabled,
      String message,
      String channelId,
      boolean sendDm,
      String dmMessage) {
    return updateGuildConfig(
        guildId,
        (config, updater) -> {
          GuildConfig.WelcomeConfig welcomeConfig = config.getWelcomeConfig();
          updater.updateIfChanged(
              config,
              userId,
              username,
              "welcomeConfig.enabled",
              welcomeConfig.isEnabled(),
              enabled,
              ConfigUpdaterUtil.booleanEquals(),
              welcomeConfig::setEnabled);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "welcomeConfig.message",
              welcomeConfig.getMessage(),
              message,
              ConfigUpdaterUtil.safeStringEquals(),
              welcomeConfig::setMessage);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "welcomeConfig.channelId",
              welcomeConfig.getChannelId(),
              channelId,
              ConfigUpdaterUtil.safeStringEquals(),
              welcomeConfig::setChannelId);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "welcomeConfig.sendDm",
              welcomeConfig.isSendDm(),
              sendDm,
              ConfigUpdaterUtil.booleanEquals(),
              welcomeConfig::setSendDm);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "welcomeConfig.dmMessage",
              welcomeConfig.getDmMessage(),
              dmMessage,
              ConfigUpdaterUtil.safeStringEquals(),
              welcomeConfig::setDmMessage);
        });
  }

  public CompletableFuture<GuildConfig> updateLeaveConfig(
      String guildId,
      String userId,
      String username,
      boolean enabled,
      String message,
      String channelId) {
    return updateGuildConfig(
        guildId,
        (config, updater) -> {
          GuildConfig.LeaveConfig leaveConfig = config.getLeaveConfig();
          updater.updateIfChanged(
              config,
              userId,
              username,
              "leaveConfig.enabled",
              leaveConfig.isEnabled(),
              enabled,
              ConfigUpdaterUtil.booleanEquals(),
              leaveConfig::setEnabled);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "leaveConfig.message",
              leaveConfig.getMessage(),
              message,
              ConfigUpdaterUtil.safeStringEquals(),
              leaveConfig::setMessage);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "leaveConfig.channelId",
              leaveConfig.getChannelId(),
              channelId,
              ConfigUpdaterUtil.safeStringEquals(),
              leaveConfig::setChannelId);
        });
  }

  public CompletableFuture<GuildConfig> updateLogSettings(
      String guildId,
      String userId,
      String username,
      boolean enabled,
      String channelId,
      boolean logJoin,
      boolean logLeave,
      boolean logMessage,
      boolean logCommand,
      boolean logPunishment) {
    return updateGuildConfig(
        guildId,
        (config, updater) -> {
          GuildConfig.LogConfig logConfig = config.getLogConfig();
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.enabled",
              logConfig.isEnabled(),
              enabled,
              ConfigUpdaterUtil.booleanEquals(),
              logConfig::setEnabled);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.channelId",
              logConfig.getChannelId(),
              channelId,
              ConfigUpdaterUtil.safeStringEquals(),
              logConfig::setChannelId);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.logJoin",
              logConfig.isLogJoin(),
              logJoin,
              ConfigUpdaterUtil.booleanEquals(),
              logConfig::setLogJoin);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.logLeave",
              logConfig.isLogLeave(),
              logLeave,
              ConfigUpdaterUtil.booleanEquals(),
              logConfig::setLogLeave);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.logMessage",
              logConfig.isLogMessage(),
              logMessage,
              ConfigUpdaterUtil.booleanEquals(),
              logConfig::setLogMessage);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.logCommand",
              logConfig.isLogCommand(),
              logCommand,
              ConfigUpdaterUtil.booleanEquals(),
              logConfig::setLogCommand);
          updater.updateIfChanged(
              config,
              userId,
              username,
              "logConfig.logPunishment",
              logConfig.isLogPunishment(),
              logPunishment,
              ConfigUpdaterUtil.booleanEquals(),
              logConfig::setLogPunishment);
        });
  }
}
