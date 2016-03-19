package mr.wruczek.supercensor3.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import mr.wruczek.supercensor3.utils.SCLogger;
import mr.wruczek.supercensor3.utils.SCPermissionsEnum;

/**
 * This work is licensed under a Creative Commons Attribution-NoDerivatives 4.0
 * International License. http://creativecommons.org/licenses/by-nd/4.0/
 *
 * @author Wruczek
 */
public class SCReportCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!SCPermissionsEnum.BASICADMIN.hasPermission(sender)) {
			sender.sendMessage("[SC] You need permission \"supercensor\" to use this command.");
			return false;
		}
		
		if (SCLogger.lastError == null || SCLogger.lastError.isEmpty()) {
			sender.sendMessage("[SC] No last error found. Nothing to report.");
			return false;
		}

		StringBuilder sb = new StringBuilder();

		for (String str : SCLogger.lastError) {
			sb.append(str + "\n");
		}

		try {
			sender.sendMessage("[SC] Link to report: " + hastebinPost(sb.toString()));
			sender.sendMessage("Please use this link when creating new issue.");
		} catch (IOException e) {
			sender.sendMessage("[SC] Cannot send error report! " + e);
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * Simple API for uploading data to hastebin.com<br>
	 * (c) 2016 Wruczek<br>
	 * MIT license
	 * 
	 * @param data Message body
	 * @return URL to paste
	 */
	public static URL hastebinPost(String data) throws IOException {
		URL url = new URL("http://hastebin.com/documents");

		byte[] postData = data.getBytes("UTF-8");

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postData);

		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

		StringBuilder sb = new StringBuilder();
		for (int c; (c = in.read()) >= 0;)
			sb.append((char) c);

		String response = sb.toString();
		String id = response.trim().substring(8, response.length() - 2); // You can also use JSON library and get "key" from response

		return new URL("http://hastebin.com/" + id);
	}
	
}