package com.demmodders.factions.commands;

import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.Utils;
import com.demmodders.factions.util.enums.FactionChatMode;
import com.demmodders.factions.util.enums.FactionRank;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
            return "test";
        }
        return "Only a player can use these commands";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayerMP)) return;

        int commandResult = 0;
        FactionManager fMan = FactionManager.getInstance();
        UUID playerID = ((EntityPlayerMP)sender).getUniqueID();
        UUID factionID = fMan.getPlayersFactionID(playerID);
        String replyMessage = null;

        if(args.length == 0){
            sender.sendMessage(new TextComponentString(getUsage(sender)));
        } else {
            switch (args[0].toLowerCase()) {
                // Global
                case "list":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        try {
                            // Get which page to display
                            int page = ((args.length == 1) ? 1 : Integer.parseInt(args[1]));

                            List<UUID> factions = fMan.getListOfFactionsUUIDs();

                            if (factions.size() > 0) {

                                StringBuilder factionText = new StringBuilder();
                                // Header
                                factionText.append(TextFormatting.DARK_GREEN).append("Showing factions page ").append(page).append(" of ").append( (int)Math.ceil(factions.size() / 10f)).append("\n").append(TextFormatting.RESET);

                                // First faction, without comma
                                if (factions.get((page - 1) * 10) == factionID) factionText.append(TextFormatting.GREEN).append(fMan.getFaction(factions.get((page - 1) * 10)).name).append(TextFormatting.RESET);
                                else factionText.append(fMan.getFaction(factions.get((page - 1) * 10)).name);
                                for (int i = ((page - 1) * 10) + 1; i < factions.size() && i < ((10 * page)); i++) {
                                    // Highlight green if their own faction
                                    if (factions.get(i) == factionID) factionText.append(", ").append(TextFormatting.GREEN).append(fMan.getFaction(factions.get(i)).name).append(TextFormatting.RESET);
                                    else factionText.append(", ").append(fMan.getFaction(factions.get(i)).name);
                                }
                                replyMessage = factionText.toString();

                            } else {
                                replyMessage = TextFormatting.GOLD + "There are no factions";
                            }

                        } catch (NumberFormatException e){
                            commandResult = 2;
                        } catch (IndexOutOfBoundsException e){
                            replyMessage = TextFormatting.GOLD + "There aren't that many pages";
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                case "info":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        if (args.length == 1) {
                            if(factionID != null){
                                replyMessage = fMan.getFaction(factionID).printFactionInfo();
                            } else {
                                replyMessage = TextFormatting.GOLD + "You don't belong to a faction, you may only look up other factions";
                            }
                        } else {
                            replyMessage = fMan.getFaction(fMan.getFactionIDFromName(args[1].toLowerCase())).printFactionInfo();
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // No Faction
                case "join":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            commandResult = 2;
                        } else {
                            if (factionID == null) {
                                factionID = fMan.getFactionIDFromName(args[1].toLowerCase());
                                if (factionID != null) {
                                    if (fMan.canAddPlayerToFaction(playerID, factionID)){
                                        fMan.setPlayerFaction(playerID, factionID);
                                        replyMessage = TextFormatting.GOLD + "Successfully joined " + TextFormatting.DARK_GREEN + fMan.getFaction(factionID).name;
                                    } else {
                                        replyMessage = TextFormatting.GOLD + "You're not invited to that faction";
                                    }
                                } else {
                                    replyMessage = TextFormatting.GOLD + "That faction doesn't exist";
                                }
                            } else {
                                replyMessage = TextFormatting.GOLD + "You can't join a faction while you're a member of a different faction, leave your current one first";
                            }
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "invites":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {

                        ArrayList<UUID> invites = fMan.getPlayer(playerID).invites;
                        if (invites.size() > 0) {
                            int page = ((args.length == 1) ? 1 : Integer.parseInt(args[1]));
                            StringBuilder inviteText = new StringBuilder();
                            // Header
                            inviteText.append(TextFormatting.DARK_GREEN).append("Showing factions page ").append(page).append(" of ").append( (int)Math.ceil(invites.size() / 10f)).append("\n").append(TextFormatting.RESET);

                            // First faction, without comma
                            inviteText.append(fMan.getFaction(invites.get((page - 1) * 10)).name);
                            for (int i = ((page - 1) * 10) + 1; i < invites.size() && i < ((10 * page)); i++) {
                                inviteText.append(", ").append(fMan.getFaction(invites.get(i)).name);
                            }
                            replyMessage = inviteText.toString();

                        } else {
                            replyMessage = TextFormatting.GOLD + "You don't have any invites";
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "reject":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            commandResult = 2;
                        } else {
                            factionID = fMan.getFactionIDFromName(args[1].toLowerCase());
                            if (factionID != null) {
                                fMan.removePlayerInvite(playerID, factionID);
                            } else {
                                replyMessage = TextFormatting.GOLD + "You don't have an invite from that faction";
                            }
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "create":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.create")) {
                        if (args.length == 1) {
                            commandResult = 2;
                        } else {
                            if (fMan.getPlayer(playerID).faction == null){
                                int result = fMan.createFaction(args[1], playerID);
                                switch (result){
                                    case 0:
                                        replyMessage = TextFormatting.GOLD + "Faction " + args[1] + " successfully created, add a description with \"/faction desc\", and invite players with \"/faction invite <Player>\"";
                                        break;
                                    case 1:
                                        replyMessage = TextFormatting.GOLD + "Failed to create faction: name too long";
                                        break;
                                    case 2:
                                        replyMessage = TextFormatting.GOLD + "Failed to create faction: name too short";
                                        break;
                                    case 3:
                                        replyMessage = TextFormatting.GOLD + "Failed to create faction: a faction with that name already exists";
                                        break;
                                }
                            } else {
                                replyMessage = "You cannot create a faction while you're part of a faction";
                            }
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // Faction member
                case "home":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (factionID != null) {
                            if (fMan.getFaction(factionID).homePos != null) {
                                TeleportHandler.getInstance().addTeleportEvent((EntityPlayerMP) sender, fMan.getFaction(factionID).homePos, FactionConfig.playerSubCat.teleportDelay);
                                replyMessage = TextFormatting.GOLD + "Teleporting in " + FactionConfig.playerSubCat.teleportDelay + " Seconds";
                            } else {
                                replyMessage = TextFormatting.GOLD + "Your faction doesn't have a home";
                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "leave":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (factionID != null) {
                            if (fMan.getPlayer(playerID).factionRank == FactionRank.OWNER){
                                replyMessage = TextFormatting.GOLD + "You are the leader of this faction, you must disband it";
                            } else {
                                fMan.setPlayerFaction(playerID, null);
                                replyMessage = TextFormatting.GOLD + "You have successfully left your faction";
                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "motd":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (factionID != null) {
                            Faction faction = fMan.getFaction(factionID);
                            replyMessage = String.format(FactionConfig.factionSubCat.factionMOTDHeader, faction.name) + "\n" + faction.motd;
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "chat":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (factionID != null) {
                            if (args.length == 1) {
                                replyMessage = TextFormatting.GOLD + "Available chat modes are normal, faction, and ally";
                            } else {
                                try{
                                    fMan.getPlayer(playerID).factionChat = FactionChatMode.valueOf(args[1].toUpperCase());
                                    replyMessage = TextFormatting.GOLD + "Successfully set chat mode to " + args[1];
                                } catch (IllegalArgumentException e){
                                    replyMessage = TextFormatting.GOLD + "Unknown chat mode, available chat modes are normal, faction, and ally";
                                }
                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // Faction Lieutenant
                case "claim":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.LIEUTENANT.ordinal()) {
                                UUID currentOwner = fMan.getChunkOwningFaction(((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).chunkCoordX, ((EntityPlayerMP) sender).chunkCoordZ);
                                int result = fMan.claimLand(factionID, ((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).chunkCoordX, ((EntityPlayerMP) sender).chunkCoordZ);
                                switch(result){
                                    case 0:
                                        replyMessage = TextFormatting.GOLD + "Successfully claimed chunk for your faction";
                                        fMan.getPlayer(playerID).lastFactionLand = factionID;
                                        break;
                                    case 1:
                                        replyMessage = TextFormatting.GOLD + "Successfully claimed chunk for your faction off of " + fMan.getFaction(currentOwner).name;
                                        fMan.getPlayer(playerID).lastFactionLand = factionID;
                                        break;
                                    case 2:
                                        replyMessage = TextFormatting.GOLD + "You do not have enough power to claim this chunk";
                                        break;
                                    case 3:
                                        replyMessage = TextFormatting.GOLD + "You cannot claim this chunk, all your claimed land must be connected";
                                        break;
                                    case 4:
                                        replyMessage = TextFormatting.GOLD + "You cannot claim this chunk, " + fMan.getFaction(currentOwner).name + " owns it and has the power to keep it";
                                        break;
                                    case 5:
                                        replyMessage = TextFormatting.GOLD + "You already own this chunk";
                                        break;
                                }
                            } else {
                                replyMessage = TextFormatting.GOLD + "You are not a high enough rank to be able to do that";
                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // Faction Officer
                case "ally":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (args.length > 1) {
                                if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                    UUID otherFaction = fMan.getFactionIDFromName(args[1]);
                                    if (otherFaction != null){
                                        int result = fMan.addAlly(factionID, otherFaction);
                                        switch (result) {
                                            case 0:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(TextFormatting.DARK_GREEN + fMan.getFaction(factionID).name + " and " + TextFormatting.GREEN + fMan.getFaction(otherFaction).name + TextFormatting.GOLD + " are now allies" + (FactionConfig.factionSubCat.allyBuild ? ", this means they can build on yours land, but you can't build on theirs till they add you as an ally as well" : "")));
                                                break;
                                            case 1:
                                                    fMan.sendFactionwideMessage(factionID, new TextComponentString(TextFormatting.DARK_GREEN + fMan.getFaction(factionID).name + " and " + TextFormatting.GREEN + fMan.getFaction(otherFaction).name + TextFormatting.GOLD + " are now allies" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land, and they can build on yours too" : "")));
                                                break;
                                            case 2:
                                                replyMessage = TextFormatting.GOLD + "That faction is already an ally";
                                                break;
                                        }
                                    } else {
                                        replyMessage = TextFormatting.GOLD + "That faction does not exist";
                                    }

                                }
                            } else {
                                commandResult = 2;
                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "enemy":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                if (args.length > 1) {

                                } else {
                                    commandResult = 2;
                                }
                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "neutral":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "sethome":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "kick":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "invite":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "setmotd":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // Faction Owner
                case "disband":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "promote":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "demote":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "setrank":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "setdesc":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (factionID != null){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {

                            }
                        } else {
                            commandResult = 3;
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
            }
            switch (commandResult){
                case 1:
                    replyMessage = TextFormatting.RED + "You do not have permission to execute this command";
                    break;
                case 2:
                    replyMessage = TextFormatting.RED + "Error, bad argument";
                    break;
                case 3:
                    replyMessage = TextFormatting.RED + "You must be a member of a faction to do that";
                    break;
            }
            if (replyMessage != null) sender.sendMessage(new TextComponentString(replyMessage));
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
