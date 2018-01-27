package eladkay.paintatt.common.block

import com.teamwizardry.librarianlib.core.common.RecipeGeneratorHandler
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

/**
 * Created by Elad on 1/4/2018.
 */
object ModBlocks {
    val paintingMachine: BlockPaintingMachine = BlockPaintingMachine()
    init {
        RecipeGeneratorHandler.addShapedRecipe("painting_machine", ItemStack(paintingMachine),
                ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE),
                ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), ItemStack(Blocks.CONCRETE, 1, 0), ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE),
                ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE), ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE))
    }
}