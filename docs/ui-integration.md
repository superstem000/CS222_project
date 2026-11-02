# Fridge First UI — Integration Notes

**Author:** bhpark3 (Brian) · **Feature area:** UI + integration

The app now opens to the Fridge First UI: **Inventory / Recipes / Weekly Plan / Grocery** in a
bottom navigation bar, plus a recipe editor. It runs on **in-memory fake repositories** seeded
with sample data, so everything works end-to-end before the real database is ready.

All new code is under `shared/src/commonMain/kotlin/com/example/mealplannerapp/fridgefirst/`.
The starter screens (`ui/`, `di/AppContainer`, `data/`) are untouched and simply not routed;
`App.kt` is the only existing source file that changed.

## Layout

| Package | What's there |
| --- | --- |
| `fridgefirst/data` | Repository interfaces the screens use: `InventoryRepository`, `RecipeBookRepository`, `WeeklyPlanRepository` |
| `fridgefirst/data/fake` | In-memory implementations + `SampleData` |
| `fridgefirst/domain` | `IngredientMatcher` (name/unit matching) and `KitchenCalculator` (missing ingredients, ranking, grocery list, cooking) |
| `fridgefirst/di` | `FridgeFirstContainer` — the one place that decides which implementations are used |
| `fridgefirst/ui` | Theme, navigation, and one screen + ViewModel per feature |

Models are shared, not duplicated: inventory uses `InventoryData` from `domain/InventoryDraft.kt`,
recipes use the existing Room `Recipe` / `Ingredient` / `RecipeWithIngredients`, and grocery lines
use `domain/GroceryItem`.

## Plugging in real implementations

1. Implement the interface (e.g. `class RoomInventoryRepository(dao: InventoryDao) : InventoryRepository`).
2. Construct it in `FridgeFirstContainer` (or build a container at the platform entry point and
   pass it to `FridgeFirstApp(container)`).
3. No screen or ViewModel changes are needed. The ViewModel tests in
   `commonTest/.../fridgefirst/ui/` describe the behavior the UI expects.

Feature owners:

- **Inventory (Andrew):** `InventoryRepository` — Flow of all items, add, update quantity, remove.
  `InMemoryInventoryRepository` currently wraps the `Inventory` draft class unchanged.
- **Recipes + weekly plan (Jayda):** `RecipeBookRepository` mirrors the starter `RecipeRepository`,
  so a Room-backed version is a thin adapter. `WeeklyPlanRepository` is just an ordered list of
  recipe ids for this week. Once the ingredient catalog exists, implement `IngredientMatcher`
  with catalog ids and pass it to `FridgeFirstContainer`.
- **Grocery + cooking (Namit):** `KitchenCalculator.groceryList` and `.cookingChanges` are interim
  implementations; replace or delegate to yours.

## Interim rules (worth agreeing on)

- Name matching follows `ingredient-matching-plan.md`: trim, lowercase, collapse spaces, simple singular.
- Units convert within mass / volume / count; incomparable units count as missing.
- Adding an item you already have (same name and unit) increases that row instead of adding a new one.
- Recipes are planned and cooked at their stored servings (no scaling yet).
- Checking off a grocery line adds exactly the missing amount to inventory.
