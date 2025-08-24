package com.combatbrackethighlighter;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "Combat Bracket Highlighter",
        description = "Highlights players inside or near your combat bracket in the Wilderness",
        tags = {"pvp", "wilderness", "combat", "bracket", "highlight"}
)
public class CombatBracketHighlighterPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private CombatBracketHighlighterConfig config;

    @Inject
    private CombatBracketHighlighterOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Combat Bracket Highlighter started!");
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("Combat Bracket Highlighter stopped!");
        overlayManager.remove(overlay);
    }

    @Provides
    CombatBracketHighlighterConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(CombatBracketHighlighterConfig.class);
    }
}