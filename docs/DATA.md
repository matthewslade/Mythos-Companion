# Card data

The encounter text lives in two JSON files in `app/src/main/assets/`. They are the source of truth: edit them directly, then run `./gradlew testDebugUnitTest`. `CardDataValidationTest` checks ids, references and markup.

## Text markup

Both games use the same small markup, which `ui/components/Markup.kt` renders:

| Markup | Meaning |
| --- | --- |
| `**text**` | bold, e.g. Arkham skill checks: `**Luck (-1) [2] check**` (`[2]` is the number of successes needed) |
| `*text*` | italic, e.g. `*delayed*`, `*lost in time and space*` |
| `[LORE - 1]` | Eldritch skill test, always shown bold |
| line break (`\n`) | a new line; tables of results use one line per row |

The app also colours game terms itself and puts a small icon in front of each one, so the data needs no colour markup:
- resources: Health/Stamina, Sanity, Clues, Focus and money;
- skill tests: the Eldritch skills use the investigator sheet colours (Lore purple, Influence orange, Observation green, Strength red, Will blue);
- threats: Doom, Eldritch tokens, the Omen, Gates, Monsters and the terror level;
- Conditions and special cards, Blessed and Cursed;
- statuses such as Delayed, Detained, Lost in Time and Space and devoured;
- things you gain, such as Unique Items, Spells, Allies and Tickets;
- Eldritch card traits (RELIC, INCANTATION and so on), shown as small capitals.

The rules live in `ui/components/Terms.kt`. The on-device test `MarkupScreenshots` renders a real example card for every term kind in light and dark mode.

## `eldritch.json`

```json
{ "id": "A1", "deck": "americas", "expansion": "base",
  "sections": [ { "header": "Arkham", "text": "…" }, { "header": "San Francisco", "text": "…" }, { "header": "Buenos Aires", "text": "…" } ] }
```

- Every card has three sections (top, middle and bottom), as in the Eldritch Companion app.
- **Split decks:** the app draws each section of a location, General or Research card as its own encounter, as the Arkham side does, so the Americas cards become separate Arkham, San Francisco and Buenos Aires decks. The split happens when a game is built (`EldritchDecks.SPLIT`), so the data keeps the printed cards. A split encounter's id is the card id plus its section number, e.g. `A1#2` is the San Francisco part of card `A1`.
- **Named cards** (gates, expeditions, Mystic Ruins, Dream-Quest, Special, Disaster and Devastation) also carry a `title`. Their first section is the lead-in, followed by `Pass` and `Fail`; Disaster cards have no headers.
- **Research and Special** cards carry an `ancientOne`, and only enter the game for that Ancient One.
- **Source:** converted from the Eldritch Companion's `assets/cards.xml` (1,086 cards, Base through Masks of Nyarlathotep). The only change was making skill-test notation consistent (`[LORE – 1]` and `[LORE-1]` became `[LORE - 1]`).

## `arkham.json`

- `expansions`, `locations` and `otherWorlds` describe the board. The `locations` list includes the Arkham neighbourhoods. The `otherWorlds` list includes each world's gate colours, taken from the 2012 app.
- **Encounters:** `locationEncounters` and `otherWorldEncounters` list every encounter.
  - Each encounter's `expansions` must all be in play for it to be used.
  - Miskatonic Horror cards always list `MH` together with the expansion they extend, e.g. `["MH", "KH"]`.
  - Other World encounters with `"world": null` are the generic **Other** entries, and they carry a gate `colour`.
- **Ids:** these are stable and are what saved games refer to. Don't renumber existing cards. Give new cards new ids.

### Corrections made to the 2012 data

The old `ae.tsv` / `oe.tsv` files were converted with these fixes:

- **Removed rows:**
  - 34 rows filed under Wizard's Hill were an exact copy of the Wireless Station deck. They were removed, leaving Wizard's Hill with its 34 real cards.
  - A blank South Church row was removed.
- **Moved rows:** four Great Hall of Celeano cards (book and library encounters) had been filed under Another Time. Moving them back gives Celeano the same counts as the other base worlds.
- **Broken expansion codes:**
  - `Dh` became `DH`.
  - `BW`/`DW` became `BG`, at Arkham Asylum, which otherwise had no Black Goat cards.
  - `GW` became `BG`, at Hall School.
  - `DH|DH`, and `DH|xx` where `MH|xx` was meant, at Gardners' Place, Whateley Farm and Bishop's Brook Bridge.
  - An `MH` card with no second code at 607 Water St. was given Kingsport.
- **Cards relabelled from King in Yellow to Curse of the Dark Pharaoh:** seven cards with Egyptian, Dark Pharaoh, Patrol-marker or museum themes, at Arkham Asylum and the Administration Building.
- **Text repairs:**
  - **Field separators:** a `%` inside the Inner Sanctum text had split the row ("1 Clue Token = $2").
  - **Markup:** stray `[[` markup removed, and spaces restored where the old HTML had glued words together ("Clue Tokenyou").
  - **Typos:** "THe", "lf", "tothe", "speacks", "Antartica", "pedistals", "heiroglyphics", "posses", "over to the Causeway" and "Combat (-1) check" (the game calls it Fight).

### Please check against the physical cards

Most locations follow a very regular pattern, so the places below stand out. Each needs someone to look at the real cards. Card ids are shown in the app, at the bottom right of every card.

1. **Ye Olde Magick Shoppe:** the original data had lost the expansion of three cards. Each one has a `note` in the JSON. The location was missing exactly one Black Goat, one Innsmouth and one King in Yellow card, so the three were assigned one each. Which card belongs to which expansion is a guess:
   - "Ah, there you are. That item you ordered…" is set to **BG**.
   - "A famous occultist is in town giving a lecture…" is set to **IH**.
   - "A group of concerned citizens have come to harangue…" is set to **KY**.
2. **Administration Building:**
   - It has 8 base cards, where most Arkham locations have 7. Two of them are the identical "monograph" encounter; check whether the base game really has it twice.
   - It also has 6 King in Yellow and 3 Curse of the Dark Pharaoh cards, against the usual 4 and 5.
3. **Bank of Arkham:** 8 base cards and 4 Dark Pharaoh cards, against the usual 7 and 5. One "base" card may belong to Curse of the Dark Pharaoh. A likely candidate is the bank robbery card, which used to say "Combat (-1) check".
4. **Arkham Asylum:** 5 King in Yellow cards against the usual 4.
5. **Graveyard:** 6 Dark Pharaoh cards against the usual 5. The revised edition of Curse of the Dark Pharaoh may account for this.
6. **Expansion-board locations with one card in the wrong place:**
   - Darke's Carnival: 15 Dunwich and 1 MH+Black Goat card; 14 and 2 expected.
   - Congregational Hospital: 15 Kingsport and 1 MH+Innsmouth card; 14 and 2 expected.
   - Devil Reef: 15 Innsmouth and 7 MH+Innsmouth cards; 14 and 8 expected.
7. **First National Grocery:** has no Miskatonic Horror cards at all; 20 would be expected. They were missing from the 2012 data and can be added if you have them.
8. **Other Worlds:**
   - The Plateau of Leng and Great Hall of Celeano have no Miskatonic Horror entries, while the other base worlds have four each.
   - Celeano also has 3 Dunwich entries where the other worlds have 4.
   - Lost Carcosa has one Curse of the Dark Pharaoh entry.
