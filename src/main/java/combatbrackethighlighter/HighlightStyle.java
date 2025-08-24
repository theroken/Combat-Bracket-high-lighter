package com.combatbrackethighlighter;

public enum HighlightStyle
{
    TILE("Tile Only"),
    GLOW("Glow Only"),
    TILE_AND_GLOW("Tile + Glow");

    private final String label;

    HighlightStyle(String label)
    {
        this.label = label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}