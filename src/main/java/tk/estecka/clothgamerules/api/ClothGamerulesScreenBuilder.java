package tk.estecka.clothgamerules.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextDescriptionBuilder;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRules;
import tk.estecka.clothgamerules.IRuleCategory;
import tk.estecka.clothgamerules.RuleEntry;

public final class ClothGamerulesScreenBuilder
{
	static private final FeatureSet ALL_FEATURES = FeatureFlags.FEATURE_MANAGER.getFeatureSet();
	static public final Text DEFAULT_TITLE = Text.translatable("editGamerule.title");

	private GameRules rules = new GameRules(ALL_FEATURES);
	private GameRules resetValues = new GameRules(ALL_FEATURES);
	private Screen parent = null;
	private Text title = DEFAULT_TITLE;
	private Consumer<Optional<GameRules>> onClosed = (_0)->{};

	private final Map<String, GameRules> displayValues = new LinkedHashMap<>();
	{
		displayValues.put("editGamerule.default", new GameRules(ALL_FEATURES));
	}


/******************************************************************************/
/* # Builder config                                                           */
/******************************************************************************/

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
		this.rules = activeValues;
		return this;
	}

	public ClothGamerulesScreenBuilder ResetValues(GameRules resetValues){
		this.resetValues = resetValues.withEnabledFeatures(ALL_FEATURES);
		return this;
	}

	public ClothGamerulesScreenBuilder DisplayValues(String translationKey, @Nullable GameRules values){
		if (values == null)
			this.displayValues.remove(translationKey);
		else
			this.displayValues.put(translationKey, values.withEnabledFeatures(ALL_FEATURES));
		return this;
	}


/******************************************************************************/
/* # Building process                                                         */
/******************************************************************************/

	static private final Text WILDCARD_TITLE = Text.translatable("cloth-gamerules.wildcardTab").formatted(Formatting.YELLOW);
	static private final Text MISSING_WIDGET = Text.empty().formatted(Formatting.RED)
		.append("(")
		.append(Text.translatable("cloth-gamerules.missing_widget"))
		.append(")")
		;

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
		Map<Identifier, GameRuleCategory> vanillaCats = new HashMap<>();

		builder.setParentScreen(parent);
		builder.setTitle(title);
		builder.setSavingRunnable(() -> onClosed.accept(Optional.of(rules)));

		rules.streamRules()
		.sorted(Comparator.comparing(GameRule::getId))
		.forEach(key->{
			IRuleCategory cat = GetCategory(key);
			Identifier catId = cat.GetId();

			vanillaCats.computeIfAbsent(catId, __-> key.getCategory());

			// var sub = subs.computeIfAbsent(catId, id -> entries.startSubCategory(cat.GetTitle()));
			var sub = subs.computeIfAbsent(catId, id -> new CategoryEntries(entries, cat));

			RuleEntry<?> ruleEntry = new RuleEntry<>(rules, resetValues, key);
			var field = StartRuleField(entries, ruleEntry);
			AbstractConfigListEntry<?> entry = (field != null) ? field.build() : StartMissingType(entries, key).build();
			sub.entries.add(entry);

			List<String> searchTags = new ArrayList<String>();
			searchTags.add(key.getTranslationKey());
			searchTags.add(I18n.translate(key.getTranslationKey()));
			entry.appendSearchTags(searchTags);
			sub.header.appendSearchTags(searchTags);
		});

		var sortedSubs =  subs.entrySet().stream().sorted((a,b)->{
			Identifier idA=a.getKey(), idB=b.getKey();
			boolean isAVanilla, isBVanilla;
			isAVanilla = a.getKey().getNamespace().equals("minecraft");
			isBVanilla = b.getKey().getNamespace().equals("minecraft");

			// Sort vanilla categories above modded ones.
			if (isAVanilla != isBVanilla)
				return -Boolean.compare(isAVanilla, isBVanilla);
			// // Sort vanilla rules in the same order as the vanilla screen.
			// // @deprecated Newer categories are sorted alphabetically.
			// else if (isAVanilla && isBVanilla)
			// 	return vanillaCats.get(idA).compareTo(vanillaCats.get(idB));
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

	static private IRuleCategory GetCategory(GameRule<?> key){
		var custom = CustomGameRuleCategory.getCategory(key);
		if (custom.isPresent())
			return IRuleCategory.Of(custom.get());
		else
			return IRuleCategory.Of(key.getCategory());
	}

	private Optional<Text[]> CreateTooltip(GameRule<?> key){
		ArrayList<Text> tooltip = new ArrayList<>(4);
		String descKey = key.getTranslationKey()+".description";

		tooltip.add(Text.literal(key.getId().toShortString()).formatted(Formatting.YELLOW));
		if (I18n.hasTranslation(descKey))
			tooltip.add(Text.translatable(descKey));

		for (var entry : this.displayValues.entrySet()){
			tooltip.add(
				Text.translatable(entry.getKey(), entry.getValue().getRuleValueName(key))
				.formatted(Formatting.GRAY)
			);
		}

		return Optional.of(tooltip.toArray(new Text[1]));
	}


/******************************************************************************/
/* # Field Builders                                                           */
/******************************************************************************/

	private <T> AbstractFieldBuilder<?,?,?>	StartRuleField(ConfigEntryBuilder entryBuilder, RuleEntry<T> entry) {
		Object ruleType = entry.GetType();

		AbstractFieldBuilder<?,?,?> field = switch (ruleType) {
			case GameRuleType.BOOL       -> StartBoolField(entryBuilder, (RuleEntry<Boolean>)entry);
			case FabricGameRuleType.ENUM -> StartEnumField(entryBuilder, (RuleEntry<Enum>)entry);
			default -> StartSringField(entryBuilder, entry);
		};

		if (field != null)
			field.setTooltipSupplier(() -> CreateTooltip(entry.key()));

		return field;
	}

	private AbstractFieldBuilder<?,?,?> StartBoolField(ConfigEntryBuilder entryBuilder, RuleEntry<Boolean> entry) {
		return entryBuilder.startBooleanToggle(entry.GetDisplayName(), entry.GetValue())
			.setSaveConsumer(entry::SetValue)
			.setErrorSupplier(entry::ErrorProvider)
			.setDefaultValue(entry.GetReset())
			;
	}

	private <T extends Enum<T>> AbstractFieldBuilder<?,?,?> StartEnumField(ConfigEntryBuilder entryBuilder, RuleEntry<T> entry) {
		Class<T> clazz = entry.key().getDefaultValue().getDeclaringClass();
		return entryBuilder.startEnumSelector(entry.GetDisplayName(), clazz, entry.GetValue())
			.setSaveConsumer(entry::SetValue)
			.setErrorSupplier(entry::ErrorProvider)
			.setDefaultValue(entry.GetReset())
			;
	}

	private <T> AbstractFieldBuilder<?,?,?> StartSringField(ConfigEntryBuilder entryBuilder, RuleEntry<T> entry) {
		return entryBuilder.startStrField(entry.GetDisplayName(), entry.GetStringValue())
			.setSaveConsumer(entry::SetStringValue)
			.setErrorSupplier(entry::StringErrorProvider)
			.setDefaultValue(entry.GetStringReset())
			;
	}

	@Deprecated
	private TextDescriptionBuilder	StartMissingType(ConfigEntryBuilder entryBuilder, GameRule<?> key){
		Text text = Text.translatable(key.getTranslationKey()).formatted(Formatting.GRAY)
			.append(" ")
			.append(MISSING_WIDGET)
			;

		var entry = entryBuilder.startTextDescription(text);
		entry.setTooltipSupplier(() -> CreateTooltip(key));
		return entry;
	}

}
