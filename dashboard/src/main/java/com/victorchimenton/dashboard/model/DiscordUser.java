package com.victorchimenton.dashboard.model;

import lombok.Data;

@Data
public class DiscordUser {
  private String id;
  private String username;
  private String avatar;
  private String locale;
  private boolean verified;
  private String email;
  private int flags;

  public String getAvatarUrl() {
    if (avatar == null) {
      return "https://cdn.discordapp.com/embed/avatars/0.png";
    }
    return "https://cdn.discordapp.com/avatars/" + id + "/" + avatar + ".png";
  }
}
