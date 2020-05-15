package com.demmodders.factions.commands;

import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.datmoddingapi.util.DatTeleporter;
import com.demmodders.datmoddingapi.util.DemConstants;
import com.demmodders.factions.faction.FactionManager;
import com.demmodders.factions.util.FactionConfig;
import com.demmodders.factions.util.FactionConstants;
import com.demmodders.factions.util.FlagDescriptions;
import com.demmodders.factions.util.enums.FactionRank;
import com.demmodders.factions.util.enums.RelationState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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

    private String printHelp(int Page) throws IndexOutOfBoundsException {
        LinkedHashMap<String, String> commands = FactionCommandList.getAdminCommands();

        // Check the help file was successfully loaded
        if (commands != null) {
            List<String> keyList = new ArrayList<>(commands.keySet());
            StringBuilder helpText = new StringBuilder();
            // Header
            helpText.append(TextFormatting.DARK_GREEN).append("Showing help page ").append(Page).append(" of ").append((int) Math.ceil(commands.size() / 10f)).append("\n").append(TextFormatting.GOLD);
            // First faction, without comma
            int firstIndex = (Page - 1) * 10;
            helpText.append(keyList.get(firstIndex)).append(" - ").append(commands.get(keyList.get(firstIndex))).append("\n");
            for (int i = firstIndex + 1; i < commands.size() && i < ((10 * Page)); i++) {
                helpText.append(TextFormatting.GOLD).append(keyList.get(i)).append(" - ").append(commands.get(keyList.get(i))).append("\n");
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
                        replyMessage = DemConstants.TextColour.ERROR + "There aren't that many pages";
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
                        if (factionID != null) {
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
                case "Enemy":
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
                        } else if (fMan.getPlayersFactionID(targetPlayer) == null) {
                            replyMessage = DemConstants.TextColour.ERROR + "That player isn't in a faction";
                        } else {
                            if (fMan.getPlayer(targetPlayer).factionRank != FactionRank.OWNER) {
                                fMan.setPlayerFaction(targetPlayer, FactionManager.WILDID, true);
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
                        } else if (fMan.getPlayer(targetPlayer).faction != null) {
                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " cannot receive an invite as they are already a member of a faction";
                        } else {
                            if (fMan.invitePlayerToFaction(targetPlayer, targetFaction)) {
                                replyMessage = DemConstants.TextColour.INFO + "Successfully invited " + args[1] + " to " + args[2];
                            } else {
                                replyMessage = DemConstants.TextColour.ERROR + args[1] + " already has an invite from " + args[2];
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
                        } else if (fMan.getPlayer(targetPlayer).faction != null) {
                            replyMessage = DemConstants.TextColour.ERROR + args[1] + " doesn't have any invites as they're in a faction";
                        } else {
                            if (fMan.removePlayerInvite(targetPlayer, targetFaction)) {
                                replyMessage = DemConstants.TextColour.INFO + "Successfully removed " + args[1] + "'s invite " + " from " + args[2];
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
                                replyMessage = DemConstants.TextColour.INFO + "Successfully set " + args[1] + "'s MOTD to " + mOTD.toString();
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
                            UUID targetFaction = fMan.getPlayerIDFromName(args[1]);
                            if (targetFaction == null) {
                                replyMessage = DemConstants.TextColour.ERROR + "Unknown Faction";
                            } else {
                                boolean result = fMan.setFactionHome(targetFaction, new Location(((EntityPlayerMP) sender).dimension, ((EntityPlayerMP) sender).posX, ((EntityPlayerMP) sender).posY, ((EntityPlayerMP) sender).posZ, ((EntityPlayerMP) sender).rotationPitch, ((EntityPlayerMP) sender).rotationYaw));
                                if (result)
                                    replyMessage = DemConstants.TextColour.INFO + "Successfully set " + args[1] + "'s home";
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
                                replyMessage = DemConstants.TextColour.INFO + "Successfully set " + args[1] + "'s description to " + desc.toString();
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
                        } else if (fMan.getPlayersFaction(targetPlayer) != null) {
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
                        } else if (fMan.getPlayersFaction(targetPlayer) != null) {
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
                            if (fMan.disbandFaction(targetFaction, null)) replyMessage = DemConstants.TextColour.INFO + "Successfully disbanded" + args[1];
                            else replyMessage = DemConstants.TextColour.ERROR + "Failed to disband " + args[1];
                        }
                    } else {
                        replyMessage = DemConstants.TextColour.ERROR + "Bad argument, command should look like: " + DemConstants.TextColour.COMMAND + "/factionadmin disband <Faction name>";
                    }
                    break;

                //TODO Finish
                default:
                    replyMessage = DemConstants.TextColour.INFO + "Unknown command, use " + DemConstants.TextColour.COMMAND + "/faction help " + DemConstants.TextColour.INFO + "for a list of available commands";
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
