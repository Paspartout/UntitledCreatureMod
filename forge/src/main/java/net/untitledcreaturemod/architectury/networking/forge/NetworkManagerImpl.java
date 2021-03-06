/*
 * This file is part of architectury.
 * Copyright (C) 2020, 2021 architectury
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.untitledcreaturemod.architectury.networking.forge;


import com.google.common.collect.*;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import net.untitledcreaturemod.UntitledCreatureMod;
import net.untitledcreaturemod.architectury.networking.NetworkManager;
import net.untitledcreaturemod.architectury.networking.NetworkManager.NetworkReceiver;
import net.untitledcreaturemod.architectury.networking.NetworkManager.Side;
import net.untitledcreaturemod.architectury.utils.Env;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = UntitledCreatureMod.MOD_ID)
public class NetworkManagerImpl {
    public static void registerReceiver(Side side, Identifier id, NetworkReceiver receiver) {
        if (side == NetworkManager.Side.C2S) {
            registerC2SReceiver(id, receiver);
        } else if (side == NetworkManager.Side.S2C) {
            registerS2CReceiver(id, receiver);
        }
    }
    
    public static Packet<?> toPacket(NetworkManager.Side side, Identifier id, PacketByteBuf buffer) {
        PacketByteBuf packetBuffer = new PacketByteBuf(Unpooled.buffer());
        packetBuffer.writeIdentifier(id);
        packetBuffer.writeBytes(buffer);
        return (side == NetworkManager.Side.C2S ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT).buildPacket(Pair.of(packetBuffer, 0), CHANNEL_ID).getThis();
    }
    
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Identifier CHANNEL_ID = new Identifier("architectury:network");
    static final Identifier SYNC_IDS = new Identifier("architectury:sync_ids");
    static final EventNetworkChannel CHANNEL = NetworkRegistry.newEventChannel(CHANNEL_ID, () -> "1", version -> true, version -> true);
    static final Map<Identifier, NetworkReceiver> S2C = Maps.newHashMap();
    static final Map<Identifier, NetworkReceiver> C2S = Maps.newHashMap();
    static final Set<Identifier> serverReceivables = Sets.newHashSet();
    private static final Multimap<PlayerEntity, Identifier> clientReceivables = Multimaps.newMultimap(Maps.newHashMap(), Sets::newHashSet);
    
    static {
        CHANNEL.addListener(createPacketHandler(NetworkEvent.ClientCustomPayloadEvent.class, C2S));
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientNetworkingManager::initClient);
        
        registerC2SReceiver(SYNC_IDS, (buffer, context) -> {
            Set<Identifier> receivables = (Set<Identifier>) clientReceivables.get(context.getPlayer());
            int size = buffer.readInt();
            receivables.clear();
            for (int i = 0; i < size; i++) {
                receivables.add(buffer.readIdentifier());
            }
        });
    }
    
    static <T extends NetworkEvent> Consumer<T> createPacketHandler(Class<T> clazz, Map<Identifier, NetworkReceiver> map) {
        return event -> {
            if (event.getClass() != clazz) return;
            NetworkEvent.Context context = event.getSource().get();
            if (context.getPacketHandled()) return;
            PacketByteBuf buffer = event.getPayload();
            if (buffer == null) return;
            Identifier type = buffer.readIdentifier();
            NetworkReceiver receiver = map.get(type);
            
            if (receiver != null) {
                receiver.receive(buffer, new NetworkManager.PacketContext() {
                    @Override
                    public PlayerEntity getPlayer() {
                        return getEnvironment() == Env.CLIENT ? getClientPlayer() : context.getSender();
                    }
                    
                    @Override
                    public void queue(Runnable runnable) {
                        context.enqueueWork(runnable);
                    }
                    
                    @Override
                    public Env getEnvironment() {
                        return context.getDirection().getReceptionSide() == LogicalSide.CLIENT ? Env.CLIENT : Env.SERVER;
                    }
                    
                    private PlayerEntity getClientPlayer() {
                        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientNetworkingManager::getClientPlayer);
                    }
                });
            } else {
                LOGGER.error("Unknown message ID: " + type);
            }
            
            context.setPacketHandled(true);
        };
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void registerS2CReceiver(Identifier id, NetworkReceiver receiver) {
        S2C.put(id, receiver);
    }
    
    public static void registerC2SReceiver(Identifier id, NetworkReceiver receiver) {
        C2S.put(id, receiver);
    }
    
    public static boolean canServerReceive(Identifier id) {
        return serverReceivables.contains(id);
    }
    
    public static boolean canPlayerReceive(ServerPlayerEntity player, Identifier id) {
        return clientReceivables.get(player).contains(id);
    }
    
    public static Packet<?> createAddEntityPacket(Entity entity){
        return NetworkHooks.getEntitySpawningPacket(entity);
    }
    
    static PacketByteBuf sendSyncPacket(Map<Identifier, NetworkReceiver> map) {
        List<Identifier> availableIds = Lists.newArrayList(map.keySet());
        PacketByteBuf packetBuffer = new PacketByteBuf(Unpooled.buffer());
        packetBuffer.writeInt(availableIds.size());
        for (Identifier availableId : availableIds) {
            packetBuffer.writeIdentifier(availableId);
        }
        return packetBuffer;
    }
    
    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkManager.sendToPlayer((ServerPlayerEntity) event.getPlayer(), SYNC_IDS, sendSyncPacket(C2S));
    }
    
    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        clientReceivables.removeAll(event.getPlayer());
    }
}
