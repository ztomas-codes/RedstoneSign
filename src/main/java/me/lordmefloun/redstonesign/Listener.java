package me.lordmefloun.redstonesign;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

public class Listener implements org.bukkit.event.Listener {

    private RedstoneSign plugin;

    public Listener(RedstoneSign plugin){
        this.plugin = plugin;
    }


    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        Player p = e.getPlayer();
        if (e.getLine(0).equalsIgnoreCase("[Redstone]")){
            if (!e.getLine(1).equalsIgnoreCase("")){
                e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&0&l[Redstone]"));
                e.setLine(2, ChatColor.translateAlternateColorCodes('&', e.getLine(1)));
                e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&2&lAKTIVACE"));
                Utils.sendMessage(p, "&aPolož někam blok abys uložil pozici, která se bude aktivovat po zakoupení");
                SignObject.creating.put(p, e.getBlock());
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        SignObject so = SignObject.getSignObjectFromSignBlock(e.getClickedBlock().getLocation());
        if (so == null){
            return;
        }
        if (!so.owner.getPlayer().equals(e.getPlayer())) {
            so.buy(e.getPlayer());
        }
        else{
            Utils.sendMessage(e.getPlayer(), "&cTento obchod vlastníš!");
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        Player p = e.getPlayer();
        if (SignObject.creating.containsKey(p)){
            try {
                Sign sign = (Sign) SignObject.creating.get(p).getState();
                double cost = Double.parseDouble(ChatColor.stripColor(sign.getLine(2)));
                SignObject.saveToConfig(plugin, p.getPlayer().getUniqueId().toString(), cost, e.getBlock().getLocation(), SignObject.creating.get(p).getLocation());
                ConfigurationSection configSection = plugin.getConfig().getConfigurationSection("Signs");
                Configuration config = plugin.getConfig();
                int configIndexing = 0;
                if (configSection != null) {
                    for (String key : configSection.getKeys(false)) {
                        configIndexing = Integer.parseInt(key);
                    }
                }
                configIndexing++;
                SignObject.signs.add(new SignObject(SignObject.creating.get(p), e.getBlock(), p.getPlayer().getUniqueId().toString(), cost, configIndexing));
                Utils.sendMessage(p, "&aTvůj redstone obchod byl úspešně vytvořen");
                SignObject.creating.remove(p);
            }
            catch (Exception a){
                Utils.sendMessage(p, "&cNěkde se stala chyba");
                SignObject.creating.remove(p);
            }
        }
    }

    @EventHandler
    public void onDestroySign(BlockBreakEvent e){
        Player p = e.getPlayer();
        SignObject so = SignObject.getSignObjectFromSignBlock(e.getBlock().getLocation());
        if (so == null){
            return;
        }
        if (!so.owner.getPlayer().equals(p)) {
            e.setCancelled(true);
        }
        else{
            so.remove(plugin);
            Utils.sendMessage(p, "&cObchod byl odstraněn");
        }
    }

    @EventHandler
    public void onDestroyTarget(BlockBreakEvent e){
        SignObject so = SignObject.getSignObjectFromTargetBlock(e.getBlock().getLocation());

        if (so == null){
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        for (SignObject obj: SignObject.getSignObjectsFromUUID(e.getPlayer().getUniqueId())) {
            obj.owner = (OfflinePlayer) e.getPlayer();
        }
    }

}