package mods.thecomputerizer.dimhoppertweaks.mixin.mods.recipestages;

import com.blamejared.recipestages.recipes.RecipeStage;
import mods.thecomputerizer.dimhoppertweaks.mixin.DelayedModAccess;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeStage.class, remap = false)
public abstract class MixinRecipeStage {
    
    @Shadow private String tier;
    
    @Inject(at = @At("RETURN"), method = "matches", cancellable = true)
    private void dimhoppertweaks$matches(InventoryCrafting inv, World world, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() && DelayedModAccess.canCraft(inv));
    }
    
    @Inject(at = @At("RETURN"), method = "getCraftingResult", cancellable = true)
    private void dimhoppertweaks$getCraftingResult(InventoryCrafting inv, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(DelayedModAccess.getCraftingResult(inv,cir.getReturnValue()));
    }
    
    @Inject(at = @At("RETURN"), method = "isGoodForCrafting", cancellable = true)
    private void dimhoppertweaks$isGoodForCrafting(InventoryCrafting inv, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() || DelayedModAccess.getCraftingStages(inv).contains(this.tier));
    }
}