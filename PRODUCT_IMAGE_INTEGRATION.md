# Adding Product Images and Branch-Specific Links to Distributed Drinks System

## Goal
Add product images to drinks in the Customer and Admin interfaces, with per-branch image labels/links, and store these instructions in markdown.

## Summary
1. Database: add image metadata to `drinks` or `stock`.
2. Common model: add image URL property to the `Drink` class.
3. Server: return image URLs in `getAvailableDrinks()` and optionally branch-specific images via new API.
4. Client: show images in drink selection and branch cards, optionally clickable branch links.
5. Document the mapping in markdown for documentation.

---

## 1. Database changes

### Option A: shared image by drink
```sql
ALTER TABLE drinks ADD COLUMN image_url VARCHAR(255) DEFAULT NULL;
UPDATE drinks SET image_url = 'https://example.com/images/coke.jpg' WHERE name = 'Coke';
UPDATE drinks SET image_url = 'https://example.com/images/fanta.jpg' WHERE name = 'Fanta';
UPDATE drinks SET image_url = 'https://example.com/images/pepsi.jpg' WHERE name = 'Pepsi';
```

### Option B: branch-specific image (preferred for your requirement)
```sql
ALTER TABLE stock ADD COLUMN image_url VARCHAR(255) DEFAULT NULL;
UPDATE stock SET image_url = 'https://example.com/images/nakuru/coke.jpg' WHERE branch='NAKURU' AND drink_id=1;
UPDATE stock SET image_url = 'https://example.com/images/mombasa/coke.jpg' WHERE branch='MOMBASA' AND drink_id=1;
-- etc for each branch + drink
```

---

## 2. Common model update (`common/Drink.java`)

```java
public class Drink implements Serializable {
    public int id;
    public String name;
    public double price;
    public String imageUrl; // new

    public Drink(int id, String name, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return name + " (KSH" + price + ")";
    }
}
```

---

## 3. Server data API changes (`RemoteServiceImpl`)

1. In `getAvailableDrinks()`, include image URL.
2. If you use branch-specific images, add method in `RemoteService`:
```java
List<Drink> getAvailableDrinksForBranch(String branch);
```
3. SQL example:
```sql
SELECT d.id, d.name, d.price, s.image_url 
FROM drinks d JOIN stock s ON s.drink_id = d.id 
WHERE s.branch = ?
```

---

## 4. Client UI changes

### In `CustomerClient.java`
- Add `imageLabel` to display selected drink image.
- Load with `ImageIcon` from URL or local path:
```java
String imageUrl = selectedDrink.imageUrl;
ImageIcon icon = new ImageIcon(new URL(imageUrl));
Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
drinkImageLabel.setIcon(new ImageIcon(img));
```
- In drink dropdown renderer, show icon and text: `DefaultListCellRenderer` with `setIcon`.

### Branch-specific links
- Add map in `CustomerClient`:
```java
Map<String, String> branchLink = Map.of(
    "NAKURU", "https://your.biz/branch/nakuru",
    "MOMBASA", "https://your.biz/branch/mombasa",
    "KISUMU", "https://your.biz/branch/kisumu",
    "NAIROBI", "https://your.biz/headquarters"
);
```
- Show clickable `JLabel` or button, e.g. `branchLinkButton`, and open in browser:
```java
Desktop.getDesktop().browse(new URI(branchLink.get(selectedBranch)));
```

### Optional: admin side `AdminClient.java`
- Add per-branch image cards in report panel.
- Use same branch link map and display with `JLabel` and `setIcon`.

---

## 5. Markdown documentation per request
- File created: `PRODUCT_IMAGE_INTEGRATION.md`
- Add section with per-branch image path lists:
```markdown
## Branch-specific product image mappings
- NAKURU Coke: https://example.com/images/nakuru/coke.jpg
- NAKURU Fanta: https://example.com/images/nakuru/fanta.jpg
- ...
``` 

---

## 6. Example branch-specific product imaging table

| Branch  | Drink | Image URL |
|--------|-------|-----------|
| NAKURU | Coke  | `/assets/nakuru/coke.png` |
| NAKURU | Fanta | `/assets/nakuru/fanta.png` |
| MOMBASA| Coke  | `/assets/mombasa/coke.png` |

---

## 7. Running the update
1. Apply DB migrations with MySQL script.
2. Recompile everything (`common`, `server`, `client`).
3. Restart server and clients.
4. Verify UI displays images and branch links work.

---

## 8. Additional notes
- For local packaging, place images in `client/resources/images/{branch}/` and use classpath load.
- Keep image resolution moderate (e.g., 150x150 px) to avoid delay.

---

## 9. Helpful command
```powershell
# compile all
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d out common/src/main/java/common/*.java
javac -cp "out;lib/mysql-connector-j-8.3.0.jar" -d out server/src/main/java/server/*.java
javac -cp "out;lib/mysql-connector-j-8.3.0.jar" -d out client/src/main/java/client/*.java
```

---

## 10. Specifically for your query
- Yes: add image field to products, branch-specific image URLs to `stock`, and show in GUI.
- Yes: provide branch-specific mapping in `.md` (done in this document).
- No further action is required for format; update docs and code as above.
