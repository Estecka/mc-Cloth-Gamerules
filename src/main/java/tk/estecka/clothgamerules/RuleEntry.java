package tk.estecka.clothgamerules;

import java.util.Optional;
import com.mojang.serialization.DataResult;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.text.Text;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;

public record RuleEntry<T>(
	GameRules values,
	GameRules reset,
	GameRule<T> key
){
	public Text GetDisplayName(){ return Text.translatable(key.getTranslationKey()); }

	public T GetValue()  { return values.getValue(key); }
	public T GetReset()  { return reset.getValue(key);    }
	public T GetDefault(){ return key.getDefaultValue();  }

	public String GetStringValue()  { return key.getValueName(GetValue());   };
	public String GetStringReset()  { return key.getValueName(GetReset());   };
	public String GetStringDefault(){ return key.getValueName(GetDefault()); };


	public void SetValue(T value) {
		this.SetStringValue(key.getValueName(value));
	}
	public void SetStringValue(String value){
		DataResult<T> result = key.deserialize(value);
		if (result.isSuccess())
			values.setValue(key, result.getOrThrow(), null);
		else {
			ClothRulesMod.LOGGER.error(
				"Could not set Gamerule {} to \"{}\":\n{}",
				key.getId(), value, result.error().get().message()
			);
		}
	}

	public Optional<Text> ErrorProvider(T value){
		return this.StringErrorProvider(key.getValueName(value));
	}
	public Optional<Text> StringErrorProvider(String value){
		Text error = null;
		DataResult<T> result = key.deserialize(value);
		if (result.isError())
			error = Text.literal(result.error().get().message());

		return Optional.ofNullable(error);
	}

	public Object GetType (){
		FabricGameRuleType fabric = ((RuleTypeExtensions)(Object)key).fabric_getType();
		if (fabric != null)
			return fabric;
		else
			return key.getType();
	}

}
