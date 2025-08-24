package com.combatbrackethighlighter;

import java.awt.Color;
import net.runelite.client.config.*;

@ConfigGroup("combatbrackethighlighter")
public interface CombatBracketHighlighterConfig extends Config
{
    // ---------------- 🖼️ Display ----------------
    @ConfigSection(
            name = "Display",
            description = "Control how highlights and labels are shown",
            position = 0
    )
    String displaySection = "displaySection";

    @ConfigItem(
            keyName = "highlightStyle",
            name = "Highlight Style",
            description = "Choose how players are highlighted",
            section = displaySection,
            position = 1
    )
    default HighlightStyle style()
    {
        return HighlightStyle.GLOW; // default option
    }

    @ConfigItem(
            keyName = "showNames",
            name = "Show Player Name",
            description = "Toggle showing player names above highlights",
            section = displaySection,
            position = 2
    )
    default boolean showNames() { return true; }

    @ConfigItem(
            keyName = "showLevel",
            name = "Show Combat Level",
            description = "Toggle showing combat levels above highlights",
            section = displaySection,
            position = 3
    )
    default boolean showLevel() { return true; }

    @ConfigItem(
            keyName = "showLevelDiff",
            name = "Show +/- Difference",
            description = "Show how many levels higher or lower they are compared to you",
            section = displaySection,
            position = 4
    )
    default boolean showLevelDiff() { return true; }

    @ConfigItem(
            keyName = "highlightOpacity",
            name = "Highlight Opacity",
            description = "Transparency of the highlight (0% = invisible, 100% = fully opaque)",
            section = colorsSection,
            position = 24
    )
    @Range(
            min = 0,
            max = 100
    )
    @Units("%")
    default int highlightOpacity() { return 80; } // default 80%

    // ---------------- ⚔️ Wilderness Rules ----------------
    @ConfigSection(
            name = "Wilderness Rules",
            description = "Rules that control who gets highlighted",
            position = 10
    )
    String wildySection = "wildySection";

    @ConfigItem(
            keyName = "onlyInWilderness",
            name = "Only in Wilderness",
            description = "Enable highlighting only when you are in the Wilderness",
            section = wildySection,
            position = 11
    )
    default boolean onlyInWilderness() { return true; }

    @ConfigItem(
            keyName = "maxDistance",
            name = "Max Distance (tiles)",
            description = "Only highlight players within this many tiles (0 = unlimited)",
            section = wildySection,
            position = 12
    )
    default int maxDistance() { return 32; }

    @ConfigItem(
            keyName = "closeBracket",
            name = "Close Level Bracket",
            description = "Highlight players within this many levels of being attackable",
            section = wildySection,
            position = 13
    )
    default int closeBracket() { return 3; }

    // ---------------- 🎨 Highlight Colors ----------------
    @ConfigSection(
            name = "Highlight Colors",
            description = "Set the colors used for different player states",
            position = 20
    )
    String colorsSection = "colorsSection";

    @ConfigItem(
            keyName = "attackableColor",
            name = "Can Attack Color",
            description = "Color used for players you can currently attack",
            section = colorsSection,
            position = 21
    )
    default Color attackableColor() { return Color.GREEN; }

    @ConfigItem(
            keyName = "nearlyAttackableColor",
            name = "Almost Attackable Color",
            description = "Color for players just 1–2 levels outside your attack range",
            section = colorsSection,
            position = 22
    )
    default Color nearlyAttackableColor() { return Color.YELLOW; }

    @ConfigItem(
            keyName = "outOfReachColor",
            name = "Too High/Low Color",
            description = "Color for players close in level but still not attackable",
            section = colorsSection,
            position = 23
    )
    default Color outOfReachColor() { return Color.RED; }
}