#!/usr/bin/env python3
"""Acceptance tester for the PoGo search expander (new.java).

Generates the exact string a shortcut would expand to on the phone, so you
can inspect it and paste it into the Pokemon GO search bar to test.

The data maps (templates, type chart, topByType) are parsed out of new.java
itself -- this always exercises the deliverable's real data, not a copy.
Only the expansion algorithm is mirrored here in Python.

Usage:
    python3 acceptance.py '@#raid-fire.'          # full shortcut syntax
    python3 acceptance.py raid-dragon-steel       # bare keyword also fine
    python3 acceptance.py 'raid-fire[2500]' raid-ghost
    python3 acceptance.py attkr-fairy 'raidn-dragon-steel[2]'
    python3 acceptance.py                         # demo suite

Command family: attkr-T (all vouched T attackers), raid-T / raid-T1-T2
(balanced boss counters), atk-T (one bucket, optimal moves), raidn-T[N]
(exact Nth counter; [N] is an index there, CP floor elsewhere).
"""

import re
import sys
from pathlib import Path

JAVA_FILE = Path(__file__).with_name("new.java")

ALL_TYPES = ["normal", "fire", "water", "electric", "grass", "ice", "fighting",
             "poison", "ground", "flying", "psychic", "bug", "rock", "ghost",
             "dragon", "dark", "steel", "fairy"]

MAX_SEARCH_LEN = 200        # keep in sync with new.java
AND_BINDS_TIGHTER = False   # keep in sync with new.java. On-device proof:
                            # "@origin,@psy&kyogre,mewtwo,charizard" showed
                            # no Charizard -> OR evaluates before AND (CNF).
EXACT_PINNING = True        # keep in sync with new.java: implication-clause
                            # encoding (s1,s2&!s1,m1&!s2,m2) = exact per-
                            # species moveset pinning via mutual exclusivity.


def parse_maps(src):
    """Extract every `<map>.put("key", "..." + "..." ...)` from the BeanShell
    source into {map_name: {key: value}}."""
    maps = {}
    put_re = re.compile(
        r'(\w+)\.put\(\s*"([^"]*)"\s*,\s*((?:"(?:[^"\\]|\\.)*"\s*(?:\+\s*)?)+)\)\s*;',
        re.S)
    for map_name, key, value_expr in put_re.findall(src):
        parts = re.findall(r'"((?:[^"\\]|\\.)*)"', value_expr)
        maps.setdefault(map_name, {})[key] = "".join(parts)
    return maps


def interleave(se_types, top_by_type, budget_by_type):
    """Round-robin across SE buckets; within a round, best tier then budget
    tier -- budget counters get guaranteed early admission."""
    best_arrs = [[e for e in top_by_type.get(t, "").split(",") if e]
                 for t in se_types]
    budget_arrs = [[e for e in budget_by_type.get(t, "").split(",") if e]
                   for t in se_types]
    max_rounds = max((len(a) for a in best_arrs + budget_arrs), default=0)
    entries, seen = [], set()
    for r in range(max_rounds):
        for a1, a2 in zip(best_arrs, budget_arrs):
            for arr in (a1, a2):
                if r < len(arr) and arr[r] not in seen:
                    seen.add(arr[r])
                    entries.append(arr[r])
    return entries


def expand_raid(keyword, param, chart, top_by_type, budget_by_type, learn_extra):
    is_raidn = keyword.startswith("raidn-")
    is_atk = keyword.startswith("atk-")
    is_attkr = keyword.startswith("attkr-")

    if is_atk or is_attkr:
        atk_type = keyword[4:] if is_atk else keyword[6:]
        if atk_type not in chart["weakTo"]:
            return None
        se_types = [atk_type]
    else:
        boss_part = keyword[6:] if is_raidn else keyword[5:]
        boss_types = boss_part.split("-")
        if not 1 <= len(boss_types) <= 2:
            return None
        if any(t not in chart["weakTo"] for t in boss_types):
            return None
        if len(boss_types) == 1:
            se_types = chart["weakTo"][boss_types[0]].split(" ")
        else:
            double_se, single_se = [], []
            for atk in ALL_TYPES:
                product = 1.0
                for d in boss_types:
                    if atk in chart["immuneTo"].get(d, "").split():
                        product *= 0.390625
                    elif atk in chart["weakTo"].get(d, "").split():
                        product *= 1.6
                    elif atk in chart["resistsFrom"].get(d, "").split():
                        product *= 0.625
                if product >= 2.5:
                    double_se.append(atk)
                elif product >= 1.5:
                    single_se.append(atk)
            se_types = double_se + single_se
        if not se_types:
            return None

    entries = interleave(se_types, top_by_type, budget_by_type)
    cp_clause = f"&cp{param}-" if param > 0 else ""
    # Spaced @move names appear to swallow a following comma (on-device),
    # so no @ term may precede ','. Groups end in a plain token: the CP
    # clause if given, else the always-true cp1- floor.
    group_tail = cp_clause if param > 0 else "&cp1-"

    if is_raidn:
        # Exact query for the Nth-ranked counter ([N] is index, not CP);
        # out-of-range leaves the shortcut untouched.
        idx = param if param > 0 else 1
        if idx > len(entries):
            return None
        s, f, c, tag = (entries[idx - 1].split("~") + ["b"])[:4]
        out = f"{s}&@{f}&@{c}"
        if "b" not in tag:
            forms = (",shadow" if "s" in tag else "") + \
                    (",megaevolve,mega" if "m" in tag else "")
            if forms:
                out += "&" + forms[1:]
        return out

    if is_attkr:
        # Every vouched species of this type that carries a T move.
        specs = []
        for e in entries:
            s = e.split("~")[0]
            if s not in specs:
                specs.append(s)
        if AND_BINDS_TIGHTER:
            out, length = [], 0
            for s in specs:
                group = f"{s}&@{se_types[0]}{group_tail}"
                extra = len(group) + (1 if out else 0)
                if length + extra <= MAX_SEARCH_LEN:
                    out.append(group)
                    length += extra
            return ",".join(out) if out else f"@{se_types[0]}{group_tail}"
        suffix = f"&@{se_types[0]}{cp_clause}"
        while specs:
            cand = ",".join(specs) + suffix
            if len(cand) <= MAX_SEARCH_LEN:
                return cand
            specs.pop()
        return f"@{se_types[0]}{cp_clause}"

    if AND_BINDS_TIGHTER:
        # Exact per-species AND-groups: species&@fast&@charged&tail,...
        out, length = [], 0
        for entry in entries:
            s, f, c = entry.split("~")[:3]
            group = f"{s}&@{f}&@{c}{group_tail}"
            extra = len(group) + (1 if out else 0)
            if length + extra <= MAX_SEARCH_LEN:
                out.append(group)
                length += extra
        if out:
            return ",".join(out)
        return ",".join(f"@{t}{group_tail}" for t in se_types)

    if EXACT_PINNING:
        # Implication encoding: species OR-clause, then per species
        # "!s,@fasts" / "!s,@chargeds" (and "!s,shadow" if only the shadow
        # form is vouched). Exact via species mutual exclusivity.
        sp_order, sp_fasts, sp_chargeds = [], {}, {}
        sp_forms = {}  # species -> set of vouching tags ("b"/"s"/"m")
        assembled = None

        def build():
            # Global clauses pin everyone for free; a per-species clause is
            # only needed where that species can LEARN a conflicting pool
            # move (learn_extra) or is vouched only in boosted form(s).
            g_fasts, g_chargeds = [], []
            for xs in sp_order:
                for m in sp_fasts[xs]:
                    if m not in g_fasts:
                        g_fasts.append(m)
                for m in sp_chargeds[xs]:
                    if m not in g_chargeds:
                        g_chargeds.append(m)
            parts = [",".join(sp_order),
                     ",".join("@" + m for m in g_fasts),
                     ",".join("@" + m for m in g_chargeds)]
            for xs in sp_order:
                extras = learn_extra.get(xs, "")
                if any(f"|{m}|" in extras for m in g_fasts):
                    parts.append("!" + xs + "".join(",@" + m for m in sp_fasts[xs]))
                if any(f"|{m}|" in extras for m in g_chargeds):
                    parts.append("!" + xs + "".join(",@" + m for m in sp_chargeds[xs]))
                if "b" not in sp_forms[xs]:
                    # megaevolve = CAN mega (energy-aware); mega = currently
                    # mega'd -- need both.
                    clause = "!" + xs
                    if "s" in sp_forms[xs]:
                        clause += ",shadow"
                    if "m" in sp_forms[xs]:
                        clause += ",megaevolve,mega"
                    if clause != "!" + xs:
                        parts.append(clause)
            return "&".join(parts) + cp_clause

        # Multi-pass: later rows can shrink the string (a base row deletes
        # a form clause), so retry skipped entries until nothing admits.
        pending = list(entries)
        progress = True
        while progress:
            progress = False
            still = []
            for entry in pending:
                s, f, c, tag = (entry.split("~") + ["b"])[:4]
                is_new = s not in sp_fasts
                prev_forms = set(sp_forms.get(s, set()))
                if is_new:
                    sp_order.append(s)
                    sp_fasts[s], sp_chargeds[s] = [], []
                    sp_forms[s] = set()
                sp_forms[s].update(tag)
                added_f = f not in sp_fasts[s] and (sp_fasts[s].append(f) or True)
                added_c = c not in sp_chargeds[s] and (sp_chargeds[s].append(c) or True)
                candidate = build()
                if len(candidate) <= MAX_SEARCH_LEN:
                    assembled = candidate
                    progress = True
                else:
                    # Roll back; may fit on a later pass once shrunk.
                    still.append(entry)
                    if added_f:
                        sp_fasts[s].pop()
                    if added_c:
                        sp_chargeds[s].pop()
                    if is_new:
                        sp_order.pop()
                        del sp_fasts[s], sp_chargeds[s], sp_forms[s]
                    else:
                        sp_forms[s] = prev_forms
            pending = still
        if assembled is None:
            return ",".join("@" + t for t in se_types) + cp_clause
        return assembled

    # raid-/atk- loose CNF shape: greedily admit entries into the
    # species/fast/charged OR-clauses (all ANDed).
    specs, fasts, chargeds = [], [], []
    assembled = None
    for entry in entries:
        s, f, c = entry.split("~")[:3]
        added = []
        for lst, val in ((specs, s), (fasts, f), (chargeds, c)):
            if val not in lst:
                lst.append(val)
                added.append(lst)
        candidate = (",".join(specs) + "&"
                     + ",".join("@" + m for m in fasts) + "&"
                     + ",".join("@" + m for m in chargeds) + cp_clause)
        if len(candidate) <= MAX_SEARCH_LEN:
            assembled = candidate
        else:
            # Roll back but keep scanning: a later entry whose moves are
            # already in the clauses may still fit (gap-fill).
            for lst in added:
                lst.pop()

    if assembled is None:
        # No curated data: fall back to the move-TYPE filter.
        return ",".join("@" + t for t in se_types) + cp_clause
    return assembled


def expand_text(text, maps):
    templates = maps["templates"]
    chart = {k: maps[k] for k in ("weakTo", "resistsFrom", "immuneTo")}
    top_by_type = maps["topByType"]
    budget_by_type = maps["budgetByType"]
    learn_extra = maps.get("learnExtra", {})

    def repl(m):
        keyword = m.group(1).lower()
        param = int(m.group(2)) if m.group(2) else 0
        template = templates.get(keyword)
        if template is None and keyword.startswith(("raid-", "raidn-", "atk-", "attkr-")):
            raid = expand_raid(keyword, param, chart, top_by_type, budget_by_type, learn_extra)
            if raid is not None:
                return raid
        if template is None:
            return m.group(0)
        days_m = re.search(r"\{DAYS:(\d+)\}", template)
        if days_m:
            days = param if param > 0 else int(days_m.group(1))
            age = "age0" if days == 0 else f"age0-{days - 1}"
            template = re.sub(r"\{DAYS:\d+\}", age, template)
        return template

    return re.sub(r"@#([\w-]+)(?:\[(\d+)\]|\.)", repl, text)


def main():
    maps = parse_maps(JAVA_FILE.read_text())
    for need in ("templates", "weakTo", "resistsFrom", "immuneTo", "topByType", "budgetByType"):
        if need not in maps:
            sys.exit(f"Could not parse map '{need}' out of {JAVA_FILE}")

    inputs = sys.argv[1:] or [
        "@#raid-fire.", "@#raid-dragon-steel.", "@#raid-ice-flying.",
        "@#raid-fire[2500]", "@#raid-fairy.", "@#transfer[7]", "@#hundo.",
    ]
    for raw in inputs:
        text = raw
        if "@#" not in text:  # bare keyword convenience: raid-fire -> @#raid-fire.
            text = "@#" + text
            if not re.search(r"[.\]]$", text):
                text += "."
        out = expand_text(text, maps)
        # Metadata on stderr, the complete expansion alone on stdout: nothing
        # can truncate it, and `acceptance.py raid-fire | pbcopy` just works.
        print(f"IN : {text}  ({len(out)} chars)", file=sys.stderr)
        print(out, flush=True)
        print("---", file=sys.stderr)


if __name__ == "__main__":
    main()
