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

## Board colours

Each Arkham Horror location button uses its neighbourhood's card-back colour. The nine Arkham neighbourhoods were sampled from card art in two fan Arkham Companion apps. The Dunwich, Kingsport and Innsmouth neighbourhoods were described from the physical cards and matched by eye. The colours are in `ui/arkham/ArkhamScreens.kt`. The expansion neighbourhoods (Village Commons, Harborside, Church Green and the rest) come from the Arkham Companion database and the wiki, which agree on all 30 locations.

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
- **Ye Olde Magick Shoppe:** three cards had lost their expansion code; checked against the physical cards:
  - "Ah, there you are. That item you ordered…" is King in Yellow.
  - "A famous occultist is in town giving a lecture…" is Black Goat of the Woods.
  - "A group of concerned citizens have come to harangue…" is Innsmouth Horror.
- **Curse of the Dark Pharaoh editions:** the data mixed the original and revised editions. Three original-edition cards that use *Barred* were removed, keeping their revised Patrol-marker twins (checked against the revised edition): the Administration Building card that had been tagged as the base game, and one card each at the Graveyard and Arkham Asylum.
- **Bank of Arkham:** the seizure encounter had been tagged as the base game; it is Curse of the Dark Pharaoh (checked against the physical cards).
- **Darke's Carnival:** the "confused old man" encounter is not in the Dunwich Horror deck (checked against the physical cards). It was moved to Miskatonic Horror + Black Goat, the one card that location was missing; the Arkham Horror wiki confirms Miskatonic Horror + Black Goat.
- **Congregational Hospital:** the "pill to help you relax" encounter is not in the Kingsport Horror deck (checked against the physical cards); the Arkham Horror wiki lists it as Miskatonic Horror with the Kingsport and Innsmouth icons, so it now needs Miskatonic Horror, Kingsport and Innsmouth.
- **Cards relabelled from King in Yellow to Curse of the Dark Pharaoh:** seven cards with Egyptian, Dark Pharaoh, Patrol-marker or museum themes, at Arkham Asylum and the Administration Building. Two more Administration Building cards (the research assistant and the Civil War thesis encounters) were confirmed as Dark Pharaoh against the physical cards. The Arkham Asylum "ancient Sumerian" encounter was also confirmed as Dark Pharaoh.
- **Text repairs:**
  - **Field separators:** a `%` inside the Inner Sanctum text had split the row ("1 Clue Token = $2").
  - **Markup:** stray `[[` markup removed, and spaces restored where the old HTML had glued words together ("Clue Tokenyou").
  - **Typos:** "THe", "lf", "tothe", "speacks", "Antartica", "pedistals", "heiroglyphics", "posses", "over to the Causeway".

### Verified against the Arkham Horror wiki

Every location's encounters were compared, card by card, with the encounter tables on the [Arkham Horror 2nd Edition wiki](https://arkham-horror-second-edition.fandom.com/). After the fixes above, all 57 locations agree with it. The wiki also showed three more fixes:
- **Devil Reef:** "You spend the entire night out on the reef…" is Miskatonic Horror + Innsmouth, not Innsmouth.
- **Combat checks:** three location cards and one Dreamlands card really do say "Combat check" (a Fight check that adds weapon bonuses). The conversion had wrongly changed them to Fight; they have been restored, and the app colours them like Fight checks.
- **First National Grocery:** has 14 Innsmouth cards and no Miskatonic Horror cards, the same as the wiki.

The wiki also lists 18 cards that are only in the original edition of Curse of the Dark Pharaoh. They are left out, because the revised edition is the one in use.

### Other Worlds and First National Grocery: imported from the Arkham Companion app

The wiki has no text for Other World encounters, so the gate deck was compared with the card database of the original **Arkham Companion** Android app ([DeckardSix/arkham-companion](https://github.com/DeckardSix/arkham-companion)). That database stores every physical gate card with its expansions, gate colours and encounters, and has the same card counts as our verified data at 56 of the 57 locations. Using it:

- **Added 162 Other World encounters:**
  - mostly Miskatonic Horror gate cards, some of which need three expansions (e.g. Miskatonic Horror, Dunwich and Kingsport);
  - the 16 Innsmouth Horror "Other" entries;
  - the revised Curse of the Dark Pharaoh cards.
- **Removed 28 original-edition Curse of the Dark Pharaoh entries.** Their revised replacements are among the additions.
- **Two entries retagged from the database:** Lost Carcosa's "glass case" encounter is Miskatonic Horror + Curse of the Dark Pharaoh, and "Noatalba is performing a ritual…" is Miskatonic Horror + Curse of the Dark Pharaoh + Kingsport.
- **Duplicates removed:**
  - another misfiled Great Hall of Celeano card that had been filed under Another Time ("An almost-overlooked fragment of text…");
  - a duplicate of Plateau of Leng's "icy winds" encounter.
- **Added First National Grocery's 20 Miskatonic Horror cards.** These were missing from both the 2012 data and the wiki.

Every imported or retagged card has a `note` saying so, which makes it easy to spot-check against the physical cards. Every world now follows the same regular pattern, and the Dunwich and Kingsport worlds have 32 encounters each.

There are no open questions about the card data at the moment.
