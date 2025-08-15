package com.chattycrabs;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
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
    description = "Crabs taunt you when you as you fight them.",
    tags = {"fun", "npc", "chat"})
public class ChattyCrabsPlugin extends Plugin {
  @Inject Client client;

  @Inject private ChattyCrabsConfig config;

  @Inject private OverlayManager overlayManager;

  private final Random random = new Random();

  private NPC targetCrab = null;
  private int tauntExpireTick = 0;
  private boolean canTaunt = true;

  private enum TauntType {
    BASE,
    MISS,
    GEMSTONE,
    DEATH
  }

  private static final Map<TauntType, List<String>> TAUNTS =
      Map.of(
          TauntType.BASE,
          new ArrayList<>(
              List.of(
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
                  "???",
                  "* sigh *",
                  "Shouldn't you be questing?",
                  "It's clobbering time!",
                  "Stop, will you?",
                  "Bet you've never seen a talking crab before, eh?",
                  "You will be hearing from my lawyer!",
                  "You hit like a fish",
                  "Bruh",
                  "I hope you drop a d med helm",
                  "Watch the claws!",
                  "Call an ambulance... but not for me!",
                  "Who has my knife???")
              ),
          TauntType.MISS,
          new ArrayList<>(
              List.of(
                  "Swing and a miss!",
                  "Go on, give us a real swing.",
                  "Is that your best, mate?",
                  "Bless ya, you tried.",
                  "Whoosh!",
                  "I'm starting to feel sorry for ya...",
                  "* dies * lol jk",
                  "xD")
          ),
          TauntType.GEMSTONE,
          new ArrayList<>(
              List.of(
                  "I'd rather be shiny!",
                  "I will blind you with my brilliance!",
                  "Bet you wish you brought a pickaxe.",
                  "100 noobs vs 1 crab",
                  "Is this really a fair fight?",
                  "Alright, who hit me with a bronze dagger?",
                  "1v1 me lol",
                  "Perhaps we could talk this out?",
                  "I don't want to be made into a necklace :(",
                  "Give me back that dragonstone!",
                  "You're still here???")
              ),
          TauntType.DEATH,
          new ArrayList<>(
              List.of(
                  "Free tele ty",
                  "gf lol",
                  "Back 2 lumby smh",
                  "Wait",
                  "brb lol",
                  "close one",
                  "ggz",
                  "oof",
                  "AHHHHHHHHHHH",
                  "See you space cowboy...",
                  "I'll be back.",
                  "Until we meet again.",
                  "Tell my wife I love her.",
                  "Remember me.")));

  private final Map<TauntType, Integer> tauntIndex = new HashMap<>();

  @Subscribe
  public void onHitsplatApplied(HitsplatApplied event) {
    if (!(event.getActor() instanceof NPC)) {
      return;
    }

    if(targetCrab == null) {
      return;
    }

    if(!canTaunt) {
      return;
    }

    NPC npc = (NPC) event.getActor();
    String npcName = npc.getName().toLowerCase();

    if (random.nextInt(4) == 0)
    {
      int damage = event.getHitsplat().getAmount();

      String taunt = "";
      if (damage <= 1) {
        taunt = getTaunt(TauntType.MISS);
      } else {
        taunt = getTaunt(TauntType.BASE);

        // 50% chance to use a gemstone taunt if fighting gemstone crab
        if (npcName.equalsIgnoreCase("gemstone crab") && random.nextInt(2) == 0) {
          taunt = getTaunt(TauntType.GEMSTONE);
        }
      }

      if (!taunt.isEmpty()) {
        npc.setOverheadText(taunt);
        if (config.displayChatMessage()) {
          client.addChatMessage(ChatMessageType.PUBLICCHAT, npc.getName(), taunt, null);
        }
        tauntExpireTick = client.getTickCount() + 5;
        // targetCrab = npc;
        canTaunt = false;
      }
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (targetCrab != null && client.getTickCount() >= tauntExpireTick && !canTaunt) {

      if(targetCrab.getOverheadText() != null) {
        targetCrab.setOverheadText(null);
      }

      if(client.getTickCount() >= tauntExpireTick + 10) {
        canTaunt = true;
      }
    }
  }

  private String getTaunt(TauntType tauntType) {
    List<String> taunts = TAUNTS.get(tauntType);
    if (taunts == null || taunts.isEmpty()) {
      return "";
    }

    int index = tauntIndex.getOrDefault(tauntType, 0);
    if(index == 0) {
      shuffleTaunts(tauntType);
    }
    String taunt = taunts.get(index);

    index = (index + 1) % taunts.size();
    tauntIndex.put(tauntType, index);

    return taunt;
  }

  @Subscribe
  public void onActorDeath(ActorDeath event) {
    NPC npc = (NPC) event.getActor();
    if (npc == targetCrab) {
      String taunt = getTaunt(TauntType.DEATH);
      npc.setOverheadText(taunt);
      if (config.displayChatMessage()) {
        client.addChatMessage(ChatMessageType.PUBLICCHAT, npc.getName(), taunt, null);
      }
      tauntExpireTick = client.getTickCount() + 10;
      targetCrab = null;
    }
  }

  private void shuffleAllTaunts() {
    for (TauntType tauntType : TAUNTS.keySet()) {
      shuffleTaunts(tauntType);
    }
  }

  private void shuffleTaunts(TauntType tauntType) {
    List<String> taunts = TAUNTS.get(tauntType);
    if (taunts != null) {
      Collections.shuffle(taunts);
    }
  }

  @Provides
  public ChattyCrabsConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ChattyCrabsConfig.class);
  }

  @Subscribe
  public void onInteractingChanged(InteractingChanged event)
  {
    if (event.getSource() == client.getLocalPlayer() && event.getTarget() instanceof NPC)
    {
      NPC npc = (NPC) event.getTarget();
      if (npc.getName().toLowerCase().contains("crab"))
      {
        targetCrab = npc;
      }
    }
  }
}
