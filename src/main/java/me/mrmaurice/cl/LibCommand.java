package me.mrmaurice.cl;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public abstract class LibCommand extends Command {

    private final Map<String, LibCommand> subCommandCache = new HashMap<>();
    private final boolean playerOnly;

    public LibCommand(String name) {
        this(name, false);
    }

    public LibCommand(String name, boolean playerOnly) {
        this(name, "", playerOnly);
    }

    public LibCommand(String name, String description, boolean playerOnly) {
        this(name, description, "", playerOnly);
    }

    public LibCommand(String name, String description, String permission, boolean playerOnly, String... aliases) {
        super(name, description, "", Arrays.asList(aliases));
        this.playerOnly = playerOnly;
        this.setPermission(permission);
        this.setPermissionMessage(ChatColor.RED + "You do not have permission for this command!");
    }

    public abstract boolean executeCommand(CommandSender commandSender, String label, String[] args);

    public List<String> completeCommand(CommandSender sender, String alias, String[] args) {
        if (args.length == 0)
            return ImmutableList.of();

        String lastWord = args[args.length - 1];

        return Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .filter(name -> StringUtil.startsWithIgnoreCase(name, lastWord))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private Optional<LibCommand> getSubCommand(String key) {
        String lowKey = key.toLowerCase();
        Optional<LibCommand> optionalKey = Optional.ofNullable(getSubCommandCache().get(lowKey));

        if (!optionalKey.isPresent()) {
            return getSubCommandCache().values()
                    .stream()
                    .filter(value -> value.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(lowKey)))
                    .findFirst()
                    .map(Optional::of)
                    .orElse(optionalKey);
        }

        return optionalKey;
    }

    public final void registerSubCommand(LibCommand... libSubCommands) {
        for (LibCommand libSubCommand : libSubCommands)
            getSubCommandCache().put(libSubCommand.getName().toLowerCase(), libSubCommand);
    }

    public final boolean isSubCommand(String key) {
        return getSubCommandCache().containsKey(key);
    }

    public final Map<String, LibCommand> getSubCommandCache() {
        return this.subCommandCache;
    }

    @Override
    public final boolean execute(CommandSender commandSender, String label, String[] args) {

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
            Optional<LibCommand> optionalSubCommand = this.getSubCommand(args[0]);

            if (optionalSubCommand.isPresent()) {
                LibCommand subCommand = optionalSubCommand.get();

                return subCommand.execute(commandSender, label,
                        args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length));
            }

        }
        return executeCommand(commandSender, label, args);
    }

    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {

        List<String> allowed = subCommandCache.values()
                .stream()
                .filter(sub -> sub.hasPermission(sender))
                .map(LibCommand::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        if (args.length == 0)
            return allowed.isEmpty() ? completeCommand(sender, alias, args) : allowed;

        if (allowed.isEmpty())
            return completeCommand(sender, alias, args);

        String lastWord = args[0];
        Optional<LibCommand> optionalSubCommand = this.getSubCommand(lastWord);

        if (optionalSubCommand.isPresent()) {
            LibCommand subCommand = optionalSubCommand.get();

            if (!subCommand.hasPermission(sender))
                return completeCommand(sender, alias, args);

            return subCommand.tabComplete(sender, alias, Arrays.copyOfRange(args, 1, args.length));
        }

        return allowed;
    }

    public final boolean hasPermission(CommandSender sender) {
        String permission = getPermission();
        if (permission == null || permission.trim().isEmpty())
            return true;
        return sender.hasPermission(permission);
    }

}