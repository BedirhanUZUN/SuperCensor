package mr.wruczek.supercensor3.PPUtils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import mr.wruczek.supercensor3.SCMain;
import mr.wruczek.supercensor3.data.SCPlayerDataManger;
import mr.wruczek.supercensor3.utils.SCLogger;
import mr.wruczek.supercensor3.utils.SCLogger.LogType;
import mr.wruczek.supercensor3.utils.SCUtils;

/**
 * This work is licensed under a Creative Commons Attribution-NoDerivatives 4.0
 * International License. http://creativecommons.org/licenses/by-nd/4.0/
 *
 * @author Wruczek
 */
public class PPCheck {

	public static void checkPlayer(final Player player, int newPoints, int oldPoints) {
		
		if (newPoints < oldPoints)
			return;

		configLoop: for (final ConfigurationSection cs : PPLoader.PPRules) {
			for (final String rule : cs.getKeys(false)) {

				// region Check for points and LatestPunishment
				if (cs.contains(rule + ".ActivateOn")) {
					if(cs.getInt(rule + ".ActivateOn") > newPoints)
						continue;
				}
				
				if(hasBeenPunished(player, rule))
					break;
				
				addPunishment(player, rule);
				// endregion
				
				// region Message player
				if (cs.contains(rule + ".MessagePlayer"))
					player.sendMessage(SCUtils.color(cs.getString(rule + ".MessagePlayer")
							.replace("%nick%", player.getDisplayName())));
				// endregion
				
				// region Run commands
				if (cs.contains(rule + ".RunCommands")) {
					// We want to sync it with Bukkit thread to avoid
					// java.lang.IllegalStateException and allow things like
					// kicking players
					Bukkit.getScheduler().scheduleSyncDelayedTask(SCMain.getInstance(), new Runnable() {
						@Override
						public void run() {
							for (String command : cs.getStringList(rule + ".RunCommands")) {
								try {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%nick%", player.getDisplayName()));
								} catch (Exception e) {
									SCUtils.logError("There was exception when executing command \"" + command 
											+ "\" on player \"" + player.getName(), LogType.PLUGIN);
								}
							}
						}
					});
				}
				// endregion
				
				// region Set PP
				if (cs.contains(rule + ".SetPenaltyPoints")) {
					PPManager.setPenaltyPoints(player, cs.getInt(rule + ".SetPenaltyPoints"));
				}
				// endregion
				
				// region Action Log
				if (cs.contains(rule + ".Log")) {
					SCUtils.logInfo(cs.getString(rule + ".Log")
							.replace("%date%", SCLogger.getDate())
							.replace("%time%", SCLogger.getTime())
							.replace("%nick%", player.getName()),
						LogType.CENSOR);
				}
				// endregion
				
				break configLoop;
			}
		}
	}
	
	public static boolean hasBeenPunished(OfflinePlayer player, String punishment) {
		for(String p : getPunishments(player))
			if(punishment.equalsIgnoreCase(p))
				return true;
		
		return false;
	}
	
	public static List<String> getPunishments(OfflinePlayer player) {
		SCPlayerDataManger pdm = new SCPlayerDataManger(player);
		return pdm.getConfig().getStringList("PushmentList");
	}
	
	public static void addPunishment(OfflinePlayer player, String punishmentName) {
		List<String> punishments = getPunishments(player);
		punishments.add(punishmentName);
		
		SCPlayerDataManger pdm = new SCPlayerDataManger(player);
		pdm.getConfig().set("PushmentList", punishments);
		pdm.saveConfig();
	}
	
	public static void clearPunishments(OfflinePlayer player) {
		SCPlayerDataManger pdm = new SCPlayerDataManger(player);
		pdm.getConfig().set("PushmentList", null);
		pdm.saveConfig();
	}

}