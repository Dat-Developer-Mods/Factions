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
import net.minecraft.util.text.TextFormatting;
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
            return "test";
        }
        return "Only a player can use these commands";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Factions.LOGGER.info(Arrays.toString(args));
        if(!(sender instanceof EntityPlayerMP)) return;

        int commandResult = 0;
        FactionManager fMan = FactionManager.getInstance();
        UUID playerID = ((EntityPlayerMP)sender).getUniqueID();
        UUID factionID = fMan.getPlayersFactionID(playerID);
        String replyMessage = "";

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
                                factionText.append(TextFormatting.DARK_GREEN).append("Showing factions page ").append(page).append(" of ").append( factions.size() % 10).append("\n").append(TextFormatting.RESET);

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
                                replyMessage = "There are no factions";
                            }

                        } catch (NumberFormatException e){
                            commandResult = 2;
                        } catch (IndexOutOfBoundsException e){
                            replyMessage = "There aren't that many pages";
                        }
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
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            // ToDo: Bad command
                        } else {
                            // ToDo: join faction
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "invites":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "reject":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
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
                                        replyMessage = "Faction " + args[1] + " successfully created, add a description with \"/faction desc\", and invite players with \"/faction invite <Player>\"";
                                        break;
                                    case 1:
                                        replyMessage = "Failed to create faction: name too long";
                                        break;
                                    case 2:
                                        replyMessage = "Failed to create faction: name too short";
                                        break;
                                    case 3:
                                        replyMessage = "Failed to create faction: a faction with that name already exists";
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
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "leave":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "motd":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.default")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // Faction Officer
                case "ally":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "enemy":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "neutral":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "claim":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "sethome":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "kick":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "invite":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "setmotd":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;

                // Faction Owner
                case "disband":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "promote":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "demote":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "setrank":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
                case "setdesc":
                    if (PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.faction.manage")) {
                        if (args.length == 1) {
                            // ToDo: give own faction info
                        } else {
                            // ToDo: Look up faction and display info
                        }
                    } else {
                        commandResult = 1;
                    }
                    break;
            }
            switch (commandResult){
                case 0:
                    sender.sendMessage(new TextComponentString(replyMessage));
                    break;
                case 1:
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "You do not have permission to execute this command"));
                    break;
                case 2:
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error, bad argument"));
                    break;
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
