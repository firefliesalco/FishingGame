package com.firefliesalco.www;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Fishy extends JavaPlugin implements Listener {

	public ArrayList<String> possibleRods = new ArrayList<String>();
	
	public HashMap<Player, List<String>> rods = new HashMap<Player, List<String>>();
		
	public HashMap<Player, Integer> fishCaught = new HashMap<Player, Integer>();
	
	public HashMap<String, Location> ponds = new HashMap<String, Location>();
	public HashMap<Player, Boolean> atPond = new HashMap<Player, Boolean>();
	
	private Location spawn;
	
	public ArrayList<String> playerTop = new ArrayList<String>();
	public ArrayList<Integer> fishTop = new ArrayList<Integer>();
	
	private String[] rodMessages = {
			"You reeled in someone's lost %rod%!",
			"A treasure chest on the end of the line!  Oh look it's a %rod%!",
			"What luck!  A %rod%!",
			"Call your mom, it's a %rod%!",
			"A bright light shines from the water as you pull in a %rod%.",
			"What the hell is that? - Your friend Billy as you reel in a %rod%.",
			"\"No way you got a %rod%\".  You imagine the look on your friends' faces when you tell them your story.",
			"xD xD what a terrible %rod% - firefliesalco",
			"LOL someone dropped a %rod% in the ocean"
	};
	
	private String[] fishMessage = {
			"You feel a nibble on the end of your line.",
			"Your bobber goes under.",
			"A %fish% accidently jumped onto the bank. EZ.",
			"A %fish% lost a 1v1.",
			"A %fish% was banned from the lake by your fishing rod, nicknamed 'Watchdog'.",
			"Oh, I guess it's %fish% season.",
			"Any day you catch a %fish% is a good day.",
			"A dabbing %fish% was expelled from the school.",
			"Hey, this %fish% reminds me of Josh.",
			"I didn't even need to pull out my death note on this %fish%.",
			"This %fish% is actually a duck.",
			"This %fish%... makes me cri evrytiem."
			
	};
	
	private String[] fish ={"Trout", "Salmon", "Tuna", "Bass", "Bluefish", "Scup", "Squeteague", "Flounder", "Mackerel", "Bonito", "Cod", "Haddock", "Pollock", "Mako Shark", "Confused Fish"};
		
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
	    try {
	        if (!getDataFolder().exists()) {
	            getDataFolder().mkdirs();
	        }
	        File file = new File(getDataFolder(), "config.yml");
	        if (!file.exists()) {
	            getLogger().info("Config.yml not found, creating!");
	            saveDefaultConfig();
	        } else {
	            getLogger().info("Config.yml found, loading!");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();

	    }
	    
	    if(getConfig().getConfigurationSection("ponds") != null){
		    for(String s : getConfig().getConfigurationSection("ponds").getKeys(false)){
		    	ConfigurationSection cs = getConfig().getConfigurationSection("ponds." + s);
		    	ponds.put(s, new Location(getServer().getWorld(cs.getString("world")), cs.getInt("x"), cs.getInt("y"),cs.getInt("z")));
		    }
	    }
	    
	    if(getConfig().getConfigurationSection("spawn") != null){
	    	spawn = new Location(getServer().getWorld(getConfig().getString("spawn.world")), getConfig().getInt("spawn.x"), getConfig().getInt("spawn.y"), getConfig().getInt("spawn.z"));
	    }else{
	    	spawn = getServer().getWorld("world").getSpawnLocation();
	    }
	    
	    for(Player p : getServer().getOnlinePlayers()){
	    	loadPlayer(p);
	    }
	    
	    possibleRods = (ArrayList<String>) getConfig().getStringList("rods");
	    if(possibleRods == null)
	    	possibleRods = new ArrayList<String>();
		
	    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){public void run(){save();}}, 1000L, 1000L);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			
			public void run(){
				ConfigurationSection cs = getConfig().getConfigurationSection("players");
				if(cs!=null){
					HashMap<String, Integer> leaderboards = new HashMap<String, Integer>();
					playerTop.clear();
					fishTop.clear();
					for(String uuid : cs.getKeys(false)){
						leaderboards.put(getServer().getOfflinePlayer(UUID.fromString(uuid)).getName(), cs.getInt(uuid + ".fish"));
					}
					
					for(int i = 0; i < Math.min(10, leaderboards.size()); i++){
						String topKey = "";
						int topVal = 0;
						for(String key : leaderboards.keySet()){
							if(leaderboards.get(key) >= topVal){
								topKey = key;
								topVal = leaderboards.get(key);
							}
						}
						leaderboards.remove(topKey);
						playerTop.add(topKey);
						fishTop.add(topVal);
					}
				}
			}
			
		}, 100L, 12000L);
		
	
	}
	
	@Override
	public void onDisable(){
		save();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(label.equalsIgnoreCase("fish")){
			
			if(args.length == 0){
				sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "go");
				sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "rod");
				sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "stats");
				sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "leave");
				if(sender.hasPermission("fish.admin")){
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "list");
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "listrods");
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "delete <name>");
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "create <name>");
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "addrod <name> <rarity (1-6)>");
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "delrod <name> <rarity>");
					sender.sendMessage(ChatColor.GOLD + "/fish " + ChatColor.AQUA + "setspawn");
				}
				
			}
			if(args.length == 1){
				if(args[0].equalsIgnoreCase("go")){
					Random r = new Random();
					Object[] l = ponds.values().toArray();
					((Player)sender).teleport((Location)l[r.nextInt(l.length)]);
					atPond.put((Player)sender, true);
					sender.sendMessage("Teleported to pond.");
				}
				if(args[0].equalsIgnoreCase("rod")){
					openInv((Player)sender, 0);
				}
				if(args[0].equalsIgnoreCase("list")&&sender.hasPermission("fish.admin")){
					int counter = 0;
					for(String pondName : ponds.keySet()){
						counter++;
						sender.sendMessage(counter + ": " + pondName);
					}
				}
				if(args[0].equalsIgnoreCase("listrods")&&sender.hasPermission("fish.admin")){
					int counter = 0;
					for(String pondName : possibleRods){
						counter++;
						sender.sendMessage(counter + ": " + pondName);
					}
				}
				if(args[0].equalsIgnoreCase("leave")){
					((Player)sender).getInventory().remove(Material.FISHING_ROD);
					((Player)sender).teleport(spawn);
					atPond.put((Player)sender, false);
				}
				if(args[0].equalsIgnoreCase("stats")){
					sender.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GOLD + fishCaught.get((Player)sender) + ChatColor.AQUA + " fish");
					sender.sendMessage(ChatColor.GOLD + "-----------------------");
					for(int i = 0; i < Math.min(10, playerTop.size()); i++){
						sender.sendMessage(ChatColor.GOLD + "#" + (i+1) + ": " + ChatColor.AQUA + playerTop.get(i) + " - " + fishTop.get(i));
					}
				}
				if(args[0].equalsIgnoreCase("setspawn")&&sender.hasPermission("fish.admin")){
					sender.sendMessage("Set Spawn");
					spawn = ((Player)sender).getLocation();
				}
			}
			if(args.length == 2){
				if(args[0].equals("create")&&sender.hasPermission("fish.admin")){
					if(ponds.containsKey(args[1].toUpperCase()))
						sender.sendMessage("Pond overridden.");
					else
						sender.sendMessage("Pond created.");
					ponds.put(args[1].toUpperCase(), ((Player)sender).getLocation());

				}
				if(args[0].equalsIgnoreCase("delete")&&sender.hasPermission("fish.admin")){
					if(ponds.containsKey(args[1].toUpperCase())){
						ponds.remove(args[1].toUpperCase());
						sender.sendMessage("Pond removed.");
					}else{
						sender.sendMessage("Pond not found.  Use /fish list");
					}
				}

			}
			if(args.length == 3){
				if(args[0].equalsIgnoreCase("addrod")&&sender.hasPermission("fish.admin")){
					String rod = fromInt(Integer.parseInt(args[2])) + args[1].replaceAll("_", " ");
					sender.sendMessage("Added rod: " + rod);
					possibleRods.add(rod);
				}
				if(args[0].equalsIgnoreCase("delrod")&&sender.hasPermission("fish.admin")){
					String format = fromInt(Integer.parseInt(args[2])) + args[1].replaceAll("_", " ");
					if(contains(possibleRods, format)!=-1){
						possibleRods.remove(contains(possibleRods, format));
						sender.sendMessage("Deleted");
					}else{
						sender.sendMessage("Not Found");
					}
				}
			}
			
			
		}
		
		return true;
	}
	
	public int contains(ArrayList<String> arr, String val){
		
		for(int i = 0; i < arr.size(); i++){
			if(val.equals(arr.get(i)))
				return i;
		}
		
		return -1;
		
	}
	
	@EventHandler
	public void onPlayerItemDrop(PlayerDropItemEvent e){
		if(e.getItemDrop().getItemStack().getType()==Material.FISHING_ROD && atPond.get(e.getPlayer()))
			e.setCancelled(true);
	}
	
	public int contains(Player p, String rod){
		
		for(int i = 0; i < rods.get(p).size(); i++){
			if(tierless(rod).equals(tierless(rods.get(p).get(i))))
				return i;
		}
		
		return -1;
	}
	
	public void openInv(Player p, int page){
		Inventory inv = Bukkit.createInventory(null, 54, "Rods - Page " + page);
		List<String> owned = rods.get(p);
		for(int i = page*36; i<Math.min(owned.size(), (page+1)*36); i++){
			ItemStack is = new ItemStack(Material.FISHING_ROD);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(owned.get(i));
			im.spigot().setUnbreakable(true);
			is.setItemMeta(im);
			int level = fromColor(ChatColor.getByChar(owned.get(i).charAt(1)))-1;
			if(level!=0)
				is.addUnsafeEnchantment(Enchantment.LURE, level);
			if(level!=0)
				is.addUnsafeEnchantment(Enchantment.LUCK, level);
			int tier = getTier(owned.get(i));
			if(tier!=0)
				is.addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, tier);
			
			
			inv.addItem(is);
		}
		if(page > 0)
			inv.setItem(45, new ItemStack(Material.ARROW));
		if(page * 36 + 36 < owned.size())
			inv.setItem(53, new ItemStack(Material.ARROW));
		p.openInventory(inv);
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e){
		if(e.getInventory().getName().contains("Rods - Page ")){
			e.setCancelled(true);
			if(e.getCurrentItem() != null){
				if(e.getCurrentItem().getType() == Material.ARROW){
					if(e.getRawSlot() == 53){
						openInv((Player)e.getWhoClicked(), Integer.parseInt(e.getInventory().getName().substring(12))+1);
					}else{
						openInv((Player)e.getWhoClicked(), Integer.parseInt(e.getInventory().getName().substring(12))-1);
					}
				}else if (e.getCurrentItem().getType()==Material.FISHING_ROD){
					e.getWhoClicked().getInventory().remove(Material.FISHING_ROD);
					e.getWhoClicked().getInventory().addItem(e.getCurrentItem());
					
					e.getWhoClicked().closeInventory();
				}
			}
		}
	}
	
	public void save(){
		
		for(Player p : getServer().getOnlinePlayers()){
			savePlayer(p);
		}
		getConfig().set("ponds", null);

		for(String s : ponds.keySet()){
	    	ConfigurationSection cs = getConfig().getConfigurationSection("ponds." + s);
	    	if(cs == null){
	    		getConfig().createSection("ponds." + s);
	    		cs = getConfig().getConfigurationSection("ponds." + s);
	    	}
	    	cs.set("x", ponds.get(s).getBlockX());
	    	cs.set("y", ponds.get(s).getBlockY());
	    	cs.set("z", ponds.get(s).getBlockZ());
	    	cs.set("world", ponds.get(s).getWorld().getName());
		}
		getConfig().set("rods", possibleRods);
		
		getConfig().set("spawn.x", spawn.getBlockX());
		getConfig().set("spawn.y", spawn.getBlockY());
		getConfig().set("spawn.z", spawn.getBlockZ());
		getConfig().set("spawn.world", spawn.getWorld().getName());
		
		saveConfig();
		
	}
	
	public void loadPlayer(Player p){
		if(getConfig().get("players." + p.getUniqueId()) != null){
			rods.put(p, getConfig().getStringList("players." + p.getUniqueId().toString() + ".rods"));
			fishCaught.put(p, getConfig().getInt("players." + p.getUniqueId().toString() + ".fish"));
		}else{
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(ChatColor.DARK_GRAY + "Broken Fishing Rod");
			rods.put(p, temp);
			fishCaught.put(p, 0);
			
		}
		atPond.put(p, false);
	}
	
	public void savePlayer(Player p){
		
		getConfig().set("players."+p.getUniqueId().toString() + ".rods", rods.get(p));
		getConfig().set("players."+p.getUniqueId().toString() + ".fish", fishCaught.get(p));
		saveConfig();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		savePlayer(e.getPlayer());
		if(atPond.get(e.getPlayer())){
			e.getPlayer().getInventory().remove(Material.FISHING_ROD);
			e.getPlayer().teleport(spawn);
		}
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		loadPlayer(e.getPlayer());
	}
	 
	public String tierless(String rod){
		if(rod.contains("*"))
			return rod.substring(0, rod.indexOf('*')-1);
		return rod;
	}
	
	public void updateRod(String rod, Player p){
		String tierless = tierless(rod);
	}
	
	@EventHandler
	public void onFishCaught(PlayerFishEvent e){
		Random r = new Random();
		if(e.getState() == PlayerFishEvent.State.CAUGHT_FISH&&atPond.get(e.getPlayer())){
			int fish = fishingRod(e.getPlayer().getItemInHand());
			((Item)e.getCaught()).setPickupDelay(41);
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){public void run(){e.getCaught().remove();}},40L);
			if(fish == 0){
				int fishCaughtt = 1;
				int bonusChance = 0;
				if(e.getPlayer().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS))
					bonusChance = e.getPlayer().getItemInHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)*25;
				while(bonusChance > 100){
					fishCaughtt++;
					bonusChance -= 100;
				}
				if(r.nextInt(100) < bonusChance)
					fishCaughtt++;
				fishCaught.put(e.getPlayer(), fishCaught.get(e.getPlayer())+fishCaughtt);
				e.getPlayer().sendMessage(ChatColor.AQUA + fishMessage[r.nextInt(fishMessage.length)].replaceAll("%fish%", this.fish[r.nextInt(this.fish.length)]));
				if(fishCaughtt > 1)
					e.getPlayer().sendMessage(ChatColor.AQUA + "Recieved " + ChatColor.GOLD + (fishCaughtt-1) + ChatColor.AQUA + " bonus fish!");
				((Item)e.getCaught()).setItemStack(new ItemStack(Material.RAW_FISH));
			}else{
				String rod = randomRod(fromInt(fish));
				int tier = 0;
				while(r.nextInt(Math.max(15-tier,2)) == 0)
					tier++;
				if(tier > 0)
					rod += " *" + ChatColor.UNDERLINE + "" + tier + ChatColor.getByChar(rod.charAt(1))+"*";
				((Item)e.getCaught()).setItemStack(new ItemStack(Material.FISHING_ROD));
				e.getPlayer().sendMessage(ChatColor.AQUA + rodMessages[r.nextInt(rodMessages.length)].replaceAll("%rod%", rod+ChatColor.AQUA));
				if(contains(e.getPlayer(), rod)!=-1){
					int spot = contains(e.getPlayer(), rod);
					if(getTier(rods.get(e.getPlayer()).get(spot))>=tier){
						e.getPlayer().sendMessage(ChatColor.AQUA + "You already owned this fishing rod, and were awarded "+ ChatColor.GOLD + "5 fish");
						fishCaught.put(e.getPlayer(), fishCaught.get(e.getPlayer())+5);
					}else{
						rods.get(e.getPlayer()).set(spot, rod);
						e.getPlayer().sendMessage(ChatColor.AQUA + "You owned a lower tier of this rod and it has been upgraded.");
						Collections.sort(rods.get(e.getPlayer()));
					}
				}else{
					rods.get(e.getPlayer()).add(rod);
					Collections.sort(rods.get(e.getPlayer()));
				}
			}
		}
	}
	
	public int getTier(String rod){
		int loc = ChatColor.stripColor(rod).indexOf('*');
		System.out.println(loc);
		if(loc != -1){
			int end = ChatColor.stripColor(rod).substring(loc + 1).indexOf('*');
			System.out.println(end);
			return Integer.parseInt(ChatColor.stripColor(rod).substring(loc+1,end+loc+1));
		}
		return 0;
	}
	
	public int fishingRod(ItemStack i){
		Random r = new Random();
		int chance = 1;
		if(i.containsEnchantment(Enchantment.LUCK)){
			chance = i.getEnchantmentLevel(Enchantment.LUCK)+1;
		}
		
		if(r.nextInt(100)<=chance){
			int rare = r.nextInt(100);
			if(rare < 4){
				return 6;
			}
			if(rare < 13){
				return 5;
			}
			if(rare < 27){
				return 4;
			}
			if(rare < 46){
				return 3;
			}
			if(rare < 70){
				return 2;
			}
			return 1;
		}
		
		
		return 0;
	}
	
	public int fromColor(ChatColor c){
		switch(c){
		case DARK_GRAY:
			return 1;
		case BLUE:
			return 2;
		case GREEN:
			return 3;
		case DARK_PURPLE:
			return 4;
		case GOLD:
			return 5;
		case DARK_RED:
			return 6;
		default:
			return 0;
		}
	}
	
	@EventHandler
	public void onCommandPreload(PlayerCommandPreprocessEvent e){
		if(atPond.get(e.getPlayer())&&!e.getMessage().split(" ")[0].substring(1).equalsIgnoreCase("fish")&&!e.getPlayer().hasPermission("fish.override")){
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Please use only fishing commands while at a pond.  Use /fish leave to go back to spawn.");
		}
		if(e.getMessage().length() > 5 && e.getMessage().substring(1,5).equalsIgnoreCase("fish")&&!atPond.get(e.getPlayer())&&!(e.getMessage().equalsIgnoreCase("/fish go")||e.getMessage().equalsIgnoreCase("/fish stats"))&&!e.getPlayer().hasPermission("fish.admin")){
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "Please go to a pond before using these commands!");
		}
	}
	
	public ChatColor fromInt(int i){
		switch(i){
		case 1:
			return ChatColor.DARK_GRAY;
		case 2:
			return ChatColor.BLUE;
		case 3:
			return ChatColor.GREEN;
		case 4:
			return ChatColor.DARK_PURPLE;
		case 5:
			return ChatColor.GOLD;
		case 6:
			return ChatColor.DARK_RED;
		default:
			return ChatColor.DARK_GRAY;
		}
		
	}
	
	public String randomRod(ChatColor rarity){
		ArrayList<String> temp = new ArrayList<String>();
		for(int i = 0; i < possibleRods.size(); i++){
			if(ChatColor.getByChar(possibleRods.get(i).substring(1, 2))==rarity)
				temp.add(possibleRods.get(i));
		}
		return temp.get(new Random().nextInt(temp.size()));
	}
	
}
