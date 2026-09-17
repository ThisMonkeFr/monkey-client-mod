package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.config.Theme;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.modules.CoordsDisplay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import java.util.*;
public class MonkeyMenuScreen extends Screen {
    private static final Identifier LOGO=Identifier.fromNamespaceAndPath("monkeyclient","textures/logo.png");
    private int left,top,pw,ph,scroll,maxScroll,bodyTop,bodyBottom;
    private int settingsWidth;
    private gg.monkeyclient.config.ModProfiles profiles;
    private int profileIndex;
    private String profileName="";
    private String page="Mods",query="";
    private Category category;
    private Module selected;
    private Screen parent;
    public MonkeyMenuScreen showSettings(Module module,Screen parent){this.selected=module;this.parent=parent;this.page="Mods";return this;}
    private KeySetting binding;
    private MenuButton bindingButton;
    private long opened=System.nanoTime();
    private final List<Card> cards=new ArrayList<>();
    private final List<Label> labels=new ArrayList<>();
    private final List<Setting<?>> themeSettings=new ArrayList<>();
    private String themePreset="Custom";
    private final Map<EditBox,java.util.function.Supplier<String>> fieldValues=new HashMap<>();
    private record Card(Module module,int x,int y,int w,int h) {}
    private record Label(String text,int x,int y,int w) {}
    public MonkeyMenuScreen() { super(Component.literal("Monkey Client")); }
    @Override protected void init() {
        pw=Math.min(760,Math.min(width-20,Math.max(300,Math.round(width*.82f))));
        ph=Math.min(440,Math.min(height-20,Math.max(210,Math.round(height*.84f))));
        left=(width-pw)/2; top=(height-ph)/2;
        build();
    }
    private void navigate(String next) { page=next; selected=null; scroll=0; binding=null; opened=System.nanoTime(); build(); }
    private void build() {
        clearWidgets(); clearFocus(); cards.clear(); labels.clear();fieldValues.clear();
        ph=Math.min(440,Math.min(height-20,Math.max(210,Math.round(height*.84f))));
        if(page.equals("Mods")&&selected==null){
            int filters=(Math.min(56,(pw-24)/5)+3)*(Category.values().length+1);
            int header=64+(pw-24-filters<85?26:0);
            int neededRows=Math.max(1,(filtered().size()+gridColumns()-1)/gridColumns());
            ph=Math.min(ph,header+neededRows*48+4);
        }
        top=(height-ph)/2;
        if(page.equals("Home")) { buildHome(); return; }
        int tabW=Math.min(76,(pw-40)/7),tx=left+8;
        for(String tab:List.of("Mods","Profiles","Theme","HUD","Friends","Screenshots","Skins")){
            String label=tabW<64&&tab.equals("Screenshots")?"Shots":tabW<48&&tab.equals("Profiles")?"Sets":tabW<48&&tab.equals("Friends")?"Chat":tab;
            var tabButton=button(tx,top+8,tabW-3,20,label,()->{if(tab.equals("HUD"))minecraft.setScreenAndShow(new HudEditorScreen(this));else if(java.util.Set.of("Friends","Screenshots","Skins").contains(tab))minecraft.setScreenAndShow(new LauncherScreen(this,tab.toLowerCase(java.util.Locale.ROOT)));else navigate(tab);},page.equals(tab));tabButton.setTooltip(Tooltip.create(Component.literal(tab)));tx+=tabW;
        }
        button(left+pw-28,top+8,20,20,"X",this::onClose,false);
        bodyTop=top+64; bodyBottom=top+ph-8;
        if(page.equals("Profiles")){buildProfiles();return;}
        if(page.equals("Mods") && selected==null) { buildCards(); return; }
        buildSettings();
    }
    private MenuButton button(int x,int y,int w,int h,String text,Runnable action,boolean on) {
        return addRenderableWidget(new MenuButton(x,y,w,h,text,action,()->on));
    }
    private void buildHome() {
        int w=Math.min(220,pw-40),x=(width-w)/2,y=height/2-10;
        button(x,y,w,20,"Client Settings",()->navigate("Mods"),false);
        button(x,y+26,(w-12)/3,20,"HUD",()->minecraft.setScreenAndShow(new HudEditorScreen(this)),false);
        button(x+(w+6)/3,y+26,(w-12)/3,20,"Profiles",()->navigate("Profiles"),false);
        button(x+2*(w+6)/3,y+26,(w-12)/3,20,"Theme",()->navigate("Theme"),false);
        button(x,y+52,w,20,"Back to Game",this::onClose,false);
    }
    private List<Module> filtered() {
        String q=query.toLowerCase(Locale.ROOT);
        return MonkeyClient.modules().all().stream().filter(m->category==null||m.category==category)
            .filter(m->(m.name+" "+m.description).toLowerCase(Locale.ROOT).contains(q)).toList();
    }
    private void buildCards() {
        int filterW=Math.min(56,(pw-24)/5),x=left+12;
        button(x,top+39,filterW,20,"ALL",()->{category=null;scroll=0;build();},category==null); x+=filterW+3;
        for(Category c:Category.values()) {
            button(x,top+39,filterW,20,c.label.toUpperCase(Locale.ROOT),()->{category=c;scroll=0;build();},category==c); x+=filterW+3;
        }
        int searchW=pw-24-(x-left-12);
        if(searchW<85) { bodyTop+=26; x=left+12; searchW=pw-24; }
        EditBox search=addRenderableWidget(new EditBox(font,x,searchW==pw-24?top+65:top+39,searchW,20,Component.literal("Search modules")));
        search.setHint(Component.literal("Search modules...")); search.setValue(query);
        search.setResponder(s->{query=s;scroll=0; rebuildCardsKeepingSearch(search);});
        buildCardRows();
    }
    private void rebuildCardsKeepingSearch(EditBox previous) {
        int cursor=previous.getCursorPosition(); build();
        for(var child:children()) if(child instanceof EditBox e) { setFocused(e);e.setFocused(true); e.setCursorPosition(Math.min(cursor,e.getValue().length()));break; }
    }
    private int gridColumns(){return Math.max(2,Math.min(6,(pw-20)/88));}
    private void buildCardRows() {
        int columns=gridColumns(),gap=4,cw=(pw-24-(columns-1)*gap)/columns;
        int rows=Math.max(1,(bodyBottom-bodyTop+gap)/48),ch=(bodyBottom-bodyTop-(rows-1)*gap)/rows;
        var list=filtered(); int totalRows=(list.size()+columns-1)/columns;
        maxScroll=Math.max(0,totalRows-rows); scroll=Math.min(scroll,maxScroll);
        for(int i=scroll*columns;i<Math.min(list.size(),(scroll+rows)*columns);i++) {
            Module m=list.get(i); int col=i%columns,row=i/columns-scroll;
            int x=left+12+col*(cw+gap),y=bodyTop+row*(ch+gap);
            cards.add(new Card(m,x,y,cw,ch));
            MenuButton open=button(x+22,y+4,cw-26,16,m.name,()->openModule(m),false);
            open.setTooltip(Tooltip.create(Component.literal(m.name+"\n"+m.description)));
            addRenderableWidget(new MenuButton(x+5,y+ch-21,cw-29,17,m.isEnabled()?"On":"Off",()->{
                m.enabled.toggle();m.onToggle(m.isEnabled());MonkeyClient.saveConfig();build();
            },m::isEnabled));
            var settings=button(x+cw-22,y+ch-21,17,17,">",()->openModule(m),false);
            settings.setTooltip(Tooltip.create(Component.literal("Edit "+m.name)));
        }
    }
    private void openModule(Module m) { selected=m; scroll=0; opened=System.nanoTime(); build(); }
    private void createThemeSettings() {
        themeSettings.clear(); var t=MonkeyClient.theme();
        themeSettings.add(new EnumSetting("style","Menu style",t.vanilla?"Vanilla":"Monkey",List.of("Monkey","Vanilla")));
        var preset=new EnumSetting("preset","Theme preset","Custom",List.of("Custom","Jungle","Volcano","Abyss","Amethyst","Ember","Frost","Classic","Amber","Ice"));
        preset.set(themePreset);themeSettings.add(preset);
        themeSettings.add(new ColorSetting("accent","Accent colour",t.accent));
        themeSettings.add(new ColorSetting("background","Background colour",t.background));
        themeSettings.add(new ColorSetting("panel","Panel colour",t.panel));
        themeSettings.add(new ColorSetting("text","Text colour",t.text));
        themeSettings.add(new ColorSetting("textDim","Secondary text colour",t.textDim));
        themeSettings.add(new NumberSetting("opacity","Panel opacity (%)",t.opacity,30,100,1));
        themeSettings.add(new BoolSetting("animations","UI animations",t.animations));
        themeSettings.add(new NumberSetting("animationMs","Animation duration (ms)",t.animationMs,50,600,10));
        Theme defaults=new Theme();
        for(var s:themeSettings) {
            if(s instanceof ColorSetting c) {
                int current=c.get();
                c.setDefault(switch(c.id) {case "accent"->defaults.accent;case "background"->defaults.background;case "panel"->defaults.panel;case "text"->defaults.text;default->defaults.textDim;});
                c.set(current);
            } else if(s instanceof NumberSetting n) {
                double current=n.get();n.setDefault(n.id.equals("opacity")?(double)defaults.opacity:(double)defaults.animationMs);n.set(current);
            } else if(s instanceof BoolSetting b) {boolean current=b.get();b.setDefault(true);b.set(current);}
        }
        themeSettings.add(new ColorSetting("buttonColor","Button color (00000000 = theme)",t.buttonColor));
        themeSettings.add(new ColorSetting("sliderColor","Slider color (00000000 = theme)",t.sliderColor));
        themeSettings.add(new ColorSetting("toggleColor","Toggle color (00000000 = theme)",t.toggleColor));
        themeSettings.add(new ColorSetting("highlightColor","Highlight color (00000000 = theme)",t.highlightColor));
        for(var setting:themeSettings)if(Set.of("buttonColor","sliderColor","toggleColor","highlightColor").contains(setting.id)){var c=(ColorSetting)setting;int current=c.get();c.setDefault(0);c.set(current);}
    }
    private List<Setting<?>> settings() {
        if(selected!=null) return selected.settings().stream().filter(s->s!=selected.enabled&&!(s instanceof JsonSetting)&&!(s instanceof ModuleSetting)&&!s.id.equals("pixels"))
            .sorted(Comparator.comparingInt(s -> Set.of("x","y","scale","textColor","shadow","background","bgColor","border","borderColor","padding","align").contains(s.id) ? 1 : 0)).toList();
        if(themeSettings.isEmpty()) createThemeSettings();
        return themeSettings;
    }
    private void applyTheme(Setting<?> changed) {
        var t=MonkeyClient.theme();
        if(changed.id.equals("preset")) {
            themePreset=((EnumSetting)changed).get();t.preset(themePreset); createThemeSettings();build();return;
        }
        for(var s:themeSettings) switch(s.id) {
            case "style" -> t.vanilla=((EnumSetting)s).get().equals("Vanilla");
            case "accent" -> t.accent=((ColorSetting)s).get();
            case "background" -> t.background=((ColorSetting)s).get();
            case "panel" -> t.panel=((ColorSetting)s).get();
            case "text" -> t.text=((ColorSetting)s).get();
            case "textDim" -> t.textDim=((ColorSetting)s).get();
            case "opacity" -> t.opacity=((NumberSetting)s).getInt();
            case "animations" -> t.animations=((BoolSetting)s).get();
            case "animationMs" -> t.animationMs=((NumberSetting)s).getInt();
            case "buttonColor" -> t.buttonColor=((ColorSetting)s).get();
            case "sliderColor" -> t.sliderColor=((ColorSetting)s).get();
            case "toggleColor" -> t.toggleColor=((ColorSetting)s).get();
            case "highlightColor" -> t.highlightColor=((ColorSetting)s).get();
        }
    }
    private void changed(Setting<?> s) { if(selected==null) applyTheme(s);else selected.onSettingChanged(s); }
    private void buildSettings() {
        int x=left+12;
        if(selected!=null) {
            button(x,top+39,24,20,"<",()->{selected=null;scroll=0;build();},false);
            labels.add(new Label(selected.name.toUpperCase(Locale.ROOT),x+32,top+45,pw-205));
            addRenderableWidget(new MenuButton(left+pw-134,top+39,48,20,selected.isEnabled()?"ON":"OFF",()->{
                selected.enabled.toggle(); selected.onToggle(selected.isEnabled()); MonkeyClient.saveConfig(); build();
            },selected::isEnabled));
            button(left+pw-80,top+39,68,20,"RESET",()->{selected.resetAll();MonkeyClient.saveConfig();build();},false);
        } else {
            labels.add(new Label("INTERFACE THEME",x,top+45,pw-168));
            button(left+pw-150,top+39,138,20,"MATCH LAUNCHER",()->{
                boolean ok=MonkeyClient.theme().matchLauncher(); createThemeSettings();build();
                status=ok?"Launcher theme applied":"Launcher theme file not found";
            },false);
        }
        if(selected!=null) {

        }
        if(selected instanceof CoordsDisplay f3) {
            button(x,bodyTop,pw/2-16,20,"HIDE ALL",()->{f3.selectAll(false);build();},false);
            button(left+pw/2+3,bodyTop,pw/2-15,20,"SHOW ALL",()->{f3.selectAll(true);build();},false);
            bodyTop+=27;
        }
        if(selected!=null && selected.editor(this)!=null) {
            button(x,bodyTop,pw-24,20,selected.editorLabel(),()->minecraft.setScreenAndShow(selected.editor(this)),false);
            bodyTop+=27;
        }
        settingsWidth=selected!=null&&pw>=440?(int)((pw-32)*.64f):pw-24;
        var list=settings();
        int rowH=38,count=Math.max(1,(bodyBottom-bodyTop)/rowH);
        maxScroll=Math.max(0,list.size()-count);scroll=Math.min(scroll,maxScroll);
        for(int i=scroll;i<Math.min(list.size(),scroll+count);i++) {
            Setting<?> s=list.get(i);int y=bodyTop+(i-scroll)*rowH;
            int cx=left+12,controlW=settingsWidth-28;
            labels.add(new Label(s.label,cx+1,y+1,settingsWidth));y+=10;
            button(cx+settingsWidth-24,y+5,24,22,"R",()->{s.reset();changed(s);build();},false).setTooltip(Tooltip.create(Component.literal("Reset "+s.label)));
            if(s instanceof StringSetting str) {
                EditBox e=field(cx,y+6,controlW,str.get(),s.label);
                e.setMaxLength(str.maxLength);e.setValue(str.get());e.setResponder(v->{str.set(v);changed(str);});
            } else if(s instanceof BoolSetting b) {
                addRenderableWidget(new MenuButton(cx,y+5,controlW,22,b.get()?"ON":"OFF",()->{b.toggle();changed(s);build();},b::get));
            } else if(s instanceof EnumSetting e) {
                button(cx,y+5,controlW,22,e.get(),()->{e.cycle();changed(s);build();},false);
            } else if(s instanceof KeySetting k) {
                MenuButton[] keyButton=new MenuButton[1];
                keyButton[0]=button(cx,y+5,controlW,22,k.display(),()->{binding=k;bindingButton=keyButton[0];bindingButton.setMessage(Component.literal("Press a key..."));},false);
            } else if(s instanceof NumberSetting n) {
                int fieldW=Math.min(70,controlW/2);
                EditBox e=field(cx,y+6,fieldW,format(n.get()),s.label);
                fieldValues.put(e,()->format(n.get()));
                e.setResponder(v->{try { double d=Double.parseDouble(v);if(!Double.isFinite(d))throw new NumberFormatException();n.set(d);changed(s);e.setTextColor(MonkeyClient.theme().text); }catch(NumberFormatException ex){e.setTextColor(0xFFFF6666);} });
                addRenderableWidget(new SettingSlider(cx+fieldW+8,y+7,controlW-fieldW-8,n,()->{e.setValue(format(n.get()));changed(s);}));
            } else if(s instanceof ColorSetting c) {
                button(cx,y+5,controlW,22,"Choose color",()->minecraft.setScreenAndShow(new ColorPickerScreen(this,c,()->changed(c))),false);

            }
        }
    }
    private MenuButton findKeyButton(KeySetting k) {
        for(var child:children()) if(child instanceof MenuButton b && b.getMessage().getString().equals(k.display()))return b;
        return null;
    }
    private String status="";
    @Override public void tick() {
        for(var entry:fieldValues.entrySet())if(!entry.getKey().isFocused()) {
            String normalized=entry.getValue().get();
            if(!entry.getKey().getValue().equals(normalized))entry.getKey().setValue(normalized);
        }
    }
    private EditBox field(int x,int y,int w,String value,String label) {
        EditBox e=addRenderableWidget(new EditBox(font,x,y,w,20,Component.literal(label)));
        e.setValue(value);e.setTextColor(0xFFFFFFFF);e.setTooltip(Tooltip.create(Component.literal(label)));return e;
    }
    private static String format(double d) { return java.math.BigDecimal.valueOf(d).stripTrailingZeros().toPlainString(); }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        var t=MonkeyClient.theme();
        float progress=t.animations?Math.min(1,(System.nanoTime()-opened)/1e6f/t.animationMs):1;
        float ease=1-(1-progress)*(1-progress)*(1-progress);
        g.fill(0,0,width,height,0x55000000);
        int alpha=(int)(255*t.opacity/100f);
        float scale=.86f+.14f*ease;
        g.pose().pushMatrix();g.pose().translate(width/2f,height/2f);g.pose().scale(scale,scale);g.pose().translate(-width/2f,-height/2f+12*(1-ease));
        if(!page.equals("Home")){
            if(t.vanilla)VanillaDraw.panel(g,left,top,pw,ph,false);
            else{g.fill(left,top,left+pw,top+ph,(t.background&0xFFFFFF)|(alpha<<24));g.fill(left,top,left+pw,top+35,(t.panel&0xFFFFFF)|(alpha<<24));outline(g,left,top,left+pw,top+ph,t.line());}
        }
        if(page.equals("Home")) {
            int cy=height/2-98;
            g.blit(RenderPipelines.GUI_TEXTURED,LOGO,width/2-28,cy,0f,0f,56,56,56,56);
            g.centeredText(font,"Monkey Client",width/2,cy+64,0xFFFFFFFF);
        }
        for(Card c:cards) {
            boolean hover=mx>=c.x&&mx<c.x+c.w&&my>=c.y&&my<c.y+c.h;
            VanillaDraw.panel(g,c.x,c.y,c.w,c.h,true);
            if(hover)g.fill(c.x+2,c.y+2,c.x+c.w-2,c.y+3,t.accent);
            g.pose().pushMatrix();g.pose().translate(c.x+4,c.y+4);g.pose().scale(1f,1f);
            g.item(icon(c.module.id),0,0);g.pose().popMatrix();
        }
        for(Label l:labels) {
            String text=font.plainSubstrByWidth(l.text,Math.max(1,l.w));
            g.text(font,text,l.x,l.y,t.mutedInk());
            if(mx>=l.x&&mx<l.x+l.w&&my>=l.y&&my<l.y+11)g.setTooltipForNextFrame(font,Component.literal(l.text),mx,my);
        }
        if(!page.equals("Home")&&maxScroll>0) {
            int track=bodyBottom-bodyTop,thumb=Math.max(14,track/(maxScroll+1));
            int sy=bodyTop+(int)((track-thumb)*scroll/(double)maxScroll);
            g.fill(left+pw-5,bodyTop,left+pw-3,bodyBottom,t.line());
            g.fill(left+pw-5,sy,left+pw-3,sy+thumb,t.accent);
        }
        if(selected!=null&&pw>=440)drawPreview(g);
        if(!status.isEmpty())g.text(font,font.plainSubstrByWidth(status,pw-24),left+12,top+ph-12,t.mutedInk());
        super.extractRenderState(g,mx,my,delta);
        g.pose().popMatrix();
    }
    private void drawPreview(GuiGraphicsExtractor g){
        int x=left+settingsWidth+22,y=bodyTop,w=pw-settingsWidth-34,h=bodyBottom-bodyTop;
        VanillaDraw.panel(g,x,y,w,h,true);g.centeredText(font,"Preview",x+w/2,y+10,0xFF303030);
        int cy=y+h/2;
        if(selected instanceof gg.monkeyclient.hud.HudElement hud&&minecraft.player!=null&&hud.visible(minecraft)){
            int hw=Math.max(1,hud.width(minecraft)),hh=Math.max(1,hud.height(minecraft));float s=Math.min(2,Math.min((w-16f)/hw,(h-40f)/hh));
            g.pose().pushMatrix();g.pose().translate(x+(w-hw*s)/2f,cy-hh*s/2f);g.pose().scale(s,s);hud.render(g,minecraft);g.pose().popMatrix();
        }else{
            g.pose().pushMatrix();g.pose().translate(x+w/2f-24,cy-32);g.pose().scale(3,3);g.item(icon(selected.id),0,0);g.pose().popMatrix();
            g.centeredText(font,selected.isEnabled()?"Enabled":"Disabled",x+w/2,cy+24,0xFF303030);
        }
    }
    private interface ProfileAction{void run()throws Exception;}
    private void profileAction(ProfileAction action){try{action.run();status="";}catch(Exception ex){status=ex.getMessage()==null?"Could not update profiles":ex.getMessage();}build();}
    private void buildProfiles(){
        if(profiles==null)try{profiles=new gg.monkeyclient.config.ModProfiles();}catch(Exception ex){status="Could not read mod profiles";return;}
        button(left+12,top+39,90,20,"Import code",()->minecraft.setScreenAndShow(new ProfileCodeScreen(this,profiles,-1)),false);
        button(left+106,top+39,90,20,"Share code",()->{if(!profiles.all().isEmpty())minecraft.setScreenAndShow(new ProfileCodeScreen(this,profiles,profileIndex));},false);
        button(left+200,top+39,80,20,"Presets",()->minecraft.setScreenAndShow(new PresetsScreen(this,profiles)),false);
        labels.add(new Label("Current profile: "+profiles.activeName(),left+12,top+68,pw-24));bodyTop=top+84;
        int columns=pw>=480?3:2,gap=6,cw=(pw-24-(columns-1)*gap)/columns;
        var list=profiles.all();profileIndex=Math.max(0,Math.min(profileIndex,list.size()-1));
        int rows=Math.max(1,(ph-185)/48);maxScroll=Math.max(0,(list.size()+columns-1)/columns-rows);scroll=Math.min(scroll,maxScroll);
        for(int i=scroll*columns;i<Math.min(list.size(),(scroll+rows)*columns);i++){
            int index=i;var p=list.get(i);var b=button(left+12+i%columns*(cw+gap),bodyTop+(i/columns-scroll)*48,cw,42,p.name(),()->{profileIndex=index;profileName=p.name();build();},profileIndex==i);
            b.setMessage(Component.empty().append(gg.monkeyclient.render.WaypointIcons.text("dirt")).append(" "+p.name()+(profiles.active(i)?" [Active]":"")));
        }
        int controlsY=top+ph-82,bw=(pw-28)/5;
        if(!list.isEmpty()){
            button(left+12,controlsY,bw-3,20,"Use profile",()->profileAction(()->profiles.apply(profileIndex)),false);
            button(left+12+bw,controlsY,bw-3,20,"Save changes",()->profileAction(()->profiles.update(profileIndex)),false);
            button(left+12+bw*2,controlsY,bw-3,20,"Copy",()->profileAction(()->{var p=profiles.all().get(profileIndex);profiles.create(p.name()+" copy",p.settings());}),false);
            button(left+12+bw*3,controlsY,bw-3,20,"Rename",()->profileAction(()->profiles.rename(profileIndex,profileName)),false);
            button(left+12+bw*4,controlsY,bw-3,20,"Delete",()->profileAction(()->profiles.delete(profileIndex)),false);
        }
        var name=field(left+12,top+ph-51,pw-108,profileName,"Profile name");name.setMaxLength(40);name.setHint(Component.literal("Profile name"));name.setResponder(v->profileName=v);
        button(left+pw-88,top+ph-51,76,20,"Create",()->profileAction(()->{profiles.create(profileName,gg.monkeyclient.config.ConfigManager.snapshot());profileIndex=profiles.all().size()-1;}),false);
    }
    private ItemStack icon(String id) {
        return new ItemStack(switch(id) {
            case "armor" -> Items.DIAMOND_CHESTPLATE;
            case "potions" -> Items.POTION;
            case "brightness" -> Items.SUNFLOWER;
            case "zoom" -> Items.SPYGLASS;
            case "coords" -> Items.COMPASS;
            case "ping" -> Items.ENDER_PEARL;
            case "fps" -> Items.CLOCK;
            case "cps" -> Items.DIAMOND_SWORD;
            case "keystrokes" -> Items.OAK_PRESSURE_PLATE;
            case "sprint" -> Items.DIAMOND_BOOTS;
            case "guiscale" -> Items.ITEM_FRAME;
            case "fog" -> Items.GLASS;
            case "outlines" -> Items.GLOWSTONE_DUST;
            case "crosshair" -> Items.TARGET;
            case "items" -> Items.IRON_PICKAXE;
            case "waypoints" -> Items.RECOVERY_COMPASS;
            case "motionblur" -> Items.FEATHER;
            case "tiers" -> Items.GOLDEN_HELMET;
            case "appleskin" -> Items.APPLE;
            case "containers" -> Items.SHULKER_BOX;
            case "nametag" -> Items.NAME_TAG;
            case "borderless" -> Items.GLASS_PANE;
            default -> Items.PAINTING;
        });
    }
    @Override public boolean mouseScrolled(double mx,double my,double dx,double dy) {
        if(binding!=null)return true;
        if(mx>=left&&mx<=left+pw&&my>=bodyTop&&my<bodyBottom&&maxScroll>0) {
            scroll=Math.max(0,Math.min(maxScroll,scroll-(int)Math.signum(dy)));build();return true;
        }
        return super.mouseScrolled(mx,my,dx,dy);
    }
    @Override public boolean keyPressed(KeyEvent e) {
        if(binding!=null) {
            if(e.key()!=256) {binding.set(e.key()==259||e.key()==261?-1:e.key());changed(binding);}
            binding=null;bindingButton=null;build();MonkeyClient.saveConfig();return true;
        }
        if(e.key()==256&&selected!=null) {if(parent!=null){onClose();return true;}selected=null;scroll=0;build();return true;}
        if((e.key()==266||e.key()==267)&&maxScroll>0) {scroll=Math.max(0,Math.min(maxScroll,scroll+(e.key()==267?1:-1)));build();return true;}
        return super.keyPressed(e);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) { boolean handled=super.mouseReleased(e);MonkeyClient.saveConfig();return handled; }
    @Override public void removed() { MonkeyClient.saveConfig(); }
    @Override public void onClose() { MonkeyClient.saveConfig();if(parent!=null)minecraft.setScreenAndShow(parent);else super.onClose(); }
    @Override public boolean isPauseScreen() { return false; }
    static void outline(GuiGraphicsExtractor g,int x1,int y1,int x2,int y2,int colour) {
        g.fill(x1,y1,x2,y1+1,colour);g.fill(x1,y2-1,x2,y2,colour);g.fill(x1,y1,x1+1,y2,colour);g.fill(x2-1,y1,x2,y2,colour);
    }
}
