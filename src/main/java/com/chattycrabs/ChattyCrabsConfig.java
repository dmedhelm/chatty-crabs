package com.chattycrabs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("chattycrabs")
public interface ChattyCrabsConfig extends Config
{
  @ConfigItem(
      keyName = "damageThreshold",
      name = "Damage Threshold",
      description = "Set the highest amount that the chatter appears.")
  default int damageThreshold() {
    return 5;
  }
}
