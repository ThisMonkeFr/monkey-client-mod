import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
public class MixinAudit {
 static List<Path> roots;static int checks,failures;
 static Path game(String name){for(Path root:roots){Path p=root.resolve(name+".class");if(Files.isRegularFile(p))return p;}return roots.get(0).resolve(name+".class");}
 static void check(boolean ok,String msg){checks++;if(!ok){failures++;System.out.println("FAIL "+msg);}}
 static ClassNode read(Path p)throws Exception{ClassNode n=new ClassNode();new ClassReader(Files.readAllBytes(p)).accept(n,0);return n;}
 static List<AnnotationNode> anns(List<AnnotationNode>a,List<AnnotationNode>b){var l=new ArrayList<AnnotationNode>();if(a!=null)l.addAll(a);if(b!=null)l.addAll(b);return l;}
 static Object val(AnnotationNode a,String k){if(a.values!=null)for(int i=0;i<a.values.size();i+=2)if(k.equals(a.values.get(i)))return a.values.get(i+1);return null;}
 static boolean field(ClassNode c,String name,String desc)throws Exception{return c.fields.stream().anyMatch(f->f.name.equals(name)&&f.desc.equals(desc))||c.superName!=null&&Files.exists(game(c.superName))&&field(read(game(c.superName)),name,desc);}
 public static void main(String[]args)throws Exception{
  Path compiled=Path.of(args[0]),res=Path.of(args[2]);roots=Arrays.stream(args[1].split(";")).map(Path::of).toList();
  for(String conf:List.of("monkeyclient.mixins.json","monkeycache.mixins.json")){
   if(!Files.exists(res.resolve(conf)))continue;
   var j=JsonParser.parseString(Files.readString(res.resolve(conf))).getAsJsonObject();
   for(var e:j.getAsJsonArray("client")){
    String name=j.get("package").getAsString()+"."+e.getAsString();var m=read(compiled.resolve(name.replace('.','/')+".class"));
    var mix=anns(m.visibleAnnotations,m.invisibleAnnotations).stream().filter(a->a.desc.endsWith("/Mixin;")).findFirst().orElseThrow();
    var types=(List<Type>)val(mix,"value");if(types==null){check(false,"Missing target "+name);continue;}
    for(var type:types){Path file=game(type.getInternalName());if(!Files.exists(file)){check(false,"Class "+type);continue;}var t=read(file);
     for(var f:m.fields)for(var a:anns(f.visibleAnnotations,f.invisibleAnnotations))if(a.desc.endsWith("/Shadow;"))check(field(t,f.name,f.desc),name+" shadow "+f.name+f.desc);
     for(var h:m.methods)for(var a:anns(h.visibleAnnotations,h.invisibleAnnotations)){
      if(a.desc.endsWith("/Accessor;")){String fn=(String)val(a,"value");if(fn!=null){var rt=Type.getReturnType(h.desc);check(field(t,fn,(rt.getSort()==Type.VOID?Type.getArgumentTypes(h.desc)[0]:rt).getDescriptor()),name+" accessor "+fn);}}
      if(a.desc.endsWith("/Shadow;"))check(t.methods.stream().anyMatch(tm->tm.name.equals(h.name)&&tm.desc.equals(h.desc)),name+" shadow method "+h.name);
      var names=(List<String>)val(a,"method");if(names==null)continue;
      for(String mn:names){var targets=t.methods.stream().filter(tm->tm.name.equals(mn)||(tm.name+tm.desc).equals(mn)).toList();check(!targets.isEmpty(),name+" target "+mn);
       Object at=val(a,"at");List<AnnotationNode> ats=at instanceof List<?>?(List<AnnotationNode>)at:at==null?List.of():List.of((AnnotationNode)at);
       for(var loc:ats){String kind=(String)val(loc,"value"),spec=(String)val(loc,"target");if(spec==null)continue;int hits=0;for(var tm:targets)for(var in:tm.instructions){if(in instanceof MethodInsnNode x&&spec.equals("L"+x.owner+";"+x.name+x.desc))hits++;if(in instanceof FieldInsnNode x&&spec.equals("L"+x.owner+";"+x.name+":"+x.desc))hits++;}if(List.of("INVOKE","INVOKE_ASSIGN","FIELD").contains(kind))check(hits>0,name+" instruction "+spec);}
       if(a.desc.endsWith("/ModifyArg;"))for(var loc:ats){String spec=(String)val(loc,"target");if(spec==null||!spec.contains("("))continue;var call=Type.getArgumentTypes(spec.substring(spec.indexOf('(')));int index=val(a,"index") instanceof Integer i?i:-1;var ret=Type.getReturnType(h.desc);check(index<0?Arrays.stream(call).filter(ret::equals).count()==1:index<call.length&&call[index].equals(ret),name+" modified argument "+h.name+" "+spec);}
       for(var tm:targets){var ha=Type.getArgumentTypes(h.desc);var ta=Type.getArgumentTypes(tm.desc);
        if(a.desc.endsWith("/ModifyVariable;")&&Boolean.TRUE.equals(val(a,"argsOnly"))){int ordinal=val(a,"ordinal") instanceof Integer i?i:0;long count=Arrays.stream(ta).filter(arg->arg.equals(ha[0])).count();check(count>ordinal,name+" variable argument "+h.name+" expected "+tm.desc);}
        if(a.desc.endsWith("/Inject;")){int i=0;while(i<ha.length&&!ha[i].getDescriptor().contains("/CallbackInfo"))i++;check(i<ha.length,name+" callback "+h.name);if(i<ha.length){check(ha[i].getDescriptor().endsWith(Type.getReturnType(tm.desc).getSort()==Type.VOID?"/CallbackInfo;":"/CallbackInfoReturnable;"),name+" return "+h.name);if(i>0)check(Arrays.equals(Arrays.copyOf(ha,i),ta),name+" args "+h.name+" expected "+tm.desc);}}
        if(a.desc.endsWith("/WrapMethod;"))check(Arrays.equals(Arrays.copyOf(ha,ha.length-1),ta),name+" wrap args "+h.name+" expected "+tm.desc);
       }
      }
     }
    }
   }
  }
  System.out.println(checks+" mixin checks, "+failures+" failures");System.exit(failures==0?0:1);
 }
}
