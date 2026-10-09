package com.yourname.mealplanner.grocery;

import com.yourname.mealplanner.mealplan.MealPlanEntry;
import com.yourname.mealplanner.pantry.PantryItem;
import com.yourname.mealplanner.recipe.RecipeIngredient;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class GroceryMergeService {

    public enum UnitFamily {
        MASS_METRIC("g"),
        VOLUME_METRIC("ml"),
        VOLUME_COOKING("tsp"),
        COUNT("pcs"),
        OTHER("other");

        private final String defaultBaseUnit;

        UnitFamily(String defaultBaseUnit) {
            this.defaultBaseUnit = defaultBaseUnit;
        }

        public String getDefaultBaseUnit() {
            return defaultBaseUnit;
        }
    }

    public static class UnitInfo {
        @Getter
        private final UnitFamily family;
        @Getter
        private final double factorToBase;
        @Getter
        private final String baseUnit;

        public UnitInfo(UnitFamily family, double factorToBase, String baseUnit) {
            this.family = family;
            this.factorToBase = factorToBase;
            this.baseUnit = baseUnit;
        }
    }

    private static final Map<String, UnitInfo> UNIT_MAP = new HashMap<>();

    static {
        // Mass Metric (Base: g)
        registerUnit("kg", UnitFamily.MASS_METRIC, 1000.0, "g");
        registerUnit("kgs", UnitFamily.MASS_METRIC, 1000.0, "g");
        registerUnit("kilogram", UnitFamily.MASS_METRIC, 1000.0, "g");
        registerUnit("kilograms", UnitFamily.MASS_METRIC, 1000.0, "g");
        registerUnit("g", UnitFamily.MASS_METRIC, 1.0, "g");
        registerUnit("gram", UnitFamily.MASS_METRIC, 1.0, "g");
        registerUnit("grams", UnitFamily.MASS_METRIC, 1.0, "g");

        // Volume Metric (Base: ml)
        registerUnit("l", UnitFamily.VOLUME_METRIC, 1000.0, "ml");
        registerUnit("liter", UnitFamily.VOLUME_METRIC, 1000.0, "ml");
        registerUnit("liters", UnitFamily.VOLUME_METRIC, 1000.0, "ml");
        registerUnit("litre", UnitFamily.VOLUME_METRIC, 1000.0, "ml");
        registerUnit("litres", UnitFamily.VOLUME_METRIC, 1000.0, "ml");
        registerUnit("ml", UnitFamily.VOLUME_METRIC, 1.0, "ml");
        registerUnit("milliliter", UnitFamily.VOLUME_METRIC, 1.0, "ml");
        registerUnit("milliliters", UnitFamily.VOLUME_METRIC, 1.0, "ml");
        registerUnit("millilitre", UnitFamily.VOLUME_METRIC, 1.0, "ml");

        // Volume Cooking (Base: tsp)
        registerUnit("cup", UnitFamily.VOLUME_COOKING, 48.0, "tsp");
        registerUnit("cups", UnitFamily.VOLUME_COOKING, 48.0, "tsp");
        registerUnit("tbsp", UnitFamily.VOLUME_COOKING, 3.0, "tsp");
        registerUnit("tablespoon", UnitFamily.VOLUME_COOKING, 3.0, "tsp");
        registerUnit("tablespoons", UnitFamily.VOLUME_COOKING, 3.0, "tsp");
        registerUnit("tsp", UnitFamily.VOLUME_COOKING, 1.0, "tsp");
        registerUnit("teaspoon", UnitFamily.VOLUME_COOKING, 1.0, "tsp");
        registerUnit("teaspoons", UnitFamily.VOLUME_COOKING, 1.0, "tsp");

        // Discrete Count
        registerUnit("piece", UnitFamily.COUNT, 1.0, "pcs");
        registerUnit("pieces", UnitFamily.COUNT, 1.0, "pcs");
        registerUnit("pcs", UnitFamily.COUNT, 1.0, "pcs");
        registerUnit("pc", UnitFamily.COUNT, 1.0, "pcs");
        registerUnit("clove", UnitFamily.COUNT, 1.0, "clove");
        registerUnit("cloves", UnitFamily.COUNT, 1.0, "clove");
        registerUnit("slice", UnitFamily.COUNT, 1.0, "slice");
        registerUnit("slices", UnitFamily.COUNT, 1.0, "slice");
        registerUnit("can", UnitFamily.COUNT, 1.0, "can");
        registerUnit("cans", UnitFamily.COUNT, 1.0, "can");
        registerUnit("pinch", UnitFamily.COUNT, 1.0, "pinch");
        registerUnit("pinches", UnitFamily.COUNT, 1.0, "pinch");
    }

    private static void registerUnit(String alias, UnitFamily family, double factor, String baseUnit) {
        UNIT_MAP.put(alias.toLowerCase(), new UnitInfo(family, factor, baseUnit));
    }

    public static class IntermediateItem {
        @Getter
        private final String normalizedName;
        @Getter
        private final UnitFamily unitFamily;
        @Getter
        private final String baseUnit;
        @Getter
        private double baseQuantity;

        public IntermediateItem(String normalizedName, UnitFamily unitFamily, String baseUnit, double baseQuantity) {
            this.normalizedName = normalizedName;
            this.unitFamily = unitFamily;
            this.baseUnit = baseUnit;
            this.baseQuantity = baseQuantity;
        }

        public void addBaseQuantity(double qty) {
            this.baseQuantity += qty;
        }

        public void deductBaseQuantity(double qty) {
            this.baseQuantity -= qty;
        }
    }

    /**
     * Merge ingredients from planned meals, scaling by servings, normalizing names and units,
     * and optionally deducting pantry items.
     */
    public List<GroceryItem> generateMergedList(
            List<MealPlanEntry> mealPlanEntries,
            List<PantryItem> pantryItems,
            boolean subtractPantry) {

        // Key: normalizedName + "|" + unitFamily + "|" + baseUnit
        Map<String, IntermediateItem> groupedItems = new LinkedHashMap<>();

        // 1. Accumulate ingredients scaled by servings
        for (MealPlanEntry entry : mealPlanEntries) {
            if (entry.getRecipe() == null || entry.getRecipe().getIngredients() == null) {
                continue;
            }

            int recipeServings = entry.getRecipe().getServings();
            int plannedServings = entry.getEffectiveServings();
            double scale = (recipeServings > 0) ? ((double) plannedServings / recipeServings) : 1.0;

            for (RecipeIngredient ing : entry.getRecipe().getIngredients()) {
                String normName = normalizeName(ing.getName());
                if (!StringUtils.hasText(normName)) {
                    continue;
                }

                UnitInfo unitInfo = resolveUnit(ing.getUnit());
                double scaledBaseQty = ing.getQuantity().doubleValue() * scale * unitInfo.getFactorToBase();

                String key = buildKey(normName, unitInfo.getFamily(), unitInfo.getBaseUnit());
                if (groupedItems.containsKey(key)) {
                    groupedItems.get(key).addBaseQuantity(scaledBaseQty);
                } else {
                    groupedItems.put(key, new IntermediateItem(
                            normName, unitInfo.getFamily(), unitInfo.getBaseUnit(), scaledBaseQty));
                }
            }
        }

        // 2. Subtract on-hand pantry items if requested
        if (subtractPantry && pantryItems != null && !pantryItems.isEmpty()) {
            for (PantryItem pantry : pantryItems) {
                String normPantryName = normalizeName(pantry.getName());
                UnitInfo pantryUnitInfo = resolveUnit(pantry.getUnit());
                double pantryBaseQty = pantry.getQuantity().doubleValue() * pantryUnitInfo.getFactorToBase();

                String key = buildKey(normPantryName, pantryUnitInfo.getFamily(), pantryUnitInfo.getBaseUnit());
                if (groupedItems.containsKey(key)) {
                    groupedItems.get(key).deductBaseQuantity(pantryBaseQty);
                }
            }
        }

        // 3. Convert intermediate items into finalized GroceryItem entities
        List<GroceryItem> result = new ArrayList<>();
        for (IntermediateItem item : groupedItems.values()) {
            if (item.getBaseQuantity() <= 0.0001) {
                // Completely covered by pantry, skip adding to shopping list
                continue;
            }

            DisplayQuantity display = convertToDisplayUnit(item.getUnitFamily(), item.getBaseUnit(), item.getBaseQuantity());

            GroceryItem groceryItem = GroceryItem.builder()
                    .name(item.getNormalizedName())
                    .quantity(display.getQuantity())
                    .unit(display.getUnit())
                    .checked(false)
                    .isCustom(false)
                    .build();

            result.add(groceryItem);
        }

        return result;
    }

    private String buildKey(String normName, UnitFamily family, String baseUnit) {
        return normName + "|" + family.name() + "|" + baseUnit.toLowerCase();
    }

    public UnitInfo resolveUnit(String rawUnit) {
        if (!StringUtils.hasText(rawUnit)) {
            return new UnitInfo(UnitFamily.OTHER, 1.0, "");
        }
        String clean = rawUnit.trim().toLowerCase();
        if (UNIT_MAP.containsKey(clean)) {
            return UNIT_MAP.get(clean);
        }
        // Handle trailing plural 's' if not found
        if (clean.endsWith("s") && clean.length() > 2) {
            String singular = clean.substring(0, clean.length() - 1);
            if (UNIT_MAP.containsKey(singular)) {
                return UNIT_MAP.get(singular);
            }
        }
        // Custom or discrete unit: keep as its own family unit
        return new UnitInfo(UnitFamily.OTHER, 1.0, clean);
    }

    public String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.toLowerCase().trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) {
            return "";
        }

        // Common irregular plural checks
        if (s.equals("tomatoes")) return "tomato";
        if (s.equals("potatoes")) return "potato";
        if (s.equals("leaves")) return "leaf";
        if (s.equals("berries")) return "berry";
        if (s.equals("strawberries")) return "strawberry";
        if (s.equals("blueberries")) return "blueberry";
        if (s.equals("raspberries")) return "raspberry";
        if (s.equals("cherries")) return "cherry";

        // Words that should retain trailing 's'
        if (s.equals("asparagus") || s.equals("couscous") || s.equals("hummus") || s.endsWith("ss")) {
            return s;
        }

        // -ies -> -y (e.g. pastries -> pastry)
        if (s.endsWith("ies") && s.length() > 4) {
            return s.substring(0, s.length() - 3) + "y";
        }

        // -oes -> -o (e.g. mangoes -> mango)
        if (s.endsWith("oes") && s.length() > 4) {
            return s.substring(0, s.length() - 2);
        }

        // -shes, -ches, -sses, -xes, -zes -> remove -es
        if ((s.endsWith("shes") || s.endsWith("ches") || s.endsWith("sses") || s.endsWith("xes") || s.endsWith("zes"))
                && s.length() > 4) {
            return s.substring(0, s.length() - 2);
        }

        // cloves -> clove, olives -> olive, chives -> chive
        if (s.endsWith("ves") && s.length() > 4) {
            return s.substring(0, s.length() - 1);
        }

        // Generic trailing -s
        if (s.endsWith("s") && s.length() > 2) {
            return s.substring(0, s.length() - 1);
        }

        return s;
    }

    public static class DisplayQuantity {
        @Getter
        private final BigDecimal quantity;
        @Getter
        private final String unit;

        public DisplayQuantity(BigDecimal quantity, String unit) {
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    public DisplayQuantity convertToDisplayUnit(UnitFamily family, String baseUnit, double baseQty) {
        if (family == UnitFamily.MASS_METRIC) {
            if (baseQty >= 1000.0) {
                return new DisplayQuantity(round(baseQty / 1000.0), "kg");
            } else {
                return new DisplayQuantity(round(baseQty), "g");
            }
        } else if (family == UnitFamily.VOLUME_METRIC) {
            if (baseQty >= 1000.0) {
                return new DisplayQuantity(round(baseQty / 1000.0), "l");
            } else {
                return new DisplayQuantity(round(baseQty), "ml");
            }
        } else if (family == UnitFamily.VOLUME_COOKING) {
            if (baseQty >= 48.0) {
                return new DisplayQuantity(round(baseQty / 48.0), "cup");
            } else if (baseQty >= 3.0) {
                return new DisplayQuantity(round(baseQty / 3.0), "tbsp");
            } else {
                return new DisplayQuantity(round(baseQty), "tsp");
            }
        } else {
            return new DisplayQuantity(round(baseQty), baseUnit);
        }
    }

    private BigDecimal round(double val) {
        BigDecimal bd = BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        if (bd.scale() < 0) {
            return bd.setScale(0, RoundingMode.UNNECESSARY);
        }
        return bd;
    }
}
