package io.apexcreations;

import org.bukkit.command.CommandSender;

public abstract class ApexSubCommand {

    private final String name, info, permission;
    private final boolean playerOnly;
    private final String[] aliases;

    public ApexSubCommand(String name, String info, boolean playerOnly) {
        this(name, info, "", playerOnly);
    }

    public ApexSubCommand(String name, String info, String permission, boolean playerOnly, String... aliases) {
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
}
