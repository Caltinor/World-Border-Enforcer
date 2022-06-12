package dicemc.worldborderenforcer.config;

import java.util.Map;

import com.mojang.serialization.Codec;

import dicemc.worldborderenforcer.config.TomlConfigHelper.ConfigObject;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec SERVER_CONFIG;
	public static ForgeConfigSpec DANGER_CONFIG;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder DANGER_BUILDER = new ForgeConfigSpec.Builder();

		DEFAULT_GROW_LEVELS = Map.of(
				//  range	rate
					0d, 	64d,
					5000d, 	32d
		);
		setupServer(SERVER_BUILDER);
		setupDanger(DANGER_BUILDER);

		SERVER_CONFIG = SERVER_BUILDER.build();
		DANGER_CONFIG = DANGER_BUILDER.build();
	}
	
	public static ForgeConfigSpec.IntValue MAX_RANGE;
	public static ForgeConfigSpec.IntValue GROW_INTERVAL;
	public static ConfigObject<Map<Double, Double>> GROW_LEVELS;	
	
	private static Map<Double, Double> DEFAULT_GROW_LEVELS;
	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("World Border Enforcer configuration settings").push("Server Settings");
		
		MAX_RANGE = builder.comment("What is the maximum number of chunks, IN RADIUS, the world border should grow to?")
				.defineInRange("Max Border Size", 312, 1, 1874999);
		GROW_INTERVAL = builder.comment("how many hours should elapse between expansions")
				.defineInRange("Grow Interval", 1, 0, Integer.MAX_VALUE);
		GROW_LEVELS = TomlConfigHelper.<Map<Double, Double>>defineObject(builder, "Growth_Rates", 
				Codec.unboundedMap(Codec.DOUBLE, Codec.DOUBLE), DEFAULT_GROW_LEVELS);
		
		builder.pop();
	}
	
	public static ForgeConfigSpec.ConfigValue<Double> WORLD_SPAWN_X;
	public static ForgeConfigSpec.ConfigValue<Double> WORLD_SPAWN_Z;
	public static ForgeConfigSpec.ConfigValue<Double> WORLD_BORDER_CURRENT;
	
	private static void setupDanger(ForgeConfigSpec.Builder builder) {
		builder.comment("Welcome to the danger zone!", "",
				"Okay but in all seriousness, be carefull with this file",
				"This config stores data relevant to the World Border Enforcer's",
				"current understanding of the world border settings.  If you",
				"modify this config and break it in the process, defaults will",
				"be used instead which may break your current border progression.", "",
				"That said, this is a config for a reason, and that is to let you",
				"make changes to the world border in config before the world actually",
				"loads so that you can tweak the enforcer's behavior to get the ",
				"chunk control behavior you desire. So proceend with caution").push("DANGER_ZONE");
		
		WORLD_SPAWN_X = builder.comment("The world spawn center X coordinate.  updates as you change spawnpos, is purely referential to the enforcer.")
				.define("WorldSpawnX", 0d);
		WORLD_SPAWN_Z = builder.comment("The world spawn center Z coordinate.  updates as you change spawnpos, is purely referential to the enforcer.")
				.define("WorldSpawnZ", 0d);
		WORLD_BORDER_CURRENT = builder.comment("This setting will update from the server automatically.",
				"Modifying it will override the current border.",
				"You can use this to reset the border range and",
				"reset all out-of-bounds chunks before the world loads")
				.define("CurrentBorder", 0d);		
		
		builder.pop();
	}
}
