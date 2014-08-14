 /************************************************************************
 *  LiveBroadcast - A simple broadcast plugin for bukkit
 *  Copyright (C) 2014 James Wolff
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/

package io.github.jwolff52.livebroadcast;

import io.github.jwolff52.livebroadcast.util.SettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public final class LiveBroadcast extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");

	public static LiveBroadcast plugin;

	public static SettingsManager sm;

	private static PluginDescriptionFile pdf;

	private String state, broadcastTitle;

	private boolean toggle = true;

	private int timer, configNumber = 1, maxMessages = 0;

	@Override
	public void onEnable() {
		pdf = getDescription();
		sm = SettingsManager.getInstance();
		sm.setup(this);

		if (!(new File(getDataFolder(), "README.txt").exists())) {
			InputStream is = LiveBroadcast.class
					.getResourceAsStream("/README.md");
			OutputStream os;
			int readBytes;
			byte[] buffer = new byte[4096];
			try {
				os = new FileOutputStream(new File(getDataFolder() + "/README.txt"));
				while ((readBytes = is.read(buffer)) > 0) {
					os.write(buffer, 0, readBytes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		while (true) {
			if (sm.getConfig().getString(configNumber + "") != null) {
				configNumber++;
				maxMessages++;
			} else {
				configNumber = 1;
				break;
			}
		}

		setBroadcastTitle(parseColors(sm.getConfig().getString("title")));
		timer = getConfig().getInt("timer") * 20;
		/*************************************************************/
		// Scheduler that prints the messages every 'timer' seconds
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
					
			@Override
			public void run() {
				if (sm.getConfig().getString(configNumber + "") != null) {
					if (!sm.getConfig().getString(configNumber + "").equalsIgnoreCase("empty")) {
						if (getToggle()){
							Bukkit.broadcastMessage(broadcastTitle + parseColors(sm.getConfig().getString(configNumber + "")));
						}
					}
					configNumber++;
				} else {
					configNumber = 1;
				}
			}
		}, 0L, timer);
		/*************************************************************/
		this.logger.info(pdf.getName() + " Version: " + pdf.getVersion()
				+ " has been enabled!");
		super.onEnable();
	}

	@Override
	public void onDisable() {
		pdf = getDescription();
		this.logger.info(pdf.getName() + " has been Disabled!");
		super.onDisable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equals("lbtoggle")){
			if (!sender.hasPermission("lb.toggle")) {
				sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			} else {
				toggle(sender);
				return true;
			}
		}else if(cmd.getName().equals("lbcredits")){
			if (!sender.hasPermission("lb.credits")) {
				sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			} else {
				credits(sender);
				return true;
			}
		}else if(cmd.getName().equals("lbadd")){
			if(!sender.hasPermission("lb.config.add")) {
				sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			}else{
				if(args.length<1){
					sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"Usage: /lbadd <message>");
					return false;
				}
				add(sender, args);
				return true;
			}
		}else if(cmd.getName().equals("lbdel")){
			if(!sender.hasPermission("lb.config.del")) {
				sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			}else{
				if(args.length<1){
					sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"Usage: /lbdel <message_number>");
					return false;
				}
				try{
					del(sender, args);
					return true;
				}catch(NumberFormatException e){
					sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"Usage: /lbdel <message_number>");
					return false;
				}
			}
		}else if(cmd.getName().equals("lblist")){
			if(!sender.hasPermission("lb.config.list")) {
				sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			}else{
				if(args.length<1){
					list(sender, "1");
					return true;
				}
				try{
					list(sender, args[0]);
					return true;
				}catch(NumberFormatException e){
					sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"Usage: /lblist [page]");
					return false;
				}
			}
		}else if(cmd.getName().equals("lbreload")){
			if (!sender.hasPermission("lb.reload")) {
				sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			} else {
				reload(sender);
				return true;
			}
		}else if(cmd.getName().equals("lbbroadcast")){
			if (!sender.hasPermission("lb.broadcast")) {
				return false;
			} else {
				if (args.length == 0) {
					sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.AQUA+"I thought you wanted to say something?\nUsage: /lbbroadcast <word> [word2] [word3]...");
					return false;
				} else if (args.length >= 1) {
					broadcast(args);
					return true;
				}
			}
		}
		return false;
	}

	private void toggle(CommandSender sender) {
		if (getToggle()) {
			setToggle(false);
			state = "off";
		} else if (!getToggle()) {
			setToggle(true);
			state = "on";
		}
		sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"LiveBroacast was turned " + state);
	}

	private void credits(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "\n======================" + ChatColor.BLUE + "\nName: LiveBroadcast\nVersion: " + pdf.getVersion() + "\nDeveloper: jwolff52" + ChatColor.GOLD + "\n======================");
	}

	private void add(CommandSender sender, String[] args) {
		String message = "";
		for (int x = 0; x < args.length - 1; x++) {
			message += args[x] + " ";
		}
		message += args[args.length - 1];
		sm.getConfig().set((maxMessages + 1) + "", message);
		message = parseColors(message);
		maxMessages++;
		sm.saveConfig();
		sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"Message: \"" + ChatColor.RESET + sm.getConfig().getString(maxMessages + "") + ChatColor.WHITE + "\" was added to the  list!");
	}

	private void del(CommandSender sender, String[] args) throws NumberFormatException{
		int cNumber=Integer.valueOf(args[0]);
		String message = getConfig().getString(args[0]);
		for(int x=1;x<cNumber;x++){
			sm.getConfig().set(x + "", sm.getConfig().getString(x + ""));
		}
		for(int x=cNumber;x<maxMessages;x++){
			sm.getConfig().set(x + "", sm.getConfig().getString((x + 1) + ""));
		}
		sm.getConfig().set(maxMessages + "", null);
		maxMessages--;
		sm.saveConfig();
		sender.sendMessage(parseColors(sm.getConfig().getString("title"))+"Message: \"" + ChatColor.RESET + message + ChatColor.WHITE + "\" was removed from the list!");
	}

	private void list(CommandSender sender, String page) throws NumberFormatException{
		if(sender instanceof Player){
			int intPage=Integer.valueOf(page)-1;
			String temp = ChatColor.DARK_BLUE + "=== LiveBroadcast ==Page " + (intPage + 1) +"/" + ((maxMessages/10) + 1) + "================";
			for (int x = (intPage*10)%maxMessages; x < (intPage*10)+10 && x < maxMessages; x++) {
				temp += "\n" + ChatColor.GREEN + "[" + (x + 1) + "] " + ChatColor.RESET + parseColors(sm.getConfig().getString(x + 1 + ""));
			}
			sender.sendMessage(temp);
		}else{
			String temp = ChatColor.DARK_BLUE + "=== LiveBroadcast =========================";
			for (int x = 0; x < maxMessages; x++) {
				temp += "\n" + ChatColor.GREEN + "[" + (x + 1) + "] " + ChatColor.RESET + parseColors(sm.getConfig().getString(x + 1 + ""));
			}
			sender.sendMessage(temp);
		}
	}

	private void reload(CommandSender sender) {
		setToggle(false);
		sm.reloadConfig();
		int tempConfigNumber=1;
		int tempMaxMessages=0;
		while (true) {
			if (sm.getConfig().getString(tempConfigNumber + "") != null) {
				tempConfigNumber++;
				tempMaxMessages++;
			} else {
				break;
			}
		}
		maxMessages=tempMaxMessages;
		setBroadcastTitle(parseColors(sm.getConfig().getString("title")));
		timer = getConfig().getInt("timer") * 20;
		sender.sendMessage(parseColors(sm.getConfig().getString("title"))+ChatColor.AQUA + "LiveBroacast configuration successfully reloaded!!");
		setToggle(true);
	}
	
	private void broadcast(String[] args){
		String message = parseColors(sm.getConfig().getString("title"));
		for (String subMessage : args) {
			message += subMessage + " ";
		}
		Bukkit.broadcastMessage(message);
	}

	public String parseColors(String temp) {
		return ChatColor.translateAlternateColorCodes('&', temp);
	}

	public boolean getToggle() {
		return toggle;
	}

	public void setToggle(boolean toggle) {
		this.toggle = toggle;
	}

	public String getBroadcastTitle() {
		return broadcastTitle;
	}

	public void setBroadcastTitle(String broadcastTitle) {
		this.broadcastTitle = broadcastTitle;
	}
}
