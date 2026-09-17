import javax.tools.*;
import java.nio.file.*;
import java.io.*;
import java.net.*;
import java.util.*;
public class CompileDriver {
 static class ClassFile extends SimpleJavaFileObject {
  final Path path; final String binary;
  ClassFile(Path path,String binary){super(path.toUri(),Kind.CLASS);this.path=path;this.binary=binary;}
  public InputStream openInputStream() throws IOException {return Files.newInputStream(path);}
 }
 public static void main(String[] args) throws Exception {
  List<Path> roots=Arrays.stream(args[0].split(";")).map(Path::of).toList();var compiler=ToolProvider.getSystemJavaCompiler(); var standard=compiler.getStandardFileManager(null,null,null);
  var manager=new ForwardingJavaFileManager<StandardJavaFileManager>(standard){
   @Override public Iterable<JavaFileObject> list(Location loc,String pkg,Set<JavaFileObject.Kind> kinds,boolean recurse)throws IOException {
    if(loc!=StandardLocation.CLASS_PATH)return super.list(loc,pkg,kinds,recurse);
    List<JavaFileObject> out=new ArrayList<>();Set<String> seen=new HashSet<>();for(Path cp:roots){Path dir=cp.resolve(pkg.replace('.','/'));
    if(kinds.contains(JavaFileObject.Kind.CLASS)&&Files.isDirectory(dir))try(var stream=Files.walk(dir,recurse?Integer.MAX_VALUE:1)){
     stream.filter(p->p.toString().endsWith(".class")).forEach(p->{String n=cp.relativize(p).toString().replace('\\','.').replace('/','.');if(seen.add(n))out.add(new ClassFile(p,n.substring(0,n.length()-6)));});
    }}return out;
   }
   @Override public String inferBinaryName(Location loc,JavaFileObject file){return file instanceof ClassFile c?c.binary:super.inferBinaryName(loc,file);}
   @Override public JavaFileObject getJavaFileForInput(Location loc,String name,JavaFileObject.Kind kind)throws IOException {
    if(loc==StandardLocation.CLASS_PATH&&kind==JavaFileObject.Kind.CLASS){for(Path cp:roots){Path p=cp.resolve(name.replace('.','/')+".class");if(Files.isRegularFile(p))return new ClassFile(p,name);}return null;}
    return super.getJavaFileForInput(loc,name,kind);
   }
  };
  List<File> sources=new ArrayList<>();try(var stream=Files.walk(Path.of(args[1]))){stream.filter(p->p.toString().endsWith(".java")).forEach(p->sources.add(p.toFile()));}
  boolean ok=compiler.getTask(null,manager,null,List.of("--release",args.length>3?args[3]:"25","-encoding","UTF-8","-proc:none","-Xmaxerrs","200","-d",args[2]),null,standard.getJavaFileObjectsFromFiles(sources)).call();
  System.out.println("Compiled "+sources.size()+" Java sources: "+ok);System.exit(ok?0:1);
 }
}
