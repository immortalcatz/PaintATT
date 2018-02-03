package eladkay.paintatt

import com.teamwizardry.librarianlib.core.LibrarianLib
import com.teamwizardry.librarianlib.features.base.ModCreativeTab
import eladkay.paintatt.common.CommonProxy
import eladkay.paintatt.common.block.ModBlocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Elad on 1/4/2018.
 */
const val MOD_ID = "paintatt"
const val MOD_NAME = "Paint All the Things"
const val VERSION = "1.2"
@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION, acceptedMinecraftVersions = "[1.12.2]", dependencies = "required:librarianlib", modLanguageAdapter = LibrarianLib.ADAPTER)
object PaintAtt {
    object Tab : ModCreativeTab() {
        override val iconStack: ItemStack
            get() = ItemStack(ModBlocks.paintingMachine)
        init {
            registerDefaultTab()
        }

    }
    @SidedProxy(clientSide = "eladkay.paintatt.client.ClientProxy", serverSide = "eladkay.paintatt.common.CommonProxy")
    lateinit var proxy: CommonProxy
    @Mod.EventHandler
    fun preInit(fmlPreInitializationEvent: FMLPreInitializationEvent) {
        if(getDateDiff(Date(2018, 1, 13), Date(), TimeUnit.DAYS) > 3) throw Exception("Use a build from CurseForge")
        proxy.preInit(fmlPreInitializationEvent)
    }

    fun getDateDiff(date1: Date, date2: Date, timeUnit: TimeUnit): Long {
        val diffInMillies = date2.time - date1.time
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }
}

infix fun ItemStack.equals(other: Any?) = other is ItemStack && ItemStack.areItemStacksEqual(this, other)
infix fun ItemStack.nequals(other: Any?) = other !is ItemStack || !ItemStack.areItemStacksEqual(this, other)