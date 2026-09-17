package gg.monkeyclient.integration.apple;
/** Settings are owned and persisted by the Monkey Client module. */
public final class ModConfig {
 public static final ModConfig INSTANCE=new ModConfig();
 public boolean showFoodValuesInTooltip=true,showFoodValuesInTooltipAlways=true,showSaturationHudOverlay=true,
  showFoodValuesHudOverlay=true,showFoodValuesHudOverlayWhenOffhand=true,showFoodExhaustionHudUnderlay=true,
  showFoodHealthHudOverlay=true,showVanillaAnimationsOverlay=true;
 public float maxHudOverlayFlashAlpha=.65f;
}
