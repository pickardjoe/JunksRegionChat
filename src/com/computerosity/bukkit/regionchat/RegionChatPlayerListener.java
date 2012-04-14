package com.computerosity.bukkit.regionchat;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

// =============================================================
// Class       : ChatProtectPlayerListener
//
// Description : Implements player listener
//
// Author      : Junkman
// =============================================================
public class RegionChatPlayerListener implements Listener
{
    private final RegionChatPlugin plugin;
    
    public RegionChatPlayerListener(RegionChatPlugin plugin)
    {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event)
    {
        // Return if not enabled
        if (! plugin.isEnabled() ) return;

        // If something cancelled the chat event, honour it...
        if(event.isCancelled()) return;
		
        // Get the sent message
        String message = event.getMessage();

        // Get player
		Player player = event.getPlayer();
		Location location = player.getLocation();
		
        ArrayList<Player> list = plugin.GetPlayersInEarshot(location);

		// Send to only selected players if required
		if(list!=null)
		{
	        // If the message starts with an asterisk it's a shout
	        if(message.startsWith("*")) 
	        {
	        	// Remove the asterisk
	        	event.setMessage(message.substring(1));
	        	return;
	        }

	        // otherwise select players to send to
			for (Player p : list)
			{
				String outMessage = plugin.messageFormat;
				outMessage = outMessage.replaceAll("\\{DISPLAYNAME\\}", player.getDisplayName());
				outMessage = outMessage.replaceAll("\\{NAME\\}", player.getName());
				outMessage = outMessage.replaceAll("\\{MESSAGE\\}", message);
				p.sendMessage(plugin.messageColour + outMessage + ChatColor.WHITE);
			}
			event.setCancelled(true);
		}
	}
}
