# xBudget

Personal finance app for Android. Track income and expenses by category, view spending trends, and compare against budget targets.

## Architecture

Single-activity app with Fragment-based navigation:

```
MainActivity          Host activity, fragment transactions, listener wiring
TransactionFragment   Monthly income/expense lists per category
StatisticsFragment    Period comparisons (daily/weekly/monthly/yearly)
DataViewFragment      Bar charts for income, expenses, and savings
```

Fragments communicate back to `MainActivity` via listener interfaces. Database access goes through a custom `DatabaseHelper` with a `GeneralCursorWrapper` for result mapping. Preferences (e.g. currency) are stored as key/value rows in a Constants table rather than SharedPreferences.

## Tech stack

- Views + AppCompat (Material Components 1.x)
- SQLite via custom `DatabaseHelper`
- MPAndroidChart for bar chart visualisation
- Joda-Time for date arithmetic and period grouping
- Currency Picker Android for currency selection

## Features

- Income and expense transactions with name, amount, category, and timestamp
- Custom categories with monthly budget allowances
- Statistics screen with period-over-period and historical-average comparisons
- Bar charts for monthly income/expense breakdown and yearly savings
- Multi-currency support with flag picker
- Guided onboarding for first-time users

## Building

Requires JDK 17.

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```
