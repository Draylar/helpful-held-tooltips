package draylar.helpfulheldtooltips.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow private int heldItemTooltipFade;

    @Shadow private ItemStack currentStack;

    @Shadow private int scaledWidth;

    @Shadow public abstract TextRenderer getFontRenderer();

    @Shadow private int scaledHeight;

    /**
     * @author Draylar
     */
    @Overwrite
    public void renderHeldItemTooltip() {
        this.client.getProfiler().push("selectedItemName");

        if (this.heldItemTooltipFade > 0 && !this.currentStack.isEmpty()) {
            Text text = (new LiteralText("")).append(this.currentStack.getName()).formatted(this.currentStack.getRarity().formatting);
            String string = text.asFormattedString();

            // italicize if stack has a custom name
            if (this.currentStack.hasCustomName()) {
                text.formatted(Formatting.ITALIC);
            }

            // get enchantments from stack
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            if(currentStack.hasEnchantments()) {
                enchantments = EnchantmentHelper.getEnchantments(currentStack);
            }

            // get positioning information
            int x = (this.scaledWidth - this.getFontRenderer().getStringWidth(string)) / 2;
            int bottomOffset = 59;
            int enchantmentOffset = enchantments.size() * 12;
            int y = this.scaledHeight - bottomOffset - enchantmentOffset;
            if (!this.client.interactionManager.hasStatusBars()) {
                y += 14;
            }

            // get opacity information
            int k = (int)((float)this.heldItemTooltipFade * 256.0F / 10.0F);
            if (k > 255) {
                k = 255;
            }

            // render the tooltip if the opacity is over 0
            if (k > 0) {
                // start gl
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // positioning information
                int var10000 = x - 2;
                int var10001 = y - 2;
                int var10002 = x + this.getFontRenderer().getStringWidth(string) + 2;

                // render tooltip
                DrawableHelper.fill(var10000, var10001, var10002, y + 9 + 2, this.client.options.getTextBackgroundColor(0));
                this.getFontRenderer().drawWithShadow(string, (float)x, (float)y, 16777215 + (k << 24));

                // draw enchantments
                int count = 1;
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    Integer level = entry.getValue();

                    Text enchantmentText = new LiteralText(new TranslatableText(enchantment.getTranslationKey()).asFormattedString() + " " + new TranslatableText("potion.potency." + (level - 1)).asFormattedString()).formatted(Formatting.GRAY);
                    x = (this.scaledWidth - this.getFontRenderer().getStringWidth(enchantmentText.asFormattedString())) / 2;
                    this.getFontRenderer().drawWithShadow(enchantmentText.asFormattedString(), (float) x, (float) y + 12 * count, 16777215 + (k << 24));

                    count++;
                }

                // end gl
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }
        }

        this.client.getProfiler().pop();
    }
}
