package gg.monkeyclient.cosmetics;
import com.mojang.blaze3d.platform.NativeImage;
/** Convert the legacy mirrored-limb layout without stretching its atlas. */
public final class SkinPixels {
 public static NativeImage modern(NativeImage source){
  if(source.getWidth()!=64||source.getHeight()!=32)return source;
  NativeImage out=new NativeImage(64,64,true);for(int y=0;y<32;y++)for(int x=0;x<64;x++)out.setPixel(x,y,source.getPixel(x,y));
  int[][] pieces={{4,16,20,48,4,4},{8,16,24,48,4,4},{0,20,24,52,4,12},{4,20,20,52,4,12},{8,20,16,52,4,12},{12,20,28,52,4,12},{44,16,36,48,4,4},{48,16,40,48,4,4},{40,20,40,52,4,12},{44,20,36,52,4,12},{48,20,32,52,4,12},{52,20,44,52,4,12}};
  for(int[] p:pieces)for(int y=0;y<p[5];y++)for(int x=0;x<p[4];x++)out.setPixel(p[2]+x,p[3]+y,source.getPixel(p[0]+p[4]-1-x,p[1]+y));
  source.close();return out;
 }
 private SkinPixels(){}
}
