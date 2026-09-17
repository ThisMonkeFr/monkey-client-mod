package gg.monkeyclient.integration.cache;

import java.util.function.LongConsumer;
import net.minecraft.world.level.ChunkPos;

public class VisibleChunksTracker {
    private int centerX, centerZ, viewDistance = -1;

    public void updateCenter(int centerX, int centerZ, LongConsumer unload, LongConsumer load) {
        update(centerX, centerZ, viewDistance, unload, load);
    }

    public void updateViewDistance(int viewDistance, LongConsumer unload, LongConsumer load) {
        update(centerX, centerZ, viewDistance, unload, load);
    }

    public void update(int newCenterX, int newCenterZ, int newViewDistance, LongConsumer unload, LongConsumer load) {
        int oldCenterX = this.centerX;
        int oldCenterZ = this.centerZ;
        int oldViewDistance = this.viewDistance;
        if (oldCenterX != newCenterX || oldCenterZ != newCenterZ || oldViewDistance != newViewDistance) {
            if (unload != null) {
                for (int x = oldCenterX - oldViewDistance; x <= oldCenterX + oldViewDistance; x++) {
                    boolean xOutsideNew = x < newCenterX - newViewDistance || x > newCenterX + newViewDistance;
                    for (int z = oldCenterZ - oldViewDistance; z <= oldCenterZ + oldViewDistance; z++) {
                        boolean zOutsideNew = z < newCenterZ - newViewDistance || z > newCenterZ + newViewDistance;
                        if (xOutsideNew || zOutsideNew) {
                            unload.accept(ChunkPos.pack(x, z));
                        }
                    }
                }
            }

            if (load != null) {
                for (int x = newCenterX - newViewDistance; x <= newCenterX + newViewDistance; x++) {
                    boolean xOutsideOld = x < oldCenterX - oldViewDistance || x > oldCenterX + oldViewDistance;
                    for (int z = newCenterZ - newViewDistance; z <= newCenterZ + newViewDistance; z++) {
                        boolean zOutsideOld = z < oldCenterZ - oldViewDistance || z > oldCenterZ + oldViewDistance;
                        if (xOutsideOld || zOutsideOld) {
                            load.accept(ChunkPos.pack(x, z));
                        }
                    }
                }
            }

            this.centerX = newCenterX;
            this.centerZ = newCenterZ;
            this.viewDistance = newViewDistance;
        }
    }

    public boolean isInViewDistance(int x, int z) {
        boolean xInside = x >= centerX - viewDistance && x <= centerX + viewDistance;
        boolean zInside = z >= centerZ - viewDistance && z <= centerZ + viewDistance;
        return xInside && zInside;
    }

    /** Same visibility changes over known cache entries, independent of empty terrain area. */
    public void updateSparse(int newX,int newZ,int distance,Iterable<Long> known,LongConsumer unload,LongConsumer load){
        if(centerX==newX&&centerZ==newZ&&viewDistance==distance)return;
        for(long pos:known){int x=ChunkPos.getX(pos),z=ChunkPos.getZ(pos);boolean before=isInViewDistance(x,z);boolean after=Math.abs((long)x-newX)<=distance&&Math.abs((long)z-newZ)<=distance;
            if(before&&!after)unload.accept(pos);else if(!before&&after)load.accept(pos);
        }
        centerX=newX;centerZ=newZ;viewDistance=distance;
    }

    public void forEach(LongConsumer consumer) {
        for (int x = centerX - viewDistance; x <= centerX + viewDistance; x++) {
            for (int z = centerZ - viewDistance; z <= centerZ + viewDistance; z++) {
                consumer.accept(ChunkPos.pack(x, z));
            }
        }
    }
}
