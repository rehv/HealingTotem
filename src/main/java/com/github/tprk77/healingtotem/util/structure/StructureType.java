package com.github.tprk77.healingtotem.util.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

/**
 * An immutable type representing a type of structure. This is basically used
 * as a pattern to search for. It has some protected methods used by Structure.
 * 
 * @author tim
 */
public class StructureType {

    public static final class Prototype {

        private final Map<BlockOffset, Material> protopattern;
        private final Map<Material, List<BlockOffset>> protoreversepattern;

        public Prototype() {
            protopattern = new HashMap<BlockOffset, Material>();
            protoreversepattern = new EnumMap<Material, List<BlockOffset>>(Material.class);
        }

        public void addBlock(int x, int y, int z, Material material) {
            final BlockOffset offset = new BlockOffset(x, y, z);

            if (material == null) {
                return;
            }

            protopattern.put(offset, material);

            List<BlockOffset> offsetlist;
            if (protoreversepattern.containsKey(material)) {
                offsetlist = protoreversepattern.get(material);
                offsetlist.add(offset);
            } else {
                protoreversepattern.put(material, new ArrayList<BlockOffset>(Arrays.asList(offset)));
            }
        }
    }

    /*
     * This object is supposed to be immutable. These maps should never be
     * modified by any method. Also, methods should never return a reference to
     * these maps from a method.
     */
    private final Map<BlockOffset, Material> pattern;
    private final Map<Material, List<BlockOffset>> reversepattern;

    public StructureType(Prototype proto) {
        pattern = new HashMap<BlockOffset, Material>(proto.protopattern);
        // we don't need to make new lists, because the are never public up to now
        reversepattern = new EnumMap<Material, List<BlockOffset>>(proto.protoreversepattern);
    }

    public int getBlockCount() {
        return pattern.size();
    }

    public Set<Material> getMaterials() {
        return reversepattern.keySet();
    }

    public Map<BlockOffset, Material> getPattern() {
        // members of the hash are immutable, so this should be ok
        return new HashMap<BlockOffset, Material>(pattern);
    }

    public Map<Material, List<BlockOffset>> getReversePattern() {
        // we need to make a new hash and new lists to protect the data
        final Map<Material, List<BlockOffset>> rp = new EnumMap<Material, List<BlockOffset>>(Material.class);
        for (final Material mat : reversepattern.keySet()) {
            rp.put(mat, new ArrayList<BlockOffset>(reversepattern.get(mat)));
        }
        return rp;
    }

    public List<StructureType> makeRotatedStructureTypes(Rotator rotator) {
        final List<StructureType> structuretypes = new ArrayList<StructureType>();
        final List<Prototype> prototypes = new ArrayList<Prototype>();

        for (int i = 0; i < rotator.getNumberOfRotations(); i++) {
            prototypes.add(new Prototype());
        }

        for (final BlockOffset offset : pattern.keySet()) {
            final Material material = pattern.get(offset);
            final List<BlockOffset> rotatedoffsets = rotator.getRotated(offset);

            for (int i = 0; i < rotator.getNumberOfRotations(); i++) {
                final Prototype rotatedproto = prototypes.get(i);
                final BlockOffset rotatedoffset = rotatedoffsets.get(i);
                rotatedproto.addBlock(rotatedoffset.x, rotatedoffset.y, rotatedoffset.z, material);
            }
        }

        for (int i = 0; i < rotator.getNumberOfRotations(); i++) {
            structuretypes.add(new StructureType(prototypes.get(i)));
        }

        return structuretypes;
    }

    @Override
    public String toString() {
        String s = "";
        for (final BlockOffset b : pattern.keySet()) {
            final Material m = pattern.get(b);
            s = s + "{x: " + b.x + ", y: " + b.y + ", z: " + b.z + ", type: " + m + "}, ";
        }
        return "[" + s + "]";
    }
}
