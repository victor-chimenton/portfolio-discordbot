package com.victorchimenton.bot.listener;

import com.victorchimenton.bot.service.GuildMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuildMemberListener extends ListenerAdapter {

  private final GuildMemberService guildMemberService;

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    var guild = event.getGuild();
    var user = event.getUser();
    var member = event.getMember();

    log.info("Member joined: {} in guild {}", user.getAsTag(), guild.getName());

    guildMemberService.handleGuildJoin(guild, user, member);
  }

  @Override
  public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
    var guild = event.getGuild();
    var user = event.getUser();

    log.info("Member left: {} from guild {}", user.getAsTag(), guild.getName());

    guildMemberService.handleGuildLeave(guild, user);
  }
}
