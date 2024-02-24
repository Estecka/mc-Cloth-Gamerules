package tk.estecka.clothgamerules;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import tk.estecka.clothgamerules.api.ClothGamerulesScreenFactory;

public class ModMenu
implements ModMenuApi
{
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return parent -> getModConfigScreen(parent);
	}

	public Screen	getModConfigScreen(Screen parent){
		return ClothGamerulesScreenFactory.CreateScreen(parent, Text.literal("Test Screen"), new GameRules(), noop->{});
	}
}
