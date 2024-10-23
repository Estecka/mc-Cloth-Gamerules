package fr.estecka.clothgamerules.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import fr.estecka.clothgamerules.IRuleCategory;
import fr.estecka.clothgamerules.IRuleString;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextDescriptionBuilder;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.*;

public final class ClothGamerulesScreenBuilder
{
	static public final Text DEFAULT_TITLE = Text.translatable("editGamerule.title");
	static private final Text WILDCARD_TITLE = Text.translatable("cloth-gamerules.wildcardTab").formatted(Formatting.YELLOW);
	static private final Text MISSING_WIDGET = Text.empty().formatted(Formatting.RED)
		.append("(")
		.append(Text.translatable("cloth-gamerules.missing_widget"))
		.append(")")
		;

	private final FeatureSet features;
	private final Map<String, GameRules> displayValues = new LinkedHashMap<>();
	private GameRules rules;
	private GameRules resetValues;
	private Screen parent = null;
	private Text title = DEFAULT_TITLE;
	private Consumer<Optional<GameRules>> onClosed = (_0)->{};

	public ClothGamerulesScreenBuilder(FeatureSet features){
		this.features = features;
		this.rules = new GameRules(features);
		this.resetValues = new GameRules(features);
		this.displayValues.put("editGamerule.default", new GameRules(features));
	}

	public ClothGamerulesScreenBuilder(){
		this(FeatureSet.empty());
	}

	public ClothGamerulesScreenBuilder Parent(Screen parent) {
		this.parent = parent;
		return this;
	}

	public ClothGamerulesScreenBuilder Title(Text title) {
		this.title = title;
		return this;
	}

	public ClothGamerulesScreenBuilder OnClosed(Consumer<Optional<GameRules>> onClosed) {
		this.onClosed = onClosed;
		return this;
	}

	public ClothGamerulesScreenBuilder ActiveValues(GameRules activeValues) {
		this.rules = activeValues.copy(this.features);
		return this;
	}

	public ClothGamerulesScreenBuilder ResetValues(GameRules resetValues){
		this.resetValues = resetValues.copy(this.features);
		return this;
	}

	public ClothGamerulesScreenBuilder DisplayValues(String translationKey, GameRules values){
		this.displayValues.put(translationKey, values.copy(this.features));
		return this;
	}

	/**
	 * https://github.com/shedaniel/cloth-config/issues/245
	 * Cloth's subcategories don't  seem to work well with the search bar. Until
	 * this is sorted out, I'm mocking them up with text descriptions.
	 */
	static private class CategoryEntries {
		public final TextListEntry header;
		public final List<AbstractConfigListEntry<?>> entries = new ArrayList<>();

		CategoryEntries(ConfigEntryBuilder entryBuilder, IRuleCategory cat){
			this.header = entryBuilder.startTextDescription(cat.GetTitle()).build();
		}
	};

	public Screen Build(){
		final ConfigBuilder builder = ConfigBuilder.create();
		final ConfigEntryBuilder entries = builder.entryBuilder();

		// Map<Identifier, SubCategoryBuilder> subs = new HashMap<>();
		Map<Identifier, CategoryEntries> subs = new HashMap<>();
		Map<Identifier, GameRules.Category> vanillaCats = new HashMap<>();

		builder.setParentScreen(parent);
		builder.setTitle(title);
		builder.setSavingRunnable(() -> onClosed.accept(Optional.of(rules)));

		rules.accept(new GameRules.Visitor() {
			@Override public <T extends Rule<T>> void visit(Key<T> key, Type<T> type){
				IRuleCategory cat = GetCategory(key);
				Identifier catId = cat.GetId();

				vanillaCats.computeIfAbsent(catId, __-> key.getCategory());

				// var sub = subs.computeIfAbsent(catId, id -> entries.startSubCategory(cat.GetTitle()));
				var sub = subs.computeIfAbsent(catId, id -> new CategoryEntries(entries, cat));

				var field = StartRuleField(entries, key, type, rules.get(key), resetValues.get(key));
				AbstractConfigListEntry<?> entry = (field != null) ? field.build() : StartMissingType(entries, key, type).build();
				sub.entries.add(entry);

				List<String> searchTags = new ArrayList<String>();
				searchTags.add(key.getTranslationKey());
				searchTags.add(I18n.translate(key.getTranslationKey()));
				entry.appendSearchTags(searchTags);
				sub.header.appendSearchTags(searchTags);
			}
		});

		var sortedSubs =  subs.entrySet().stream().sorted((a,b)->{
			Identifier idA=a.getKey(), idB=b.getKey();
			boolean mA, mB;
			mA = a.getKey().getNamespace().equals("minecraft");
			mB = b.getKey().getNamespace().equals("minecraft");

			// Sort vanilla categories above modded ones.
			if (mA != mB)
				return -Boolean.compare(mA, mB);
			// Sort vanilla rules in the same order as the vanilla screen.
			else if (mA && mB)
				return vanillaCats.get(idA).compareTo(vanillaCats.get(idB));
			else {
				int diff = idA.getNamespace().compareTo(idB.getNamespace());
				if (diff != 0)
					return diff;
				else
					return idA.getPath().compareTo(idB.getPath());
			}
		});

		Map<String, ConfigCategory> tabs = new HashMap<>();
		final var wildcard = builder.getOrCreateCategory(WILDCARD_TITLE);
		for (var entry : sortedSubs.toList()) {
			Identifier id = entry.getKey();
			// SubCategoryBuilder sub = entry.getValue();
			CategoryEntries sub = entry.getValue();
			ConfigCategory tab = tabs.computeIfAbsent(id.getNamespace(), ns -> builder.getOrCreateCategory(Text.literal(ns)));

			// sub.setExpanded(true);
			// tab.addEntry(sub.build());
			wildcard.addEntry(sub.header);
			tab.addEntry(sub.header);
			sub.entries.forEach(e -> {tab.addEntry(e); wildcard.addEntry(e);});
		}

		builder.setFallbackCategory(wildcard);
		return builder.build();
	}

	static private IRuleCategory GetCategory(Key<?> key){
		var custom = CustomGameRuleCategory.getCategory(key);
		if (custom.isPresent())
			return IRuleCategory.Of(custom.get());
		else
			return IRuleCategory.Of(key.getCategory());
	}

	private Optional<Text[]>	CreateTooltip(Key<?> key, Type<?> type){
		ArrayList<Text> tooltip = new ArrayList<>(4);
		String descKey = key.getTranslationKey()+".description";

		tooltip.add(Text.literal(key.getName()).formatted(Formatting.YELLOW));
		if (I18n.hasTranslation(descKey))
			tooltip.add(Text.translatable(descKey));

		for (var entry : this.displayValues.entrySet()){
			tooltip.add(
				Text.translatable(entry.getKey(),entry.getValue().get(key).serialize())
				.formatted(Formatting.GRAY)
			);
		}

		return Optional.of(tooltip.toArray(new Text[1]));
	}

	private <T extends Rule<T>> @Nullable AbstractFieldBuilder<?,?,?>	StartRuleField(ConfigEntryBuilder entryBuilder, Key<T> key, Type<T> type, Rule<T> rule, Rule<T> resetValue) {
		AbstractFieldBuilder<?,?,?> field = null;

		String nameKey = key.getTranslationKey();
		Text displayName = Text.translatable(nameKey);

		IRuleString validateableRule = IRuleString.Of(rule);
		if (validateableRule != null){
			field = entryBuilder.startStrField(displayName, validateableRule.GetValue())
				.setSaveConsumer(s -> validateableRule.TryParse(nameKey))
				.setErrorSupplier(validateableRule::ErrorProvider)
				.setDefaultValue(resetValue.serialize())
				;
		}
		else if (rule instanceof BooleanRule boolRule){
			field = entryBuilder.startBooleanToggle(displayName, boolRule.get())
				.setSaveConsumer(b -> boolRule.set(b, null))
				.setDefaultValue(((BooleanRule)resetValue).get())
				;
		}
		else if (rule instanceof EnumRule enumRule){
			@SuppressWarnings("unchecked")
			Class<Enum<?>> enumClass = enumRule.getEnumClass();
			field = entryBuilder.startEnumSelector(displayName, enumClass, enumRule.get())
				.setSaveConsumer(e -> enumRule.set(e, null))
				.setErrorSupplier(e -> enumRule.supports(e) ? Optional.empty() : Optional.of(Text.translatable("argument.enum.invalid", e.toString())))
				.setDefaultValue(((EnumRule<?>)resetValue).get())
				;
		}

		if (field != null)
			field.setTooltipSupplier(() -> CreateTooltip(key, type));

		return field;
	}

	private TextDescriptionBuilder	StartMissingType(ConfigEntryBuilder entryBuilder, Key<?> key, Type<?> type){
		Text text = Text.translatable(key.getTranslationKey()).formatted(Formatting.GRAY)
			.append(" ")
			.append(MISSING_WIDGET)
			;

		var entry = entryBuilder.startTextDescription(text);
		entry.setTooltipSupplier(() -> CreateTooltip(key, type));
		return entry;
	}
}
