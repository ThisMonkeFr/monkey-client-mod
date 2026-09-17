package gg.monkeyclient.integration.cache;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
/** Sparse region-header index: a 1,000-chunk radius never queues millions of missing chunks. */
public final class CachedChunkIndex {
 public final Set<Long> positions=ConcurrentHashMap.newKeySet();
 public volatile boolean ready;
 public CachedChunkIndex(List<Path> directories){Thread.ofVirtual().name("Monkey cached terrain index").start(()->{try{for(var directory:directories)scan(directory,positions);}finally{ready=true;}});}
 public static void scan(Path directory,Set<Long> positions){
  if(!Files.isDirectory(directory))return;
  try(var files=Files.newDirectoryStream(directory,"r.*.*.mca")){
   for(var path:files)try{
    String[] name=path.getFileName().toString().split("\\.");if(name.length!=4)continue;
    int rx=Integer.parseInt(name[1]),rz=Integer.parseInt(name[2]);
    try(var file=new RandomAccessFile(path.toFile(),"r")){if(file.length()<4096)continue;for(int i=0;i<1024;i++){int offset=file.readInt();if((offset>>>8)!=0&&(offset&255)!=0){int x=rx*32+(i&31),z=rz*32+(i>>5);positions.add((x&0xffffffffL)|((z&0xffffffffL)<<32));}}}
   }catch(IOException|NumberFormatException ignored){}
  }catch(IOException ignored){}
 }
}
