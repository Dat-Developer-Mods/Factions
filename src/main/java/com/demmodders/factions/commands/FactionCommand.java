package com.demmodders.factions.commands;

import com.demmodders.factions.Factions;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FactionCommand extends CommandBase {
    @Override
    public String getName() {
        return "faction";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {

        }
        return "Only a player can use these commands";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Factions.LOGGER.info(Arrays.toString(args));
        if(!(sender instanceof EntityPlayerMP)) return;

        int commandResult = 0;
        UUID playerID = ((EntityPlayerMP)sender).getUniqueID();
        UUID factionID = FactionManager.getInstance().getPlayersFactionID(playerID);

        if(args.length == 0){
            sender.sendMessage(new TextComponentString(getUsage(sender)));
        } else {
            switch (args[0].toLowerCase()) {
                // Global
                case "list":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        //ToDo: send list of factions, in pages
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "info":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // No Faction
                case "join":
                case "invites":
                case "reject":

                // Faction member
                case "home":
                case "leave":
                case "motd":

                // Faction Officer
                case "ally":
                case "enemy":
                case "neutral":
                case "claim":
                case "sethome":
                case "kick":
                case "invite":
                case "setmotd":
                    
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("f");
        return aliases;
    }
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
