package com.github.tprk77.healingtotem.util.structure;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * 
 * @author tim
 */
public class Structure {

    protected final StructureType structuretype;
    protected final Block rootblock;
    protected final Set<Block> blocks;

    /**
     * Create a structure, by searching for a structure type.
     * 
     * @param structuretypes A list of structure types to search for.
     * @param block Where to start the search. This could be any block in the
     *            structure, or the "root" block with offset <0, 0, 0> (which is not
     *            necessarily part of the structure).
     */
    public Structure(List<StructureType> structuretypes, Block block) {

        for (final StructureType possiblestructuretype : structuretypes) {
            final Map<BlockOffset, Material> pattern = possiblestructuretype.getPattern();

            if (pattern.containsValue(block.getType())) {
                final Map<Material, List<BlockOffset>> reversepattern = possiblestructuretype.getReversePattern();
                final List<BlockOffset> offsets = reversepattern.get(block.getType());

                for (final BlockOffset offset : offsets) {
                    final Block possiblerootblock = block.getRelative(-offset.x, -offset.y, -offset.z);
                    final Set<Block> possibleblocks = verifyStructure(possiblestructuretype, possiblerootblock);

                    if (possibleblocks != null) {
                        structuretype = possiblestructuretype;
                        rootblock = possiblerootblock;
                        blocks = possibleblocks;
                        return;
                    }
                }
            } else {
                // block might be the root block which is not part of the structure
                final Set<Block> possibleblocks = verifyStructure(possiblestructuretype, block);

                if (possibleblocks != null) {
                    structuretype = possiblestructuretype;
                    rootblock = block;
                    blocks = possibleblocks;
                    return;
                }
            }
        }

        structuretype = null;
        rootblock = null;
        blocks = null;
    }

    public StructureType getStructureType() {
        return structuretype;
    }

    public Block getRootBlock() {
        return rootblock;
    }

    public Set<Block> getBlocks() {
        // defend against outisde adding/removing
        return new HashSet<Block>(blocks);
    }

    public World getWorld() {
        return rootblock.getWorld();
    }

    public boolean containsBlock(Block block) {
        return blocks.contains(block);
    }

    public boolean verifyStructure() {
        return (structuretype != null && rootblock != null && blocks != null);
    }

    /*private void durp(){
    	// figure out rotations things

    	Map<BlockOffset, Material> pattern = structuretype.getPattern();
    	Map<BlockOffset, Material> rotpattern = new HashMap<BlockOffset, Material>();

    	for(BlockOffset offset)
    }*/

    /**
     * Search around the given block for the block pattern. For the search to be
     * successful v must point to a block which is part of the pattern. If the
     * block is not part of the pattern, or the pattern is incomplete,
     * then this function will return null. If the pattern is found, a
     * Block corresponding to the pattern origin will be returned.
     * 
     * @param block The Block to search around.
     * @return If the search is successful, then a Block corresponding to the
     *         pattern origin (offset <0, 0, 0>). If the search fails, then null.
     */
    /*private Block searchAtBlock(Block block){

    	Map<BlockOffset, Material> pattern = structuretype.getPattern();

    	if(!pattern.containsValue(block.getType())){
    		return null;
    	}

    	Map<Material, List<BlockOffset>> reversepattern = structuretype.getReversePattern();
    	List<BlockOffset> offsets = reversepattern.get(block.getType());

    	for(BlockOffset offset : offsets){
    		Block possiblerootblock = block.getRelative(
    						-offset.x(), -offset.y, -offset.z);

    		if(verifyStructure(possiblerootblock) != null){
    			return possiblerootblock;
    		}
    	}

    	return null;
    }*/

    /**
     * TODO THIS SHOULD GET MOVED INTO THE CONSTRUCTOR.
     * 
     * Verify that a structure exists at this root block.
     * 
     * @param rootblock The origin of the structure.
     * @return If the structure is valid return the list of blocks, or if the
     *         structure was not valid return null.
     */
    private Set<Block> verifyStructure(StructureType structuretype, Block rootblock) {

        if (rootblock == null)
            return null;

        final Map<BlockOffset, Material> pattern = structuretype.getPattern();
        final Set<Block> possibleblocks = new HashSet<Block>();

        for (final BlockOffset offset : pattern.keySet()) {
            final Block block = rootblock.getRelative(offset.x, offset.y, offset.z);

            final Material material = pattern.get(offset);
            if (block.getType() != material) {
                return null;
            } else {
                possibleblocks.add(block);
            }
        }

        return possibleblocks;
    }
}
