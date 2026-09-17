package gg.monkeyclient.render;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import java.util.Map;
public final class Icons {
 private static final FontDescription FONT=new FontDescription.Resource(Identifier.fromNamespaceAndPath("monkeyclient","icons"));
 private static final Map<String,String> GLYPHS=Map.ofEntries(Map.entry("monkey", "\uE000"),
Map.entry("mctiers", "\uE001"),
Map.entry("subtiers", "\uE002"),
Map.entry("mcpvp", "\uE003"),
Map.entry("vanilla", "\uE004"),
Map.entry("uhc", "\uE005"),
Map.entry("pot", "\uE006"),
Map.entry("nethop", "\uE007"),
Map.entry("smp", "\uE008"),
Map.entry("sword", "\uE009"),
Map.entry("axe", "\uE00A"),
Map.entry("mace", "\uE00B"),
Map.entry("minecart", "\uE00C"),
Map.entry("dia_crystal", "\uE00D"),
Map.entry("debuff", "\uE00E"),
Map.entry("elytra", "\uE00F"),
Map.entry("speed", "\uE010"),
Map.entry("creeper", "\uE011"),
Map.entry("manhunt", "\uE012"),
Map.entry("dia_smp", "\uE013"),
Map.entry("bow", "\uE014"),
Map.entry("bed", "\uE015"),
Map.entry("og_vanilla", "\uE016"),
Map.entry("trident", "\uE017"),
Map.entry("pvp_crystal", "\uE018"),
Map.entry("pvp_sword", "\uE019"),
Map.entry("pvp_spear", "\uE01A"),
Map.entry("pvp_mace", "\uE01B"),
Map.entry("pvp_early", "\uE01C"),
Map.entry("pvp_shield", "\uE01D"),
Map.entry("pvp_pot", "\uE01E"),
Map.entry("pvp_nether", "\uE01F"),
Map.entry("pvp_late", "\uE020"),
Map.entry("pvp_dia_smp", "\uE021"),
Map.entry("pvp_creeper", "\uE022"),
Map.entry("pvp_smp", "\uE023"),
Map.entry("pvp_cart", "\uE024"),
Map.entry("pvp_bow", "\uE025"));
 public static Component text(String key){return Component.literal(GLYPHS.getOrDefault(key,"")).withStyle(Style.EMPTY.withFont(FONT).withColor(0xFFFFFF));}
}
