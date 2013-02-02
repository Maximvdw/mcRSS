package VdW.Maxim.mcRSS;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class listener implements Listener{
	public listener(mcRSS mcRSS) {
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		// Check if it is Maximvdw
		if(e.getPlayer().getName().equalsIgnoreCase("Maximvdw")){
			// Show message

			Bukkit.broadcastMessage("[mcRSS] " + ChatColor.AQUA +  e.getPlayer().getName() + ChatColor.BLUE  + " is the mcRSS plugin creator");
		}
	}
}
