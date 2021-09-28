package me.lordmefloun.redstonesign;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
    private Utils(){
        throw new RuntimeException("Cannot create instance of utils class");
    }
    public static void sendMessage(Player p, String message){
        p.sendMessage( ChatColor.translateAlternateColorCodes('&', message));
    }
}
