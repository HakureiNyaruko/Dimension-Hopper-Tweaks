package mods.thecomputerizer.dimhoppertweaks.client.particle;

import mods.thecomputerizer.dimhoppertweaks.registry.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.event.ClientTicks;

import javax.annotation.Nullable;
import javax.vecmath.Point4f;
import java.util.Objects;

import static mods.thecomputerizer.dimhoppertweaks.client.render.BetterBlightFireRenderer.TEXTURE;
import static mods.thecomputerizer.dimhoppertweaks.client.render.BetterBlightFireRenderer.TEXTURE_GRAY;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static net.silentchaos512.scalinghealth.lib.module.ModuleAprilTricks.instance;

@SideOnly(CLIENT)
public class ParticleBlightFire extends Particle {

    private final double rangeFactor;
    private final boolean tomfoolery;
    private Point4f frameUV;
    private Vec3d centerVec = null;

    public ParticleBlightFire(World world, double x, double y, double z, double velocityX, double velocityY,
                              double velocityZ, float maxAge, double rangeFactor, float scale, boolean shouldRandomize) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.rangeFactor = rangeFactor;
        if(shouldRandomize) randomizeInitialPos();
        this.tomfoolery = instance.isEnabled() && instance.isRightDay();
        this.particleTexture = tomfoolery ? ParticleRegistry.getGrayFireAtlas() : ParticleRegistry.getFireAtlas();
        this.particleScale = (0.2f+(this.rand.nextFloat()/(1f/(0.2f/2f))))*scale;
        this.particleMaxAge = (int)(maxAge-(this.rand.nextFloat()*maxAge));
        this.particleAlpha = 1f;
        this.frameUV = getUVFrame();
    }

    public void setCenter(Vec3d center) {
        this.centerVec = center;
    }

    private Point4f getUVFrame() {
        int frame = ClientTicks.ticksInGame % 64;
        boolean isOffset = frame>31;
        if(isOffset) frame-=32;
        float minU = isOffset ? 0.5f : 0;
        return new Point4f(minU,minU+0.5f,(float)frame/32f,(float)(frame+1)/32f);
    }

    private double randomizeDouble(double original) {
        double factor = Math.log(this.rangeFactor)/Math.log(2);
        return (original-factor)+(this.rand.nextDouble()*factor*2);
    }

    private void randomizeInitialPos() {
        double x = randomizeDouble(this.posX);
        double y = randomizeDouble(this.posY);
        double z = randomizeDouble(this.posZ);
        setPosition(x,y,z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = posX;
        this.prevPosY = posY;
        this.prevPosZ = posZ;
        if(this.particleAge++>=this.particleMaxAge) setExpired();
        else {
            if(Objects.isNull(this.centerVec)) {
                move(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.95d;
                this.motionY *= 0.95d;
                this.motionZ *= 0.95d;
                if (this.onGround) {
                    this.motionX *= 0.8d;
                    this.motionZ *= 0.8d;
                }
            } else {
                float ageFactor = (float)this.particleAge/(float)this.particleMaxAge;
                this.posX = getClampedDistance(this.centerVec.x,this.posX,ageFactor);
                this.posY = getClampedDistance(this.centerVec.y,this.posY,ageFactor);
                this.posZ = getClampedDistance(this.centerVec.z,this.posZ,ageFactor);
            }
            this.frameUV = getUVFrame();
        }
    }

    private double getClampedDistance(double target, double from, double ageFactor) {
        double dif = target-from;
        double max = dif*ageFactor;
        return from+(max>0d ? Math.min(max,dif) : Math.max(max,dif));
    }

    @Override
    public int getFXLayer() {
        return 2;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ,
                               float rotationYZ, float rotationXY, float rotationXZ) {
        TextureManager manager = Minecraft.getMinecraft().getTextureManager();
        if(this.tomfoolery) manager.bindTexture(TEXTURE_GRAY);
        else manager.bindTexture(TEXTURE);
        double minU = particleTexture.getMinU()+this.frameUV.x;
        double maxU = particleTexture.getMinU()+this.frameUV.y;
        double minV = particleTexture.getMinV()+this.frameUV.z;
        double maxV = particleTexture.getMinV()+this.frameUV.w;
        double x = this.prevPosX+(this.posX-this.prevPosX)*partialTicks-interpPosX;
        double y = this.prevPosY+(this.posY-this.prevPosY)*partialTicks-interpPosY;
        double z = this.prevPosZ+(this.posZ-this.prevPosZ)*partialTicks-interpPosZ;
        double scaledUDDirX = rotationXY*this.particleScale;
        double scaledUDDirY = rotationZ*this.particleScale;
        double scaledUDDirZ = rotationXZ*this.particleScale;
        double scaledLRDirX = rotationX*this.particleScale;
        double scaledLRDirZ = rotationYZ*this.particleScale;
        int combinedBrightness = getBrightnessForRender(partialTicks);
        int skyLight = combinedBrightness >> 16 & 65535;
        int blockLight = combinedBrightness & 65535;
        buffer.pos(x-((scaledLRDirX-scaledUDDirX)/2d),y-scaledUDDirY,z-((scaledLRDirZ-scaledUDDirZ)/2d))
                .tex(maxU,maxV).color(this.particleRed,this.particleGreen,this.particleBlue,this.particleAlpha)
                .lightmap(skyLight,blockLight).endVertex();
        buffer.pos(x-((scaledLRDirX+scaledUDDirX)/2d),y+scaledUDDirY,z-((scaledLRDirZ+scaledUDDirZ)/2d))
                .tex(maxU,minV).color(this.particleRed,this.particleGreen,this.particleBlue,this.particleAlpha)
                .lightmap(skyLight,blockLight).endVertex();
        buffer.pos(x+((scaledLRDirX+scaledUDDirX)/2d),y+scaledUDDirY,z+((scaledLRDirZ+scaledUDDirZ)/2d))
                .tex(minU,minV).color(this.particleRed,this.particleGreen,this.particleBlue,this.particleAlpha)
                .lightmap(skyLight,blockLight).endVertex();
        buffer.pos(x+((scaledLRDirX-scaledUDDirX)/2d),y-scaledUDDirY,z+((scaledLRDirZ-scaledUDDirZ)/2d))
                .tex(minU,maxV).color(this.particleRed,this.particleGreen,this.particleBlue,this.particleAlpha)
                .lightmap(skyLight,blockLight).endVertex();
    }

    public static class Factory implements IParticleFactory {

        @Override
        public @Nullable ParticleBlightFire createParticle(int id, World world, double posX, double posY, double posZ,
                                                 double velocityX, double velocityY, double velocityZ, int... args) {
            return new ParticleBlightFire(Minecraft.getMinecraft().world,posX,posY,posZ,velocityX,velocityY,velocityZ,
                    args.length>=1 ? (float)args[0] : 100f, args.length>=2 ? (double)args[1] : 32d,
                    args.length>=3 ? ((float)args[2])/100f : 0.5f, args.length<4 || args[3]!=1);
        }
    }
}