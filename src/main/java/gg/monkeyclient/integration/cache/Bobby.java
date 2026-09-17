package gg.monkeyclient.integration.cache;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.FogEditor;
/** Bridge from the adapted Bobby cache to Monkey Client's persisted World Editor settings. */
public final class Bobby {
 public static final String MOD_ID="monkeyclient";
 private static final Bobby INSTANCE=new Bobby();
 private final BobbyConfig config=new BobbyConfig();
 public static Bobby getInstance(){return INSTANCE;}
 public BobbyConfig getConfig(){return config;}
 public boolean isEnabled(){return MonkeyClient.ready()&&MonkeyClient.modules().get(FogEditor.class).extendedActive();}
}
