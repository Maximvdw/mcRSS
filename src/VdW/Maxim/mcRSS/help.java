package VdW.Maxim.mcRSS;

import java.util.logging.Logger;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class help extends JavaPlugin {
	// Default language text
	static String error_permissions = "&cYou do not have permission!";
	static String error_console = "This function is only available ingame!";
	
	// Default help text
	static String mcrss_help_str = "&6----[ mcRSS Help ]----\n" + 
	"&b/mcrss reader&e Read RSS feeds\n" + 
    "&b/mcrss about&a Show plugin information\n" + 
	"&b/mcrss reload&c Reload the plugin";
	static String mcrss_rss_help_str = "&6----[ mcRSS Feed Help ]----\n" + 
	"&b/mcrss reader <feed>&e Show latest titles of a specific feed\n" +
	"&b/mcrss reader list&e List available feeds\n" + 
	"&b/mcrss reader params&e Show available parameters\n" + 
	"&b/mcrss reader add <feed>&c Add an RSS feed\n" +
	"&b/mcrss reader remove <feed>&c Remove an RSS feed\n" +
	"&b/mcrss reader cache&c Force cache all RSS feeds";
	static String mcrss_rss_params_help_str = "&6----[ mcRSS Feed Parameters Help ]----\n" + 
	"&b-node <number> &e Select a specific node\n" + 
	"&b-read&e Read the content\n" + 
	"&b-broadcast&c Broadcast to all players\n" + 
	"&b-private <playername>&c Send to player";
	static String mcrss_rss_list_str = "&6----[ mcRSS Feed List ]----";
	static String mcrss_rss_list_str2 = "&e Use this to replace <feed>";
	static String mcrss_about_str = "&6-----------[ mcRSS About ]-----------\n" +
			"&6Name: mcRSS - RSS Reader\n" +
			"&6Version: ";
	static String mcrss_about_str2 = "\n&" +
			"6Author: Maxim Van de Wynckel (Maximvdw)\n" +
			"&6Site: dev.bukkit.org/server-mods/mcrss\n" + 
			"&6------------------------------------";
	
	public static void mcrss_help(Player player, PluginDescriptionFile pdfFile){
		// CHANGE HELP ID HERE
		String id = mcrss_help_str;
		String Permission = "mcrss.help";
		
		// Load String Shortcuts
		// Put this at the start of every routine
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//
		
		Logger logger = Logger.getLogger("Minecraft");
		// Generate help text
		String HelpTxt = chatColor.stringtodata(id);
		String HelpTxt_console = chatColor.stringtodelete(id);
		
		// Send to sender
		if(player==null){
			// Command is executed by Console
			logger.info(cmdFormat + "Console peformed 'Help'" + "\n" + HelpTxt_console);
		}else{
			// Command is executed by player
			// Check if he has permissions
			if(player.hasPermission(Permission)){
				// Send help text
				player.sendMessage(HelpTxt);
			}else{
				// Error
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
	}
    public static void mcrss_about(Player player, PluginDescriptionFile pdfFile){
    	// CHANGE HELP ID HERE
		String id = mcrss_about_str;
		String Permission = "mcrss.about";
		
		// Load String Shortcuts
		// Put this at the start of every routine
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//
		
		Logger logger = Logger.getLogger("Minecraft");
		// Generate help text
		String HelpTxt = chatColor.stringtodata(id + pdfFile.getVersion() + mcrss_about_str2);
		String HelpTxt_console = chatColor.stringtodelete(id + pdfFile.getVersion() + mcrss_about_str2);
		
		// Send to sender
		if(player==null){
			// Command is executed by Console
			logger.info(cmdFormat + "Console peformed 'About'" + "\n" + HelpTxt_console);
		}else{
			// Command is executed by player
			// Check if he has permissions
			if(player.hasPermission(Permission)){
				// Send help text
				player.sendMessage(HelpTxt);
			}else{
				// Error
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
    }
	public static void mcrss_rss_help(Player player, PluginDescriptionFile pdfFile){
		// CHANGE HELP ID HERE
		String id = mcrss_rss_help_str;
		String Permission = "mcrss.rss.help";
		
		// Load String Shortcuts
		// Put this at the start of every routine
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//
		
		Logger logger = Logger.getLogger("Minecraft");
		// Generate help text
		String HelpTxt = chatColor.stringtodata(id);
		String HelpTxt_console = chatColor.stringtodelete(id);
		
		// Send to sender
		if(player==null){
			// Command is executed by Console
			logger.info(cmdFormat + "Console peformed 'Help'" + "\n" + HelpTxt_console);
		}else{
			// Command is executed by player
			// Check if he has permissions
			if(player.hasPermission(Permission)){
				// Send help text
				player.sendMessage(HelpTxt);
			}else{
				// Error
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
	}
	public static void mcrss_rss_params_help(Player player, PluginDescriptionFile pdfFile){
		// CHANGE HELP ID HERE
		String id = mcrss_rss_params_help_str;
		String Permission = "mcrss.rss.params";
		
		// Load String Shortcuts
		// Put this at the start of every routine
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//
		
		Logger logger = Logger.getLogger("Minecraft");
		// Generate help text
		String HelpTxt = chatColor.stringtodata(id);
		String HelpTxt_console = chatColor.stringtodelete(id);
		
		// Send to sender
		if(player==null){
			// Command is executed by Console
			logger.info(cmdFormat + "Console peformed 'Help'" + "\n" + HelpTxt_console);
		}else{
			// Command is executed by player
			// Check if he has permissions
			if(player.hasPermission(Permission)){
				// Send help text
				player.sendMessage(HelpTxt);
			}else{
				// Error
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
	}
	public static void mcrss_rss_list(Player player, PluginDescriptionFile pdfFile, String[][] feeds){
		// CHANGE HELP ID HERE
		String id = mcrss_rss_list_str;
		String Permission = "mcrss.rss.list";
		
		// Load String Shortcuts
		// Put this at the start of every routine
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//
		
		Logger logger = Logger.getLogger("Minecraft");
		
		for (int i = 0; i < feeds.length-1; i++) {
			if (feeds[i][3].equalsIgnoreCase("true")
					|| feeds[i][3].equalsIgnoreCase("1")
					|| feeds[i][3].equalsIgnoreCase("yes")) {
				id = id + "\n&a" + feeds[i][0];
				id = id + "\n&2" + feeds[i][1] + mcrss_rss_list_str2;
			}
		}
		// Generate help text
		String HelpTxt = chatColor.stringtodata(id);
		String HelpTxt_console = chatColor.stringtodelete(id);
		
		// Send to sender
		if(player==null){
			// Command is executed by Console
			logger.info(cmdFormat + "Console peformed 'Feed List'" + "\n" + HelpTxt_console);
		}else{
			// Command is executed by player
			// Check if he has permissions
			if(player.hasPermission(Permission)){
				// Send help text
				player.sendMessage(HelpTxt);
			}else{
				// Error
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
	}
}
