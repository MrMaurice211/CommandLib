package me.mrmaurice.cl;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Map;

public class CommandHandler {

    private CommandMap commandMap;

    private CommandHandler() {
        try {
            Server server = Bukkit.getServer();
            Field commandMap = server.getClass().getDeclaredField("commandMap");
            commandMap.setAccessible(true);
            this.commandMap = (CommandMap) commandMap.get(server);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void register(LibCommand... commands) {
        for (LibCommand libCommand : commands) {
            commandMap.register(libCommand.getName(), libCommand);
        }
    }

    public void unregister(String... commands) {
        for (String command : commands) {
            this.unregisterCommand(Bukkit.getPluginCommand(command));
        }
    }

    private void unregisterCommand(PluginCommand cmd) {
        try {
            Object map = getKnownCommands(this.commandMap);
            @SuppressWarnings("unchecked")
            Map<String, LibCommand> knownCommands = (Map<String, LibCommand>) map;
            knownCommands.remove(cmd.getName());
            for (String alias : cmd.getAliases()) {
                knownCommands.remove(alias);
            }
        } catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object getKnownCommands(Object object) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField("knownCommands");
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    public static <T extends Plugin> CommandHandler init() {
        return new CommandHandler();
    }

}
