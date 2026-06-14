package net.hollowed.antique.index;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

@SuppressWarnings("unused")
public record AdventureArmorMaterial(int durability, Map<ArmorType, Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> modelId, float burningReduction, float stepHeight, float movementSpeed) {

    public Item.Properties applySettings(Item.Properties settings, ArmorType equipmentType) {
        return settings.durability(equipmentType.getDurability(this.durability)).attributes(this.createAttributeModifiers(equipmentType)).enchantable(this.enchantmentValue).component(DataComponents.EQUIPPABLE, Equippable.builder(equipmentType.getSlot()).setEquipSound(this.equipSound).setAsset(this.modelId).build()).repairable(this.repairIngredient);
    }

    public Item.Properties applyBodyArmorSettings(Item.Properties settings, HolderSet<EntityType<?>> allowedEntities) {
        return settings.durability(ArmorType.BODY.getDurability(this.durability)).attributes(this.createAttributeModifiers(ArmorType.BODY)).repairable(this.repairIngredient).component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.BODY).setEquipSound(this.equipSound).setAsset(this.modelId).setAllowedEntities(allowedEntities).build());
    }

    public Item.Properties applyBodyArmorSettings(Item.Properties settings, Holder<SoundEvent> equipSound, boolean damageOnHurt, HolderSet<EntityType<?>> allowedEntities) {
        if (damageOnHurt) {
            settings = settings.durability(ArmorType.BODY.getDurability(this.durability)).repairable(this.repairIngredient);
        }

        return settings.attributes(this.createAttributeModifiers(ArmorType.BODY)).component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.BODY).setEquipSound(equipSound).setAsset(this.modelId).setAllowedEntities(allowedEntities).setDamageOnHurt(damageOnHurt).build());
    }

    private ItemAttributeModifiers createAttributeModifiers(ArmorType equipmentType) {
        int i = this.defense.getOrDefault(equipmentType, 0);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        EquipmentSlotGroup attributeModifierSlot = EquipmentSlotGroup.bySlot(equipmentType.getSlot());
        Identifier identifier = Identifier.minecraft("armor." + equipmentType.getName());
        builder.add(Attributes.ARMOR, new AttributeModifier(identifier, i, Operation.ADD_VALUE), attributeModifierSlot);
        builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(identifier, this.toughness, Operation.ADD_VALUE), attributeModifierSlot);
        if (this.knockbackResistance > 0.0F) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(identifier, this.knockbackResistance, Operation.ADD_VALUE), attributeModifierSlot);
        }
        if (this.burningReduction > 0.0F && attributeModifierSlot.test(EquipmentSlot.CHEST)) {
            builder.add(Attributes.BURNING_TIME, new AttributeModifier(Identifier.minecraft("burn_time"), -this.burningReduction, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), attributeModifierSlot);
        }
        if (attributeModifierSlot.test(EquipmentSlot.FEET)) {
            if (this.stepHeight > 0.0F) {
                builder.add(Attributes.STEP_HEIGHT, new AttributeModifier(Identifier.minecraft("step_height"), this.stepHeight, AttributeModifier.Operation.ADD_VALUE), attributeModifierSlot);
            }
            if (this.movementSpeed > 0.0F) {
                builder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(Identifier.minecraft("movement_speed"), this.movementSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), attributeModifierSlot);
            }
        }

        return builder.build();
    }

    public int durability() {
        return this.durability;
    }

    public Map<ArmorType, Integer> defense() {
        return this.defense;
    }

    public int enchantmentValue() {
        return this.enchantmentValue;
    }

    public Holder<SoundEvent> equipSound() {
        return this.equipSound;
    }

    public float toughness() {
        return this.toughness;
    }

    public float knockbackResistance() {
        return this.knockbackResistance;
    }

    public TagKey<Item> repairIngredient() {
        return this.repairIngredient;
    }

}
