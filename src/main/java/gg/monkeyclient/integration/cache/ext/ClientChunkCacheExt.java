package gg.monkeyclient.integration.cache.ext;

import gg.monkeyclient.integration.cache.FakeChunkManager;
import gg.monkeyclient.integration.cache.VisibleChunksTracker;

public interface ClientChunkCacheExt {
    FakeChunkManager bobby_getFakeChunkManager();
    VisibleChunksTracker bobby_getRealChunksTracker();
    void bobby_onFakeChunkAdded(int x, int z);
    void bobby_onFakeChunkRemoved(int x, int z, boolean willBeReplaced);
}
