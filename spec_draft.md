# Build Spec — PoGo Raid-Counter Search Expander (Tasker / BeanShell)

**Target:** extend an existing Tasker BeanShell text-expander module so that a single
shortcut like `@#raid-fire.` or `@#raid-dragon-steel.` expands, in-place inside the
Pokémon GO search bar, into a search string that lists the **top raid attackers with
their exact optimal movesets** followed by a **type-based catch-all fallback**.

Data is **baked in statically**. The runtime script must **not** make any network calls.
You (Claude Code) fetch the ranking data once from the GO Hub API, transcode it, and
hardcode it into the script. When the meta shifts, re-run the fetch and regenerate.

---

## 1. Context — the existing module

The current module registers a `Consumer` under the trigger key `@#` in a Tasker Java
variable `textExpanderReplacements`. It fires from an AccessibilityService, reads the
focused node's text, regex-scans for `@#<keyword>` shortcuts, replaces them, and writes
the text back via `ACTION_SET_TEXT`.

- Trigger regex (keep, already supports hyphens if you widen the class):
  `@#([\w-]+)(?:\[(\d+)\]|\.)`
  - group 1 = keyword (allow letters, digits, `-`)
  - group 2 = optional bracket parameter, e.g. `@#recent[3]`
  - terminator is either `.` or `]`
- Existing static templates must keep working unchanged:
  `pvp, transfer, trash, hundo, shundo, legendary, mythical, shadow, purified, recent`
  including the `{DAYS:n}` → `age0` / `age0-(n-1)` expansion used by `transfer/trash/recent`.
- Language/runtime: **BeanShell** (loose-typed Java; no generics enforcement, no
  Java-8 lambdas — use anonymous classes; `void` is a valid "unset" sentinel in Tasker
  BeanShell). Keep it compatible with the existing file's style.

The original working file is the canonical base to extend. Do not break it.

---

## 2. The unified command

One command family. No separate `top-` vs `raid-` commands — the output must be a single
comma-joined OR chain that is itself chainable (the user may append `&…` filters).

| Shortcut | Meaning |
|---|---|
| `@#raid-TYPE.` | Counters for a **single-type** boss |
| `@#raid-T1-T2.` | Counters for a **dual-type** boss (chart-aware cancellation) |
| `@#raid-TYPE[CP].` | Same, with a CP floor applied to every term (`&cpCP-`) |
| `@#raid-T1-T2[CP].` | Same, dual-type, with CP floor |

`TYPE` is one of the 18 lowercase type names:
`normal fire water electric grass ice fighting poison ground flying psychic bug rock ghost dragon dark steel fairy`.

### 2.1 Single-type behavior (`@#raid-fire.`)

1. Look up the boss type's weaknesses (the attack types that are super-effective).
   For a single type this is just the chart's "weak to" list — **do not** compute it
   yourself at runtime; you can, but the native operator (step 3) already handles the
   fallback, so the curated list only needs the weakness types to pick the right
   attacker buckets.
2. **Curated block:** for each SE attack type, emit that type's baked-in top-N attackers
   as precise tokens `species&@fast move&@charged move`. De-duplicate by species across
   all SE types (first occurrence wins).
3. **Fallback block:** append the native operator `>TYPE`. Per Niantic, `>fire` returns
   every Pokémon in storage that has *any* move super-effective against fire — this is
   the safety net that catches anything not in the curated list.
4. Join curated tokens + fallback with commas (OR). Result for `@#raid-fire.`:

   ```
   <top water/ground/rock attackers, exact sets, deduped>,>fire
   ```

### 2.2 Dual-type behavior (`@#raid-dragon-steel.`)

`>TYPE` only accepts one type, so it **cannot** express dual-type cancellation. Compute it:

1. For every attack type, multiply its effectiveness against **both** defending types
   (see §6 for the multipliers and chart). Keep only attack types whose **product ≥ 1.5×**
   (i.e. still net super-effective after the other half's resist/immunity). Order
   double-SE (≥2.5×) first, then single-SE.
   - `dragon-steel` → Ice/Dragon/Fairy each SE on Dragon but resisted by Steel → cancel to
     1.0× → dropped. Fire SE on Steel but resisted by Dragon → dropped. **Survivors:
     Fighting, Ground** (both neutral on Dragon, SE on Steel).
2. **Curated block:** union of the baked-in top-N attackers for each surviving SE type,
   deduped by species.
3. **Fallback block:** since `>` is single-type only, emit `@type` (move-type filter) for
   **each surviving SE type** instead: `@fighting,@ground`. This catches any mon carrying a
   move of a surviving SE type.
4. Join with commas. Result for `@#raid-dragon-steel.`:

   ```
   <top fighting+ground attackers, exact sets, deduped>,@fighting,@ground
   ```

### 2.3 CP floor `[CP]`

If a bracket number is present, append `&cpCP-` (open-ended lower bound) to **every**
comma-separated term individually — do **not** rely on it binding to the whole chain
(see §7.2 precedence warning). Example `@#raid-fire[2500].`:

```
garchomp&@mud shot&@earth power&cp2500-,…,>fire&cp2500-
```

---

## 3. Data acquisition (run once, offline of the phone)

### 3.1 Endpoint

`POST https://db.pokemongohub.net/api/counters`
Content-Type: `text/plain;charset=UTF-8`. Body is a JSON blob describing a neutral dummy
defender ("BOB", normal-type) and ranking options. The field that selects which ranking
you get is **`rankForType`**.

Loop `rankForType` over all 18 types to harvest every bucket you'll ever need (single- and
dual-type queries only ever reference these 18 per-type lists).

### 3.2 Payload config decisions — **read this, it changes results**

The sample payload the user provided has shadows, megas, and primals **enabled**. The
user's reference infographic is titled **"Top *Basic* Raid Attackers"** — base forms only.
Decide deliberately and keep it consistent with intent:

| Field | Sample value | Recommendation for this use case | Why |
|---|---|---|---|
| `includeShadowPokemon` | `true` | **`false`** (default) | Shadow optimal sets often assume Frustration is TM'd off; encoding shadows needs a `shadow&` prefix and complicates dedupe. Enable only if the user wants shadows and you add the prefix + moveset handling. |
| `includeMegaPokemon` | `true` | **`false`** (default) | You can only field one Mega; Mega names collide with base species in search; adds noise. |
| `includePrimalPokemon` | `true` | **`false`** (default) | Same as Mega. |
| `weather` | `"extreme"` | keep `"extreme"` | Neutral / no-boost ranking; avoids weather-biasing the list. Verify this is the API's "no boost" sentinel. |
| `allowLegacyMoves` | `true` | keep `true` | The optimal sets in the infographic use legacy moves (Force Palm, Meteor Mash, Blast Burn, etc.). Search matches them fine by name; if the user lacks the legacy move on a given mon it simply won't match — desired. |
| `allowOfftypeAttackers` | `true` | your call | If `true`, a bucket may include a mon whose *best* set for that type is off-STAB. Usually fine; set `false` for stricter on-type parity with the infographic. |
| `allowMixedMovesets` | `true` | keep `true` | Lets fast/charged come from different sources for max DPS — matches "optimal set" intent. |
| `targetRaidLevel` | `"5"` | keep `"5"` | T5/legendary raid context. |
| `responseSize` | `10` | **`10`** | User wants top-10 per type (but see §7.3 length risk). |

Make each of these a clearly-labeled constant/flag in your fetch script so a regen is a
one-line change.

### 3.3 Reference curl (vary `rankForType`)

```bash
curl 'https://db.pokemongohub.net/api/counters' \
  --compressed -X POST \
  -H 'Content-Type: text/plain;charset=UTF-8' \
  -H 'Origin: https://db.pokemongohub.net' \
  -H 'Referer: https://db.pokemongohub.net/best/attackers-per-type' \
  --data-raw '{"target":{"maxCP":5000,"atk":200,"def":180,"sta":200,"id":0,"name":"BOB","pokemonId":"BOB","type1":"normal","isMythical":false,"isLegendary":false,"generation":0,"candyToEvolve":0,"kmBuddyDistance":0,"baseCaptureRate":0,"baseFleeRate":0,"kmDistanceToHatch":0,"thirdMoveStardust":0,"thirdMoveCandy":0,"is_deployable":false,"is_transferable":false,"isTradable":false,"form":null,"formId":"","template_id":"BOB_THE_DEFENDER","isAvailable":false,"isHidden":false,"disableTransferToPokemonHome":false,"isShinyAvailable":false,"isDynamaxAvailable":false,"dynamaxTier":0},"locale":"en","targetRaidLevel":"5","weather":"extreme","responseSize":10,"includeShadowPokemon":false,"includeMegaPokemon":false,"includePrimalPokemon":false,"includeUnavailablePokemon":false,"allowLegacyMoves":true,"rankForType":"RANK_TYPE_HERE","allowMixedMovesets":true,"allowOfftypeAttackers":true,"allowedAttackerChargeMoveId":null,"allowedAttackerQuickMoveId":null,"allowOnlyDynamaxEligiblePokemon":false}'
```

---

## 4. Data extraction & transcoding

The API response is JSON. **Inspect the actual shape first** — field names below are
predicted, confirm them:

- Per attacker, you need three things: **species display name**, **fast move display
  name**, **charged move display name**. Look for a `moveset`/`bestMoveset` object or
  `move1`/`move2`, or `quickMove`/`chargeMove`, likely with both an ID
  (`MUD_SHOT_FAST`) and a localized `name` ("Mud Shot"). **Use the localized `en` name**,
  not the ID.

### 4.1 Turn each into a search token

Token format (see §7 for the syntax rules this relies on):

```
<species>&@<fast name>&@<charged name>
```

Normalization rules:

- **Lowercase** everything (search is case-insensitive).
- **Preserve internal spaces** in move names and species names — spacing is required for
  proper-name matching. `Mud Shot` → `@mud shot`, `Earth Power` → `@earth power`.
- Strip trailing legacy markers/asterisks (those come from the infographic, not the API).
- **Forms:** the moveset usually disambiguates the form on its own (e.g. only Galarian
  Darmanitan can run `@ice fang&@avalanche`), so `darmanitan&@ice fang&@avalanche`
  selects the right form without needing a form qualifier. Prefer relying on the moveset.
  For regional forms that share a moveset with another form, add the region word if the
  search supports it (e.g. `alolan`, `galarian`, `hisuian`) — verify on device.
- **Shadows (only if enabled):** prefix `shadow&` → `shadow&mamoswine&@powder snow&@avalanche`.
- **Hyphenated names** (Ho-Oh, Porygon-Z): keep the hyphen; verify it matches on device.

### 4.2 The baked-in structure

Emit a single map literal in the BeanShell script, one entry per attack type, value =
comma-joined tokens, e.g.:

```java
topByType.put("ground",
    "garchomp&@mud shot&@earth power,"
  + "excadrill&@mud slap&@scorching sands,"
  + "rhyperior&@mud slap&@earthquake");   // …up to N entries
```

Populate all 18 types. `normal` may be empty (no normal-type raid-attacker meta) — handle
an empty bucket gracefully (skip it, still emit the fallback).

A tunable constant `ATTACKERS_PER_TYPE` (default 10) should cap how many of the fetched
entries you actually bake per type, so trimming for length (§7.3) is a one-line change and
a re-transcode, not a hand-edit of the map.

---

## 5. Search-string assembly (runtime, in BeanShell)

For a parsed `@#raid-<typepart>[param]`:

1. Split `typepart` on `-` → 1 or 2 boss types. Validate each is a known type; if invalid
   or >2 types, leave the shortcut untouched (append original match unchanged).
2. Determine the SE attack-type list:
   - 1 boss type → its `weakTo` list.
   - 2 boss types → run the §6 multiplier loop, keep ≥1.5×, double-SE first.
3. Curated tokens: for each SE type in order, pull its baked bucket, split on `,`,
   dedupe by species (substring before first `&`), collect.
4. Fallback tokens: single → `[">"+bossType]`; dual → `["@"+t for t in SE types]`.
5. If a CP param is present, append `&cp<param>-` to **each** token in both blocks.
6. Join everything with `,`. That string is the replacement.

Edge cases:
- Empty curated set (e.g. all buckets empty) → still emit the fallback so the search is
  never empty.
- Guard against a bucket referencing a species already emitted from an earlier SE type.

---

## 6. Reference — type chart (already validated, use as-is)

Pokémon GO damage multipliers:

| Relation | Multiplier |
|---|---|
| Super-effective (SE) | ×1.6 |
| Not very effective (NVE / resist) | ×0.625 |
| "Immune" (0× in main series) | ×0.390625 |

Dual-type = product of both. Thresholds for the dual-type filter: **≥2.5 = double-SE**,
**≥1.5 = single-SE (keep)**, everything else dropped.

**weakTo** (defending type → attacking types that are SE):

```
normal:   fighting
fire:     water, ground, rock
water:    electric, grass
electric: ground
grass:    fire, ice, poison, flying, bug
ice:      fire, fighting, rock, steel
fighting: flying, psychic, fairy
poison:   ground, psychic
ground:   water, grass, ice
flying:   electric, ice, rock
psychic:  bug, ghost, dark
bug:      fire, flying, rock
rock:     water, grass, fighting, ground, steel
ghost:    ghost, dark
dragon:   ice, dragon, fairy
dark:     fighting, bug, fairy
steel:    fire, fighting, ground
fairy:    poison, steel
```

**resistsFrom** (defending type → attacking types it resists, ×0.625):

```
fire:     fire, grass, ice, bug, steel, fairy
water:    fire, water, ice, steel
electric: electric, flying, steel
grass:    water, electric, grass, ground
ice:      ice
fighting: bug, rock, dark
poison:   grass, fighting, poison, bug, fairy
ground:   poison, rock
flying:   grass, fighting, bug
psychic:  fighting, psychic
bug:      grass, fighting, ground
rock:     normal, fire, poison, flying
ghost:    poison, bug
dragon:   fire, water, grass, electric
dark:     ghost, dark
steel:    normal, grass, ice, flying, psychic, bug, rock, dragon, steel, fairy
fairy:    fighting, bug, dark
(normal has no resistances)
```

**immuneTo** (defending type → attacking types that "miss", ×0.390625):

```
normal: ghost
ghost:  normal, fighting
dark:   psychic
fairy:  dragon
flying: ground
ground: electric
steel:  poison
```

Multiplier lookup per (attacker `atk`, defender `def`): if `atk ∈ immuneTo[def]` → 0.390625;
else if `atk ∈ weakTo[def]` → 1.6; else if `atk ∈ resistsFrom[def]` → 0.625; else 1.0.

---

## 7. Pokémon GO search syntax — rules this depends on

Confirmed from Niantic's help center, the GO Hub cheat sheet, and the community wiki:

- `@<move name>` matches any Pokémon that knows that named move, in any slot (e.g.
  `@scratch`). Move *name* matching does **not** take a slot number.
- `@1<type>` / `@2<type>` / `@3<type>` match by **move type** in the fast / first-charged /
  second-charged slot respectively (e.g. `@3ghost` = ghost-type second charged move).
  **Slot numbers only work with types, never with move names.**
- `@<type>` (no number) matches any Pokémon with a move of that type in any slot.
- `>TYPE` matches every Pokémon that has a move super-effective against `TYPE` (single type
  only). `<TYPE` matches Pokémon weak to that type (defensive; not used here).
- `&` = AND, `,` = OR, `!` = NOT, `cpN-` = CP ≥ N.
- Because Mud Shot / Earth Power / etc. each exist in only one slot, `@mud shot&@earth power`
  already pins the right fast+charged pair without slot numbers.

### 7.1 Move-name spacing — **verify on device**
Spacing is generally optional, **except for proper names of Pokémon, moves, and tags**,
where the internal spaces are part of the match. So bake `@mud shot`, `@earth power`,
`@dazzling gleam` **with** spaces. Confirm on-device that a space inside a token is treated
as part of the name and not as a delimiter; if the game trims/rejects it, fall back to
`@1<type>&@2<type>` (type-slot) tokens for that entry.

### 7.2 Operator precedence — **verify, do not depend on it**
"OR of AND-groups" (`monA&x&y , monB&x&y , >type`) requires `&` to bind tighter than `,`.
This is the standard and the only interpretation under which team-building strings work,
but the community has reported ambiguity. Two mitigations, both applied:
- Keep every attacker as a self-contained `&`-group; only put `,` **between** complete
  groups.
- Apply the CP floor per-term (§2.3) rather than once at the end.
Have Claude Code empirically confirm precedence on device with a 2-mon test string and note
the result in a comment.

### 7.3 Character-limit risk — **verify, then tune**
Single-type bosses can weakness up to 5 SE types; dual-type survivors can also be several.
At `ATTACKERS_PER_TYPE=10` with `species&@fast&@charged` tokens (~35–45 chars each) plus a
CP suffix, a worst-case string can run into the hundreds of characters and may exceed the
search bar's input limit (limit is undocumented — test it). If truncation occurs:
- lower `ATTACKERS_PER_TYPE` (the single tunable), and/or
- drop to charged-move-only tokens (`species&@charged`) to roughly halve length, and/or
- rely more on the `>type` / `@type` fallback and keep only the top 3–5 curated.
Design the code so this is a constant change + re-transcode, never a rewrite.

---

## 8. On-device verification checklist

1. `@#raid-fire.` populates and the search actually filters your box to fire counters.
2. A pinned entry (e.g. `garchomp&@mud shot&@earth power`) matches **only** your Garchomp
   with that exact set, not one running Outrage. (Confirms name+space matching.)
3. `@#raid-dragon-steel.` yields only fighting/ground counters + `@fighting,@ground` — no
   ice/dragon/fairy/fire leakage. (Confirms cancellation math.)
4. `@#raid-fire[2500].` applies the CP floor to every term. (Confirms precedence handling.)
5. Longest realistic string (a 3-weakness single type at N=10) doesn't truncate.
6. All original static templates still expand correctly.

---

## 9. Acceptance examples (expected shape, exact mons depend on live API)

```
@#raid-fire.
→ garchomp&@mud shot&@earth power, excadrill&@mud slap&@scorching sands,
  rhyperior&@mud slap&@earthquake, …(water & rock buckets, deduped)…, >fire

@#raid-dragon-steel.
→ lucario&@force palm&@aura sphere, …(fighting bucket)…,
  …(ground bucket, deduped)…, @fighting, @ground

@#raid-ice-flying.
→ (rock is double-SE ×2.56, so rock attackers lead),
  …fire/electric/steel buckets…, @rock, @fire, @electric, @steel

@#raid-fire[2500].
→ each term above with &cp2500- appended
```

(Commas shown with spaces for readability; emit them without surrounding spaces.)

---

## 10. Deliverable

A single updated `.bsh` file that:

- preserves the existing static-template behavior and `{DAYS:n}` logic,
- adds the unified `@#raid-…` command per §2 and §5,
- contains the fully-populated `topByType` map (all 18 types, ≤`ATTACKERS_PER_TYPE`
  entries each, exact movesets as `@name` tokens with spaces),
- embeds the §6 chart,
- exposes `ATTACKERS_PER_TYPE` and the "pin fast move?" behavior as top-of-file constants,
- includes a short header comment recording the fetch date and the payload flags used
  (basic vs shadow/mega), so the next regen is unambiguous.

Also provide the throwaway fetch/transcode script (any language) used to generate the map,
so the user can re-run it after future meta shifts.
