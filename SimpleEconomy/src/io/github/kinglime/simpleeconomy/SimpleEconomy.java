package io.github.kinglime.simpleeconomy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleEconomy extends JavaPlugin implements Listener {
	
	public HashMap <String, Double> accounts;
	public char currencyType;
	
	public HashMap<String, Double> readMap() throws IOException {
		HashMap <String, Double> readAccounts = new HashMap <String, Double>();
		Scanner fileIn = new Scanner(new File(this.getDataFolder(), "accounts.dat"));
		while (fileIn.hasNextLine()) {
			String[] splitted = fileIn.nextLine().split(" ");
			readAccounts.put(splitted[0], Double.parseDouble(splitted[1]));
		}
		fileIn.close();
		return readAccounts;
	}
	
	public void saveMap() throws IOException {
		PrintWriter fileOut = new PrintWriter(new FileWriter(this.getDataFolder() + "\\accounts.dat"));
		for (Map.Entry<String, Double> entry : accounts.entrySet()) {
			fileOut.println(entry.getKey() + " " + entry.getValue());
		}
		fileOut.close();
	}
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		File config = new File(this.getDataFolder(), "config.yml");
		if (!config.exists()) {
		    this.saveDefaultConfig();
		}
		File data = new File(this.getDataFolder(), "accounts.dat");
		if (!data.exists()) {
			try {
				data.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			accounts = readMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(Player player: getServer().getOnlinePlayers()) {
			if (!accounts.containsKey(player.getName())) {
				accounts.put(player.getName(), getConfig().getDouble("SimpleEconomy.StartAmount"));
			}
		}
		currencyType = getConfig().getString("SimpleEconomy.CurrencyChar").charAt(0);
	}
	
	public void onDisable() {
		try {
			saveMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!accounts.containsKey(player.getName())) {
			accounts.put(player.getName(), getConfig().getDouble("SimpleEconomy.StartAmount"));
		}
	}
	
	public Player guessPlayer(String name) {
		List<Player> player = Bukkit.getServer().matchPlayer(name);
		int size = player.size();
		if (size == 1) {
			return player.get(0);
		} else {
			return null;
		}
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("money")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return false;
			} else {
				Player player = (Player) sender;
				player.sendMessage("Balance: " + currencyType + getBalance(player));
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("pay")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return false;
			} else {
				Player player = (Player) sender;
				if (args.length == 2) {
					String payTo = args[0];
					double amount;
					try {
						amount = Double.parseDouble(args[1]);
					}
					catch (NumberFormatException e) {
						player.sendMessage("The amount must be a valid number");
						return false;
					}
					if (amount > 0 && amount <= getBalance(player)) {
						Player playerToSendTo = guessPlayer(payTo);
						if (playerToSendTo != null) {
							if (playerToSendTo.getName().equals(player.getName())) {
								player.sendMessage("You cannot send money to your self.");
								return false;
							} else {
								if ((changeBalance(true, amount, playerToSendTo) == true) && (changeBalance(false, amount, player))) {
									player.sendMessage("Sent " + currencyType + amount + " to " + playerToSendTo.getName() + ".");
									playerToSendTo.sendMessage(player.getName() + " sent you " + currencyType + amount + ".");
									return true;
								} else {
									player.sendMessage("Error sending money.");
									return false;
								}
								}
						} else {
							player.sendMessage("Can't find player " + payTo + ".");
							return false;
						}
					} else {
						player.sendMessage("Insufficient funds.");
						return false;
					}
				} else {
					sender.sendMessage("Not enough arguments");
					return false;
				}
			}
		}
		return false;
	}
	
	public double getBalance(Player player) {
		double balance = accounts.get(player.getName());
		return balance;
	}
	
	public boolean changeBalance(boolean positive, double amount, Player player) {
		if (accounts.get(player.getName()) != null) {
			double balance = accounts.get(player.getName());
				double newBalance = 0;
				if (positive == true) {
					newBalance = balance + amount;
				} else if (positive == false) {
					newBalance = balance - amount;
				}
				if (newBalance >= 0) {
					accounts.put(player.getName(), newBalance);
					return true;
				} else {
					return false;
				}
		} else {
			return false;
		}
	}
}
