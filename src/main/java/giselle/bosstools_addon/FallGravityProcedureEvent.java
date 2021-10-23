package giselle.bosstools_addon;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class FallGravityProcedureEvent extends EntityEvent
{
	public FallGravityProcedureEvent(Entity entity)
	{
		super(entity);
	}

	@Override
	public boolean isCancelable()
	{
		return true;
	}

}
