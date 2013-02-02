package VdW.Maxim.mcRSS;

/* Name:		mcRSS
 * Version: 	1.3.0
 * Made by: 	Maxim Van de Wynckel
 * Build date:	21/01/2013						
 */

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class reader extends JavaPlugin {

	// Gather data from main class
	public static String[][] feeds;
	public static Document[] doc_feeds;
	public static mcRSS plugin; // Just type plugin when referencing to
								// mcRSS
	public final Logger logger = Logger.getLogger("Minecraft"); // The logger
																// allows you to
																// post in
																// console
	Plugin mcRSS;

	// Language texts
	static String error_unknown_feed = "&cUnknown feed given!";
	static String error_available_feed = "&cAvailable feeds:";
	static String error_permissions = "&cYou do not have permission!";
	static String error_unknown_player = "&cNo valid player!";
	static String error_unknown_node = "&cUnknown node given!";
	static String error_conflict_broadcast = "&cBroadcast and Private can't be combined!";
	static String error_send_yourself = "&cYou cannot send private messages to yourself!";
	static String error_node_border = "&cPlease choose a node between 1-10!";
	static String warning_private_send = "&i&8[mcRSS] Private feed has been send to ";
	static String warning_private_from = "&i&8[mcRSS] Private feed from ";
	static String warning_book = "&i&8[mcRSS] Book placed in inventory";

	// Parameters
	public static boolean broadcast = false; // Option broadcast
	public static boolean readnode = false; // Option node
	public static boolean readcontent = false; // Option read
	public static boolean privatemessage = false; // Option private
	public static boolean book = false; // Option book
	public static ItemStack book_item = null; // The book item
	public static String readnode_number = "1"; // The integer of option node
	public static int readnode_number_int = 1; // The integer of option node
	public static String privatemessage_name; // The string of option private
	public static int feed_id; // This is the feed id in feeds[X][]
	public static NodeList nodes; // This is a list with all nodes within a feed
	public static Player targetPlayer;

	public reader(Plugin name){
		// Get player list
		mcRSS = name;
	}
	
	public String getStringFromDocument(Document doc)
	{
	    try
	    {
	       DOMSource domSource = new DOMSource(doc);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       return writer.toString();
	    }
	    catch(TransformerException ex)
	    {
	       ex.printStackTrace();
	       return null;
	    }
	}
	
	public void getArguments(Player player, String[] args,
			PluginDescriptionFile pdfFile) {
		String cmdFormat = "[" + pdfFile.getName() + "] ";
		// Check if player has basic permission
		if (player.hasPermission("mcrss.rss.read")) {
			// Delete previous data
			broadcast = false;
			readnode = false;
			readcontent = false;
			privatemessage = false;
			readnode_number = "0";
			privatemessage_name = "";
			feed_id = 0;
			nodes = null;
			book = false;
			book_item = null;

			// Get all arguments and selected params
			// First get what feed is selected
			for (int i = 0; i < feeds.length; i++) {
				if (args[1].equalsIgnoreCase(feeds[i][1])) {
					// That feed is selected
					try {
						nodes = doc_feeds[i].getElementsByTagName("item");
						feed_id = i;
						break;
					} catch (Exception ex) {
						// No data in node
						this.logger.severe(cmdFormat + "The selected feed '"
								+ feeds[i][1] + "' Does not contain any data!");
					}
				}
			}
			// check if a node is found
			if (nodes == null) {
				// There is no topic given
				player.sendMessage(chatColor.stringtodata(error_unknown_feed));
				// Get all available feeds
				String id = error_available_feed;
				for (int i = 0; i < feeds.length - 1; i++) {
					if (feeds[i][3].equalsIgnoreCase("true")
							|| feeds[i][3].equalsIgnoreCase("1")
							|| feeds[i][3].equalsIgnoreCase("yes")) {
						id = id + " " + feeds[i][1];
					}
				}
				player.sendMessage(ChatColor.ITALIC
						+ chatColor.stringtodata(id));
				return;
			} else {
				// Check if any arguments are given
				for (int i = 2; i < args.length; i++) {
					// Check every argument
					if (args[i].equalsIgnoreCase("-b")
							|| args[i].equalsIgnoreCase("-broadcast")) {
						// Broadcast option given
						// Check if player has permissions to do this
						if (player.hasPermission("mcrss.rss.broadcast")) {
							broadcast = true;
						} else {
							// No permissions
							player.sendMessage(chatColor
									.stringtodata(error_permissions));
							return;
						}
					} else if (args[i].equalsIgnoreCase("-n")
							|| args[i].equalsIgnoreCase("-node")) {
						// Node option given
						// Check if player has permissions to do this
						if (player.hasPermission("mcrss.rss.read")) {
							readnode = true;
							// Get node
							readnode_number = args[i + 1];
							// Check if it is a number
							if (isInteger(readnode_number)) {
								// Do nothing
								readnode_number_int = Integer
										.parseInt(readnode_number) - 1;
							} else {
								// Fake argument
								player.sendMessage(chatColor
										.stringtodata(error_unknown_node));
								return;
							}
						} else {
							// No permissions
							player.sendMessage(chatColor
									.stringtodata(error_permissions));
							return;
						}
					} else if (args[i].equalsIgnoreCase("-r")
							|| args[i].equalsIgnoreCase("-read")) {
						// Read option given
						// Check if player has permissions to do this
						if (player.hasPermission("mcrss.rss.read")) {
							readcontent = true;
						} else {
							// No permissions
							player.sendMessage(chatColor
									.stringtodata(error_permissions));
							return;
						}
					} else if (args[i].equalsIgnoreCase("-pm")
							|| args[i].equalsIgnoreCase("-private")) {
						// Check if player has permissions to do this
						if (player.hasPermission("mcrss.rss.send")) {
							// Private option given
							privatemessage = true;
							// Get target
							privatemessage_name = args[i + 1];
						} else {
							// No permissions
							player.sendMessage(chatColor
									.stringtodata(error_permissions));
							return;
						}
					} else if (args[i].equalsIgnoreCase("-book")) {
						// Check if player has permissions to do this
						if (player.hasPermission("mcrss.rss.book")) {
							// Book option given
							book = true;
						} else {
							// No permissions
							player.sendMessage(chatColor
									.stringtodata(error_permissions));
							return;
						}
					}
				}
			}
		} else {
			// No permissions
			player.sendMessage(chatColor.stringtodata(error_permissions));
			return;
		}
		// Now check for conflicting params
		if (broadcast == true & privatemessage == true) {
			// Conflict
			player.sendMessage(chatColor.stringtodata(error_conflict_broadcast));
			return;
		}
		// Now check for valid arguments
		// Checks if it is a valid player
		if (privatemessage == true) {
			mcRSS method = new mcRSS();
			if (method.isValidPlayer(privatemessage_name) == false) {
				// Error
				player.sendMessage(error_unknown_player);
				return;
			}
			Player targetPlayer = Bukkit.getPlayer(privatemessage_name);
			// Check if player is not the same as target
			if (targetPlayer == player) {
				// Same
				player.sendMessage(chatColor.stringtodata(error_send_yourself));
				//return;
			}
		}
		// Check if it is a valid node
		if (readnode == true) {
			if (readnode_number_int >= 0 & readnode_number_int <= 9) {
				// It is good
			} else {
				// Error
				player.sendMessage(chatColor.stringtodata(error_node_border));
				return;
			}
		}

		// Now that we know all parameters
		// Lets gather data
		read(player, pdfFile);
	}

	private static String getCharacterDataFromElement(Element e) {
		try {
			Node child = e.getFirstChild();
			if (child instanceof CharacterData) {
				CharacterData cd = (CharacterData) child;
				return cd.getData();
			}
		} catch (Exception ex) {

		}
		return "";
	} // private String getCharacterDataFromElement

	protected float getFloat(String value) {
		if (value != null && !value.equals("")) {
			return Float.parseFloat(value);
		}
		return 0;
	}

	protected static String getElementValue(Element parent, String label) {
		try {
			return getCharacterDataFromElement((Element) parent
					.getElementsByTagName(label).item(0));
		} catch (Exception ex) {
			// Error POSSIBLE the element not found
		}
		return null;
	}

	private static reader instance = null;

	public reader getInstance() {
		if (instance == null) {
			instance = new reader(mcRSS);
		}
		return instance;
	}

	public void read(Player player, PluginDescriptionFile pdfFile) {
		// Create String with all data, then send it
		String data = "&6" + feeds[feed_id][0] + "\n";
		if (privatemessage == true) {
			// Add line of text
			data = data + warning_private_from + player.getName() + "\n";
		}
		if (readnode == true) {
			try {
				Element element = (Element) nodes.item(readnode_number_int);
				String str = getElementValue(element, "pubDate");
				SimpleDateFormat formatter = new SimpleDateFormat(
						"EEE, dd MMM yyyy HH:mm:ss ZZZZ", Locale.UK); // please
																		// notice
																		// the
																		// capital
																		// M
				Date date = formatter.parse(str);
				SimpleDateFormat formatter2 = new SimpleDateFormat(
						"dd/MM/yyyy HH:mm:ss");
				String authorName = "";
				if (getElementValue(element, "dc:creator") != "") {
					authorName = " by "
							+ getElementValue(element, "dc:creator");
				}
				data = data
						+ ("&b" + getElementValue(element, "title") + "&1"
								+ authorName + "\n");
				data = data
						+ ("&b" + "Date: " + "&1" + formatter2.format(date))
						+ "\n";
				data = data
						+ ("&b" + "URL: " + "&1" + getElementValue(element,
								"link")) + "\n";
				if (readcontent == true) {
					// Add description

					data = data
							+ ("&b"
									+ "Description: "
									+ "&1"
									+ StripHTML(getElementValue(element,
											"description")) + "\n");
				}
				data = data + ("\n");
			} catch (Exception ex) {
				// Error occured
			}
		} else {
			for (int i = 0; i < 5; i++) {
				if (i % 2 == 0) {
					// Light color
					Element element = (Element) nodes.item(i);
					String authorName = "";
					if (getElementValue(element, "dc:creator") != "") {
						authorName = " by "
								+ getElementValue(element, "dc:creator");
					}
					data = data
							+ ("&b" + getElementValue(element, "title") + "&1"
									+ authorName + "\n");
					data = data
							+ ("&b" + "URL: " + "&1" + getElementValue(element,
									"link")) + "\n";
					if (readcontent == true) {
						// Add description
						data = data
								+ ("&b" + "Description: " + "&1" + StripHTML(getElementValue(
										element, "description"))) + "\n";
					}
				} else {
					// Darker
					Element element = (Element) nodes.item(i);
					String authorName = "";
					if (getElementValue(element, "dc:creator") != "") {
						authorName = " by "
								+ getElementValue(element, "dc:creator");
					}
					data = data
							+ ("&a" + getElementValue(element, "title") + "&2"
									+ authorName + "\n");
					data = data
							+ ("&a" + "URL: " + "&2" + getElementValue(element,
									"link")) + "\n";
					if (readcontent == true) {
						// Add description
						data = data
								+ ("&a" + "Description: " + "&2" + StripHTML(getElementValue(
										element, "description"))) + "\n";
					}
				}
				data = data + "\n";
			}// for
		}
		// Send the message
		if (broadcast == true) {
			// Broadcast data
			data = chatColor.stringtodata(data);
			if (book == true) {
				// Broadcast a book to all players
				bookWriter writer = new bookWriter();
				// Get book item
				book_item = writer.getWrittenCertificate(feeds[feed_id][1],
						data);
				// Now place the book in his inventory
				Player playerlist[] = mcRSS.getServer().getOnlinePlayers();
				for (int i = 0; i < playerlist.length; i++) {
					PlayerInventory inventory = playerlist[i].getInventory();
					inventory.addItem(book_item); // Adds the book
					// Send message
					playerlist[i].sendMessage(chatColor
							.stringtodata(warning_private_from
									+ player.getName()));
				}
			} else {
				// Broadcast the text in chat
				Bukkit.broadcastMessage(data);
			}
		} else if (privatemessage == true) {
			// Private message data
			Player targetPlayer = mcRSS.getServer().getPlayer(privatemessage_name);
			// Convert data to gray
			data = data.replace("&b", "&7"); // Replace aqua with gray
			data = data.replace("&1", "&8"); // Replace blue with Darkgray
			data = data.replace("&a", "&7"); // Replace green with gray
			data = data.replace("&2", "&8"); // Replace darkgreen with Darkgray
			data = data.replace("&6", "&f"); // Replace gold with White
			data = chatColor.stringtodata(data);
			if (book == true) {
				// Send a book to a player
				bookWriter writer = new bookWriter();
				// Get book item
				book_item = writer.getWrittenCertificate(feeds[feed_id][1],
						data);
				// Now place the book in his inventory
				PlayerInventory inventory = targetPlayer.getInventory();
				inventory.addItem(book_item); // Adds the book
				// Send message
				targetPlayer.sendMessage(chatColor
						.stringtodata(warning_private_from + player.getName()));
			} else {
				// Send chat message
				targetPlayer.sendMessage(data);
				player.sendMessage(chatColor.stringtodata(warning_private_send
						+ targetPlayer.getName()));
			}
		} else if (privatemessage == false & broadcast == false) {
			// Send to player
			data = chatColor.stringtodata(data);
			if (book == true) {
				// Send a book to a player
				bookWriter writer = new bookWriter();
				// Get book item
				book_item = writer.getWrittenCertificate(feeds[feed_id][1],
						data);
				// Now place the book in his inventory
				PlayerInventory inventory = player.getInventory();
				inventory.addItem(book_item); // Adds the book
				// Send message
				player.sendMessage(chatColor.stringtodata(warning_book ));
			} else {
				// Send chat message
				player.sendMessage(data);
			}
		}
	}

	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String StripHTML(String source) {
		try {
			String result;

			// Remove HTML Development formatting
			// replace line breaks with space
			// because browsers inserts space
			result = source.replace("\n", " ");
			// replace line breaks with space
			// because browsers inserts space
			result = result.replace("\n", " ");
			// Remove step-formatting
			result = result.replace("\t", "");
			// Remove repeating spaces because browsers ignore them
			result = result.replaceAll("( )+", " ");

			// Remove the header (prepare first by clearing attributes)
			result = result.replaceAll("<( )*head([^>])*>", "<head>");
			result = result.replaceAll("(<( )*(/)( )*head( )*>)", "</head>");
			result = result.replaceAll("(<head>).*(</head>)", "");

			// remove all scripts (prepare first by clearing attributes)
			result = result.replaceAll("<( )*script([^>])*>", "<script>");
			result = result
					.replaceAll("(<( )*(/)( )*script( )*>)", "</script>");
			// result = result.replaceAll(result,
			// @"(<script>)([^(<script>\.</script>)])*(</script>)",
			// "",
			// System.Text.RegularExpressions.RegexOptions.IgnoreCase);
			result = result.replaceAll("(<script>).*(</script>)", "");

			// remove all styles (prepare first by clearing attributes)
			result = result.replaceAll("<( )*style([^>])*>", "<style>");
			result = result.replaceAll("(<( )*(/)( )*style( )*>)", "</style>");
			result = result.replaceAll("(<style>).*(</style>)", "");

			// insert tabs in spaces of <td> tags
			result = result.replaceAll("<( )*td([^>])*>", "\t");

			// insert line breaks in places of <BR> and <LI> tags
			result = result.replaceAll("<( )*br( )*>", "\n");
			result = result.replaceAll("<( )*li( )*>", "\n");

			// insert line paragraphs (double line breaks) in place
			// if <P>, <DIV> and <TR> tags
			result = result.replaceAll("<( )*div([^>])*>", "\n");
			result = result.replaceAll("<( )*tr([^>])*>", "\n\n");
			result = result.replaceAll("<( )*p([^>])*>", "\n\n");

			// Remove remaining tags like <a>, links, images,
			// comments etc - anything that's enclosed inside < >
			result = result.replaceAll("<[^>]*>", "");

			// replace special characters:
			result = result.replaceAll(" ", " ");

			result = result.replaceAll("&bull;", " * ");
			result = result.replaceAll("&lsaquo;", "<");
			result = result.replaceAll("&rsaquo;", ">");
			result = result.replaceAll("&trade;", "(tm)");
			result = result.replaceAll("&frasl;", "/");
			result = result.replaceAll("&lt;", "<");
			result = result.replaceAll("&gt;", ">");
			result = result.replaceAll("&copy;", "(c)");
			result = result.replaceAll("&reg;", "(r)");
			// Remove all others. More can be added, see
			// http://hotwired.lycos.com/webmonkey/reference/special_characters/
			result = result.replaceAll("&(.{2,6});", "");

			// for testing
			// result.replaceAll(result,
			// this.txtRegex.Text,"",
			// System.Text.RegularExpressions.RegexOptions.IgnoreCase);

			// make line breaking consistent
			result = result.replace("\n", "\n");

			// Remove extra line breaks and tabs:
			// replace over 2 breaks with 2 and over 4 tabs with 4.
			// Prepare first to remove any whitespaces in between
			// the escaped characters and remove redundant tabs in between line
			// breaks
			result = result.replaceAll("(\n)( )+(\n)", "\n\n");
			result = result.replaceAll("(\t)( )+(\t)", "\t\t");
			result = result.replaceAll("(\t)( )+(\n)", "\t\n");
			result = result.replaceAll("(\n)( )+(\t)", "\n\t");
			// Remove redundant tabs
			result = result.replaceAll("(\n)(\t)+(\n)", "\n\n");
			// Remove multiple tabs following a line break with just one tab
			result = result.replaceAll("(\n)(\t)+", "\n\t");
			// Initial replacement target String for line breaks
			String breaks = "\n\n\n";
			// Initial replacement target String for tabs
			String tabs = "\t\t\t\t\t";
			for (int index = 0; index < result.length(); index++) {
				result = result.replace(breaks, "\n\n");
				result = result.replace(tabs, "\t\t\t\t");
				breaks = breaks + "\n";
				tabs = tabs + "\t";
			}

			// That's it.
			return result;
		} catch (Exception ex) {
			return source;
		}
	}
}
