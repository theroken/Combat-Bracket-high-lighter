package com.combatbrackethighlighter;

import java.awt.*;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.events.MenuEntryAdded;
import static net.runelite.api.MenuAction.*;
import net.runelite.api.MenuAction;
import net.runelite.client.util.Text;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

class HoverTooltipOverlay extends Overlay
{
    private final Client client;
    private final HiscoreClient hiscoreClient;

    private String hoveredPlayerName;
    private HiscoreResult cachedStats;
    private long lastLookupTime;

    @Inject
    HoverTooltipOverlay(Client client, HiscoreClient hiscoreClient)
    {
        this.client = client;
        this.hiscoreClient = hiscoreClient;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    // Detect hovered player from menu entry
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        int type = event.getType();

        if (type == MenuAction.PLAYER_FIRST_OPTION.getId()
                || type == MenuAction.PLAYER_SECOND_OPTION.getId()
                || type == MenuAction.PLAYER_THIRD_OPTION.getId()
                || type == MenuAction.PLAYER_FOURTH_OPTION.getId()
                || type == MenuAction.PLAYER_FIFTH_OPTION.getId()
                || type == MenuAction.PLAYER_SIXTH_OPTION.getId()
                || type == MenuAction.PLAYER_SEVENTH_OPTION.getId()
                || type == MenuAction.PLAYER_EIGHTH_OPTION.getId()
                || type == MenuAction.RUNELITE_PLAYER.getId())
        {
            hoveredPlayerName = Text.removeTags(event.getTarget());
        }
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        Player me = client.getLocalPlayer();
        if (me == null) return null;

        // Don’t show tooltip while I'm in combat
        if (me.getInteracting() != null) return null;

        if (hoveredPlayerName == null) return null;

        HiscoreResult stats = getStats(hoveredPlayerName);
        if (stats == null) return null;

        String build = classifyBuild(stats);

        // Tooltip text
        String line1 = "Build: " + (build != null ? build : "Unknown");
        String line2 = String.format("Att: %d Str: %d Def: %d",
                stats.getSkill(HiscoreSkill.ATTACK).getLevel(),
                stats.getSkill(HiscoreSkill.STRENGTH).getLevel(),
                stats.getSkill(HiscoreSkill.DEFENCE).getLevel()
        );
        String line3 = String.format("Rng: %d Mage: %d",
                stats.getSkill(HiscoreSkill.RANGED).getLevel(),
                stats.getSkill(HiscoreSkill.MAGIC).getLevel()
        );

        // Box size
        int width = Math.max(
                g.getFontMetrics().stringWidth(line1),
                Math.max(
                        g.getFontMetrics().stringWidth(line2),
                        g.getFontMetrics().stringWidth(line3)
                )
        ) + 10;
        int height = 3 * 15 + 8;

        // Draw box near mouse
        Point mouse = client.getMouseCanvasPosition();
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(mouse.getX() + 15, mouse.getY() - height, width, height);

        g.setColor(Color.WHITE);
        g.drawString(line1, mouse.getX() + 20, mouse.getY() - height + 15);
        g.drawString(line2, mouse.getX() + 20, mouse.getY() - height + 30);
        g.drawString(line3, mouse.getX() + 20, mouse.getY() - height + 45);

        return null;
    }

    // --- Fetch hiscores with caching ---
    private HiscoreResult getStats(String name)
    {
        long now = System.currentTimeMillis();
        if (cachedStats != null && (now - lastLookupTime) < 5 * 60 * 1000)
        {
            return cachedStats;
        }

        try
        {
            HiscoreResult result = hiscoreClient.lookup(name, HiscoreEndpoint.NORMAL);
            if (result != null)
            {
                cachedStats = result;
                lastLookupTime = now;
            }
            return result;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // --- Build classification ---
    private String classifyBuild(HiscoreResult stats)
    {
        int att = stats.getSkill(HiscoreSkill.ATTACK).getLevel();
        int str = stats.getSkill(HiscoreSkill.STRENGTH).getLevel();
        int def = stats.getSkill(HiscoreSkill.DEFENCE).getLevel();
        int rng = stats.getSkill(HiscoreSkill.RANGED).getLevel();
        int mage = stats.getSkill(HiscoreSkill.MAGIC).getLevel();

        // Warhammer Pure
        if (att <= 1 && def <= 5 && str >= 60)
            return "Warhammer Pure";

        // Gmaul Pure
        if (att == 50 && def <= 5 && str >= 80)
            return "Gmaul Pure";

        // 1 Defence Pure
        if (def <= 5 && (str >= 80 || rng >= 80 || mage >= 80))
            return "Pure";

        // Voider
        if (def >= 40 && def <= 45 && rng >= 80)
            return "Voider";

        // Zerker
        if (def >= 42 && def <= 47)
            return "Zerker";

        // Range Tank
        if (def >= 70 && rng >= 90 && att < 70 && str < 70)
            return "Range Tank";

        // Med Build
        if (def >= 60 && def < 70)
            return "Med Build";

        // Main
        if (def >= 70)
            return "Main";

        return "Other";
    }
}