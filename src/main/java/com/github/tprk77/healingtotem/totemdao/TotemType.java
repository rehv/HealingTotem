package com.github.tprk77.healingtotem.totemdao;

import java.util.List;

import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

import com.github.tprk77.healingtotem.util.structure.Rotator;
import com.github.tprk77.healingtotem.util.structure.StructureType;

/**
 * An immutable type representing totem types.
 * 
 * @author tim
 */
public final class TotemType {

    private final String name;
    private final int power;
    private final double range;

    private final StructureType structuretype;
    private final List<StructureType> rotatedstructuretypes;
    private final Rotator rotator;

    private final boolean affectsplayers;
    private final boolean affectsmobs;
    private final boolean affectstamedwolves;
    private final boolean affectsangrywolves;

    public TotemType(String name, int power, double range, StructureType structuretype) {
        this(name, power, range, structuretype, Rotator.NONE, true, true, true, true);
    }

    public TotemType(String name, int power, double range, StructureType structuretype, Rotator rotator) {
        this(name, power, range, structuretype, rotator, true, true, true, true);
    }

    public TotemType(String name, int power, double range, StructureType structuretype, Rotator rotator, boolean affectsplayers, boolean affectsmobs, boolean affectstamedwolves, boolean affectsangrywolves) {
        this.name = name;
        this.power = power;
        this.range = range;

        this.structuretype = structuretype;
        this.rotatedstructuretypes = structuretype.makeRotatedStructureTypes(rotator);
        this.rotator = rotator;

        this.affectsplayers = affectsplayers;
        this.affectsmobs = affectsmobs;
        this.affectstamedwolves = affectstamedwolves;
        this.affectsangrywolves = affectsangrywolves;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public int getEffectivePower(LivingEntity entity) {
        if (entity instanceof Player) {
            return affectsplayers ? power : 0;
        } else if (entity instanceof Monster || entity instanceof Slime || entity instanceof Ghast) {
            return affectsmobs ? -power : 0;
        } else if (entity instanceof Wolf) {
            if (((Wolf) entity).isTamed()) {
                return affectstamedwolves ? power : 0;
            } else if (((Wolf) entity).isAngry()) {
                return affectsangrywolves ? -power : 0;
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
        return "totemtype { name: " + name + ", power: " + power + ", range: " + range + ", affects players: " + affectsplayers + ", affects mobs: " + affectsmobs + ", affects tamed wolves: " + affectstamedwolves + ", affects angry wolves: " + affectsangrywolves + "}";
    }
}
