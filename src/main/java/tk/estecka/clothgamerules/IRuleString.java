package tk.estecka.clothgamerules;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.IntRule;

public interface IRuleString
{
	boolean TryParse(String value);
	String GetValue();

	default String GetErrorString(){
		return "argument.enum.invalid";
	}

	default Optional<Text> ErrorProvider(String value){
		return this.TryParse(value) ?
			Optional.empty():
			Optional.of(Text.translatable(this.GetErrorString(), value));
	}


/******************************************************************************/
/* # Wrappers                                                                 */
/******************************************************************************/

	static public @Nullable IRuleString Of(GameRules.Rule<?> rule){
		if (rule instanceof IntRule    typedRule) return Of(typedRule);
		if (rule instanceof DoubleRule typedRule) return Of(typedRule);
		return null;
	}

	static public IRuleString Of(IntRule rule) {
		return new IRuleString() {
			@Override public boolean TryParse(String value){ return rule.validateAndSet(value); }
			@Override public String GetValue(){ return String.valueOf(rule.get()); }
			@Override public String GetErrorString() { return "parsing.int.invalid"; }
		};
	}
	
	static public IRuleString Of(DoubleRule rule) {
		return new IRuleString() {
			@Override public boolean TryParse(String value){ return rule.validate(value); }
			@Override public String GetValue(){ return String.valueOf(rule.get()); }
			@Override public String GetErrorString() { return "parsing.double.invalid"; }
		};
	}
}
