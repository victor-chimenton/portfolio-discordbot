package com.victorchimenton.bot.listener;

import com.victorchimenton.core.service.GuildConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GuildListener extends ListenerAdapter {

  private final GuildConfigService guildConfigService;

  @Override
  public void onGuildJoin(GuildJoinEvent event) {
    Guild guild = event.getGuild();
    String guildId = guild.getId();
    log.info("Bot joined a new guild: {} ({})", guild.getName(), guildId);

    guildConfigService
        .getOrCreateGuildConfig(guild.getId())
        .thenAccept(config -> log.info("Created new config for guild: {}", guild.getId()))
        .exceptionally(
            ex -> {
              log.error("Failed to create config for guild: {}", guild.getId(), ex);
              return null;
            });
  }

  @Override
  public void onGuildLeave(GuildLeaveEvent event) {
    log.info("Bot left guild: {} ({})", event.getGuild().getName(), event.getGuild().getId());
  }
}
