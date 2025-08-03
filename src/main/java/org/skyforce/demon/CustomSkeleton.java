package org.skyforce.demon;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.monster.EntitySkeleton;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.level.World;

import java.lang.reflect.Field;

public class CustomSkeleton extends EntitySkeleton {

    public CustomSkeleton(World world) {
        super(EntityTypes.bg, world);

        PathfinderGoalSelector goalSelector = getGoalSelector();
        PathfinderGoalSelector targetSelector = getTargetSelector();

        if (goalSelector != null && targetSelector != null) {
            goalSelector.a();
            targetSelector.a();

            goalSelector.a(0, new PathfinderGoalFloat(this));
            goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.2D, false));
            goalSelector.a(2, new PathfinderGoalRandomStrollLand(this, 1.0D));
            goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
            goalSelector.a(4, new PathfinderGoalRandomLookaround(this));

            targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
        }
    }




    private PathfinderGoalSelector getGoalSelector() {
        try {
            for (Field field : net.minecraft.world.entity.EntityInsentient.class.getDeclaredFields()) {
                if (PathfinderGoalSelector.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value != null) {
                        return (PathfinderGoalSelector) value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private PathfinderGoalSelector getTargetSelector() {
        try {
            boolean foundFirst = false;
            for (Field field : net.minecraft.world.entity.EntityInsentient.class.getDeclaredFields()) {
                if (PathfinderGoalSelector.class.isAssignableFrom(field.getType())) {
                    if (!foundFirst) {
                        foundFirst = true;
                        continue; // skip the first (goalSelector)
                    }
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value != null) {
                        return (PathfinderGoalSelector) value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}