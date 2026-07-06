/*
 * PoGo Search Text Expander -- Tasker BeanShell module.
 *
 * Extends old.java with a raid-counter command family (strictness ladder):
 *   @#attkr-fairy.          every vouched fairy attacker (optimal + budget
 *                           lists), any build that still has a fairy move
 *   @#raid-fire.            balanced counters for a single-type boss
 *   @#raid-dragon-steel.    same for a dual-type boss (chart-aware)
 *   @#atk-fairy.            ONE attack-type bucket, optimal-move clauses
 *   @#raidn-dragon-steel[2] EXACT query for the 2nd-ranked counter
 *                           (species&@fast&@charged; [N] defaults to 1)
 *   [CP] on attkr/raid/atk = CP floor, e.g. @#raid-fire[2500]
 *
 * Verified search grammar (Niantic help center, community wiki, 2026-07):
 *   - "," ":" ";" are OR; "&" and "|" are AND; "!" is NOT.
 *   - OR is ALWAYS evaluated before AND; parentheses are not supported.
 *     A search is therefore an AND of OR-clauses: "a,b&c,d" = (a OR b) AND (c OR d).
 *   - There is NO ">type" (super-effective) operator, and per-species
 *     movesets cannot be pinned inside an OR list.
 *   - The search field truncates around 200 characters (MAX_SEARCH_LEN).
 *
 * Expansion shape with EXACT_PINNING=true (the default):
 *   species,...&@fasts,...&@chargeds,...[&!s,vouched-moves][&!s,forms][&cp]
 * Global OR-clauses do most of the pinning for free: a species physically
 * cannot satisfy moves it can't learn. A per-species implication clause
 * ("!s,@its-vouched-moves") is added ONLY where the learnExtra table says
 * the species CAN learn a conflicting pool move (e.g. Feraligatr can learn
 * Waterfall but is vouched with Water Gun). Species vouched solely by
 * boosted forms get a form clause from the union of their ranking rows:
 * "!s,shadow", "!s,megaevolve,mega" (can-mega OR currently-mega'd), or
 * both combined; a base/budget row anywhere removes the requirement.
 * Entries are admitted greedily with multi-pass retry, interleaved round
 * by round across the SE buckets and tiers (best + budget), so budget.webp
 * counters are always represented alongside the meta picks. With no
 * curated data the move-TYPE clause stands alone as fallback.
 * EXACT_PINNING=false restores the plain tri-clause without implication
 * or form clauses.
 *
 * Moves with apostrophes are baked with the typographic U+2019 apostrophe
 * (the in-game glyph; a keyboard ' does not match -- see transcode.py's
 * APOSTROPHE_MODE to change strategy after on-device testing).
 *
 * Curated data between the GENERATED markers is produced by transcode.py
 * (runs on a computer, never on the phone). Sources: GO Hub best attackers
 * per type (pasted 2026-07-06) + budget.webp (DialgaDex, July 2026).
 *
 * All original static templates and the {DAYS:n} logic are unchanged.
 * Generated: 2026-07-06
 */
import io.reactivex.functions.Consumer;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.os.Bundle;

replacementMap = tasker.getJavaVariable("textExpanderReplacements");
if (replacementMap == null || replacementMap == void) {
    replacementMap = new HashMap();
    tasker.setJavaVariable("textExpanderReplacements", replacementMap);
}

pogoSearchReplacer = new Consumer() {
    accept(Object argsObject) {
        /* Cast the generic Object to a Map to access fields by name. */
        Map args = (Map) argsObject;

        /* Retrieve arguments cleanly. */
        AccessibilityNodeInfo source = (AccessibilityNodeInfo) args.get("source");
        String currentText = (String) args.get("text");
        String trigger = (String) args.get("trigger");

        /* The in-game search field truncates around this many characters. */
        int MAX_SEARCH_LEN = 200;

        /* Operator precedence, CONFIRMED on device (2026-07-06):
         * "@origin,@psy&kyogre,mewtwo" returned Origin-Pulse Kyogre and
         * Psystrike Mewtwo, and appending ",charizard" to the species side
         * surfaced NO Charizard. So ',' (OR) is evaluated BEFORE '&' (AND):
         * a search is an AND of OR-clauses, parentheses do not exist, and
         * an OR of exact AND-groups is unexpressible (a comma-joined group
         * chain collapses into one impossible conjunction -- verified: it
         * returns nothing). raidn- (one pure-AND query) is the exact tool.
         * Also observed: @move matching is PREFIX-based (@psy = Psystrike,
         * Psychic, ...). Flip this only if the game's parser changes. */
        boolean AND_BINDS_TIGHTER = false;

        /* Exact pinning. Species terms are mutually exclusive, so the
         * inexpressible OR-of-AND-groups rewrites into implications:
         *   s1,s2 & !s1,m1 & !s2,m2  ("if it is s1, it must have m1").
         * Learnability makes most implications redundant (Inteleon can
         * never have Origin Pulse), so a per-species clause is emitted
         * only where the learnExtra table records a real conflict, plus
         * form clauses for boosted-form-only vouchings. Set false for
         * the plain tri-clause without implication/form clauses. */
        boolean EXACT_PINNING = true;

        /* Search templates definition. */
        Map templates = new HashMap();
        templates.put("pvp", "0-1attack&3-4hp&3-4defense");
        templates.put("transfer", "{DAYS:0}&0*,1*,2*&!shiny&!@special&!background");
        templates.put("trash", "{DAYS:0}&0*&!shiny&!legendary&!mythical&!background&!@special");
        templates.put("hundo", "4*");
        templates.put("shundo", "4*&shiny");
        templates.put("legendary", "legendary");
        templates.put("mythical", "mythical");
        templates.put("shadow", "shadow");
        templates.put("purified", "purified");
        templates.put("recent", "{DAYS:0}");

        /* Pokemon GO type chart. Values are space-separated attack-type
         * lists per defending type. GO multipliers: SE x1.6, resist x0.625,
         * "immune" x0.390625; dual-type = product of both halves. */
        String[] ALL_TYPES = { "normal", "fire", "water", "electric", "grass",
            "ice", "fighting", "poison", "ground", "flying", "psychic", "bug",
            "rock", "ghost", "dragon", "dark", "steel", "fairy" };

        Map weakTo = new HashMap();
        weakTo.put("normal", "fighting");
        weakTo.put("fire", "water ground rock");
        weakTo.put("water", "electric grass");
        weakTo.put("electric", "ground");
        weakTo.put("grass", "fire ice poison flying bug");
        weakTo.put("ice", "fire fighting rock steel");
        weakTo.put("fighting", "flying psychic fairy");
        weakTo.put("poison", "ground psychic");
        weakTo.put("ground", "water grass ice");
        weakTo.put("flying", "electric ice rock");
        weakTo.put("psychic", "bug ghost dark");
        weakTo.put("bug", "fire flying rock");
        weakTo.put("rock", "water grass fighting ground steel");
        weakTo.put("ghost", "ghost dark");
        weakTo.put("dragon", "ice dragon fairy");
        weakTo.put("dark", "fighting bug fairy");
        weakTo.put("steel", "fire fighting ground");
        weakTo.put("fairy", "poison steel");

        Map resistsFrom = new HashMap();
        resistsFrom.put("fire", "fire grass ice bug steel fairy");
        resistsFrom.put("water", "fire water ice steel");
        resistsFrom.put("electric", "electric flying steel");
        resistsFrom.put("grass", "water electric grass ground");
        resistsFrom.put("ice", "ice");
        resistsFrom.put("fighting", "bug rock dark");
        resistsFrom.put("poison", "grass fighting poison bug fairy");
        resistsFrom.put("ground", "poison rock");
        resistsFrom.put("flying", "grass fighting bug");
        resistsFrom.put("psychic", "fighting psychic");
        resistsFrom.put("bug", "grass fighting ground");
        resistsFrom.put("rock", "normal fire poison flying");
        resistsFrom.put("ghost", "poison bug");
        resistsFrom.put("dragon", "fire water grass electric");
        resistsFrom.put("dark", "ghost dark");
        resistsFrom.put("steel", "normal grass ice flying psychic bug rock dragon steel fairy");
        resistsFrom.put("fairy", "fighting bug dark");

        Map immuneTo = new HashMap();
        immuneTo.put("normal", "ghost");
        immuneTo.put("ghost", "normal fighting");
        immuneTo.put("dark", "psychic");
        immuneTo.put("fairy", "dragon");
        immuneTo.put("flying", "ground");
        immuneTo.put("ground", "electric");
        immuneTo.put("steel", "poison");

        /* Curated raid attackers per ATTACK type, two tiers (top meta and
         * budget builds), comma-joined species~fast~charged entries. */
        Map topByType = new HashMap();
        Map budgetByType = new HashMap();
        Map learnExtra = new HashMap();
        /* ===== BEGIN GENERATED topByType (transcode.py) ===== */
        /* Entries are species~fast~charged~formtags in rank order ('~' is
         * internal). topByType = GO Hub best (top 10, 2026-07-06);
         * budgetByType = budget.webp (DialgaDex, Jul 2026). learnExtra =
         * pool moves a species can learn beyond its vouched rows; a
         * per-species clause is emitted only on such conflicts. */
        topByType.put("normal", "regigigas~hidd~crus~bs,regigigas~hidd~giga i~b,mewtwo~psycho c~hyper b~sm,zacian~meta~giga i~b,lopunny~poun~hyper b~m,porygon-z~lock~hyper b~bs,meloetta~quic~hyper b~b");
        topByType.put("fire", "charizard~fire s~blas~m,charizard~fire s~over~m,reshiram~fire f~fusion f~bs,reshiram~fire f~over~b,blaziken~fire s~blas~m,blaziken~fire s~over~m,blacephalon~inci~mind~b,heatran~fire s~magm~s,moltres~fire s~over~s,delphox~fire s~blas~s,chandelure~fire s~over~s");
        topByType.put("water", "kyogre~waterf~orig~bsm,kyogre~waterf~hydro p~b,swampert~water g~hydro c~sm,swampert~mud s~hydro c~sm,blastoise~water g~hydro c~m,blastoise~water g~hydro p~m,gyarados~waterf~hydro p~m,feraligatr~water g~hydro c~s,feraligatr~water g~hydro p~s,kingler~bubble~crab~s,samurott~waterf~hydro c~s,samurott~waterf~hydro p~s,primarina~waterf~hydro p~b");
        topByType.put("electric", "mewtwo~psycho c~thunderb~m,zeraora~volt~plas~b,raikou~thunder s~wild c~s,zekrom~charg~wild c~b,xurkitree~thunder s~disc~b,manectric~thunder f~wild c~m,thundurus~volt~thunderb~s,zapdos~thunder s~thunderb~s,zapdos~charg~thunderb~s,magnezone~volt~wild c~s");
        topByType.put("grass", "sceptile~fury~fren~m,sceptile~fury~leaf b~m,venusaur~vine~fren~sm,venusaur~vine~sola~m,kartana~razo~leaf b~b,chesnaught~vine~fren~s,zarude~vine~power w~b,shaymin~magi~grass k~b,tangrowth~vine~power w~s,victreebel~magi~leaf b~m,rillaboom~razo~fren~b,rillaboom~razo~grass k~b");
        topByType.put("ice", "kyurem~ice f~ice bu~b,kyurem~dragon t~free~b,mewtwo~psycho c~ice be~sm,mamoswine~powd~aval~s,gardevoir~charm~trip~m,zamazenta~ice f~behemoth ba~b,baxcalibur~ice f~aval~b,kyogre~waterf~aval~m");
        topByType.put("fighting", "lucario~forc~aura~bm,lucario~coun~aura~b,blaziken~coun~aura~sm,keldeo~low k~secr~b,mewtwo~psycho c~focu~m,heracross~coun~clos~m,conkeldurr~forc~dynami~s,conkeldurr~coun~dynami~b,terrakion~double k~sacred s~b,terrakion~double k~clos~b");
        topByType.put("poison", "eternatus~poison j~sludge b~b,gengar~lick~sludge b~m,gengar~shadow c~sludge b~m,beedrill~poison j~sludge b~m,nihilego~poison j~sludge b~b,victreebel~acid~sludge b~m,overqwil~poison j~sludge b~s,naganadel~poison j~sludge b~b,roserade~poison j~sludge b~b,revavroom~poison j~gunk~b,scolipede~poison j~sludge b~s");
        topByType.put("ground", "groudon~mud s~prec~bsm,groudon~mud s~earthq~s,garchomp~mud s~earth p~sm,garchomp~mud s~earthq~s,landorus~mud s~sands~b,landorus~mud s~earth p~s,landorus~mud s~earthq~b,excadrill~mud-s~scor~s,rhyperior~mud-s~drill r~s,swampert~mud s~earthq~m");
        topByType.put("flying", "rayquaza~air s~dragon a~bm,moltres~wing~fly~bs,salamence~fire f~fly~sm,enamorus~fairy w~fly~b,charizard~air s~blas~m,yveltal~gust~obli~b,yveltal~gust~hurr~b,articuno~psycho c~fly~b,staraptor~wing~fly~b");
        topByType.put("psychic", "mewtwo~conf~psyst~m,mewtwo~psycho c~psyst~bs,mewtwo~psycho c~psychic~s,mewtwo~conf~psychic~m,alakazam~conf~psychic~m,alakazam~conf~futu~m,latios~zen h~psychic~sm,gardevoir~conf~psychic~m,gallade~conf~psychic~m,metagross~zen h~psychic~m");
        topByType.put("bug", "heracross~fury~megah~m,pinsir~fury~x-sc~sm,pinsir~bug bi~x-sc~sm,scizor~fury~x-sc~sm,vikavolt~bug bi~x-sc~s,volcarona~bug bi~bug bu~b,beedrill~bug bi~x-sc~m,escavalier~bug bi~megah~s,metagross~fury~meteor m~s");
        topByType.put("rock", "diancie~rock th~rock sl~m,rhyperior~smac~rock w~bs,rhyperior~smac~ston~s,tyranitar~smac~ston~sm,aerodactyl~rock th~rock sl~m,gigalith~lock~meteor b~s,gigalith~lock~rock sl~s,tyrantrum~rock th~meteor b~s,rampardos~smac~rock sl~s,rayquaza~dragon t~anci~m");
        topByType.put("ghost", "necrozma~psycho c~moong~b,necrozma~shadow c~moong~b,mewtwo~psycho c~shadow ba~sm,gengar~lick~shadow ba~m,gengar~shadow c~shadow ba~m,darkrai~snar~shadow ba~s,chandelure~hex~shadow ba~s,banette~shadow c~shadow ba~m,lunala~shadow c~shadow ba~b,kyurem~shadow c~free~b");
        topByType.put("dragon", "rayquaza~dragon t~brea~m,rayquaza~dragon t~outr~m,eternatus~dragon t~dynama~b,kyurem~dragon t~free~b,garchomp~dragon t~brea~sm,kyurem~dragon b~ice bu~b,salamence~dragon t~drac~m,haxorus~dragon t~brea~s,dialga~dragon b~drac~s,dragonite~dragon t~drac~m,dragonite~dragon b~drac~m,dragonite~dragon t~outr~m");
        topByType.put("dark", "tyranitar~bite~brut~sm,hydreigon~bite~brut~s,hydreigon~bite~dark p~s,darkrai~snar~shadow ba~bs,absol~snar~brut~sm,absol~snar~dark p~m,gengar~suck~shadow ba~m,houndoom~snar~foul~m,salamence~bite~brut~m");
        topByType.put("steel", "zacian~meta~behemoth bl~b,zamazenta~meta~behemoth ba~b,necrozma~meta~suns~b,metagross~bullet p~meteor m~bsm,lucario~forc~meteor m~m,dialga~meta~iron h~s,excadrill~meta~iron h~s,necrozma~meta~moong~b");
        topByType.put("fairy", "gardevoir~charm~dazz~sm,zacian~meta~play~b,enamorus~fairy w~dazz~b,alakazam~psycho c~dazz~m,tapu lele~asto~natu~b,xerneas~geom~moonb~b,tapu koko~quic~natu~b,tapu bulu~bullet s~natu~b,latias~charm~outr~m");
        budgetByType.put("normal", "");
        budgetByType.put("fire", "volcarona~fire s~over~b,chandelure~fire s~over~b,cinderace~fire s~blas~b");
        budgetByType.put("water", "inteleon~water g~hydro c~b,quaquaval~water g~hydro c~b,primarina~waterf~hydro c~b");
        budgetByType.put("electric", "electivire~thunder s~wild c~b,magnezone~volt~wild c~b,luxray~spar~wild c~b");
        budgetByType.put("grass", "rillaboom~razo~fren~b,meowscarada~leafa~fren~b,roserade~magi~grass k~b");
        budgetByType.put("ice", "baxcalibur~ice f~aval~b,mamoswine~powd~aval~b,darmanitan~ice f~aval~b");
        budgetByType.put("fighting", "lucario~forc~aura~b,blaziken~coun~aura~b,conkeldurr~forc~dynami~b");
        budgetByType.put("poison", "roserade~poison j~sludge b~b,overqwil~poison j~sludge b~b,revavroom~poison j~gunk~b");
        budgetByType.put("ground", "garchomp~mud s~earth p~b,excadrill~mud-s~scor~b,rhyperior~mud-s~earthq~b");
        budgetByType.put("flying", "salamence~fire f~fly~b,toucannon~peck~beak~b,staraptor~gust~fly~b");
        budgetByType.put("psychic", "metagross~zen h~psychic~b,espeon~conf~psychic~b,alakazam~conf~psychic~b");
        budgetByType.put("bug", "volcarona~bug bi~bug bu~b,vikavolt~bug bi~x-sc~b,kleavor~fury~x-sc~b");
        budgetByType.put("rock", "rhyperior~smac~rock w~b,glimmora~rock th~meteor b~b,rampardos~smac~rock sl~b");
        budgetByType.put("ghost", "gholdengo~hex~shadow ba~b,dragapult~asto~shadow ba~b,chandelure~hex~shadow ba~b");
        budgetByType.put("dragon", "baxcalibur~ice f~glai~b,haxorus~dragon t~brea~b,garchomp~dragon t~brea~b");
        budgetByType.put("dark", "hydreigon~bite~brut~b,tyranitar~bite~brut~b,kingambit~snar~foul~b");
        budgetByType.put("steel", "metagross~bullet p~meteor m~b,tinkaton~fairy w~gigat~b,lucario~forc~meteor m~b");
        budgetByType.put("fairy", "gardevoir~charm~dazz~b,togekiss~charm~dazz~b,hatterene~charm~dazz~b");
        learnExtra.put("absol", "|psycho c|megah|play|");
        learnExtra.put("aerodactyl", "|anci|earth p|iron h|bite|");
        learnExtra.put("alakazam", "|focu|shadow ba|");
        learnExtra.put("articuno", "|ice be|anci|");
        learnExtra.put("banette", "|hex|");
        learnExtra.put("baxcalibur", "|brea|dragon b|");
        learnExtra.put("beedrill", "|drill r|");
        learnExtra.put("blacephalon", "|over|shadow ba|asto|");
        learnExtra.put("blastoise", "|ice be|bite|");
        learnExtra.put("blaziken", "|focu|ston|");
        learnExtra.put("chandelure", "|inci|");
        learnExtra.put("charizard", "|dragon b|");
        learnExtra.put("chesnaught", "|smac|");
        learnExtra.put("cinderace", "|focu|");
        learnExtra.put("conkeldurr", "|focu|ston|");
        learnExtra.put("darkrai", "|focu|");
        learnExtra.put("darmanitan", "|over|fire f|inci|rock sl|");
        learnExtra.put("delphox", "|psychic|zen h|");
        learnExtra.put("diancie", "|moonb|dazz|");
        learnExtra.put("dragapult", "|dragon b|outr|hex|");
        learnExtra.put("electivire", "|thunderb|low k|");
        learnExtra.put("enamorus", "|asto|");
        learnExtra.put("escavalier", "|drill r|coun|");
        learnExtra.put("excadrill", "|earthq|drill r|rock sl|");
        learnExtra.put("feraligatr", "|waterf|ice f|");
        learnExtra.put("gallade", "|clos|leaf b|low k|charm|");
        learnExtra.put("garchomp", "|outr|");
        learnExtra.put("gardevoir", "|shadow ba|magi|");
        learnExtra.put("gengar", "|focu|hex|psychic|dazz|");
        learnExtra.put("gholdengo", "|focu|asto|dazz|");
        learnExtra.put("groudon", "|dragon t|");
        learnExtra.put("gyarados", "|bite|dragon b|outr|");
        learnExtra.put("hatterene", "|psychic|conf|");
        learnExtra.put("haxorus", "|earthq|coun|");
        learnExtra.put("heatran", "|iron h|earth p|ston|");
        learnExtra.put("heracross", "|earthq|");
        learnExtra.put("hydreigon", "|dragon b|");
        learnExtra.put("inteleon", "|hydro p|");
        learnExtra.put("keldeo", "|hydro p|");
        learnExtra.put("kingambit", "|iron h|meta|");
        learnExtra.put("kingler", "|meta|mud s|x-sc|");
        learnExtra.put("kleavor", "|rock sl|quic|");
        learnExtra.put("kyurem", "|drac|");
        learnExtra.put("landorus", "|rock sl|focu|");
        learnExtra.put("latias", "|psychic|dragon b|zen h|");
        learnExtra.put("latios", "|dragon b|");
        learnExtra.put("lopunny", "|focu|low k|");
        learnExtra.put("lucario", "|clos|shadow ba|");
        learnExtra.put("lunala", "|moong|psychic|air s|conf|");
        learnExtra.put("luxray", "|hidd|");
        learnExtra.put("magnezone", "|disc|spar|");
        learnExtra.put("mamoswine", "|anci|mud-s|");
        learnExtra.put("manectric", "|snar|over|charg|");
        learnExtra.put("meloetta", "|psychic|thunderb|dazz|");
        learnExtra.put("meowscarada", "|play|");
        learnExtra.put("metagross", "|earthq|");
        learnExtra.put("moltres", "|anci|");
        learnExtra.put("naganadel", "|air s|");
        learnExtra.put("nihilego", "|rock sl|acid|gunk|");
        learnExtra.put("pinsir", "|clos|");
        learnExtra.put("porygon-z", "|disc|");
        learnExtra.put("primarina", "|moonb|charm|psychic|");
        learnExtra.put("quaquaval", "|clos|");
        learnExtra.put("raikou", "|thunderb|volt|");
        learnExtra.put("rampardos", "|outr|");
        learnExtra.put("regigigas", "|focu|");
        learnExtra.put("reshiram", "|drac|dragon b|ston|");
        learnExtra.put("revavroom", "|over|");
        learnExtra.put("rillaboom", "|earth p|");
        learnExtra.put("roserade", "|dazz|razo|");
        learnExtra.put("salamence", "|outr|");
        learnExtra.put("samurott", "|fury|megah|");
        learnExtra.put("sceptile", "|earthq|bullet s|");
        learnExtra.put("scizor", "|iron h|bullet p|");
        learnExtra.put("scolipede", "|x-sc|megah|bug bi|");
        learnExtra.put("shaymin", "|zen h|");
        learnExtra.put("staraptor", "|clos|quic|");
        learnExtra.put("tangrowth", "|rock sl|sludge b|");
        learnExtra.put("tapu bulu", "|grass k|megah|");
        learnExtra.put("tapu koko", "|volt|dazz|thunderb|");
        learnExtra.put("tapu lele", "|conf|focu|");
        learnExtra.put("terrakion", "|rock sl|earthq|smac|");
        learnExtra.put("thundurus", "|focu|");
        learnExtra.put("tinkaton", "|play|");
        learnExtra.put("togekiss", "|anci|air s|");
        learnExtra.put("toucannon", "|bullet s|");
        learnExtra.put("tyrantrum", "|outr|earthq|dragon t|");
        learnExtra.put("venusaur", "|sludge b|razo|");
        learnExtra.put("victreebel", "|razo|");
        learnExtra.put("vikavolt", "|disc|spar|mud-s|");
        learnExtra.put("volcarona", "|psychic|");
        learnExtra.put("xerneas", "|clos|giga i|megah|");
        learnExtra.put("xurkitree", "|power w|");
        learnExtra.put("yveltal", "|snar|focu|suck|");
        learnExtra.put("zacian", "|iron h|quic|snar|fire f|clos|");
        learnExtra.put("zamazenta", "|clos|iron h|moonb|");
        learnExtra.put("zapdos", "|anci|");
        learnExtra.put("zarude", "|bite|");
        learnExtra.put("zekrom", "|outr|dragon b|");
        learnExtra.put("zeraora", "|clos|wild c|spar|");
        /* ===== END GENERATED topByType ===== */

        /* Pattern to match @#keyword. or @#keyword[number] -- keyword may
         * contain hyphens for the raid-... family. */
        Pattern pattern = Pattern.compile("@#([\\w-]+)(?:\\[(\\d+)\\]|\\.)");
        Matcher matcher = pattern.matcher(currentText);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String keyword = matcher.group(1).toLowerCase();
            String paramStr = matcher.group(2);

            /* Process parameter if present. DAYS for the static templates,
             * CP floor for the raid family. */
            int param = 0;
            if (paramStr != null) {
                try {
                    param = Integer.parseInt(paramStr);
                } catch (NumberFormatException e) {
                    param = 0;
                }
            }

            /* Get template for this keyword. */
            String template = (String) templates.get(keyword);

            /* Raid command family (strictness ladder, loosest to exact):
             *   attkr-T [CP]   every vouched T-type attacker (any build that
             *                  still carries a T move) -- optimal AND budget
             *   raid-T / raid-T1-T2 [CP]  balanced counters vs a boss:
             *                  (species)&(optimal fasts)&(optimal chargeds)
             *                  across all weakness buckets
             *   atk-T [CP]     one bucket, same three-clause shape
             *   raidn-T / raidn-T1-T2 [N]  the Nth-ranked counter as an
             *                  EXACT query (species&@fast&@charged) -- the
             *                  only shape that truly binds one species to
             *                  its optimal moveset; [N] defaults to 1 */
            boolean isRaid = keyword.startsWith("raid-");
            boolean isRaidN = keyword.startsWith("raidn-");
            boolean isAtk = keyword.startsWith("atk-");
            boolean isAttkr = keyword.startsWith("attkr-");
            if (template == null && (isRaid || isRaidN || isAtk || isAttkr)) {
                String raidExpansion = null;
                String typePart;
                if (isRaid) {
                    typePart = keyword.substring(5);
                } else if (isAtk) {
                    typePart = keyword.substring(4);
                } else {
                    typePart = keyword.substring(6);
                }
                String[] bossTypes;
                if (isAtk || isAttkr) {
                    bossTypes = new String[0];
                } else {
                    bossTypes = typePart.split("-");
                }
                boolean typesOk;
                if (isAtk || isAttkr) {
                    typesOk = (weakTo.get(typePart) != null);
                } else {
                    typesOk = (bossTypes.length == 1 || bossTypes.length == 2);
                    int bi = 0;
                    while (typesOk && bi < bossTypes.length) {
                        if (weakTo.get(bossTypes[bi]) == null) {
                            typesOk = false;
                        }
                        bi = bi + 1;
                    }
                }

                if (typesOk) {
                    /* Determine the attack-type buckets to draw from. */
                    ArrayList seTypes = new ArrayList();
                    if (isAtk || isAttkr) {
                        seTypes.add(typePart);
                    } else if (bossTypes.length == 1) {
                        String[] w = ((String) weakTo.get(bossTypes[0])).split(" ");
                        int wi = 0;
                        while (wi < w.length) {
                            seTypes.add(w[wi]);
                            wi = wi + 1;
                        }
                    } else {
                        /* Dual-type: keep attack types whose product over both
                         * halves stays >= 1.5 (net SE); double-SE (>=2.5) first. */
                        ArrayList doubleSe = new ArrayList();
                        ArrayList singleSe = new ArrayList();
                        int ai = 0;
                        while (ai < ALL_TYPES.length) {
                            String atk = ALL_TYPES[ai];
                            String pad = " " + atk + " ";
                            double product = 1.0;
                            int di = 0;
                            while (di < bossTypes.length) {
                                String def = bossTypes[di];
                                String imm = (String) immuneTo.get(def);
                                String wk = (String) weakTo.get(def);
                                String rs = (String) resistsFrom.get(def);
                                if (imm != null && (" " + imm + " ").indexOf(pad) >= 0) {
                                    product = product * 0.390625;
                                } else if (wk != null && (" " + wk + " ").indexOf(pad) >= 0) {
                                    product = product * 1.6;
                                } else if (rs != null && (" " + rs + " ").indexOf(pad) >= 0) {
                                    product = product * 0.625;
                                }
                                di = di + 1;
                            }
                            if (product >= 2.5) {
                                doubleSe.add(atk);
                            } else if (product >= 1.5) {
                                singleSe.add(atk);
                            }
                            ai = ai + 1;
                        }
                        seTypes.addAll(doubleSe);
                        seTypes.addAll(singleSe);
                    }

                    if (!seTypes.isEmpty()) {
                        /* Collect curated entries (species~fast~charged),
                         * interleaving round by round across the SE buckets
                         * and, within each round, best tier then budget tier.
                         * This guarantees budget counters early admission
                         * instead of queueing behind the whole best list. */
                        ArrayList bestArrs = new ArrayList();
                        ArrayList budgetArrs = new ArrayList();
                        int maxRounds = 0;
                        int si = 0;
                        while (si < seTypes.size()) {
                            String seType = (String) seTypes.get(si);
                            String b1 = (String) topByType.get(seType);
                            String b2 = (String) budgetByType.get(seType);
                            String[] a1 = (b1 == null || b1.length() == 0) ? new String[0] : b1.split(",");
                            String[] a2 = (b2 == null || b2.length() == 0) ? new String[0] : b2.split(",");
                            bestArrs.add(a1);
                            budgetArrs.add(a2);
                            if (a1.length > maxRounds) { maxRounds = a1.length; }
                            if (a2.length > maxRounds) { maxRounds = a2.length; }
                            si = si + 1;
                        }
                        ArrayList entryList = new ArrayList();
                        Map seenEntry = new HashMap();
                        int ri = 0;
                        while (ri < maxRounds) {
                            si = 0;
                            while (si < seTypes.size()) {
                                String[] a1 = (String[]) bestArrs.get(si);
                                String[] a2 = (String[]) budgetArrs.get(si);
                                if (ri < a1.length && seenEntry.get(a1[ri]) == null) {
                                    seenEntry.put(a1[ri], Boolean.TRUE);
                                    entryList.add(a1[ri]);
                                }
                                if (ri < a2.length && seenEntry.get(a2[ri]) == null) {
                                    seenEntry.put(a2[ri], Boolean.TRUE);
                                    entryList.add(a2[ri]);
                                }
                                si = si + 1;
                            }
                            ri = ri + 1;
                        }

                        String cpClause = (param > 0) ? ("&cp" + param + "-") : "";

                        /* Tail for the dormant AND-binds-tighter paths only:
                         * CP clause if given, else the always-true cp1-. */
                        String groupTail = (param > 0) ? cpClause : "&cp1-";

                        if (isRaidN) {
                            /* Exact query for the Nth-ranked counter. The
                             * bracket parameter is the INDEX here, not CP;
                             * an out-of-range index leaves the shortcut
                             * untouched so the mistake is visible. */
                            int idx = (param > 0) ? param : 1;
                            if (idx <= entryList.size()) {
                                String[] nparts = ((String) entryList.get(idx - 1)).split("~");
                                raidExpansion = nparts[0] + "&@" + nparts[1] + "&@" + nparts[2];
                                String ntag = (nparts.length > 3) ? nparts[3] : "b";
                                if (ntag.indexOf("b") < 0) {
                                    /* Vouched only in boosted form(s):
                                     * shadow and/or mega-ready-or-mega'd. */
                                    String nf = "";
                                    if (ntag.indexOf("s") >= 0) { nf = nf + ",shadow"; }
                                    if (ntag.indexOf("m") >= 0) { nf = nf + ",megaevolve,mega"; }
                                    if (nf.length() > 0) {
                                        raidExpansion = raidExpansion + "&" + nf.substring(1);
                                    }
                                }
                            }
                        } else if (isAttkr) {
                            /* Every vouched species of this type, any build
                             * that still carries a T-type move. Trim species
                             * from the end if the list overflows the field. */
                            ArrayList vSpecs = new ArrayList();
                            int vi = 0;
                            while (vi < entryList.size()) {
                                String[] vparts = ((String) entryList.get(vi)).split("~");
                                if (!vSpecs.contains(vparts[0])) {
                                    vSpecs.add(vparts[0]);
                                }
                                vi = vi + 1;
                            }
                            if (AND_BINDS_TIGHTER) {
                                /* Per-species groups: s&@T[&cp],... so the
                                 * T-move requirement binds to each species. */
                                StringBuffer vSb = new StringBuffer();
                                int vk = 0;
                                while (vk < vSpecs.size()) {
                                    String vGroup = (String) vSpecs.get(vk) + "&@" + typePart + groupTail;
                                    int vExtra = vGroup.length() + ((vSb.length() > 0) ? 1 : 0);
                                    if (vSb.length() + vExtra <= MAX_SEARCH_LEN) {
                                        if (vSb.length() > 0) { vSb.append(","); }
                                        vSb.append(vGroup);
                                    }
                                    vk = vk + 1;
                                }
                                if (vSb.length() > 0) {
                                    raidExpansion = vSb.toString();
                                } else {
                                    raidExpansion = "@" + typePart + groupTail;
                                }
                            } else {
                                String vSuffix = "&@" + typePart + cpClause;
                                while (raidExpansion == null) {
                                    if (vSpecs.isEmpty()) {
                                        raidExpansion = "@" + typePart + cpClause;
                                    } else {
                                        StringBuffer vSb = new StringBuffer();
                                        int vk = 0;
                                        while (vk < vSpecs.size()) {
                                            if (vk > 0) { vSb.append(","); }
                                            vSb.append((String) vSpecs.get(vk));
                                            vk = vk + 1;
                                        }
                                        String vCand = vSb.toString() + vSuffix;
                                        if (vCand.length() <= MAX_SEARCH_LEN) {
                                            raidExpansion = vCand;
                                        } else {
                                            vSpecs.remove(vSpecs.size() - 1);
                                        }
                                    }
                                }
                            }
                        } else if (AND_BINDS_TIGHTER) {
                            /* Exact per-species AND-groups (OR of ANDs):
                             * species&@fast&@charged[&cp],... Every group
                             * pins one species to its optimal moveset. */
                            StringBuffer gSb = new StringBuffer();
                            int gi = 0;
                            while (gi < entryList.size()) {
                                String[] gparts = ((String) entryList.get(gi)).split("~");
                                String group = gparts[0] + "&@" + gparts[1] + "&@" + gparts[2] + groupTail;
                                int gExtra = group.length() + ((gSb.length() > 0) ? 1 : 0);
                                if (gSb.length() + gExtra <= MAX_SEARCH_LEN) {
                                    if (gSb.length() > 0) { gSb.append(","); }
                                    gSb.append(group);
                                }
                                gi = gi + 1;
                            }
                            if (gSb.length() > 0) {
                                raidExpansion = gSb.toString();
                            } else {
                                /* No curated data: per-type fallback groups
                                 * (tail repeated per group so it binds and
                                 * no @ term precedes a comma). */
                                StringBuffer typeSb = new StringBuffer();
                                si = 0;
                                while (si < seTypes.size()) {
                                    if (si > 0) { typeSb.append(","); }
                                    typeSb.append("@").append((String) seTypes.get(si)).append(groupTail);
                                    si = si + 1;
                                }
                                raidExpansion = typeSb.toString();
                            }
                        } else if (EXACT_PINNING) {
                            /* Implication encoding: a species OR-clause,
                             * then per species "!s,@fasts" / "!s,@chargeds"
                             * clauses (and "!s,shadow" when only the shadow
                             * form is vouched). Logically identical to the
                             * OR of per-species AND-groups, because species
                             * terms are mutually exclusive. */
                            ArrayList spOrder = new ArrayList();
                            Map spFasts = new HashMap();
                            Map spChargeds = new HashMap();
                            Map spBase = new HashMap();
                            Map spShadow = new HashMap();
                            Map spMega = new HashMap();
                            String assembled = null;
                            boolean[] used = new boolean[entryList.size()];
                            boolean progress = true;
                            /* Multi-pass: later rows can shrink the string
                             * (a base row deletes a form clause), so retry
                             * skipped entries until a pass admits nothing. */
                            while (progress) {
                            progress = false;
                            int ni = 0;
                            while (ni < entryList.size()) {
                                if (used[ni]) {
                                    ni = ni + 1;
                                    continue;
                                }
                                String[] parts = ((String) entryList.get(ni)).split("~");
                                String sp = parts[0];
                                String tag = (parts.length > 3) ? parts[3] : "b";
                                boolean isNew = (spFasts.get(sp) == null);
                                boolean addedF = false;
                                boolean addedC = false;
                                Boolean prevBase = (Boolean) spBase.get(sp);
                                Boolean prevShadow = (Boolean) spShadow.get(sp);
                                Boolean prevMega = (Boolean) spMega.get(sp);
                                if (isNew) {
                                    spOrder.add(sp);
                                    spFasts.put(sp, new ArrayList());
                                    spChargeds.put(sp, new ArrayList());
                                    spBase.put(sp, Boolean.FALSE);
                                    spShadow.put(sp, Boolean.FALSE);
                                    spMega.put(sp, Boolean.FALSE);
                                }
                                if (tag.indexOf("b") >= 0) { spBase.put(sp, Boolean.TRUE); }
                                if (tag.indexOf("s") >= 0) { spShadow.put(sp, Boolean.TRUE); }
                                if (tag.indexOf("m") >= 0) { spMega.put(sp, Boolean.TRUE); }
                                ArrayList fl = (ArrayList) spFasts.get(sp);
                                ArrayList cl = (ArrayList) spChargeds.get(sp);
                                if (!fl.contains(parts[1])) { fl.add(parts[1]); addedF = true; }
                                if (!cl.contains(parts[2])) { cl.add(parts[2]); addedC = true; }

                                /* Build the candidate string: species clause,
                                 * global fast + charged OR-clauses, then a
                                 * per-species implication clause ONLY where
                                 * that species can actually LEARN a pool move
                                 * it is not vouched with (learnExtra) -- the
                                 * global clauses already pin everyone else,
                                 * since they physically can't satisfy other
                                 * species' moves. */
                                ArrayList gFasts = new ArrayList();
                                ArrayList gChargeds = new ArrayList();
                                int xk = 0;
                                while (xk < spOrder.size()) {
                                    String xs = (String) spOrder.get(xk);
                                    ArrayList xf = (ArrayList) spFasts.get(xs);
                                    ArrayList xc = (ArrayList) spChargeds.get(xs);
                                    int xm = 0;
                                    while (xm < xf.size()) {
                                        if (!gFasts.contains(xf.get(xm))) { gFasts.add(xf.get(xm)); }
                                        xm = xm + 1;
                                    }
                                    xm = 0;
                                    while (xm < xc.size()) {
                                        if (!gChargeds.contains(xc.get(xm))) { gChargeds.add(xc.get(xm)); }
                                        xm = xm + 1;
                                    }
                                    xk = xk + 1;
                                }
                                StringBuffer xb = new StringBuffer();
                                xk = 0;
                                while (xk < spOrder.size()) {
                                    if (xk > 0) { xb.append(","); }
                                    xb.append((String) spOrder.get(xk));
                                    xk = xk + 1;
                                }
                                xb.append("&");
                                xk = 0;
                                while (xk < gFasts.size()) {
                                    if (xk > 0) { xb.append(","); }
                                    xb.append("@").append((String) gFasts.get(xk));
                                    xk = xk + 1;
                                }
                                xb.append("&");
                                xk = 0;
                                while (xk < gChargeds.size()) {
                                    if (xk > 0) { xb.append(","); }
                                    xb.append("@").append((String) gChargeds.get(xk));
                                    xk = xk + 1;
                                }
                                xk = 0;
                                while (xk < spOrder.size()) {
                                    String xs = (String) spOrder.get(xk);
                                    String extras = (String) learnExtra.get(xs);
                                    if (extras == null) { extras = ""; }
                                    ArrayList xf = (ArrayList) spFasts.get(xs);
                                    ArrayList xc = (ArrayList) spChargeds.get(xs);
                                    boolean confF = false;
                                    int xm = 0;
                                    while (xm < gFasts.size()) {
                                        if (extras.indexOf("|" + (String) gFasts.get(xm) + "|") >= 0) { confF = true; }
                                        xm = xm + 1;
                                    }
                                    boolean confC = false;
                                    xm = 0;
                                    while (xm < gChargeds.size()) {
                                        if (extras.indexOf("|" + (String) gChargeds.get(xm) + "|") >= 0) { confC = true; }
                                        xm = xm + 1;
                                    }
                                    if (confF) {
                                        xb.append("&!").append(xs);
                                        xm = 0;
                                        while (xm < xf.size()) {
                                            xb.append(",@").append((String) xf.get(xm));
                                            xm = xm + 1;
                                        }
                                    }
                                    if (confC) {
                                        xb.append("&!").append(xs);
                                        xm = 0;
                                        while (xm < xc.size()) {
                                            xb.append(",@").append((String) xc.get(xm));
                                            xm = xm + 1;
                                        }
                                    }
                                    boolean xBase = ((Boolean) spBase.get(xs)).booleanValue();
                                    boolean xShadow = ((Boolean) spShadow.get(xs)).booleanValue();
                                    boolean xMega = ((Boolean) spMega.get(xs)).booleanValue();
                                    if (!xBase && (xShadow || xMega)) {
                                        /* Vouched only in boosted form(s).
                                         * megaevolve = CAN mega (Niantic,
                                         * energy-aware); mega = currently
                                         * mega-evolved -- need both. */
                                        xb.append("&!").append(xs);
                                        if (xShadow) { xb.append(",shadow"); }
                                        if (xMega) { xb.append(",megaevolve,mega"); }
                                    }
                                    xk = xk + 1;
                                }
                                String candidate = xb.toString() + cpClause;
                                if (candidate.length() <= MAX_SEARCH_LEN) {
                                    assembled = candidate;
                                    used[ni] = true;
                                    progress = true;
                                } else {
                                    /* Roll back this entry; it may fit on a
                                     * later pass once the string shrinks. */
                                    if (addedF) { fl.remove(fl.size() - 1); }
                                    if (addedC) { cl.remove(cl.size() - 1); }
                                    if (isNew) {
                                        spOrder.remove(spOrder.size() - 1);
                                        spFasts.remove(sp);
                                        spChargeds.remove(sp);
                                        spBase.remove(sp);
                                        spShadow.remove(sp);
                                        spMega.remove(sp);
                                    } else {
                                        spBase.put(sp, prevBase);
                                        spShadow.put(sp, prevShadow);
                                        spMega.put(sp, prevMega);
                                    }
                                }
                                ni = ni + 1;
                            }
                            }
                            if (assembled == null) {
                                /* No curated data: move-TYPE fallback. */
                                StringBuffer typeSb = new StringBuffer();
                                si = 0;
                                while (si < seTypes.size()) {
                                    if (si > 0) { typeSb.append(","); }
                                    typeSb.append("@").append((String) seTypes.get(si));
                                    si = si + 1;
                                }
                                raidExpansion = typeSb.toString() + cpClause;
                            } else {
                                raidExpansion = assembled;
                            }
                        } else {

                        /* Greedily admit entries in rank order. Each entry
                         * contributes its species to the species OR-clause,
                         * its fast move to the fast OR-clause and its charged
                         * move to the charged OR-clause; all three clauses
                         * are ANDed. A match is therefore always a curated
                         * species carrying curated fast AND charged moves --
                         * the strictest guarantee the search grammar allows
                         * (true per-species move pinning is an OR-of-ANDs,
                         * which comma-binds-tighter + no parens cannot say).
                         * Stop before the string overflows the field. */
                        ArrayList specs = new ArrayList();
                        ArrayList fasts = new ArrayList();
                        ArrayList chargeds = new ArrayList();
                        String assembled = null;
                        int ni = 0;
                        while (ni < entryList.size()) {
                            String[] parts = ((String) entryList.get(ni)).split("~");
                            boolean addSpec = !specs.contains(parts[0]);
                            boolean addFast = !fasts.contains(parts[1]);
                            boolean addCharged = !chargeds.contains(parts[2]);
                            if (addSpec) { specs.add(parts[0]); }
                            if (addFast) { fasts.add(parts[1]); }
                            if (addCharged) { chargeds.add(parts[2]); }
                            StringBuffer sb = new StringBuffer();
                            int ki = 0;
                            while (ki < specs.size()) {
                                if (ki > 0) { sb.append(","); }
                                sb.append((String) specs.get(ki));
                                ki = ki + 1;
                            }
                            sb.append("&");
                            ki = 0;
                            while (ki < fasts.size()) {
                                if (ki > 0) { sb.append(","); }
                                sb.append("@").append((String) fasts.get(ki));
                                ki = ki + 1;
                            }
                            sb.append("&");
                            ki = 0;
                            while (ki < chargeds.size()) {
                                if (ki > 0) { sb.append(","); }
                                sb.append("@").append((String) chargeds.get(ki));
                                ki = ki + 1;
                            }
                            String candidate = sb.toString() + cpClause;
                            if (candidate.length() <= MAX_SEARCH_LEN) {
                                assembled = candidate;
                            } else {
                                /* Roll back this entry but keep scanning:
                                 * a later entry whose moves are already in
                                 * the clauses may still fit (gap-fill). */
                                if (addSpec) { specs.remove(specs.size() - 1); }
                                if (addFast) { fasts.remove(fasts.size() - 1); }
                                if (addCharged) { chargeds.remove(chargeds.size() - 1); }
                            }
                            ni = ni + 1;
                        }

                        if (assembled == null) {
                            /* No curated data: fall back to move-TYPE filter. */
                            StringBuffer typeSb = new StringBuffer();
                            si = 0;
                            while (si < seTypes.size()) {
                                if (si > 0) { typeSb.append(","); }
                                typeSb.append("@").append((String) seTypes.get(si));
                                si = si + 1;
                            }
                            raidExpansion = typeSb.toString() + cpClause;
                        } else {
                            raidExpansion = assembled;
                        }

                        }
                    }
                }

                if (raidExpansion != null) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(raidExpansion));
                    continue;
                }
                /* Invalid type spec: fall through and leave it as-is. */
            }

            if (template == null) {
                /* Unknown keyword, leave it as-is. */
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            /* Expand template with DAYS parameter. */
            String expanded = template;
            Pattern daysPattern = Pattern.compile("\\{DAYS:(\\d+)\\}");
            Matcher daysMatcher = daysPattern.matcher(template);

            if (daysMatcher.find()) {
                int defaultValue = Integer.parseInt(daysMatcher.group(1));
                int effectiveDays = (param > 0) ? param : defaultValue;

                String replacement;
                if (effectiveDays == 0) {
                    replacement = "age0";
                } else {
                    int upperBound = effectiveDays - 1;
                    replacement = "age0-" + upperBound;
                }

                expanded = daysMatcher.replaceAll(Matcher.quoteReplacement(replacement));
            }

            /* Replace the match with expanded version. */
            matcher.appendReplacement(result, Matcher.quoteReplacement(expanded));
        }

        matcher.appendTail(result);
        String newText = result.toString();

        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText);

        boolean success = source.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

        if (success) {
            tasker.log("Text Expander: Expanded PoGo search shortcuts");
        } else {
            tasker.log("Text Expander: ACTION_SET_TEXT failed (stale node?)");
        }
    }
};

/* Trigger fires on . or ] after @# pattern */
replacementMap.put("@#", pogoSearchReplacer);
tasker.logAndToast("Added PoGo Search Expander module (raid counters).");
