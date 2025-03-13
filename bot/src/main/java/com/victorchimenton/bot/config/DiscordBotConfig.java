package com.victorchimenton.bot.config;

import com.victorchimenton.bot.listener.GuildListener;
import com.victorchimenton.bot.listener.GuildMemberListener;
import com.victorchimenton.bot.listener.MessageListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DiscordBotConfig {

  @Value("${discord.bot.token}")
  private String botToken;

  private final MessageListener messageListener;
  private final GuildListener guildListener;
  private final GuildMemberListener guildMemberListener;

  @Bean
  public JDA jda() throws Exception {
    log.info("Initializing discord bot...");
    var jda =
        JDABuilder.createDefault(botToken)
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.playing("Serving $help"))
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.MESSAGE_CONTENT)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilter.ALL)
            .addEventListeners(messageListener, guildListener, guildMemberListener)
            .build();

    jda.awaitReady();
    log.info(
        "Discord client initialized successfully! Logged in as {}", jda.getSelfUser().getAsTag());
    return jda;
  }
}
