package tk.estecka.clothgamerules;

import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.world.rule.GameRuleType;

public enum ETypeToken {
	STRING (null),
	BOOL   (GameRuleType.BOOL),
	INT    (GameRuleType.INT),
	ENUM   (FabricGameRuleType.ENUM),
	DOUBLE (FabricGameRuleType.DOUBLE),
	;

	public final Object identity;
	private ETypeToken(Object identity){
		this.identity = identity;
	}
}
