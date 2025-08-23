package com.combatbrackethighlighter;

public enum HighlightStyle
{
    NAME_TAG("Name Only"),
    TILE("Tile Glow"),
    OUTLINE("Glow Outline"),
    NAME_AND_OUTLINE("Name + Glow");

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