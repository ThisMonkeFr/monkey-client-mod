package gg.monkeyclient.modules;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.BoolSetting;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.Identifier;
import java.util.*;
/** Retains the old config ID, but now edits the real F3 overlay. */
public class CoordsDisplay extends Module {
    public final BoolSetting coordsOnly = add(new BoolSetting("coordsOnly", "Coordinates only", false));
    public final BoolSetting help = add(new BoolSetting("help", "F3 shortcut hints", false));
    public final BoolSetting charts = add(new BoolSetting("charts", "Allow debug charts", false));
    public final BoolSetting hideModEntries = add(new BoolSetting("hideModEntries", "Hide all mod-added F3 entries", true));
    private final Map<Identifier, BoolSetting> entries = new LinkedHashMap<>();
    public CoordsDisplay() {
        super("coords", "F3 Editor", "Choose exactly which debug entries appear in F3", Category.VISUAL, false);
        DebugScreenEntries.allEntries().keySet().stream().sorted(Comparator.comparing(Identifier::toString)).forEach(id -> {
            String label = Arrays.stream(id.getPath().split("_")).map(s -> s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1)).collect(java.util.stream.Collectors.joining(" "));
            entries.put(id, add(new BoolSetting("entry." + id, label, id.equals(DebugScreenEntries.PLAYER_POSITION) || id.equals(DebugScreenEntries.FPS))));
        });
    }
    public Collection<Identifier> selected() { return DebugScreenEntries.allEntries().keySet().stream().filter(this::includes).toList(); }
    public boolean includes(Identifier id) { if(coordsOnly.get())return id.equals(DebugScreenEntries.PLAYER_POSITION); if(!id.getNamespace().equals("minecraft")&&hideModEntries.get())return false;return entries.containsKey(id)?entries.get(id).get():!hideModEntries.get(); }
    public void selectAll(boolean value) { entries.values().forEach(s -> s.set(value)); help.set(value); charts.set(value); }
}
