package io.github.kinglime.simpleeconomy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleEconomy extends JavaPlugin {
	
	HashMap <String, Double> accounts = new HashMap <String, Double>();
	
	
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
		File config = new File(this.getDataFolder(), "config.yml");
		if (!config.exists()) {
		    this.saveDefaultConfig();
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
	}
	
	public void onDisable() {
		try {
			saveMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
