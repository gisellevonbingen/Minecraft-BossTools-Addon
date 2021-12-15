package boss_tools_giselle_addon.common.block;

import boss_tools_giselle_addon.common.block.entity.GravityNormalizerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.mrscauthd.boss_tools.machines.AbstractMachineBlock;

public class GravityNormalizerBlock extends AbstractMachineBlock<GravityNormalizerBlockEntity>
{
	public GravityNormalizerBlock(BlockBehaviour.Properties properties)
	{
		super(properties);
	}

	@Override
	public GravityNormalizerBlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new GravityNormalizerBlockEntity(pos, state);
	}

}
