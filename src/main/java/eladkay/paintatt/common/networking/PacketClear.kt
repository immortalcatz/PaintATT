package eladkay.paintatt.common.networking

import com.teamwizardry.librarianlib.features.chunkdata.ChunkData
import com.teamwizardry.librarianlib.features.network.PacketBase
import com.teamwizardry.librarianlib.features.saving.Save
import eladkay.paintatt.common.ChunkDataPaintAtt
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

/**
 * Created by Elad on 1/6/2018.
 */
class PacketClear : PacketBase { // Must extend PacketBase

    @Save
    private lateinit var pos: BlockPos
    constructor() {
    }

    constructor(pos: BlockPos) {
        this.pos = pos
    }
    override fun handle(ctx: MessageContext) {
        if (ctx.side.isServer) return
        if (Minecraft.getMinecraft().player == null) return
        val world = Minecraft.getMinecraft().player.world
        val data = ChunkData.get(world, ChunkPos(pos), ChunkDataPaintAtt::class.java)!!
        data.paintData.clear()
        data.markDirty()
    }
}