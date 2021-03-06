package com.github.tprk77.healingtotem.totemdao;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import com.github.tprk77.healingtotem.util.structure.Structure;

/**
 * A Totem Pole...
 * 
 * @author tim
 */
public class Totem extends Structure {

    private final TotemType totemtype;
    private final String owner;

    public Totem(TotemType totemtype, Block block) {
        this(totemtype, block, null);
    }

    public Totem(TotemType totemtype, Block block, String owner) {
        super(totemtype.getAllStructureTypes(), block);
        this.totemtype = totemtype;
        this.owner = owner;
    }

    public TotemType getTotemType() {
        return totemtype;
    }

    public String getOwner() {
        return owner;
    }

    public boolean inRange(LivingEntity livingentity) {
        try {
            final double range = totemtype.getRange();
            return getRootBlock().getLocation().distanceSquared(livingentity.getLocation()) < (range * range);
        } catch (final IllegalArgumentException ex) {
            return false;
        }
    }

    public int getEffectiveHealingPower(LivingEntity livingentity) {

        if (isPowered()) {
            return 0;
        }

        return totemtype.getEffectiveHealingPower(livingentity);
    }

    public int getEffectiveFoodPower(LivingEntity livingentity) {

        if (isPowered()) {
            return 0;
        }

        return totemtype.getFoodPower();
    }

    private boolean isPowered() {
        for (final Block block : blocks) {
            if (block.isBlockPowered() || block.isBlockIndirectlyPowered()) {
                return true;
            }
        }
        return false;
    }
}
