package eladkay.paintatt.common.block

import com.teamwizardry.librarianlib.features.base.block.tile.BlockModContainer
import com.teamwizardry.librarianlib.features.base.block.tile.TileModInventoryTickable
import com.teamwizardry.librarianlib.features.container.ContainerBase
import com.teamwizardry.librarianlib.features.container.GuiHandler
import com.teamwizardry.librarianlib.features.container.InventoryWrapper
import com.teamwizardry.librarianlib.features.container.SlotType
import com.teamwizardry.librarianlib.features.container.builtin.BaseWrappers
import com.teamwizardry.librarianlib.features.container.internal.SlotBase
import com.teamwizardry.librarianlib.features.helpers.ItemNBTHelper
import com.teamwizardry.librarianlib.features.kotlin.getTileEntitySafely
import com.teamwizardry.librarianlib.features.kotlin.isNotEmpty
import com.teamwizardry.librarianlib.features.kotlin.nbt
import com.teamwizardry.librarianlib.features.saving.Save
import eladkay.paintatt.MOD_ID
import eladkay.paintatt.PaintAtt
import eladkay.paintatt.client.gui.GuiPaintingMachine
import net.minecraft.block.Block
import net.minecraft.block.BlockFence
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Created by Elad on 1/4/2018.
 */
class BlockPaintingMachine : BlockModContainer("painting_machine", Material.IRON) {
    init {
        setHardness(2f)
    }
    override fun createTileEntity(world: World, state: IBlockState): TileEntity? {
        return TilePaintingMachine()
    }

    override fun onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        GuiHandler.open(PaintingMachineContainer.name, playerIn, pos)
        return true
    }

    override fun breakBlock(worldIn: World, pos: BlockPos, state: IBlockState) {
        val te = worldIn.getTileEntitySafely(pos) as? TilePaintingMachine ?: return
        spawnAsEntity(worldIn, pos, te.getStackInSlot(TilePaintingMachine.input))
        spawnAsEntity(worldIn, pos, te.getStackInSlot(TilePaintingMachine.output))
        spawnAsEntity(worldIn, pos, te.getStackInSlot(TilePaintingMachine.ghost))
        super.breakBlock(worldIn, pos, state)

    }

//    override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState?, fortune: Int) {
//        super.getDrops(drops, world, pos, state, fortune)
//        val te = world.getTileEntitySafely(pos) as? TilePaintingMachine ?: return
//        drops.add(te.getStackInSlot(TilePaintingMachine.input))
//        drops.add(te.getStackInSlot(TilePaintingMachine.output))
//        drops.add(te.getStackInSlot(TilePaintingMachine.ghost))
//
//    }



    companion object {

        fun itemAndNbtEqual(itemStack: ItemStack, other: ItemStack): Boolean
                = itemStack.isItemEqual(other) &&
                ItemNBTHelper.getCompound(itemStack, PaintAtt.proxy.nbtTagPaint)?.getString(PaintAtt.proxy.nbtTagPaintBlock) ==
                        ItemNBTHelper.getCompound(other, PaintAtt.proxy.nbtTagPaint)?.getString(PaintAtt.proxy.nbtTagPaintBlock)
        fun getItemStackForPaintedBlock(iBlockState: Block, painted: Block): ItemStack {
            val stack = ItemStack(iBlockState, 1)
            ItemNBTHelper.setCompound(stack, "display", nbt { comp() } as NBTTagCompound)
            ItemNBTHelper.setCompound(stack, PaintAtt.proxy.nbtTagPaint, nbt { comp(PaintAtt.proxy.nbtTagPaintBlock to painted.registryName!!)} as NBTTagCompound)
            return stack
        }
    }



    //    @TileRegister("painting_machine") its bork
    class TilePaintingMachine : TileModInventoryTickable(3) {
        override fun tick() {
            if(isInputValid() && isGhostValid()) {
                if(progress == 0f) {
                    this.module.handler.extractItem(input, 1, false)
                    this.module.handler.insertItem(output,
                            getItemStackForPaintedBlock(getBlockFromItem(module.handler.getStackInSlot(input).item), getBlockFromItem(module.handler.getStackInSlot(ghost).item)),
                            false)

                    progress = maxProgress
                } else progress--
            } else progress = maxProgress


        }

        fun isInputValid(): Boolean {
            val paintedStack = getItemStackForPaintedBlock(getBlockFromItem(module.handler.getStackInSlot(input).item), getBlockFromItem(module.handler.getStackInSlot(ghost).item))
            val input = this.module.handler.getStackInSlot(input)
            val ghost = this.module.handler.getStackInSlot(ghost)
            return input.isNotEmpty &&
                    input.item is ItemBlock &&
                    input.item != ghost.item &&
                    this.module.handler.insertItem(output, paintedStack, true).isEmpty

        }
        fun isGhostValid(): Boolean {
            return this.module.handler.getStackInSlot(ghost).isNotEmpty &&
                    module.handler.getStackInSlot(ghost).item is ItemBlock
        }
        companion object {
            val input = 0
            val ghost = 1
            val output = 2
            val maxProgress = 20f
        }

        @Save
        var progress: Float = 0f
    }

    class PaintingMachineContainer(player: EntityPlayer, te: TilePaintingMachine) : ContainerBase(player) {

        val invPlayer = BaseWrappers.player(player)
        val invBlock = PaintingMachineWrapper(te)

        init {
            addSlots(invPlayer)
            addSlots(invBlock)

            transferRule().from(invPlayer.main).from(invPlayer.hotbar).deposit(invBlock.input).deposit(invBlock.ghost)
            transferRule().from(invBlock.input).from(invBlock.output).from(invBlock.ghost).deposit(invPlayer.main).deposit(invPlayer.hotbar)
        }

        companion object {
            val name = ResourceLocation(MOD_ID, "painting_machine")

            init {
                GuiHandler.registerBasicContainer(name, { player, _, tile -> PaintingMachineContainer(player, tile as TilePaintingMachine) }, { _, container -> GuiPaintingMachine(container) })
            }
        }
    }
    class PaintingMachineWrapper(inventory: TilePaintingMachine) : InventoryWrapper(inventory) {
        val input = slots[TilePaintingMachine.input]
        val output = slots[TilePaintingMachine.output]
        val ghost = slots[TilePaintingMachine.ghost]
        init {
            ghost.type = object : SlotType() {
                override fun stackLimit(slot: SlotBase, stack: ItemStack): Int {
                    return 1
                }

                override fun isValid(slot: SlotBase, stack: ItemStack, default: Boolean): Boolean {
                    return stack.item is ItemBlock && (stack.item as ItemBlock).block.javaClass !in cantBeGhost && (stack.item as ItemBlock).block !in cantBeGhostOrInput  // blocks you can't paint as or can't paint
                }

            }

            input.type = object : SlotType() {

                override fun isValid(slot: SlotBase, stack: ItemStack, default: Boolean): Boolean {
                    return stack.item is ItemBlock && (stack.item as ItemBlock).block !in cantBeGhostOrInput // blocks you can't paint as
                }
            }
        }
    }
}
val cantBeGhost = listOf<Class<out Block>>(BlockFence::class.java)
val cantBeGhostOrInput = listOf<Block>(Blocks.CHEST, Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.MOB_SPAWNER, Blocks.MOB_SPAWNER, Blocks.PISTON,
        Blocks.STICKY_PISTON, Blocks.PISTON_HEAD, Blocks.PISTON_EXTENSION, Blocks.ENDER_CHEST, Blocks.ENCHANTING_TABLE, Blocks.END_PORTAL,
        Blocks.BEACON, Blocks.SKULL, Blocks.STANDING_BANNER, Blocks.BED)
