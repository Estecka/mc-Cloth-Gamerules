package fr.estecka.clothgamerules.api;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import fr.estecka.clothgamerules.GamerulesMenuFactory;

public class ClothGamerulesScreenBuilder
{
	private final FeatureSet features;
	private GameRules activeRules;
	private GameRules resetRules;

	private Screen parent = null;
	private Text title = GamerulesMenuFactory.DEFAULT_TITLE;
	private Consumer<Optional<GameRules>> onClosed = (_0)->{};

	public ClothGamerulesScreenBuilder(FeatureSet features){
		this.features = features;
		this.activeRules = new GameRules(features);
		this.resetRules = new GameRules(features);
	}

	public ClothGamerulesScreenBuilder(){
		this(FeatureSet.empty());
	}

	public ClothGamerulesScreenBuilder Parent(Screen parent) { this.parent = parent; return this; }
	public ClothGamerulesScreenBuilder Title(Text title) { this.title = title; return this; }
	public ClothGamerulesScreenBuilder OnClosed(Consumer<Optional<GameRules>> onClosed) { this.onClosed = onClosed; return this; }

	public ClothGamerulesScreenBuilder ActiveValues(GameRules activeValues) {
		this.activeRules = activeValues.copy(this.features);
		return this;
	}
	public ClothGamerulesScreenBuilder ResetValues(GameRules resetValues){
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
