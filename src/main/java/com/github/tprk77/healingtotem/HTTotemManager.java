package com.github.tprk77.healingtotem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.tprk77.healingtotem.totemdao.Totem;
import com.github.tprk77.healingtotem.totemdao.TotemType;
import com.github.tprk77.healingtotem.util.BlockHashable;
import com.github.tprk77.healingtotem.util.structure.BlockOffset;
import com.github.tprk77.healingtotem.util.structure.Rotator;
import com.github.tprk77.healingtotem.util.structure.StructureType;

/**
 * 
 * @author tim
 */
public class HTTotemManager {

    private final HTPlugin plugin;

    private final String totemTypesFilename = "totemtypes.yml";
    private final String totemsFilename = "totems.yml";

    private final List<TotemType> totemTypes;
    private final List<Totem> totems;

    HashMap<BlockHashable, Set<Totem>> blockHash;
    HashMap<String, Set<Totem>> ownerHash;
    HashMap<String, HTHealerRunnable> totemRunners;

    public HTTotemManager(HTPlugin plugin) {
        this.plugin = plugin;
        totemTypes = new ArrayList<TotemType>();
        totems = new ArrayList<Totem>();
        blockHash = new HashMap<BlockHashable, Set<Totem>>();
        ownerHash = new HashMap<String, Set<Totem>>();
        totemRunners = new HashMap<String, HTHealerRunnable>();
    }

    public List<Totem> getTotems() {
        return new ArrayList<Totem>(totems);
    }

    public List<TotemType> getTotemTypes() {
        return new ArrayList<TotemType>(totemTypes);
    }

    public void cancelAllTasks() {
        final Set<Entry<String, HTHealerRunnable>> totemRunnerSet = totemRunners.entrySet();
        for (final Map.Entry<String, HTHealerRunnable> entry : totemRunnerSet) {
            final HTHealerRunnable healerrunnable = entry.getValue();
            healerrunnable.cancel();
        }
    }

    public void addTotem(Totem totem) {

        // add to block hash
        for (final Block block : totem.getBlocks()) {
            final BlockHashable bh = new BlockHashable(block);
            final Set<Totem> existing = blockHash.get(bh);
            if (existing == null) {
                blockHash.put(bh, new HashSet<Totem>(Arrays.asList(totem)));
            } else {
                existing.add(totem);
            }
        }

        // add to owner hash
        final String owner = totem.getOwner();
        final Set<Totem> existing = ownerHash.get(owner);
        if (existing == null) {
            ownerHash.put(owner, new HashSet<Totem>(Arrays.asList(totem)));
        } else {
            existing.add(totem);
        }

        // instantiate HTHealerRunnable if it's a new totemType
        int typeCount = 0;
        for (final Totem totemAux : totems) {
            if (totem.getTotemType() == totemAux.getTotemType()) {
                typeCount++;
            }
        }
        if (typeCount == 0) {
            final HTHealerRunnable healerRunnable = new HTHealerRunnable(HTPlugin.getInstance(), totem.getTotemType().getName());
            healerRunnable.schedule(totem.getTotemType().getUpdateRate());
            totemRunners.put(totem.getTotemType().getName(), healerRunnable);
        }

        totems.add(totem);
    }

    public void removeTotem(Totem totem) {
        totems.remove(totem);

        // remove from block hash
        for (final Block block : totem.getBlocks()) {
            final BlockHashable bh = new BlockHashable(block);
            final Set<Totem> existing = blockHash.get(bh);
            existing.remove(totem);
            if (existing.isEmpty()) {
                blockHash.remove(bh);
            }
        }

        // remove from owner hash
        final String owner = totem.getOwner();
        final Set<Totem> existing = ownerHash.get(owner);
        existing.remove(totem);
        if (existing.isEmpty()) {
            ownerHash.remove(owner);
        }

        // cancel HTHealerRunnable if there's no more totems of that type
        int typeCount = 0;
        for (final Totem totemAux : totems) {
            if (totem.getTotemType() == totemAux.getTotemType()) {
                typeCount++;
            }
        }
        if (typeCount == 0) {
            final HTHealerRunnable healerRunnable = totemRunners.get(totem.getTotemType().getName());
            healerRunnable.cancel();
            totemRunners.remove(totem.getTotemType().getName());
        }
    }

    public Set<Totem> getTotemsFromBlock(Block block) {
        final BlockHashable bh = new BlockHashable(block);
        final Set<Totem> totemset = blockHash.get(bh);
        if (totemset == null)
            return null;
        return new HashSet<Totem>(totemset);
    }

    public Set<Totem> getTotemsFromPlayer(Player player) {
        final String owner = player.getName();
        final Set<Totem> totemset = ownerHash.get(owner);
        if (totemset == null)
            return null;
        return new HashSet<Totem>(totemset);
    }

    public TotemType getTotemType(String name) {
        for (final TotemType type : totemTypes) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public void loadTotemTypesOrDefault() {

        final File totemtypesfile = new File(plugin.getDataFolder(), totemTypesFilename);
        if (!totemtypesfile.isFile()) {
            try {
                totemtypesfile.getParentFile().mkdirs();
                totemtypesfile.createNewFile();
                saveDefaultTotemTypes();
            } catch (final Exception ex) {
                plugin.getLogger().warning("Could not create file " + totemtypesfile.getName());
                ex.printStackTrace();
            }
        }

        loadTotemTypes();
    }

    private void loadTotemTypes() {
        final File totemTypesFile = new File(plugin.getDataFolder(), totemTypesFilename);
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(totemTypesFile);

        final ConfigurationSection totemTypesSection = config.getConfigurationSection("totemTypes");
        final Set<String> totemTypeList = totemTypesSection.getKeys(false);

        for (final String totemTypeName : totemTypeList) {
            final ConfigurationSection totemSection = totemTypesSection.getConfigurationSection(totemTypeName);
            final TotemType totemType = loadYamlTotemType(totemSection);
            if (totemType != null) {
                totemTypes.add(totemType);
            } else {
                plugin.getLogger().warning("A totem type could not be loaded.");
            }
        }
        plugin.getLogger().info("Loaded " + totemTypes.size() + " totem types.");
    }

    private void saveDefaultTotemTypes() {
        final File totemTypesFile = new File(plugin.getDataFolder(), totemTypesFilename);
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(totemTypesFile);

        TotemType totemType;
        StructureType structureType;
        StructureType.Prototype proto;

        config.createSection("totemTypes");
        final ConfigurationSection totemTypesSection = config.getConfigurationSection("totemTypes");
        ConfigurationSection totemSection;

        config.options().copyDefaults(true);

        proto = new StructureType.Prototype();
        proto.addBlock(0, 0, 0, Material.COBBLESTONE);
        proto.addBlock(0, 1, 0, Material.COBBLESTONE);
        proto.addBlock(0, 2, 0, Material.COBBLESTONE);
        proto.addBlock(0, 3, 0, Material.LAPIS_BLOCK);
        proto.addBlock(0, 4, 0, Material.LAPIS_BLOCK);
        structureType = new StructureType(proto);
        totemType = new TotemType("minor", 1, 15.0, structureType, Rotator.NONE);
        totemTypesSection.createSection(totemType.getName());
        totemSection = totemTypesSection.getConfigurationSection(totemType.getName());
        saveYamlTotemType(totemType, totemSection);

        proto = new StructureType.Prototype();
        proto.addBlock(0, 0, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 1, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 2, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 3, 0, Material.GOLD_BLOCK);
        proto.addBlock(0, 4, 0, Material.GOLD_BLOCK);
        structureType = new StructureType(proto);
        totemType = new TotemType("normal", 1, 30.0, structureType, Rotator.NONE);
        totemTypesSection.createSection(totemType.getName());
        totemSection = totemTypesSection.getConfigurationSection(totemType.getName());
        saveYamlTotemType(totemType, totemSection);

        proto = new StructureType.Prototype();
        proto.addBlock(0, 0, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 1, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 2, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 3, 0, Material.GOLD_BLOCK);
        proto.addBlock(0, 4, 0, Material.DIAMOND_BLOCK);
        structureType = new StructureType(proto);
        totemType = new TotemType("major", 2, 45.0, structureType, Rotator.NONE);
        totemTypesSection.createSection(totemType.getName());
        totemSection = totemTypesSection.getConfigurationSection(totemType.getName());
        saveYamlTotemType(totemType, totemSection);

        proto = new StructureType.Prototype();
        proto.addBlock(0, 0, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 1, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 2, 0, Material.IRON_BLOCK);
        proto.addBlock(0, 3, 0, Material.DIAMOND_BLOCK);
        proto.addBlock(0, 4, 0, Material.DIAMOND_BLOCK);
        structureType = new StructureType(proto);
        totemType = new TotemType("super", 2, 75.0, structureType, Rotator.NONE);
        totemTypesSection.createSection(totemType.getName());
        totemSection = totemTypesSection.getConfigurationSection(totemType.getName());
        saveYamlTotemType(totemType, totemSection);

        proto = new StructureType.Prototype();
        proto.addBlock(0, 0, 0, Material.COBBLESTONE);
        proto.addBlock(0, 1, 0, Material.COBBLESTONE);
        proto.addBlock(0, 2, 0, Material.COBBLESTONE);
        proto.addBlock(0, 3, 0, Material.JACK_O_LANTERN);
        proto.addBlock(0, 4, 0, Material.JACK_O_LANTERN);
        structureType = new StructureType(proto);
        totemType = new TotemType("evilminor", -1, 15.0, structureType, Rotator.NONE);
        totemTypesSection.createSection(totemType.getName());
        totemSection = totemTypesSection.getConfigurationSection(totemType.getName());
        saveYamlTotemType(totemType, totemSection);

        proto = new StructureType.Prototype();
        proto.addBlock(0, 0, 0, Material.NETHERRACK);
        proto.addBlock(0, 1, 0, Material.NETHERRACK);
        proto.addBlock(0, 2, 0, Material.NETHERRACK);
        proto.addBlock(0, 3, 0, Material.GLOWSTONE);
        proto.addBlock(0, 4, 0, Material.GLOWSTONE);
        structureType = new StructureType(proto);
        totemType = new TotemType("evilnormal", -1, 30.0, structureType, Rotator.NONE);
        totemTypesSection.createSection(totemType.getName());
        totemSection = totemTypesSection.getConfigurationSection(totemType.getName());
        saveYamlTotemType(totemType, totemSection);

        try {
            config.save(totemTypesFile);
        } catch (final IOException e) {
            plugin.getLogger().warning("Could not save file " + totemTypesFile.getName());
            e.printStackTrace();
        }
    }

    protected void saveTotems() {
        final File totemsFile = new File(plugin.getDataFolder(), totemsFilename);
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(totemsFile);

        int i = 0;
        config.createSection("totems");
        final ConfigurationSection totemsSection = config.getConfigurationSection("totems");

        for (final Totem totem : totems) {
            totemsSection.createSection("totem" + Integer.toString(i));
            final ConfigurationSection totemSection = totemsSection.getConfigurationSection("totem" + Integer.toString(i));
            saveYamlTotem(totem, totemSection);
            i++;
        }

        if (!plugin.getConfigManager().isQuiet()) {
            plugin.getLogger().info("Saved " + totems.size() + " totems");
        }

        try {
            config.save(totemsFile);
        } catch (final IOException e) {
            plugin.getLogger().warning("Could not save file " + totemsFile.getName());
            e.printStackTrace();
        }
    }

    public void loadTotems() {
        final File totemsFile = new File(plugin.getDataFolder(), totemsFilename);
        final YamlConfiguration config = new YamlConfiguration();
        if (!totemsFile.exists()) {
            try {
                totemsFile.createNewFile();
                config.createSection("totems");
                config.save(totemsFile);
            } catch (final Exception e) {
                plugin.getLogger().warning("Could not create file " + totemsFile.getName());
                e.printStackTrace();
            }
        }
        try {
            config.load(totemsFile);
        } catch (final Exception e) {
            plugin.getLogger().warning("Could not load file " + totemsFile.getName());
            e.printStackTrace();
        }

        final ConfigurationSection totemsSection = config.getConfigurationSection("totems");
        final Set<String> totemList = totemsSection.getKeys(false);

        for (final String totemName : totemList) {
            final ConfigurationSection totemSection = totemsSection.getConfigurationSection(totemName);
            final Totem totem = loadYamlTotem(totemSection);
            if (totem != null) {
                addTotem(totem);
            } else {
                plugin.getLogger().warning("A totem could not be loaded.");
            }
        }

        plugin.getLogger().info("Loaded " + totems.size() + " totems");
    }

    private void saveYamlTotem(Totem totem, ConfigurationSection totemSection) {
        totemSection.set("world", totem.getRootBlock().getWorld().getName());
        totemSection.set("x", totem.getRootBlock().getX());
        totemSection.set("y", totem.getRootBlock().getY());
        totemSection.set("z", totem.getRootBlock().getZ());
        totemSection.set("type", totem.getTotemType().getName());

        final String owner = totem.getOwner();
        if (totem.getOwner() != null) {
            totemSection.set("owner", owner);
        }
    }

    private Totem loadYamlTotem(ConfigurationSection totemSection) {
        final String worldName = totemSection.getString("world");
        if (worldName == null) {
            plugin.getLogger().warning(totemSection.getName() + "'s world is not set.");
            return null;
        }

        final int x = totemSection.getInt("x", Integer.MIN_VALUE);
        final int y = totemSection.getInt("y", Integer.MIN_VALUE);
        final int z = totemSection.getInt("z", Integer.MIN_VALUE);
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE) {
            plugin.getLogger().warning(totemSection.getName() + "'s x, y or z is not set.");
            return null;
        }

        final String totemTypeName = totemSection.getString("type");
        if (totemTypeName == null) {
            plugin.getLogger().warning(totemSection.getName() + "'s type is not set.");
            return null;
        }

        final String owner = totemSection.getString("owner");

        final World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning(totemSection.getName() + "'s world is invalid.");
            return null;
        }

        final TotemType totemType = getTotemType(totemTypeName);
        if (totemType == null) {
            plugin.getLogger().warning(totemSection.getName() + "'s type is invalid.");
            return null;
        }

        final Block block = world.getBlockAt(x, y, z);
        final Totem totem = new Totem(totemType, block, owner);

        if (!totem.verifyStructure()) {
            plugin.getLogger().warning(totemSection.getName() + "'s structure is invalid.");
            return null;
        }

        return totem;
    }

    private void saveYamlTotemType(TotemType totemType, ConfigurationSection totemSection) {
        totemSection.addDefault("power", totemType.getPower());
        totemSection.addDefault("range", totemType.getRange());
        totemSection.addDefault("updaterate", totemType.getUpdateRate());
        totemSection.addDefault("rotator", totemType.getRotator().toString());
        totemSection.createSection("structure");
        final ConfigurationSection structureSection = totemSection.getConfigurationSection("structure");
        saveYamlStructure(totemType.getStructureType(), structureSection);
        totemSection.addDefault("affectsPlayers", totemType.affectsPlayers());
        totemSection.addDefault("affectsMobs", totemType.affectsMobs());
        totemSection.addDefault("affectsTamedWolves", totemType.affectsTamedWolves());
        totemSection.addDefault("affectsAngryWolves", totemType.affectsAngryWolves());
    }

    private void saveYamlStructure(StructureType structureType, ConfigurationSection structureSection) {
        int i = 0;
        final Set<BlockOffset> strutSet = structureType.getPattern().keySet();
        final BlockOffset[] sort = strutSet.toArray(new BlockOffset[strutSet.size()]);

        boolean changed = true;

        while (changed) {
            changed = false;
            for (int j = 0; j < (sort.length) - 1; j++) {
                if (sort[j].getY() > sort[j + 1].getY()) {
                    final BlockOffset aux = sort[j + 1];
                    sort[j + 1] = sort[j];
                    sort[j] = aux;
                    changed = true;
                }
            }
        }

        for (int j = 0; j < (sort.length); j++) {
            for (final BlockOffset offset2 : structureType.getPattern().keySet()) {
                if (offset2.equals(sort[i])) {
                    i++;
                    structureSection.createSection("block" + (i));
                    final ConfigurationSection blockSection = structureSection.getConfigurationSection("block" + (i));
                    final Material material = structureType.getPattern().get(offset2);
                    blockSection.addDefault("x", offset2.getX());
                    blockSection.addDefault("y", offset2.getY());
                    blockSection.addDefault("z", offset2.getZ());
                    blockSection.addDefault("material", material.toString());
                    break;
                }
            }
        }
    }

    private TotemType loadYamlTotemType(ConfigurationSection totemSection) {
        final String name = totemSection.getName();

        final int power = totemSection.getInt("power", Integer.MIN_VALUE);
        if (power == Integer.MIN_VALUE) {
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s power is not set.");
            return null;
        }

        final double range = totemSection.getDouble("range");
        if (Double.isNaN(range)) {
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s range is not set.");
            return null;
        }

        int updaterate = totemSection.getInt("updaterate", Integer.MIN_VALUE);
        if (updaterate == Integer.MIN_VALUE) {
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s updaterate is not set, using default.");
            updaterate = plugin.getConfigManager().getDefaultUpdateRate();
        } else if (updaterate == 0) {
            updaterate = plugin.getConfigManager().getDefaultUpdateRate();
        } else if (updaterate < 20) {
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s updaterate is less than 20, using default.");
            updaterate = plugin.getConfigManager().getDefaultUpdateRate();
        }

        String rotatorString = totemSection.getString("rotator");
        if (rotatorString == null) {
            rotatorString = "NULL";
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s rotator is not set.");
        }

        Rotator rotator = Rotator.matchRotator(rotatorString);
        if (rotator == null) {
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s power is invalid, using default.");
            rotator = Rotator.getDefault();
        }

        final ConfigurationSection structureSection = totemSection.getConfigurationSection("structure");
        final StructureType structureType = loadYamlStructure(structureSection);
        if (structureType == null) {
            plugin.getLogger().warning("TotemType " + totemSection.getName() + "'s structure is invalid.");
            return null;
        }

        if (structureType.getBlockCount() < 3) {
            plugin.getLogger().warning("For technical reasons, TotemType " + totemSection.getName() + "'s structure block count must be at least 3.");
            return null;
        }

        final boolean affectsPlayers = totemSection.getBoolean("affectsPlayers", true);
        final boolean affectsMobs = totemSection.getBoolean("affectsMobs", true);
        final boolean affectsTamedWolves = totemSection.getBoolean("affectsTamedWolves", true);
        final boolean affectsAngryWolves = totemSection.getBoolean("affectsAngryWolves", true);

        return new TotemType(name, power, range, updaterate, structureType, rotator, affectsPlayers, affectsMobs, affectsTamedWolves, affectsAngryWolves);
    }

    private StructureType loadYamlStructure(ConfigurationSection structureSection) {
        final StructureType.Prototype proto = new StructureType.Prototype();
        final String totemTypeName = structureSection.getParent().getName();
        for (final String key : structureSection.getKeys(false)) {
            final ConfigurationSection blockSection = structureSection.getConfigurationSection(key);

            final int x = blockSection.getInt("x", Integer.MIN_VALUE);
            final int y = blockSection.getInt("y", Integer.MIN_VALUE);
            final int z = blockSection.getInt("z", Integer.MIN_VALUE);

            if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE) {
                plugin.getLogger().warning("TotemType " + totemTypeName + " " + blockSection.getName() + "'s x, y or z is not set.");
                return null;
            }

            final String materialName = blockSection.getString("material");
            if (materialName == null) {
                plugin.getLogger().warning("TotemType " + totemTypeName + " " + blockSection.getName() + "'s material is not set.");
                return null;
            }

            final Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("TotemType " + totemTypeName + " " + blockSection.getName() + "'s material is invalid.");
                return null;
            }

            proto.addBlock(x, y, z, material);
        }
        return new StructureType(proto);
    }

}