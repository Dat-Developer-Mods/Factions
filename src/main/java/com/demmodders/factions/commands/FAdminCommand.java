package com.demmodders.factions.commands;

import com.demmodders.factions.Factions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FAdminCommand extends CommandBase {
    @Override
    public String getName() {
        return "factionadmin";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {

        }
        return "Only a player can use this commands";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        Factions.LOGGER.info(PermissionAPI.hasPermission((EntityPlayerMP) sender, "demfactions.admin.test"));
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
        return aliases;
    }
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
