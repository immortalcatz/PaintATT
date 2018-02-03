package eladkay.paintatt.common

import com.teamwizardry.librarianlib.features.chunkdata.ChunkData
import com.teamwizardry.librarianlib.features.chunkdata.ChunkDataRegistry
import com.teamwizardry.librarianlib.features.helpers.ItemNBTHelper
import com.teamwizardry.librarianlib.features.kotlin.localize
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.network.PacketHandler
import com.teamwizardry.librarianlib.features.network.TargetAll
import com.teamwizardry.librarianlib.features.network.get
import eladkay.paintatt.MOD_ID
import eladkay.paintatt.PaintAtt
import eladkay.paintatt.common.block.BlockPaintingMachine
import eladkay.paintatt.common.block.ModBlocks
import eladkay.paintatt.common.networking.PacketClear
import eladkay.paintatt.common.networking.PacketPaintSync
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.event.FMLInterModComms
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side


/**
 * Created by Elad on 1/4/2018.
 * Todo: tab, waila
 */
open class CommonProxy {
    open fun preInit(fmlPreInitializationEvent: FMLPreInitializationEvent) {
        ModBlocks
        GameRegistry.registerTileEntity(BlockPaintingMachine.TilePaintingMachine::class.java, "$MOD_ID:painting_machine")
        MinecraftForge.EVENT_BUS.register(this)
        PacketHandler.register(PacketPaintSync::class.java, Side.CLIENT)
        PacketHandler.register(PacketClear::class.java, Side.CLIENT)
        ChunkDataRegistry.register("$MOD_ID:paintatt".toRl(), ChunkDataPaintAtt::class.java, ::ChunkDataPaintAtt, { true })
        PaintAtt.Tab
        FMLInterModComms.sendMessage("waila", "register", "eladkay.paintatt.common.CompatWaila.onWailaCall");
//        object : ItemMod("tester") {
//            override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand?): ActionResult<ItemStack> {
//                val data = ChunkData.get(worldIn, ChunkPos(playerIn.position), ChunkDataPaintAtt::class.java)!!
//                playerIn.sendSpamlessMessage(data.paintData.toString(), "atttest")
//                return super.onItemRightClick(worldIn, playerIn, handIn)
//            }
//        }
    }

    val nbtTagPaint = "paintatt_paint"
    val nbtTagPaintBlock = "block"
    val nbtTagPaintMeta = "meta"

    //give @p minecraft:stone 1 0 {paintatt_paint:{block:"minecraft:ice"}}
    @SubscribeEvent
    fun blockPlaceEvent(event: BlockEvent.PlaceEvent) {
        val tag = ItemNBTHelper.getCompound(event.itemInHand, nbtTagPaint) ?: return
        val container = ChunkData.get(event.world, ChunkPos(event.pos), ChunkDataPaintAtt::class.java)!!//getTrueSaveData(event.world)
        val nbt = nbt { comp(nbtTagPaintBlock to tag.getString(nbtTagPaintBlock), nbtTagPaintMeta to tag.getInteger(nbtTagPaintMeta)) }
        container.paintData.put(event.pos, nbt as NBTTagCompound)
        container.markDirty()
        // TODO: packet clear and then packets for every paint block
        PacketHandler[MOD_ID].send(TargetAll, PacketClear(event.pos))
        for((pos, nbtTag) in container.paintData)
            PacketHandler[MOD_ID].send(TargetAll, PacketPaintSync(pos, nbtTag))
        //event.world.scheduleBlockUpdate(event.pos, event.placedBlock.block, 1, 1)
//        event.world.neighborChanged(event.pos, event.placedBlock.block, event.pos)
    }

    @SubscribeEvent
    fun blockBreakEvent(event: BlockEvent.BreakEvent) {
        val container = ChunkData.get(event.world, ChunkPos(event.pos), ChunkDataPaintAtt::class.java) ?: return
        if(event.player.isCreative && event.pos in container.paintData.keys) {
            container.paintData.remove(event.pos)
            container.markDirty()
            PacketHandler[MOD_ID].send(TargetAll, PacketClear(event.pos))
            for((pos, nbtTag) in container.paintData)
                PacketHandler[MOD_ID].send(TargetAll, PacketPaintSync(pos, nbtTag))
            Thread {
                Thread.sleep(1000)
                event.world.markBlockRangeForRenderUpdate(event.pos, event.pos)
            }.start()


        }
    }
    @SubscribeEvent
    fun blockHarvestEvent(event: BlockEvent.HarvestDropsEvent) {
        val container = ChunkData.get(event.world, ChunkPos(event.pos), ChunkDataPaintAtt::class.java) ?: return
        if(!container.paintData.containsKey(event.pos)) return

        val nbt = container.paintData[event.pos] ?: return
        container.paintData.remove(event.pos)
        container.markDirty()
        PacketHandler[MOD_ID].send(TargetAll, PacketClear(event.pos))
        for((pos, nbtTag) in container.paintData)
            PacketHandler[MOD_ID].send(TargetAll, PacketPaintSync(pos, nbtTag))
        val tag = nbt.getString(nbtTagPaintBlock)
        val meta = nbt.getInteger(nbtTagPaintMeta)
        event.drops.clear()
        event.drops.add(BlockPaintingMachine.getItemStackForPaintedBlock(event.state.block, Block.REGISTRY.getObject(ResourceLocation(tag)), meta, event.state.block.getMetaFromState(event.state)))
    }



    @SubscribeEvent
    fun tooltip(event: ItemTooltipEvent) {
        val tagg = ItemNBTHelper.getCompound(event.itemStack, nbtTagPaint) ?: return
        val tag = tagg.getString(nbtTagPaintBlock)
        val meta = tagg.getInteger(nbtTagPaintMeta)
        val stack = ItemStack(Block.REGISTRY.getObject(tag.toRl()), 1, meta);
        event.toolTip[0] = "${(event.itemStack.unlocalizedName + ".name").localize()} [Painted as ${stack.displayName}]"
        event.toolTip.add("Painted as " + stack.displayName)
    }
}