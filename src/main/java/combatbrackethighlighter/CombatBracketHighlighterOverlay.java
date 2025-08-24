package com.combatbrackethighlighter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

class CombatBracketHighlighterOverlay extends Overlay
{
    private final Client client;
    private final CombatBracketHighlighterConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    // Hardcoded outline width (was config before)
    private static final int OUTLINE_WIDTH = 2;

    @Inject
    CombatBracketHighlighterOverlay(Client client, CombatBracketHighlighterConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        final Player me = client.getLocalPlayer();
        if (me == null) return null;

        final int wildyLevel = getWildernessLevel(me.getWorldLocation());
        final boolean inWildy = wildyLevel > 0;
        if (config.onlyInWilderness() && !inWildy) return null;

        final int myLevel = me.getCombatLevel();
        final WorldPoint myWp = me.getWorldLocation();
        final int maxDist = Math.max(0, config.maxDistance());

        for (Player p : getPlayersIterable()) {
            if (p == null || p == me || p.getWorldLocation() == null) continue;
            if (maxDist > 0 && myWp.distanceTo(p.getWorldLocation()) > maxDist) continue;

            final int other = p.getCombatLevel();
            final int diff = Math.abs(other - myLevel);

            final boolean attackable = inWildy && diff <= wildyLevel;
            final int missBy = inWildy ? (diff - wildyLevel) : Integer.MAX_VALUE;
            final boolean nearlyAttackable = inWildy && (missBy == 1 || missBy == 2);
            final boolean closeButOut = (!attackable && !nearlyAttackable && diff <= config.closeBracket());

            final Color color;
            if (attackable) {
                color = applyOpacity(config.attackableColor(), config.highlightOpacity());
            } else if (nearlyAttackable) {
                color = applyOpacity(config.nearlyAttackableColor(), config.highlightOpacity());
            } else if (closeButOut) {
                color = applyOpacity(config.outOfReachColor(), config.highlightOpacity());
            } else {
                continue;
            }

            // --- Draw highlight based on style ---
            switch (config.style()) {
                case TILE:
                    drawTileOutline(g, p, color);
                    break;

                case GLOW:
                    drawHullOutlineSafe(p, color, OUTLINE_WIDTH);
                    break;

                case TILE_AND_GLOW:
                    drawTileOutline(g, p, color);
                    drawHullOutlineSafe(p, color, OUTLINE_WIDTH);
                    break;
            }

            // --- Build label text independently ---
            StringBuilder label = new StringBuilder();

            if (config.showNames()) {
                label.append(p.getName());
            }

            if (config.showLevel()) {
                if (label.length() > 0) label.append(" | ");
                label.append("Lvl ").append(other);
            }

            if (config.showLevelDiff() && diff != 0) {
                if (label.length() > 0) label.append(" ");
                int signed = other - myLevel;
                label.append("(").append(signed >= 0 ? "+" : "").append(signed).append(")");
            }

            if (label.length() > 0)
            {
                net.runelite.api.Point textLocation = p.getCanvasTextLocation(g, label.toString(), p.getLogicalHeight() + 40);
                if (textLocation != null)
                {
                    OverlayUtil.renderTextLocation(g, textLocation, label.toString(), color);
                }
            }
        }

        return null;
    }

    private Color applyOpacity(Color base, int percent)
    {
        int alpha = (int) (255 * (percent / 100.0));
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private void drawTileOutline(Graphics2D g, Player p, Color color)
    {
        LocalPoint lp = p.getLocalLocation();
        if (lp == null) return;
        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly != null)
        {
            // force transparency respect
            g.setComposite(java.awt.AlphaComposite.SrcOver.derive(color.getAlpha() / 255f));
            OverlayUtil.renderPolygon(g, poly, color);
            g.setComposite(java.awt.AlphaComposite.SrcOver); // reset to normal
        }
    }

    private void drawHullOutlineSafe(Player p, Color color, int width)
    {
        try {
            // Just pass the color with alpha baked in
            modelOutlineRenderer.drawOutline(
                    p,
                    width,
                    color,      // keep alpha here
                    0           // feather = 0 (no blur unless you want it)
            );
        } catch (Throwable ignored) {}
    }

    private int getWildernessLevel(WorldPoint wp)
    {
        if (wp == null) return 0;
        if (wp.getY() < 3520) return 0;
        int lvl = ((wp.getY() - 3520) / 8) + 1;
        return Math.min(Math.max(lvl, 1), 56);
    }

    private Iterable<Player> getPlayersIterable()
    {
        try
        {
            WorldView vw = client.getTopLevelWorldView();
            if (vw != null && vw.players() != null)
            {
                ArrayList<Player> list = new ArrayList<>();
                for (Player p : vw.players()) if (p != null) list.add(p);
                return list;
            }
        }
        catch (Throwable ignored) {}

        try
        {
            java.util.List<Player> listFromClient = client.getPlayers();
            if (listFromClient != null)
            {
                ArrayList<Player> list = new ArrayList<>(listFromClient.size());
                for (Player p : listFromClient) if (p != null) list.add(p);
                return list;
            }
        }
        catch (Throwable ignored) {}

        return new ArrayList<>();
    }
}