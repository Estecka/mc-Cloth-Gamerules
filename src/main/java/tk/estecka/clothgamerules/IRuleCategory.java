package tk.estecka.clothgamerules;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRuleCategory;

public interface IRuleCategory
{
	Text	GetTitle();
	Identifier	GetId();


/******************************************************************************/
/* # Wrappers                                                                 */
/******************************************************************************/

	static public IRuleCategory Of(GameRuleCategory vanilla){
		return new IRuleCategory() {
			@Override public Text GetTitle(){
				return vanilla.getText().formatted(Formatting.BOLD, Formatting.YELLOW);
			}
		
			@Override public Identifier GetId(){
				return vanilla.id();
			}
		};
	}

	static public IRuleCategory Of(CustomGameRuleCategory fabric){
		return new IRuleCategory() {
			@Override public Text GetTitle(){
				return fabric.getName();
			}
		
			@Override public Identifier GetId(){
				return fabric.getId();
			}
		};
	}

}
