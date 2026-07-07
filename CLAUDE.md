# CLAUDE.md

Tasker text expander for Pokémon GO raid-counter searches. The deliverable
constraint that shapes everything: **`new.java` is the single file the user
pastes into a Tasker "Java Function" action** — BeanShell, phone-side, no
other files exist in that environment.

## Architecture (compiled)

All logic runs at build time in `transcode.py`; the phone runs a thin
dispatcher. Never hand-edit the block between the GENERATED markers in
`new.java` — regenerate it:

```
python3 transcode.py --apply new.java     # splice fresh data/expansions
python3 acceptance.py raid-dragon-steel   # inspect any command's string
```

- `transcode.py` — data + compiler. Emits, between the markers:
  `expansions` (command key → final search string), `cpx` (variant repacked
  with 8 chars reserved for a runtime `&cpN-`; only where it differs),
  `seTypes` + `topByType`/`budgetByType` (feed the raidn- runtime).
- `new.java` — hand-written runtime skeleton (~280 lines) + generated block.
  It is BOTH source and artifact: tracked in git on purpose; `--apply`
  patches in place and needs the markers to exist.
- `acceptance.py` — parses the baked maps out of `new.java` and mirrors the
  trivial dispatcher. Prints expansion to stdout (pipeable), metadata to
  stderr. This is the user's acceptance-test tool.
- `old.java` — original pre-raid module, reference only.

## Pokémon GO search grammar (verified ON DEVICE — do not re-litigate)

- `,` `:` `;` are OR; `&` and `|` are AND; `!` negates one term.
- **OR evaluates before AND.** No parentheses (ignored). A search is an AND
  of OR-clauses. Proof: `@origin,@psy&kyogre,mewtwo,charizard` returned no
  Charizard; a comma-joined chain of AND-groups returns NOTHING.
- Therefore OR-of-AND-groups is inexpressible — but species terms are
  mutually exclusive, so it rewrites to implication clauses:
  `s1,s2&!s1,m1&!s2,m2` ("if s1 then m1"). This is the core encoding.
- `@move` matches by name **prefix** (`@psy` = Psystrike, Psychic…) — baked
  prefixes are shortest-unique vs `GO_MOVES` (extend that list when Niantic
  adds moves, else a baked prefix may silently match a new move).
- There is **no** `>type` operator (the original spec invented it).
- Species name matching is substring-based and matches Shadow/Mega/forme
  variants and nicknames.
- Moves with apostrophes use the typographic U+2019 in-game; a keyboard
  `'` does not match (moot for baked prefixes, which stop earlier).
- Field truncates around 200 chars (`MAX_SEARCH_LEN`, community figure).
- `megaevolve` = can Mega/Primal right now (energy-aware); `mega` =
  currently Mega-Evolved. Form clauses need both: `!s,megaevolve,mega`.

## Data layers in transcode.py (edit these, then regen)

- `BEST` — GO Hub per-type rankings, pasted verbatim (rank order matters).
- `BUDGET` — budget infographic transcription (always gets seats via the
  round-robin best/budget interleave).
- `EXTRA_SETS` — (a) contested/co-equal sets the sources omit (Mud Shot
  Swampert), (b) non-legacy fallbacks where the listed set needs a CD/Elite
  move. Rows insert adjacent to the species' existing bucket rows.
- `EXTRA_LEARNABLE` — pool moves a species CAN learn beyond its vouched
  rows; drives conflict-only implication clauses. A missing pair = small
  leak; an extra pair = wasted length. When a move becomes vouched via
  EXTRA_SETS, remove it here.
- Form tags: rows are `species~fast~charged~tags` (b/s/m union). A base or
  budget row anywhere removes the form requirement; shadow-only →
  `!s,shadow`; mega-only → `!s,megaevolve,mega`.

## Runtime facts (new.java dispatcher)

- Trigger: `@#keyword.` or `@#keyword[N]` (dispatcher task fires on `.`/`]`;
  the terminator also keeps `[CP]` typeable). Keyword lowercased and `_`
  normalized to `-` (keyboard autocorrect eats hyphen-before-dot).
- Dual raid keys canonicalize by lexicographic sort of the two types.
- `[N]` is a CP floor on raid/atk/attkr (clamped to 9999 to fit the cpx
  reserve) and an index on raidn (out-of-range → shortcut left as-is).
- Unknown keys/types fall through untouched.
- BeanShell constraints honored throughout: no generics, no lambdas, no
  enhanced-for; typed locals, while-loops, explicit casts.

## Verification workflow

`acceptance.py` output must match the phone behavior by construction now
(lookup-only), but when touching the java runtime, rebuild the harness:
BeanShell 2.0b6 jar (maven: org.apache-extras.beanshell:bsh:2.0b6) +
compiled stubs for `android.view.accessibility.AccessibilityNodeInfo`
(records the set text), `android.os.Bundle`, `io.reactivex.functions
.Consumer`, plus a scripted `tasker` object; a driver script sources
new.java and calls the registered Consumer, then diff its outputs against
`acceptance.py` for the same inputs. When touching the compiler, capture a
full baseline first (all 18+153+18+18 keys, cp and raidn samples) and diff
after — the precompilation refactor was verified with 471/471 identical.

## Copyright hygiene

`budget.webp`, `operators.jpg`, and any saved web pages stay gitignored;
only transcribed facts live in the repo. Remote:
`git@github.com:0xjams/Pogo-Search-Generator.git`.
