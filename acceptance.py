#!/usr/bin/env python3
"""Acceptance tester for the PoGo search expander (new.java).

Prints the exact string a shortcut expands to on the phone, so you can
inspect it and paste it into the Pokemon GO search bar to test.

Since the precompilation refactor, all packing happens at build time in
transcode.py and new.java only does map lookups -- so this script simply
parses the baked maps out of new.java and mirrors the (tiny) dispatcher:
template DAYS expansion, expansion/cpx lookup with CP append, and the
raidn- Nth pick.

Usage:
    python3 acceptance.py '@#raid-fire.'          # full shortcut syntax
    python3 acceptance.py raid-dragon-steel       # bare keyword also fine
    python3 acceptance.py 'raid-fire[2500]' raid-ghost
    python3 acceptance.py attkr-fairy 'raidn-dragon-steel[2]'
    python3 acceptance.py                         # demo suite

Command family: attkr-T (all vouched T attackers), raid-T / raid-T1-T2
(balanced boss counters), atk-T (one bucket, exact optimal builds),
raidn-T[N] (exact Nth counter; [N] is an index there, CP floor elsewhere).
"""

import base64
import gzip
import re
import sys
from pathlib import Path

JAVA_FILE = Path(__file__).with_name("new.java")

_KIND = {"e": "expansions", "c": "cpx", "s": "seTypes",
         "t": "topByType", "b": "budgetByType"}


def parse_maps(src):
    """Extract the plain `.put()` maps (templates) AND inflate the baked
    DATA_B64 blob (base64 -> gzip -> TSV) into the five data maps."""
    maps = {}
    put_re = re.compile(
        r'(\w+)\.put\(\s*"([^"]*)"\s*,\s*((?:"(?:[^"\\]|\\.)*"\s*(?:\+\s*)?)+)\)\s*;',
        re.S)
    for map_name, key, value_expr in put_re.findall(src):
        parts = re.findall(r'"((?:[^"\\]|\\.)*)"', value_expr)
        maps.setdefault(map_name, {})[key] = "".join(parts)

    m = re.search(r'String DATA_B64 =\s*((?:"[A-Za-z0-9+/=]*"\s*\+?\s*)+);', src)
    if m:
        blob = "".join(re.findall(r'"([A-Za-z0-9+/=]*)"', m.group(1)))
        payload = gzip.decompress(base64.b64decode(blob)).decode("utf-8")
        for name in _KIND.values():
            maps.setdefault(name, {})
        for row in payload.split("\n"):
            parts = row.split("\t")
            if len(parts) == 3:
                maps[_KIND[parts[0]]][parts[1]] = parts[2]
    return maps


def expand_raidn(canon, param, maps):
    se = maps["seTypes"].get(canon)
    if se is None:
        return None
    # Interleave: round-robin across SE buckets; per round, best then budget.
    se_types = se.split(" ")
    best_arrs = [[e for e in maps["topByType"].get(t, "").split(",") if e]
                 for t in se_types]
    budget_arrs = [[e for e in maps["budgetByType"].get(t, "").split(",") if e]
                   for t in se_types]
    max_rounds = max((len(a) for a in best_arrs + budget_arrs), default=0)
    entries, seen = [], set()
    for r in range(max_rounds):
        for a1, a2 in zip(best_arrs, budget_arrs):
            for arr in (a1, a2):
                if r < len(arr) and arr[r] not in seen:
                    seen.add(arr[r])
                    entries.append(arr[r])
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


def expand_text(text, maps):
    templates = maps["templates"]

    def repl(m):
        # Underscores alias hyphens (keyboard-autocorrect workaround).
        keyword = m.group(1).lower().replace("_", "-")
        param = int(m.group(2)) if m.group(2) else 0
        template = templates.get(keyword)

        prefixes = ("raid-", "raidn-", "atk-", "attkr-")
        if template is None and keyword.startswith(prefixes):
            is_raidn = keyword.startswith("raidn-")
            is_raid = keyword.startswith("raid-")
            lookup_key = se_key = None
            if is_raid or is_raidn:
                tp = keyword[6 if is_raidn else 5:].split("-")
                canon = None
                if len(tp) == 1:
                    canon = tp[0]
                elif len(tp) == 2:
                    canon = "-".join(sorted(tp))
                if canon is not None:
                    if is_raidn:
                        se_key = canon
                    else:
                        lookup_key = "raid-" + canon
            else:
                lookup_key = keyword

            if lookup_key is not None:
                exp = None
                if param > 0:
                    exp = maps.get("cpx", {}).get(lookup_key)
                if exp is None:
                    exp = maps["expansions"].get(lookup_key)
                if exp is not None:
                    if param > 0:
                        exp += f"&cp{min(param, 9999)}-"
                    return exp
            if se_key is not None:
                out = expand_raidn(se_key, param, maps)
                if out is not None:
                    return out

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
    for need in ("templates", "expansions", "seTypes", "topByType",
                 "budgetByType"):
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
