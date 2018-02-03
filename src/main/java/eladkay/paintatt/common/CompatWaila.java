package eladkay.paintatt.common;

import com.teamwizardry.librarianlib.features.chunkdata.ChunkData;
import eladkay.paintatt.PaintAtt;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;
import java.util.List;

public class CompatWaila {
    public static void onWailaCall(IWailaRegistrar registrar) {
//        registrar.registerStackProvider(new PaintedBlock(), Block.class);
        registrar.registerHeadProvider(new PaintedBlock(), Block.class);
    }

    private static class PaintedBlock implements IWailaDataProvider {
//        @Nonnull
//        @Override
//        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
//            ChunkDataPaintAtt data = ChunkData.get(accessor.getWorld(), new ChunkPos(accessor.getPosition()), ChunkDataPaintAtt.class);
//            if(data == null) return accessor.getStack();
//            NBTTagCompound tag = data.getPaintData().get(accessor.getPosition());
//            if(tag == null) return accessor.getStack();
//            ResourceLocation rl = new ResourceLocation(tag.getString(PaintAtt.proxy.getNbtTagPaintBlock()));
//            int meta = tag.getInteger(PaintAtt.proxy.getNbtTagPaintMeta());
//            return new ItemStack(Block.REGISTRY.getObject(rl), 1, meta);
//        }
//
//        @Nonnull
//        @Override
//        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//            ChunkDataPaintAtt data = ChunkData.get(accessor.getWorld(), new ChunkPos(accessor.getPosition()), ChunkDataPaintAtt.class);
//            if(data == null) return currenttip;
//            NBTTagCompound tag = data.getPaintData().get(accessor.getPosition());
//            if(tag == null) return currenttip;
//            ResourceLocation rl = new ResourceLocation(tag.getString(PaintAtt.proxy.getNbtTagPaintBlock()));
//            int meta = tag.getInteger(PaintAtt.proxy.getNbtTagPaintMeta());
//            ItemStack stack = new ItemStack(Block.REGISTRY.getObject(rl), 1, meta);
//            currenttip.add(new ItemStack(accessor.getBlock(), 1, accessor.getMetadata()).getDisplayName() + " painted as " + stack.getDisplayName());
//            return currenttip;
//        }

        @Nonnull
        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
            ChunkDataPaintAtt data = ChunkData.get(accessor.getWorld(), new ChunkPos(accessor.getPosition()), ChunkDataPaintAtt.class);
            if(data == null) return currenttip;
            NBTTagCompound tag = data.getPaintData().get(accessor.getPosition());
            if(tag == null) return currenttip;
            ResourceLocation rl = new ResourceLocation(tag.getString(PaintAtt.proxy.getNbtTagPaintBlock()));
            int meta = tag.getInteger(PaintAtt.proxy.getNbtTagPaintMeta());
            ItemStack stack = new ItemStack(Block.REGISTRY.getObject(rl), 1, meta);
            currenttip.add("Painted as " + stack.getDisplayName());
            return currenttip;
        }
    }
}
