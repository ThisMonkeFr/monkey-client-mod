package gg.monkeyclient.integration.apple.helpers;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;

public record ConsumableFood(FoodProperties food, Consumable consumable)
{
}
