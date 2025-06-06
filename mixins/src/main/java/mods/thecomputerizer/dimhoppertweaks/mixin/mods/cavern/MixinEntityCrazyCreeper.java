package mods.thecomputerizer.dimhoppertweaks.mixin.mods.cavern;

import cavern.entity.monster.EntityCavenicCreeper;
import cavern.entity.monster.EntityCrazyCreeper;
import mods.thecomputerizer.dimhoppertweaks.client.particle.ParticleBlightFire;
import mods.thecomputerizer.dimhoppertweaks.mixin.DelayedModAccess;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cavern.world.CaveDimensions.CAVENIA;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@Mixin(value = EntityCrazyCreeper.class, remap = false)
public abstract class MixinEntityCrazyCreeper extends EntityCavenicCreeper {

    @Shadow @Final private BossInfoServer bossInfo;
    @Unique private boolean dimhoppertweaks$inCavenia = true;

    public MixinEntityCrazyCreeper(World world) {
        super(world);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void dimhoppertweaks$init(World world, CallbackInfo ci) {
        if(world.provider.getDimensionType()!=CAVENIA) {
            this.dimhoppertweaks$inCavenia = false;
            this.bossInfo.setVisible(false);
        }
    }
    
    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        super.dropLoot(wasRecentlyHit, lootingModifier, source);
        this.entityDropItem(DelayedModAccess.getStack("contenttweaker:crazy_creeper_essence",1),0f);
    }

    /**
     * @author The_Computerizer
     * @reason Fix weird boss bar issues
     */
    @Overwrite
    protected void updateAITasks() {
        super.updateAITasks();
        if(!this.world.isRemote && this.dimhoppertweaks$inCavenia) {
            boolean isCloseEnough = false;
            for(EntityPlayerMP player : this.bossInfo.getPlayers()) {
                if (this.getDistance(player)<30d) {
                    isCloseEnough = true;
                    break;
                }
            }
            this.bossInfo.setDarkenSky(isCloseEnough);
            this.bossInfo.setVisible(isCloseEnough);
        }
        this.bossInfo.setPercent(this.getHealth()/this.getMaxHealth());
    }

    /**
     * @author The_Computerizer
     * @reason Fix too many particles being rendered
     */
    @Overwrite
    @SideOnly(CLIENT)
    public void onUpdate() {
        super.onUpdate();
        if(this.world.isRemote) {
            AxisAlignedBB aabb = this.getEntityBoundingBox();
            Vec3d center = aabb.getCenter();
            double xFlip = this.rand.nextInt(2)*2-1;
            double yFlip = this.rand.nextInt(2)*2-1;
            double zFlip = this.rand.nextInt(2)*2-1;
            double ptX = center.x+(Math.abs(aabb.maxX-aabb.minX)*xFlip);
            double ptY = center.y+(Math.abs(aabb.maxY-aabb.minY)*0.75d*yFlip);
            double ptZ = center.z+(Math.abs(aabb.maxZ-aabb.minZ)*zFlip);
            double motionX = this.rand.nextDouble()*xFlip;
            double motionY = this.rand.nextDouble()*0.75d*yFlip;
            double motionZ = this.rand.nextDouble()*zFlip;
            ParticleBlightFire particle = new ParticleBlightFire(this.world,ptX,ptY,ptZ,motionX,motionY,motionZ,
                    100f,32d,0.5f,false);
            particle.setCenter(center);
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(particle);
        }
    }
}