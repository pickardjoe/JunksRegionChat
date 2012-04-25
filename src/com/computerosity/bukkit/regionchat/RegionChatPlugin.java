package com.computerosity.bukkit.regionchat;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.plugin.Plugin;

// =============================================================
// Class       : RegionChattPlugin
//
// Description : Main plugin
//
// Author      : Junkman
// =============================================================
public class RegionChatPlugin extends JavaPlugin
{
	private Logger log = Logger.getLogger("Minecraft");
	private WorldGuardPlugin wgPlugin=null;
	public String messageFormat="";
	public ChatColor messageColour;
	private Hashtable<String,String> regions = new Hashtable<String,String>();
	
	@SuppressWarnings("unused")
	private RegionChatPlayerListener playerListener=null;
	
    @Override
    public void onEnable()
    {
    	// Link into worldguard
    	wgPlugin = getWorldGuardPlugin();
    	
    	if(wgPlugin==null)
    	{
    		reportError(null,"WorldGuard is required for RegionChat, disabling plugin",false);
    		this.setEnabled(false);
    		return;
    	}
    	
        // Load configuration
    	loadConfig();
    	
        getCommand("rc").setExecutor(new RegionChatCommand(this));

    	// Setup listener
    	playerListener = new RegionChatPlayerListener(this);
    	
        // Write initialisation success
        PluginDescriptionFile pdfFile = this.getDescription();
        WriteToConsole("RegionChat Version " + pdfFile.getVersion() + " is enabled");
    }
    
    public void loadConfig()
    {
		// Get configuration file
        getConfig().options().copyDefaults(true);
		FileConfiguration config = this.getConfig();

		messageFormat = config.getString("format", "({DISPLAYNAME}) {MESSAGE}");
		try
		{
			messageColour = ChatColor.valueOf(config.getString("colour", "GRAY"));
		}
		catch(IllegalArgumentException e)
		{
			reportError(e,"Invalid colour in config file, reverting to default",false);
			config.set("colour","GRAY");
			messageColour = ChatColor.GRAY;
		}
		
		// Load regions
		int rcCount=0;
		ConfigurationSection rs = config.getConfigurationSection("regions");
		if(rs!=null)
		{
			Map<String,Object> keys =  rs.getValues(true);
			if(keys!=null)
			{
				for(String key : keys.keySet())
				{
					SetRegion(key,(String) keys.get(key));
					rcCount++;
				}
			}
			WriteToConsole("Loaded " + rcCount + " region(s)");
		}
		
    	// Save file
    	saveConfig();
    }

	public void SaveConfig()
	{
        getConfig().options().copyDefaults(true);
		FileConfiguration config = this.getConfig();
		
		config.set("format",messageFormat);
		config.set("colour",messageColour.name());
		
		// Write regions
		ConfigurationSection cr = config.createSection("regions");
		for(String key : regions.keySet())
		{
			cr.set(key,regions.get(key));
		}
		
    	// Save file
    	saveConfig();
	}

	private WorldGuardPlugin getWorldGuardPlugin()
	{
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin))
	    {
	        return null; // Maybe you want throw an exception instead
	    }
	    
	    return (WorldGuardPlugin) plugin;
	}

	public boolean SetRegion(String region,String owner)
	{
		if(regions.get(region)!=null) return false;
		
		regions.put(region, owner);
		
		SaveConfig();
		
		return true;
	}
	
	public boolean UnsetRegion(String region,String player)
	{
		if(regions.get(region)==null) return false;
		
		// TODO: Check permissions...
		regions.remove(region);
		
		SaveConfig();
		
		return true;
	}

	private boolean isPrivateRegion(String region)
	{
		for(String key : regions.keySet())
		{
			if(key.equalsIgnoreCase(region)) return true;
		}
		return false;
	}
	
	// Save the configuration
	// Get a list of players who can hear, return null if it's everyone
	public ArrayList<Player> GetPlayersInEarshot(Location location)
	{
		ArrayList<Player> list = null;
		
		// Is player in a worldguard region?
		RegionManager regionManager = wgPlugin.getRegionManager(location.getWorld());
		ApplicableRegionSet set = regionManager.getApplicableRegions(location);
		
		for (ProtectedRegion region : set)
		{
			String name = region.getId();
		    
			if(isPrivateRegion(name)) //if (region is a regionchat region)
			{
				// Setup new message list
				if(list==null) list = new ArrayList<Player>();
				
		    	// Only send to players who are also in that region
				for (Player p : getServer().getOnlinePlayers())
				{
					Location l = p.getLocation();
					
					if(region.contains(toVector(l))) 
					{
						if(!list.contains(p)) list.add(p);
					}
				}
			}
		}
		
		return list;
	}

	public void WriteToConsole(String message)
    {
        System.out.println("[JRC] " + message);
    }
    
    public void reportError(Exception e, String message)
    {
        reportError(e,message,true);
    }
    
    public void reportError(Exception e, String message,boolean dumpStackTrace)
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.severe("[JRC " + pdfFile.getVersion() + "] " + message);
        if(dumpStackTrace) e.printStackTrace();
    }
    
    public void ReportWarning(String message)
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.warning("[JRC " + pdfFile.getVersion() + "] " + message);
    }

	public WorldGuardPlugin getWorldGuard()
	{
		return wgPlugin;
	}

	public String IsRegionOwner(Player player, String region)
	{
		RegionManager regionManager = wgPlugin.getRegionManager(player.getWorld());
		ProtectedRegion pregion = regionManager.getRegion(region);
		if (pregion==null) return null;
		
		DefaultDomain d = pregion.getOwners();
		if(d.contains(player.getName()))
			return player.getName();
		else
			return "";
	}
}
