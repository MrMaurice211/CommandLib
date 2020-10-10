package io.apexcreations;

import java.util.*;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public abstract class ApexCommand extends Command {

	private final Map<String, ApexSubCommand> subCommandCache = new HashMap<>();
	private final boolean playerOnly;

	public ApexCommand(String name) {
		this(name, false);
	}

	public ApexCommand(String name, boolean playerOnly) {
		this(name, "Unknown Description", "", playerOnly);
	}

	public ApexCommand(String name, String description, String permission, boolean playerOnly, String... aliases) {
		super(name);
		this.setDescription(description);
		this.setPermission(permission);
		this.playerOnly = playerOnly;
		this.setAliases(Arrays.asList(aliases));
		this.setPermissionMessage(ChatColor.RED + "You do not have permission for this command!");
	}

	@Override
	public boolean execute(CommandSender commandSender, String label, String[] args) {

		boolean playerInstance = commandSender instanceof Player;

		if (!playerInstance && this.playerOnly) {
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPermissionMessage()));
			return false;
		}

		if (!getPermission().isEmpty() && !commandSender.hasPermission(getPermission())) {
			commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPermissionMessage()));
			return true;
		}

		if (args.length > 0) {
			Optional<ApexSubCommand> optionalSubCommand = this.getSubCommand(args[0]);

			if (optionalSubCommand.isPresent()) {
				ApexSubCommand subCommand = optionalSubCommand.get();

				String permission = subCommand.getPermission();
				if (!permission.isEmpty() && !commandSender.hasPermission(permission)) {
					commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPermissionMessage()));
					return false;
				}
				subCommand.execute(commandSender,
						args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length));
				return false;
			}

		}
		return executeCommand(commandSender, label, args);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		if(args.length == 1)
			return StringUtil.copyPartialMatches(args[0], getSubCommandCache().keySet(), Lists.newArrayList());
		if(args.length > 1) {
			Optional<ApexSubCommand> optionalSubCommand = this.getSubCommand(args[0]);

			if (optionalSubCommand.isPresent()) {
				ApexSubCommand subCommand = optionalSubCommand.get();

				String permission = subCommand.getPermission();
				if (!permission.isEmpty() && !sender.hasPermission(permission))
					return Lists.newArrayList();

				return subCommand.onTabComplete(sender, args);
			}
		}
		return Lists.newArrayList();
	}

	public abstract boolean executeCommand(CommandSender commandSender, String label, String[] args);

	private Optional<ApexSubCommand> getSubCommand(String key) {
		String lowKey = key.toLowerCase();
		Optional<ApexSubCommand> optionalKey = Optional.ofNullable(getSubCommandCache().get(lowKey));
		if (!optionalKey.isPresent()) {
			return getSubCommandCache().values().stream()
					.filter(value -> Arrays.stream(value.getAliases()).anyMatch(s -> s.equalsIgnoreCase(lowKey)))
					.findFirst().map(Optional::of).orElse(optionalKey);
		}
		return optionalKey;
	}

	public void registerSubCommand(ApexSubCommand... apexSubCommands) {
		for (ApexSubCommand apexSubCommand : apexSubCommands) {
			getSubCommandCache().put(apexSubCommand.getName().toLowerCase(), apexSubCommand);
		}
	}

	public boolean isSubCommand(String key) {
		return getSubCommandCache().containsKey(key);
	}

	public Map<String, ApexSubCommand> getSubCommandCache() {
		return this.subCommandCache;
	}
}