package gg.monkeyclient.platform;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
public interface ClientCommandSource {
 ClientLevel getLevel();
 void sendFeedback(Component message);
 void sendError(Component message);
}
