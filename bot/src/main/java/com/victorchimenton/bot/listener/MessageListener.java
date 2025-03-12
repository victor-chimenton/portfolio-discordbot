package com.victorchimenton.bot.listener;

import com.victorchimenton.core.model.GuildConfig;
import com.victorchimenton.core.service.GuildConfigService;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {

  private final GuildConfigService guildConfigService;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot() || !event.isFromGuild()) {
      return;
    }

    Guild guild = event.getGuild();
    Member member = event.getMember();
    Message message = event.getMessage();

    if (member == null) {
      return;
    }

    guildConfigService
        .getConfigByGuildId(guild.getId())
        .thenAccept(
            guildConfig -> {
              if (guildConfig.isPresent()) {
                GuildConfig config = guildConfig.get();

                CompletableFuture.runAsync(
                    () -> {
                      if (config.isBlockInvites()
                          && !member.hasPermission(Permission.ADMINISTRATOR)) {
                        String content = message.getContentRaw();
                        if (content.matches(".*discord.gg/[a-zA-Z0-9]+.*")
                            || content.matches(".*discordapp.com/invite/[a-zA-Z0-9]+.*")) {
                          message.delete().queue();
                          log.debug(
                              "Deleted message with invite from {}", member.getUser().getAsTag());
                        }
                      }

                      if (config.isBlockLinks()
                          && !member.hasPermission(Permission.ADMINISTRATOR)) {
                        String content = message.getContentRaw();
                        if (content.matches(".*https?://.*")) {
                          message.delete().queue();
                          log.debug(
                              "Deleted message with link from {}", member.getUser().getAsTag());
                        }
                      }
                    });
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error processing message in guild {}", guild.getId(), ex);
              return null;
            });
  }
}
