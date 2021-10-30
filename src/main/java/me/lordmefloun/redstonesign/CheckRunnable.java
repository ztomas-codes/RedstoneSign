package me.lordmefloun.redstonesign;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckRunnable extends BukkitRunnable {
    @Override
    public void run() {
        for (SignObject so : SignObject.signs) {
            if (!(so.signblock.getType().equals(Material.SIGN) || so.signblock.getType().equals(Material.WALL_SIGN) )){
                if (so.owner.getPlayer() != null) Utils.sendMessage(so.owner.getPlayer(), "&cTvůj obchod byl zrušen, protože někdo zrušil cedulku, doporučujeme používat residence");
                so.remove(RedstoneSign.getPlugin(RedstoneSign.class));
            }
        }
    }
}
