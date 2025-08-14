package com.chattycrabs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChattyCrabsPluginTest {
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(ChattyCrabsPlugin.class);
    RuneLite.main(args);
  }
}
