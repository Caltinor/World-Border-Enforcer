package dicemc.worldborderenforcer;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec SERVER_CONFIG;
	
	static {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

		setupServer(SERVER_BUILDER);

		SERVER_CONFIG = SERVER_BUILDER.build();
	}
	
	public static ForgeConfigSpec.IntValue MAX_RANGE;
	public static ForgeConfigSpec.DoubleValue GROW_INTERVAL;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> GROW_LEVELS;	
	
	private static void setupServer(ForgeConfigSpec.Builder builder) {
		builder.comment("World Border Enforcer configuration settings").push("Settings");
		
		MAX_RANGE = builder.comment("What is the maximum number of blocks, in diameter, the world border should grow to?")
				.defineInRange("Max Border Size", 5000, 1, 1874999);
		GROW_INTERVAL = builder.comment("how many hours should elapse between expansions")
				.defineInRange("Grow Interval", 1d, 0d, Integer.MAX_VALUE);
		GROW_LEVELS = builder.comment("The amount of blocks the border grows by is defined in this setting.",
						"The number on the right is how many blocks the border will grow for ",
						"the current size setting.  The number on the left is the size setting.","",
						"As your border grows, this map is scanned to find the highest left value",
						"that is below the current world border size.  The growth value paired with ",
						"this left value is what is used.  ", "",
						"for example if we have values ['0:10.0', '30:20.0', '1000:1.0'] and we start the",
						"border at 10, the border will grow 10 blocks each interval.  once it passes",
						"30 though, we have a new 'highest value' of 30, which means our border now",
						"grows by 20 each interval.  This repeats until our border is 1000, at which",
						"point the border grows one block per interval","",
						"Note: you do not need to order your map for this to work properly")
				.defineList("Growth_Rates", new ArrayList<>(List.of("0:32", "500:16", "2000:1")), s -> s instanceof String);
		
		builder.pop();
	}
}
