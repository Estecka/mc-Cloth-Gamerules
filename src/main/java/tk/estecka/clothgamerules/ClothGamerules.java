package tk.estecka.clothgamerules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.loader.api.FabricLoader;

public class ClothGamerules
{
	static public final Logger LOGGER = LoggerFactory.getLogger("cloth-gamerules");

	static public final boolean IS_PREFRULE_INSTALLED = FabricLoader.getInstance().isModLoaded("preferred-gamerules");
}
