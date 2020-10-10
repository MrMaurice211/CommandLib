package me.mrmaurice.cl;

import java.util.*;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public abstract class LibCommand extends Command {

	private final Map<String, LibSubCommand> subCommandCache = new HashMap<>();
	private final boolean playerOnly;

	public LibCommand(String name) {
		this(name, false);
	}

	public LibCommand(String name, boolean playerOnly) {
		this(name, "Unknown Description", "", playerOnly);
	}

	public LibCommand(String name, String description, String permission, boolean playerOnly, String... aliases) {
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
			Optional<LibSubCommand> optionalSubCommand = this.getSubCommand(args[0]);

			if (optionalSubCommand.isPresent()) {
				LibSubCommand subCommand = optionalSubCommand.get();

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
			Optional<LibSubCommand> optionalSubCommand = this.getSubCommand(args[0]);

			if (optionalSubCommand.isPresent()) {
				LibSubCommand subCommand = optionalSubCommand.get();

				String permission = subCommand.getPermission();
				if (!permission.isEmpty() && !sender.hasPermission(permission))
					return Lists.newArrayList();

				return subCommand.onTabComplete(sender, args);
			}
		}
		return Lists.newArrayList();
	}

	public abstract boolean executeCommand(CommandSender commandSender, String label, String[] args);

	private Optional<LibSubCommand> getSubCommand(String key) {
		String lowKey = key.toLowerCase();
		Optional<LibSubCommand> optionalKey = Optional.ofNullable(getSubCommandCache().get(lowKey));
		if (!optionalKey.isPresent()) {
			return getSubCommandCache().values().stream()
					.filter(value -> Arrays.stream(value.getAliases()).anyMatch(s -> s.equalsIgnoreCase(lowKey)))
					.findFirst().map(Optional::of).orElse(optionalKey);
		}
		return optionalKey;
	}

	public void registerSubCommand(LibSubCommand... libSubCommands) {
		for (LibSubCommand libSubCommand : libSubCommands) {
			getSubCommandCache().put(libSubCommand.getName().toLowerCase(), libSubCommand);
		}
	}

	public boolean isSubCommand(String key) {
		return getSubCommandCache().containsKey(key);
	}

	public Map<String, LibSubCommand> getSubCommandCache() {
		return this.subCommandCache;
	}
}