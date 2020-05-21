package com.demmodders.factions.commands;

import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.datmoddingapi.util.DatTeleporter;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.datmoddingapi.util.DemStringUtils;
import com.demmodders.factions.faction.Faction;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.FlagDescriptions;
import com.demmodders.factions.util.enums.CommandResult;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.*;

public class FAdminCommand extends CommandBase {
    @Override
    public String getName() {
        return "factionadmin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return printHelp(1);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> possibilities = new ArrayList<>();
        FactionManager fMan = FactionManager.getInstance();
        UUID factionID = fMan.getPlayersFactionID(((EntityPlayerMP) sender).getUniqueID());
        if (args.length == 1) {
            // All commands are possible
            HashMap<String, String> commands = FactionCommandList.getAdminCommands();
            if (commands != null) possibilities = new ArrayList<>(commands.keySet());
        } else if (args.length == 2){
            // Only the the first argument of commands with 1 or more arguments are possible
            switch(args[0].toLowerCase()) {
                // Argument is a page
                case "help":
                    possibilities.add("1");
                    possibilities.add("2");
                    possibilities.add("3");
                    break;

                // Argument is an invite in the player
                case "":
                    possibilities = fMan.getListOfFactionsNamesFromFactionList(fMan.getPlayer(((EntityPlayerMP) sender).getUniqueID()).invites);
                    break;

                // Argument is a faction name
                case "claim":
                case "ally":
                case "enemy":
                case "neutral":
                case "setmotd":
                case "sethome":
                case "desc":
                case "flag":
                case "disband":
                case "setpower":
                case "setmaxpower":
                case "resetpower":
                case "resetmaxpower":
                    possibilities = fMan.getListOfFactionsNames();
                    break;

                // Argument is a currently online player
                case "setfaction":
                case "kick":
                case "invite":
                case "uninvite":
                case "promote":
                case "demote":
                case "setrank":
                case "setowner":
                    possibilities = Arrays.asList(server.getOnlinePlayerNames());
                    break;
            }
        } else if (args.length == 3) {
            // Only the the second argument of commands with 2 arguments are possible
            switch (args[0].toLowerCase()) {
                // Argument is a faction name
                case "setfaction":
                case "ally":
                case "enemy":
                case "neutral":
                case "invite":
                case "uninvite":
                    possibilities = fMan.getListOfFactionsNames();
                    break;

                // Specifics
                case "flag":
                    possibilities.add("set");
                    possibilities.add("remove");
                    break;

                case "setrank":
                    possibilities.add("grunt");
                    possibilities.add("lieutenant");
                    possibilities.add("sergeant");
                    break;
            }
        } else if (args.length == 4) {
            switch (args[0].toLowerCase()) {
                case "flag":
                    possibilities.addAll(FlagDescriptions.getPlayerFlags().keySet());
                    possibilities.addAll(FlagDescriptions.getAdminFlags().keySet());
                    break;
            }
        }
        return getListOfStringsMatchingLastWord(args, possibilities);
    }

    private String printHelp(int Page) throws IndexOutOfBoundsException {
        LinkedHashMap<String, String> commands = FactionCommandList.getAdminCommands();

        // Check the help file was successfully loaded
        if (commands != null) {
            List<String> keyList = new ArrayList<>(commands.keySet());
            StringBuilder helpText = new StringBuilder();
            // Header
            helpText.append(DemConstants.TextColour.HEADER).append("Showing admin help page ").append(Page).append(" of ").append((int) Math.ceil(commands.size() / 10f)).append("\n");
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // Commonly used objects
        FactionManager fMan = FactionManager.getInstance();

        UUID playerID = null;
        UUID factionID = null;

        boolean console = true;
        if (sender instanceof EntityPlayerMP){
            playerID = ((EntityPlayerMP)sender).getUniqueID();
            factionID = fMan.getPlayersFactionID(playerID);
            console = false;
        }

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
                    } catch (NumberFormatException e) {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin help <page>";
                    } catch (IndexOutOfBoundsException e) {
                        replyMessage = DemConstants.TextColour.INFO + "There aren't that many pages";
                    }
                    break;
                case "setfaction":
                    if (args.length > 2) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        UUID targetFaction = fMan.getFactionIDFromName(args[2]);
                        if (targetPlayer == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                        } else if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            fMan.setPlayerFaction(targetPlayer, targetFaction, true);
                            replyMessage = DemConstants.TextColour.INFO + args[1] + " Successfully joined " + args[2];
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin setfaction <Player name> <Faction name>";
                    }
                    break;
                case "home":
                    if (!console) {
                        if (!factionID.equals(fMan.WILDID)) {
                            // Make sure they have a faction home
                            Location destination = fMan.getFaction(factionID).homePos;
                            if (destination != null) {
                                if (destination.dim != ((EntityPlayerMP) sender).dimension) {
                                    ((EntityPlayerMP) sender).changeDimension(destination.dim, new DatTeleporter(destination));
                                } else {
                                    ((EntityPlayerMP) sender).connection.setPlayerLocation(destination.x, destination.y, destination.z, destination.yaw, destination.pitch);
                                }
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "Your faction doesn't have a home";
                            }
                        } else {
                            commandResult = CommandResult.NOFACTION;
                        }
                    } else {
                        commandResult = CommandResult.NOCONSOLE;
                    }
                    break;
                case "claim":
                    if (!console) {
                        UUID targetFaction = (args.length > 1 ? fMan.getFactionIDFromName(args[1]) : factionID);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            fMan.forceClaimLand(targetFaction, ((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).chunkCoordX, ((EntityPlayerMP) sender).chunkCoordZ);
                            replyMessage = DemConstants.TextColour.INFO + "Successfully claimed this chunk for " + fMan.getFaction(targetFaction).name;
                            fMan.getPlayer(playerID).lastFactionLand = targetFaction;
                            break;
                        }
                    } else {
                        commandResult = CommandResult.NOCONSOLE;
                    }
                    break;
                case "ally":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        UUID targetFaction2 = fMan.getFactionIDFromName(args[2]);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction: " + args[1];
                        } else if (targetFaction2 == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction: " + args[2];
                        } else {
                            fMan.setFactionRelation(targetFaction, targetFaction2, RelationState.ALLY);
                            fMan.setFactionRelation(targetFaction2, targetFaction, RelationState.ALLY);
                            replyMessage = DemConstants.TextColour.INFO + "Successfully made " + args[1] + " and " + args[2] + " allies";
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin ally <Faction1 name> <Faction2 name>";
                    }
                    break;
                case "enemy":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        UUID targetFaction2 = fMan.getFactionIDFromName(args[2]);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction: " + args[1];
                        } else if (targetFaction2 == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction: " + args[2];
                        } else {
                            fMan.setFactionRelation(targetFaction, targetFaction2, RelationState.ENEMY);
                            fMan.setFactionRelation(targetFaction2, targetFaction, RelationState.ENEMY);
                            replyMessage = DemConstants.TextColour.INFO + "Successfully made " + args[1] + " and " + args[2] + " enemies";
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin enemy <Faction1 name> <Faction2 name>";
                    }
                    break;
                case "neutral":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        UUID targetFaction2 = fMan.getFactionIDFromName(args[2]);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction: " + args[1];
                        } else if (targetFaction2 == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction: " + args[2];
                        } else {
                            fMan.setFactionRelation(targetFaction, targetFaction2, null);
                            fMan.setFactionRelation(targetFaction2, targetFaction, null);
                            replyMessage = DemConstants.TextColour.INFO + "Successfully made " + args[1] + " and " + args[2] + " neutral";
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin neutral <Faction1 name> <Faction2 name>";
                    }
                    break;
                case "kick":
                    if (args.length > 1) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        if (targetPlayer == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                        } else if (fMan.getPlayersFactionID(targetPlayer).equals(FactionManager.WILDID)) {
                            replyMessage = DemConstants.TextColour.ERROR + "That player isn't in a faction";
                        } else {
                            if (fMan.getPlayer(targetPlayer).factionRank != FactionRank.OWNER) {
                                fMan.setPlayerFaction(targetPlayer, FactionManager.WILDID, true);
                                replyMessage = DemConstants.TextColour.INFO + "Successfully kicked " + args[1] + " from their faction";
                                fMan.sendMessageToPlayer(targetPlayer, DemConstants.TextColour.INFO + "You have been kicked from your faction by an admin");
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "The owner cannot be kicked from their faction, you'll have to give away their rank first with " + DemConstants.TextColour.COMMAND + "/factionadmin setowner <Player name>" + DemConstants.TextColour.ERROR + " or disband their faction with " + DemConstants.TextColour.COMMAND + "/factionadmin disband <Faction name>";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin kick <Player name>";
                    }
                    break;
                case "invite":
                    if (args.length > 2) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        UUID targetFaction = fMan.getFactionIDFromName(args[2]);
                        if (targetPlayer == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                        } else if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else if (fMan.getPlayer(targetPlayer).faction != FactionManager.WILDID) {
                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " cannot receive an invite as they are already a member of a faction";
                        } else {
                            switch(fMan.invitePlayerToFaction(targetPlayer, targetFaction)) {
                                case 0:
                                    replyMessage = DemConstants.TextColour.INFO + "Successfully invited " + args[1] + " to " + args[2];
                                    break;
                                case 1:
                                    replyMessage = DemConstants.TextColour.ERROR + args[1] + " already has an invite from " + args[2];
                                    break;
                                case 2:
                                    replyMessage = DemConstants.TextColour.ERROR + args[1] + " is already a member of " + args[2];
                                    break;
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin invite <Player name> <Faction name>";
                    }
                    break;
                case "uninvite":
                    if (args.length > 2) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        UUID targetFaction = fMan.getFactionIDFromName(args[2]);
                        if (targetPlayer == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                        } else if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            if (fMan.removePlayerInvite(targetPlayer, targetFaction)) {
                                replyMessage = DemConstants.TextColour.INFO + "Successfully removed " + DemStringUtils.makePossessive(args[1]) + " invite " + " from " + args[2];
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + args[1] + " doesn't have an invite from " + args[2];
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin uninvite <Player name> <Faction name>";
                    }
                    break;
                case "setmotd":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown Faction";
                        } else {
                            StringBuilder mOTD = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                mOTD.append(args[i]).append(" ");
                            }
                            if (mOTD.toString().length() <= FactionConfig.factionSubCat.maxFactionMOTDLength) {
                                fMan.getFaction(targetFaction).motd = mOTD.toString();
                                replyMessage = DemConstants.TextColour.INFO + "Successfully set " + DemStringUtils.makePossessive(args[1]) + " MOTD to " + mOTD.toString();
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "That MOTD is too long";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin setmotd <Faction name> [MOTD]";
                    }
                    break;
                case "sethome":
                    if (!console) {
                        if (args.length > 1) {
                            UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                            if (targetFaction == null) {
                                replyMessage = DemConstants.TextColour.ERROR + "Unknown Faction";
                            } else {
                                boolean result = fMan.setFactionHome(targetFaction, new Location(((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).posX, ((EntityPlayerMP) sender).posY, ((EntityPlayerMP) sender).posZ, ((EntityPlayerMP) sender).rotationPitch, ((EntityPlayerMP) sender).rotationYaw));
                                if (result)
                                    replyMessage = DemConstants.TextColour.INFO + "Successfully set " + DemStringUtils.makePossessive(args[1]) + " home";
                                else
                                    replyMessage = DemConstants.TextColour.ERROR + "Unable to set faction home, they don't own this land";
                            }
                        } else {
                            replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin sethome <Faction name>";
                        }
                    } else {
                        commandResult = CommandResult.NOCONSOLE;
                    }
                    break;
                case "desc":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown Faction";
                        } else {
                            StringBuilder desc = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                desc.append(args[i]).append(" ");
                            }
                            if (desc.toString().length() <= FactionConfig.factionSubCat.maxFactionMOTDLength) {
                                fMan.getFaction(targetFaction).desc = desc.toString();
                                replyMessage = DemConstants.TextColour.INFO + "Successfully set " + DemStringUtils.makePossessive(args[1]) + " description to " + desc.toString();
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + "That description is too long";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin desc <Faction name> [description]";
                    }
                    break;
                case "flag":
                    if (args.length > 3) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown Faction";
                        } else {

                            HashMap<String, String> flags = new HashMap<>(FlagDescriptions.getPlayerFlags());
                            flags.putAll(FlagDescriptions.getAdminFlags());
                            if (flags.containsKey(args[3].toLowerCase())) {
                                if (args[2].equals("set")) {
                                    if (!fMan.getFaction(targetFaction).hasFlag(args[3].toLowerCase())) {
                                        fMan.getFaction(targetFaction).setFlag(args[3].toLowerCase());
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully set flag";
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + args[1] + " already has that flag set";
                                    }
                                } else if (args[2].equals("remove")) {
                                    if (fMan.getFaction(targetFaction).hasFlag(args[3].toLowerCase())) {
                                        fMan.getFaction(targetFaction).removeFlag(args[3].toLowerCase());
                                        replyMessage = DemConstants.TextColour.INFO + "Successfully removed flag";
                                    } else {
                                        replyMessage = DemConstants.TextColour.ERROR + args[1] + " doesn't have that flag set";
                                    }
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "Unknown flag operation, correct operations are: set, remove";
                                }
                            } else {
                                StringBuilder flagsMessage = new StringBuilder();
                                flagsMessage.append(DemConstants.TextColour.ERROR).append("Unknown flag, available flags are:");
                                for (String flag : flags.keySet()) {
                                    flagsMessage.append("\n").append(DemConstants.TextColour.INFO).append(flag).append(" - ").append(flags.get(flag));
                                }
                                replyMessage = flagsMessage.toString();
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin flag set|remove <flag>";
                    }
                    break;
                case "promote":
                    if (args.length > 1) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        if (targetPlayer == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                        } else if (fMan.getPlayersFactionID(targetPlayer).equals(FactionManager.WILDID)) {
                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " isn't in a faction";
                        } else {
                            switch (fMan.getPlayer(targetPlayer).factionRank) {
                                case GRUNT:
                                    fMan.setPlayerRank(targetPlayer, FactionRank.LIEUTENANT);
                                    replyMessage = DemConstants.TextColour.INFO + "Promoted " + args[1] + " to Lieutenant";
                                    break;
                                case LIEUTENANT:
                                    fMan.setPlayerRank(targetPlayer, FactionRank.OFFICER);
                                    replyMessage = DemConstants.TextColour.INFO + "Promoted " + args[1] + " to Officer";
                                    break;
                                case OFFICER:
                                    replyMessage = DemConstants.TextColour.ERROR + "That player has the highest rank they can be promoted to";
                                    break;
                                case OWNER:
                                    replyMessage = DemConstants.TextColour.ERROR + "That player is the maximum rank possible";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin promote <Player name>";
                    }
                    break;
                case "demote":
                    if (args.length > 1) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        if (targetPlayer == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                        } else if (fMan.getPlayersFactionID(targetPlayer).equals(FactionManager.WILDID)) {
                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " isn't in a faction";
                        } else {
                            switch (fMan.getPlayer(targetPlayer).factionRank){
                                case GRUNT:
                                    replyMessage = DemConstants.TextColour.ERROR + "That player is the minimum rank possible";
                                    break;
                                case LIEUTENANT:
                                    fMan.setPlayerRank(targetPlayer, FactionRank.GRUNT);
                                    replyMessage = DemConstants.TextColour.INFO + "Demoted " + args[1] + " to Grunt";
                                    break;
                                case OFFICER:
                                    fMan.setPlayerRank(targetPlayer, FactionRank.LIEUTENANT);
                                    replyMessage = DemConstants.TextColour.INFO + "Demoted " + args[1] + " to Lieutenant";
                                    break;
                                case OWNER:
                                    replyMessage = DemConstants.TextColour.ERROR + "You cannot demote the owner";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin demote <Player name>";
                    }
                    break;
                case "setrank":
                    if (args.length > 2) {
                        try {
                            FactionRank rank = FactionRank.valueOf(args[2].toUpperCase());
                            UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                            if (targetPlayer == null) {
                                replyMessage = DemConstants.TextColour.ERROR + "Unknown player";
                            } else if (rank == FactionRank.OWNER) {
                                replyMessage = DemConstants.TextColour.ERROR + "To set a player as the owner, use " + DemConstants.TextColour.COMMAND + "/factionadmin setowner <player>";
                            } else {
                                fMan.setPlayerRank(targetPlayer, rank);
                                replyMessage = DemConstants.TextColour.INFO + "Set " + args[1] + " to " + rank.toString().toLowerCase();
                            }
                        } catch (IllegalArgumentException e){
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown rank, available ranks are: grunt, lieutenant, and officer";
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin setrank <member name> <new rank>";
                    }
                    break;
                case "setowner":
                    if (args.length > 1) {
                        UUID targetPlayer = fMan.getPlayerIDFromName(args[1]);
                        if (fMan.getPlayersFaction(targetPlayer) == null){
                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " is not in a faction";
                        } else {
                            UUID ownerID = fMan.getPlayersFaction(targetPlayer).getOwnerID();
                            fMan.setPlayerRank(ownerID, FactionRank.OFFICER);
                            fMan.setPlayerRank(targetPlayer, FactionRank.OWNER);
                            fMan.sendFactionwideMessage(factionID, new TextComponentString(DemConstants.TextColour.INFO + fMan.getPlayer(targetPlayer).lastKnownName + " is now the new leader of " + FactionConstants.TextColour.OWN + fMan.getFaction(factionID).name));
                            replyMessage = DemConstants.TextColour.INFO + "Successfully set " + args[1] + " as the owner of their faction";
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin setowner <Member name>";
                    }
                    break;
                case "disband":
                    if (args.length > 1) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null){
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else if (args.length == 2) {
                            replyMessage = DemConstants.TextColour.INFO + "Are you sure you want to disband " + args[1] + "? Type " + DemConstants.TextColour.COMMAND + "/factionadmin disband " + args[1] + " confirm " + DemConstants.TextColour.INFO + "to confirm you want to disband them";
                        } else if (args[2].equalsIgnoreCase("confirm")) {
                            if (fMan.disbandFaction(targetFaction, null)) replyMessage = DemConstants.TextColour.INFO + "Successfully disbanded " + args[1];
                            else replyMessage = DemConstants.TextColour.ERROR + "Failed to disband " + args[1];
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin disband <Faction name>";
                    }
                    break;
                case "setpower":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null){
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            try {
                                Faction theFaction = fMan.getFaction(targetFaction);
                                int newPower = Integer.parseInt(args[2]);
                                if (theFaction.power.maxPower < newPower) {
                                    theFaction.power.power = newPower;
                                    replyMessage = DemConstants.TextColour.INFO + "Successfully set the power of " + args[1] + " to " + args[2];
                                } else {
                                    replyMessage = DemConstants.TextColour.ERROR + "The new power must be less than the faction's max power, you can set the factions max power with " + DemConstants.TextColour.COMMAND + "/factionadmin setmaxpower <Faction Name> <New Max Power>";
                                }
                            } catch (NumberFormatException e) {
                                replyMessage = DemConstants.TextColour.ERROR + "The new power must be a number";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin setpower <Faction Name> <New Power>";
                    }
                    break;
                case "setmaxpower":
                    if (args.length > 2) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null){
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            try {
                                Faction theFaction = fMan.getFaction(targetFaction);
                                int newPower = Integer.parseInt(args[2]);
                                theFaction.power.maxPower = newPower;
                                if (theFaction.power.maxPower < theFaction.power.power) {
                                    theFaction.power.power = newPower;
                                }

                                replyMessage = DemConstants.TextColour.INFO + "Successfully set the max power of " + args[1] + " to " + args[2];
                            } catch (NumberFormatException e) {
                                replyMessage = DemConstants.TextColour.ERROR + "The new power must be a number";
                            }
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin setmaxpower <Faction Name> <New Max Power>";
                    }
                    break;
                case "resetpower":
                    if (args.length > 1) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null){
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            Faction theFaction = fMan.getFaction(targetFaction);
                            theFaction.power.power = FactionConfig.factionSubCat.factionStartingPower;

                            replyMessage = DemConstants.TextColour.INFO + "Successfully reset the power of " + args[1] + " to " + FactionConfig.factionSubCat.factionStartingPower;
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin resetpower <Faction Name>";
                    }
                    break;
                case "resetMaxPower":
                    if (args.length > 1) {
                        UUID targetFaction = fMan.getFactionIDFromName(args[1]);
                        if (targetFaction == null){
                            replyMessage = DemConstants.TextColour.ERROR + "Unknown faction";
                        } else {
                            Faction theFaction = fMan.getFaction(targetFaction);
                            theFaction.power.maxPower = FactionConfig.factionSubCat.factionStartingMaxPower;
                            if (theFaction.power.maxPower < theFaction.power.power) {
                                theFaction.power.power = theFaction.power.maxPower;
                            }
                            replyMessage = DemConstants.TextColour.INFO + "Successfully reset the max power of " + args[1] + " to " + FactionConfig.factionSubCat.factionStartingMaxPower;
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin resetmaxpower <Faction Name>";
                    }
                    break;
                default:
                    replyMessage = DemConstants.TextColour.INFO + "Unknown command, use " + DemConstants.TextColour.COMMAND + "/factionadmin help " + DemConstants.TextColour.INFO + "for a list of available commands";
            }
            switch (commandResult){
                case NOFACTION:
                    replyMessage = DemConstants.TextColour.ERROR + "You must be a member of a faction to do that";
                    break;
                case NOCONSOLE:
                    replyMessage = DemConstants.TextColour.ERROR + "This command is only available to players";
                    break;
            }
            if (replyMessage != null) sender.sendMessage(new TextComponentString(replyMessage));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("fa");
        aliases.add("fadmin");
        aliases.add("factiona");
        return aliases;
    }
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
