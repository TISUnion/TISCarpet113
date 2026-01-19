package carpet.logging.itemNoClip;

import carpet.logging.AbstractLogger;
import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

public class ItemNoClipLogger extends AbstractLogger
{
	public static final String NAME = "itemNoClip";
	private static final ItemNoClipLogger INSTANCE = new ItemNoClipLogger();

	public ItemNoClipLogger()
	{
		super(NAME);
	}

	public static ItemNoClipLogger getInstance()
	{
		return INSTANCE;
	}

	public void onItemBecomeNoClip(EntityItem item)
	{
		if (!LoggerRegistry.__itemNoClip)
		{
			return;
		}
		this.log(() -> new ITextComponent[]{
				advTr(
						"become_no_clip", "%s changed to noClip state @ %s",
						Messenger.entity(item),
						Messenger.coord(new Vec3d(item.posX, item.posY, item.posZ), item.dimension)
				),
		});
	}
}
