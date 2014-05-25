package net.communitycraft.permissions.commands.general;

import net.cogzmc.core.Core;
import net.cogzmc.core.modular.command.ArgumentRequirementException;
import net.cogzmc.core.modular.command.CommandException;
import net.cogzmc.core.modular.command.EmptyHandlerException;
import net.cogzmc.core.modular.command.ModuleCommand;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.CPermissible;
import net.cogzmc.core.player.CPlayer;
import net.cogzmc.core.player.DatabaseConnectException;
import net.communitycraft.permissions.PermissionsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class PermissibleSubCommand<PermissibleType extends CPermissible> extends ModuleCommand {
    protected PermissibleSubCommand(String name) {
        super(name);
    }

    @Override
    protected final void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        //Check argument lengths
        if (args.length < 1 + (needsSecondArgument() ? 1 : 0)) throw new ArgumentRequirementException("You must specify enough arguments for this command!");
        //Get the permissible
        PermissibleType permissible = getPermissible(args[0]);
        if (permissible == null) throw new ArgumentRequirementException("The argument you specified is not a valid " + getNameOfPermissibleType());
        boolean tookControlOfMessage = false;
        if (needsSecondArgument()) {
            String arg = args[1];
            if (!validateArgument(arg)) throw new ArgumentRequirementException("The argument you passed is not valid!");
            try {
                doAction(permissible, arg);
            } catch (EmptyHandlerException e) {
                doAction(permissible, arg, sender);
                tookControlOfMessage = true;
            }
            if (!tookControlOfMessage) sender.sendMessage(getSuccessMessage(permissible, arg));
        } else {
            try {
                doAction(permissible);
            } catch (EmptyHandlerException e) {
                doAction(permissible, sender);
                tookControlOfMessage = true;
            }
            if (!tookControlOfMessage) sender.sendMessage(getSuccessMessage(permissible));
        }
        if (shouldReload()) {
            if (permissible instanceof COfflinePlayer && !(permissible instanceof CPlayer)) try {
                Core.getPlayerManager().savePlayerData((COfflinePlayer) permissible);
            } catch (DatabaseConnectException e) {
                PermissionsManager.getInstance().logMessage("&cCould not save " + permissible.getName() + " to the database during command " + getFormattedName() + "!");
            }
            Core.getPermissionsManager().reloadPermissions();
        }
    }

    @Override
    protected List<String> handleTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return super.handleTabComplete(sender, command, alias, args);
        return getComplete(args[0]);
    }

    protected abstract PermissibleType getPermissible(String name);
    protected abstract String getNameOfPermissibleType();
    protected abstract boolean needsSecondArgument();
    protected abstract List<String> getComplete(String arg);

    protected void doAction(PermissibleType permissible, String argument) throws CommandException {throw new EmptyHandlerException();}
    protected void doAction(PermissibleType permissible) throws CommandException {throw new EmptyHandlerException();}

    protected void doAction(PermissibleType permissible, String argument, CommandSender sender) throws CommandException {throw new EmptyHandlerException();}
    protected void doAction(PermissibleType permissible, CommandSender sender) throws CommandException {throw new EmptyHandlerException();}

    protected boolean validateArgument(String argument) {return true;}
    protected String getSuccessMessage(PermissibleType target) {return PermissionsManager.getInstance().getFormat("success-command");}
    protected String getSuccessMessage(PermissibleType target, String argument) {return PermissionsManager.getInstance().getFormat("success-command");}

    protected boolean shouldReload() {return true;}
}
