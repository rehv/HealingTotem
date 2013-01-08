package com.github.tprk77.healingtotem.util;

import org.bukkit.block.Block;

/**
 * TODO merge with BlockOffset?
 * 
 * Immutable and ok to use as a hash key.
 * 
 * @author tim
 */
public final class BlockHashable {

    protected final String world;
    protected final int x;
    protected final int y;
    protected final int z;

    public BlockHashable(Block block) {
        world = block.getWorld().getName();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockHashable))
            return false;
        final BlockHashable bh = (BlockHashable) o;
        return (x == bh.x && y == bh.y && z == bh.z && world.equals(bh.world));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (world != null ? world.hashCode() : 0);
        hash = 53 * hash + x;
        hash = 53 * hash + y;
        hash = 53 * hash + z;
        return hash;
    }

    @Override
    public String toString() {
        return "(" + world + ") <" + x + ", " + y + ", " + z + ">";
    }
}