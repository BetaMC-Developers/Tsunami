package net.minecraft.server;

import org.betamc.tsunami.Tsunami;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class EntityItem extends Entity {

    public ItemStack itemStack;
    private int e;
    public int b = 0;
    public int pickupDelay;
    private int f = 5;
    public float d = (float) (Math.random() * 3.141592653589793D * 2.0D);
    private int lastTick = (int) (System.currentTimeMillis() / 50); // CraftBukkit

    public EntityItem(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(world);
        this.b(0.25F, 0.25F);
        this.height = this.width / 2.0F;
        this.setPosition(d0, d1, d2);
        this.itemStack = itemstack;
        // CraftBukkit start - infinite item fix
        if (this.itemStack.count <= -1) {
            this.itemStack.count = 1;
        }
        // CraftBukkit end
        this.yaw = (float) (Math.random() * 360.0D);
        this.motX = (double) ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.motY = 0.20000000298023224D;
        this.motZ = (double) ((float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D));
    }

    protected boolean n() {
        return false;
    }

    public EntityItem(World world) {
        super(world);
        this.b(0.25F, 0.25F);
        this.height = this.width / 2.0F;
    }

    protected void b() {}

    public void m_() {
        super.m_();
        // CraftBukkit start
        int currentTick = (int) (System.currentTimeMillis() / 50);
        this.pickupDelay -= (currentTick - this.lastTick);
        this.lastTick = currentTick;
        // CraftBukkit end

        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.motY -= 0.03999999910593033D;
        if (this.world.getMaterial(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)) == Material.LAVA) {
            this.motY = 0.20000000298023224D;
            this.motX = (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            this.motZ = (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            this.world.makeSound(this, "random.fizz", 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
        }

        this.g(this.locX, (this.boundingBox.b + this.boundingBox.e) / 2.0D, this.locZ);
        this.move(this.motX, this.motY, this.motZ);

        // Tsunami start - add item merging
        boolean itemMoved = MathHelper.floor(this.lastX) != MathHelper.floor(this.locX)
                || MathHelper.floor(this.lastY) != MathHelper.floor(this.locY)
                || MathHelper.floor(this.lastZ) != MathHelper.floor(this.locZ);

        int rate = itemMoved ? 2 : 40;
        if (this.ticksLived % rate == 0 && Tsunami.config().getBoolean("merge-dropped-items", false) && this.isMergable()) {
            for (Object obj : this.world.a(EntityItem.class, this.boundingBox.b(0.5, 0.25, 0.5))) {
                EntityItem entityitem = (EntityItem) obj;
                tryMerge(entityitem);
                if (!this.isMergable()) break;
            }
        }
        // Tsunami end

        float f = 0.98F;

        if (this.onGround) {
            f = 0.58800006F;
            int i = this.world.getTypeId(MathHelper.floor(this.locX), MathHelper.floor(this.boundingBox.b) - 1, MathHelper.floor(this.locZ));

            if (i > 0) {
                f = Block.byId[i].frictionFactor * 0.98F;
            }
        }

        this.motX *= (double) f;
        this.motY *= 0.9800000190734863D;
        this.motZ *= (double) f;
        if (this.onGround) {
            this.motY *= -0.5D;
        }

        ++this.e;
        ++this.b;
        if (this.b >= 6000) {
            //Project Poseidon Start
            if(CraftEventFactory.callItemDespawnEvent(this).isCancelled()) {
                this.b = 0;
                return;
            }
            // CraftBukkit end
            this.die();
        }
    }

    public boolean f_() {
        return this.world.a(this.boundingBox, Material.WATER, this);
    }

    protected void burn(int i) {
        this.damageEntity((Entity) null, i);
    }

    public boolean damageEntity(Entity entity, int i) {
        this.af();
        this.f -= i;
        if (this.f <= 0) {
            this.die();
        }

        return false;
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.a("Health", (short) ((byte) this.f));
        nbttagcompound.a("Age", (short) this.b);
        nbttagcompound.a("Item", this.itemStack.a(new NBTTagCompound()));
    }

    public void a(NBTTagCompound nbttagcompound) {
        this.f = nbttagcompound.d("Health") & 255;
        this.b = nbttagcompound.d("Age");
        NBTTagCompound nbttagcompound1 = nbttagcompound.k("Item");

        this.itemStack = new ItemStack(nbttagcompound1);
    }

    public void b(EntityHuman entityhuman) {
        if (!this.world.isStatic) {
            int i = this.itemStack.count;

            // CraftBukkit start
            int canHold = entityhuman.inventory.canHold(this.itemStack);
            int remaining = this.itemStack.count - canHold;
            if (this.pickupDelay <= 0 && canHold > 0) {
                this.itemStack.count = canHold;
                PlayerPickupItemEvent event = new PlayerPickupItemEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), (org.bukkit.entity.Item) this.getBukkitEntity(), remaining);
                this.world.getServer().getPluginManager().callEvent(event);
                this.itemStack.count = canHold + remaining;

                if (event.isCancelled()) {
                    return;
                }

                // Possibly < 0; fix here so we do not have to modify code below
                this.pickupDelay = 0;
            }
            // CraftBukkit end

            if (this.pickupDelay == 0 && entityhuman.inventory.pickup(this.itemStack)) {
                if (this.itemStack.id == Block.LOG.id) {
                    entityhuman.a((Statistic) AchievementList.g);
                }

                if (this.itemStack.id == Item.LEATHER.id) {
                    entityhuman.a((Statistic) AchievementList.t);
                }

                this.world.makeSound(this, "random.pop", 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                entityhuman.receive(this, i);
                if (this.itemStack.count <= 0) {
                    this.die();
                }
            }
        }
    }

    // Tsunami start
    public boolean isMergable() {
        return !this.dead && this.itemStack.count < this.itemStack.getMaxStackSize();
    }

    public void tryMerge(EntityItem entityitem) {
        if (entityitem == this || !this.isMergable() || !entityitem.isMergable())
            return;

        ItemStack item = this.itemStack;
        ItemStack itemOther = entityitem.itemStack;
        if (item.doMaterialsMatch(itemOther) && item.count + itemOther.count <= item.getMaxStackSize()) {
            if (item.count > itemOther.count) {
                item.count += itemOther.count;
                this.pickupDelay = Math.max(this.pickupDelay, entityitem.pickupDelay);
                this.b = Math.min(this.b, entityitem.b);
                entityitem.die();
            } else {
                itemOther.count += item.count;
                entityitem.pickupDelay = Math.max(this.pickupDelay, entityitem.pickupDelay);
                entityitem.b = Math.min(this.b, entityitem.b);
                this.die();
            }
        }
    }
    // Tsunami end

}
