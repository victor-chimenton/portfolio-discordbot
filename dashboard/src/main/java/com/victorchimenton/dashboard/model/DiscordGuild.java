package com.victorchimenton.dashboard.model;

import lombok.Data;

@Data
public class DiscordGuild
{
  private String id;
  private String name;
  private String icon;
  private boolean owner;
  private long permission;

  private Boolean botInGuild;

  public String getIconUrl() {
    if (icon == null) {
      return "https://cdn.discordapp.com/embed/avatars/0.png";
    }
    return "https://cdn.discordapp.com/icons/" + id + "/" + icon + ".png";
  }

  public boolean hasPermissions(String permName) {
    if ("MANAGE_GUILD".equals(permName)) {
      return (permission & 0x0000000020L) == 0x0000000020L;
    }
    if ("ADMINISTRATOR".equals(permName)) {
      return (permission & 0x0000000008L) == 0x0000000008L;
    }
    return false;
  }

  public boolean isBotInGuild(String botId) {
    // implement via discord api
    return true;
  }
}
