package beyond_earth_giselle_addon.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class EnchantmentsConfig
{
	public final ConfigValue<Integer> space_breathing_energyUsing;
	public final ConfigValue<Integer> space_breathing_oxygenDuration;

	public final ConfigValue<Integer> gravity_normalizing_energyUsing;

	public final ConfigValue<Integer> space_fire_proof_energyUsing;

	public final ConfigValue<Integer> venus_acid_proof_energyUsing;

	public EnchantmentsConfig(ForgeConfigSpec.Builder builder)
	{
		builder.push("space_breathing");
		this.space_breathing_energyUsing = builder.define("energyUsing", 10);
		this.space_breathing_oxygenDuration = builder.define("oxygenDuration", 4);
		builder.pop();

		builder.push("gravity_normalizing");
		this.gravity_normalizing_energyUsing = builder.define("energyUsing", 10);
		builder.pop();

		builder.push("space_fire_proof");
		this.space_fire_proof_energyUsing = builder.define("energyUsing", 10);
		builder.pop();

		builder.push("venus_acid_proof");
		this.venus_acid_proof_energyUsing = builder.define("energyUsing", 10);
		builder.pop();
	}

}
