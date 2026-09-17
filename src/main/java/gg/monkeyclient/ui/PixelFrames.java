package gg.monkeyclient.ui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public final class PixelFrames {
    private PixelFrames() {}
    public static void slot(GuiGraphicsExtractor g, int x, int y, int s) {
        g.fill(x, y, x+s, y+s, 0xFF090A09);
        g.fill(x+1, y+1, x+s-1, y+s-1, 0xFFAAAAA0);
        g.fill(x+2, y+2, x+s-2, y+s-2, 0xFF55564E);
        g.fill(x+3, y+3, x+s-3, y+s-3, 0xB020211D);
        g.fill(x+3, y+3, x+s-3, y+4, 0xFF34362F);
        g.fill(x+3, y+3, x+4, y+s-3, 0xFF34362F);
        g.fill(x+3, y+s-4, x+s-3, y+s-3, 0xFF85877C);
        g.fill(x+s-4, y+3, x+s-3, y+s-3, 0xFF85877C);
    }
    public static void effect(GuiGraphicsExtractor g, int x, int y, int s) {
        g.fill(x+2,y,x+s-2,y+s,0xFF0B0B0D);
        g.fill(x,y+2,x+s,y+s-2,0xFF0B0B0D);
        g.fill(x+2,y+2,x+s-2,y+s-2,0xFF79797C);
        g.fill(x+3,y+3,x+s-3,y+s-3,0xD0202024);
        g.fill(x+4,y+3,x+s-4,y+4,0xFFB2B2B4);
    }
    public static void warning(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x+2,y,x+8,y+9,0xFFB31D20);
        g.fill(x,y+2,x+10,y+7,0xFFB31D20);
        for (int i=0;i<3;i++) { int p=x+2+i*2; g.fill(p,y+1,p+1,y+5,0xFFFFD5CC); g.fill(p,y+6,p+1,y+7,0xFFFFD5CC); }
    }
}