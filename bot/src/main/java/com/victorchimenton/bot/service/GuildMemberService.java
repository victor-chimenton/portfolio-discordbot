package com.victorchimenton.bot.service;

import com.victorchimenton.core.model.GuildConfig;
import com.victorchimenton.core.service.GuildConfigService;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuildMemberService {
  private final GuildConfigService guildConfigService;
  private final JDA jda;

  public void handleGuildJoin(Guild guild, User user, Member member) {
    guildConfigService
        .getConfigByGuildId(guild.getId())
        .thenAccept(
            configOpt -> {
              if (configOpt.isPresent()) {
                var config = configOpt.get();
                CompletableFuture.runAsync(
                    () -> {
                      sendWelcomeMessage(guild, user, config);
                      assignAutoRole(guild, member, config);
                    });
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error handling guild join for guild {}", guild.getId(), ex);
              return null;
            });
  }

  public void handleGuildLeave(Guild guild, User user) {
    guildConfigService
        .getConfigByGuildId(guild.getId())
        .thenAccept(
            configOpt -> {
              if (configOpt.isPresent()) {
                var config = configOpt.get();
                CompletableFuture.runAsync(
                    () -> {
                      sendLeaveMessage(guild, user, config);
                    });
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error handling guild leave for guild {}", guild.getId(), ex);
              return null;
            });
  }

  private void sendWelcomeMessage(Guild guild, User user, GuildConfig config) {
    var welcomeConfig = config.getWelcomeConfig();
    if (welcomeConfig.isEnabled() && welcomeConfig.getChannelId() != null) {
      try {
        var channel = guild.getTextChannelById(welcomeConfig.getChannelId());
        if (channel != null) {
          var message = processMessagePlaceholders(welcomeConfig.getMessage(), user, guild);
          channel
              .sendMessage(message)
              .queue(
                  success ->
                      log.debug(
                          "Sent welcome message for user {} in guild {}",
                          user.getAsTag(),
                          guild.getName()),
                  error ->
                      log.warn("Failed to send welcome message in guild {}", guild.getId(), error));
        }
      } catch (Exception e) {
        log.error("Error sending welcome message in guild {}", guild.getId(), e);
      }
    }

    if (welcomeConfig.isSendDm()) {
      try {
        var message = processMessagePlaceholders(welcomeConfig.getDmMessage(), user, guild);
        user.openPrivateChannel()
            .queue(
                channel ->
                    channel
                        .sendMessage(message)
                        .queue(
                            success -> log.debug("Sent welcome DM to user {}", user.getAsTag()),
                            error ->
                                log.warn(
                                    "Failed to send welcome DM to user {}",
                                    user.getAsTag(),
                                    error)),
                error ->
                    log.warn(
                        "Failed to open private channel with user {}", user.getAsTag(), error));
      } catch (Exception e) {
        log.error("Error sending welcome DM to user {}", user.getAsTag(), e);
      }
    }
  }

  private void sendLeaveMessage(Guild guild, User user, GuildConfig config) {
    var leaveConfig = config.getLeaveConfig();
    if (leaveConfig.isEnabled() && leaveConfig.getChannelId() != null) {
      try {
        var channel = guild.getTextChannelById(leaveConfig.getChannelId());
        if (channel != null) {
          var message = processMessagePlaceholders(leaveConfig.getMessage(), user, guild);
          channel
              .sendMessage(message)
              .queue(
                  success ->
                      log.debug(
                          "Sent leave message for user {} in guild {}",
                          user.getAsTag(),
                          guild.getName()),
                  error ->
                      log.warn("Failed to send leave message in guild {}", guild.getId(), error));
        }
      } catch (Exception e) {
        log.error("Error sending leave message in guild {}", guild.getId(), e);
      }
    }
  }

  private void assignAutoRole(Guild guild, Member member, GuildConfig config) {
    var autoRoleId = config.getAutoRoleId();
    if (autoRoleId != null && !autoRoleId.isBlank()) {
      try {
        var role = guild.getRoleById(autoRoleId);
        if (role != null) {
          guild
              .addRoleToMember(member, role)
              .queue(
                  success ->
                      log.debug(
                          "Assigned auto role to user {} in guild {}",
                          member.getUser().getAsTag(),
                          guild.getName()),
                  error ->
                      log.warn("Failed to assign auto role in guild {}", guild.getId(), error));
        }
      } catch (Exception e) {
        log.error("Error assigning auto role in guild {}", guild.getId(), e);
      }
    }
  }

  private String processMessagePlaceholders(String message, User user, Guild guild) {
    return message
        .replace("{user}", user.getAsMention())
        .replace("{username}", user.getName())
        .replace("{server}", guild.getName())
        .replace("{memberCount}", String.valueOf(guild.getMemberCount()));
  }
}
