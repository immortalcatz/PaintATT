package eladkay.paintatt.client

import com.teamwizardry.librarianlib.core.client.RenderHookHandler
import com.teamwizardry.librarianlib.features.chunkdata.ChunkData
import com.teamwizardry.librarianlib.features.helpers.ItemNBTHelper
import com.teamwizardry.librarianlib.features.kotlin.toRl
import com.teamwizardry.librarianlib.features.methodhandles.MethodHandleHelper
import eladkay.paintatt.common.ChunkDataPaintAtt
import eladkay.paintatt.common.CommonProxy
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.ChunkCache
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.lang.Float.floatToRawIntBits
import java.lang.Float.intBitsToFloat
/**
 * Created by Elad on 1/4/2018.
 */
class ClientProxy : CommonProxy() {
    private val ChunkCache.worldObj by MethodHandleHelper.delegateForReadOnly<ChunkCache, World>(ChunkCache::class.java, "world", "field_72815_e", "e")
    override fun preInit(fmlPreInitializationEvent: FMLPreInitializationEvent) {
        super.preInit(fmlPreInitializationEvent)
        RenderHookHandler.registerBlockHook { blockModelRenderer, world, model, state, pos, buffer ->
            val worldObj = when(world) {
                is World -> world
                is ChunkCache -> world.worldObj
                else -> world as? World ?: return@registerBlockHook
            }
            val container = ChunkData.get(worldObj, ChunkPos(pos), ChunkDataPaintAtt::class.java)!!//getTrueSaveData(worldObj)
            if(!container.paintData.containsKey(pos)) return@registerBlockHook
            val nbt = container.paintData[pos] ?: return@registerBlockHook
            val tag = ResourceLocation(nbt.getString(nbtTagPaintBlock))
            val meta = nbt.getInteger(nbtTagPaintMeta)
            val sprite = getSprite(tag, meta)//Minecraft.getMinecraft().textureMapBlocks.getAtlasSprite("${tag.resourceDomain}:blocks/${tag.resourcePath}")
            retextureModel(sprite, world, model, state, pos, buffer, true)

        }

        RenderHookHandler.registerItemHook { itemStack, iBakedModel ->
            val str = ItemNBTHelper.getCompound(itemStack, nbtTagPaint)?.getString(nbtTagPaintBlock) ?: return@registerItemHook
            val tag = str.toRl()
            val sprite = Minecraft.getMinecraft().textureMapBlocks.getAtlasSprite("${tag.resourceDomain}:blocks/${tag.resourcePath}")


        }

    }

    fun getSprite(name: ResourceLocation, meta: Int): TextureAtlasSprite {
        val iBlockState = Block.REGISTRY.getObject(name).getStateFromMeta(meta)
        val modelMgr = Minecraft.getMinecraft().blockRendererDispatcher.blockModelShapes.modelManager.blockModelShapes
        return modelMgr.getTexture(iBlockState)
    }

    @SideOnly(Side.CLIENT)
    private fun retextureModel(newTex: TextureAtlasSprite, world: IBlockAccess, model: IBakedModel, state: IBlockState, pos: BlockPos, buffer: BufferBuilder, checkSides: Boolean, rand: Long = MathHelper.getPositionRandom(pos)): Boolean {
        var total = 0

        var list = model.getQuads(state, null, rand)

        if (!list.isEmpty()) {
            retextureQuads(newTex, buffer, list, total)
            total += list.size
        }

        for (enumfacing in EnumFacing.values().reversed()) {
            list = model.getQuads(state, enumfacing, rand)

            if (!list.isEmpty() && (!checkSides || state.shouldSideBeRendered(world, pos, enumfacing))) {
                retextureQuads(newTex, buffer, list, total)
                total += list.size
            }
        }

        return total > 0
    }

    @SideOnly(Side.CLIENT)
    private fun retextureQuads(newTex: TextureAtlasSprite, buffer: BufferBuilder, list: List<BakedQuad>, total: Int) {
        val j = list.size
        val format = buffer.vertexFormat
        for (i in (j - 1) downTo 0) {
            val bakedquad = list[i]
            val shift = format.getUvOffsetById(0) / 4
            val truePos = (buffer.vertexCount - (total + j - i) * 4) * format.integerSize + shift
            val jShift = format.integerSize
            val buf = buffer.byteBuffer.asIntBuffer()

            val sprite = bakedquad.sprite

            for (vertexIndex in 0 until 4) {
                val oldU = buf[truePos + vertexIndex * jShift]
                val oldV = buf[truePos + vertexIndex * jShift + 1]

                val originalU = sprite.getUnInterpolatedU(intBitsToFloat(oldU))
                val originalV = sprite.getUnInterpolatedV(intBitsToFloat(oldV))

                val newU = newTex.getInterpolatedU(originalU.toDouble())
                val newV = newTex.getInterpolatedV(originalV.toDouble())

                buf.put(truePos + vertexIndex * jShift, floatToRawIntBits(newU))
                buf.put(truePos + vertexIndex * jShift + 1, floatToRawIntBits(newV))
            }
        }
    }

}