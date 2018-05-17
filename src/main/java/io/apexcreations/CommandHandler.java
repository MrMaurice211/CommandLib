package io.apexcreations;

import java.lang.reflect.Field;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

public class CommandHandler<T extends Plugin> {

    private CommandMap commandMap;
    private T plugin;

    private CommandHandler(T plugin) {
        this.plugin = plugin;
        try {
            Field commandMap = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMap.setAccessible(true);
            this.commandMap = (CommandMap) commandMap.get(Bukkit.getServer());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void register(ApexCommand... commands) {
        for (ApexCommand apexCommand : commands) {
            commandMap.register(apexCommand.getName(), apexCommand);
        }
    }

    public void unregister(String... commands) {
        for (String command: commands) {
            this.unregisterCommand(Bukkit.getPluginCommand(command));
        }
    }

    private void unregisterCommand(PluginCommand cmd) {
        try {
            Object map = getPrivateField(this.commandMap, "knownCommands");
            @SuppressWarnings("unchecked")
            HashMap<String, ApexCommand> knownCommands = (HashMap<String, ApexCommand>) map;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                knownCommands.remove(alias);
            }
        } catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object getPrivateField(Object object, String field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    public static <T extends Plugin> CommandHandler<T> init(T plugin) {
        return new CommandHandler<>(plugin);
    }

    public T getPlugin() {
        return plugin;
    }
}
