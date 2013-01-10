package com.github.tprk77.healingtotem.totemdao;

import java.util.List;

import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

import com.github.tprk77.healingtotem.HTPlugin;
import com.github.tprk77.healingtotem.util.structure.Rotator;
import com.github.tprk77.healingtotem.util.structure.StructureType;

/**
 * An immutable type representing totem types.
 * 
 * @author tim
 */
public final class TotemType {

    private final String name;
    private final int healingpower;
    private final int foodpower;
    private final int updaterate;
    private final double range;

    private final StructureType structuretype;
    private final List<StructureType> rotatedstructuretypes;
    private final Rotator rotator;

    private final boolean affectsplayers;
    private final boolean affectsmobs;
    private final boolean affectstamedwolves;
    private final boolean affectsangrywolves;

    public TotemType(String name, int power, double range, StructureType structuretype) {
        this(name, power, 0, range, HTPlugin.getInstance().getConfigManager().getDefaultUpdateRate(), structuretype, Rotator.NONE, true, true, true, true);
    }

    public TotemType(String name, int power, double range, StructureType structuretype, Rotator rotator) {
        this(name, power, 0, range, HTPlugin.getInstance().getConfigManager().getDefaultUpdateRate(), structuretype, rotator, true, true, true, true);
    }

    public TotemType(String name, int healingpower, int foodpower, double range, int updaterate, StructureType structuretype, Rotator rotator, boolean affectsplayers, boolean affectsmobs, boolean affectstamedwolves, boolean affectsangrywolves) {
        this.name = name;
        this.healingpower = healingpower;
        this.foodpower = foodpower;
        this.range = range;

        this.structuretype = structuretype;
        this.rotatedstructuretypes = structuretype.makeRotatedStructureTypes(rotator);
        this.rotator = rotator;

        this.affectsplayers = affectsplayers;
        this.affectsmobs = affectsmobs;
        this.affectstamedwolves = affectstamedwolves;
        this.affectsangrywolves = affectsangrywolves;
        this.updaterate = updaterate;
    }

    public String getName() {
        return name;
    }

    public int getHealingPower() {
        return healingpower;
    }

    public int getFoodPower() {
        return foodpower;
    }

    public int getEffectiveHealingPower(LivingEntity entity) {
        if (entity instanceof Player) {
            return affectsplayers ? healingpower : 0;
        } else if (entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast) {
            return affectsmobs ? -healingpower : 0;
        } else if (entity instanceof Wolf) {
            if (((Wolf) entity).isTamed()) {
                return affectstamedwolves ? healingpower : 0;
            } else if (((Wolf) entity).isAngry()) {
                return affectsangrywolves ? -healingpower : 0;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public double getRange() {
        return range;
    }

    public int getUpdateRate() {
        return updaterate;
    }

    public StructureType getStructureType() {
        return structuretype;
    }

    public List<StructureType> getAllStructureTypes() {
        return rotatedstructuretypes;
    }

    public Rotator getRotator() {
        return rotator;
    }

    public boolean affectsPlayers() {
        return affectsplayers;
    }

    public boolean affectsMobs() {
        return affectsmobs;
    }

    public boolean affectsTamedWolves() {
        return affectstamedwolves;
    }

    public boolean affectsAngryWolves() {
        return affectsangrywolves;
    }

    @Override
    public String toString() {
        return "totemtype { name: " + name + ", healingpower: " + healingpower + ", foodpower: " + foodpower + ", updaterate: " + updaterate + ", range: " + range + ", affects players: " + affectsplayers + ", affects mobs: " + affectsmobs + ", affects tamed wolves: " + affectstamedwolves
                + ", affects angry wolves: " + affectsangrywolves + "}";
    }
}
