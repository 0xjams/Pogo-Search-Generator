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
Underscores alias hyphens (`@#raid_ice.` == `@#raid-ice.`) for keyboards
whose autocorrect eats a hyphen before the terminating dot.

## Architecture: compiled at build time

All packing logic — type-chart cancellation, best/budget tier interleave,
implication clauses, learnability guards, shadow/mega form clauses, legacy
fallbacks, move-prefix shortening, field-length budgeting — runs **on your
computer** in `transcode.py`, which bakes the final search string for every
possible command (all 18 single types, all 153 dual combinations, atk/attkr)
into `new.java`. The phone-side BeanShell is a thin dispatcher: map lookup,
CP-clause append, `raidn-` Nth pick, and the classic `{DAYS:n}` templates.

- `transcode.py` — data + compiler. Curated layers: `BEST` (GO Hub rankings,
  pasted verbatim), `BUDGET` (budget infographic transcription), `EXTRA_SETS`
  (contested/co-equal sets and non-legacy fallbacks), `EXTRA_LEARNABLE`
  (learnability guards), `GO_MOVES` (move inventory for prefix safety), and
  the type chart.
- `new.java` — the generated single-file Tasker module (paste into a Tasker
  Java Function action). The precompiled data is baked as a gzip+base64
  blob (~34 KB file total) that the runtime inflates; `baked.tsv` is its
  human-readable twin for reviewing diffs.
- `acceptance.py` — prints the exact expansion for any shortcut, parsed
  straight out of new.java: `python3 acceptance.py raid-dragon-steel`
- `old.java` — the original pre-raid module, kept as reference.

## Meta update workflow

1. Paste fresh GO Hub per-type tables into `BEST` (and/or adjust
   `EXTRA_SETS` / `EXTRA_LEARNABLE`; add new moves to `GO_MOVES`).
2. `python3 transcode.py --apply new.java`
3. `python3 acceptance.py <commands you care about>` to inspect.
4. Paste `new.java` into the Tasker Java Function action.

## Credits & disclosure

- Built with AI assistance (Anthropic's Claude): the search-grammar
  reverse-engineering, the CNF implication-clause encoding, the build-time
  compiler, and the verification harness were developed iteratively with
  on-device test results feeding back into the design.
- The accessibility-based approach this module relies on (reacting to text
  in other apps' input fields and writing back via accessibility actions
  from Tasker's Java support) builds on capabilities introduced in the
  [Tasker 6.6.12 release candidate](https://old.reddit.com/r/tasker/comments/1p1etf6/dev_tasker_6612_release_candidate_full/)
  by João Dias (joaomgcd).
- Rankings sourced from [Pokémon GO Hub's DB](https://db.pokemongohub.net/best/attackers-per-type)
  and DialgaDex's "Top Basic Raid Attackers" infographic (data transcribed
  as facts; the source images are not distributed with this repo).

Images (`budget.webp`, `operators.jpg`) and saved web pages are third-party
content and gitignored; their factual content is transcribed in `transcode.py`.
