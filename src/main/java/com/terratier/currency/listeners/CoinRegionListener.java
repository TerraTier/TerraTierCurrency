package com.terratier.currency.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.terratier.currency.TerraTierCurrency;
import com.terratier.currency.currency.CurrencyIds;
import com.terratier.currency.managers.CurrencyManager;
import com.terratier.currency.utils.Utils;

public class CoinRegionListener implements Listener {
    private static final long MESSAGE_COOLDOWN_MS = 1500L;

    private final CurrencyManager currencyManager = CurrencyManager.getInstance();
    private final Map<UUID, Long> lastDenyMessage = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (TerraTierCurrency.REQUIRED_TOTAL_COINS_FLAG == null || !changedBlock(event.getFrom(), event.getTo())) {
            return;
        }

        Player player = event.getPlayer();
        Integer requiredTotalCoins = getRequiredTotalCoins(player, event.getTo());
        if (requiredTotalCoins == null || requiredTotalCoins <= 0) {
            return;
        }

        Integer previousRequiredTotalCoins = getRequiredTotalCoins(player, event.getFrom());
        double balance = currencyManager.getBalance(player.getUniqueId(), CurrencyIds.TOTAL_COINS);
        if (balance >= requiredTotalCoins || requiredTotalCoins.equals(previousRequiredTotalCoins)) {
            return;
        }

        event.setCancelled(true);
        sendDenyMessage(player, requiredTotalCoins, balance);
    }

    private boolean changedBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()
                || !from.getWorld().equals(to.getWorld());
    }

    private Integer getRequiredTotalCoins(Player player, Location location) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return regions.queryValue(WorldGuardPlugin.inst().wrapPlayer(player), TerraTierCurrency.REQUIRED_TOTAL_COINS_FLAG);
    }

    private void sendDenyMessage(Player player, int requiredTotalCoins, double balance) {
        long now = System.currentTimeMillis();
        long lastMessage = lastDenyMessage.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastMessage < MESSAGE_COOLDOWN_MS) {
            return;
        }

        lastDenyMessage.put(player.getUniqueId(), now);
        player.sendMessage(Utils.colorize("<red>You need at least <white>" + requiredTotalCoins
                + " total coins <red>to enter this area. You have <white>" + String.format("%.2f", balance)
                + "<red>."));
    }
}
