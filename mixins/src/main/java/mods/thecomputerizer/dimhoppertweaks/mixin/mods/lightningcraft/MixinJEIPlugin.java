package mods.thecomputerizer.dimhoppertweaks.mixin.mods.lightningcraft;

import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sblectric.lightningcraft.integration.jei.JEIPlugin;

import static sblectric.lightningcraft.init.LCItems.guide;
import static sblectric.lightningcraft.init.LCItems.ingot;
import static sblectric.lightningcraft.init.LCItems.material;
import static sblectric.lightningcraft.ref.Log.logger;

@SuppressWarnings("deprecation")
@Mixin(value = JEIPlugin.class, remap = false)
public abstract class MixinJEIPlugin {

    /**
     * @author The_Computerizer
     * @reason Remove crusher and infusion categories
     */
    @Overwrite
    public void register(IModRegistry reg) {
        reg.addDescription(new ItemStack(ingot,1,0),"lightningcraft.electricium_info");
        reg.addDescription(new ItemStack(material,1,11),"lightningcraft.ichor_info");
        reg.addDescription(new ItemStack(guide),"lightningcraft.guide_info");
        reg.addDescription(new ItemStack(ingot,1,1),"lightningcraft.skyfather_info");
        reg.addDescription(new ItemStack(ingot,1,2),"lightningcraft.mystic_info");
        logger.info("JEI integration complete.");
    }
}