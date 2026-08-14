# Equivox automation (AE2 / Refined Storage)

## Energy Condenser
- **MK1:** one shared inventory on all faces — insert EMC items, extract only the lock target.
- **MK2 (preferred for buses):**
  - **Sides** → insert items to burn
  - **Top / bottom** → extract condensed products
- Put the target in the lock slot (GUI), then point an importer/storage bus at the output face.

## Transmutation Provider
Bridge for **Transmutation Table knowledge / personal EMC**.

1. Place the **Provider**.
2. Place a **Transmutation Table on top of it** (Provider must be directly under the table).
3. Stay online as the owner — buses only work while you are online.
4. Attach an AE2/RS **storage bus / external storage** to the Provider.

Learned items appear as virtual stock **only if you can afford at least 1** from personal EMC (burn valuables in the Transmutation Table). Amounts track your EMC; extract spends it. Use a **Storage Bus** on the Provider (a cable alone is not enough).

**Tome of Knowledge:** full-knowledge players are not indexed (EMC map is too large). Learn items normally, or remove the tome, for bus export.

**GUI check:** Provider must show Device Online, table linked, owner online, Exposed > 0. If Exposed is fine but the terminal is empty, you likely have 0 personal EMC for those items.

## Arcane Tablet
Portable crafting that pulls from inventory first, then learned items / EMC.
- JEI recipe transfer works when `enable_optional_integrations=true` (JEI present).
- For network autocrafting of EMC items, use a **Transmutation Provider** under a Table + storage bus.

## Transmutation Table
Player GUI for learning / manual transmute. Put it **on** a Provider to unlock AE2/RS export of that owner's knowledge.
