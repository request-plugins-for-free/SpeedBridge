package me.tofpu.speedbridge.listener;

import me.tofpu.speedbridge.game.service.IGameService;
import me.tofpu.speedbridge.island.IIsland;
import me.tofpu.speedbridge.island.properties.IslandProperties;
import me.tofpu.speedbridge.island.service.IIslandService;
import me.tofpu.speedbridge.user.IUser;
import me.tofpu.speedbridge.user.service.IUserService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    private final IUserService userService;
    private final IIslandService islandService;
    private final IGameService gameService;

    public PlayerInteractListener(final IUserService userService, final IIslandService islandService, final IGameService gameService) {
        this.userService = userService;
        this.islandService = islandService;
        this.gameService = gameService;
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.hasBlock() || event.getAction() != Action.PHYSICAL) return;

        final Player player = event.getPlayer();
        final IUser user;
        if ((user = userService.searchForUUID(player.getUniqueId())) == null) return;

        final IIsland island = islandService.getIslandBySlot(user.getProperties().getIslandSlot());
        final IslandProperties properties = island.getProperties();

        final Location pressurePlate = event.getClickedBlock().getLocation();
        if (isEqual(pressurePlate, properties.getLocationA())) {
            gameService.addTimer(user);
        } else if (gameService.hasTimer(user) && isEqual(pressurePlate, properties.getLocationB())) {
            gameService.updateTimer(player);
        }
    }

    public boolean isEqual(final Location a, final Location b) {
        return ((a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockY() == b.getBlockY()));
    }
}