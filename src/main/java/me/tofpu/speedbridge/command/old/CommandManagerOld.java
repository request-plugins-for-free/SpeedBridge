package me.tofpu.speedbridge.command.old;

import com.google.common.collect.Maps;
import me.tofpu.speedbridge.data.file.config.path.Path;
import me.tofpu.speedbridge.game.controller.GameController;
import me.tofpu.speedbridge.game.controller.stage.SetupStage;
import me.tofpu.speedbridge.game.result.Result;
import me.tofpu.speedbridge.game.service.IGameService;
import me.tofpu.speedbridge.island.mode.manager.ModeManager;
import me.tofpu.speedbridge.lobby.service.ILobbyService;
import me.tofpu.speedbridge.user.IUser;
import me.tofpu.speedbridge.user.properties.UserProperties;
import me.tofpu.speedbridge.user.service.IUserService;
import me.tofpu.speedbridge.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CommandManagerOld implements CommandExecutor {
    private final String[] commands = {""};
    private final GameController gameController;

    private final IUserService userService;
    private final IGameService gameService;
    private final ILobbyService lobbyService;

    public CommandManagerOld(final GameController gameController, final IUserService userService, final IGameService gameService, final ILobbyService lobbyService) {
        this.gameController = gameController;
        this.userService = userService;
        this.gameService = gameService;
        this.lobbyService = lobbyService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        final Player player = (Player) sender;

        if (args.length == 0) return false;
        switch (args[0]) {
            case "join":
                final boolean length = args.length > 1;

                Result joinResult;
                if (length) {
                    final Integer integer = Util.parseInt(args[1]);

                    if (integer != null) joinResult =
                            gameService.join(player, integer);
                    else
                        joinResult = gameService.join(
                                player,
                                ModeManager.getModeManager().get(args[1])
                        );
                } else joinResult = gameService.join(player);

                switch (joinResult) {
                    case SUCCESS:
                        Util.message(player, Path.MESSAGES_JOINED);
                        break;

                    case INVALID_LOBBY:
                        if (player.isOp()) {
                            Util.message(player, Path.MESSAGES_NO_LOBBY);
                            break;
                        }
                    case FULL:
                        Util.message(player, Path.MESSAGES_NOT_AVAILABLE);
                        break;
                    case DENY:
                        Util.message(player, Path.MESSAGES_ALREADY_JOINED);
                        break;
                }
                break;
            case "leave":
                if (!gameService.isPlaying(player)) {
                    Util.message(player, Path.MESSAGES_NOT_PLAYING);
                    return false;
                }
                gameService.leave(player);
                break;
            case "score":
                final Map<String, Double> map = Maps.newHashMap();
                final IUser user = userService.searchForUUID(player.getUniqueId());
                final UserProperties properties = user == null ? null : user.getProperties();

                map.put("%score%", user == null ? 0 : properties.getTimer() == null ? 0 : properties.getTimer().getResult());
                Util.message(player, Path.MESSAGES_YOUR_SCORE, map);
                break;
            case "leaderboard":
                player.sendMessage(lobbyService.getLeaderboard().printLeaderboard());
                break;
            case "create":
                if (args.length < 2) return false;
                if (gameService.isPlaying(player)) {
                    Util.message(player, Path.MESSAGES_CANNOT_EDIT);
                    return false;
                }

                final Integer createSlot = tryParse(args[1]);
                if (createSlot == null) {
                    Util.message(player, Path.MESSAGES_INSERT_NUMBER);
                    return false;
                }

                gameController.createIsland(player, createSlot);
                Util.message(player, Path.MESSAGES_ISLAND_CREATION);
                break;
            case "lobby":
                if (gameService.isPlaying(player)) {
                    gameService.leave(player);
                }
                if (lobbyService.hasLobbyLocation()) player.teleport(lobbyService.getLobbyLocation());
                break;
            case "set":
                if (gameService.isPlaying(player)) {
                    Util.message(player, Path.MESSAGES_CANNOT_EDIT);
                    return false;
                }
                if (args.length == 2) {
                    switch (args[1]) {
                        case "lobby":
                            Util.message(player, Path.MESSAGES_LOBBY_LOCATION);
                            lobbyService.setLobbyLocation(player.getLocation());
                            break;
                    }
                    return false;
                }
                // /speedbridge set (slot) spawn/point-a/point-b
                if (args.length < 3) return false;
                final Integer setSlot = tryParse(args[1]);
                if (setSlot == null) {
                    Util.message(player, Path.MESSAGES_INSERT_NUMBER);
                    return false;
                }

                switch (this.gameController.setupIsland(player, SetupStage.valueOf(args[2].toUpperCase().replace("-", "_")))) {
                    case SUCCESS:
                        Util.message(player, Path.MESSAGES_ISLAND_CREATION);
                        break;
                    case DENY:
                        Util.message(player, Path.MESSAGES_INVALID_ISLAND);
                        break;
                }
                break;
            case "finish":
                if (gameService.isPlaying(player)) {
                    Util.message(player, Path.MESSAGES_CANNOT_EDIT);
                    return false;
                }

                final Result finishResult = this.gameController.finishSetup(player);
                if (finishResult == Result.SUCCESS) {
                    Util.message(player, Path.MESSAGES_ISLAND_COMPLETED);
                } else if (finishResult == Result.DENY) {
                    Util.message(player, Path.MESSAGES_ISLAND_INCOMPLETE);
                }
                break;
        }
        return false;
    }

    public Integer tryParse(final String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}