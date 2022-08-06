package yan.lx.bedrockminer.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

public class BreakingFlowController {
    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();

    public static boolean isWorking() {
        return working;
    }

    private static boolean working = false;

    public static void addBlockPosToList(BlockPos pos, Direction dir) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (!world.getBlockState(pos).isAir()) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            String haveEnoughItems = InventoryManager.warningMessage();
            if (haveEnoughItems != null) {
                Messager.actionBar(haveEnoughItems);
                return;
            }
            if (shouldAddNewTargetBlock(pos)){
                TargetBlock targetBlock = new TargetBlock(pos, world, dir);
                cachedTargetBlockList.add(targetBlock);
                System.out.println("new task.");
            }
        } else {
            Messager.actionBar("Please make sure the block you hit is still a valid block.");
        }
    }

    public static void tick() {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;

        if (!"survival".equals(minecraftClient.interactionManager.getCurrentGameMode().getName())) {
            return;
        }

        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            TargetBlock selectedBlock = cachedTargetBlockList.get(i);

            //When the player switches worlds or is too far away from the target block, delete all cached tasks
            if (selectedBlock.getWorld() != MinecraftClient.getInstance().world ) {
                cachedTargetBlockList = new ArrayList<TargetBlock>();
                break;
            }

            if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 5f)) {
                TargetBlock.Status status = cachedTargetBlockList.get(i).tick();
                if (status == TargetBlock.Status.RETRACTING) {
                    continue;
                } else if (status == TargetBlock.Status.FAILED || status == TargetBlock.Status.RETRACTED) {
                    cachedTargetBlockList.remove(i);
                } else {
                    break;
                }

            }
        }
    }

    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return blockPos.isWithinDistance(player.getPos(), range);
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos){
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().getManhattanDistance(pos) == 0){
                return false;
            }
        }
        return true;
    }

    public static void switchOnOff(){
        if (working){
            Messager.chat("");
            Messager.chat("§5Miner-3000 stopped.§r");
            Messager.chat("");
            working = false;
        } else {
            Messager.chat("");
            Messager.chat("§5Miner-3000 started. Left click a block to break it.§r");
            Messager.chat("");
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            working = true;
        }
    }
}
