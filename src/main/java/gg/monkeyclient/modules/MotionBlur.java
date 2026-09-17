package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.mixin.PostChainAccessor;
import gg.monkeyclient.mixin.PostPassAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.util.Set;
/** GPU temporal accumulation, before GUI submission. No CPU framebuffer readback. */
public class MotionBlur extends Module {
 public final NumberSetting strength=add(new NumberSetting("strength","Blur strength",.45,0,.95,.01));
 public final BoolSetting movingOnly=add(new BoolSetting("movingOnly","Blur only during camera movement",true)),inMenus=add(new BoolSetting("inMenus","Blur behind menus",false));
 private com.mojang.blaze3d.buffers.GpuBuffer dynamicUniform;private PostChain uniformOwner;
 private PostChain previous;private long lastFrame;private int width,height;private double x,y,z;private float yaw,pitch;private boolean reset=true,failed;
 private final ByteBuffer uniform=ByteBuffer.allocateDirect(16).order(java.nio.ByteOrder.nativeOrder());
 public MotionBlur(){super("motionblur","Motion Blur","GPU temporal motion blur with adjustable strength",Category.VISUAL,false);}
 @Override public void onToggle(boolean on){reset=true;failed=false;lastFrame=0;}
 public void render(GraphicsResourceAllocator allocator){
  var mc=Minecraft.getInstance();
  if(!isEnabled()||failed||mc.level==null||(!inMenus.get()&&mc.gui.screen()!=null)){reset=true;return;}
  try{
   var target=mc.gameRenderer.mainRenderTarget();
   var chain=mc.getShaderManager().getPostChain(Identifier.fromNamespaceAndPath("monkeyclient","motion"),Set.of(PostChain.MAIN_TARGET_ID));
   if(chain==null)return;
   var camera=mc.gameRenderer.mainCamera();var p=camera.position();long now=System.nanoTime();
   double dt=lastFrame==0?1d/60:Math.min(.25,(now-lastFrame)/1e9);
   boolean changed=chain!=previous||target.width!=width||target.height!=height;
   double movement=Math.abs(p.x-x)+Math.abs(p.y-y)+Math.abs(p.z-z)+Math.abs(camera.yRot()-yaw)+Math.abs(camera.xRot()-pitch);
   float weight=reset||changed||movement>100||movingOnly.get()&&movement<.0001?0:(float)Math.pow(strength.get(),dt*60);
   uniform.clear();uniform.putFloat(weight).putFloat(0).putFloat(0).putFloat(0).flip();
   for(var pass:((PostChainAccessor)chain).monkeyclient$passes()){
    var uniforms=((PostPassAccessor)pass).monkeyclient$uniforms();
    if(uniforms.containsKey("MotionSettings")){
     if(uniformOwner!=chain||dynamicUniform==null||dynamicUniform.isClosed()){
      dynamicUniform=RenderSystem.getDevice().createBuffer(()->"Monkey motion settings",com.mojang.blaze3d.buffers.GpuBuffer.USAGE_UNIFORM|com.mojang.blaze3d.buffers.GpuBuffer.USAGE_COPY_DST,16);uniformOwner=chain;
     }
     var old=uniforms.put("MotionSettings",dynamicUniform);if(old!=null&&old!=dynamicUniform&&!old.isClosed())old.close();
     RenderSystem.getDevice().createCommandEncoder().writeToBuffer(dynamicUniform.slice(),uniform);
    }
   }
   chain.process(target,allocator);
   previous=chain;width=target.width;height=target.height;lastFrame=now;x=p.x;y=p.y;z=p.z;yaw=camera.yRot();pitch=camera.xRot();reset=false;
  }catch(Exception e){failed=true;MonkeyClient.LOG.warn("Motion blur unavailable: {}",e.toString());}
 }
}
