package gg.monkeyclient.platform;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
/** Keeps saved bindings portable across GLFW and SDL releases. */
public final class LegacyKeys {
 public static final int GLFW_KEY_B=66,GLFW_KEY_C=67,GLFW_KEY_G=71,GLFW_KEY_M=77,GLFW_KEY_LAST=348,GLFW_PRESS=1;
 public static final int GLFW_KEY_LEFT_SHIFT=340,GLFW_KEY_LEFT_CONTROL=341,GLFW_KEY_LEFT_ALT=342,GLFW_KEY_LEFT_SUPER=343,GLFW_KEY_RIGHT_SHIFT=344,GLFW_KEY_RIGHT_CONTROL=345,GLFW_KEY_RIGHT_ALT=346,GLFW_KEY_RIGHT_SUPER=347,GLFW_CURSOR=0,GLFW_CURSOR_DISABLED=1;
 private static final int[] MAP=new int[349];
 static{
  for(int i=65;i<=90;i++)MAP[i]=i-61;
  for(int i=49;i<=57;i++)MAP[i]=i-19;MAP[48]=39;
  int[][] pairs={{32,44},{39,52},{44,54},{45,45},{46,55},{47,56},{59,51},{61,46},{91,47},{92,49},{93,48},{96,53},{256,41},{257,40},{258,43},{259,42},{260,73},{261,76},{262,79},{263,80},{264,81},{265,82},{266,75},{267,78},{268,74},{269,77},{280,57},{281,71},{282,83},{283,70},{284,72},{320,98},{330,99},{331,84},{332,85},{333,86},{334,87},{335,88},{336,103},{340,225},{341,224},{342,226},{343,227},{344,229},{345,228},{346,230},{347,231},{348,118}};
  for(var p:pairs)MAP[p[0]]=p[1];for(int i=290;i<=301;i++)MAP[i]=i-232;for(int i=302;i<=313;i++)MAP[i]=i-198;for(int i=321;i<=329;i++)MAP[i]=i-232;
 }
 public static int nativeCode(int code){return code>=0&&code<MAP.length?MAP[code]:0;}
 public static int fromNative(int scan){for(int i=32;i<MAP.length;i++)if(MAP[i]==scan&&scan!=0)return i;return -1;}
 public static boolean isKeyDown(int key){int scan=nativeCode(key);return scan!=0&&InputConstants.isKeyDown(scan);}
 public static int glfwGetKey(long ignored,int key){return isKeyDown(key)?1:0;}
 public static String glfwGetKeyName(int key,int ignored){int scan=nativeCode(key);return scan==0?null:InputConstants.Type.KEYBOARD.getOrCreate(scan).getDisplayName().getString();}
 public static int glfwGetInputMode(long ignored,int mode){return Minecraft.getInstance().mouseHandler.isMouseGrabbed()?1:0;}
 public static int glfwGetMouseButton(long ignored,int button){var mouse=Minecraft.getInstance().mouseHandler;return (button==0?mouse.isLeftPressed():button==1?mouse.isRightPressed():mouse.isMiddlePressed())?1:0;}
}
