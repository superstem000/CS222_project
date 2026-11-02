# Ingredient Catalog & Mismatched Ingredients — Research and Plan

**Author:** jeiland2 (Jayda)
**Feature area:** Recipe Storage / Weekly Plan (missing-ingredient comparison)
**Status:** Research complete, implementation planned for next week

---

## 1. The Problem

The missing-ingredient comparison and grocery-list generation both depend on matching a recipe's ingredients against what is in inventory. Right now, nothing guarantees that the two line up. Mismatches fall into three types:

| Type | Example | What goes wrong |
| --- | --- | --- |
| **Name mismatch** | Recipe says "Eggs", inventory says "egg" or "large eggs" | App thinks eggs are missing and adds them to the grocery list |
| **Unit mismatch (same kind)** | Recipe needs 2 cups milk, inventory has 500 mL milk | Quantities can't be compared without converting |
| **Unit mismatch (different kind)** | Recipe needs 2 cups flour, inventory has 1 kg flour | Volume vs. mass — converting needs the ingredient's density |

---

## 2. Options Researched

### 2.1 Name matching

| Option | Pros | Cons |
| --- | --- | --- |
| Free-text names, compare strings | Easiest to build | Breaks on plurals, capitalization, typos, extra words |
| Normalize strings (lowercase, trim, singularize) | Catches the most common cases | Still misses synonyms ("scallion" vs. "green onion") |
| **Shared ingredient catalog with IDs** | Recipes and inventory point at the same row, so matching is just comparing IDs | Users must pick from (or add to) the catalog |
| Catalog + alias table | Handles synonyms too | Extra table; aliases have to be entered |

**Decision:** Shared catalog with IDs. Names are normalized before insert so "Eggs" and "egg" resolve to the same row. An alias table is a stretch goal.

### 2.2 Unit handling

Units are grouped into three **unit categories**:

| Category | Units | Base unit |
| --- | --- | --- |
| Mass | g, kg, oz, lb | g |
| Volume | tsp, tbsp, cup, fl oz, mL, L | mL |
| Count | whole, piece, can, clove | piece |

Conversions **within** a category use fixed factors (e.g., 1 cup = 236.588 mL, 1 lb = 453.592 g, 1 oz = 28.3495 g, 1 tbsp = 14.787 mL, 1 tsp = 4.929 mL). Every quantity is converted to its category's base unit before comparing.

Conversions **across** categories (volume ↔ mass) need a density per ingredient. I looked at two data sources:

- **USDA FoodData Central** — gives portion weights for many foods (e.g., grams in 1 cup), which would make volume ↔ mass possible.
- **TheMealDB** — has an ingredient name list that could seed the catalog, but no density or portion data.

**Decision:** Cross-category conversion is **out of scope for now**. If the categories don't match, the ingredient is treated as **missing** and the user can edit the grocery entry. This matches the fallback in our proposal's Risk 1. USDA portion data is noted as a possible future improvement.

---

## 3. Planned Design

### Tables

| Table | Key fields | Notes |
| --- | --- | --- |
| `IngredientCatalog` | id, name (normalized, unique), defaultUnitCategory | One row per real-world ingredient |
| `RecipeIngredient` | recipeId, ingredientId, quantity, unit | References the catalog by ID |
| `InventoryItem` (Andrew) | id, ingredientId, quantity, unit | Needs a foreign key to the catalog — coordinating with Andrew |

### Normalization rules (applied before insert/lookup)

1. Trim whitespace and lowercase.
2. Collapse repeated spaces.
3. Simple singularization: "eggs" → "egg", "tomatoes" → "tomato" (with an exception list, e.g., "hummus" stays as is).
4. Look up the normalized name; reuse the existing ID if found, otherwise create a new row.

### Comparison logic (missing-ingredient check)

For each ingredient in a recipe:

1. Find inventory items with the same `ingredientId`. If none → **missing (full amount)**.
2. If the unit categories differ → **missing (full amount)** (fallback).
3. Convert both quantities to the base unit and add up all matching inventory rows.
4. If on hand ≥ needed → **have it**. Otherwise → **missing (needed − on hand)**, shown in the recipe's units.

---

## 4. Test Cases to Write

| # | Case | Expected |
| --- | --- | --- |
| 1 | Recipe 2 egg, inventory 6 egg | Have it |
| 2 | Recipe "Eggs", inventory "egg" | Same catalog ID → matched |
| 3 | Recipe 2 cup milk, inventory 250 mL milk | Missing ≈ 1 cup (473.176 − 250 = 223.176 mL) |
| 4 | Recipe 1 lb chicken, inventory 500 g chicken | Have it (453.592 g ≤ 500 g) |
| 5 | Recipe 2 cup flour, inventory 1 kg flour | Different categories → missing (fallback) |
| 6 | Ingredient not in inventory at all | Missing, full amount |
| 7 | Two inventory rows of the same ingredient | Quantities summed before comparing |

---

## 5. Open Questions for the Team

- Does Andrew's `InventoryItem` table switch to `ingredientId` this week, or does the catalog land first?
- Should the user be warned when the categories don't match (e.g., "Can't compare cups to grams"), or should it silently go on the grocery list?
- Is an alias table worth it before the demo, or only if there's time in Week 9?

## 6. Next Week

- Build `IngredientCatalog` with normalized lookup.
- Implement the missing-ingredient comparison using the logic above.
- Write unit tests for cases 1–7.
