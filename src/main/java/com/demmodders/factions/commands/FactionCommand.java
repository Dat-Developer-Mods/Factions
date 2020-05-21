package com.demmodders.factions.commands;

import com.demmodders.datmoddingapi.delayedexecution.DelayHandler;
import com.demmodders.datmoddingapi.delayedexecution.delayedevents.DelayedTeleportEvent;
import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.DemUtils;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.FlagDescriptions;
import com.demmodders.factions.util.enums.CommandResult;
import com.demmodders.factions.util.enums.FactionChatMode;
import com.demmodders.factions.util.enums.FactionRank;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.*;

public class FactionCommand extends CommandBase {
    // Map symbols
    final static String[] symbols = new String[]{"/", "\\", "|", "#", "?", "!", "%", "$", "&", "*", "£", "[", "]"};

    @Override
    public String getName() {
        return "faction";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            return printHelp(1);
        }
        return "Only a player can use these commands";
    }

    @Override
    public List<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("f");
        return aliases;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> possibilities = new ArrayList<>();
        FactionManager fMan = FactionManager.getInstance();
        UUID factionID = fMan.getPlayersFactionID(((EntityPlayerMP) sender).getUniqueID());
        if (args.length == 1) {
            // All commands are possible
            HashMap<String, String> commands = FactionCommandList.getCommands();
            if (commands != null) possibilities = new ArrayList<>(commands.keySet());
        } else if (args.length == 2){
            // Only the the first argument of commands with 1 or more arguments are possible
            switch(args[0].toLowerCase()) {
                // Argument is a page
                case "help":
                    possibilities.add("1");
                    possibilities.add("2");
                    possibilities.add("3");
                    possibilities.add("4");
                    break;
                case "list":
                    for (int i = 1; i < fMan.getListOfFactionsUUIDs().size(); i++) {
                        possibilities.add(String.valueOf(i));
                    }
                    break;

                // Argument is an invite in the player
                case "join":
                case "reject":
                    possibilities = fMan.getListOfFactionsNamesFromFactionList(fMan.getPlayer(((EntityPlayerMP) sender).getUniqueID()).invites);
                    break;

                // Argument is a faction name
                case "info":
                case "neutral":
                case "ally":
                case "enemy":
                    possibilities = fMan.getListOfFactionsNames();
                    break;

                // Argument is a member of the faction
                case "kick":
                case "setrank":
                case "demote":
                case "promote":
                case "setowner":
                    possibilities = fMan.getFaction(factionID).getMemberNames();
                    break;

                // Argument is a currently online player
                case "invite":
                case "uninvite":
                case "rank":
                    possibilities = Arrays.asList(server.getOnlinePlayerNames());
                    break;

                // Specifics
                case "flag":
                    possibilities.add("set");
                    possibilities.add("remove");
                    break;

                case "chat":
                    possibilities.add("normal");
                    possibilities.add("faction");
                    possibilities.add("ally");
                    break;
            }
        } else if (args.length == 3) {
            // Only the the second argument of commands with 2 arguments are possible
            switch (args[0].toLowerCase()) {
                case "setrank":
                    possibilities.add("grunt");
                    possibilities.add("lieutenant");
                    possibilities.add("sergeant");
                    break;
                case "flag":
                    possibilities.addAll(FlagDescriptions.getPlayerFlags().keySet());
                    break;
            }
        }
        return getListOfStringsMatchingLastWord(args, possibilities);
    }

    private String printHelp(int Page) throws IndexOutOfBoundsException {
        LinkedHashMap<String, String> commands = FactionCommandList.getCommands();

        // Check the help file was successfully loaded
        if (commands != null) {
            List<String> keyList = new ArrayList<>(commands.keySet());
            StringBuilder helpText = new StringBuilder();
            // Header
            helpText.append(DemConstants.TextColour.HEADER).append("Showing help page ").append(Page).append(" of ").append((int) Math.ceil(commands.size() / 10f)).append("\n");
            // First faction, without comma
            int firstIndex = (Page - 1) * 10;
            helpText.append(DemConstants.TextColour.COMMAND).append(keyList.get(firstIndex)).append(DemConstants.TextColour.INFO).append(" - ").append(commands.get(keyList.get(firstIndex)));
            for (int i = firstIndex + 1; i < commands.size() && i < ((10 * Page)); i++) {
                helpText.append("\n").append(DemConstants.TextColour.COMMAND).append(keyList.get(i)).append(DemConstants.TextColour.INFO).append(" - ").append(commands.get(keyList.get(i)));
            }
            return helpText.toString();
        }
        return DemConstants.TextColour.ERROR + "Could not generate help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if(!(sender instanceof EntityPlayerMP)) return;

        // Commonly used objects
        FactionManager fMan = FactionManager.getInstance();
        UUID playerID = ((EntityPlayerMP)sender).getUniqueID();
        UUID factionID = fMan.getPlayersFactionID(playerID);

        CommandResult commandResult = CommandResult.SUCCESS;
        String replyMessage = null;

        if(args.length == 0){
            // If no arguments given, tell the user how to use the command
            sender.sendMessage(new TextComponentString(getUsage(sender)));
        } else {
            // Check for the command
            switch (args[0].toLowerCase()) {
                // Global
                case "help":
                    try {
                        int page = ((args.length == 1) ? 1 : Integer.parseInt(args[1]));
                        replyMessage = printHelp(page);
                    } catch  (NumberFormatException e) {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction help <page>";
                    } catch (IndexOutOfBoundsException e){
                        replyMessage = DemConstants.TextColour.INFO + "There aren't that many pages";
                    }
                    break;
                case "list":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        try {
                            // Get which page to display
                            int page = ((args.length == 1) ? 1 : Integer.parseInt(args[1]));

                            List<UUID> factions = fMan.getListOfFactionsUUIDs();

                            if (factions.size() > 0) {
                                // Ensure some factions actually exist
                                StringBuilder factionText = new StringBuilder();
                                // Header
                                factionText.append(DemConstants.TextColour.HEADER).append("Showing factions page ").append(page).append(" of ").append( (int)Math.ceil(factions.size() / 10f)).append("\n").append(TextFormatting.RESET);

                                int start = (page - 1) * 10;

                                // First faction, without comma
                                factionText.append(fMan.getRelationColour(factionID, factions.get(start))).append(fMan.getFaction(factions.get(start)).name).append(TextFormatting.RESET);
                                for (int i = (start) + 1; i < factions.size() && i < ((10 * page)); i++) {
                                    // Highlight green if their own faction
                                    factionText.append(", ").append(fMan.getRelationColour(factionID, factions.get(i))).append(fMan.getFaction(factions.get(i)).name).append(TextFormatting.RESET);
                                }
                                replyMessage = factionText.toString();

                            } else {
                                replyMessage = DemConstants.TextColour.INFO + "There are no factions";
                            }

                        } catch (NumberFormatException e){
                            replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction list <page>";
                        } catch (IndexOutOfBoundsException e){
                            replyMessage = DemConstants.TextColour.INFO + "There aren't that many pages";
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "info":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        // If they haven't given an argument, show info on their faction
                        if (args.length == 1) {
                            if(!factionID.equals(fMan.WILDID)){
                                replyMessage = fMan.getFaction(factionID).printFactionInfo();
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "You don't belong to a faction, you may only look up other factions";
                            }
                        } else {
                            UUID otherFaction = fMan.getFactionIDFromName(args[1]);
                            if (otherFaction != null) {
                                replyMessage = fMan.getFaction(otherFaction).printFactionInfo();
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "That faction doesn't exist";
                            }
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "rank":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.info")) {
                        // If they haven't given an argument, show info on their faction
                        if (args.length == 1) {
                            if(!factionID.equals(FactionManager.WILDID)){
                                replyMessage = DemConstants.TextColour.INFO + "You have the rank of ";
                                replyMessage += FactionRank.getFactionRankString(fMan.getPlayer(playerID).factionRank);

                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "You don't belong to a faction, you don't have a rank";
                            }
                        } else {
                            UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                            if (otherPlayer == null) {
                                replyMessage = DemConstants.TextColour.ERROR + "That player doesn't exist";
                            } else if(!factionID.equals(FactionManager.WILDID)){
                                replyMessage = DemConstants.TextColour.INFO + args[1] + " has the rank of ";
                                replyMessage += FactionRank.getFactionRankString(fMan.getPlayer(otherPlayer).factionRank);

                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + args[1] + " doesn't belong to a faction, they don't have a rank";
                            }
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "map":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.map")) {
                        StringBuilder message = new StringBuilder();

                        // Map for faction -> symbol
                        HashMap<UUID, String> symbolMap = new HashMap<>();

                        // Determine start corner and end corner
                        int startX = ((EntityPlayerMP) sender).chunkCoordX - (FactionConfig.playerSubCat.mapWidth / 2);
                        int endX = ((EntityPlayerMP) sender).chunkCoordX + (FactionConfig.playerSubCat.mapWidth / 2);

                        int startZ = ((EntityPlayerMP) sender).chunkCoordZ - (FactionConfig.playerSubCat.mapHeight / 2);
                        int endZ = ((EntityPlayerMP) sender).chunkCoordZ + (FactionConfig.playerSubCat.mapHeight / 2);

                        // Iterate over all the chunks within those coords
                        for (int i = startZ; i <= endZ; i++) {
                            for (int j = startX; j <= endX; j++) {
                                // Check if the chunk is owned
                                UUID theFaction = fMan.getChunkOwningFaction(((EntityPlayerMP) sender).dimension, j, i);
                                message.append(TextFormatting.RESET);
                                // If its the same coord as the player's coord, display the centre symbol
                                if (i == ((EntityPlayerMP) sender).chunkCoordZ && j == ((EntityPlayerMP) sender).chunkCoordX) message.append(TextFormatting.BLUE).append("+");

                                // If the chunk is owned, mark it
                                else if (!theFaction.equals(FactionManager.WILDID) && !fMan.getFaction(theFaction).hasFlag("uncharted")) {
                                    if (!symbolMap.containsKey(theFaction))
                                        symbolMap.put(theFaction, symbols[symbolMap.size() % symbols.length]);

                                    message.append(fMan.getRelationColour(factionID, theFaction)).append(symbolMap.get(theFaction));
                                }

                                // Otherwise place a dash
                                else message.append("-");
                            }

                            // Go to the next line
                            message.append("\n");
                        }

                        // Display symbol mapping
                        message.append("-: Wild");

                        for (UUID theFaction : symbolMap.keySet()){
                            message.append(TextFormatting.RESET).append(", ").append(fMan.getRelationColour(factionID, theFaction)).append(symbolMap.get(theFaction)).append(": ").append(fMan.getFaction(theFaction).name);
                        }

                        replyMessage = message.toString();
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                // No Faction
                case "join":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            // Make sure they give a faction name
                            replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction join <faction name>";
                        } else {
                            // Make sure they're not in a faction
                            if (factionID.equals(fMan.WILDID)) {

                                // Make sure they gave a valid faction
                                factionID = fMan.getFactionIDFromName(args[1].toLowerCase());
                                if (factionID != null) {

                                    // Check this player can actually join the faction
                                    if (fMan.canAddPlayerToFaction(playerID, factionID)){
                                        fMan.setPlayerFaction(playerID, factionID, true);
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully joined " + FactionConstants.TextColour.OWN + fMan.getFaction(factionID).name;
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "You're not invited to that faction";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "That faction doesn't exist";
                                }
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "You can't join a faction while you're a member of a different faction, you must leave your current one first";
                            }
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "invites":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        ArrayList<UUID> invites = fMan.getPlayer(playerID).invites;
                        // Ensure they have some invites
                        if (invites.size() > 0) {
                            int page = ((args.length == 1) ? 1 : Integer.parseInt(args[1]));
                            StringBuilder inviteText = new StringBuilder();
                            // Header
                            inviteText.append(DemConstants.TextColour.HEADER).append("Showing invites page ").append(page).append(" of ").append( (int)Math.ceil(invites.size() / 10f)).append("\n").append(TextFormatting.RESET);

                            int start = (page - 1) * 10;

                            // First faction, without comma
                            inviteText.append(fMan.getFaction(invites.get(start)).name);
                            for (int i = (start) + 1; i < invites.size() && i < ((10 * page)); i++) {
                                inviteText.append(", ").append(fMan.getFaction(invites.get(i)).name);
                            }
                            replyMessage = inviteText.toString();

                        } else {
                            replyMessage = DemConstants.TextColour.ERROR + "You don't have any invites";
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "reject":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        // Make sure the player has entered a faction name
                        if (args.length == 1) {
                            replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction reject <faction name>";
                        } else {
                            // Check there's a faction with that name
                            factionID = fMan.getFactionIDFromName(args[1]);
                            if (factionID != null && fMan.getPlayer(playerID).hasInvite(factionID)) {
                                // Remove the invite, notify everyone involved
                                fMan.removePlayerInvite(playerID, factionID);
                                fMan.sendFactionwideMessage(factionID,new TextComponentString(DemConstants.TextColour.INFO + sender.getName() + " has rejected your invite"));
                                replyMessage = DemConstants.TextColour.INFO + "You have successfully rejected your invite from " + args[1];
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "You don't have an invite from that faction";
                            }
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "create":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.create")) {
                        // Make sure they give a name
                        if (args.length == 1) {
                            replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction create <name>";
                        } else {
                            // Make sure they're not already in the faction
                            if (FactionManager.WILDID.equals(fMan.getPlayer(playerID).faction)){
                                int result = fMan.createFaction(args[1], playerID);
                                // Make sure the name is valid
                                switch (result){
                                    case 0:
                                        replyMessage = DemConstants.TextColour.INFO + "Faction " + FactionConstants.TextColour.OWN + args[1] + DemConstants.TextColour.INFO + " successfully created, add a description with " + DemConstants.TextColour.COMMAND + "/faction desc <Description>, " + DemConstants.TextColour.INFO + "and invite players with " + DemConstants.TextColour.COMMAND + "/faction invite <Player>";
                                        break;
                                    case 1:
                                        replyMessage = DemConstants.TextColour.ERROR + "That name is too long";
                                        break;
                                    case 2:
                                        replyMessage = DemConstants.TextColour.ERROR + "That name is too short";
                                        break;
                                    case 3:
                                        replyMessage = DemConstants.TextColour.ERROR + "A faction with that name already exists";
                                        break;
                                    case 4:
                                        replyMessage = DemConstants.TextColour.ERROR + "Failed to create faction";
                                        break;
                                }
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "You cannot create a faction while you're in a faction";
                            }
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                // Faction member
                case "home":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        // Make sure they're in a faction
                        if (!factionID.equals(fMan.WILDID)) {
                            // Make sure they have a faction home
                            if (fMan.getFaction(factionID).homePos != null) {
                                // Make sure they're teleport cooldown has passed
                                int age = (int)(DemUtils.calculateAge(fMan.getPlayer(playerID).lastTeleport) / 1000);

                                // Create a delayed event to teleport the player
                                if (age > FactionConfig.playerSubCat.reTeleportDelay) {
                                    EntityPlayerMP playerMP = (EntityPlayerMP) sender;
                                    DelayHandler.addEvent(new DelayedTeleportEvent(fMan.getFaction(factionID).homePos, playerMP, FactionConfig.playerSubCat.teleportDelay));
                                    replyMessage = DemConstants.TextColour.INFO + "Teleporting in " + FactionConfig.playerSubCat.teleportDelay + " Seconds";
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "You must wait " + (FactionConfig.playerSubCat.teleportDelay - age) + " more seconds before you can do that again";
                                }
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "Your faction doesn't have a home";
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "members":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        // Make sure they're in a faction
                        if (!factionID.equals(fMan.WILDID)) {
                            StringBuilder message = new StringBuilder();

                            List<UUID> grunts = new ArrayList<>();
                            List<UUID> lieutenants = new ArrayList<>();
                            List<UUID> officers = new ArrayList<>();
                            UUID owner = new UUID(0L,0L);

                            for (UUID player : fMan.getFaction(factionID).members){
                                switch (fMan.getPlayer(player).factionRank){
                                    case GRUNT:
                                        grunts.add(player);
                                        break;
                                    case LIEUTENANT:
                                        lieutenants.add(player);
                                        break;
                                    case OFFICER:
                                        officers.add(player);
                                        break;
                                    case OWNER:
                                        owner = player;
                                        break;
                                }
                            }

                            // Owner
                            message.append(DemConstants.TextColour.INFO).append("Owner: ").append(fMan.getPlayerStatusColour(owner, true)).append(fMan.getPlayer(owner).lastKnownName);

                            // Officers
                            if (!officers.isEmpty()) {
                                message.append("\n").append(DemConstants.TextColour.INFO).append("Officers: ").append("\n");
                                message.append(fMan.getPlayerStatusColour(officers.get(0), true)).append(fMan.getPlayer(officers.get(0)).lastKnownName);
                                for (int i = 1; i < officers.size(); i++) {
                                    message.append(DemConstants.TextColour.INFO).append(", ").append(fMan.getPlayerStatusColour(officers.get(0), true)).append(fMan.getPlayer(officers.get(i)).lastKnownName);
                                }
                            }

                            // Lieutenants
                            if (!lieutenants.isEmpty()) {
                                message.append("\n").append(DemConstants.TextColour.INFO).append("Lieutenants: ").append("\n");
                                message.append(fMan.getPlayerStatusColour(lieutenants.get(0), true)).append(fMan.getPlayer(lieutenants.get(0)).lastKnownName);
                                for (int i = 1; i < lieutenants.size(); i++) {
                                    message.append(DemConstants.TextColour.INFO).append(", ").append(fMan.getPlayerStatusColour(lieutenants.get(0), true)).append(fMan.getPlayer(lieutenants.get(i)).lastKnownName);
                                }
                            }

                            // Grunts
                            if (!grunts.isEmpty()) {
                                message.append("\n").append(DemConstants.TextColour.INFO).append("Grunts: ").append("\n");
                                message.append(fMan.getPlayerStatusColour(grunts.get(0), true)).append(fMan.getPlayer(grunts.get(0)).lastKnownName);
                                for (int i = 1; i < grunts.size(); i++) {
                                    message.append(DemConstants.TextColour.INFO).append(", ").append(fMan.getPlayerStatusColour(grunts.get(0), true)).append(fMan.getPlayer(grunts.get(i)).lastKnownName);
                                }
                            }

                            replyMessage = message.toString();

                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "leave":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        // Check they're in a faction
                        if (!factionID.equals(fMan.WILDID)) {
                            // Ensure they're not the owner
                            if (fMan.getPlayer(playerID).factionRank == FactionRank.OWNER){
                                replyMessage = DemConstants.TextColour.ERROR + "You are the leader of this faction, you must disband your faction or pass on your status as the owner";
                            } else {
                                fMan.setPlayerFaction(playerID, FactionManager.WILDID, true);
                                replyMessage = DemConstants.TextColour.INFO + "You have successfully left your faction";
                                fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO + fMan.getPlayer(playerID).lastKnownName + " has left the faction"));
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "motd":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        // Check they're in a faction
                        if (!factionID.equals(fMan.WILDID)) {
                            Faction faction = fMan.getFaction(factionID);
                            replyMessage = DemConstants.TextColour.HEADER + String.format(FactionConfig.factionSubCat.factionMOTDHeader, faction.name) + "\n" + faction.motd;
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "chat":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        // Check they're in a faction
                        if (!factionID.equals(fMan.WILDID)) {
                            if (args.length == 1) {
                                replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction chat <Chatmode>" + DemConstants.TextColour.ERROR + " Available chat modes are normal, faction, and ally";
                            } else {
                                try{
                                    fMan.getPlayer(playerID).factionChat = FactionChatMode.valueOf(args[1].toUpperCase());
                                    replyMessage = DemConstants.TextColour.INFO + "Successfully set chat mode to " + args[1];
                                } catch (IllegalArgumentException e){
                                    replyMessage = DemConstants.TextColour.INFO + "Unknown chat mode, available chat modes are normal, faction, and ally";
                                }
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                //TODO: Members command, check members and ranks

                // Faction Lieutenant
                case "claim":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        // Check they're in a faction
                        if (!factionID.equals(fMan.WILDID)){
                            // Make sure they're the correct rank
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.LIEUTENANT.ordinal()) {
                                // Check if the land is valid for claiming
                                UUID currentOwner = fMan.getChunkOwningFaction(((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).chunkCoordX, ((EntityPlayerMP) sender).chunkCoordZ);
                                int result = fMan.claimLand(factionID, playerID, ((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).chunkCoordX, ((EntityPlayerMP) sender).chunkCoordZ);
                                switch(result){
                                    case 0:
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully claimed chunk for your faction";
                                        fMan.getPlayer(playerID).lastFactionLand = factionID;
                                        break;
                                    case 1:
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully claimed this chunk for your faction off of " + fMan.getFaction(currentOwner).name;
                                        fMan.getPlayer(playerID).lastFactionLand = factionID;
                                        break;
                                    case 2:
                                        replyMessage = DemConstants.TextColour.ERROR + "You do not have enough power to claim this chunk";
                                        break;
                                    case 3:
                                        replyMessage = DemConstants.TextColour.ERROR + "You cannot claim this chunk, all your claimed land must be connected";
                                        break;
                                    case 4:
                                        replyMessage = DemConstants.TextColour.ERROR + "You cannot claim this chunk, " + fMan.getRelationColour(factionID, currentOwner) + fMan.getFaction(currentOwner).name + DemConstants.TextColour.ERROR +  " owns it and has the power to keep it";
                                        break;
                                    case 5:
                                        replyMessage = DemConstants.TextColour.ERROR + "You already own this chunk";
                                        break;
                                    case 6:
                                        replyMessage = DemConstants.TextColour.ERROR + "Failed to claim chunk";
                                        break;
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "unclaim":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        // Check they're in a faction
                        if (!factionID.equals(fMan.WILDID)){
                            // Make sure they're the correct rank
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.LIEUTENANT.ordinal()) {
                                // Check if the land is valid for claiming
                                int result = fMan.unClaimLand(factionID, playerID, ((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).chunkCoordX, ((EntityPlayerMP) sender).chunkCoordZ);
                                switch(result){
                                    case 0:
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully removed your faction's claimed on this chunk";
                                        fMan.getPlayer(playerID).lastFactionLand = FactionManager.WILDID;
                                        break;
                                    case 1:
                                        replyMessage = DemConstants.TextColour.ERROR + "Your faction doesn't own this chunk";
                                        break;
                                    case 2:
                                        replyMessage = DemConstants.TextColour.ERROR + "Failed to unclaim chunk";
                                        break;
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                // Faction Officer
                case "ally":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        // Check they're in a faction
                        if (!factionID.equals(fMan.WILDID)){
                            // Check they have a high enough rank
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                if (args.length > 1) {
                                    UUID otherFaction = fMan.getFactionIDFromName(args[1]);
                                    // Check the other faction exists
                                    if (otherFaction != null) {
                                        int result = fMan.addAlly(factionID, otherFaction, playerID);
                                        switch (result) {
                                            //TODO: Replace with string building kinda thing
                                            case 0:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(FactionConstants.TextColour.ALLY + fMan.getFaction(otherFaction).name + DemConstants.TextColour.INFO + " is now you ally" + (FactionConfig.factionSubCat.allyBuild ? ", this means they can build on your land, but you can't build on theirs till they add you as an ally as well" : "")));
                                                break;
                                            case 1:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(FactionConstants.TextColour.ALLY + fMan.getFaction(otherFaction).name + DemConstants.TextColour.INFO + " is now your mutual ally" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land, and they can build on yours too" : "")));
                                                break;
                                            case 2:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(FactionConstants.TextColour.ALLY + fMan.getFaction(otherFaction).name + DemConstants.TextColour.INFO + " is now you ally" + (FactionConfig.factionSubCat.allyBuild ? ", this means you can build on their land, and they can build on yours too" : "") + FactionConstants.TextColour.ENEMY  + ", however, they still regard you as an enemy"));
                                                break;
                                            case 3:
                                                replyMessage = DemConstants.TextColour.ERROR + "That faction is already an ally";
                                                break;
                                            case 4:
                                                replyMessage = DemConstants.TextColour.ERROR + "That's your faction";
                                                break;
                                            case 5:
                                                replyMessage = DemConstants.TextColour.ERROR + "You cannot add that faction as an ally";
                                                break;
                                            case 6:
                                                replyMessage = DemConstants.TextColour.ERROR + "Failed to add that faction as an ally";
                                                break;
                                        }
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "That faction does not exist";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction ally <faction name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "enemy":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        // Check the player is in a faction
                        if (!factionID.equals(fMan.WILDID)){
                            // Check the player has permission in their faction
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                // Check the player gives an an argument
                                if (args.length > 1) {
                                    UUID otherFaction = fMan.getFactionIDFromName(args[1]);

                                    // Check the other faction exists
                                    if (otherFaction != null) {
                                        int result = fMan.addEnemy(factionID, otherFaction, playerID);
                                        switch (result) {
                                            //TODO: Replace with string building kinda thing
                                            case 0:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(FactionConstants.TextColour.ENEMY + fMan.getFaction(otherFaction).name + DemConstants.TextColour.INFO + " is now you enemy" + (FactionConfig.factionSubCat.enemyBuild ? ", this means you can build on their land, and they can build on yours too" : "") + ", they don't regard you as an enemy yet though"));
                                                break;
                                            case 1:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(FactionConstants.TextColour.ENEMY + fMan.getFaction(otherFaction).name + DemConstants.TextColour.INFO + " is now your mutual enemy" + (FactionConfig.factionSubCat.enemyBuild ? ", this means you can build on their land, and they can build on yours too" : "")));
                                                break;
                                            case 2:
                                                fMan.sendFactionwideMessage(factionID, new TextComponentString(FactionConstants.TextColour.ENEMY + fMan.getFaction(otherFaction).name + DemConstants.TextColour.INFO + " is now your enemy" + (FactionConfig.factionSubCat.enemyBuild ? ", this means you can build on their land, and they can build on yours too" : "") + FactionConstants.TextColour.ALLY + ", however, they still regard you as an ally"));
                                                break;
                                            case 3:
                                                replyMessage = DemConstants.TextColour.ERROR + "That faction is already an enemy";
                                                break;
                                            case 4:
                                                replyMessage = DemConstants.TextColour.ERROR + "That's your faction";
                                                break;
                                            case 5:
                                                replyMessage = DemConstants.TextColour.ERROR + "You cannot add that faction as an enemy";
                                                break;
                                            case 6:
                                                replyMessage = DemConstants.TextColour.ERROR + "Failed to add that faction as an enemy";
                                                break;
                                        }
                                    } else {
                                        replyMessage = DemConstants.TextColour.INFO + "That faction does not exist";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction enemy <faction name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "neutral":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        // Check the player is in a faction
                        if (!factionID.equals(fMan.WILDID)){
                            // Check the player has permission in their faction
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                // Check the player gives an argument
                                if (args.length > 1) {
                                    UUID otherFaction = fMan.getFactionIDFromName(args[1]);

                                    int result = fMan.addNeutral(factionID, otherFaction, playerID);
                                    switch (result) {
                                        //TODO: Replace with string building kinda thing
                                        case 0:
                                            fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO + "You no longer have any relations with " + fMan.getFaction(otherFaction).name));
                                            break;
                                        case 1:
                                            fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO + "You're no longer regard " + fMan.getFaction(otherFaction).name + " as an enemy"));
                                            break;
                                        case 2:
                                            fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO  + "You no longer regard " + fMan.getFaction(otherFaction).name + " as an ally"));                                        break;
                                        case 3:
                                        case 4:
                                            replyMessage = DemConstants.TextColour.ERROR + "You do not have relation with that faction";
                                            break;
                                        case 5:
                                            replyMessage = DemConstants.TextColour.ERROR + "That's your faction";
                                            break;
                                        case 6:
                                            replyMessage = DemConstants.TextColour.ERROR + "Failed to set that faction as neutral";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction neutral <faction name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "sethome":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                boolean result = fMan.setFactionHome(factionID, new Location(((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).posX, ((EntityPlayerMP) sender).posY, ((EntityPlayerMP) sender).posZ, ((EntityPlayerMP) sender).rotationPitch, ((EntityPlayerMP) sender).rotationYaw));
                                if (result) replyMessage = DemConstants.TextColour.INFO + "Successfully set faction home, you and your members can travel to it with " + DemConstants.TextColour.COMMAND + "/faction home";
                                else replyMessage = DemConstants.TextColour.ERROR + "Unable to set faction home, you don't own this land";
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "kick":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                if (args.length > 1) {
                                    UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                    if (otherPlayer == null || !fMan.getPlayer(otherPlayer).faction.equals(factionID)){
                                        replyMessage = DemConstants.TextColour.ERROR + "That player is not in your faction";
                                    } else if (otherPlayer.equals(playerID)) {
                                        replyMessage = DemConstants.TextColour.ERROR + "If you want to leave the faction, use  " + DemConstants.TextColour.COMMAND + "/faction leave";
                                    } else if (fMan.getPlayer(otherPlayer).factionRank == FactionRank.OWNER) {
                                        replyMessage = DemConstants.TextColour.ERROR + "You cannot kick the owner of the faction";
                                    } else {
                                        fMan.setPlayerFaction(otherPlayer, FactionManager.WILDID, true);
                                        fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO + fMan.getPlayer(otherPlayer).lastKnownName + " has been kicked from the faction"));
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction kick <member name>";
                                }
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "invite":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                if (args.length > 1) {
                                    UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                    if (otherPlayer != null){
                                        if (otherPlayer != playerID) {
                                            switch(fMan.invitePlayerToFaction(otherPlayer, factionID)) {
                                                case 0:
                                                    replyMessage = DemConstants.TextColour.INFO + args[1] + " was successfully invited to the faction";
                                                    break;
                                                case 1:
                                                    replyMessage = DemConstants.TextColour.ERROR + args[1] + " already has an invite from you";
                                                    break;
                                                case 2:
                                                    replyMessage =  DemConstants.TextColour.ERROR + args[1] + " is a member of your faction";
                                            }
                                        } else {
                                            replyMessage = DemConstants.TextColour.ERROR + "That's you";
                                        }
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "The factions system doesn't know who that is, they must have joined the server before they can be invited to a faction";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction invite <player name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "uninvite":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                if (args.length > 1) {
                                    UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                    if (otherPlayer != null){
                                        if (fMan.removePlayerInvite(otherPlayer, factionID)){
                                            replyMessage = DemConstants.TextColour.INFO + "Successfully removed invite to " + args[1];
                                            fMan.sendMessageToPlayer(otherPlayer, DemConstants.TextColour.INFO + fMan.getFaction(factionID).name + " Has revoked your invite to join their faction");
                                        } else {
                                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " doesn't have an invite from you";
                                        }
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "The factions system doesn't know who that is, they must have joined the server before they can be invited to a faction";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction uninvite <player name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "setmotd":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OFFICER.ordinal()) {
                                if (args.length > 1) {
                                    StringBuilder mOTD = new StringBuilder();
                                    for (int i = 1; i < args.length; i++){
                                        mOTD.append(args[i]).append(" ");
                                    }
                                    if (mOTD.toString().length() <= FactionConfig.factionSubCat.maxFactionMOTDLength) {
                                        fMan.getFaction(factionID).motd = mOTD.toString();
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully set MOTD to " + mOTD.toString();
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "That MOTD is too long";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction setmotd <MOTD>";

                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                // Faction Owner
                case "disband":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {
                                if (args.length == 1){
                                    replyMessage = DemConstants.TextColour.ERROR + "Are you sure? type " + DemConstants.TextColour.COMMAND + "/faction disband " + fMan.getFaction(factionID).name;
                                } else {
                                    String factionName = fMan.getFaction(factionID).name;
                                    if (args[1].equals(factionName)){
                                        if (fMan.disbandFaction(factionID, playerID)){
                                            FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(new TextComponentString(DemConstants.TextColour.INFO + factionName + " has been disbanded"));
                                        } else {
                                            replyMessage = DemConstants.TextColour.ERROR + "Failed to disband faction";
                                        }
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "Failed to disband faction, to disband your faction type " + DemConstants.TextColour.COMMAND + "/faction disband " + fMan.getFaction(factionID).name;
                                    }
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "promote":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {
                                if (args.length > 1) {
                                    UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                    if (otherPlayer == null || !fMan.getPlayer(otherPlayer).faction.equals(factionID)){
                                        replyMessage = DemConstants.TextColour.ERROR + "That player is not in your faction";
                                    } else if (otherPlayer.equals(playerID)) {
                                        replyMessage = DemConstants.TextColour.ERROR + "You can't promote yourself";
                                    } else {
                                        switch (fMan.getPlayer(otherPlayer).factionRank){
                                            case GRUNT:
                                                fMan.setPlayerRank(otherPlayer, FactionRank.LIEUTENANT);
                                                replyMessage = DemConstants.TextColour.INFO + "Promoted " + args[1] + " to Lieutenant";
                                                break;
                                            case LIEUTENANT:
                                                fMan.setPlayerRank(otherPlayer, FactionRank.OFFICER);
                                                replyMessage = DemConstants.TextColour.INFO + "Promoted " + args[1] + " to Officer";
                                                break;
                                            case OFFICER:
                                                replyMessage = DemConstants.TextColour.ERROR + "That player has the highest rank you can promote them to";
                                                break;
                                            case OWNER:
                                                replyMessage = DemConstants.TextColour.ERROR + "That player is the maximum rank possible";
                                        }
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction promote <member name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "demote":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {
                                if (args.length > 1) {
                                    UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                    if (otherPlayer == null || !fMan.getPlayer(otherPlayer).faction.equals(factionID)){
                                        replyMessage = DemConstants.TextColour.ERROR + "That player is not in your faction";
                                    } else if (otherPlayer.equals(playerID)) {
                                        replyMessage = DemConstants.TextColour.ERROR + "You can't demote yourself";
                                    } else {
                                        switch (fMan.getPlayer(otherPlayer).factionRank){
                                            case GRUNT:
                                                replyMessage = DemConstants.TextColour.ERROR + "That player is the minimum rank possible";
                                                break;
                                            case LIEUTENANT:
                                                fMan.setPlayerRank(otherPlayer, FactionRank.GRUNT);
                                                replyMessage = DemConstants.TextColour.INFO + "Demoted " + args[1] + " to Grunt";
                                                break;
                                            case OFFICER:
                                                fMan.setPlayerRank(otherPlayer, FactionRank.LIEUTENANT);
                                                replyMessage = DemConstants.TextColour.INFO + "Demoted " + args[1] + " to Lieutenant";
                                                break;
                                            case OWNER:
                                                replyMessage = DemConstants.TextColour.ERROR + "You cannot demote the owner";
                                        }
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction demote <member name>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "setrank":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {
                                if (args.length > 2) {
                                    try {
                                        UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                        FactionRank rank = FactionRank.valueOf(args[2].toUpperCase());

                                        if (otherPlayer == null || !fMan.getPlayer(otherPlayer).faction.equals(factionID)){
                                            replyMessage = DemConstants.TextColour.ERROR + "That player is not in your faction";
                                        } else if (otherPlayer.equals(playerID)){
                                            replyMessage = DemConstants.TextColour.ERROR + "You cannot set your own rank";
                                        } else if (fMan.getPlayer(otherPlayer).factionRank == FactionRank.OFFICER) {
                                            replyMessage = DemConstants.TextColour.ERROR + "You cannot change the rank of the owner";
                                        } else if (fMan.getPlayer(otherPlayer).factionRank.ordinal() >= fMan.getPlayer(playerID).factionRank.ordinal()) {
                                            replyMessage = DemConstants.TextColour.ERROR + "You cannot change the rank of a player of the same rank or higher than you";
                                        } else if (rank == FactionRank.OWNER) {
                                            replyMessage = DemConstants.TextColour.ERROR + "To set a player as the owner, use " + DemConstants.TextColour.COMMAND + "/faction setowner <player>";
                                        } else {
                                            fMan.setPlayerRank(otherPlayer, rank);
                                            replyMessage = DemConstants.TextColour.INFO + "Set " + args[1] + " to " + rank.toString().toLowerCase();
                                        }
                                    } catch (IllegalArgumentException e){
                                        replyMessage = DemConstants.TextColour.ERROR + "Unknown rank, available ranks are: grunt, lieutenant, and officer";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction setrank <member name> <new rank>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "setowner":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank == FactionRank.OWNER) {
                                if (args.length > 1) {
                                    UUID otherPlayer = fMan.getPlayerIDFromName(args[1]);
                                    if (otherPlayer == null || !fMan.getPlayer(otherPlayer).faction.equals(factionID)){
                                        replyMessage = DemConstants.TextColour.ERROR + "That player is not in your faction";
                                    } else if (otherPlayer.equals(playerID)){
                                        replyMessage = DemConstants.TextColour.ERROR + "You are already the owner";
                                    } else {
                                        fMan.setPlayerRank(playerID, FactionRank.OFFICER);
                                        fMan.setPlayerRank(otherPlayer, FactionRank.OWNER);
                                        fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO + fMan.getPlayer(otherPlayer).lastKnownName + " is now the new leader of " + FactionConstants.TextColour.OWN + fMan.getFaction(factionID).name));
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction setowner <member name>";
                                }
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "flag":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {
                                if (args.length > 2) {
                                    HashMap<String, String> flags = FlagDescriptions.getPlayerFlags();
                                    if (FlagDescriptions.getPlayerFlags().containsKey(args[2].toLowerCase())){
                                        if (args[1].equals("set")) {
                                            if(!fMan.getFaction(factionID).hasFlag(args[2].toLowerCase())) {
                                                fMan.getFaction(factionID).setFlag(args[2].toLowerCase());
                                                replyMessage = DemConstants.TextColour.INFO + "Successfully set flag";
                                            } else {
                                                replyMessage = DemConstants.TextColour.ERROR + "Your faction already has that flag set";
                                            }
                                        } else if (args[1].equals("remove")){
                                            if(fMan.getFaction(factionID).hasFlag(args[2].toLowerCase())) {
                                                fMan.getFaction(factionID).removeFlag(args[2].toLowerCase());
                                                replyMessage = DemConstants.TextColour.INFO + "Successfully removed flag";
                                            } else {
                                                replyMessage = DemConstants.TextColour.ERROR + "Your faction doesn't have that flag set";
                                            }
                                        } else {
                                            replyMessage = DemConstants.TextColour.ERROR + "Unknown flag operation, correct operations are: set, remove";
                                        }
                                    } else {
                                        StringBuilder flagsMessage = new StringBuilder();
                                        flagsMessage.append(DemConstants.TextColour.ERROR).append("Unknown flag, available flags are:");
                                        for(String flag : flags.keySet()){
                                            flagsMessage.append("\n").append(DemConstants.TextColour.INFO).append(flag).append(" - ").append(flags.get(flag));
                                        }
                                        replyMessage = flagsMessage.toString();
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction flag set|remove <flag>";

                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;

                case "desc":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (!factionID.equals(fMan.WILDID)){
                            if (fMan.getPlayer(playerID).factionRank.ordinal() >= FactionRank.OWNER.ordinal()) {
                                if (args.length > 1) {
                                    StringBuilder desc = new StringBuilder();
                                    for (int i = 1; i < args.length; i++){
                                        desc.append(args[i]).append(" ");
                                    }
                                    if (desc.toString().length() <= FactionConfig.factionSubCat.maxFactionDescLength) {
                                        fMan.getFaction(factionID).desc = desc.toString();
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully set description to " + desc.toString();
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + "That description is too long";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/faction desc <description>";
                                }
                            } else {
                                commandResult = CommandResult.NOFACTIONPERMISSION;
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOPERMISSION;
                    }
                    break;
                default:
                    replyMessage = DemConstants.TextColour.INFO + "Unknown command, use " + DemConstants.TextColour.COMMAND + "/faction help " + DemConstants.TextColour.INFO + "for a list of available commands";
            }
            switch (commandResult){
                case NOPERMISSION:
                    replyMessage = DemConstants.TextColour.ERROR + "You do not have permission to execute this command";
                    break;
                case NOFACTION:
                    replyMessage = DemConstants.TextColour.ERROR + "You must be a member of a faction to do that";
                    break;
                case NOFACTIONPERMISSION:
                    replyMessage = DemConstants.TextColour.ERROR + "You're not a high enough rank in your faction to do that";
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
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}

