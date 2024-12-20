package com.chromanyan.chunkulator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Chunkulator.MODID)
public class Chunkulator {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "chunkulator";

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        private static Map<Block, Integer> calculateChunkBlocks(LocalPlayer player) {
            ClientLevel level = player.clientLevel;
            BlockPos initialPos = player.chunkPosition().getWorldPosition();

            Map<Block, Integer> blockMap = new HashMap<>();

            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                for (int x = initialPos.getX(); x < initialPos.getX() + 16; x++) {
                    for (int z = initialPos.getZ(); z < initialPos.getZ() + 16; z++) {
                        BlockPos curPos = new BlockPos(x, y, z);
                        Block curBlock = level.getBlockState(curPos).getBlock();

                        if (blockMap.containsKey(curBlock)) {
                            blockMap.compute(curBlock, (k, count) -> count + 1);
                        } else {
                            blockMap.put(curBlock, 1);
                        }
                    }
                }
            }

            return blockMap;
        }

        private static void beginChunkulation() {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null) return;

            if (player.getPermissionLevel() < 2) {
                player.sendSystemMessage(Component.translatable("message.chunkulator.nopermission"));
                return;
            }

            Map<Block, Integer> blockMap = calculateChunkBlocks(player);
            player.sendSystemMessage(Component.translatable("message.chunkulator.complete"));

            int total = 0;

            for (Map.Entry<Block, Integer> entry : blockMap.entrySet()) {
                Block block = entry.getKey();
                int count = entry.getValue();

                player.sendSystemMessage(Component.translatable("message.chunkulator.blockentry", block.getName(), count));

                if (block.defaultDestroyTime() < 0 || block.equals(Blocks.AIR)) continue;

                total += count;
            }

            player.sendSystemMessage(Component.translatable("message.chunkulator.blocktotal", total));
        }

        @SubscribeEvent
        public static void onClientChat(ClientChatEvent event) {
            if (!event.getMessage().equalsIgnoreCase("&chunkulator") && !event.getMessage().equalsIgnoreCase("&chunkulate")) return;

            beginChunkulation();
            event.setCanceled(true);
        }
    }
}
