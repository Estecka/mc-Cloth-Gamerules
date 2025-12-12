package tk.estecka.clothgamerules;

import java.util.Optional;
import com.mojang.serialization.DataResult;
import net.minecraft.text.Text;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

public interface IRuleString
{
	boolean TryParse(String value);
	String GetValue();
	String GetReset();

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

	static public <T> IRuleString Of(GameRules instances, GameRules reset, GameRule<T> key) {
		return new IRuleString() {
			@Override public boolean TryParse(String value){
				DataResult<T> result = key.deserialize(value);
				if (result.isError())
					return false;
				else {
					instances.setValue(key, result.getOrThrow(), null);
					return true;
				}

			}
			@Override public String GetValue(){
				return instances.getRuleValueName(key);
			}
			@Override public String GetReset(){
				return instances.getRuleValueName(key);
			}

			@Override
			public Optional<Text> ErrorProvider(String value) {
				Text message = null;
				var result = key.deserialize(value);
				if (result.isError())
					message = Text.literal(result.error().get().message());

				return Optional.ofNullable(message);
			}
		};
	}
}
