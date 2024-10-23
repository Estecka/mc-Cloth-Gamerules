package tk.estecka.clothgamerules.api;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import tk.estecka.clothgamerules.GamerulesMenuFactory;
import static tk.estecka.clothgamerules.GamerulesMenuFactory.DEFAULT_TITLE;

public interface ClothGamerulesScreenFactory
{
	public class Builder
	{
		private final FeatureSet features;
		private GameRules activeRules;
		private GameRules resetRules;

		private Screen parent = null;
		private Text title = GamerulesMenuFactory.DEFAULT_TITLE;
		private Consumer<Optional<GameRules>> onClosed = (_0)->{};

		public Builder(FeatureSet features){
			this.features = features;
			this.activeRules = new GameRules(features);
			this.resetRules = new GameRules(features);
		}

		public Builder(){
			this(FeatureSet.empty());
		}

		public Builder Parent(Screen parent) { this.parent = parent; return this; }
		public Builder Title(Text title) { this.title = title; return this; }
		public Builder OnClosed(Consumer<Optional<GameRules>> onClosed) { this.onClosed = onClosed; return this; }

		public Builder ActiveValues(GameRules activeValues) {
			this.activeRules = activeValues.copy(this.features);
			return this;
		}
		public Builder ResetValues(GameRules resetValues){
			this.resetRules = resetValues.copy(this.features);
			return this;
		}

		public Screen Build(){
			return GamerulesMenuFactory.CreateScreen(
				parent,
				title,
				activeRules,
				resetRules,
				onClosed
			);
		}
	}

	static public Screen CreateScreen(Screen parent, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, DEFAULT_TITLE, rules, new GameRules(FeatureSet.empty()), onClose);
	}

	static public Screen CreateScreen(Screen parent, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, DEFAULT_TITLE, rules, resetValues, onClose);
	}

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, title, rules, new GameRules(FeatureSet.empty()), onClose);
	}

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		return GamerulesMenuFactory.CreateScreen(parent, title, rules, resetValues, onClose);
	}
}
