package VdW.Maxim.mcRSS;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class bookWriter {
	
	public bookWriter() {
	}

	ItemStack getWrittenCertificate(String forum, String content) {
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
		is.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
		BookMeta book = (BookMeta) is.getItemMeta();
		book.setAuthor("mcRSS");
		book.setTitle(ChatColor.RED + forum);

		List<String> pages = getBookPages(content);
		for (int i = 0; i < pages.size(); i++) {
			book.addPage(pages.get(i) + ChatColor.LIGHT_PURPLE);
		}
		is.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
		is.setItemMeta(book); // Save changes
		
		// Return the result
		return is;
	}

	private List<String> getBookPages(String content) {
		List<String> i = new ArrayList<String>();
		// A page may only contain 255 chars
		// So devide the content into pages
		try{
		int count = 0;
		int chunk_count = 0;
		for(int j=0;j<content.length()-1;j++){
			if(count==240 || count == content.length()){
				i.add(content.substring(count*chunk_count, count+count*chunk_count)); // Add page with chunk
				chunk_count +=1;
				count=0;
			}
			count += 1;
		}
		}catch(Exception ex){
			i.add(ChatColor.RED + "Something went wrong while making this book!\nCheck for an update of mcRSS!");
		}
		return i;
	}
}