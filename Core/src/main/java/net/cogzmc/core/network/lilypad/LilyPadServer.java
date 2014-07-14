package net.cogzmc.core.network.lilypad;

import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.request.impl.RedirectRequest;
import lombok.Data;
import lombok.SneakyThrows;
import net.cogzmc.core.Core;
import net.cogzmc.core.network.NetCommand;
import net.cogzmc.core.network.NetworkServer;
import net.cogzmc.core.player.COfflinePlayer;
import net.cogzmc.core.player.CPlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.cogzmc.core.network.lilypad.LilyPadNetworkManager.encodeNetCommand;

@Data
final class LilyPadServer implements NetworkServer {
    private final String name;
    private final Integer maximumPlayers;
    private final LilyPadNetworkManager networkManager;
    private Date lastPing = new Date();
    private List<COfflinePlayer> players = new ArrayList<>();

    @Override
    @SneakyThrows
    public void sendPlayerToServer(CPlayer player) {
        if (!networkManager.getConnect().isConnected()) {
            Core.getInstance().getLogger().severe("LILYPAD CONNECT IS NOT CONNECTED TO THE CLOUD. Unable to send player to the server.");
            return;
        }
        networkManager.getConnect().request(new RedirectRequest(name, player.getName()));
    }

    @Override
    public Integer getOnlineCount() {
        return players.size();
    }

    @Override
    @SneakyThrows
    public void sendNetCommand(NetCommand command) {
        if (!networkManager.getConnect().isConnected()) {
            Core.getInstance().getLogger().severe("LILYPAD CONNECT IS NOT CONNECTED TO THE CLOUD. Unable to send a netcommand.");
            return;
        }
        networkManager.getConnect().request(new MessageRequest(name, LilyPadNetworkManager.NET_COMMAND_CHANNEL, encodeNetCommand(command).toJSONString()));
    }
}
