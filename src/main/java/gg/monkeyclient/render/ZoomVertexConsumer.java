package gg.monkeyclient.render;
import com.mojang.blaze3d.vertex.VertexConsumer;
/** Magnifies the complete HUD around the screen centre without changing alpha. */
public record ZoomVertexConsumer(VertexConsumer delegate,float scale,float centerX,float centerY) implements VertexConsumer {
 public VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(centerX+(x-centerX)*scale,centerY+(y-centerY)*scale,z);return this;}
 public VertexConsumer setColor(int r,int g,int b,int a){delegate.setColor(r,g,b,a);return this;}
 public VertexConsumer setColor(int c){delegate.setColor(c);return this;}
 public VertexConsumer setUv(float u,float v){delegate.setUv(u,v);return this;}
 public VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
 public VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
 public VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
 public VertexConsumer setLineWidth(float w){delegate.setLineWidth(w);return this;}
}
