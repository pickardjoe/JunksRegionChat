package com.computerosity.bukkit.regionchat;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// =============================================================
// Class       : RegistrationCommand
//
// Description : Process /jrg commands
//
// Author      : Junkman
// =============================================================
public class RegionChatCommand implements CommandExecutor
{
    private final RegionChatPlugin plugin;

    public RegionChatCommand(RegionChatPlugin plugin)
    {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
    {
    	boolean handled = false;
    	
    	Player player = null;
        if ((sender instanceof Player)) player = (Player) sender;

        if(player==null) return false;
        
        if (split.length == 0)
        {
             CommandHelp(player);
             handled = true;
        }
        else
        {
            String token = split[0];

            // Splurge the remaining bits together
            String arg = "";
            if (split.length > 1)
            {
                for (int i = 1; i < split.length; i++)
                    arg = arg + split[i] + " ";
                arg = arg.trim();
            }

            if (token.equalsIgnoreCase("canhear"))
            {
            	CommandCanHear(player);
            	handled = true;
            }
            else if (token.equalsIgnoreCase("private"))
            {
            	CommandPrivate(player,arg);
            	handled = true;
            }
            else if (token.equalsIgnoreCase("public"))
            {
            	CommandPublic(player,arg);
            	handled = true;
            }
        }
        
    	return handled;
    }
    
    private boolean CommandPrivate(Player player,String region)
	{
    	boolean ok = false;
    	
    	// TODO: Attempt to find region if not supplied
    	if(region.length()==0)
    	{
    		sendPlayerMessage(player,"Syntax: /rc private <region>",ChatColor.RED);
    		return false;
    	}

    	// Get region info
    	String isOwner = plugin.IsRegionOwner(player,region);
    	if(isOwner==null)
    	{
    		sendPlayerMessage(player,"RegionChat: WorldGuard region not found",ChatColor.RED);
    		return false;
    	}
    	
    	// Can set own region and region is one of theirs?
    	if (player.hasPermission("jrc.own.private") && !isOwner.isEmpty()) ok = true;
    	
    	// Can set any region?
    	if (player.hasPermission("jrc.private")) ok = true;
    	
    	// If we're permitted - go for it
    	if(ok)
    	{
	    	if(plugin.SetRegion(region, player.getName()))		
			{
	    		sendPlayerMessage(player,"RegionChat: set as PRIVATE",ChatColor.GREEN);
			}
	    	else
	    	{
	    		sendPlayerMessage(player,"RegionChat: already PRIVATE",ChatColor.RED);
	    	}
    	}
    	else
    	{
    		sendPlayerMessage(player,"RegionChat: No permission to set this region private",ChatColor.RED);
    	}
    	
		return ok;
	}

	private boolean CommandPublic(Player player,String region)
	{
    	boolean ok = false;
    	
    	// TODO: Attempt to find region if not supplied
    	if(region.length()==0)
    	{
    		sendPlayerMessage(player,"Syntax: /rc public <region>",ChatColor.RED);
    		return false;
    	}

    	// Get region info
    	String isOwner = plugin.IsRegionOwner(player,region);
    	if(isOwner==null)
    	{
    		sendPlayerMessage(player,"RegionChat: WorldGuard Region not found",ChatColor.RED);
    		return false;
    	}
    	
    	// Can set own region and region is one of theirs?
    	if (player.hasPermission("jrc.own.public") && !isOwner.isEmpty()) ok = true;
    	
    	// Can set any region?
    	if (player.hasPermission("jrc.public")) ok = true;
    	
    	// If we're permitted - go for it
    	if(ok)
    	{
			if(plugin.UnsetRegion(region,player.getName()))
			{
				sendPlayerMessage(player,"RegionChat: set as PUBLIC",ChatColor.GREEN);
			}
	    	else
	    	{
	    		sendPlayerMessage(player,"RegionChat: already PUBLIC",ChatColor.RED);
	    	}
    	}
    	else
    	{
    		sendPlayerMessage(player,"RegionChat: No permission to set this region public",ChatColor.RED);
    	}
    	
		return ok;
	}

	public boolean CommandHelp(Player player)
    {
    	player.sendMessage("Commands:\n--------\n");
    	player.sendMessage("/rc canhear          Shows players who can hear you");
    	player.sendMessage("/rc private <region> Set region to private chat");
    	player.sendMessage("/rc public <region>  Set region to public chat");
		return true;
    }
    
    public boolean CommandCanHear(Player player)
    {
    	if (!player.hasPermission("jrc.canhear")) 
    	{
    		sendPlayerMessage(player,"RegionChat: No permission to use this command",ChatColor.RED);
    		return false;
    	}
    
    	Location location = player.getLocation();
        ArrayList<Player> list = plugin.GetPlayersInEarshot(location);

		// Send to only selected players if required
		if(list==null) 
		{
			player.sendMessage("RegionChat: Everyone can hear you");
		}
		else
		{
			String pList ="";
			for (Player p : list)
			{
				if(p!=player)
				{
					if(pList.length()>0) pList += ",";
					pList += p.getDisplayName();
				}
			}
			
			if(pList.length()>0)
			{
				player.sendMessage("RegionChat: The following players can hear you:");
				player.sendMessage(pList);
			}
			else
				player.sendMessage("RegionChat: Nobody can hear you...");
		}
		return true;
    }

    public void sendPlayerMessage(Player player,String message)
    {
    	player.sendMessage(message);
    }
    
    public void sendPlayerMessage(Player player,String message,ChatColor color)
	{
		player.sendMessage(color + message + ChatColor.WHITE);
	}
}
