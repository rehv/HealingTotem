package com.github.tprk77.healingtotem.util.structure;

import org.bukkit.block.Block;

/**
 * An immutable type representing a block offset. Simply an integer triple of
 * <x, y, z>. There is no "world" member. Mostly used for getting "relative"
 * blocks.
 *
 * @author tim
 */
public final class BlockOffset {

	protected final int x;
	protected final int y;
	protected final int z;

	public BlockOffset(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockOffset(BlockOffset offset){
		x = offset.x;
		y = offset.y;
		z = offset.z;
	}

	public BlockOffset(Block block){
		x = block.getX();
		y = block.getY();
		z = block.getZ();
	}

	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public int getZ(){
		return z;
	}

	public BlockOffset add(BlockOffset o){
		return new BlockOffset(x + o.x, y + o.y, z + o.z);
	}

	public BlockOffset subtract(BlockOffset o){
		return new BlockOffset(x - o.x, y - o.y, z - o.z);
	}

	@Override
	public String toString(){
		return "<" + x + ", " + y + ", " + z + ">";
	}
}
