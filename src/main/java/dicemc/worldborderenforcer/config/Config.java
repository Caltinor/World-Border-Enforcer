package dicemc.worldborderenforcer.config;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

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
	public static ForgeConfigSpec.DoubleValue GROW_INTERVAL;
	public static ConfigObject<Map<Double, Double>> GROW_LEVELS;	
	
	private static Map<Double, Double> DEFAULT_GROW_LEVELS;
	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("World Border Enforcer configuration settings").push("Server Settings");
		
		MAX_RANGE = builder.comment("What is the maximum number of blocks, in diameter, the world border should grow to?")
				.defineInRange("Max Border Size", 5000, 1, 1874999);
		GROW_INTERVAL = builder.comment("how many hours should elapse between expansions")
				.defineInRange("Grow Interval", 1d, 0d, Integer.MAX_VALUE);
		GROW_LEVELS = TomlConfigHelper.defineObject(builder
				.comment("The amount of blocks the border grows by is defined in this setting.",
						"The number on the right is how many blocks the border will grow for ",
						"the current size setting.  The number on the left is the size setting.","",
						"As your border grows, this map is scanned to find the highest left value",
						"that is below the current world border size.  The growth value paird with ",
						"this left value is what is used.  ", "",
						"for example if we have values [0=10.0, 30=20.0, 1000=1.0] and we start the",
						"border at 10, the border will grow 10 blocks each interval.  once it passes",
						"30 though, we have a new 'highest value' of 30, which means our border now",
						"grows by 20 each interval.  This repeats until our border is 1000, at which",
						"point the border grows one block per interval","",
						"Note: you do not need to order your map for this to work properly"), 
				"Growth_Rates",	Codec.unboundedMap(
						Codec.STRING.flatXmap(str -> DataResult.success(Double.valueOf(str)), dbl -> DataResult.success(String.valueOf(dbl))), 
						Codec.DOUBLE), 
				DEFAULT_GROW_LEVELS);
		
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
