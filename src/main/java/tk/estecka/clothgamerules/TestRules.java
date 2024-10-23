package tk.estecka.clothgamerules;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TestRules
implements ModInitializer
{
	static public enum ETestEnum {
		NONE,
		SOME,
		ALL
	}

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment())
		{
			CustomGameRuleCategory category = new CustomGameRuleCategory(Identifier.of("cloth-gamerules", "test_rules"), Text.literal("Test Rules"));
			GameRuleRegistry.register("clothrule.bool",   category, GameRuleFactory.createBooleanRule(true));
			GameRuleRegistry.register("clothrule.int",    category, GameRuleFactory.createIntRule(1));
			GameRuleRegistry.register("clothrule.double", category, GameRuleFactory.createDoubleRule(1.0));
			GameRuleRegistry.register("clothrule.enum",   category, GameRuleFactory.createEnumRule(ETestEnum.SOME));
	
			GameRuleRegistry.register("clothrule.int.bounded",    category, GameRuleFactory.createIntRule(1, 0, 16));
			GameRuleRegistry.register("clothrule.double.bounded", category, GameRuleFactory.createDoubleRule(1.0, 0.1, 9.999));
			GameRuleRegistry.register("clothrule.enum.limited",   category, GameRuleFactory.createEnumRule(ETestEnum.SOME, new ETestEnum[]{ ETestEnum.NONE, ETestEnum.SOME }));
		}

	}
}
