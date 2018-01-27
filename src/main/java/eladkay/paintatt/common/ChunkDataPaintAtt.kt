package eladkay.paintatt.common

import com.teamwizardry.librarianlib.features.chunkdata.ChunkData
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import java.util.*

/**
 * Created by Elad on 1/13/2018.
 */
class ChunkDataPaintAtt(chunk: Chunk) : ChunkData(chunk) {
    @Save
    var paintData = HashMap<BlockPos, NBTTagCompound>()
}