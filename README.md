# Smart Pantry Manager

A native Android application developed in Java that enables users to manage pantry inventory and discover recipes based on ingredients they already have available.

## Overview

Smart Pantry Manager helps users track pantry ingredients, monitor expiry dates and identify recipes they can prepare without purchasing additional ingredients.

The application implements a strict recipe-matching algorithm that checks ingredient availability and required quantities before suggesting a recipe. Recipes missing exactly one ingredient are displayed separately under the **Almost There** section.

All data is stored locally using SQLite. No internet connection or user account is required.

## Features

- **Pantry Management:** Create, view, update and delete pantry ingredients.
- **Input Validation:** Validate ingredient names and quantities before saving.
- **Inventory Persistence:** Retain pantry data between application sessions.
- **Recipe Matching:** Identify recipes for which all required ingredients and quantities are available.
- **Almost There:** Display recipes missing exactly one ingredient.
- **Recipe Details:** View ingredient lists, required quantities, units and preparation instructions.
- **Expiry Management:** Highlight expired ingredients and items approaching their expiry dates.
- **Settings:** Enable or disable the expiring-soon alert preference.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Application development |
| Android Studio | Development environment |
| SQLite | Local data storage |
| SQLiteOpenHelper | Database management |
| RecyclerView | Displaying pantry items and recipes |
| ConstraintLayout | UI layout management |
| LinearLayout | UI layout management |
| SharedPreferences | Application preferences |
| Android Intents | Activity navigation and data passing |
| JUnit | Unit testing |

## Application Structure

The application consists of five main screens:

| Screen | Functionality |
|---|---|
| Pantry List | Displays pantry inventory and provides navigation. |
| Add/Edit Ingredient | Creates and updates pantry items. |
| Suggested Recipes | Displays strict matches and near-matches. |
| Recipe Detail | Displays recipe ingredients and preparation instructions. |
| Settings | Manages the expiring-soon alert preference. |

## Database Design

The application uses SQLite with three tables:

- `pantry` - Stores pantry ingredients, quantities, measurement units and expiry dates.
- `recipes` - Stores 18 pre-loaded recipes.
- `recipe_ingredients` - Stores the ingredients and quantities required by each recipe.

Ingredient matching is performed in Java using the `RecipeMatcher` class rather than through a direct foreign-key relationship between pantry items and recipe ingredients.

## Recipe Matching

The `RecipeMatcher` class implements the application's core recipe-matching logic.

Its functionality includes:

- Ingredient name normalisation.
- Case and whitespace handling.
- Singular and plural matching.
- Compatible unit conversion, including grams and kilograms.
- Combining duplicate pantry entries.
- Comparing available quantities with recipe requirements.
- Identifying strict recipe matches.
- Identifying recipes missing exactly one ingredient.

The matching logic is separated from Android-specific components, allowing it to be tested independently.

## Testing

The `RecipeMatcher` class has eight JUnit unit tests covering the recipe-matching functionality.

## Getting Started

### Prerequisites

- Android Studio
- Java
- Android SDK API 24 or later
- Android device or emulator

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/Andiswa-Dlamini/smart-pantry.git
   ```

2. Open the project in Android Studio.
3. Allow Gradle to synchronise the project.
4. Connect an Android device with USB debugging enabled or start an emulator.
5. Build and run the application.

## Project Structure

The application follows an activity-based Android architecture, with separate Java classes responsible for screen functionality, database operations, data models and recipe-matching logic.

Core components include:

- `PantryActivity` - Main pantry screen.
- `RecipeListActivity` - Recipe suggestions and filtering.
- `RecipeDetailActivity` - Individual recipe details.
- `DatabaseHelper` - SQLite database creation and management.
- `PantryDataSource` - Pantry and recipe data operations.
- `RecipeMatcher` - Ingredient normalisation and recipe matching.

## Potential Enhancements

- Deduct pantry quantities after preparing a recipe.
- Implement device notifications for expiring ingredients.
- Allow users to create and save custom recipes.
- Add recipe search and filtering.
- Improve navigation using Android navigation components.

## Project Information

**Platform:** Android  
**Language:** Java  
**Database:** SQLite  
**IDE:** Android Studio


**Repository:** [Smart Pantry Manager](https://github.com/Andiswa-Dlamini/smart-pantry)

**Portfolio:** [Andiswa Dlamini](https://andiswadlamini.netlify.app/)
