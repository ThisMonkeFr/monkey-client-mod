package gg.monkeyclient.module;

/** Groups modules in the menu, mirroring the launcher's structure. */
public enum Category {
    VISUAL("Visual"), HUD("HUD"), UTILITY("Utility"), MINECRAFT("Minecraft");
    public final String label;
    Category(String label) { this.label = label; }
}
