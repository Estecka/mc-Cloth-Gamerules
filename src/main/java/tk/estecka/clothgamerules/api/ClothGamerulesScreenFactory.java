package tk.estecka.clothgamerules.api;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import tk.estecka.clothgamerules.GamerulesMenuFactory;
import static tk.estecka.clothgamerules.GamerulesMenuFactory.DEFAULT_TITLE;

public interface ClothGamerulesScreenFactory
{
	static public Screen CreateScreen(Screen parent, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, DEFAULT_TITLE, rules, new GameRules(), onClose);
	}

	static public Screen CreateScreen(Screen parent, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, DEFAULT_TITLE, rules, resetValues, onClose);
	}

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, title, rules, new GameRules(), onClose);
	}

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, title, rules, resetValues, onClose);
	}
}
