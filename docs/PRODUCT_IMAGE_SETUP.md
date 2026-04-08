# Product Image Setup (Branch Specific)

This project now supports branch-specific product images in the Customer UI.

## 1. Where To Put Images

Create image files in:

- `client/assets/images/NAKURU/`
- `client/assets/images/MOMBASA/`
- `client/assets/images/KISUMU/`
- `client/assets/images/COMMON/` (fallback for all branches)

## 2. File Naming Rule

Use drink names converted to lowercase slug format:

- `Coke` -> `coke.png`
- `Fanta Orange` -> `fanta_orange.png`
- `Pepsi` -> `pepsi.jpg`

Supported extensions:

- `.png`
- `.jpg`

## 3. How The App Resolves Images

For each drink card, the app checks in this order:

1. `client/assets/images/<BRANCH>/<drink_slug>.png`
2. `client/assets/images/<BRANCH>/<drink_slug>.jpg`
3. `client/assets/images/COMMON/<drink_slug>.png`
4. `client/assets/images/COMMON/<drink_slug>.jpg`

If no file is found, the UI shows `No Image`.

## 4. Example

For `Coke` in `MOMBASA` branch:

- preferred: `client/assets/images/MOMBASA/coke.png`
- fallback: `client/assets/images/COMMON/coke.png`

## 5. Notes

- Nairobi is treated as Headquarters (admin-only), so no customer branch image folder is needed for Nairobi.
- Restart the Customer client after adding/changing images.
