package com.chattycrabs;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@PluginDescriptor(
    name = "Chatty Crabs",
    description = "Crabs taunt you when you deal low damage",
    tags = {"fun", "npc", "chat"})
public class ChattyCrabsPlugin extends Plugin {
  @Inject Client client;

  @Inject private ChattyCrabsConfig config;

  @Inject private OverlayManager overlayManager;

  private final Map<Integer, String> npcTaunts = new HashMap<>();
  private final Random random = new Random();

  private NPC targetCrab = null;
  private int tauntExpireTick = 0;
  private int damageThreshold = 5;

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  List<String> baseTaunts =
      Arrays.asList(
          "Is that all you got?",
          "Pathetic!",
          "Weak as your nan's tea.",
          "You'll have to try harder than that!",
          "That tickled.",
          "sit rat",
          "'Tis but a scratch.",
          "I'll send ya back to lumby for that!",
          "You havin' a laugh?",
          "zit zoggy!",
          "???");

  List<String> deathTaunts =
      Arrays.asList(
          "Free tele ty",
          "gf lol",
          "Back 2 lumby smh",
          "Wait",
          "brb",
          "close one",
          "ggz",
          "oof",
          "AHHHHHHHHH");

  List<String> missTaunts =
      Arrays.asList(
          "Swing and a miss!",
          "Go on, give us a real swing.",
          "Is that your best, mate?",
          "Bless ya, you tried.",
          "Whoosh!",
          "I'm starting to feel sorry for ya...");

  @Subscribe
  public void onHitsplatApplied(HitsplatApplied event) {
    if (!(event.getActor() instanceof NPC)) {
      return;
    }

    NPC npc = (NPC) event.getActor();

    if (!npc.getName().toLowerCase().contains("crab")) {
      return;
    }

    if(!event.getHitsplat().isMine()) {
      return;
    }

    if (tauntExpireTick + 5 < client.getTickCount()) {
      int damage = event.getHitsplat().getAmount();

      String taunt = "";
      if (damage == 0) {
        taunt = getRandomTaunt(missTaunts);
      } else if (damage <= damageThreshold) {
        taunt = getRandomTaunt(baseTaunts);
      }

      npc.setOverheadText(taunt);
      client.addChatMessage(ChatMessageType.PUBLICCHAT, npc.getName(), taunt, npc.getName());
      tauntExpireTick = client.getTickCount() + 5;
      targetCrab = npc;
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (targetCrab != null && client.getTickCount() >= tauntExpireTick) {
      targetCrab.setOverheadText(null);
      targetCrab = null;
    }
  }

  public String getRandomTaunt(List<String> tauntList) {
    return tauntList.get(random.nextInt(tauntList.size()));
  }

  @Subscribe
  public void onActorDeath(ActorDeath event) {
    NPC npc = (NPC) event.getActor();
    if (npc == targetCrab) {
      String taunt = getRandomTaunt(deathTaunts);
      npc.setOverheadText(taunt);
      client.addChatMessage(ChatMessageType.PUBLICCHAT, npc.getName(), taunt, npc.getName());
      tauntExpireTick = client.getTickCount() + 10;
      targetCrab = null;
    }
  }

  @Provides
  ChattyCrabsConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ChattyCrabsConfig.class);
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    // Make sure it's your plugin's config
    if (!event.getGroup().equals("chattycrabs")) {
      return;
    }

    if (event.getKey().equals("damageThreshold")) {
      damageThreshold = config.damageThreshold(); // update threshold
    }
  }
}
