package com.victorchimenton.bot.listener;

import com.victorchimenton.bot.service.ModerationService;
import com.victorchimenton.core.model.GuildConfig;
import com.victorchimenton.core.service.GuildConfigService;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {

  private final GuildConfigService guildConfigService;
  private final ModerationService moderationService;

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot() || !event.isFromGuild()) {
      return;
    }

    var guild = event.getGuild();
    var member = event.getMember();
    var message = event.getMessage();

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
                      if (moderationService.checkMessage(message, member, config)) {
                        message
                            .delete()
                            .queue(
                                success ->
                                    log.debug(
                                        "Deleted message from {}", member.getUser().getAsTag()),
                                error ->
                                    log.warn(
                                        "Failed to delete message from {}",
                                        member.getUser().getAsTag(),
                                        error));
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
