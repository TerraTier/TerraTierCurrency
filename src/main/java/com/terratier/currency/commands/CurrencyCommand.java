package com.terratier.currency.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.terratier.currency.currency.Currency;
import com.terratier.currency.currency.CurrencyIds;
import com.terratier.currency.currency.CurrencyList;
import com.terratier.currency.managers.CurrencyManager;
import com.terratier.currency.utils.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Handles the /currency command and supports viewing or modifying currency balances.
 */
public class CurrencyCommand implements CommandExecutor, TabCompleter {
    private static final String DEFAULT_CURRENCY = CurrencyIds.COINS;

    private final CurrencyManager currencyManager = CurrencyManager.getInstance();
    private final CurrencyList currencyList = CurrencyList.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            return showBalance(player, player.getName());
        }

        String subcommand = args[0].toLowerCase();
        switch (subcommand) {
            case "add":
            case "subtract":
            case "set":
                return handleModifyCommand(player, label, args);
            case "view":
                return handleViewCommand(player, args);
            default:
                return showBalance(player, args[0]);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Arrays.stream(new String[] {"add", "subtract", "set", "view"})
                    .filter(s -> s.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("subtract") || sub.equals("set") || sub.equals("view")) {
                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(p -> p.getName())
                        .filter(n -> n.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("add") || sub.equals("subtract") || sub.equals("set")) {
                String prefix = args[2].toLowerCase();
                return currencyList.getAllCurrencies().keySet().stream()
                        .filter(k -> k.startsWith(prefix))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private boolean handleViewCommand(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Utils.colorize("<red>Usage: /currency view <player>"));
            return true;
        }
        return showBalance(sender, args[1]);
    }

    private boolean handleModifyCommand(Player sender, String label, String[] args) {
        if (!sender.hasPermission("terratier.currency.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 3) {
            return modifyBalance(sender, args[0], args[1], DEFAULT_CURRENCY, args[2]);
        }

        if (args.length == 4) {
            return modifyBalance(sender, args[0], args[1], args[2], args[3]);
        }

        sender.sendMessage(Utils.colorize("<red>Usage: /" + label + " <add|subtract|set> <player> [currency] <amount>"));
        return true;
    }

    private boolean modifyBalance(Player sender, String action, String targetName, String currencyId, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + targetName + "' not found!", NamedTextColor.RED));
            return true;
        }

        Currency currency = currencyList.getCurrency(currencyId);
        if (currency == null) {
            sender.sendMessage(Utils.colorize("<red>Currency '" + currencyId + "' not found."));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0 && !action.equalsIgnoreCase("set")) {
                sender.sendMessage(Component.text("Amount must be positive!", NamedTextColor.RED));
                return true;
            }
            if (amount < 0) {
                sender.sendMessage(Component.text("Amount cannot be negative!", NamedTextColor.RED));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid amount: " + amountStr, NamedTextColor.RED));
            return true;
        }

        if (action.equalsIgnoreCase("add")) {
            currencyManager.addBalance(target.getUniqueId(), currency.getId(), amount);
            sender.sendMessage(Utils.colorize("<green>Added " + amount + " " + currency.getDisplayName() + " to " + target.getName()));
            target.sendMessage(Utils.colorize("<green>You received " + amount + " " + currency.getDisplayName()));
            if (currency.getId().equals(CurrencyIds.COINS)) {
                target.sendMessage(Utils.colorize("<green>Your Total Coins increased by " + amount));
            }
        } else if (action.equalsIgnoreCase("set")) {
            currencyManager.setBalance(target.getUniqueId(), currency.getId(), amount);
            sender.sendMessage(Utils.colorize("<green>Set " + target.getName() + "'s " + currency.getDisplayName() + " to " + amount));
            target.sendMessage(Utils.colorize("<yellow>Your " + currency.getDisplayName() + " was set to " + amount));
        } else {
            if (currencyManager.subtractBalance(target.getUniqueId(), currency.getId(), amount)) {
                sender.sendMessage(Utils.colorize("<green>Removed " + amount + " " + currency.getDisplayName() + " from " + target.getName()));
                target.sendMessage(Utils.colorize("<red>You lost " + amount + " " + currency.getDisplayName()));
            } else {
                sender.sendMessage(Component.text(target.getName() + " doesn't have enough " + currency.getDisplayName() + "!", NamedTextColor.RED));
            }
        }

        return true;
    }

    private boolean showBalance(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Component.text("Player '" + targetName + "' not found!", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Utils.colorize("<yellow>Balances for <white>" + target.getName()));
        for (Map.Entry<String, Currency> entry : currencyList.getAllCurrencies().entrySet()) {
            Currency currency = entry.getValue();
            double balance = currencyManager.getBalance(target.getUniqueId(), currency.getId());
            String formatted = String.format("  <gray>%s: <white>%.2f %s", currency.getDisplayName(), balance, currency.getIcon());
            sender.sendMessage(Utils.colorize(formatted));
        }

        return true;
    }
}
