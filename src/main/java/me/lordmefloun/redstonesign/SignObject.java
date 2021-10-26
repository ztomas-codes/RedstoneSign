package me.lordmefloun.redstonesign;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class SignObject {

    public Block signblock;
    public Block target;
    public OfflinePlayer owner;
    public double cost;
    public int indexingInConfig;
    public long lastUsed;

    public static HashSet<SignObject> signs = new HashSet<>();
    public static HashMap<Player, Block> creating = new HashMap<>();
    public Material decoy;

    public SignObject(Block signblock, Block target, String owner, double cost, int indexingInConfig){
        this.signblock = signblock;
        this.target = target;
        UUID uuid = UUID.fromString(owner);
        this.indexingInConfig = indexingInConfig;
        this.owner = Bukkit.getOfflinePlayer(uuid);
        this.cost = cost;
        decoy = this.target.getType();
    }

    public void buy(Player p){
        Economy econ = RedstoneSign.getEconomy();
        double playerBalance =  econ.getBalance(p);

        if (playerBalance >= cost){
            if ((System.currentTimeMillis() / 1000) >= 10 + lastUsed) {
                Utils.sendMessage(p, "&b[&cRedstone&b] &aÚspešně sis koupil redstone akci za &b" + cost + "&a od &b" + owner.getName());
                econ.withdrawPlayer(p, cost);
                econ.depositPlayer(owner, cost);
                lastUsed = System.currentTimeMillis() / 1000;
                trigger();
            } else Utils.sendMessage(p, "&b[&cRedstone&b] &cPočkej ještě chvíli!");
        }else Utils.sendMessage(p, "&b[&cRedstone&b] &cNemáš dostatek peněz");

    }

    public static SignObject getSignObjectFromSignBlock(Location loc){
        for (SignObject obj : signs){
            if (obj.signblock.getLocation().equals(loc)){
                return obj;
            }
        }
        return null;
    }

    public static SignObject getSignObjectFromTargetBlock(Location loc){
        for (SignObject obj : signs){
            if (obj.target.getLocation().equals(loc)){
                return obj;
            }
        }
        return null;
    }

    public void trigger(){
        target.setType(Material.REDSTONE_BLOCK);
        new BukkitRunnable(){
            @Override
            public void run(){
                target.setType(decoy);
            }
        }.runTaskLater(RedstoneSign.getPlugin(RedstoneSign.class), 10L);
    }

    public void remove(RedstoneSign pl){
        Configuration config = pl.getConfig();
        config.set("Signs." +indexingInConfig, null);
        RedstoneSign.getPlugin(RedstoneSign.class).saveConfig();
        signs.remove(this);
        this.signblock = null;
    }

    public static void saveToConfig(RedstoneSign pl, String owner, double cost, Location target, Location sign){
        ConfigurationSection configSection = pl.getConfig().getConfigurationSection("Signs");
        Configuration config = pl.getConfig();
        int configIndexing = 0;
        if (configSection != null) {for (String key : configSection.getKeys(false)) {configIndexing = Integer.parseInt(key);}}
        configIndexing++;

        System.out.println(configIndexing);

        config.set("Signs." + configIndexing + ".owner", owner);
        config.set("Signs." + configIndexing + ".cost", cost);
        config.set("Signs." + configIndexing + ".world", target.getWorld().getName());

        config.set("Signs." + configIndexing + ".targetblock.x", target.getBlockX());
        config.set("Signs." + configIndexing + ".targetblock.y", target.getBlockY());
        config.set("Signs." + configIndexing + ".targetblock.z", target.getBlockZ());

        config.set("Signs." + configIndexing + ".signblock.x", sign.getBlockX());
        config.set("Signs." + configIndexing + ".signblock.y", sign.getBlockY());
        config.set("Signs." + configIndexing + ".signblock.z", sign.getBlockZ());

        RedstoneSign.getPlugin(RedstoneSign.class).saveConfig();
    }

    public static void loadSignsFromConfig(RedstoneSign pl){
        ConfigurationSection configSection = pl.getConfig().getConfigurationSection("Signs");
        Configuration config = pl.getConfig();
        if (configSection != null) {
            for (String key : configSection.getKeys(false)) {
                Block signblock = new Location(Bukkit.getWorld(config.getString("Signs." + key + ".world")),
                        config.getDouble("Signs." + key + ".signblock.x"),
                        config.getDouble("Signs." + key + ".signblock.y"),
                        config.getDouble("Signs." + key + ".signblock.z")
                ).getBlock();
                Block target = new Location(Bukkit.getWorld(config.getString("Signs." + key + ".world")),
                        config.getDouble("Signs." + key + ".targetblock.x"),
                        config.getDouble("Signs." + key + ".targetblock.y"),
                        config.getDouble("Signs." + key + ".targetblock.z")
                ).getBlock();

                String owner = config.getString("Signs." + key + ".owner");
                double cost = config.getDouble("Signs." + key + ".cost");

                signs.add(new SignObject(signblock, target, owner, cost, Integer.parseInt(key)));
            }
        }
    }

}