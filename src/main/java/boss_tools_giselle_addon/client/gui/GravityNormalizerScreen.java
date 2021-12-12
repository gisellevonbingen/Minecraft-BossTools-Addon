package boss_tools_giselle_addon.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import boss_tools_giselle_addon.common.BossToolsAddon;
import boss_tools_giselle_addon.common.inventory.container.GravityNormalizerContainer;
import boss_tools_giselle_addon.common.network.AddonNetwork;
import boss_tools_giselle_addon.common.network.GravityNormalizerMessageRange;
import boss_tools_giselle_addon.common.network.GravityNormalizerMessageWorkingAreaVisible;
import boss_tools_giselle_addon.common.tile.GravityNormalizerTileEntity;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.mrscauthd.boss_tools.gauge.GaugeTextHelper;
import net.mrscauthd.boss_tools.gauge.GaugeValueHelper;

public class GravityNormalizerScreen extends AbstractMachineScreen<GravityNormalizerContainer>
{
	public static final ResourceLocation TEXTURE = BossToolsAddon.rl("textures/gui/container/gravity_normalizer.png");
	public static final int ENERGY_LEFT = 144;
	public static final int ENERGY_TOP = 21;

	private Button workingAreaPlusButton;
	private Button workingAreaMinusButton;

	public GravityNormalizerScreen(GravityNormalizerContainer container, PlayerInventory inventory, ITextComponent text)
	{
		super(container, inventory, text);
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public boolean hasWorkingArea()
	{
		return true;
	}

	@Override
	public boolean isWorkingAreaVisible()
	{
		return this.getMenu().getTileEntity().isWorkingAreaVisible();
	}

	@Override
	public AxisAlignedBB getWorkingArea()
	{
		return this.getMenu().getTileEntity().getWorkingArea();
	}

	@Override
	public void setWorkingAreaVisible(boolean visible)
	{
		super.setWorkingAreaVisible(visible);

		AddonNetwork.CHANNEL.sendToServer(new GravityNormalizerMessageWorkingAreaVisible(this.getMenu().getTileEntity(), visible));
	}

	@Override
	protected void renderContents(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
	{
		super.renderContents(stack, mouseX, mouseY, partialTicks);

		GravityNormalizerTileEntity tileEntity = this.getMenu().getTileEntity();
		this.renderEnergy(stack, mouseX, mouseY, this.leftPos + ENERGY_LEFT, this.topPos + ENERGY_TOP, tileEntity.getPrimaryEnergyStorage());
	}

	@Override
	protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY)
	{
		super.renderBg(stack, partialTicks, mouseX, mouseY);

		this.minecraft.getTextureManager().bind(TEXTURE);
		this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(MatrixStack stack, int mouseX, int mouseY)
	{
		super.renderLabels(stack, mouseX, mouseY);

		stack.pushPose();
		float usingScale = 0.8F;
		stack.scale(usingScale, usingScale, usingScale);
		GravityNormalizerTileEntity tileEntity = this.getMenu().getTileEntity();
		ITextComponent usingText = GaugeTextHelper.getUsingText(GaugeValueHelper.getEnergy(tileEntity.getEnergyPowerSystem().getPowerForOperation())).build();
		int usingWidth = this.font.width(usingText);

		this.font.draw(stack, usingText, (int) ((this.imageWidth - 5) / usingScale) - usingWidth, (int) (this.inventoryLabelY / usingScale), 0x333333);
		stack.popPose();
	}

	@Override
	protected void init()
	{
		super.init();

		this.workingAreaPlusButton = this.addButton(new Button(this.leftPos - 20, this.topPos + 5, 20, 20, new StringTextComponent("+"), this::onWorkingAreaUpButtonClick));
		this.workingAreaMinusButton = this.addButton(new Button(this.leftPos - 20, this.topPos + 25, 20, 20, new StringTextComponent("-"), this::onWorkingAreaDownButtonClick));
	}

	public void onWorkingAreaUpButtonClick(Button button)
	{
		GravityNormalizerTileEntity tileEntity = this.getMenu().getTileEntity();
		int nextRange = tileEntity.getRange() + 1;
		AddonNetwork.CHANNEL.sendToServer(new GravityNormalizerMessageRange(tileEntity, nextRange));
	}

	public void onWorkingAreaDownButtonClick(Button button)
	{
		GravityNormalizerTileEntity tileEntity = this.getMenu().getTileEntity();
		int nextRange = tileEntity.getRange() - 1;
		AddonNetwork.CHANNEL.sendToServer(new GravityNormalizerMessageRange(tileEntity, nextRange));
	}

	public Button getWorkingAreaPlusButton()
	{
		return this.workingAreaPlusButton;
	}

	public Button getWorkingAreaMinusButton()
	{
		return this.workingAreaMinusButton;
	}

}
