#!/usr/bin/env python3
"""Transcode raid-attacker rankings into the BeanShell `topByType` map.

Verified Pokemon GO search grammar this design is built on (Niantic help
center, pokemongo.fandom.com/wiki/Pokemon_search, operators.jpg):
  - `,` `:` `;` are OR; `&` and `|` are AND; `!` is NOT.
  - OR is ALWAYS evaluated before AND ("colons, semicolons and commas are
    always evaluated before ampersand"). No parentheses. So a search is an
    AND of OR-clauses -- "a,b&c,d" means (a OR b) AND (c OR d).
  - Therefore "speciesA&@moveA1&@moveA2,speciesB&..." can NOT express
    per-species movesets; there is also no ">type" (super-effective)
    operator. The closest legal shape, which the expander emits, is:
        [species1,species2,...&]@fast1,@fast2,...&@charged1,...[&cpN-]
    = (has one of the curated OPTIMAL fast moves) AND (has one of the
    curated OPTIMAL charged moves) [AND (is a curated species), when it
    fits] [AND (CP floor)]. Cross-species move pairings can still sneak
    through, but every match carries top-ranked raid moves in both slots.
  - The in-game search field truncates around 200 characters; the runtime
    trims the species list to fit (MAX_SEARCH_LEN in new.java).

Data sources, merged per attack type (species collapse to base names:
"Shadow Garchomp" / "Mega Garchomp" -> garchomp; the plain name matches all
of them in search, and your mega candidate IS the base mon):
  1. BEST -- GO Hub "best attackers per type" (db.pokemongohub.net), pasted
     by the user on 2026-07-06. Top ATTACKERS_PER_TYPE distinct species kept,
     in rank order. To refresh after a meta shift, paste the new tables in.
  2. BUDGET -- "Top Basic Raid Attackers July 2026" infographic
     (budget.webp, DialgaDex). Always appended (these are the cheap builds).

Usage:
    python3 transcode.py                   # print the generated block
    python3 transcode.py --apply new.java  # splice it between the markers
"""

import argparse
import base64
import gzip
import re

# ---------------------------------------------------------------- tunables --
ATTACKERS_PER_TYPE = 10  # distinct BEST species kept per type (BUDGET extras
                         # ride alongside; the runtime's greedy packer already
                         # enforces the 200-char in-game limit per search, so
                         # this only bounds how deep the baked data goes)

# Moves with apostrophes (Nature's Madness): the in-game name appears to use
# the typographic apostrophe U+2019, and Niantic documents that punctuation
# in names must be matched exactly -- a keyboard "'" (U+0027) fails. Since we
# inject text programmatically we can emit the exact glyph.
#   "curly"    -> bake the move with U+2019 (best bet, verify on device)
#   "straight" -> bake with ASCII ' (if the curly variant fails on device)
#   "drop"     -> exclude entries whose moves contain an apostrophe
# (Mostly moot with USE_MOVE_PREFIXES: prefixes stop before the apostrophe.)
APOSTROPHE_MODE = "curly"

# @move matching is PREFIX-based (confirmed on device: @psy matched
# Psystrike, @origin matched Origin Pulse). Emitting the shortest
# unambiguous prefix per move fits more attackers into the 200-char limit
# ("precipice blades" -> "prec"). Uniqueness is checked against GO_MOVES
# below -- when Niantic adds new moves, extend that list and regen, or a
# baked prefix could silently start matching the new move too.
USE_MOVE_PREFIXES = True
MIN_PREFIX_LEN = 4   # floor against collisions with moves we don't know of

# Form policy. Entries carry a form tag: ~s = shadow-sourced, ~m = mega/
# primal-sourced, ~b = base/budget. The runtime's exact-pinning mode adds
# per-species form clauses for species vouched ONLY by one boosted form:
#   shadow-only -> "!species,shadow"      (regular twins stop matching)
#   mega-only   -> "!species,megaevolve"  (Niantic: currently eligible to
#                  Mega Evolve/Primal Reversion, energy considered)
# Species also vouched as base/budget get no form clause.
INCLUDE_MEGA_RANKED = True     # also covers Primal
INCLUDE_SHADOW_RANKED = True

TYPES = ["normal", "fire", "water", "electric", "grass", "ice", "fighting",
         "poison", "ground", "flying", "psychic", "bug", "rock", "ghost",
         "dragon", "dark", "steel", "fairy"]

MAX_SEARCH_LEN = 200  # in-game search field limit (community figure)
CP_RESERVE = 8        # len("&cp9999-"); the runtime clamps [CP] to 9999

# Pokemon GO type chart (validated). Space-separated attack types.
WEAK_TO = {
    "normal": "fighting", "fire": "water ground rock",
    "water": "electric grass", "electric": "ground",
    "grass": "fire ice poison flying bug", "ice": "fire fighting rock steel",
    "fighting": "flying psychic fairy", "poison": "ground psychic",
    "ground": "water grass ice", "flying": "electric ice rock",
    "psychic": "bug ghost dark", "bug": "fire flying rock",
    "rock": "water grass fighting ground steel", "ghost": "ghost dark",
    "dragon": "ice dragon fairy", "dark": "fighting bug fairy",
    "steel": "fire fighting ground", "fairy": "poison steel",
}
RESISTS_FROM = {
    "fire": "fire grass ice bug steel fairy", "water": "fire water ice steel",
    "electric": "electric flying steel", "grass": "water electric grass ground",
    "ice": "ice", "fighting": "bug rock dark",
    "poison": "grass fighting poison bug fairy", "ground": "poison rock",
    "flying": "grass fighting bug", "psychic": "fighting psychic",
    "bug": "grass fighting ground", "rock": "normal fire poison flying",
    "ghost": "poison bug", "dragon": "fire water grass electric",
    "dark": "ghost dark",
    "steel": "normal grass ice flying psychic bug rock dragon steel fairy",
    "fairy": "fighting bug dark",
}
IMMUNE_TO = {
    "normal": "ghost", "ghost": "normal fighting", "dark": "psychic",
    "fairy": "dragon", "flying": "ground", "ground": "electric",
    "steel": "poison",
}

# ------------------------------------------------------------ GO Hub BEST --
# (display name, fast, charged) verbatim from the site, rank order.
# Moves are kept for documentation only -- the search grammar cannot pin them.
BEST = {
    "normal": [
        ("Shadow Regigigas", "Hidden Power", "Crush Grip"),
        ("Regigigas", "Hidden Power", "Crush Grip"),
        ("Mega Mewtwo Y", "Psycho Cut", "Hyper Beam"),
        ("Mega Mewtwo X", "Psycho Cut", "Hyper Beam"),
        ("Crowned Sword Zacian", "Metal Claw", "Giga Impact"),
        ("Mega Lopunny", "Pound", "Hyper Beam"),
        ("Shadow Porygon-Z", "Lock-On", "Hyper Beam"),
        ("Shadow Mewtwo", "Psycho Cut", "Hyper Beam"),
        ("Aria Forme Meloetta", "Quick Attack", "Hyper Beam"),
        ("Porygon-Z", "Lock-On", "Hyper Beam"),
    ],
    "fighting": [
        ("Mega Lucario", "Force Palm", "Aura Sphere"),
        ("Mega Blaziken", "Counter", "Aura Sphere"),
        ("Keldeo (Resolute Forme)", "Low Kick", "Secret Sword"),
        ("Mega Mewtwo X", "Psycho Cut", "Focus Blast"),
        ("Mega Heracross", "Counter", "Close Combat"),
        ("Shadow Conkeldurr", "Force Palm", "Dynamic Punch"),
        ("Shadow Blaziken", "Counter", "Aura Sphere"),
        ("Terrakion", "Double Kick", "Sacred Sword"),
        ("Lucario", "Force Palm", "Aura Sphere"),
        ("Mega Mewtwo Y", "Psycho Cut", "Focus Blast"),
    ],
    "flying": [
        ("Mega Rayquaza", "Air Slash", "Dragon Ascent"),
        ("Rayquaza", "Air Slash", "Dragon Ascent"),
        ("Shadow Moltres", "Wing Attack", "Fly"),
        ("Shadow Salamence", "Fire Fang", "Fly"),
        ("Mega Salamence", "Fire Fang", "Fly"),
        ("Enamorus (Incarnate Forme)", "Fairy Wind", "Fly"),
        ("Mega Charizard Y", "Air Slash", "Blast Burn"),
        ("Yveltal", "Gust", "Oblivion Wing"),
        ("Moltres", "Wing Attack", "Fly"),
        ("Galarian Articuno", "Psycho Cut", "Fly"),
    ],
    "poison": [
        ("Eternatus", "Poison Jab", "Sludge Bomb"),
        ("Mega Gengar", "Lick", "Sludge Bomb"),
        ("Mega Beedrill", "Poison Jab", "Sludge Bomb"),
        ("Nihilego", "Poison Jab", "Sludge Bomb"),
        ("Mega Victreebel", "Acid", "Sludge Bomb"),
        ("Shadow Overqwil", "Poison Jab", "Sludge Bomb"),
        ("Naganadel", "Poison Jab", "Sludge Bomb"),
        ("Roserade", "Poison Jab", "Sludge Bomb"),
        ("Revavroom", "Poison Jab", "Gunk Shot"),
        ("Shadow Scolipede", "Poison Jab", "Sludge Bomb"),
    ],
    "ground": [
        ("Primal Groudon", "Mud Shot", "Precipice Blades"),
        ("Mega Garchomp", "Mud Shot", "Earth Power"),
        ("Shadow Groudon", "Mud Shot", "Precipice Blades"),
        ("Shadow Garchomp", "Mud Shot", "Earth Power"),
        ("Landorus (Therian Forme)", "Mud Shot", "Sandsear Storm"),
        ("Shadow Landorus (Incarnate Forme)", "Mud Shot", "Earth Power"),
        ("Shadow Excadrill", "Mud-Slap", "Scorching Sands"),
        ("Shadow Rhyperior", "Mud-Slap", "Drill Run"),
        ("Groudon", "Mud Shot", "Precipice Blades"),
        ("Mega Swampert", "Mud Shot", "Earthquake"),
    ],
    "rock": [
        ("Mega Diancie", "Rock Throw", "Rock Slide"),
        ("Shadow Rhyperior", "Smack Down", "Rock Wrecker"),
        ("Mega Tyranitar", "Smack Down", "Stone Edge"),
        ("Mega Aerodactyl", "Rock Throw", "Rock Slide"),
        ("Shadow Gigalith", "Lock-On", "Meteor Beam"),
        ("Shadow Tyranitar", "Smack Down", "Stone Edge"),
        ("Shadow Tyrantrum", "Rock Throw", "Meteor Beam"),
        ("Shadow Rampardos", "Smack Down", "Rock Slide"),
        ("Rhyperior", "Smack Down", "Rock Wrecker"),
        ("Mega Rayquaza", "Dragon Tail", "Ancient Power"),
    ],
    "bug": [
        ("Mega Heracross", "Fury Cutter", "Megahorn"),
        ("Mega Pinsir", "Fury Cutter", "X-Scissor"),
        ("Mega Scizor", "Fury Cutter", "X-Scissor"),
        ("Shadow Vikavolt", "Bug Bite", "X-Scissor"),
        ("Shadow Scizor", "Fury Cutter", "X-Scissor"),
        ("Volcarona", "Bug Bite", "Bug Buzz"),
        ("Mega Beedrill", "Bug Bite", "X-Scissor"),
        ("Shadow Pinsir", "Fury Cutter", "X-Scissor"),
        ("Shadow Escavalier", "Bug Bite", "Megahorn"),
        ("Shadow Metagross", "Fury Cutter", "Meteor Mash"),
    ],
    "ghost": [
        ("Dawn Wings Necrozma", "Psycho Cut", "Moongeist Beam"),
        ("Mega Mewtwo Y", "Psycho Cut", "Shadow Ball"),
        ("Mega Gengar", "Lick", "Shadow Ball"),
        ("Mega Mewtwo X", "Psycho Cut", "Shadow Ball"),
        ("Shadow Darkrai", "Snarl", "Shadow Ball"),
        ("Shadow Chandelure", "Hex", "Shadow Ball"),
        ("Mega Banette", "Shadow Claw", "Shadow Ball"),
        ("Lunala", "Shadow Claw", "Shadow Ball"),
        ("Shadow Mewtwo", "Psycho Cut", "Shadow Ball"),
        ("Black Kyurem", "Shadow Claw", "Freeze Shock"),
    ],
    "steel": [
        ("Crowned Sword Zacian", "Metal Claw", "Behemoth Blade"),
        ("Crowned Shield Zamazenta", "Metal Claw", "Behemoth Bash"),
        ("Dusk Mane Necrozma", "Metal Claw", "Sunsteel Strike"),
        ("Mega Metagross", "Bullet Punch", "Meteor Mash"),
        ("Shadow Metagross", "Bullet Punch", "Meteor Mash"),
        ("Mega Lucario", "Force Palm", "Meteor Mash"),
        ("Shadow Dialga", "Metal Claw", "Iron Head"),
        ("Metagross", "Bullet Punch", "Meteor Mash"),
        ("Shadow Excadrill", "Metal Claw", "Iron Head"),
        ("Dawn Wings Necrozma", "Metal Claw", "Moongeist Beam"),
    ],
    "fire": [
        ("Mega Charizard Y", "Fire Spin", "Blast Burn"),
        ("Shadow Reshiram", "Fire Fang", "Fusion Flare"),
        ("Mega Blaziken", "Fire Spin", "Blast Burn"),
        ("Blacephalon", "Incinerate", "Mind Blown"),
        ("Shadow Heatran", "Fire Spin", "Magma Storm"),
        ("Mega Charizard X", "Fire Spin", "Blast Burn"),
        ("Reshiram", "Fire Fang", "Fusion Flare"),
        ("Shadow Moltres", "Fire Spin", "Overheat"),
        ("Shadow Delphox", "Fire Spin", "Blast Burn"),
        ("Shadow Chandelure", "Fire Spin", "Overheat"),
    ],
    "water": [
        ("Primal Kyogre", "Waterfall", "Origin Pulse"),
        ("Mega Swampert", "Water Gun", "Hydro Cannon"),
        ("Shadow Kyogre", "Waterfall", "Origin Pulse"),
        ("Mega Blastoise", "Water Gun", "Hydro Cannon"),
        ("Mega Gyarados", "Waterfall", "Hydro Pump"),
        ("Shadow Swampert", "Water Gun", "Hydro Cannon"),
        ("Kyogre", "Waterfall", "Origin Pulse"),
        ("Shadow Feraligatr", "Water Gun", "Hydro Cannon"),
        ("Shadow Kingler", "Bubble", "Crabhammer"),
        ("Shadow Samurott", "Waterfall", "Hydro Cannon"),
    ],
    "grass": [
        ("Mega Sceptile", "Fury Cutter", "Frenzy Plant"),
        ("Mega Venusaur", "Vine Whip", "Frenzy Plant"),
        ("Kartana", "Razor Leaf", "Leaf Blade"),
        ("Shadow Chesnaught", "Vine Whip", "Frenzy Plant"),
        ("Zarude", "Vine Whip", "Power Whip"),
        ("Shaymin (Sky Forme)", "Magical Leaf", "Grass Knot"),
        ("Shadow Tangrowth", "Vine Whip", "Power Whip"),
        ("Shadow Venusaur", "Vine Whip", "Frenzy Plant"),
        ("Mega Victreebel", "Magical Leaf", "Leaf Blade"),
        ("Rillaboom", "Razor Leaf", "Frenzy Plant"),
    ],
    "electric": [
        ("Mega Mewtwo Y", "Psycho Cut", "Thunderbolt"),
        ("Zeraora", "Volt Switch", "Plasma Fists"),
        ("Mega Mewtwo X", "Psycho Cut", "Thunderbolt"),
        ("Shadow Raikou", "Thunder Shock", "Wild Charge"),
        ("Zekrom", "Charge Beam", "Wild Charge"),
        ("Xurkitree", "Thunder Shock", "Discharge"),
        ("Mega Manectric", "Thunder Fang", "Wild Charge"),
        ("Shadow Thundurus (Therian Forme)", "Volt Switch", "Thunderbolt"),
        ("Shadow Zapdos", "Thunder Shock", "Thunderbolt"),
        ("Shadow Magnezone", "Volt Switch", "Wild Charge"),
    ],
    "psychic": [
        ("Mega Mewtwo Y", "Confusion", "Psystrike"),
        ("Mega Mewtwo X", "Confusion", "Psystrike"),
        ("Shadow Mewtwo", "Psycho Cut", "Psystrike"),
        ("Mega Alakazam", "Confusion", "Psychic"),
        ("Mega Latios", "Zen Headbutt", "Psychic"),
        ("Mewtwo", "Psycho Cut", "Psystrike"),
        ("Mega Gardevoir", "Confusion", "Psychic"),
        ("Mega Gallade", "Confusion", "Psychic"),
        ("Mega Metagross", "Zen Headbutt", "Psychic"),
        ("Shadow Latios", "Zen Headbutt", "Psychic"),
    ],
    "ice": [
        ("White Kyurem", "Ice Fang", "Ice Burn"),
        ("Black Kyurem", "Dragon Tail", "Freeze Shock"),
        ("Mega Mewtwo Y", "Psycho Cut", "Ice Beam"),
        ("Mega Mewtwo X", "Psycho Cut", "Ice Beam"),
        ("Shadow Mamoswine", "Powder Snow", "Avalanche"),
        ("Mega Gardevoir", "Charm", "Triple Axel"),
        ("Crowned Shield Zamazenta", "Ice Fang", "Behemoth Bash"),
        ("Shadow Mewtwo", "Psycho Cut", "Ice Beam"),
        ("Baxcalibur", "Ice Fang", "Avalanche"),
        ("Primal Kyogre", "Waterfall", "Avalanche"),
    ],
    "dragon": [
        ("Mega Rayquaza", "Dragon Tail", "Breaking Swipe"),
        ("Eternatus", "Dragon Tail", "Dynamax Cannon"),
        ("Black Kyurem", "Dragon Tail", "Freeze Shock"),
        ("Mega Garchomp", "Dragon Tail", "Breaking Swipe"),
        ("White Kyurem", "Dragon Breath", "Ice Burn"),
        ("Shadow Garchomp", "Dragon Tail", "Breaking Swipe"),
        ("Mega Salamence", "Dragon Tail", "Draco Meteor"),
        ("Shadow Haxorus", "Dragon Tail", "Breaking Swipe"),
        ("Shadow Dialga", "Dragon Breath", "Draco Meteor"),
        ("Mega Dragonite", "Dragon Tail", "Draco Meteor"),
    ],
    "dark": [
        ("Mega Tyranitar", "Bite", "Brutal Swing"),
        ("Shadow Tyranitar", "Bite", "Brutal Swing"),
        ("Shadow Hydreigon", "Bite", "Brutal Swing"),
        ("Shadow Darkrai", "Snarl", "Shadow Ball"),
        ("Mega Absol", "Snarl", "Brutal Swing"),
        ("Mega Gengar", "Sucker Punch", "Shadow Ball"),
        ("Mega Houndoom", "Snarl", "Foul Play"),
        ("Shadow Absol", "Snarl", "Brutal Swing"),
        ("Mega Salamence", "Bite", "Brutal Swing"),
        ("Darkrai", "Snarl", "Shadow Ball"),
    ],
    "fairy": [
        ("Mega Gardevoir", "Charm", "Dazzling Gleam"),
        ("Crowned Sword Zacian", "Metal Claw", "Play Rough"),
        ("Enamorus (Incarnate Forme)", "Fairy Wind", "Dazzling Gleam"),
        ("Shadow Gardevoir", "Charm", "Dazzling Gleam"),
        ("Mega Alakazam", "Psycho Cut", "Dazzling Gleam"),
        ("Tapu Lele", "Astonish", "Nature's Madness"),
        ("Xerneas", "Geomancy", "Moonblast"),
        ("Tapu Koko", "Quick Attack", "Nature's Madness"),
        ("Tapu Bulu", "Bullet Seed", "Nature's Madness"),
        ("Mega Latias", "Charm", "Outrage"),
    ],
}

# ------------------------------------------------- budget.webp transcription --
# "Top Basic Raid Attackers - July 2026" (DialgaDex / bulbavisual).
BUDGET = {
    "normal": [],
    "ground": [("garchomp", "mud shot", "earth power"),
               ("excadrill", "mud-slap", "scorching sands"),
               ("rhyperior", "mud-slap", "earthquake")],
    "fighting": [("lucario", "force palm", "aura sphere"),
                 ("blaziken", "counter", "aura sphere"),
                 ("conkeldurr", "force palm", "dynamic punch")],
    "rock": [("rhyperior", "smack down", "rock wrecker"),
             ("glimmora", "rock throw", "meteor beam"),
             ("rampardos", "smack down", "rock slide")],
    "ice": [("baxcalibur", "ice fang", "avalanche"),
            ("mamoswine", "powder snow", "avalanche"),
            ("darmanitan", "ice fang", "avalanche")],
    "fire": [("volcarona", "fire spin", "overheat"),
             ("chandelure", "fire spin", "overheat"),
             ("cinderace", "fire spin", "blast burn")],
    "fairy": [("gardevoir", "charm", "dazzling gleam"),
              ("togekiss", "charm", "dazzling gleam"),
              ("hatterene", "charm", "dazzling gleam")],
    "grass": [("rillaboom", "razor leaf", "frenzy plant"),
              ("meowscarada", "leafage", "frenzy plant"),
              ("roserade", "magical leaf", "grass knot")],
    "steel": [("metagross", "bullet punch", "meteor mash"),
              ("tinkaton", "fairy wind", "gigaton hammer"),
              ("lucario", "force palm", "meteor mash")],
    "flying": [("salamence", "fire fang", "fly"),
               ("toucannon", "peck", "beak blast"),
               ("staraptor", "gust", "fly")],
    "water": [("inteleon", "water gun", "hydro cannon"),
              ("quaquaval", "water gun", "hydro cannon"),
              ("primarina", "waterfall", "hydro cannon")],
    "bug": [("volcarona", "bug bite", "bug buzz"),
            ("vikavolt", "bug bite", "x-scissor"),
            ("kleavor", "fury cutter", "x-scissor")],
    "dark": [("hydreigon", "bite", "brutal swing"),
             ("tyranitar", "bite", "brutal swing"),
             ("kingambit", "snarl", "foul play")],
    "electric": [("electivire", "thunder shock", "wild charge"),
                 ("magnezone", "volt switch", "wild charge"),
                 ("luxray", "spark", "wild charge")],
    "psychic": [("metagross", "zen headbutt", "psychic"),
                ("espeon", "confusion", "psychic"),
                ("alakazam", "confusion", "psychic")],
    "ghost": [("gholdengo", "hex", "shadow ball"),
              ("dragapult", "astonish", "shadow ball"),
              ("chandelure", "hex", "shadow ball")],
    "poison": [("roserade", "poison jab", "sludge bomb"),
               ("overqwil", "poison jab", "sludge bomb"),
               ("revavroom", "poison jab", "gunk shot")],
    "dragon": [("baxcalibur", "ice fang", "glaive rush"),
               ("haxorus", "dragon tail", "breaking swipe"),
               ("garchomp", "dragon tail", "breaking swipe")],
}

# ------------------------------------------------------ GO move inventory --
# Every fast + charged move known to exist in Pokemon GO (lowercase, straight
# apostrophes), used ONLY to verify prefix uniqueness. Over-inclusion is
# harmless (it just lengthens prefixes); omissions are what can make a baked
# prefix silently match a move it shouldn't.
# Last reviewed 2026-07-06; extend when Niantic adds moves, then regen.
GO_MOVES = [
    # fast
    "acid", "air slash", "astonish", "bite", "bubble", "bug bite",
    "bullet punch", "bullet seed", "charge beam", "charm", "confusion",
    "counter", "cut", "double kick", "dragon breath", "dragon tail",
    "ember", "extrasensory", "fairy wind", "feint attack", "fire fang",
    "fire spin", "force palm", "frost breath", "fury cutter", "geomancy",
    "gust", "hex", "hidden power", "ice fang", "ice shard", "incinerate",
    "infestation", "iron tail", "karate chop", "leafage", "lick", "lock-on",
    "low kick", "magical leaf", "metal claw", "mud shot", "mud-slap",
    "peck", "poison jab", "poison sting", "pound", "powder snow", "present",
    "psycho cut", "psywave", "quick attack", "razor leaf", "rock smash",
    "rock throw", "rollout", "sand attack", "scratch", "shadow claw",
    "smack down", "snarl", "spark", "splash", "steel wing", "struggle bug",
    "sucker punch", "tackle", "take down", "thunder fang", "thunder shock",
    "transform", "vine whip", "volt switch", "water gun", "water shuriken",
    "waterfall", "wing attack", "yawn", "zen headbutt",
    # charged
    "acid spray", "aerial ace", "aeroblast", "air cutter", "ancient power",
    "aqua jet", "aqua tail", "aura sphere", "aurora beam", "avalanche",
    "beak blast", "behemoth bash", "behemoth blade", "blast burn",
    "blaze kick", "bleakwind storm", "blizzard", "body slam", "bone club",
    "boomburst", "brave bird", "breaking swipe", "brick break", "brine",
    "brutal swing", "bubble beam", "bug buzz", "bulldoze", "close combat",
    "crabhammer", "cross chop", "cross poison", "crunch", "crush grip",
    "dark pulse", "dazzling gleam", "dig", "disarming voice", "discharge",
    "doom desire", "double iron bash", "draco meteor", "dragon ascent",
    "dragon claw", "dragon pulse", "drain punch", "draining kiss",
    "drill peck", "drill run", "dynamic punch", "dynamax cannon",
    "earth power", "earthquake", "energy ball", "fell stinger",
    "fire blast", "fire punch", "flame burst", "flame charge",
    "flame wheel", "flamethrower", "flash cannon", "fly", "focus blast",
    "foul play", "freeze shock", "frenzy plant", "fusion bolt",
    "fusion flare", "future sight", "giga drain", "giga impact",
    "gigaton hammer", "glaciate", "glaive rush", "grass knot", "gunk shot",
    "gyro ball", "heart stamp", "heat wave", "heavy slam",
    "high horsepower", "horn attack", "horn drill", "hurricane",
    "hydro cannon", "hydro pump", "hyper beam", "hyper fang", "ice beam",
    "ice burn", "ice punch", "icicle spear", "icy wind", "iron head",
    "last resort", "leaf blade", "leaf storm", "leaf tornado",
    "liquidation", "low sweep", "lunge", "luster purge", "magma storm",
    "magnet bomb", "megahorn", "meteor beam", "meteor mash", "mind blown",
    "mirror coat", "mirror shot", "mist ball", "moonblast",
    "moongeist beam", "mud bomb", "muddy water", "mystical fire",
    "nature's madness", "night shade", "night slash", "oblivion wing",
    "obstruct", "octazooka", "ominous wind", "origin pulse", "outrage",
    "overheat", "parabolic charge", "payback", "petal blizzard",
    "play rough", "plasma fists", "poison fang", "poltergeist",
    "power gem", "power whip", "power-up punch", "precipice blades",
    "psybeam", "psychic", "psychic fangs", "psycho boost", "psyshock",
    "psystrike", "rock blast", "rock slide", "rock tomb", "rock wrecker",
    "roar of time", "sacred fire", "sacred sword", "sand tomb",
    "sandsear storm", "scald", "scorching sands", "secret sword",
    "seed bomb", "seed flare", "shadow ball", "shadow bone", "shadow force",
    "shadow punch", "shadow sneak", "signal beam", "silver wind",
    "skull bash", "sky attack", "sludge", "sludge bomb", "sludge wave",
    "solar beam", "spacial rend", "springtide storm", "stomp", "stone edge",
    "submission", "sunsteel strike", "superpower", "surf", "swift",
    "synchronoise", "techno blast", "thunder", "thunder punch",
    "thunderbolt", "torch song", "trailblaze", "triple axel", "twister",
    "upper hand", "v-create", "vise grip", "water pulse", "weather ball",
    "wild charge", "wildbolt storm", "wrap", "x-scissor", "zap cannon",
]

# ------------------------------------------------------- moveset overrides --
# Additional vouched sets the source rankings omit. GO Hub's per-type table
# scores same-type fast damage, but raid sims often favor an energy fast:
# e.g. it lists Swampert as Water Gun/Hydro Cannon while the community
# consensus raid set is Mud Shot/Hydro Cannon (more energy -> more Hydro
# Cannons -> higher total DPS). Rows here are inserted next to the species'
# existing entries in that type's bucket, so BOTH sets end up vouched.
# Two classes of additions:
#   (a) contested/co-equal sets the source ranking omitted, and
#   (b) NON-LEGACY fallbacks where the listed set needs a Community Day /
#       Elite TM move (marked * in the sources) -- so players without the
#       legacy version still match with their best obtainable build.
# Deliberately NOT added (no raid-viable non-legacy alternative exists):
# Magma Storm Heatran, Meteor Mash Metagross, Geomancy Xerneas, Hydro
# Cannon Swampert, Ice Burn / Freeze Shock Kyurem, Nature's Madness tapus,
# Smack Down Tyranitar (its only rock fast).
EXTRA_SETS = {
    "normal": [
        ("Regigigas", "Hidden Power", "Giga Impact"),        # Crush Grip*
    ],
    "fighting": [
        ("Lucario", "Counter", "Aura Sphere"),               # Force Palm*
        ("Conkeldurr", "Counter", "Dynamic Punch"),          # long-time standard
        ("Terrakion", "Double Kick", "Close Combat"),        # Sacred Sword*
    ],
    "fire": [
        ("Mega Charizard Y", "Fire Spin", "Overheat"),       # Blast Burn*
        ("Mega Blaziken", "Fire Spin", "Overheat"),          # Blast Burn*
        ("Reshiram", "Fire Fang", "Overheat"),               # Fusion Flare*
    ],
    "water": [
        ("Shadow Swampert", "Mud Shot", "Hydro Cannon"),     # energy fast
        ("Mega Swampert", "Mud Shot", "Hydro Cannon"),
        ("Kyogre", "Waterfall", "Hydro Pump"),               # Origin Pulse*
        ("Mega Blastoise", "Water Gun", "Hydro Pump"),       # Hydro Cannon*
        ("Shadow Feraligatr", "Water Gun", "Hydro Pump"),    # Hydro Cannon*
        ("Shadow Samurott", "Waterfall", "Hydro Pump"),      # Hydro Cannon*
        ("Primarina", "Waterfall", "Hydro Pump"),            # Hydro Cannon*
    ],
    "grass": [
        ("Mega Sceptile", "Fury Cutter", "Leaf Blade"),      # Frenzy Plant*
        ("Mega Venusaur", "Vine Whip", "Solar Beam"),        # Frenzy Plant*
        ("Rillaboom", "Razor Leaf", "Grass Knot"),           # Frenzy Plant*
    ],
    "electric": [
        ("Shadow Zapdos", "Charge Beam", "Thunderbolt"),     # Thunder Shock*
    ],
    "psychic": [
        ("Shadow Mewtwo", "Psycho Cut", "Psychic"),          # Psystrike*
        ("Mega Mewtwo Y", "Confusion", "Psychic"),           # Psystrike*
        ("Mega Alakazam", "Confusion", "Future Sight"),      # Psychic*
    ],
    "ground": [
        ("Shadow Groudon", "Mud Shot", "Earthquake"),        # Precipice Blades*
        ("Shadow Garchomp", "Mud Shot", "Earthquake"),       # Earth Power*
        ("Landorus (Therian Forme)", "Mud Shot", "Earthquake"),  # Sandsear*
    ],
    "rock": [
        ("Shadow Rhyperior", "Smack Down", "Stone Edge"),    # Rock Wrecker*
        ("Shadow Gigalith", "Lock-On", "Rock Slide"),        # Meteor Beam*
    ],
    "ghost": [
        # Shadow Claw is Gengar's non-legacy standard (Lick*); Shadow Claw
        # is the consensus set for Dawn Wings, not Psycho Cut.
        ("Mega Gengar", "Shadow Claw", "Shadow Ball"),
        ("Dawn Wings Necrozma", "Shadow Claw", "Moongeist Beam"),
    ],
    "poison": [
        ("Mega Gengar", "Shadow Claw", "Sludge Bomb"),       # Lick*
    ],
    "bug": [
        # Bug Bite and Fury Cutter are co-equal standards on Pinsir.
        ("Mega Pinsir", "Bug Bite", "X-Scissor"),
        ("Shadow Pinsir", "Bug Bite", "X-Scissor"),
    ],
    "dragon": [
        ("Mega Dragonite", "Dragon Breath", "Draco Meteor"), # contested fast
        ("Mega Dragonite", "Dragon Tail", "Outrage"),        # Draco Meteor*
        ("Mega Rayquaza", "Dragon Tail", "Outrage"),         # Breaking Swipe*
    ],
    "dark": [
        ("Shadow Hydreigon", "Bite", "Dark Pulse"),          # Brutal Swing*
        ("Mega Absol", "Snarl", "Dark Pulse"),               # Brutal Swing*
    ],
    "flying": [
        ("Yveltal", "Gust", "Hurricane"),                    # Oblivion Wing*
        ("Staraptor", "Wing Attack", "Fly"),                 # Gust*
    ],
}

# ------------------------------------------------- learnability overlaps --
# Per species: moves from the baked pool it CAN learn but is NOT vouched
# with (any row). The runtime emits a per-species implication clause ONLY
# when one of these overlaps the query's move pool -- everything else is
# already exact for free, because a species physically can't satisfy the
# other clauses ("inteleon will never have origin pulse"). Best-effort,
# hand-curated from GO learnsets: a MISSING pair here shows up as a small
# leak (species matching with a suboptimal-but-listed move); an EXTRA pair
# just costs string length. Extend when you spot either.
EXTRA_LEARNABLE = {
    "lucario": ["close combat", "shadow ball"],
    "blaziken": ["focus blast", "stone edge"],
    "keldeo": ["hydro pump"],
    "heracross": ["earthquake"],
    "conkeldurr": ["focus blast", "stone edge"],
    "terrakion": ["rock slide", "earthquake", "smack down"],
    "regigigas": ["focus blast"],
    "zacian": ["iron head", "quick attack", "snarl", "fire fang", "close combat"],
    "lopunny": ["focus blast", "low kick"],
    "porygon-z": ["discharge"],
    "meloetta": ["psychic", "thunderbolt", "dazzling gleam"],
    "groudon": ["dragon tail"],
    "garchomp": ["outrage"],
    "landorus": ["rock slide", "focus blast"],
    "excadrill": ["earthquake", "drill run", "rock slide"],
    "diancie": ["moonblast", "dazzling gleam"],
    "aerodactyl": ["ancient power", "earth power", "iron head", "bite"],
    "tyrantrum": ["outrage", "earthquake", "dragon tail"],
    "rampardos": ["outrage"],
    "pinsir": ["close combat"],
    "scizor": ["iron head", "bullet punch"],
    "vikavolt": ["discharge", "spark", "mud-slap"],
    "kleavor": ["rock slide", "quick attack"],
    "volcarona": ["psychic"],
    "beedrill": ["drill run"],
    "escavalier": ["drill run", "counter"],
    "metagross": ["earthquake"],
    "zamazenta": ["close combat", "iron head", "moonblast"],
    "tinkaton": ["play rough"],
    "charizard": ["dragon breath"],
    "reshiram": ["draco meteor", "dragon breath", "stone edge"],
    "blacephalon": ["overheat", "shadow ball", "astonish"],
    "heatran": ["iron head", "earth power", "stone edge"],
    "moltres": ["ancient power"],
    "delphox": ["psychic", "zen headbutt"],
    "chandelure": ["incinerate"],
    "cinderace": ["focus blast"],
    "inteleon": ["hydro pump"],
    "blastoise": ["ice beam", "bite"],
    "gyarados": ["bite", "dragon breath", "outrage"],
    "feraligatr": ["waterfall", "ice fang"],
    "kingler": ["metal claw", "mud shot", "x-scissor"],
    "samurott": ["fury cutter", "megahorn"],
    "quaquaval": ["close combat"],
    "primarina": ["moonblast", "charm", "psychic"],
    "sceptile": ["earthquake", "bullet seed"],
    "venusaur": ["sludge bomb", "razor leaf"],
    "chesnaught": ["smack down"],
    "zarude": ["bite"],
    "shaymin": ["zen headbutt"],
    "tangrowth": ["rock slide", "sludge bomb"],
    "rillaboom": ["earth power"],
    "meowscarada": ["play rough"],
    "roserade": ["dazzling gleam", "razor leaf"],
    "zeraora": ["close combat", "wild charge", "spark"],
    "raikou": ["thunderbolt", "volt switch"],
    "zekrom": ["outrage", "dragon breath"],
    "xurkitree": ["power whip"],
    "manectric": ["snarl", "overheat", "charge beam"],
    "thundurus": ["focus blast"],
    "zapdos": ["ancient power"],
    "magnezone": ["discharge", "spark"],
    "electivire": ["thunderbolt", "low kick"],
    "luxray": ["hidden power"],
    "alakazam": ["focus blast", "shadow ball"],
    "latios": ["dragon breath"],
    "gardevoir": ["shadow ball", "magical leaf"],
    "gallade": ["close combat", "leaf blade", "low kick", "charm"],
    "latias": ["psychic", "dragon breath", "zen headbutt"],
    "kyurem": ["draco meteor"],
    "mamoswine": ["ancient power", "mud-slap"],
    "darmanitan": ["overheat", "fire fang", "incinerate", "rock slide"],
    "baxcalibur": ["breaking swipe", "dragon breath"],
    "salamence": ["outrage"],
    "haxorus": ["earthquake", "counter"],
    "hydreigon": ["dragon breath"],
    "darkrai": ["focus blast"],
    "absol": ["psycho cut", "megahorn", "play rough"],
    "gengar": ["focus blast", "hex", "psychic", "dazzling gleam"],
    "nihilego": ["rock slide", "acid", "gunk shot"],
    "victreebel": ["razor leaf"],
    "naganadel": ["air slash"],
    "scolipede": ["x-scissor", "megahorn", "bug bite"],
    "revavroom": ["overheat"],
    "gholdengo": ["focus blast", "astonish", "dazzling gleam"],
    "dragapult": ["dragon breath", "outrage", "hex"],
    "lunala": ["moongeist beam", "psychic", "air slash", "confusion"],
    "banette": ["hex"],
    "enamorus": ["astonish"],
    "yveltal": ["snarl", "focus blast", "sucker punch"],
    "articuno": ["ice beam", "ancient power"],
    "toucannon": ["bullet seed"],
    "staraptor": ["close combat", "quick attack"],
    "togekiss": ["ancient power", "air slash"],
    "hatterene": ["psychic", "confusion"],
    "xerneas": ["close combat", "giga impact", "megahorn"],
    "tapu lele": ["confusion", "focus blast"],
    "tapu koko": ["volt switch", "dazzling gleam", "thunderbolt"],
    "tapu bulu": ["grass knot", "megahorn"],
    "kingambit": ["iron head", "metal claw"],
}

# Bare search keywords a prefix must never collide with exactly.
RESERVED_WORDS = {"mega", "shadow", "purified", "legendary", "mythical",
                  "shiny", "lucky", "hatched", "traded", "defender",
                  "evolve", "item", "costume", "male", "female", "weather",
                  "special", "eggsonly", "background", "favorite"}

BEGIN_MARK = "/* ===== BEGIN GENERATED topByType (transcode.py) ===== */"
END_MARK = "/* ===== END GENERATED topByType ===== */"

# Form/variant words to strip so a display name collapses to the base
# species the in-game search matches (name matching is a prefix match on
# the species name, and it matches shadows/megas/formes alike).
PREFIXES = ("shadow ", "mega ", "primal ", "crowned sword ", "crowned shield ",
            "dusk mane ", "dawn wings ", "aria forme ", "white ", "black ",
            "galarian ", "alolan ", "hisuian ", "paldean ")


def base_species(display):
    n = display.lower().strip()
    n = re.sub(r"\s*\([^)]*\)", "", n)          # (Therian Forme) etc.
    changed = True
    while changed:
        changed = False
        for p in PREFIXES:
            if n.startswith(p):
                n = n[len(p):]
                changed = True
    n = re.sub(r"\s+[xy]$", "", n)              # charizard y, mewtwo x
    return n.strip()


def _all_moves():
    """GO_MOVES plus every move used in BEST/BUDGET (belt and braces)."""
    moves = set(GO_MOVES)
    for table in (BEST, BUDGET):
        for rows in table.values():
            for _s, fast, charged in rows:
                moves.add(fast.lower().strip())
                moves.add(charged.lower().strip())
    return moves


_ALL_MOVES = None


def shortest_prefix(m):
    """Shortest prefix (>= MIN_PREFIX_LEN) uniquely selecting move `m`
    among all known GO moves; the full name if none exists (e.g. `psychic`
    is a prefix of `psychic fangs`). Prefixes never end mid-punctuation,
    never equal a reserved keyword, and never sit inside a type name."""
    global _ALL_MOVES
    if _ALL_MOVES is None:
        _ALL_MOVES = _all_moves()
    if len(m) <= MIN_PREFIX_LEN:
        return m
    for length in range(MIN_PREFIX_LEN, len(m)):
        p = m[:length]
        if p[-1] in " -'":
            continue
        if p in RESERVED_WORDS:
            continue
        if any(t.startswith(p) for t in TYPES):
            continue
        if sum(1 for mv in _ALL_MOVES if mv.startswith(p)) == 1:
            return p
    return m


def norm_move(move):
    m = move.lower().strip()
    if "'" in m and APOSTROPHE_MODE == "drop":
        return None
    if USE_MOVE_PREFIXES:
        m = shortest_prefix(m)
    if "'" in m and APOSTROPHE_MODE == "curly":
        m = m.replace("'", "’")
    return m


def build_buckets():
    """Two tiers per type, each a list of 'species~fast~charged' in rank
    order: best (GO Hub, one entry per base species, capped) and budget
    (the infographic, kept whole). The runtime interleaves them per round
    so budget counters are guaranteed a seat in the search string."""
    best, budget = {}, {}
    for t in TYPES:
        # Keep every form row per species, merging rows that share the same
        # moveset into one entry whose tag is the UNION of vouching forms
        # (e.g. kyogre~waterf~orig~bsm). The runtime derives the form
        # requirement from that union: 'b' present -> none; otherwise
        # "!s,shadow" / "!s,megaevolve,mega" / both combined. The cap
        # counts distinct species, not rows.
        distinct, order, tags = [], [], {}
        for display, fast, charged in BEST.get(t, []):
            d = display.lower()
            if d.startswith(("mega ", "primal ")) and not INCLUDE_MEGA_RANKED:
                continue
            if d.startswith("shadow ") and not INCLUDE_SHADOW_RANKED:
                continue
            s = base_species(display)
            f, c = norm_move(fast), norm_move(charged)
            if f is None or c is None:
                continue
            key = (s, f, c)
            if key not in tags:
                if s not in distinct:
                    if len(distinct) >= ATTACKERS_PER_TYPE:
                        continue
                    distinct.append(s)
                tags[key] = set()
                order.append(key)
            if d.startswith("shadow "):
                tags[key].add("s")
            elif d.startswith(("mega ", "primal ")):
                tags[key].add("m")
            else:
                tags[key].add("b")
        for display, fast, charged in EXTRA_SETS.get(t, []):
            d = display.lower()
            if d.startswith(("mega ", "primal ")) and not INCLUDE_MEGA_RANKED:
                continue
            if d.startswith("shadow ") and not INCLUDE_SHADOW_RANKED:
                continue
            s = base_species(display)
            f, c = norm_move(fast), norm_move(charged)
            if f is None or c is None:
                continue
            if d.startswith("shadow "):
                tag = "s"
            elif d.startswith(("mega ", "primal ")):
                tag = "m"
            else:
                tag = "b"
            key = (s, f, c)
            if key in tags:
                tags[key].add(tag)
                continue
            idxs = [i for i, k in enumerate(order) if k[0] == s]
            if not idxs:
                if len(distinct) >= ATTACKERS_PER_TYPE:
                    continue
                distinct.append(s)
            tags[key] = {tag}
            # Adjacent to its species' rows so both sets admit together.
            order.insert(idxs[-1] + 1 if idxs else len(order), key)

        best[t] = [f"{s}~{f}~{c}~{''.join(x for x in 'bsm' if x in tags[(s, f, c)])}"
                   for s, f, c in order]
        bentries = []
        for s, fast, charged in BUDGET.get(t, []):
            f, c = norm_move(fast), norm_move(charged)
            if f is None or c is None:
                continue
            bentries.append(f"{s}~{f}~{c}~b")
        budget[t] = bentries

    # The exact-pinning encoding relies on species terms being mutually
    # exclusive; a species name that is a substring of another would break
    # that (name matching is substring-based). Warn loudly if so.
    names = {e.split("~")[0] for tbl in (best, budget)
             for es in tbl.values() for e in es}
    for a in names:
        for b in names:
            if a != b and a in b:
                print(f"WARNING: species term '{a}' is a substring of "
                      f"'{b}' -- exact pinning may cross-match them")
    return best, budget


# ---------------------------------------------------- build-time packer --
# The full expansion algorithm now runs HERE at build time; the phone-side
# BeanShell only does map lookups (plus the tiny raidn/DAYS/CP logic).

def compute_se(boss_types):
    """SE attack types vs a 1- or 2-type boss; double-SE first."""
    if len(boss_types) == 1:
        return WEAK_TO[boss_types[0]].split(" ")
    double_se, single_se = [], []
    for atk in TYPES:
        product = 1.0
        for d in boss_types:
            if atk in IMMUNE_TO.get(d, "").split():
                product *= 0.390625
            elif atk in WEAK_TO.get(d, "").split():
                product *= 1.6
            elif atk in RESISTS_FROM.get(d, "").split():
                product *= 0.625
        if product >= 2.5:
            double_se.append(atk)
        elif product >= 1.5:
            single_se.append(atk)
    return double_se + single_se


def interleave(se_types, best, budget):
    """Round-robin across SE buckets; per round, best tier then budget."""
    best_arrs = [best.get(t, []) for t in se_types]
    budget_arrs = [budget.get(t, []) for t in se_types]
    max_rounds = max((len(a) for a in best_arrs + budget_arrs), default=0)
    entries, seen = [], set()
    for r in range(max_rounds):
        for a1, a2 in zip(best_arrs, budget_arrs):
            for arr in (a1, a2):
                if r < len(arr) and arr[r] not in seen:
                    seen.add(arr[r])
                    entries.append(arr[r])
    return entries


def learn_extra_tokens():
    out = {}
    for sp, moves in EXTRA_LEARNABLE.items():
        toks = [tk for tk in (norm_move(m) for m in moves) if tk]
        if toks:
            out[sp] = "|" + "|".join(toks) + "|"
    return out


def assemble_exact(se_types, best, budget, extras_tok, limit):
    """Species clause + global fast/charged clauses + conflict-only
    implication clauses + form clauses, greedily packed to `limit`."""
    entries = interleave(se_types, best, budget)
    sp_order, sp_fasts, sp_chargeds, sp_forms = [], {}, {}, {}
    assembled = None

    def build():
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
            extras = extras_tok.get(xs, "")
            if any(f"|{m}|" in extras for m in g_fasts):
                parts.append("!" + xs + "".join(",@" + m for m in sp_fasts[xs]))
            if any(f"|{m}|" in extras for m in g_chargeds):
                parts.append("!" + xs + "".join(",@" + m for m in sp_chargeds[xs]))
            if "b" not in sp_forms[xs]:
                clause = "!" + xs
                if "s" in sp_forms[xs]:
                    clause += ",shadow"
                if "m" in sp_forms[xs]:
                    clause += ",megaevolve,mega"
                if clause != "!" + xs:
                    parts.append(clause)
        return "&".join(parts)

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
            if len(candidate) <= limit:
                assembled = candidate
                progress = True
            else:
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
        return ",".join("@" + t for t in se_types)
    return assembled


def assemble_attkr(atk_type, best, budget, limit):
    """Every vouched species of one type that carries a type move."""
    specs = []
    for e in interleave([atk_type], best, budget):
        s = e.split("~")[0]
        if s not in specs:
            specs.append(s)
    suffix = "&@" + atk_type
    while specs:
        cand = ",".join(specs) + suffix
        if len(cand) <= limit:
            return cand
        specs.pop()
    return "@" + atk_type


def build_se_map():
    """Canonical typePart -> SE list, for all single types and all
    lexicographically-sorted dual pairs (the cancellation is symmetric)."""
    se_map = {}
    for t in TYPES:
        se_map[t] = compute_se([t])
    for a, b in ((a, b) for a in sorted(TYPES) for b in sorted(TYPES) if a < b):
        se = compute_se([a, b])
        if se:
            se_map[f"{a}-{b}"] = se
    return se_map


def emit_block(best, budget, indent=" " * 8):
    extras_tok = learn_extra_tokens()
    se_map = build_se_map()

    expansions, cpx = {}, {}
    for canon, se in se_map.items():
        full = assemble_exact(se, best, budget, extras_tok, MAX_SEARCH_LEN)
        cpv = assemble_exact(se, best, budget, extras_tok,
                             MAX_SEARCH_LEN - CP_RESERVE)
        expansions["raid-" + canon] = full
        if cpv != full:
            cpx["raid-" + canon] = cpv
    for t in TYPES:
        full = assemble_exact([t], best, budget, extras_tok, MAX_SEARCH_LEN)
        cpv = assemble_exact([t], best, budget, extras_tok,
                             MAX_SEARCH_LEN - CP_RESERVE)
        expansions["atk-" + t] = full
        if cpv != full:
            cpx["atk-" + t] = cpv
        full = assemble_attkr(t, best, budget, MAX_SEARCH_LEN)
        cpv = assemble_attkr(t, best, budget, MAX_SEARCH_LEN - CP_RESERVE)
        expansions["attkr-" + t] = full
        if cpv != full:
            cpx["attkr-" + t] = cpv

    # Serialize all maps into one TSV payload, gzip it (the strings are
    # highly repetitive, ~5x), and bake a base64 blob the runtime inflates.
    # Keeps the Tasker paste small; baked.tsv is the human-readable twin.
    rows = []
    for canon in se_map:
        rows.append(f"s\t{canon}\t{' '.join(se_map[canon])}")
    for t in TYPES:
        rows.append(f"t\t{t}\t{','.join(best[t])}")
    for t in TYPES:
        rows.append(f"b\t{t}\t{','.join(budget[t])}")
    for k in expansions:
        rows.append(f"e\t{k}\t{expansions[k]}")
    for k in cpx:
        rows.append(f"c\t{k}\t{cpx[k]}")
    payload = "\n".join(rows)
    blob = base64.b64encode(
        gzip.compress(payload.encode("utf-8"), 9, mtime=0)).decode()
    chunks = [blob[i:i + 96] for i in range(0, len(blob), 96)]

    lines = [indent + BEGIN_MARK]
    lines.append(indent + "/* PRECOMPILED by transcode.py: all command expansions plus the")
    lines.append(indent + " * raidn- data, as gzip+base64 TSV (rows: kind<TAB>key<TAB>value;")
    lines.append(indent + " * kinds e=expansions c=cpx s=seTypes t=topByType b=budgetByType).")
    lines.append(indent + " * Human-readable twin: baked.tsv in the repo. */")
    lines.append(indent + "String DATA_B64 =")
    for i, ch in enumerate(chunks):
        prefix = '      "' if i == 0 else '    + "'
        suffix = '";' if i == len(chunks) - 1 else '"'
        lines.append(f"{indent}{prefix}{ch}{suffix}")
    lines.append(indent + END_MARK)
    return "\n".join(lines), payload


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", metavar="JAVA", help="splice block into this file")
    opts = ap.parse_args()

    best, budget = build_buckets()
    block, payload = emit_block(best, budget)

    if opts.apply:
        with open(opts.apply) as f:
            src = f.read()
        pat = re.compile(re.escape(BEGIN_MARK) + ".*?" + re.escape(END_MARK), re.S)
        if not pat.search(src):
            raise SystemExit(f"Markers not found in {opts.apply}")
        stripped = block.strip()
        src = pat.sub(lambda m: stripped, src, count=1)
        with open(opts.apply, "w") as f:
            f.write(src)
        import pathlib
        sidecar = pathlib.Path(opts.apply).with_name("baked.tsv")
        sidecar.write_text(payload + "\n", encoding="utf-8")
        print(f"Patched {opts.apply} (+ {sidecar.name})")
    else:
        print(block)


if __name__ == "__main__":
    main()
