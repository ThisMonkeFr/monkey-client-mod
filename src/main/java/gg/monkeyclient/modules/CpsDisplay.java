package gg.monkeyclient.modules;

import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.setting.BoolSetting;
import gg.monkeyclient.module.setting.StringSetting;
import net.minecraft.client.Minecraft;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class CpsDisplay extends HudElement {
    private final BoolSetting left = add(new BoolSetting("left", "Left click", true));
    private final BoolSetting right = add(new BoolSetting("right", "Right click", true));
    private final BoolSetting oneLine = add(new BoolSetting("oneLine", "Both on one line", true));

    private final StringSetting leftLabel=add(new StringSetting("leftLabel","Left click label","LCPS",24));
    private final StringSetting rightLabel=add(new StringSetting("rightLabel","Right click label","RCPS",24));
    private final StringSetting combinedLabel=add(new StringSetting("combinedLabel","Combined label","CPS",24));
    private final Deque<Long> lefts = new ArrayDeque<>();
    private final Deque<Long> rights = new ArrayDeque<>();


    public CpsDisplay() { super("cps", "CPS Display", "Clicks per second", false); }

    public void click(int button) {
        if(button==0)lefts.addLast(System.currentTimeMillis());
        else if(button==1)rights.addLast(System.currentTimeMillis());
    }
    @Override public boolean visible(Minecraft mc){return left.get()||right.get();}
    @Override public void onToggle(boolean on){lefts.clear();rights.clear();}
    private int count(Deque<Long> q) {
        long cutoff = System.currentTimeMillis() - 1000;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.removeFirst();
        return q.size();
    }

    private String[] lines() {
        int l = count(lefts), r = count(rights);
        if (oneLine.get()) {
            if (left.get() && right.get()) return new String[]{ l + " | " + r + " "+combinedLabel.get() };
            if (left.get()) return new String[]{ l + " "+combinedLabel.get() };
            return new String[]{ r + " "+combinedLabel.get() };
        }
        if (left.get() && right.get()) return new String[]{ l + " "+leftLabel.get(), r + " "+rightLabel.get() };
        return new String[]{ (left.get() ? l + " "+leftLabel.get() : r + " "+rightLabel.get()) };
    }

    @Override public int width(Minecraft mc) {
        int w = 0;
        for (String s : lines()) w = Math.max(w, textWidth(mc,s));
        return w;
    }
    @Override public int height(Minecraft mc) { return lines().length * (textHeight(mc) + 1); }

    @Override public void render(GuiGraphicsExtractor g, Minecraft mc) {

        String[] ls = lines();
        for (int i = 0; i < ls.length; i++)
            text(g, mc, ls[i], 0, i * (textHeight(mc) + 1), foregroundColor(), shadow.get());
    }
}
