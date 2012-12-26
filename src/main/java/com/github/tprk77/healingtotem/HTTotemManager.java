package com.github.tprk77.healingtotem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    private List<TotemType> totemTypes;
    private List<Totem> totems;

    HashMap<BlockHashable, Set<Totem>> blockHash;
    HashMap<String, Set<Totem>> ownerHash;

    public HTTotemManager(HTPlugin plugin){
        this.plugin = plugin;
        totemTypes = new ArrayList<TotemType>();
        totems = new ArrayList<Totem>();
        blockHash = new HashMap<BlockHashable, Set<Totem>>();
        ownerHash = new HashMap<String, Set<Totem>>();
    }

    public List<Totem> getTotems(){
        return new ArrayList<Totem>(totems);
    }

    public List<TotemType> getTotemTypes(){
        return new ArrayList<TotemType>(totemTypes);
    }

    public void addTotem(Totem totem){
        totems.add(totem);

        // add to block hash
        for(Block block : totem.getBlocks()){
            BlockHashable bh = new BlockHashable(block);
            Set<Totem> existing = blockHash.get(bh);
            if(existing == null){
                blockHash.put(bh, new HashSet<Totem>(Arrays.asList(totem)));
            }else{
                existing.add(totem);
            }
        }

        // add to owner hash
        String owner = totem.getOwner();
        Set<Totem> existing = ownerHash.get(owner);
        if(existing == null){
            ownerHash.put(owner, new HashSet<Totem>(Arrays.asList(totem)));
        }else{
            existing.add(totem);
        }
    }

    public void removeTotem(Totem totem){
        totems.remove(totem);

        // remove from block hash
        for(Block block : totem.getBlocks()){
            BlockHashable bh = new BlockHashable(block);
            Set<Totem> existing = blockHash.get(bh);
            existing.remove(totem);
            if(existing.isEmpty()){
                blockHash.remove(bh);
            }
        }

        // remove from owner hash
        String owner = totem.getOwner();
        Set<Totem> existing = ownerHash.get(owner);
        existing.remove(totem);
        if(existing.isEmpty()){
            ownerHash.remove(owner);
        }
    }

    public Set<Totem> getTotemsFromBlock(Block block){
        BlockHashable bh = new BlockHashable(block);
        Set<Totem> totemset = blockHash.get(bh);
        if(totemset == null) return null;
        return new HashSet<Totem>(totemset);
    }

    public Set<Totem> getTotemsFromPlayer(Player player){
        String owner = player.getName();
        Set<Totem> totemset = ownerHash.get(owner);
        if(totemset == null) return null;
        return new HashSet<Totem>(totemset);
    }

    public TotemType getTotemType(String name){
        for(TotemType type : totemTypes){
            if(type.getName().equals(name)){
                return type;
            }
        }
        return null;
    }

    public void loadTotemTypesOrDefault(){
        
        File totemtypesfile = new File(plugin.getDataFolder(), totemTypesFilename);
        if(!totemtypesfile.isFile()){
            try{
                totemtypesfile.getParentFile().mkdirs();
                totemtypesfile.createNewFile();
                saveDefaultTotemTypes();
            }catch(Exception ex){
                plugin.getLogger().warning("Could not create file " + totemtypesfile.getName());
            }
        }

        loadTotemTypes();
    }
    
    private void loadTotemTypes() {
        File totemTypesFile = new File (plugin.getDataFolder(), totemTypesFilename);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(totemTypesFile);
        
        ConfigurationSection totemTypesSection = config.getConfigurationSection("totemTypes");
        Set<String> totemTypeList = totemTypesSection.getKeys(false);
        
        for (String totemTypeName : totemTypeList) {
            ConfigurationSection totemSection = totemTypesSection.getConfigurationSection(totemTypeName);
            TotemType totemType = loadYamlTotemType(totemSection);
            if(totemType != null){
                totemTypes.add(totemType);
            }
            else {
                plugin.getLogger().warning("A totem type could not be loaded.");
            }
        }
        plugin.getLogger().info("Loaded "+totemTypes.size()+" totem types.");
    }

    private void saveDefaultTotemTypes() {
        File totemTypesFile = new File (plugin.getDataFolder(), totemTypesFilename);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(totemTypesFile);
        
        TotemType totemType;
        StructureType structureType;
        StructureType.Prototype proto;
        
        config.createSection("totemTypes");
        ConfigurationSection totemTypesSection = config.getConfigurationSection("totemTypes");
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
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save file " + totemTypesFile.getName());
            e.printStackTrace();
        }
    }
    
    protected void saveTotems() {
        File totemsFile = new File(plugin.getDataFolder(), totemsFilename);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(totemsFile);

        int i=0;
        config.createSection("totems");
        ConfigurationSection totemsSection = config.getConfigurationSection("totems"); 
        
        for(Totem totem : totems){
            totemsSection.createSection("totem"+Integer.toString(i));
            ConfigurationSection totemSection = totemsSection.getConfigurationSection("totem"+Integer.toString(i));
            saveYamlTotem(totem, totemSection);
            i++;
        }

        if (!plugin.getConfigManager().isQuiet()) {
            plugin.getLogger().info("Saved " + totems.size() + " totems");
        }

        try {
            config.save(totemsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save file " + totemsFile.getName());
            e.printStackTrace();
        }
    }
    
    public void loadTotems() {
        File totemsFile = new File(plugin.getDataFolder(), totemsFilename);
        YamlConfiguration config = new YamlConfiguration();
        if (!totemsFile.exists()) {
            try {
                totemsFile.createNewFile();
                config.createSection("totems");
                config.save(totemsFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not create file " + totemsFile.getName());
                e.printStackTrace();
            }
        }
        try {
            config.load(totemsFile);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load file " + totemsFile.getName());
            e.printStackTrace();
        }
               
        ConfigurationSection totemsSection = config.getConfigurationSection("totems");
        Set<String> totemList = totemsSection.getKeys(false);

        for(String totemName : totemList){
            ConfigurationSection totemSection = totemsSection.getConfigurationSection(totemName);
            Totem totem = loadYamlTotem(totemSection);
            if (totem != null) {
                addTotem(totem);
            }
            else {
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

        String owner = totem.getOwner();
        if(totem.getOwner() != null){
            totemSection.set("owner", owner);
        }
    }
    
    private Totem loadYamlTotem(ConfigurationSection totemSection) {
        String worldName = totemSection.getString("world");
        if (worldName == null) {
            plugin.getLogger().warning(totemSection.getName()+"'s world is not set.");
            return null;
        }
        
        int x = totemSection.getInt("x", Integer.MIN_VALUE);
        int y = totemSection.getInt("y", Integer.MIN_VALUE);
        int z = totemSection.getInt("z", Integer.MIN_VALUE);
        if(x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE){
            plugin.getLogger().warning(totemSection.getName()+"'s x, y or z is not set.");
            return null;
        }
        
        String totemTypeName = totemSection.getString("type");
        if(totemTypeName == null){
            plugin.getLogger().warning(totemSection.getName()+"'s type is not set.");
            return null;
        }

        String owner = totemSection.getString("owner");

        World world = plugin.getServer().getWorld(worldName);
        if(world == null){
            plugin.getLogger().warning(totemSection.getName()+"'s world is invalid.");
            return null;
        }

        TotemType totemType = getTotemType(totemTypeName);
        if(totemType == null){
            plugin.getLogger().warning(totemSection.getName()+"'s type is invalid.");
            return null;
        }

        Block block = world.getBlockAt(x, y, z);
        Totem totem = new Totem(totemType, block, owner);

        if(!totem.verifyStructure()){
            plugin.getLogger().warning(totemSection.getName()+"'s structure is invalid.");
            return null;
        }

        return totem;
    }
    
    private void saveYamlTotemType(TotemType totemType, ConfigurationSection totemSection) {
        totemSection.addDefault("power", totemType.getPower());
        totemSection.addDefault("range", totemType.getRange());
        totemSection.addDefault("rotator", totemType.getRotator().toString());
        totemSection.createSection("structure");
        ConfigurationSection structureSection = totemSection.getConfigurationSection("structure");
        saveYamlStructure(totemType.getStructureType(), structureSection);
        totemSection.addDefault("affectsPlayers", totemType.affectsPlayers());
        totemSection.addDefault("affectsMobs", totemType.affectsMobs());
        totemSection.addDefault("affectsTamedWolves", totemType.affectsTamedWolves());
        totemSection.addDefault("affectsAngryWolves", totemType.affectsAngryWolves());
    }
    
    private void saveYamlStructure(StructureType structureType, ConfigurationSection structureSection) {
        int i=0;
        Set<BlockOffset> strutSet = structureType.getPattern().keySet();
        BlockOffset[] sort = strutSet.toArray(new BlockOffset[strutSet.size()]); 
        
        boolean changed = true;
        
        while (changed) {
            changed = false;
            for (int j = 0; j < (sort.length)-1; j++){
                if (sort[j].getY() > sort[j+1].getY()){
                        BlockOffset aux = sort[j+1];
                        sort[j+1] = sort[j];
                        sort[j] = aux;
                        changed = true;
                }
            }
        }
        
        for (int j = 0; j < (sort.length); j++) {
            for (BlockOffset offset2 : structureType.getPattern().keySet()) {
                if (offset2.equals(sort[i])) {
                    i++;
                    structureSection.createSection("block"+(i));
                    ConfigurationSection blockSection = structureSection.getConfigurationSection("block"+(i));
                    Material material = structureType.getPattern().get(offset2);
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
        String name = totemSection.getName();
        
        int power = totemSection.getInt("power", Integer.MIN_VALUE);
        if (power == Integer.MIN_VALUE) {
            plugin.getLogger().warning(totemSection.getName()+"'s power is not set.");
            return null;
        }
        
        double range = totemSection.getDouble("range");
        if (Double.isNaN(range)) {
            plugin.getLogger().warning(totemSection.getName()+"'s range is not set.");
            return null;    
        }
        
        String rotatorString = totemSection.getString("rotator");
        if (rotatorString == null) {
            rotatorString = "NULL";
            plugin.getLogger().warning(totemSection.getName()+"'s rotator is not set.");
        }
        
        Rotator rotator = Rotator.matchRotator(rotatorString);
        if (rotator == null) {
            plugin.getLogger().warning(totemSection.getName()+"'s power is invalid, using default.");
            rotator = Rotator.getDefault();
        }
        
        ConfigurationSection structureSection = totemSection.getConfigurationSection("structure");
        StructureType structureType = loadYamlStructure(structureSection);
        if (structureType == null) {
            plugin.getLogger().warning(totemSection.getName()+"'s structure is invalid.");
            return null;
        }
        
        if (structureType.getBlockCount() < 3) {
            plugin.getLogger().warning("For technical reasons, "+totemSection.getName()+"'s structure block count must be at least 3.");
            return null;
        }
        
        boolean affectsPlayers = totemSection.getBoolean("affectsPlayers", true);
        boolean affectsMobs = totemSection.getBoolean("affectsMobs", true);
        boolean affectsTamedWolves = totemSection.getBoolean("affectsTamedWolves", true);
        boolean affectsAngryWolves = totemSection.getBoolean("affectsAngryWolves", true);
        
        return new TotemType(name, power, range, structureType, rotator, affectsPlayers, affectsMobs, affectsTamedWolves, affectsAngryWolves);
    }
    
    private StructureType loadYamlStructure(ConfigurationSection structureSection) {
        StructureType.Prototype proto = new StructureType.Prototype();
        for (String key :  structureSection.getKeys(false)) {
            ConfigurationSection blockSection = structureSection.getConfigurationSection(key);
            
            int x = blockSection.getInt("x", Integer.MIN_VALUE);
            int y = blockSection.getInt("y", Integer.MIN_VALUE);
            int z = blockSection.getInt("z", Integer.MIN_VALUE);
            if(x == Integer.MIN_VALUE || y == Integer.MIN_VALUE || z == Integer.MIN_VALUE){
                plugin.getLogger().warning(blockSection.getName()+"'s x, y or z is not set.");
                return null;
            }
            
            String materialName = blockSection.getString("material");
            if (materialName == null) {
                plugin.getLogger().warning(blockSection.getName()+"'s material is not set.");
                return null;
            }
            
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning(blockSection.getName()+"'s material is invalid.");
                return null;
            }
            
            proto.addBlock(x, y, z, material);
        }
        return new StructureType(proto);
    }

}