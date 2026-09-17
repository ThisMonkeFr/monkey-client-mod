package gg.monkeyclient.render;
import java.util.List;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;

/** Twenty Minecraft textures, shared with the launcher's profile picker. */
public final class WaypointIcons {
 public static final List<String> NAMES=List.of("grass","dirt","stone","cobble","oak","bricks","sand","gravel","obsidian","netherrack","end_stone","glowstone","diamond_block","emerald_block","gold","copper","amethyst","blue_ice","cherry","glass");
 private static final FontDescription FONT=new FontDescription.Resource(Identifier.fromNamespaceAndPath("monkeyclient","waypoints"));
 public static Component text(String name){int i=NAMES.indexOf(name);return Component.literal(String.valueOf((char)(0xE100+Math.max(0,i)))).withStyle(Style.EMPTY.withFont(FONT).withColor(0xFFFFFF));}
}
