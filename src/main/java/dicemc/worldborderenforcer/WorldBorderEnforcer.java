package dicemc.worldborderenforcer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod(WorldBorderEnforcer.MODID)
public class WorldBorderEnforcer{
    public static final String MODID = "worldborderenforcer";
    
    public WorldBorderEnforcer(){
    	ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);
    	
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    private static final long ONE_HOUR = 72000;
    
    @SubscribeEvent
    public void onServerPreStart(ServerAboutToStartEvent event) {
    	server = event.getServer();
    }
    
    @SubscribeEvent
    public void onServerStart(ServerStartingEvent event) {
    	long baseInterval = (int)((double)ONE_HOUR * Config.GROW_INTERVAL.get());
    	nextHourToCheck = baseInterval + server.overworld().getGameTime() - (server.overworld().getGameTime() % baseInterval);
    	mapConfigToMap();
    }
    
    public static MinecraftServer server = null;
    long nextHourToCheck = 0;
    Map<Double, Double> growthRates;
    
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
    	double currentLevel = growthRates.keySet().stream().filter(value -> value <= currentRange).max(Double::compare).get();
    	return growthRates.get(currentLevel);
    }
    
    @SubscribeEvent
	public void onConfigReload(ModConfigEvent.Reloading event) {
    	if (event.getConfig().getFileName().equalsIgnoreCase(MODID+"-server.toml"))
    		mapConfigToMap();
    }

    private void mapConfigToMap() {
    	growthRates = new HashMap<>();
    	Config.GROW_LEVELS.get().forEach(str -> {
    		int commaIndex = str.indexOf(":");
    		double key = Double.valueOf(str.substring(0, commaIndex));
    		double value = Double.valueOf(str.substring(commaIndex+1, str.length()));
    		System.out.println(key+"|"+value);
    		growthRates.put(key, value);
    	});
    }
}
