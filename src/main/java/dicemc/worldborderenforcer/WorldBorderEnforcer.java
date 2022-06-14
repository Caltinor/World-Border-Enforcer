package dicemc.worldborderenforcer;

import dicemc.worldborderenforcer.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(WorldBorderEnforcer.MODID)
public class WorldBorderEnforcer{
    public static final String MODID = "worldborderenforcer";
    
    public WorldBorderEnforcer(){
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.DANGER_CONFIG, "WorldBorderEnforcer-DangerZone.toml");
    	
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    private static final long ONE_HOUR = 72000;
    
    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event) {
    	server = event.getServer();
    	long baseInterval = (int)((double)ONE_HOUR * Config.GROW_INTERVAL.get());
    	nextHourToCheck = baseInterval + server.overworld().getGameTime() - (server.overworld().getGameTime() % baseInterval);
    	WorldBorder border = event.getServer().overworld().getWorldBorder();
    	Config.WORLD_SPAWN_X.set(border.getCenterX());
    	Config.WORLD_SPAWN_Z.set(border.getCenterZ());
    	if (Config.WORLD_BORDER_CURRENT.get() == 0)
    		Config.WORLD_BORDER_CURRENT.set(border.getSize());
    	else
    		border.setSize(Config.WORLD_BORDER_CURRENT.get());
    }
    
    MinecraftServer server = null;
    long nextHourToCheck = 0;
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
    	if (server.overworld().getGameTime() >= nextHourToCheck) {
    		nextHourToCheck += (int)((double)ONE_HOUR * Config.GROW_INTERVAL.get());
    		double currentSize = server.overworld().getWorldBorder().getSize();
    		if (currentSize >= Config.MAX_RANGE.get()) 
    			return;
    		double growth = getRateForCurrentRange();
    		server.overworld().getWorldBorder().setSize(currentSize + growth);
    		server.getPlayerList().getPlayers().forEach(player -> {
    			player.sendSystemMessage(Component.translatable("worldborderenforcer.chat.border_grow", growth));
    		});
    	}
    }
    
    private double getRateForCurrentRange() {
    	double currentRange = server.overworld().getWorldBorder().getSize();
    	double currentLevel = Config.GROW_LEVELS.get().keySet().stream().filter(value -> value <= currentRange).max(Double::compare).get();
    	return Config.GROW_LEVELS.get().get(currentLevel);
    }

}
