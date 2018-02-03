package eladkay.paintatt.client.gui

import com.teamwizardry.librarianlib.features.gui.Option
import com.teamwizardry.librarianlib.features.gui.component.GuiComponentEvents
import com.teamwizardry.librarianlib.features.gui.components.ComponentProgressBar
import com.teamwizardry.librarianlib.features.gui.components.ComponentSprite
import com.teamwizardry.librarianlib.features.gui.components.ComponentSpriteProgressBar
import com.teamwizardry.librarianlib.features.gui.components.ComponentText
import com.teamwizardry.librarianlib.features.guicontainer.ComponentSlot
import com.teamwizardry.librarianlib.features.guicontainer.GuiContainerBase
import com.teamwizardry.librarianlib.features.guicontainer.builtin.BaseLayouts
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.isNotEmpty
import com.teamwizardry.librarianlib.features.sprite.Texture
import eladkay.paintatt.MOD_ID
import eladkay.paintatt.common.block.BlockPaintingMachine
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

/**
 * Created by Elad on 1/4/2018.
 */
open class GuiPaintingMachine(inventorySlotsIn: BlockPaintingMachine.PaintingMachineContainer) : GuiContainerBase(inventorySlotsIn, 176, 166) {

    init {

        val te = inventorySlotsIn.invBlock.block
        val bg = ComponentSprite(BG, 0, 0)
        mainComponents.add(bg)

        val inventory = BaseLayouts.player(inventorySlotsIn.invPlayer)
        bg.add(inventory.root)
        inventory.main.pos = vec(8, 84)
        inventory.hotbar.pos = vec(8, 142)


        bg.add(ComponentSprite(SLOT, 50, 33))
        bg.add(ComponentSprite(SLOT, 100, 33))
        bg.add(ComponentSprite(SLOT, 75, 55))


        val input = ComponentSlot(inventorySlotsIn.invBlock.input, 51, 34)
        input.BUS.hook(GuiComponentEvents.MouseOverEvent::class.java) {
            event ->
            if(event.isOver && inventorySlotsIn.invBlock.input.stack.isNotEmpty)
                renderTooltip(inventorySlotsIn.invBlock.input.stack, event.mousePos.xi, event.mousePos.yi)
        }
        bg.add(input)

        val output = ComponentSlot(inventorySlotsIn.invBlock.output, 101, 34)
        output.BUS.hook(GuiComponentEvents.MouseOverEvent::class.java) {
            event ->
            if(event.isOver && inventorySlotsIn.invBlock.input.stack.isNotEmpty)
                renderTooltip(inventorySlotsIn.invBlock.input.stack, event.mousePos.xi, event.mousePos.yi)
        }
        bg.add(output)

        val ghost = ComponentSlot(inventorySlotsIn.invBlock.ghost, 76, 56)
        ghost.BUS.hook(GuiComponentEvents.MouseOverEvent::class.java) {
            event ->
            if(event.isOver && inventorySlotsIn.invBlock.input.stack.isNotEmpty)
                renderTooltip(inventorySlotsIn.invBlock.input.stack, event.mousePos.xi, event.mousePos.yi)
        }
        bg.add(ghost)

        val state = te.world.getBlockState(te.pos)
        bg.add(ComponentText(88, 6, horizontal = ComponentText.TextAlignH.CENTER).`val`(I18n.format(ItemStack(state.block, 1, state.block.damageDropped(state)).displayName)))

        val progressBar = ComponentProgressBar(PROGRESS_FG, PROGRESS_BG, 77, 37,
                direction = Option(ComponentSpriteProgressBar.ProgressDirection.X_POS),
                progress = Option(0f, { 1 - (te.progress / BlockPaintingMachine.TilePaintingMachine.maxProgress) }))


            progressBar.render.tooltip { listOf(I18n.format("$MOD_ID:gui.progress", (100 - (100 * te.progress / BlockPaintingMachine.TilePaintingMachine.maxProgress)).toInt())) }
        bg.add(progressBar)

    }

    fun renderTooltip(stack: ItemStack, x: Int, y: Int) {
        val font = stack.item.getFontRenderer(stack)
        net.minecraftforge.fml.client.config.GuiUtils.preItemToolTip(stack)
        this.drawHoveringText(this.getItemToolTip(stack), x, y, font ?: fontRenderer)
        net.minecraftforge.fml.client.config.GuiUtils.postItemToolTip()
    }
    companion object {
        private val TEX = Texture(ResourceLocation(MOD_ID, "textures/guis/gui_painting_machine.png"))
        private val BG = TEX.getSprite("bg", 176, 166)

        private val PROGRESS_BG = TEX.getSprite("progression_bg", 14, 10)
        private val PROGRESS_FG = TEX.getSprite("progression_fg", 12, 8)

        private val POWER_BG = TEX.getSprite("power_bg", 8, 56)
        private val POWER_FG = TEX.getSprite("power_fg", 8, 56)

        private val SLOT = TEX.getSprite("slot", 18, 18)
    }
}
