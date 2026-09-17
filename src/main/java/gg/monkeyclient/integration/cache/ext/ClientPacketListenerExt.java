package gg.monkeyclient.integration.cache.ext;

import net.minecraft.client.multiplayer.ClientPacketListener;

public interface ClientPacketListenerExt {
    void bobby_queueUnloadFakeLightDataTask(Runnable runnable);

    static ClientPacketListenerExt get(ClientPacketListener handler) {
        return (ClientPacketListenerExt) handler;
    }
}
