package tk.estecka.clothgamerules;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public interface IRuleCategory
{
	Text	GetTitle();
	Identifier	GetId();


/******************************************************************************/
/* # Wrappers                                                                 */
/******************************************************************************/

	static public IRuleCategory Of(GameRules.Category vanilla){
		return new IRuleCategory() {
			@Override public Text	GetTitle(){
				return Text.translatable(vanilla.getCategory()).formatted(Formatting.BOLD, Formatting.YELLOW);
			}
		
			@Override public Identifier GetId(){
				return Identifier.of("minecraft", vanilla.getCategory().replace('.', '_'));
			}
			
		};
	}

	static public IRuleCategory Of(CustomGameRuleCategory fabric){
		return new IRuleCategory() {
			@Override public Text	GetTitle(){
				return fabric.getName();
			}
		
			@Override public Identifier GetId(){
				return fabric.getId();
			}
			
		};
	}
}
