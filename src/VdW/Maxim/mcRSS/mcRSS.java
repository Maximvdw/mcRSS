package VdW.Maxim.mcRSS;

/* Name:		mcRSS
 * Version: 	1.3.0
 * Made by: 	Maxim Van de Wynckel
 * Build date:	21/01/2013						
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class mcRSS extends JavaPlugin {
	// This is the startup class, it is the class
	// that will start and load all threads and data

	public final Logger logger = Logger.getLogger("Minecraft"); // The logger
																// allows you to
																// post in
																// console
	public static mcRSS plugin; // Just type plugin when referencing to mcRSS
	public final listener pl = new listener(this);
	public boolean[] FirstCache;
	public String[] changes;

	// Global Variable feeds
	public static String[][] feeds;
	public static Document[] doc_feeds;

	// Configuration Files
	File configFile;
	FileConfiguration config;
	File rssFile;
	FileConfiguration rss;

	// Default language text
	static String error_permissions = "&cYou do not have permission!";
	static String error_console = "This function is only available ingame!";
	static String warning_cache_done = "&aCache complete!";
	static String warning_reload_done = "&amcRSS Reload complete!";
	static String error_add = "&cTo add an RSS feed &4/mcrss reader add <feed> <title> <url>\n&cReplace spaces with &4%20 &c!";
	static String error_remove = "&cTo remove an RSS feed &4/mcrss reader remove <feed>";
	static String warning_changes = "[mcRSS] &aThere are new updates in the feed &2";
	static String confirm_added = "&aThe RSS feed has been added!";
	static String confirm_removed = "&aThe RSS feed has been removed!";

	private static mcRSS instance = null;

	public static mcRSS getInstance() {
		if (instance == null) {
			instance = new mcRSS();
		}
		return instance;
	}

	@Override
	public void onEnable() {
		// Load String Shortcuts
		// Put this at the start of every routine
		final PluginDescriptionFile pdfFile = this.getDescription();
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//

		// This function will be started when the plugin is Enabled
		// Load everything here

		// Show plugin information in console
		this.logger.info(cmdFormat + "Made by: Maxim Van de Wynckel");

		// First load all configurations
		// If a configuration file does not exist, create one
		this.logger.info(cmdFormat + "Loading configuration file...");
		// first we have to initialize all Files and FileConfigurations
		configFile = new File(getDataFolder(), "config.yml"); // //
		rssFile = new File(getDataFolder(), "rss.yml"); // //
		// then we use firstRun(); method
		try {
			firstRun();
		} catch (Exception e) {
			// Error
		}

		// and we declare the FileConfigurations using YamlConfigurations and
		// then we just use loadYamls(); method
		// this is the critical part, this is needed cause if we do not use
		// this,
		// it will read from the yml located at your jar, not in
		// /plugins/<pluginName>/*yml.
		config = new YamlConfiguration();
		rss = new YamlConfiguration();
		loadYamls();
		// Check if the plugin needs to be enabled
		if (config.getBoolean("enabled") == false) {
			// Stop the plugin
			return;
		}
		// Get config version !!!
		if (config.getInt("version") != 2) {
			try {
				this.logger.info(cmdFormat + "Updating Configuration file!");
				// NO version
				// Copy defaults
				configFile.getParentFile().mkdirs(); // creates the
				// /plugins/<pluginName>/
				// directory if not found
				this.logger.info(cmdFormat + "Copying config.yml file!");
				copy(getResource("config.yml"), configFile); // copies the yaml
																// from
				// your jar to the
				// folder
				// /plugin/<pluginName>
				loadYamls();
			} catch (Exception ex) {
				// Error
				this.logger.severe(cmdFormat + "Unable to update config.yml!");
			}
		}

		// Load Listener
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.pl, this);

		// Load all config settings concerning the rss reader
		loadConfig_feeds();
		this.logger.info(cmdFormat + "Configuration file loaded!.");
		this.logger.info(cmdFormat + "Loaded cache time ["
				+ config.getInt("cache_time") + "]");

		// Now start a thread that downloads the data every X seconds
		this.getServer().getScheduler()
				.runTaskTimerAsynchronously(this, new Runnable() {
					public void run() {
						Getdata(null);
					}
				}, 0L, (config.getLong("cache_time") * 20));

		// Load Metrics
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
		this.logger.info(cmdFormat + "Metrics Stats loaded!");

		// Finally
		this.logger.info(cmdFormat + pdfFile.getName() + " "
				+ pdfFile.getVersion() + " has been Enabled!");
	}

	private void firstRun() throws Exception {
		// Load String Shortcuts
		// Put this at the start of every routine
		final PluginDescriptionFile pdfFile = this.getDescription();
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		//

		if (!configFile.exists()) { // checks if the yaml does not exists
			configFile.getParentFile().mkdirs(); // creates the
													// /plugins/<pluginName>/
													// directory if not found
			this.logger.info(cmdFormat + "Copying config.yml file!");
			copy(getResource("config.yml"), configFile); // copies the yaml from
															// your jar to the
															// folder
															// /plugin/<pluginName>
		}
		if (!rssFile.exists()) { // checks if the yaml does not exists
			rssFile.getParentFile().mkdirs(); // creates the
												// /plugins/<pluginName>/
												// directory if not found
			this.logger.info(cmdFormat + "Copying rss.yml file!");
			copy(getResource("rss.yml"), rssFile); // copies the yaml from
													// your jar to the
													// folder
													// /plugin/<pluginName>
		}
	}

	/*
	 * this copy(); method copies the specified file from your jar to your
	 * /plugins/<pluginName>/ folder
	 */
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadYamls() {
		try {
			config.load(configFile); // loads the contents of the File to its
										// FileConfiguration
			rss.load(rssFile); // loads the contents of the File to its
			// FileConfiguration
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveYamls() {
		try {
			config.save(configFile); // saves the FileConfiguration to its File
			rss.save(rssFile); // saves the FileConfiguration to its File
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		// This function will be started when the plugin is Disabled
		// Stop everything here
		saveYamls();
	}

	public Player getPlayer(String player) {
		// Return player name
		return Bukkit.getPlayer(player);
	}

	public boolean isValidPlayer(String player) {
		Player targetPlayer = Bukkit.getPlayer(player);
		// Check if player exist
		if (targetPlayer == null) {
			// No player
			return false;
		} else {
			return true;
		}
	}

	public void loadConfig_feeds() {
		// Load String Shortcuts
		// Put this at the start of every routine
		PluginDescriptionFile pdfFile = this.getDescription();
		String cmdFormat = "[" + pdfFile.getName() + "] ";

		this.logger.info(cmdFormat + "Loading Feed data from config...");
		// Start loading
		// First do a quick check to see how many feeds are entered

		// This gets a list (feeds) with all subcats
		Object[] feeds_list = rss.getConfigurationSection("feeds")
				.getKeys(false).toArray();
		this.logger.info(cmdFormat + "Loaded " + feeds_list.length
				+ " feeds in rss.yml file!");

		// Dynamically resize the doc_feeds and feeds array
		feeds = new String[feeds_list.length + 1][4];
		doc_feeds = new Document[feeds_list.length + 1];
		changes = new String[feeds_list.length + 1];
		FirstCache = new boolean[feeds_list.length + 1];

		// Now save all data to feeds
		for (int i = 0; i < feeds_list.length; i++) {
			feeds[i][0] = rss.getString("feeds." + feeds_list[i].toString()
					+ ".title"); // Get title of Feed
			feeds[i][1] = feeds_list[i].toString(); // Get shortcut
			feeds[i][2] = rss.getString("feeds." + feeds_list[i].toString()
					+ ".url"); // Get url of feed
			feeds[i][3] = rss.getString("feeds." + feeds_list[i].toString()
					+ ".enabled"); // Get if enabled True/False
			FirstCache[i] = true; // So it will not check for Changes yet
		}
		this.logger.info(cmdFormat + "RSS Feeds from config loaded!");
	}

	public void reload(Player player, PluginDescriptionFile pdfFile) {
		// Load String Shortcuts
		// Put this at the start of every routine
		String cmdFormat = "[" + pdfFile.getName() + "] ";

		// Reload mcRSS plugin
		// First check if the player has permissions
		if (player == null) {
			this.logger.info(cmdFormat + "Reloading mcRSS plugin!");
			try {
				loadYamls();
				loadConfig_feeds();
			} catch (Exception e) {
				this.logger.severe(cmdFormat + "Unable to reload plugin!");
			}
		} else {
			// Check if player has permissions
			if (player.hasPermission("mcrss.reload")) {
				this.logger.info(cmdFormat + "Reloading mcRSS plugin!");
				try {
					loadYamls();
					loadConfig_feeds();
				} catch (Exception e) {
					this.logger.severe(cmdFormat + "Unable to reload plugin!");
				}
				player.sendMessage(chatColor.stringtodata(warning_reload_done));
			} else {
				// No permission
				// Show that he has no permission
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
	}

	public void addFeed(Player player, String title, String url, String shortcut) {
		// Load String Shortcuts
		// Put this at the start of every routine
		PluginDescriptionFile pdfFile = this.getDescription();
		String cmdFormat = "[" + pdfFile.getName() + "] ";

		// Add a feed to rss.yml
		try {
			if (player == null) {
				// Console message
			} else {
				// Player message
				if (player.hasPermission("mcrss.rss.add")) {
					// Player has permissions
				} else {
					// No permissions
					player.sendMessage(chatColor
							.stringtodata(error_permissions));
					return;
				}
			}
			// Execute
			rss.set("feeds." + shortcut + ".title", title);
			rss.set("feeds." + shortcut + ".url", url);
			rss.set("feeds." + shortcut + ".enabled", true);
			this.saveYamls();
			this.loadConfig_feeds();

			// Show message
			if (player == null) {
				this.logger.info(cmdFormat + "The feed " + shortcut
						+ " has been added!");
			} else {
				player.sendMessage(chatColor.stringtodata(confirm_added));
			}

			this.Getdata(player);
		} catch (Exception ex) {
			// Error ocured
			if (player == null) {
				// Console message
				this.logger.severe(cmdFormat
						+ "Unable to add the given RSS feed!");
			} else {
				// Player message
				player.sendMessage(chatColor.stringtodata(error_add));
			}
		}
	}

	public void removeFeed(Player player, String feed) {
		// Load String Shortcuts
		// Put this at the start of every routine
		PluginDescriptionFile pdfFile = this.getDescription();
		String cmdFormat = "[" + pdfFile.getName() + "] ";

		// Remove a feed
		try {
			if (player == null) {
				// Console
			} else {
				if (player.hasPermission("mcrss.rss.remove")) {
					// Has permissions
				} else {
					// No permissions
					player.sendMessage(chatColor
							.stringtodata(error_permissions));
					return;
				}
			}
			rss.set("feeds." + feed + ".enabled", false);
			this.saveYamls();
			this.loadConfig_feeds();

			// Show message
			if (player == null) {
				this.logger.info(cmdFormat + "The feed " + feed
						+ " has been removed!");
			} else {
				player.sendMessage(chatColor.stringtodata(confirm_removed));
			}

			this.Getdata(player);
		} catch (Exception ex) {
			// Error ocured
			if (player == null) {
				// Console message
				this.logger.severe(cmdFormat
						+ "Unable to remove the given RSS feed!");
			} else {
				// Player message
				player.sendMessage(chatColor.stringtodata(error_remove));
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String cmdLabel, String[] args) {
		// This code will execute when a command has been entered
		// At this point we do not know what command, or who did it
		Logger logger = Logger.getLogger("Minecraft");
		PluginDescriptionFile pdfFile = this.getDescription();

		// Convert the sender to player
		// Set Default player to NULL
		Player player = null;
		// Check if it is a real player
		if (sender instanceof Player) {
			// Add playerID to player
			player = (Player) sender;
		}

		// Check if the player entered a valid command (Only mcrss or rss are
		// allowed)
		// This class will only check for the first row of Arguments
		if (cmdLabel.equalsIgnoreCase("mcrss")
				|| cmdLabel.equalsIgnoreCase("rss")) {
			// Valid Command Entered
			if (args.length == 0) {
				// Show help when command argument contains "help" "?" or
				// nothing
				help.mcrss_help(player, pdfFile);
			} else {
				if (args[0].equalsIgnoreCase("help")
						|| args[0].equalsIgnoreCase("?")) {
					// Show help when command argument contains "help" "?" or
					// nothing
					help.mcrss_help(player, pdfFile);
				} else if (args[0].equalsIgnoreCase("reader")) {
					// Check if more arguments are given
					if (args.length == 1) {
						// Show help when command argument contains "help" "?"
						// or nothing
						help.mcrss_rss_help(player, pdfFile);
					} else {
						if (args[1].equalsIgnoreCase("help")
								|| args[1].equalsIgnoreCase("?")) {
							// Show help when command argument contains "help"
							// "?" or nothing
							help.mcrss_rss_help(player, pdfFile);
						} else if (args[1].equalsIgnoreCase("params")) {
							if (args.length == 2) {
								// Show help when command argument contains
								// "help" "?" or nothing
								help.mcrss_rss_params_help(player, pdfFile);
							} else {
								help.mcrss_rss_params_help(player, pdfFile);
							}
						} else if (args[1].equalsIgnoreCase("list")) {
							// List all available feeds
							help.mcrss_rss_list(player, pdfFile, feeds);
						} else if (args[1].equalsIgnoreCase("cache")) {
							// Force cache
							Getdata(player);
						} else if (args[1].equalsIgnoreCase("add")) {
							// Add an RSS feed
							// Check if an extra argument has been given
							if (args.length != 5) {
								// Show help when command argument contains
								player.sendMessage(chatColor
										.stringtodata(error_add));
							} else {
								// Arg given
								this.addFeed(player,
										args[3].replaceAll("%20", " "),
										args[4].replaceAll("%20", " "), args[2]);
							}
						} else if (args[1].equalsIgnoreCase("remove")) {
							// Add an RSS feed
							// Check if an extra argument has been given
							if (args.length != 3) {
								// Show help when command argument contains
								player.sendMessage(chatColor
										.stringtodata(error_remove));
							} else {
								// Arg given
								this.removeFeed(player, args[2]);
							}
						} else {
							// A Feed has been given
							// Send all data to class
							reader.feeds = feeds;
							reader.doc_feeds = doc_feeds;
							if (player == null) {
								// Unable to perform from within console
								logger.info(error_console);
							} else {
								reader method = new reader(this);
								method.getArguments(player, args, pdfFile);
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("reload")) {
					// Reload the plugin
					reload(player, pdfFile);
				} else if (args[0].equalsIgnoreCase("about")) {
					// Show more info about the plugin
					help.mcrss_about(player, pdfFile);
				} else {
					// Unknown command
					// Show help
					help.mcrss_help(player, pdfFile);
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	public void Getdata(Player player) {
		// Load String Shortcuts
		// Put this at the start of every routine
		PluginDescriptionFile pdfFile = this.getDescription();
		Logger logger = Logger.getLogger("Minecraft");

		if (player == null) {
			// Do nothing
		} else {
			if (player.hasPermission("mcrss.rss.cache")) {
				// Do nothing
			} else {
				// Show that he has no permission
				player.sendMessage(chatColor.stringtodata(error_permissions));
			}
		}
		String cmdFormat = "[" + pdfFile.getName() + "] ";

		try {
			// Show info
			logger.info("[" + pdfFile.getName() + "] " + "Downloading data...");

			// Load every Feed
			for (int i = 0; i < feeds.length - 1; i++) {
				if (feeds[i][3].equalsIgnoreCase("true")
						|| feeds[i][3].equalsIgnoreCase("1")
						|| feeds[i][3].equalsIgnoreCase("yes")) {
					try {
						DocumentBuilder builder = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						URL u = new URL(feeds[i][2]); // your feed url
						doc_feeds[i] = builder.parse(u.openStream());
						logger.info("[" + pdfFile.getName() + "] "
								+ feeds[i][1] + " Feeds downloaded!");

						// Check for changes
						try {
							reader read = new reader(this);
							if (FirstCache[i] == false) {
								// Check if docs changed
								NodeList nodes;
								nodes = doc_feeds[i]
										.getElementsByTagName("item");
								String data_RSS = "";
								for (int j = 0; j < nodes.getLength(); j++) {
									Element element = (Element) nodes.item(j);
									data_RSS += reader.getElementValue(element,
											"title");
								}

								// Check if equal to changes
								if (!data_RSS.replace(changes[i], "#CONTROL#")
										.startsWith("#CONTROL#")) {
									// CHANGE
									// Send message to all players with notify
									// permission
									Player[] playerlist = this.getServer()
											.getOnlinePlayers();
									for (int j = 0; j < playerlist.length; j++) {
										// Send message
										if (playerlist[j]
												.hasPermission("mcrss.rss.notify")
												|| playerlist[j]
														.hasPermission("mcrss.rss.notify."
																+ feeds[i][1])) {
											// Player has permissions to get notified
											playerlist[j].sendMessage(chatColor
											.stringtodata(warning_changes
													+ feeds[i][1]));
										}
									}
								}
								changes[i] = data_RSS; // Set changes
														// to latest
							} else {
								// Save MD5 hash of content
								NodeList nodes;
								nodes = doc_feeds[i]
										.getElementsByTagName("item");
								String data_RSS = "";
								for (int j = 0; j < nodes.getLength(); j++) {
									Element element = (Element) nodes.item(j);
									data_RSS += reader.getElementValue(element,
											"title");
								}

								changes[i] = data_RSS;
								FirstCache[i] = false;
							}
						} catch (Exception err) {
							// Error while downloading from URL
							logger.severe("[" + pdfFile.getName() + "] "
									+ "ERROR: " + err.toString());
						}
					} catch (IOException dl) {
						// Error while downloading from URL
						logger.severe("[" + pdfFile.getName() + "] "
								+ "Error: Unable to download URL!");
						logger.severe("[" + pdfFile.getName() + "] "
								+ "Error: " + dl.getMessage());
						logger.severe("[" + pdfFile.getName() + "] " + "URL: '"
								+ feeds[i][2] + "' !");
					} catch (Exception dl) {
						// Error while downloading from URL
						logger.severe("[" + pdfFile.getName() + "] "
								+ "Error: Unable to download URL!");
						logger.severe("[" + pdfFile.getName() + "] " + "URL: '"
								+ feeds[i][2] + "' !");
					}
				}
			}
			logger.info("[" + pdfFile.getName() + "] "
					+ chatColor.stringtodelete(warning_cache_done));

			// Check if command was performed by player
			if (player != null) {
				// Send OK message
				player.sendMessage(chatColor.stringtodata(warning_cache_done));
			}
		} catch (Exception e) {
			logger.severe("[" + pdfFile.getName() + "] "
					+ "Error occured while downloading data!");
			logger.severe("[" + pdfFile.getName() + "] " + "ERROR: "
					+ e.getLocalizedMessage());
		}
	}
}