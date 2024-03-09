package tk.estecka.clothgamerules;

import java.util.ArrayList;
import java.util.HashMap;
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
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextDescriptionBuilder;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.*;
import tk.estecka.preferredgamerules.IRuleFactory;

public class GamerulesMenuFactory
{
	static public final Text DEFAULT_TITLE = Text.translatable("editGamerule.title");
	static private final Text WILDCARD_TITLE = Text.translatable("cloth-gamerules.wildcardTab").formatted(Formatting.YELLOW);

	/**
	 * https://github.com/shedaniel/cloth-config/issues/245
	 * Cloth's subcategories don't  seem to work well with the search bar. Until
	 * this is sorted out, I'm mocking them up with text descriptions.
	 */
	static class CategoryEntries {
		public final TextListEntry header;
		public final List<AbstractConfigListEntry<?>> entries = new ArrayList<>();

		CategoryEntries(ConfigEntryBuilder entryBuilder, IRuleCategory cat){
			this.header = entryBuilder.startTextDescription(cat.GetTitle()).build();
		}
	};

	static public final Text MISSING_WIDGET = Text.empty().formatted(Formatting.RED)
		.append("(")
		.append(Text.translatable("cloth-gamerules.missing_widget"))
		.append(")")
		;

	static public Screen CreateScreen(Screen parent, Text title, GameRules rules, GameRules resetValues, Consumer<Optional<GameRules>> onClose){
		final ConfigBuilder builder = ConfigBuilder.create();
		final ConfigEntryBuilder entries = builder.entryBuilder();

		Map<String, ConfigCategory> tabs = new HashMap<>();
		// Map<Identifier, SubCategoryBuilder> subs = new HashMap<>();
		Map<Identifier, CategoryEntries> subs = new HashMap<>();

		builder.setParentScreen(parent);
		builder.setTitle(title);
		builder.setSavingRunnable(() -> onClose.accept(Optional.of(rules)));

		final var wildcard = builder.getOrCreateCategory(WILDCARD_TITLE);
		builder.setFallbackCategory(wildcard);

		GameRules.accept(new GameRules.Visitor() {
			@Override public <T extends Rule<T>> void visit(Key<T> key, Type<T> type){
				IRuleCategory cat = GetCategory(key);
				Identifier catId = cat.GetId();

				tabs.computeIfAbsent(catId.getNamespace(), ns -> builder.getOrCreateCategory(Text.literal(ns)));
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

			if (mA != mB) // Sort vanilla rules to the top.
				return -Boolean.compare(mA, mB);
			else {
				int diff = idA.getNamespace().compareTo(idB.getNamespace());
				if (diff != 0)
					return diff;
				else
					return idA.getPath().compareTo(idB.getPath());
			}
		});

		for (var entry : sortedSubs.toList()) {
			Identifier id = entry.getKey();
			ConfigCategory tab = tabs.get(id.getNamespace());
			// SubCategoryBuilder sub = entry.getValue();
			CategoryEntries sub = entry.getValue();

			// sub.setExpanded(true);
			// tab.addEntry(sub.build());
			wildcard.addEntry(sub.header);
			tab.addEntry(sub.header);
			sub.entries.forEach(e -> {tab.addEntry(e); wildcard.addEntry(e);});
		}

		return builder.build();
	}

	static public IRuleCategory GetCategory(Key<?> key){
		var custom = CustomGameRuleCategory.getCategory(key);
		if (custom.isPresent())
			return IRuleCategory.Of(custom.get());
		else
			return IRuleCategory.Of(key.getCategory());
	}

	static public Optional<Text[]>	CreateTooltip(Key<?> key, Type<?> type){
		ArrayList<Text> tooltip = new ArrayList<>(4);
		String descKey = key.getTranslationKey()+".description";

		tooltip.add(Text.literal(key.getName()).formatted(Formatting.YELLOW));
		if (I18n.hasTranslation(descKey))
			tooltip.add(Text.translatable(descKey));

		if (ClothGamerules.IS_PREFRULE_INSTALLED){
			var factory = IRuleFactory.Of(type);
			tooltip.add(Text.translatable("editGamerule.preferred", factory.preferredgamerules$CreatePreferredRule().serialize()).formatted(Formatting.GRAY));
			tooltip.add(Text.translatable("editGamerule.default",   factory.preferredgamerules$CreateDefaultRule  ().serialize()).formatted(Formatting.GRAY));
		}
		else
			tooltip.add(Text.translatable("editGamerule.default", type.createRule().serialize()).formatted(Formatting.GRAY));

		return Optional.of(tooltip.toArray(new Text[1]));
	}

	static public <T extends Rule<T>> @Nullable AbstractFieldBuilder<?,?,?>	StartRuleField(ConfigEntryBuilder entryBuilder, Key<T> key, Type<T> type, Rule<T> rule, Rule<T> resetValue) {
		AbstractFieldBuilder<?,?,?> field = null;

		String nameKey = key.getTranslationKey();
		Text displayName = Text.translatable(nameKey);

		IRuleString validateableRule = IRuleString.Of(rule);
		if (validateableRule != null){
			field = entryBuilder.startStrField(displayName, validateableRule.GetValue())
				.setSaveConsumer(s -> validateableRule.Validate(nameKey))
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

	static public TextDescriptionBuilder	StartMissingType(ConfigEntryBuilder entryBuilder, Key<?> key, Type<?> type){
		Text text = Text.translatable(key.getTranslationKey()).formatted(Formatting.GRAY)
			.append(" ")
			.append(MISSING_WIDGET)
			;

		var entry = entryBuilder.startTextDescription(text);
		entry.setTooltipSupplier(() -> CreateTooltip(key, type));
		return entry;
	}

}
