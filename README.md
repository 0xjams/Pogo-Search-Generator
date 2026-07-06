# pogosearch

Tasker text expander that turns shortcuts typed in the Pokémon GO search bar
into raid-counter search strings — exact per-species optimal-moveset matching,
within the limits of the game's search grammar (verified on device: OR binds
tighter than AND, no parentheses, prefix-based `@move` matching, ~200-char
field).

## Commands

| Shortcut | Meaning |
|---|---|
| `@#raid-fire.` | balanced counters vs a single-type boss |
| `@#raid-dragon-steel.` | counters vs a dual-type boss (type-chart cancellation) |
| `@#atk-water.` | one attack-type bucket, exact optimal builds |
| `@#attkr-water.` | every vouched attacker of that type (loose view) |
| `@#raidn-fire[2]` | exact query for the 2nd-ranked counter |
| `[CP]` suffix | CP floor on raid/atk/attkr, e.g. `@#raid-fire[2500]` |

Plus the original static templates (`pvp`, `transfer[days]`, `hundo`, ...).

## Files

- `transcode.py` — data + generator. All curated layers live here:
  `BEST` (GO Hub rankings, pasted verbatim), `BUDGET` (budget infographic
  transcription), `EXTRA_SETS` (contested/co-equal sets and non-legacy
  fallbacks), `EXTRA_LEARNABLE` (learnability guards), `GO_MOVES` (full move
  inventory for prefix safety).
- `new.java` — the generated single-file Tasker BeanShell module. The block
  between the GENERATED markers is machine-written; the rest is runtime.
- `acceptance.py` — prints the exact expansion for any shortcut (parses the
  data straight out of new.java): `python3 acceptance.py raid-dragon-steel`
- `old.java` — the original pre-raid module, kept as reference.

## Meta update workflow

1. Paste fresh GO Hub per-type tables into `BEST` (and/or adjust
   `EXTRA_SETS` / `EXTRA_LEARNABLE`; add new moves to `GO_MOVES`).
2. `python3 transcode.py --apply new.java`
3. `python3 acceptance.py <commands you care about>` to inspect.
4. Paste `new.java` into the Tasker Java Function action.

Images (`budget.webp`, `operators.jpg`) are third-party artwork and are
gitignored; their factual content is transcribed in `transcode.py`.
