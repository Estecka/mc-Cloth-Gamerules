package tk.estecka.clothgamerules.api;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import fr.estecka.clothgamerules.api.ClothGamerulesScreenBuilder;

/**
 * @deprecated Use {@link fr.estecka.clothgamerules.api.ClothGamerulesScreenBuilder} instead.
 * This API  assumes  all  experimental  features  are  disabled, and  may cause
 * crashes  with the new  Minecart Experiment. Adding  more overload  with extra
 * parameters is no longer sustainable.
 */
@Deprecated(forRemoval=true)
public interface ClothGamerulesScreenFactory
{

	static public Screen CreateScreen(Screen parent, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).ActiveValues(rules).OnClosed(onClose).Build();
	}

	static public Screen CreateScreen(Screen parent, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).ActiveValues(rules).ResetValues(resetValues).OnClosed(onClose).Build();
	}

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).Title(title).ActiveValues(rules).OnClosed(onClose).Build();
	}

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return new ClothGamerulesScreenBuilder().Parent(parent).Title(title).ActiveValues(rules).ResetValues(resetValues).OnClosed(onClose).Build();
	}
}
