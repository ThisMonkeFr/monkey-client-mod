import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import gg.monkeyclient.integration.cache.*;
import net.minecraft.world.level.ChunkPos;
import com.google.gson.*;
/** Checks native targets without starting OpenGL or using an account. */
public class ExtendedVerify extends Verify {
 static List<AnnotationNode> annotations(List<AnnotationNode> a,List<AnnotationNode> b){var list=new ArrayList<AnnotationNode>();if(a!=null)list.addAll(a);if(b!=null)list.addAll(b);return list;}
 static boolean field(ClassNode type,String name,String desc)throws Exception {if(type.fields.stream().anyMatch(f->f.name.equals(name)&&f.desc.equals(desc)))return true;return type.superName!=null&&field(gameClass(type.superName),name,desc);}
 static String inferred(String name){name=name.replaceFirst("^.*\\$","").replaceFirst("^(get|set|is)","");return Character.toLowerCase(name.charAt(0))+name.substring(1);}
 public static void main(String[] args)throws Exception {
  Path compiled=Path.of(args[0]),resources=Path.of(args[1]);int mixins=0;
  for(String configName:List.of("monkeyclient.mixins.json","monkeycache.mixins.json")){
   var config=JsonParser.parseString(Files.readString(resources.resolve(configName))).getAsJsonObject();
   for(var entry:config.getAsJsonArray("client")){
    var mixin=read(compiled.resolve((config.get("package").getAsString()+"."+entry.getAsString()).replace('.','/')+".class"));mixins++;
    var ma=annotations(mixin.visibleAnnotations,mixin.invisibleAnnotations).stream().filter(a->a.desc.endsWith("/Mixin;")).findFirst().orElseThrow();
    var types=(List<Type>)value(ma,"value");if(types==null)throw new AssertionError("Missing class target "+mixin.name);
    for(var targetType:types){var target=gameClass(targetType.getInternalName());
     for(var f:mixin.fields)for(var a:annotations(f.visibleAnnotations,f.invisibleAnnotations))if(a.desc.endsWith("/Shadow;"))check(field(target,f.name,f.desc),"Shadow field "+target.name+"."+f.name);
     for(var m:mixin.methods)for(var a:annotations(m.visibleAnnotations,m.invisibleAnnotations)){
      if(a.desc.endsWith("/Accessor;")){String name=(String)value(a,"value");if(name==null)name=inferred(m.name);Type ret=Type.getReturnType(m.desc);check(field(target,name,(ret.getSort()==Type.VOID?Type.getArgumentTypes(m.desc)[0]:ret).getDescriptor()),"Accessor "+target.name+"."+name);}
      var names=(List<String>)value(a,"method");if(names==null)continue;
      for(String name:names){var targets=target.methods.stream().filter(tm->tm.name.equals(name)||(tm.name+tm.desc).equals(name)).toList();check(!targets.isEmpty(),"Target "+target.name+"."+name);
       Object ats=value(a,"at");List<AnnotationNode> atList=ats==null?List.of():ats instanceof List<?>?(List<AnnotationNode>)ats:List.of((AnnotationNode)ats);
       for(var at:atList){String kind=(String)value(at,"value"),spec=(String)value(at,"target");if(spec==null)continue;int hits=0;
        for(var tm:targets)for(var ins:tm.instructions){if(ins instanceof MethodInsnNode mi&&("INVOKE".equals(kind)||"INVOKE_ASSIGN".equals(kind))&&spec.equals("L"+mi.owner+";"+mi.name+mi.desc))hits++;if(ins instanceof FieldInsnNode fi&&"FIELD".equals(kind)&&spec.equals("L"+fi.owner+";"+fi.name+":"+fi.desc))hits++;}
        if("INVOKE".equals(kind)||"INVOKE_ASSIGN".equals(kind)||"FIELD".equals(kind))check(hits>0,"Instruction "+mixin.name+" "+spec);
       }
       if(a.desc.endsWith("/Inject;"))for(var tm:targets){Type[] handler=Type.getArgumentTypes(m.desc),original=Type.getArgumentTypes(tm.desc);int ci=-1;for(int i=0;i<handler.length;i++)if(handler[i].getDescriptor().contains("/CallbackInfo")){ci=i;break;}check(ci>=0,"Callback exists "+m.name);String kind=Type.getReturnType(tm.desc).getSort()==Type.VOID?"/CallbackInfo;":"/CallbackInfoReturnable;";check(handler[ci].getDescriptor().endsWith(kind),"Callback type "+m.name);if(ci>0)check(Arrays.equals(Arrays.copyOf(handler,ci),original),"Callback arguments "+mixin.name+"."+m.name);}
       if(a.desc.endsWith("/WrapMethod;"))for(var tm:targets){Type[] handler=Type.getArgumentTypes(m.desc);check(Arrays.equals(Arrays.copyOf(handler,handler.length-1),Type.getArgumentTypes(tm.desc)),"Wrapped method arguments "+m.name);}
      }
     }
    }
   }
  }
  var known=new HashSet<Long>(List.of(ChunkPos.pack(-1000,-1000),ChunkPos.pack(1000,1000),ChunkPos.pack(0,0),ChunkPos.pack(2000,0)));var loaded=new HashSet<Long>();var removed=new HashSet<Long>();var tracker=new VisibleChunksTracker();
  tracker.updateSparse(0,0,1000,known,removed::add,loaded::add);check(loaded.size()==3&&removed.isEmpty(),"1000 radius loads known chunks only");loaded.clear();
  tracker.updateSparse(1,0,1000,known,removed::add,loaded::add);check(removed.equals(Set.of(ChunkPos.pack(-1000,-1000)))&&loaded.isEmpty(),"Sparse movement unloads leaving edge");removed.clear();
  tracker.updateSparse(1,0,-1,known,removed::add,loaded::add);check(removed.size()==2,"Disabling cache unloads every visible entry");
  Path temp=Files.createTempDirectory("monkey-cache-test");try{byte[] header=new byte[4096];java.nio.ByteBuffer.wrap(header).putInt(2*256+1).position(1023*4).putInt(3*256+1);Files.write(temp.resolve("r.-2.3.mca"),header);var positions=new HashSet<Long>();CachedChunkIndex.scan(temp,positions);check(positions.equals(Set.of(ChunkPos.pack(-64,96),ChunkPos.pack(-33,127))),"Region headers include negative coordinates and last slot");}finally{Files.deleteIfExists(temp.resolve("r.-2.3.mca"));Files.delete(temp);}
  for(String file:List.of("motion","hand_fade")){var json=JsonParser.parseString(Files.readString(resources.resolve("assets/monkeyclient/post_effect/"+file+".json")));var result=net.minecraft.client.renderer.PostChainConfig.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,json);check(result.error().isEmpty(),"Native post-chain codec "+file+" "+result.error());}
  System.out.println("PASS: "+checks+" additional checks across "+mixins+" mixins, sparse cache and native post-processing resources.");
 }
}
