package boss_tools_giselle_addon.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigCommon
{
	public final ItemsConfig items;

	public final MachinesConfig machines;

	public final ContentConfig contents;

	public final MekanismConfig mekanism;

	public ConfigCommon(ForgeConfigSpec.Builder builder)
	{
		builder.push("items");
		this.items = new ItemsConfig(builder);
		builder.pop();

		builder.push("machines");
		this.machines = new MachinesConfig(builder);
		builder.pop();

		builder.push("contents");
		this.contents = new ContentConfig(builder);
		builder.pop();

		builder.push("compats");

		builder.push("mekanism");
		this.mekanism = new MekanismConfig(builder);
		builder.pop();

		builder.pop();
	}

}
