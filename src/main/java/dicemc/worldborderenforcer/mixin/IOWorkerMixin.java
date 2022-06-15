package dicemc.worldborderenforcer.mixin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dicemc.worldborderenforcer.WorldBorderEnforcer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.storage.IOWorker;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;

@Mixin(IOWorker.class)
public abstract class IOWorkerMixin implements Consumer<RegionFileStorage>
{
	@Mutable
	@Accessor
	public abstract void setStorage(RegionFileStorage cache);
	
	@Override
	public void accept(RegionFileStorage cache)
	{
		this.setStorage(cache);
	}
	
	@Inject(method="<init>", at=@At("RETURN"))
	private void onConstruction(Path path, boolean sync, String threadName, CallbackInfo info)
	{
		this.accept(new ChunkGetterRegionFileStorage(path, sync));
	}
	
	private static class ChunkGetterRegionFileStorage extends RegionFileStorage {

		public ChunkGetterRegionFileStorage(Path path, boolean sync) {super(path, sync);}
		
		@Override
		protected void write(ChunkPos pos, CompoundTag compound) throws IOException{
			if (isChunkInBounds(pos))
			{
				super.write(pos, compound);
			}
		}

		@Override
		@Nullable
		public CompoundTag read(ChunkPos pos) throws IOException{
			if (isChunkInBounds(pos))
				return super.read(pos);			
			else
				return null;
		}
		
		private boolean isChunkInBounds(ChunkPos pos) {
			WorldBorder border = WorldBorderEnforcer.server.overworld().getWorldBorder();
			return pos.getMaxBlockX() >= border.getCenterX() - border.getSize()
				&& pos.getMinBlockX() <= border.getCenterX() + border.getSize()
				&& pos.getMaxBlockZ() >= border.getCenterZ() - border.getSize()
				&& pos.getMinBlockX() <= border.getCenterZ() + border.getSize();
		}		
	}
}