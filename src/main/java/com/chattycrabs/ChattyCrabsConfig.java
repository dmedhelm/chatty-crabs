package com.chattycrabs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chattycrabs")
public interface ChattyCrabsConfig extends Config
{
  @ConfigItem(
      keyName = "displayChatMessage",
      name = "Display Chat Message",
      description = "If true, the crab taunt will show up in chat.")
  default boolean displayChatMessage() {
    return false;
  }
}
