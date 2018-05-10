package io.apexcreations;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public abstract class ApexSubCommand {
    private final String name, info, permission;
    private final boolean playerOnly;
    private final String[] aliases;
    private final Plugin plugin;

    public ApexSubCommand(Plugin plugin, String name, String info, boolean playerOnly) {
        this(plugin, name, info, "", playerOnly);
    }

    public ApexSubCommand(Plugin plugin, String name, String info, String permission, boolean playerOnly, String... aliases) {
        this.plugin = plugin;
        this.name = name;
        this.info = info;
        this.permission = permission;
        this.aliases = aliases;
        this.playerOnly = playerOnly;
    }

    public abstract void execute(CommandSender commandSender, String[] args);

    public boolean isPlayerOnly() {
        return this.playerOnly;
    }

    public String getName() {
        return this.name;
    }

    public String getInfo() {
        return this.info;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public String getPermission() {
        return this.permission;
    }

    protected Plugin getPlugin() {
        return this.plugin;
    }
}
