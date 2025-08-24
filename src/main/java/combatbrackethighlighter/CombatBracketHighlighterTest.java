package com.combatbrackethighlighter;


import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CombatBracketHighlighterTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(CombatBracketHighlighterPlugin.class);

        RuneLite.main(args);
    }
}
