import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.modules.Brightness;
import gg.monkeyclient.modules.Zoom;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;

public class Verify {
    static int checks;
    static void check(boolean ok,String name) { if(!ok)throw new AssertionError(name); checks++; }
    static Object value(AnnotationNode a,String key) {
        if(a.values==null)return null;
        for(int i=0;i<a.values.size();i+=2)if(a.values.get(i).equals(key))return a.values.get(i+1);
        return null;
    }
    static ClassNode read(Path p)throws Exception {var n=new ClassNode();new ClassReader(Files.readAllBytes(p)).accept(n,0);return n;}
    static ClassNode gameClass(String name)throws Exception {try(var in=Verify.class.getClassLoader().getResourceAsStream(name+".class")){if(in==null)throw new AssertionError(name);var n=new ClassNode();new ClassReader(in).accept(n,0);return n;}}
    public static void main(String[] args)throws Exception {
        var n=new NumberSetting("n","Scale",3,1,6,.1);
        n.set(20d);check(n.get()==6,"Clamp maximum");
        n.set(-5d);check(n.get()==1,"Clamp minimum");
        n.set(3.44);check(n.get()==3.4,"Snap step");
        n.set(Double.NaN);check(n.get()==3.4,"Reject NaN");
        n.set(Double.POSITIVE_INFINITY);check(n.get()==3.4,"Reject infinity");
        var restored=new NumberSetting("n","Scale",3,1,6,.1);restored.load(n.save());check(restored.get().equals(n.get()),"Number round trip");
        var b=new BoolSetting("b","Toggle",false);b.toggle();var b2=new BoolSetting("b","Toggle",false);b2.load(b.save());check(b2.get(),"Boolean round trip");
        var c=new ColorSetting("c","Colour",0x80ABCDEF);c.rainbow=true;
        var c2=new ColorSetting("c","Colour",0);c2.load(c.save());check(c2.get()==0x80ABCDEF&&c2.rainbow,"ARGB + rainbow round trip");
        c.reset();check(!c.rainbow&&c.get()==0x80ABCDEF,"Colour reset clears rainbow");
        var e=new EnumSetting("e","Mode","Hold",List.of("Hold","Toggle"));e.cycle();check(e.get().equals("Toggle"),"Enum cycle");
        e.load(new com.google.gson.JsonPrimitive("invalid"));check(e.get().equals("Toggle"),"Reject invalid enum");
        var key=new KeySetting("key","Bind",67);key.set(71);var key2=new KeySetting("key","Bind",67);key2.load(key.save());check(key2.get()==71,"Binding round trip");
        key2.load(new com.google.gson.JsonPrimitive(99999));check(key2.get()==71,"Reject invalid key");
        var light=new Brightness();check(light.effective()==5,"Toggled gamma percentage");
        light.boosted.set(false);check(light.effective()==1,"Default gamma percentage");
        light.defaultGamma.set(150d);check(light.effective()==1.5,"Custom default gamma");
        light.resetAll();check(light.effective()==5&&light.defaultGamma.get()==100,"Module reset resets gamma settings");
        var zoom=new Zoom();check(zoom.fovFactor()==1,"Zoom starts neutral");
        check(!zoom.scroll(1),"Inactive zoom leaves scroll alone");
        var toggled=Zoom.class.getDeclaredField("toggled");toggled.setAccessible(true);toggled.set(zoom,true);
        zoom.smooth.set(0d);zoom.advanceFrame();check(zoom.fovFactor()==.25,"World zoom divisor");
        check(zoom.scroll(1),"Active zoom consumes scrolling");zoom.advanceFrame();check(zoom.fovFactor()<.25,"Scroll zooms closer");
        zoom.scroll.set(false);double before=zoom.fovFactor();zoom.scroll(1);zoom.advanceFrame();check(zoom.fovFactor()==before,"Disabled scroll adjustment preserves level");
        Thread.sleep(20);zoom.advanceFrame();check(zoom.crosshairAlpha()>0&&zoom.crosshairAlpha()<1,"Zoom fades only the crosshair smoothly");float world=zoom.zoomFov(90);check(world<90&&zoom.visualScale()>1,"HUD grows with world magnification");check(zoom.handFov(70)<70,"Both hands share zoom magnification");
        zoom.onToggle(false);check(zoom.fovFactor()==1&&zoom.crosshairAlpha()==1,"Zoom reset restores camera and crosshair");
        check(gg.monkeyclient.modules.TiersDisplay.parseRankings("{\"rankings\":{\"sword\":{\"tier\":2,\"pos\":0},\"axe\":{\"tier\":1,\"pos\":1,\"retired\":true}}}","best",false).equals("HT2 sword"),"Tier parsing excludes retired rankings");
        check(gg.monkeyclient.modules.TiersDisplay.parseMcpvp("title=\"Overall tier: HT2\"","overall").equals("HT2 overall"),"MCPVP overall tier parsing");
        String kits="title=\"Overall tier: HT1\" title=\"Sword LT2 — counts\" title=\"Mace HT2 — counts\" title=\"Crystal MT2\"";
        check(gg.monkeyclient.modules.TiersDisplay.parseMcpvp(kits,"best").equals("HT2 Mace"),"MCPVP best selects a kit and excludes overall");
        check(gg.monkeyclient.modules.TiersDisplay.parseMcpvp(kits,"Sword").equals("LT2 Sword"),"MCPVP selected kit remains selectable");
        check(gg.monkeyclient.modules.TiersDisplay.tierScore("HT2 sword")<gg.monkeyclient.modules.TiersDisplay.tierScore("MT2 Crystal"),"Cross-list high tiers beat middle tiers");
        var sprint=new gg.monkeyclient.modules.ToggleSprint();sprint.flight.set(100d);check(sprint.flight.get()==100,"Flight speed accepts 100");
        check(gg.monkeyclient.modules.ToggleSprint.flightSpeed(10)==.5f,"10x flight is always ten times vanilla speed");
        check(gg.monkeyclient.modules.ToggleSprint.flightSpeed(1)==.05f,"Disabled boost restores vanilla flight");
        check(gg.monkeyclient.modules.ToggleSprint.flightSpeed(Double.NaN)==.05f,"Invalid saved flight cannot poison movement");
        check(gg.monkeyclient.modules.ToggleSprint.flightSpeed(1000000)==5f,"Flight cannot exceed configured maximum");
        for(String preset:List.of("pvp","hoplite")){var data=com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/monkeyclient/presets/"+preset+".json"))).getAsJsonObject();check(data.get("format").getAsString().equals("monkeyclient-profile"),"Preset format "+preset);check(data.getAsJsonObject("settings").getAsJsonObject("modules").size()==22,"Preset covers all modules "+preset);}
        check(sprint.flightMode.get().equals("Hold key"),"Flight boost requires held key by default");
        sprint.flightMode.set("Automatic");check(sprint.flightMode.get().equals("Automatic"),"Automatic flight remains selectable");
        var waypoint=new gg.monkeyclient.modules.Waypoints();var point=new com.google.gson.JsonObject();point.addProperty("color",0xFF123456);
        check(waypoint.blockColor(point)==0xFF123456,"Existing waypoint block colors inherit their marker color");
        point.addProperty("blockColor",0xFFABCDEF);check(waypoint.blockColor(point)==0xFFABCDEF&&waypoint.pointColor(point)==0xFF123456,"Waypoint block and marker colors remain independent");
        Path compiled=Path.of(args[0]);
        try(var files=Files.list(compiled.resolve("gg/monkeyclient/mixin"))) {
            for(Path file:files.filter(p->p.toString().endsWith(".class")).toList()) {
                ClassNode mixin=read(file);
                var config=com.google.gson.JsonParser.parseString(Files.readString((Files.exists(Path.of("src/main/resources/monkeyclient.mixins.json"))?Path.of("src/main/resources/monkeyclient.mixins.json"):Path.of("work/mod-source/monkey-mod/src/main/resources/monkeyclient.mixins.json")))).getAsJsonObject();
                check(config.getAsJsonArray("client").asList().stream().anyMatch(x->x.getAsString().equals(file.getFileName().toString().replace(".class",""))),"Mixin registered "+file);
                var ma=mixin.invisibleAnnotations.stream().filter(a->a.desc.endsWith("/Mixin;")).findFirst().orElseThrow();
                Type targetType=((List<Type>)value(ma,"value")).getFirst();
                ClassNode target=gameClass(targetType.getInternalName());
                for(MethodNode method:mixin.methods) {
                    List<AnnotationNode> anns=new ArrayList<>();
                    if(method.visibleAnnotations!=null)anns.addAll(method.visibleAnnotations);
                    if(method.invisibleAnnotations!=null)anns.addAll(method.invisibleAnnotations);
                    for(var a:anns) {
                        if(a.desc.endsWith("/Accessor;")){String field=(String)value(a,"value");check(target.fields.stream().anyMatch(f->f.name.equals(field)&&f.desc.equals(Type.getReturnType(method.desc).getDescriptor())),"Accessor field "+field);}
                        if(a.desc.endsWith("/Shadow;"))check(target.methods.stream().anyMatch(m->m.name.equals(method.name)&&m.desc.equals(method.desc)),"Shadow method "+method.name);
                        if(!a.desc.endsWith("/Inject;")&&!a.desc.endsWith("/Redirect;")&&!a.desc.endsWith("/ModifyVariable;"))continue;
                        var names=(List<String>)value(a,"method");
                        for(String name:names) {
                            List<MethodNode> targets=target.methods.stream().filter(m->m.name.equals(name)|| (m.name+m.desc).equals(name)).toList();
                            check(!targets.isEmpty(),"Mixin target "+target.name+"."+name);
                            Object ats=value(a,"at");List<AnnotationNode> atList=ats instanceof List<?>?(List<AnnotationNode>)ats:List.of((AnnotationNode)ats);
                            for(var at:atList)if("INVOKE".equals(value(at,"value"))) {
                                String spec=(String)value(at,"target");int end=spec.indexOf(';'),paren=spec.indexOf('(');
                                String owner=spec.substring(1,end),call=spec.substring(end+1,paren),desc=spec.substring(paren);
                                int hits=0;
                                for(var tm:targets)for(var ins:tm.instructions)if(ins instanceof MethodInsnNode mi&&mi.owner.equals(owner)&&mi.name.equals(call)&&mi.desc.equals(desc)) {
                                    hits++;
                                    if(a.desc.endsWith("/Redirect;")) {
                                        String expected="("+((mi.getOpcode()==Opcodes.INVOKESTATIC)?"":"L"+owner+";")+desc.substring(1);
                                        check(method.desc.equals(expected),"Redirect descriptor "+method.name);
                                    }
                                }
                                check(hits>0,"Invocation exists "+spec);
                            }
                            if(a.desc.endsWith("/Inject;"))for(var tm:targets) {
                                Type[] handler=Type.getArgumentTypes(method.desc),original=Type.getArgumentTypes(tm.desc);
                                String callback=Type.getReturnType(tm.desc).getSort()==Type.VOID?"CallbackInfo;":"CallbackInfoReturnable;";
                                check(handler[handler.length-1].getDescriptor().endsWith(callback),"Callback kind "+method.name);
                                if(handler.length>1)check(Arrays.equals(Arrays.copyOf(handler,handler.length-1),original),"Callback arguments "+method.name);
                            }
                        }
                    }
                }
                for(var field:mixin.fields) {
                    var anns=field.visibleAnnotations;
                    if(anns!=null&&anns.stream().anyMatch(a->a.desc.endsWith("/Shadow;")))check(target.fields.stream().anyMatch(f->f.name.equals(field.name)&&f.desc.equals(field.desc)),"Shadow field "+field.name);
                }
            }
        }
        var camera=gameClass("net/minecraft/client/Camera");
        var update=camera.methods.stream().filter(m->m.name.equals("update")).findFirst().orElseThrow();
        check(Arrays.stream(update.instructions.toArray()).anyMatch(i->i instanceof MethodInsnNode m&&m.name.equals("calculateFov")),"World update uses calculateFov");
        check(!Files.exists(compiled.resolve("net/minecraft/client/Camera.class")),"Output contains no Minecraft classes");
        System.out.println("PASS: "+checks+" regression and Minecraft 26.2 mixin-target checks.");
    }
}
