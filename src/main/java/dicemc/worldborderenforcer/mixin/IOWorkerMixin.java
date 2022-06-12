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

import dicemc.worldborderenforcer.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
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
			return pos.getMaxBlockX() >= Config.WORLD_SPAWN_X.get() - borderRadius()
				&& pos.getMinBlockX() <= Config.WORLD_SPAWN_X.get() + borderRadius()
				&& pos.getMaxBlockZ() >= Config.WORLD_SPAWN_Z.get() - borderRadius()
				&& pos.getMinBlockX() <= Config.WORLD_SPAWN_Z.get() + borderRadius();
		}
		
		private double borderRadius() {
			return Config.WORLD_BORDER_CURRENT.get() / 2d;
		}
		
	}
}