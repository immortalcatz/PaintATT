package eladkay.paintatt.asm;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class PaintAttAsmHooks {
    public static boolean isFullCube(Block block, IBlockState state) {
        return block.isFullCube(state);
    }
}
