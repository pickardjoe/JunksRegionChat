package com.computerosity.bukkit.regionchat;

import java.util.ArrayList;

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
    	boolean ok = false;
    	
    	Player player = null;
        if ((sender instanceof Player)) player = (Player) sender;

        if(player==null) return false;
        
    	if (!player.hasPermission("jrc.public")) 
    	{
    		player.sendMessage("No permission to use this command");
    		return ok;
    	}

        if (split.length == 0)
            ok = CommandHelp(player);
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
            	ok = CommandCanHear(player);
            else if (token.equalsIgnoreCase("private"))
            	ok = CommandPrivate(player,arg);
            else if (token.equalsIgnoreCase("public"))
            	ok = CommandPublic(player,arg);
        }
        
    	return ok;
    }
    
    private boolean CommandPrivate(Player player,String region)
	{
    	if (!player.hasPermission("jrc.private")) 
    	{
    		player.sendMessage("No permission to use this command");
    		return false;
    	}

    	if(region.length()==0)
    	{
    		player.sendMessage("Syntax: /rc private <region>");
    		return false;
    	}
    	
    	if(plugin.SetRegion(region, player.getName()))		
		{
			player.sendMessage("RegionChat set as PRIVATE");
		}
    	else
    	{
    		player.sendMessage("RegionChat set failed (already set?)");
    	}
		return true;
	}

	private boolean CommandPublic(Player player,String region)
	{
    	if (!player.hasPermission("jrc.public")) 
    	{
    		player.sendMessage("No permission to use this command");
    		return false;
    	}

    	if(region.length()==0)
    	{
    		player.sendMessage("Syntax: /rc public <region>");
    		return false;
    	}
    	
		if(plugin.UnsetRegion(region,player.getName()))
		{
			player.sendMessage("RegionChat set as PUBLIC");
		}
    	else
    	{
    		player.sendMessage("RegionChat unset failed (not found)");
    	}
		return true;

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
    		player.sendMessage("No permission to use this command");
    		return false;
    	}
    
    	Location location = player.getLocation();
        ArrayList<Player> list = plugin.GetPlayersInEarshot(location);

		// Send to only selected players if required
		if(list==null) 
		{
			player.sendMessage("Everyone can hear you");
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
				player.sendMessage("The following players can hear you:");
				player.sendMessage(pList);
			}
			else
				player.sendMessage("Nobody can hear you...");
		}
		return true;
    }
}
