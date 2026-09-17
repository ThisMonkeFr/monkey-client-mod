package gg.monkeyclient.modules;
import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.*;
public class PotionDisplay extends HudElement {
    private final BoolSetting ambient=add(new BoolSetting("ambient","Include beacon effects",true));
    private final BoolSetting timer=add(new BoolSetting("timer","Show duration",true));
    private final BoolSetting amplifier=add(new BoolSetting("amplifier","Show effect level",true));
    private final EnumSetting layout=add(new EnumSetting("layout","Layout","Compact icons",List.of("Compact icons","Duration bars")));
    private final NumberSetting barWidth=add(new NumberSetting("barWidth","Duration bar width",140,80,300,1));
    private final ColorSetting barColor=add(new ColorSetting("barColor","Duration bar color",0xFF7CC24A));
    private final BoolSetting effectColor=add(new BoolSetting("effectColor","Use effect color for bars",false));
    private final Map<String,Integer> maxDuration=new HashMap<>();
    public PotionDisplay(){super("potions","Potion Display","Native effect icons or named duration bars; replaces vanilla HUD",false);settings().remove(textSize);bg.setDefault(false);padding.setDefault(0d);x.setDefault(.72);y.setDefault(.02);}
    private List<MobEffectInstance> effects(Minecraft mc){
        if(mc.player==null)return List.of();
        return mc.player.getActiveEffects().stream().filter(e->e.showIcon()&&(ambient.get()||!e.isAmbient())).sorted(Comparator.comparing(e->e.getEffect().value().getDescriptionId())).toList();
    }
    @Override public void onTick(){
        var list=effects(Minecraft.getInstance());Set<String> active=new HashSet<>();
        for(var e:list){String id=e.getEffect().value().getDescriptionId();active.add(id);maxDuration.merge(id,e.getDuration(),Math::max);}
        maxDuration.keySet().retainAll(active);
    }
    @Override public void onToggle(boolean on){maxDuration.clear();}
    private boolean bars(){return layout.get().equals("Duration bars");}
    @Override public boolean visible(Minecraft mc){return !effects(mc).isEmpty();}
    @Override public int width(Minecraft mc){return bars()?barWidth.getInt():Math.max(1,effects(mc).size())*26-2;}
    @Override public int height(Minecraft mc){return bars()?Math.max(1,effects(mc).size())*28:24;}
    private static String time(MobEffectInstance e,boolean compact){
        if(e.isInfiniteDuration())return "∞";
        int s=Math.max(0,(e.getDuration()+19)/20);
        return compact?(s>=3600?s/3600+"h":s>=60?s/60+"m":s+"s"):String.format(Locale.ROOT,"%d:%02d",s/60,s%60);
    }
    @Override public void render(GuiGraphicsExtractor g,Minecraft mc){
        var list=effects(mc);
        for(int i=0;i<list.size();i++){
            var e=list.get(i);int sx=bars()?0:i*26,sy=bars()?i*28:0;
            String frame=e.isAmbient()?"hud/effect_background_ambient":"hud/effect_background";
            g.blitSprite(RenderPipelines.GUI_TEXTURED,Identifier.withDefaultNamespace(frame),sx,sy,24,24);
            g.blitSprite(RenderPipelines.GUI_TEXTURED,Hud.getMobEffectSprite(e.getEffect()),sx+3,sy+3,18,18);
            int level=e.getAmplifier()+1;
            String roman=level<=5?new String[]{"I","II","III","IV","V"}[level-1]:String.valueOf(level);
            if(bars()){
                int w=barWidth.getInt();g.fill(26,sy,w,sy+24,backgroundColor());
                String name=e.getEffect().value().getDisplayName().getString()+(amplifier.get()?" "+roman:"");
                g.text(mc.font,mc.font.plainSubstrByWidth(name,Math.max(1,w-30)),28,sy+2,foregroundColor(),shadow.get());
                if(timer.get())g.text(mc.font,time(e,false),28,sy+12,foregroundColor(),shadow.get());
                double fraction=e.isInfiniteDuration()?1:Math.min(1,e.getDuration()/(double)Math.max(1,maxDuration.getOrDefault(e.getEffect().value().getDescriptionId(),e.getDuration())));
                int color=effectColor.get()?0xFF000000|e.getEffect().value().getColor():themeColors.get()?gg.monkeyclient.MonkeyClient.theme().accent:barColor.resolve();
                g.fill(28,sy+22,w-2,sy+24,0x80000000);g.fill(28,sy+22,28+(int)((w-30)*fraction),sy+24,color);
            }else{
                if(amplifier.get())g.text(mc.font,roman,sx+22-mc.font.width(roman),sy+1,foregroundColor(),true);
                if(timer.get()){String t=time(e,true);g.text(mc.font,t,sx+22-mc.font.width(t),sy+15,foregroundColor(),true);}
            }
        }
    }
}
