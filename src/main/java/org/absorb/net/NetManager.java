package org.absorb.net;

import org.absorb.net.handler.NetHandler;
import org.absorb.net.packet.IncomingPacket;
import org.absorb.net.packet.IncomingPacketBuilder;
import org.absorb.net.packet.PacketState;
import org.absorb.net.packet.login.handshake.IncomingHandshakePacketBuilder;
import org.absorb.net.packet.login.pre.IncomingPreLoginPacketBuilder;
import org.absorb.net.packet.play.channel.incoming.IncomingPluginMessagePacketBuilder;
import org.absorb.net.packet.play.client.inventory.close.IncomingCloseInventoryPacketBuilder;
import org.absorb.net.packet.play.client.inventory.creative.IncomingCreativeInventoryClickPacketBuilder;
import org.absorb.net.packet.play.entity.player.abilities.IncomingChangeAbilityPacketBuilder;
import org.absorb.net.packet.play.entity.player.movement.incoming.IncomingPlayerMovementPacketBuilder;
import org.absorb.net.packet.play.entity.player.movement.incoming.basic.IncomingBasicPlayerMovementPacketBuilder;
import org.absorb.net.packet.play.entity.player.movement.incoming.rotation.IncomingRotationPacketBuilder;
import org.absorb.net.packet.play.entity.player.teleport.confirm.IncomingTeleportConfirmPacketBuilder;
import org.absorb.net.packet.play.message.IncomingMessagePacketBuilder;
import org.absorb.net.packet.play.recipe.craft.IncomingRecipeRequestPacketBuilder;
import org.absorb.net.packet.play.settings.client.IncomingClientSettingsPacketBuilder;
import org.absorb.net.packet.status.ping.IncomingPingPacketBuilder;
import org.absorb.net.packet.status.request.IncomingStatusRequestPacketBuilder;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.*;
import java.util.function.Supplier;

public class NetManager {

    private final NetHandler handler;
    private final Map<SocketAddress, Client> info = new HashMap<>();
    private final Map<Map.Entry<Integer, PacketState>, Supplier<IncomingPacketBuilder<? extends IncomingPacket>>> packetBuilders =
            new HashMap<>();

    public NetManager(NetHandler handler) {
        this.handler = handler;
        this.init();
    }

    private void init() {
        this.registerPacketBuilders();
    }

    public void registerIncomingPacketBuilder(IncomingPacketBuilder<? extends IncomingPacket> builder) {
        Optional<Map.Entry<Integer, PacketState>> opKey =
                this
                        .packetBuilders
                        .keySet()
                        .stream()
                        .filter(entry -> entry.getValue()==builder.getState() && entry.getKey()==builder.getId())
                        .findAny();
        if (opKey.isPresent()) {
            this.packetBuilders.replace(opKey.get(), builder::copy);
            return;
        }
        this.packetBuilders.put(new AbstractMap.SimpleImmutableEntry<>(builder.getId(), builder.getState()), builder::copy);
    }

    private void registerPacketBuilders() {
        this.registerIncomingPacketBuilder(new IncomingHandshakePacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingPreLoginPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingStatusRequestPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingPingPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingPingPacketBuilder().setUsePlay(true));
        this.registerIncomingPacketBuilder(new IncomingClientSettingsPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingTeleportConfirmPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingRecipeRequestPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingPlayerMovementPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingPluginMessagePacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingBasicPlayerMovementPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingChangeAbilityPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingRotationPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingMessagePacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingCreativeInventoryClickPacketBuilder());
        this.registerIncomingPacketBuilder(new IncomingCloseInventoryPacketBuilder());
    }

    public NetHandler getHandler() {
        return this.handler;
    }

    public Optional<Client> getClient(SocketAddress address) {
        return Optional.ofNullable(this.info.get(address));
    }

    public Collection<Client> getClients() {
        return this.info.values();
    }

    public void register(Client info) throws IOException {
        this.info.put(info.getAddress(), info);
    }

    public Optional<IncomingPacketBuilder<? extends IncomingPacket>> getIncomingPacketBuilder(int networkId,
                                                                                              PacketState state) {
        return this
                .packetBuilders
                .entrySet()
                .parallelStream()
                .filter(entry -> entry.getKey().getKey()==networkId)
                .filter(entry -> entry.getKey().getValue()==state)
                .findAny()
                .map(entry -> entry.getValue().get());
    }

    public void unregister(Client info) {
        try {
            info.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.info.remove(info.getAddress());
    }
}
