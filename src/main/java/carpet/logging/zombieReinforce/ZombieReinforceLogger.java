package carpet.logging.zombieReinforce;

import carpet.logging.AbstractLogger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

public class ZombieReinforceLogger extends AbstractLogger
{
	public static final String NAME = "zombieReinforce";
	private static final ZombieReinforceLogger INSTANCE = new ZombieReinforceLogger();

	public ZombieReinforceLogger()
	{
		super(NAME);
	}

	public static ZombieReinforceLogger getInstance()
	{
		return INSTANCE;
	}

	public void onZombieReinforceAttempt(EntityZombie source, EntityZombie fellow, boolean success)
	{
		if (!LoggerRegistry.__zombieReinforce)
		{
			return;
		}

		if (success)
		{
			this.log(() -> new ITextComponent[]{
					advTr(
							"success", "%s at %s summoned zombie reinforce to %s",
							Messenger.entity(source),
							Messenger.coord(new Vec3d(source.posX, source.posY, source.posZ), source.dimension),
							Messenger.coord(new Vec3d(fellow.posX, fellow.posY, fellow.posZ), fellow.dimension)
					),
			});
		}
		else
		{
			this.log(() -> new ITextComponent[]{
					advTr(
							"fail", "%s at @s failed to summon zombie reinforce",
							Messenger.entity(source),
							Messenger.coord(new Vec3d(source.posX, source.posY, source.posZ), source.dimension)
					),
			});
		}
	}
}
