package tk.estecka.clothgamerules.mixin;

import java.util.Optional;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.world.GameRules;
import tk.estecka.clothgamerules.api.ClothGamerulesScreenBuilder;

@Mixin(targets="net/minecraft/client/gui/screen/world/CreateWorldScreen$MoreTab")
public class CreateWorldMoreTabMixin 
{
	// CreateWorldScreen.this
	@Shadow private CreateWorldScreen field_42178;

	@WrapOperation( method="openGameRulesScreen", expect=1, at=@At(value="NEW", target="net/minecraft/client/gui/screen/world/EditGameRulesScreen") )
	private EditGameRulesScreen	CatchArguments(GameRules rules, Consumer<Optional<GameRules>> saveConsumer, Operation<EditGameRulesScreen> original, @Share("rules") LocalRef<GameRules> ruleRef, @Share("consumer") LocalRef<Consumer<Optional<GameRules>>> consumerRef){
		ruleRef.set(rules);
		consumerRef.set(saveConsumer);
		return original.call(rules, saveConsumer);
	}
	
	@ModifyArg( method="openGameRulesScreen", expect=1, at=@At(value="INVOKE", target="net/minecraft/client/MinecraftClient.setScreen (Lnet/minecraft/client/gui/screen/Screen;)V") )
	private Screen ReplaceGameruleScreen(Screen original, @Share("rules") LocalRef<GameRules> ruleRef, @Share("consumer") LocalRef<Consumer<Optional<GameRules>>> consumerRef){
		return new ClothGamerulesScreenBuilder(field_42178.getWorldCreator().getGeneratorOptionsHolder().dataConfiguration().enabledFeatures())
			.Parent(field_42178)
			.ActiveValues(ruleRef.get())
			.OnClosed(consumerRef.get())
			.Build()
			;
	}
}
