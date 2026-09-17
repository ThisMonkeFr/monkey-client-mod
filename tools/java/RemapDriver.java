import java.nio.file.*;
import java.io.*;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.*;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;
public class RemapDriver {
 public static void main(String[]a)throws Exception{
  var tree=new MemoryMappingTree();try(var reader=new BufferedReader(new FileReader(a[0]))){MappingReader.read(reader,tree);}
  var builder=TinyRemapper.newRemapper().withMappings(TinyUtils.createMappingProvider(tree,a[1],a[2])).renameInvalidLocals(true);
  if(a.length>6&&a[6].equals("mixins"))builder.extension(new MixinExtension());
  var remapper=builder.build();
  try(var out=new OutputConsumerPath.Builder(Path.of(a[4])).assumeArchive(false).build()){
   for(String cp:a[5].split(";"))remapper.readClassPath(Path.of(cp));remapper.readInputs(Path.of(a[3]));remapper.apply(out);
  }finally{remapper.finish();}
  System.out.println("Remapped "+a[1]+" → "+a[2]+" into "+a[4]);
 }
}
