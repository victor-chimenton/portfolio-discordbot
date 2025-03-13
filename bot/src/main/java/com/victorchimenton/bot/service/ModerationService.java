package com.victorchimenton.bot.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.victorchimenton.core.model.GuildConfig;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationService {
  private static final Pattern INVITE_PATTERN =
      Pattern.compile("(discord\\.gg|discordapp\\.com/invite)/[a-zA-Z0-9-]+");
  private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

  private final Map<String, Cache<String, AtomicInteger>> messageCounters =
      new ConcurrentHashMap<>();

  public boolean checkMessage(Message message, Member member, GuildConfig config) {
    if (member == null || member.getUser().isBot()) {
      return false;
    }

    var guildId = message.getGuild().getId();
    var userId = member.getId();
    var content = message.getContentRaw();
    var deleteMessage = false;

    if (config.isSpamControl()
        && !member.isOwner()
        && !member.hasPermission(Permission.ADMINISTRATOR)) {
      var counter = getMessageCounter(guildId, userId);
      int messageCount = counter.incrementAndGet();

      if (messageCount > 5) {
        log.debug(
            "Potential spam detected from user {} in guild {}",
            member.getUser().getAsTag(),
            message.getGuild().getName());
        deleteMessage = true;
      }
    }

    if (!deleteMessage
        && config.isBlockInvites()
        && !member.isOwner()
        && !member.hasPermission(Permission.ADMINISTRATOR)) {
      if (INVITE_PATTERN.matcher(content).find()) {
        log.debug(
            "Blocked Discord invite from {} in guild {}",
            member.getUser().getAsTag(),
            message.getGuild().getName());
        deleteMessage = true;
      }
    }
    if (!deleteMessage
        && config.isBlockLinks()
        && !member.isOwner()
        && !member.hasPermission(Permission.ADMINISTRATOR)) {
      if (URL_PATTERN.matcher(content).find()) {
        log.debug(
            "Blocked URL from {} in guild {}",
            member.getUser().getAsTag(),
            message.getGuild().getName());
        deleteMessage = true;
      }
    }

    return deleteMessage;
  }

  private AtomicInteger getMessageCounter(String guildId, String userId) {
    var guildCache =
        messageCounters.computeIfAbsent(
            guildId,
            id -> CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(5)).build());

    try {
      return guildCache.get(userId, () -> new AtomicInteger(0));
    } catch (Exception e) {
      log.error("Error getting message counter for user {} in guild {}", userId, guildId, e);
      return new AtomicInteger(0);
    }
  }
}
