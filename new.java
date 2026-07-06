/*
 * PoGo Search Text Expander -- Tasker BeanShell module (PRECOMPILED).
 *
 * Command family (strictness ladder):
 *   @#attkr-fairy.          every vouched fairy attacker (loose view)
 *   @#raid-fire.            balanced counters for a single-type boss
 *   @#raid-dragon-steel.    same for a dual-type boss (chart-aware)
 *   @#atk-fairy.            ONE attack-type bucket, exact optimal builds
 *   @#raidn-dragon-steel[2] EXACT query for the 2nd-ranked counter
 *   [CP] on attkr/raid/atk = CP floor, e.g. @#raid-fire[2500]
 *
 * ALL packing logic (type-chart cancellation, tier interleave, implication
 * clauses, learnability guards, form clauses, legacy fallbacks, prefix
 * shortening, 200-char budgeting) runs at BUILD time in transcode.py. The
 * generated block below holds the final search string per command key:
 *   expansions  command key -> search string (fits the field)
 *   cpx         repacked variant with room for a runtime &cpN- clause
 *               (only where it differs from the full string)
 *   seTypes     canonical type-part -> SE attack types (raidn- only)
 *   topByType / budgetByType  species~fast~charged~formtags entries
 *               per attack type (raidn- only)
 * This file is regenerated with: python3 transcode.py --apply new.java
 * Runtime logic here is just: template DAYS expansion, map lookup with CP
 * append (clamped to 9999 to fit the reserved room), and the raidn- Nth
 * pick. Dual-type keys are canonicalized by lexicographic sort, so
 * raid-steel-dragon finds raid-dragon-steel's string.
 *
 * Search grammar facts these strings rely on (verified on device 2026-07):
 * ','/':'/';' are OR, '&'/'|' are AND, OR evaluates before AND, no
 * parentheses, '!' negates one term, @move matches by name PREFIX, and
 * moves with apostrophes use the typographic U+2019 in-game.
 *
 * All original static templates and the {DAYS:n} logic are unchanged.
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

        /* Precompiled data (machine-written; do not edit by hand). */
        Map expansions = new HashMap();
        Map cpx = new HashMap();
        Map seTypes = new HashMap();
        Map topByType = new HashMap();
        Map budgetByType = new HashMap();
        /* ===== BEGIN GENERATED topByType (transcode.py) ===== */
        /* PRECOMPILED by transcode.py -- the packing algorithm runs at
         * build time. expansions = final search string per command key;
         * cpx = repacked variant leaving room for a runtime &cpN- clause
         * (present only where it differs). seTypes + topByType/budgetByType
         * (species~fast~charged~formtags) feed the tiny raidn- runtime. */
        seTypes.put("normal", "fighting");
        seTypes.put("fire", "water ground rock");
        seTypes.put("water", "electric grass");
        seTypes.put("electric", "ground");
        seTypes.put("grass", "fire ice poison flying bug");
        seTypes.put("ice", "fire fighting rock steel");
        seTypes.put("fighting", "flying psychic fairy");
        seTypes.put("poison", "ground psychic");
        seTypes.put("ground", "water grass ice");
        seTypes.put("flying", "electric ice rock");
        seTypes.put("psychic", "bug ghost dark");
        seTypes.put("bug", "fire flying rock");
        seTypes.put("rock", "water grass fighting ground steel");
        seTypes.put("ghost", "ghost dark");
        seTypes.put("dragon", "ice dragon fairy");
        seTypes.put("dark", "fighting bug fairy");
        seTypes.put("steel", "fire fighting ground");
        seTypes.put("fairy", "poison steel");
        seTypes.put("bug-dark", "fire flying bug rock fairy");
        seTypes.put("bug-dragon", "ice flying rock dragon fairy");
        seTypes.put("bug-electric", "fire rock");
        seTypes.put("bug-fairy", "fire poison flying rock steel");
        seTypes.put("bug-fighting", "flying fire psychic fairy");
        seTypes.put("bug-fire", "rock water flying");
        seTypes.put("bug-flying", "rock fire electric ice flying");
        seTypes.put("bug-ghost", "fire flying rock ghost dark");
        seTypes.put("bug-grass", "fire flying ice poison bug rock");
        seTypes.put("bug-ground", "fire water ice flying");
        seTypes.put("bug-ice", "fire rock flying steel");
        seTypes.put("bug-normal", "fire flying rock");
        seTypes.put("bug-poison", "fire flying psychic rock");
        seTypes.put("bug-psychic", "fire flying bug rock ghost dark");
        seTypes.put("bug-rock", "water rock steel");
        seTypes.put("bug-steel", "fire");
        seTypes.put("bug-water", "electric flying rock");
        seTypes.put("dark-dragon", "fairy ice fighting bug dragon");
        seTypes.put("dark-electric", "fighting ground bug fairy");
        seTypes.put("dark-fairy", "poison steel fairy");
        seTypes.put("dark-fighting", "fairy fighting flying");
        seTypes.put("dark-fire", "water fighting ground rock");
        seTypes.put("dark-flying", "electric ice rock fairy");
        seTypes.put("dark-ghost", "fairy");
        seTypes.put("dark-grass", "bug fire ice fighting poison flying fairy");
        seTypes.put("dark-ground", "water grass ice fighting bug fairy");
        seTypes.put("dark-ice", "fighting fire bug rock steel fairy");
        seTypes.put("dark-normal", "fighting bug fairy");
        seTypes.put("dark-poison", "ground");
        seTypes.put("dark-psychic", "bug fairy");
        seTypes.put("dark-rock", "fighting water grass ground bug steel fairy");
        seTypes.put("dark-steel", "fighting fire ground");
        seTypes.put("dark-water", "electric grass fighting bug fairy");
        seTypes.put("dragon-electric", "ice ground dragon fairy");
        seTypes.put("dragon-fairy", "ice poison steel fairy");
        seTypes.put("dragon-fighting", "fairy ice flying psychic dragon");
        seTypes.put("dragon-fire", "ground rock dragon");
        seTypes.put("dragon-flying", "ice rock dragon fairy");
        seTypes.put("dragon-ghost", "ice ghost dragon dark fairy");
        seTypes.put("dragon-grass", "ice poison flying bug dragon fairy");
        seTypes.put("dragon-ground", "ice dragon fairy");
        seTypes.put("dragon-ice", "fighting rock dragon steel fairy");
        seTypes.put("dragon-normal", "ice fighting dragon fairy");
        seTypes.put("dragon-poison", "ice ground psychic dragon");
        seTypes.put("dragon-psychic", "ice bug ghost dragon dark fairy");
        seTypes.put("dragon-rock", "ice fighting ground dragon steel fairy");
        seTypes.put("dragon-steel", "fighting ground");
        seTypes.put("dragon-water", "dragon fairy");
        seTypes.put("electric-fairy", "poison ground");
        seTypes.put("electric-fighting", "ground psychic fairy");
        seTypes.put("electric-fire", "ground water rock");
        seTypes.put("electric-flying", "ice rock");
        seTypes.put("electric-ghost", "ground ghost dark");
        seTypes.put("electric-grass", "fire ice poison bug");
        seTypes.put("electric-ground", "water grass ice ground");
        seTypes.put("electric-ice", "fire fighting ground rock");
        seTypes.put("electric-normal", "fighting ground");
        seTypes.put("electric-poison", "ground psychic");
        seTypes.put("electric-psychic", "ground bug ghost dark");
        seTypes.put("electric-rock", "ground water grass fighting");
        seTypes.put("electric-steel", "ground fire fighting");
        seTypes.put("electric-water", "grass ground");
        seTypes.put("fairy-fighting", "poison flying psychic steel fairy");
        seTypes.put("fairy-fire", "water poison ground rock");
        seTypes.put("fairy-flying", "electric ice poison rock steel");
        seTypes.put("fairy-ghost", "ghost steel");
        seTypes.put("fairy-grass", "poison fire ice flying steel");
        seTypes.put("fairy-ground", "water grass ice steel");
        seTypes.put("fairy-ice", "steel fire poison rock");
        seTypes.put("fairy-normal", "poison steel");
        seTypes.put("fairy-poison", "ground psychic steel");
        seTypes.put("fairy-psychic", "poison ghost steel");
        seTypes.put("fairy-rock", "steel water grass ground");
        seTypes.put("fairy-steel", "fire ground");
        seTypes.put("fairy-water", "electric grass poison");
        seTypes.put("fighting-fire", "water ground flying psychic");
        seTypes.put("fighting-flying", "electric ice flying psychic fairy");
        seTypes.put("fighting-ghost", "flying psychic ghost fairy");
        seTypes.put("fighting-grass", "flying fire ice poison psychic fairy");
        seTypes.put("fighting-ground", "water grass ice flying psychic fairy");
        seTypes.put("fighting-ice", "fire fighting flying psychic steel fairy");
        seTypes.put("fighting-normal", "fighting flying psychic fairy");
        seTypes.put("fighting-poison", "psychic ground flying");
        seTypes.put("fighting-psychic", "flying ghost fairy");
        seTypes.put("fighting-rock", "water grass fighting ground psychic steel fairy");
        seTypes.put("fighting-steel", "fire fighting ground");
        seTypes.put("fighting-water", "electric grass flying psychic fairy");
        seTypes.put("fire-flying", "rock water electric");
        seTypes.put("fire-ghost", "water ground rock ghost dark");
        seTypes.put("fire-grass", "poison flying rock");
        seTypes.put("fire-ground", "water ground");
        seTypes.put("fire-ice", "rock water fighting ground");
        seTypes.put("fire-normal", "water fighting ground rock");
        seTypes.put("fire-poison", "ground water psychic rock");
        seTypes.put("fire-psychic", "water ground rock ghost dark");
        seTypes.put("fire-rock", "water ground fighting rock");
        seTypes.put("fire-steel", "ground water fighting");
        seTypes.put("fire-water", "electric ground rock");
        seTypes.put("flying-ghost", "electric ice rock ghost dark");
        seTypes.put("flying-grass", "ice fire poison flying rock");
        seTypes.put("flying-ground", "ice water");
        seTypes.put("flying-ice", "rock fire electric steel");
        seTypes.put("flying-normal", "electric ice rock");
        seTypes.put("flying-poison", "electric ice psychic rock");
        seTypes.put("flying-psychic", "electric ice rock ghost dark");
        seTypes.put("flying-rock", "water electric ice rock steel");
        seTypes.put("flying-steel", "fire electric");
        seTypes.put("flying-water", "electric rock");
        seTypes.put("ghost-grass", "fire ice flying ghost dark");
        seTypes.put("ghost-ground", "water grass ice ghost dark");
        seTypes.put("ghost-ice", "fire rock ghost dark steel");
        seTypes.put("ghost-normal", "dark");
        seTypes.put("ghost-poison", "ground psychic ghost dark");
        seTypes.put("ghost-psychic", "ghost dark");
        seTypes.put("ghost-rock", "water grass ground ghost dark steel");
        seTypes.put("ghost-steel", "fire ground ghost dark");
        seTypes.put("ghost-water", "electric grass ghost dark");
        seTypes.put("grass-ground", "ice fire flying bug");
        seTypes.put("grass-ice", "fire fighting poison flying bug rock steel");
        seTypes.put("grass-normal", "fire ice fighting poison flying bug");
        seTypes.put("grass-poison", "fire ice flying psychic");
        seTypes.put("grass-psychic", "bug fire ice poison flying ghost dark");
        seTypes.put("grass-rock", "ice fighting bug steel");
        seTypes.put("grass-steel", "fire fighting");
        seTypes.put("grass-water", "poison flying bug");
        seTypes.put("ground-ice", "fire water grass fighting steel");
        seTypes.put("ground-normal", "water grass ice fighting");
        seTypes.put("ground-poison", "water ice ground psychic");
        seTypes.put("ground-psychic", "water grass ice bug ghost dark");
        seTypes.put("ground-rock", "water grass ice fighting ground steel");
        seTypes.put("ground-steel", "fire water fighting ground");
        seTypes.put("ground-water", "grass");
        seTypes.put("ice-normal", "fighting fire rock steel");
        seTypes.put("ice-poison", "fire ground psychic rock steel");
        seTypes.put("ice-psychic", "fire bug rock ghost dark steel");
        seTypes.put("ice-rock", "fighting steel water grass ground rock");
        seTypes.put("ice-steel", "fire fighting ground");
        seTypes.put("ice-water", "electric grass fighting rock");
        seTypes.put("normal-poison", "ground psychic");
        seTypes.put("normal-psychic", "bug dark");
        seTypes.put("normal-rock", "fighting water grass ground steel");
        seTypes.put("normal-steel", "fighting fire ground");
        seTypes.put("normal-water", "electric grass fighting");
        seTypes.put("poison-psychic", "ground ghost dark");
        seTypes.put("poison-rock", "ground water psychic steel");
        seTypes.put("poison-steel", "ground fire");
        seTypes.put("poison-water", "electric ground psychic");
        seTypes.put("psychic-rock", "water grass ground bug ghost dark steel");
        seTypes.put("psychic-steel", "fire ground ghost dark");
        seTypes.put("psychic-water", "electric grass bug ghost dark");
        seTypes.put("rock-steel", "fighting ground water");
        seTypes.put("rock-water", "grass electric fighting ground");
        seTypes.put("steel-water", "electric fighting ground");
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
        expansions.put("raid-normal", "lucario,blaziken,conkeldurr,keldeo,mewtwo,terrakion&@forc,@coun,@low k,@psycho c,@double k&@aura,@dynami,@secr,@focu,@sacred s&!blaziken,@aura&!conkeldurr,@dynami&!mewtwo,megaevolve,mega");
        expansions.put("raid-fire", "kyogre,inteleon,groudon,garchomp,diancie,rhyperior,quaquaval&@waterf,@water g,@mud s,@rock th,@smac&@orig,@hydro p,@hydro c,@prec,@earth p,@rock sl,@rock w&!inteleon,@hydro c&!diancie,megaevolve,mega");
        expansions.put("raid-water", "mewtwo,electivire,sceptile,rillaboom,zeraora&@psycho c,@thunder s,@fury,@razo,@volt&@thunderb,@wild c,@fren,@plas&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega&!zeraora,@plas");
        expansions.put("raid-electric", "groudon,garchomp,excadrill,rhyperior,landorus,swampert&@mud s,@mud-s&@prec,@earthq,@earth p,@scor,@drill r,@sands&!excadrill,@scor&!swampert,megaevolve,mega");
        expansions.put("raid-grass", "charizard,volcarona,kyurem,baxcalibur,eternatus,roserade,rayquaza,salamence&@fire s,@air s,@ice f,@poison j,@fire f&@blas,@over,@ice bu,@aval,@sludge b,@dragon a,@fly&!charizard,megaevolve,mega");
        expansions.put("raid-ice", "charizard,volcarona,lucario,diancie,rhyperior,zacian,chandelure&@fire s,@forc,@rock th,@smac,@meta&@blas,@over,@aura,@rock sl,@rock w,@behemoth bl&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-fighting", "rayquaza,salamence,mewtwo,metagross,gardevoir,moltres,toucannon,espeon,togekiss&@air s,@fire f,@conf,@psycho c,@zen h,@charm,@wing,@peck&@dragon a,@fly,@psyst,@psychic,@dazz,@beak&!togekiss,@charm");
        expansions.put("raid-poison", "groudon,garchomp,mewtwo,metagross,excadrill,espeon,rhyperior,alakazam&@mud s,@conf,@psycho c,@zen h,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@scor,@futu&!metagross,@psychic&!excadrill,@scor");
        expansions.put("raid-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,quaquaval&@waterf,@water g,@fury,@razo,@ice f&@orig,@hydro p,@aval,@hydro c,@fren,@leaf b,@ice bu&!inteleon,@hydro c&!sceptile,megaevolve,mega");
        expansions.put("raid-flying", "mewtwo,electivire,kyurem,baxcalibur,diancie&@psycho c,@thunder s,@ice f,@rock th&@thunderb,@wild c,@ice bu,@aval,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-psychic", "heracross,volcarona,necrozma,gholdengo,tyranitar,hydreigon,kleavor,chandelure&@fury,@bug bi,@psycho c,@hex,@bite&@megah,@bug bu,@moong,@shadow ba,@brut,@dark p,@x-sc&!heracross,megaevolve,mega");
        expansions.put("raid-bug", "charizard,volcarona,rayquaza,salamence,diancie,rhyperior,cinderace&@fire s,@air s,@fire f,@rock th,@smac&@blas,@over,@dragon a,@fly,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-rock", "kyogre,inteleon,sceptile,rillaboom,lucario,groudon,garchomp,quaquaval&@waterf,@water g,@fury,@razo,@forc,@mud s&@orig,@hydro c,@fren,@aura,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-ghost", "necrozma,gholdengo,tyranitar,hydreigon,dragapult,mewtwo,chandelure&@psycho c,@shadow c,@hex,@bite,@asto&@moong,@shadow ba,@brut,@dark p&!gholdengo,@hex&!dragapult,@asto&!mewtwo,shadow,megaevolve,mega");
        expansions.put("raid-dragon", "kyurem,baxcalibur,rayquaza,gardevoir,mamoswine,haxorus,zacian&@ice f,@dragon t,@charm,@powd,@meta&@ice bu,@free,@aval,@glai,@brea,@outr,@dazz,@play&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        expansions.put("raid-dark", "lucario,heracross,volcarona,gardevoir,blaziken,pinsir,vikavolt,togekiss&@forc,@coun,@fury,@bug bi,@charm&@aura,@megah,@bug bu,@dazz,@x-sc&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        expansions.put("raid-steel", "charizard,volcarona,lucario,groudon,garchomp,chandelure,blaziken,excadrill&@fire s,@forc,@coun,@mud s,@mud-s&@blas,@over,@aura,@prec,@earthq,@earth p,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-fairy", "eternatus,roserade,zacian,metagross,gengar,overqwil,zamazenta,tinkaton&@poison j,@meta,@bullet p,@lick,@shadow c,@fairy w&@sludge b,@behemoth bl,@meteor m,@behemoth ba,@gigat&!gengar,megaevolve,mega");
        expansions.put("raid-bug-dark", "charizard,volcarona,rayquaza,salamence,heracross,chandelure&@fire s,@air s,@bug bi,@fire f,@fury&@blas,@over,@bug bu,@dragon a,@fly,@megah&!charizard,megaevolve,mega&!heracross,megaevolve,mega");
        expansions.put("raid-bug-dragon", "kyurem,baxcalibur,rayquaza,salamence,diancie,rhyperior&@ice f,@air s,@dragon t,@fire f,@rock th,@smac&@ice bu,@aval,@dragon a,@brea,@fly,@rock sl,@rock w&!baxcalibur,@aval&!diancie,megaevolve,mega");
        expansions.put("raid-bug-electric", "charizard,volcarona,diancie,rhyperior,chandelure,glimmora,reshiram&@fire s,@rock th,@smac,@fire f&@blas,@over,@rock sl,@rock w,@meteor b,@fusion f&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-bug-fairy", "charizard,volcarona,eternatus,roserade,rayquaza,salamence,rhyperior,chandelure,overqwil&@fire s,@air s,@poison j,@fire f,@smac&@blas,@over,@sludge b,@dragon a,@fly,@rock w&!charizard,megaevolve,mega");
        expansions.put("raid-bug-fighting", "rayquaza,salamence,charizard,volcarona,mewtwo,gardevoir,moltres,staraptor&@air s,@fire f,@fire s,@conf,@psycho c,@charm,@wing,@gust&@dragon a,@fly,@blas,@over,@psyst,@dazz&!charizard,megaevolve,mega");
        expansions.put("raid-bug-fire", "diancie,rhyperior,kyogre,inteleon,rayquaza,salamence,glimmora,quaquaval&@rock th,@smac,@waterf,@water g,@air s,@fire f&@rock sl,@rock w,@orig,@hydro c,@dragon a,@fly,@meteor b&!diancie,megaevolve,mega");
        expansions.put("raid-bug-flying", "diancie,rhyperior,charizard,volcarona,mewtwo&@rock th,@smac,@fire s,@psycho c&@rock sl,@rock w,@ston,@blas,@over,@thunderb&!diancie,megaevolve,mega&!charizard,megaevolve,mega&!mewtwo,megaevolve,mega");
        expansions.put("raid-bug-ghost", "charizard,volcarona,rayquaza,salamence,diancie,rhyperior,cinderace&@fire s,@air s,@fire f,@rock th,@smac&@blas,@over,@dragon a,@fly,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-bug-grass", "charizard,volcarona,rayquaza,salamence,kyurem,baxcalibur,eternatus,roserade&@fire s,@air s,@fire f,@ice f,@poison j&@blas,@over,@dragon a,@fly,@ice bu,@aval,@sludge b&!charizard,megaevolve,mega");
        expansions.put("raid-bug-ground", "charizard,volcarona,kyogre,inteleon,kyurem,baxcalibur,rayquaza,chandelure,quaquaval&@fire s,@air s,@waterf,@water g,@ice f&@blas,@over,@orig,@aval,@hydro c,@ice bu,@dragon a&!charizard,megaevolve,mega");
        expansions.put("raid-bug-ice", "charizard,volcarona,diancie,rhyperior,rayquaza,salamence,cinderace&@fire s,@air s,@rock th,@smac,@fire f&@blas,@over,@rock sl,@rock w,@dragon a,@fly&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-bug-normal", "charizard,volcarona,rayquaza,salamence,diancie,rhyperior,cinderace&@fire s,@air s,@fire f,@rock th,@smac&@blas,@over,@dragon a,@fly,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-bug-poison", "charizard,volcarona,rayquaza,salamence,mewtwo,rhyperior,chandelure,reshiram&@fire s,@air s,@fire f,@conf,@psycho c,@smac&@blas,@over,@dragon a,@fly,@psyst,@rock w,@fusion f&!charizard,megaevolve,mega");
        expansions.put("raid-bug-psychic", "charizard,volcarona,rayquaza,salamence,heracross,chandelure&@fire s,@air s,@bug bi,@fire f,@fury&@blas,@over,@bug bu,@dragon a,@fly,@megah&!charizard,megaevolve,mega&!heracross,megaevolve,mega");
        expansions.put("raid-bug-rock", "kyogre,inteleon,diancie,rhyperior,zacian,metagross,quaquaval,primarina&@waterf,@water g,@rock th,@smac,@meta,@bullet p&@orig,@hydro c,@rock sl,@rock w,@behemoth bl,@meteor m&!diancie,megaevolve,mega");
        expansions.put("raid-bug-steel", "charizard,volcarona,chandelure,reshiram,cinderace,blaziken,heatran&@fire s,@fire f&@blas,@over,@fusion f,@magm&!charizard,megaevolve,mega&!blaziken,megaevolve,mega&!heatran,shadow");
        expansions.put("raid-bug-water", "mewtwo,electivire,rayquaza,salamence,diancie&@psycho c,@thunder s,@air s,@fire f,@rock th&@thunderb,@wild c,@dragon a,@fly,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-dark-dragon", "gardevoir,kyurem,baxcalibur,lucario,heracross,volcarona,zacian,togekiss&@charm,@ice f,@forc,@coun,@fury,@bug bi,@meta&@dazz,@ice bu,@aval,@glai,@aura,@megah,@bug bu,@play&!heracross,megaevolve,mega");
        expansions.put("raid-dark-electric", "lucario,groudon,garchomp,heracross,volcarona,gardevoir,blaziken&@forc,@coun,@mud s,@fury,@bug bi,@charm&@aura,@prec,@earthq,@earth p,@megah,@bug bu,@dazz&!heracross,@megah&!heracross,megaevolve,mega");
        expansions.put("raid-dark-fairy", "eternatus,roserade,zacian,metagross,gardevoir,gengar&@poison j,@meta,@bullet p,@charm,@lick&@sludge b,@behemoth bl,@play,@meteor m,@dazz&!roserade,@sludge b&!gengar,@sludge b&!gengar,megaevolve,mega");
        expansions.put("raid-dark-fighting", "gardevoir,lucario,rayquaza,salamence,zacian,togekiss,blaziken,moltres,enamorus&@charm,@forc,@coun,@air s,@fire f,@meta,@wing,@fairy w&@dazz,@aura,@dragon a,@fly,@play&!zacian,@meta&!togekiss,@charm");
        expansions.put("raid-dark-fire", "kyogre,inteleon,lucario,groudon,garchomp,diancie,rhyperior,quaquaval&@waterf,@water g,@forc,@coun,@mud s,@rock th,@smac&@orig,@hydro c,@aura,@prec,@earth p,@rock sl,@rock w&!diancie,megaevolve,mega");
        expansions.put("raid-dark-flying", "mewtwo,electivire,kyurem,baxcalibur,diancie&@psycho c,@thunder s,@ice f,@rock th&@thunderb,@wild c,@ice bu,@aval,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-dark-ghost", "gardevoir,zacian,togekiss,enamorus,hatterene,alakazam,tapu lele,xerneas&@charm,@meta,@fairy w,@psycho c,@asto,@geom&@dazz,@play,@natu,@moonb&!enamorus,@fairy w&!alakazam,megaevolve,mega");
        expansions.put("raid-dark-grass", "heracross,volcarona,charizard,kyurem,baxcalibur,lucario&@fury,@bug bi,@fire s,@ice f,@forc,@coun&@megah,@bug bu,@over,@blas,@ice bu,@aval,@aura&!heracross,megaevolve,mega&!charizard,megaevolve,mega");
        expansions.put("raid-dark-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,lucario,volcarona&@waterf,@water g,@fury,@razo,@ice f,@forc,@bug bi&@orig,@aval,@hydro c,@fren,@ice bu,@aura,@bug bu&!sceptile,megaevolve,mega");
        expansions.put("raid-dark-ice", "lucario,charizard,volcarona,heracross,rhyperior,gardevoir&@forc,@fire s,@bug bi,@fury,@smac,@charm&@aura,@blas,@over,@bug bu,@megah,@rock w,@dazz&!charizard,megaevolve,mega&!heracross,megaevolve,mega");
        expansions.put("raid-dark-normal", "lucario,heracross,volcarona,gardevoir,blaziken,pinsir,vikavolt,togekiss&@forc,@coun,@fury,@bug bi,@charm&@aura,@megah,@bug bu,@dazz,@x-sc&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        expansions.put("raid-dark-poison", "groudon,garchomp,excadrill,rhyperior,landorus,swampert&@mud s,@mud-s&@prec,@earthq,@earth p,@scor,@drill r,@sands&!excadrill,@scor&!swampert,megaevolve,mega");
        expansions.put("raid-dark-psychic", "heracross,volcarona,gardevoir,pinsir,vikavolt,zacian,togekiss,kleavor,hatterene&@fury,@bug bi,@charm,@meta&@megah,@bug bu,@dazz,@x-sc,@play&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        expansions.put("raid-dark-rock", "lucario,kyogre,inteleon,sceptile,rillaboom,groudon,garchomp&@forc,@coun,@waterf,@water g,@fury,@razo,@mud s&@aura,@orig,@hydro c,@fren,@leaf b,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-dark-steel", "lucario,charizard,volcarona,groudon,garchomp,blaziken,chandelure,excadrill&@forc,@coun,@fire s,@mud s,@mud-s&@aura,@blas,@over,@prec,@earthq,@earth p,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-dark-water", "mewtwo,electivire,sceptile,rillaboom,lucario,magnezone&@psycho c,@thunder s,@fury,@razo,@forc,@volt&@thunderb,@wild c,@fren,@aura&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-dragon-electric", "kyurem,baxcalibur,groudon,garchomp,rayquaza,gardevoir&@ice f,@mud s,@dragon t,@charm&@ice bu,@aval,@glai,@prec,@earth p,@brea,@dazz&!baxcalibur,@aval,@glai&!groudon,@mud s&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-fairy", "kyurem,baxcalibur,eternatus,roserade,zacian,metagross,gardevoir&@ice f,@dragon t,@poison j,@meta,@bullet p,@charm&@ice bu,@free,@aval,@sludge b,@behemoth bl,@play,@meteor m,@dazz&!roserade,@sludge b");
        expansions.put("raid-dragon-fighting", "gardevoir,kyurem,baxcalibur,rayquaza,salamence,mewtwo,metagross,moltres,espeon&@charm,@conf,@ice f,@air s,@fire f,@psycho c,@zen h,@wing&@dazz,@trip,@psychic,@ice bu,@aval,@glai,@dragon a,@fly,@psyst");
        expansions.put("raid-dragon-fire", "groudon,garchomp,diancie,rhyperior,rayquaza&@mud s,@dragon t,@rock th,@smac,@mud-s&@prec,@earthq,@earth p,@brea,@rock sl,@rock w&!groudon,@mud s&!diancie,megaevolve,mega&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-flying", "kyurem,baxcalibur,diancie,rhyperior,rayquaza&@ice f,@dragon t,@rock th,@smac&@ice bu,@free,@aval,@glai,@rock sl,@rock w,@brea&!baxcalibur,@aval,@glai&!diancie,megaevolve,mega&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-ghost", "kyurem,baxcalibur,necrozma,gholdengo,rayquaza,hydreigon&@ice f,@dragon t,@psycho c,@hex,@bite&@ice bu,@free,@aval,@glai,@moong,@shadow ba,@brea,@brut&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-grass", "kyurem,baxcalibur,eternatus,roserade,rayquaza,salamence,heracross&@ice f,@dragon t,@poison j,@air s,@fire f,@fury&@ice bu,@free,@aval,@glai,@sludge b,@dragon a,@fly,@megah&!heracross,megaevolve,mega");
        expansions.put("raid-dragon-ground", "kyurem,baxcalibur,rayquaza,gardevoir,mamoswine,haxorus,zacian&@ice f,@dragon t,@charm,@powd,@meta&@ice bu,@free,@aval,@glai,@brea,@outr,@dazz,@play&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-ice", "lucario,diancie,rhyperior,rayquaza,baxcalibur&@forc,@coun,@rock th,@smac,@dragon t,@ice f&@aura,@rock sl,@rock w,@brea,@outr,@glai&!diancie,megaevolve,mega&!rayquaza,megaevolve,mega&!baxcalibur,@glai");
        expansions.put("raid-dragon-normal", "kyurem,baxcalibur,lucario,rayquaza,gardevoir,mamoswine,blaziken&@ice f,@dragon t,@forc,@coun,@charm,@powd&@ice bu,@free,@aval,@glai,@aura,@brea,@dazz&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-poison", "kyurem,baxcalibur,groudon,garchomp,mewtwo,metagross,espeon,darmanitan&@ice f,@dragon t,@mud s,@conf,@psycho c,@zen h&@ice bu,@free,@aval,@glai,@prec,@earth p,@psyst,@ice be,@psychic&!groudon,@mud s");
        expansions.put("raid-dragon-psychic", "kyurem,baxcalibur,heracross,volcarona,necrozma,gholdengo,hydreigon&@ice f,@fury,@bug bi,@psycho c,@hex,@bite&@ice bu,@aval,@glai,@megah,@bug bu,@moong,@shadow ba,@brut&!heracross,megaevolve,mega");
        expansions.put("raid-dragon-rock", "kyurem,baxcalibur,lucario,groudon,garchomp,rayquaza&@ice f,@dragon t,@forc,@mud s&@ice bu,@free,@aval,@glai,@aura,@prec,@earth p,@brea&!baxcalibur,@aval,@glai&!groudon,@mud s&!rayquaza,megaevolve,mega");
        expansions.put("raid-dragon-steel", "lucario,groudon,garchomp,blaziken,excadrill,conkeldurr,rhyperior,keldeo,landorus&@forc,@coun,@mud s,@mud-s,@low k&@aura,@prec,@earthq,@earth p,@scor,@dynami,@drill r,@secr,@sands&!excadrill,@scor");
        expansions.put("raid-dragon-water", "rayquaza,baxcalibur,gardevoir,haxorus,zacian,togekiss,eternatus,garchomp&@dragon t,@ice f,@charm,@meta&@brea,@outr,@glai,@dazz,@play,@dynama&!rayquaza,megaevolve,mega&!baxcalibur,@glai&!garchomp,@brea");
        expansions.put("raid-electric-fairy", "eternatus,roserade,groudon,garchomp,gengar,overqwil,excadrill,revavroom&@poison j,@mud s,@lick,@shadow c,@mud-s&@sludge b,@prec,@earthq,@earth p,@scor,@gunk&!gengar,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-electric-fighting", "groudon,garchomp,mewtwo,metagross,gardevoir,excadrill,espeon&@mud s,@conf,@psycho c,@zen h,@charm,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@dazz,@scor&!metagross,@psychic&!excadrill,@scor");
        expansions.put("raid-electric-fire", "groudon,garchomp,kyogre,inteleon,diancie,rhyperior&@mud s,@waterf,@water g,@rock th,@smac&@prec,@earthq,@earth p,@orig,@hydro p,@hydro c,@rock sl,@rock w&!inteleon,@hydro c&!diancie,megaevolve,mega");
        expansions.put("raid-electric-flying", "kyurem,baxcalibur,diancie,rhyperior,mamoswine,glimmora,darmanitan&@ice f,@dragon t,@rock th,@smac,@powd&@ice bu,@free,@aval,@rock sl,@rock w,@ston,@meteor b&!diancie,megaevolve,mega&!darmanitan,@aval");
        expansions.put("raid-electric-ghost", "groudon,garchomp,necrozma,gholdengo,tyranitar,hydreigon,rhyperior,mewtwo&@mud s,@psycho c,@shadow c,@hex,@bite,@mud-s&@prec,@earthq,@earth p,@moong,@shadow ba,@brut&!mewtwo,shadow,megaevolve,mega");
        expansions.put("raid-electric-grass", "charizard,volcarona,kyurem,baxcalibur,eternatus,roserade,heracross&@fire s,@ice f,@poison j,@fury&@blas,@over,@ice bu,@aval,@sludge b,@megah&!charizard,megaevolve,mega&!heracross,megaevolve,mega");
        expansions.put("raid-electric-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,groudon&@waterf,@water g,@fury,@razo,@ice f,@mud s&@orig,@hydro p,@aval,@hydro c,@fren,@ice bu,@prec&!inteleon,@hydro c&!sceptile,megaevolve,mega");
        expansions.put("raid-electric-ice", "charizard,volcarona,lucario,groudon,garchomp,diancie,rhyperior&@fire s,@forc,@mud s,@rock th,@smac&@blas,@over,@aura,@prec,@earth p,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-electric-normal", "lucario,groudon,garchomp,blaziken,excadrill,conkeldurr,rhyperior,keldeo,landorus&@forc,@coun,@mud s,@mud-s,@low k&@aura,@prec,@earthq,@earth p,@scor,@dynami,@drill r,@secr,@sands&!excadrill,@scor");
        expansions.put("raid-electric-poison", "groudon,garchomp,mewtwo,metagross,excadrill,espeon,rhyperior,alakazam&@mud s,@conf,@psycho c,@zen h,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@scor,@futu&!metagross,@psychic&!excadrill,@scor");
        expansions.put("raid-electric-psychic", "groudon,garchomp,heracross,volcarona,necrozma,gholdengo,hydreigon&@mud s,@fury,@bug bi,@psycho c,@shadow c,@hex,@bite&@prec,@earth p,@megah,@bug bu,@moong,@shadow ba,@brut&!heracross,megaevolve,mega");
        expansions.put("raid-electric-rock", "groudon,garchomp,kyogre,inteleon,sceptile,rillaboom,lucario,quaquaval&@mud s,@waterf,@water g,@fury,@razo,@forc&@prec,@earth p,@orig,@hydro c,@fren,@aura&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-electric-steel", "groudon,garchomp,charizard,volcarona,lucario,excadrill,chandelure,blaziken&@mud s,@fire s,@forc,@coun,@mud-s&@prec,@earthq,@earth p,@blas,@over,@aura,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-electric-water", "sceptile,rillaboom,groudon,garchomp,meowscarada,rhyperior&@fury,@razo,@mud s,@leafa,@mud-s&@fren,@leaf b,@prec,@earthq,@earth p&!sceptile,@fren,@leaf b&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-fairy-fighting", "eternatus,roserade,rayquaza,salamence,mewtwo,metagross,zacian,espeon&@poison j,@air s,@fire f,@conf,@psycho c,@zen h,@meta&@sludge b,@dragon a,@fly,@psyst,@psychic,@behemoth bl,@play&!zacian,@meta");
        expansions.put("raid-fairy-fire", "kyogre,inteleon,eternatus,roserade,groudon,garchomp,diancie,rhyperior&@waterf,@water g,@poison j,@mud s,@rock th,@smac&@orig,@hydro c,@sludge b,@prec,@earth p,@rock sl,@rock w&!diancie,megaevolve,mega");
        expansions.put("raid-fairy-flying", "mewtwo,electivire,kyurem,baxcalibur,eternatus,roserade,magnezone&@psycho c,@thunder s,@ice f,@poison j,@volt&@thunderb,@wild c,@ice bu,@aval,@sludge b&!mewtwo,megaevolve,mega&!electivire,@wild c");
        expansions.put("raid-fairy-ghost", "necrozma,gholdengo,zacian,metagross,dragapult,zamazenta,chandelure&@psycho c,@shadow c,@meta,@hex,@bullet p,@asto&@moong,@shadow ba,@behemoth bl,@meteor m,@behemoth ba&!gholdengo,@hex&!dragapult,@asto");
        expansions.put("raid-fairy-grass", "eternatus,roserade,charizard,volcarona,kyurem,baxcalibur,rayquaza,salamence&@poison j,@fire s,@air s,@ice f,@fire f&@sludge b,@blas,@over,@ice bu,@aval,@dragon a,@fly&!charizard,megaevolve,mega");
        expansions.put("raid-fairy-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,zacian,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@meta&@orig,@aval,@hydro c,@fren,@leaf b,@ice bu,@behemoth bl&!sceptile,megaevolve,mega");
        expansions.put("raid-fairy-ice", "zacian,metagross,charizard,volcarona,eternatus,roserade,rhyperior,chandelure&@meta,@bullet p,@fire s,@poison j,@smac&@behemoth bl,@meteor m,@blas,@over,@sludge b,@rock w&!charizard,megaevolve,mega");
        expansions.put("raid-fairy-normal", "eternatus,roserade,zacian,metagross,gengar,overqwil,zamazenta,tinkaton&@poison j,@meta,@bullet p,@lick,@shadow c,@fairy w&@sludge b,@behemoth bl,@meteor m,@behemoth ba,@gigat&!gengar,megaevolve,mega");
        expansions.put("raid-fairy-poison", "groudon,garchomp,mewtwo,metagross,zacian,espeon,alakazam&@mud s,@conf,@psycho c,@zen h,@bullet p,@meta&@prec,@earthq,@earth p,@psyst,@psychic,@meteor m,@behemoth bl,@futu&!metagross,@psychic,@meteor m");
        expansions.put("raid-fairy-psychic", "eternatus,roserade,necrozma,gholdengo,zacian,metagross,gengar&@poison j,@psycho c,@meta,@hex,@bullet p,@lick&@sludge b,@moong,@shadow ba,@behemoth bl,@meteor m&!gengar,@lick&!gengar,megaevolve,mega");
        expansions.put("raid-fairy-rock", "zacian,metagross,kyogre,inteleon,sceptile,rillaboom,groudon,quaquaval&@meta,@bullet p,@waterf,@water g,@fury,@razo,@mud s&@behemoth bl,@meteor m,@orig,@hydro c,@fren,@prec&!sceptile,megaevolve,mega");
        expansions.put("raid-fairy-steel", "charizard,volcarona,groudon,garchomp,chandelure,excadrill,reshiram,landorus&@fire s,@mud s,@mud-s,@fire f&@blas,@over,@prec,@earthq,@earth p,@scor,@fusion f&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-fairy-water", "mewtwo,electivire,sceptile,rillaboom,eternatus&@psycho c,@thunder s,@fury,@razo,@poison j&@thunderb,@wild c,@fren,@sludge b&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-fighting-fire", "kyogre,inteleon,groudon,garchomp,rayquaza,salamence,mewtwo,quaquaval,espeon&@waterf,@water g,@mud s,@air s,@fire f,@conf,@psycho c&@orig,@hydro c,@prec,@earthq,@earth p,@dragon a,@fly,@psyst,@psychic");
        expansions.put("raid-fighting-flying", "mewtwo,electivire,kyurem,baxcalibur,rayquaza,salamence,espeon&@psycho c,@conf,@thunder s,@ice f,@air s,@fire f&@thunderb,@psyst,@psychic,@wild c,@ice bu,@aval,@dragon a,@fly&!electivire,@wild c");
        expansions.put("raid-fighting-ghost", "rayquaza,salamence,mewtwo,metagross,necrozma,gholdengo,moltres,espeon,staraptor&@air s,@fire f,@conf,@psycho c,@zen h,@shadow c,@hex,@wing,@gust&@dragon a,@fly,@psyst,@psychic,@shadow ba,@moong");
        expansions.put("raid-fighting-grass", "rayquaza,salamence,charizard,volcarona,kyurem,baxcalibur,eternatus,roserade&@air s,@fire f,@fire s,@ice f,@poison j&@dragon a,@fly,@blas,@over,@ice bu,@aval,@sludge b&!charizard,megaevolve,mega");
        expansions.put("raid-fighting-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,rayquaza,salamence&@waterf,@water g,@fury,@razo,@ice f,@air s,@fire f&@orig,@aval,@hydro c,@fren,@ice bu,@dragon a,@fly&!sceptile,megaevolve,mega");
        expansions.put("raid-fighting-ice", "charizard,volcarona,lucario,rayquaza,salamence,mewtwo,chandelure,cinderace&@fire s,@air s,@forc,@coun,@fire f,@conf,@psycho c&@blas,@over,@aura,@dragon a,@fly,@psyst&!charizard,megaevolve,mega");
        expansions.put("raid-fighting-normal", "lucario,rayquaza,salamence,mewtwo,metagross,gardevoir,blaziken,moltres,espeon,staraptor&@forc,@coun,@air s,@fire f,@conf,@psycho c,@zen h,@charm,@wing,@gust&@aura,@dragon a,@fly,@psyst,@psychic,@dazz");
        expansions.put("raid-fighting-poison", "mewtwo,metagross,groudon,garchomp,rayquaza,salamence,espeon,moltres&@conf,@psycho c,@zen h,@mud s,@air s,@fire f,@wing&@psyst,@psychic,@prec,@earthq,@earth p,@dragon a,@fly&!metagross,@psychic");
        expansions.put("raid-fighting-psychic", "rayquaza,salamence,necrozma,gholdengo,gardevoir,moltres,toucannon&@air s,@fire f,@psycho c,@hex,@charm,@wing,@peck&@dragon a,@fly,@moong,@shadow ba,@dazz,@beak&!gholdengo,@shadow ba&!gardevoir,@dazz");
        expansions.put("raid-fighting-rock", "kyogre,inteleon,sceptile,rillaboom,lucario,groudon,garchomp,quaquaval&@waterf,@water g,@fury,@razo,@forc,@mud s&@orig,@hydro c,@fren,@aura,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-fighting-steel", "charizard,volcarona,lucario,groudon,garchomp,chandelure,blaziken,excadrill&@fire s,@forc,@coun,@mud s,@mud-s&@blas,@over,@aura,@prec,@earthq,@earth p,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-fighting-water", "mewtwo,electivire,sceptile,rillaboom,rayquaza,zacian&@psycho c,@thunder s,@fury,@razo,@air s,@meta&@thunderb,@psyst,@wild c,@fren,@leaf b,@dragon a,@play&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-fire-flying", "diancie,rhyperior,kyogre,inteleon,mewtwo,glimmora&@rock th,@smac,@waterf,@water g,@psycho c&@rock sl,@rock w,@ston,@orig,@hydro c,@thunderb,@meteor b&!diancie,megaevolve,mega&!mewtwo,megaevolve,mega");
        expansions.put("raid-fire-ghost", "kyogre,inteleon,groudon,garchomp,diancie,rhyperior,necrozma,quaquaval&@waterf,@water g,@mud s,@rock th,@smac,@psycho c&@orig,@hydro c,@prec,@earth p,@rock sl,@rock w,@moong&!diancie,megaevolve,mega");
        expansions.put("raid-fire-grass", "eternatus,roserade,rayquaza,salamence,diancie,rhyperior,gengar&@poison j,@air s,@fire f,@rock th,@smac,@lick&@sludge b,@dragon a,@fly,@rock sl,@rock w&!diancie,megaevolve,mega&!gengar,megaevolve,mega");
        expansions.put("raid-fire-ground", "kyogre,inteleon,groudon,garchomp,quaquaval,excadrill,primarina,rhyperior&@waterf,@water g,@mud s,@mud-s&@orig,@hydro p,@hydro c,@prec,@earthq,@earth p,@scor&!inteleon,@hydro c&!excadrill,@scor");
        expansions.put("raid-fire-ice", "diancie,rhyperior,kyogre,inteleon,lucario,groudon,garchomp,glimmora&@rock th,@smac,@waterf,@water g,@forc,@mud s&@rock sl,@rock w,@orig,@hydro c,@aura,@prec,@earth p,@meteor b&!diancie,megaevolve,mega");
        expansions.put("raid-fire-normal", "kyogre,inteleon,lucario,groudon,garchomp,diancie,rhyperior,quaquaval&@waterf,@water g,@forc,@coun,@mud s,@rock th,@smac&@orig,@hydro c,@aura,@prec,@earth p,@rock sl,@rock w&!diancie,megaevolve,mega");
        expansions.put("raid-fire-poison", "groudon,garchomp,kyogre,inteleon,mewtwo,metagross,rhyperior,quaquaval,espeon,alakazam&@mud s,@waterf,@water g,@conf,@psycho c,@zen h,@smac&@prec,@earth p,@orig,@hydro c,@psyst,@psychic,@rock w,@ston");
        expansions.put("raid-fire-psychic", "kyogre,inteleon,groudon,garchomp,diancie,rhyperior,necrozma,quaquaval&@waterf,@water g,@mud s,@rock th,@smac,@psycho c&@orig,@hydro c,@prec,@earth p,@rock sl,@rock w,@moong&!diancie,megaevolve,mega");
        expansions.put("raid-fire-rock", "kyogre,inteleon,groudon,garchomp,lucario,diancie,rhyperior,quaquaval&@waterf,@water g,@mud s,@forc,@rock th,@smac&@orig,@hydro c,@prec,@earthq,@earth p,@aura,@rock sl,@rock w&!diancie,megaevolve,mega");
        expansions.put("raid-fire-steel", "groudon,garchomp,kyogre,inteleon,lucario,excadrill,quaquaval&@mud s,@waterf,@water g,@forc,@coun,@mud-s&@prec,@earthq,@earth p,@orig,@hydro p,@hydro c,@aura,@scor&!inteleon,@hydro c&!excadrill,@scor");
        expansions.put("raid-fire-water", "mewtwo,electivire,groudon,garchomp,diancie&@psycho c,@thunder s,@mud s,@rock th&@thunderb,@wild c,@prec,@earthq,@earth p,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-flying-ghost", "mewtwo,electivire,kyurem,baxcalibur,diancie&@psycho c,@thunder s,@ice f,@rock th&@thunderb,@wild c,@ice bu,@aval,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-flying-grass", "kyurem,baxcalibur,charizard,volcarona,eternatus,roserade,rayquaza,salamence&@ice f,@fire s,@air s,@poison j,@fire f&@ice bu,@aval,@blas,@over,@sludge b,@dragon a,@fly&!charizard,megaevolve,mega");
        expansions.put("raid-flying-ground", "kyurem,baxcalibur,kyogre,inteleon,mamoswine,quaquaval,darmanitan,primarina,zamazenta&@ice f,@dragon t,@waterf,@water g,@powd&@ice bu,@free,@aval,@orig,@hydro p,@hydro c,@behemoth ba&!inteleon,@hydro c");
        expansions.put("raid-flying-ice", "diancie,rhyperior,charizard,volcarona,mewtwo&@rock th,@smac,@fire s,@psycho c&@rock sl,@rock w,@ston,@blas,@over,@thunderb&!diancie,megaevolve,mega&!charizard,megaevolve,mega&!mewtwo,megaevolve,mega");
        expansions.put("raid-flying-normal", "mewtwo,electivire,kyurem,baxcalibur,diancie&@psycho c,@thunder s,@ice f,@rock th&@thunderb,@wild c,@ice bu,@aval,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-flying-poison", "mewtwo,electivire,kyurem,baxcalibur,metagross,magnezone,espeon,darmanitan&@psycho c,@conf,@thunder s,@ice f,@zen h,@volt&@thunderb,@psyst,@ice be,@psychic,@wild c,@ice bu,@aval&!electivire,@wild c");
        expansions.put("raid-flying-psychic", "mewtwo,electivire,kyurem,baxcalibur,diancie&@psycho c,@thunder s,@ice f,@rock th&@thunderb,@wild c,@ice bu,@aval,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-flying-rock", "kyogre,inteleon,mewtwo,electivire,kyurem,baxcalibur,quaquaval&@waterf,@water g,@psycho c,@thunder s,@ice f&@orig,@aval,@hydro c,@thunderb,@wild c,@ice bu&!mewtwo,megaevolve,mega&!electivire,@wild c");
        expansions.put("raid-flying-steel", "charizard,volcarona,mewtwo,electivire,chandelure,magnezone&@fire s,@psycho c,@thunder s,@volt&@blas,@over,@thunderb,@wild c&!charizard,megaevolve,mega&!mewtwo,megaevolve,mega&!electivire,@wild c");
        expansions.put("raid-flying-water", "mewtwo,electivire,diancie,rhyperior,magnezone&@psycho c,@thunder s,@rock th,@smac,@volt&@thunderb,@wild c,@rock sl,@rock w,@ston&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        expansions.put("raid-ghost-grass", "charizard,volcarona,kyurem,baxcalibur,rayquaza,salamence,necrozma,chandelure,reshiram&@fire s,@air s,@ice f,@fire f,@psycho c&@blas,@over,@ice bu,@aval,@dragon a,@fly,@moong&!charizard,megaevolve,mega");
        expansions.put("raid-ghost-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,necrozma,hydreigon&@waterf,@water g,@fury,@razo,@ice f,@psycho c,@bite&@orig,@aval,@hydro c,@fren,@ice bu,@moong,@brut&!sceptile,megaevolve,mega");
        expansions.put("raid-ghost-ice", "charizard,volcarona,diancie,rhyperior,necrozma,gholdengo&@fire s,@rock th,@smac,@psycho c,@hex&@blas,@over,@rock sl,@rock w,@ston,@moong,@shadow ba&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-ghost-normal", "tyranitar,hydreigon,kingambit,darkrai,absol,gengar,houndoom&@bite,@snar,@suck&@brut,@dark p,@foul,@shadow ba&!absol,shadow,megaevolve,mega&!gengar,megaevolve,mega&!houndoom,megaevolve,mega");
        expansions.put("raid-ghost-poison", "groudon,garchomp,mewtwo,metagross,necrozma,gholdengo,hydreigon,espeon,tyranitar,chandelure&@mud s,@conf,@psycho c,@zen h,@shadow c,@hex,@bite&@prec,@earth p,@psyst,@psychic,@shadow ba,@moong,@brut");
        expansions.put("raid-ghost-psychic", "necrozma,gholdengo,tyranitar,hydreigon,dragapult,mewtwo,chandelure&@psycho c,@shadow c,@hex,@bite,@asto&@moong,@shadow ba,@brut,@dark p&!gholdengo,@hex&!dragapult,@asto&!mewtwo,shadow,megaevolve,mega");
        expansions.put("raid-ghost-rock", "kyogre,inteleon,sceptile,rillaboom,groudon,garchomp,necrozma&@waterf,@water g,@fury,@razo,@mud s,@psycho c&@orig,@hydro c,@fren,@leaf b,@prec,@earth p,@moong&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-ghost-steel", "charizard,volcarona,groudon,garchomp,necrozma,gholdengo,hydreigon,chandelure&@fire s,@mud s,@psycho c,@hex,@bite&@blas,@over,@prec,@earthq,@earth p,@moong,@shadow ba,@brut&!charizard,megaevolve,mega");
        expansions.put("raid-ghost-water", "mewtwo,electivire,sceptile,rillaboom,necrozma,magnezone&@psycho c,@thunder s,@fury,@razo,@volt&@thunderb,@wild c,@fren,@moong&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-grass-ground", "kyurem,baxcalibur,charizard,volcarona,rayquaza,salamence,chandelure&@ice f,@dragon t,@fire s,@air s,@bug bi,@fire f&@ice bu,@free,@aval,@blas,@over,@bug bu,@dragon a,@fly&!charizard,megaevolve,mega");
        expansions.put("raid-grass-ice", "charizard,volcarona,lucario,eternatus,roserade,rayquaza,salamence&@fire s,@air s,@bug bi,@forc,@coun,@poison j,@fire f&@blas,@over,@bug bu,@aura,@sludge b,@dragon a,@fly&!charizard,megaevolve,mega");
        expansions.put("raid-grass-normal", "charizard,volcarona,kyurem,baxcalibur,lucario,eternatus,roserade,rayquaza&@fire s,@air s,@ice f,@forc,@coun,@poison j&@blas,@over,@ice bu,@aval,@aura,@sludge b,@dragon a&!charizard,megaevolve,mega");
        expansions.put("raid-grass-poison", "charizard,volcarona,kyurem,baxcalibur,rayquaza,salamence,mewtwo,cinderace&@fire s,@air s,@ice f,@fire f,@conf,@psycho c&@blas,@over,@ice bu,@aval,@dragon a,@fly,@psyst&!charizard,megaevolve,mega");
        expansions.put("raid-grass-psychic", "heracross,volcarona,charizard,kyurem,baxcalibur,roserade&@fury,@bug bi,@fire s,@ice f,@poison j&@megah,@bug bu,@over,@blas,@ice bu,@aval,@sludge b&!heracross,megaevolve,mega&!charizard,megaevolve,mega");
        expansions.put("raid-grass-rock", "kyurem,baxcalibur,lucario,heracross,volcarona,zacian,metagross&@ice f,@forc,@coun,@fury,@bug bi,@meta,@bullet p&@ice bu,@aval,@aura,@meteor m,@megah,@bug bu,@behemoth bl&!heracross,megaevolve,mega");
        expansions.put("raid-grass-steel", "charizard,volcarona,lucario,chandelure,blaziken,reshiram,cinderace,conkeldurr,keldeo&@fire s,@forc,@coun,@fire f,@low k&@blas,@over,@aura,@fusion f,@dynami,@secr&!charizard,megaevolve,mega");
        expansions.put("raid-grass-water", "eternatus,roserade,rayquaza,salamence,heracross,volcarona,gengar&@poison j,@air s,@fire f,@fury,@bug bi,@lick&@sludge b,@dragon a,@fly,@megah,@bug bu&!heracross,megaevolve,mega&!gengar,megaevolve,mega");
        expansions.put("raid-ground-ice", "charizard,volcarona,kyogre,inteleon,sceptile,rillaboom,lucario&@fire s,@waterf,@water g,@fury,@razo,@forc&@blas,@over,@orig,@hydro c,@fren,@aura&!charizard,megaevolve,mega&!sceptile,megaevolve,mega");
        expansions.put("raid-ground-normal", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,lucario&@waterf,@water g,@fury,@razo,@ice f,@forc&@orig,@hydro p,@aval,@hydro c,@fren,@ice bu,@aura&!inteleon,@hydro c&!sceptile,megaevolve,mega");
        expansions.put("raid-ground-poison", "kyogre,inteleon,kyurem,baxcalibur,groudon,garchomp,mewtwo,metagross,espeon&@waterf,@water g,@ice f,@mud s,@conf,@psycho c,@zen h&@orig,@aval,@hydro c,@ice bu,@prec,@earth p,@psyst,@ice be,@psychic");
        expansions.put("raid-ground-psychic", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,heracross&@waterf,@water g,@fury,@razo,@ice f&@orig,@aval,@hydro c,@fren,@ice bu,@megah&!sceptile,megaevolve,mega&!heracross,megaevolve,mega");
        expansions.put("raid-ground-rock", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,lucario,groudon,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@forc,@mud s&@orig,@aval,@hydro c,@fren,@ice bu,@aura,@prec&!sceptile,megaevolve,mega");
        expansions.put("raid-ground-steel", "charizard,volcarona,kyogre,inteleon,lucario,groudon,garchomp,chandelure,quaquaval&@fire s,@waterf,@water g,@forc,@coun,@mud s&@blas,@over,@orig,@hydro c,@aura,@prec,@earth p&!charizard,megaevolve,mega");
        expansions.put("raid-ground-water", "sceptile,rillaboom,meowscarada,venusaur,roserade&@fury,@razo,@leafa,@vine,@magi&@fren,@leaf b,@grass k,@sola&!sceptile,megaevolve,mega&!venusaur,@vine&!venusaur,shadow,megaevolve,mega&!roserade,@magi");
        expansions.put("raid-ice-normal", "lucario,charizard,volcarona,diancie,rhyperior,zacian&@forc,@coun,@fire s,@rock th,@smac,@meta&@aura,@blas,@over,@rock sl,@rock w,@ston,@behemoth bl&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-ice-poison", "charizard,volcarona,groudon,garchomp,mewtwo,metagross,espeon,cinderace&@fire s,@mud s,@conf,@psycho c,@zen h&@blas,@over,@prec,@earth p,@psyst,@psychic&!charizard,megaevolve,mega&!volcarona,@over");
        expansions.put("raid-ice-psychic", "charizard,volcarona,heracross,diancie,chandelure&@fire s,@bug bi,@fury,@rock th&@blas,@over,@bug bu,@megah,@rock sl&!charizard,megaevolve,mega&!heracross,megaevolve,mega&!diancie,megaevolve,mega");
        expansions.put("raid-ice-rock", "lucario,zacian,metagross,kyogre,inteleon,sceptile,rillaboom&@forc,@coun,@meta,@bullet p,@waterf,@water g,@fury,@razo&@aura,@meteor m,@behemoth bl,@orig,@hydro c,@fren,@leaf b&!sceptile,megaevolve,mega");
        expansions.put("raid-ice-steel", "charizard,volcarona,lucario,groudon,garchomp,chandelure,blaziken,excadrill&@fire s,@forc,@coun,@mud s,@mud-s&@blas,@over,@aura,@prec,@earthq,@earth p,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-ice-water", "mewtwo,electivire,sceptile,rillaboom,lucario,magnezone&@psycho c,@thunder s,@fury,@razo,@forc,@volt&@thunderb,@wild c,@fren,@aura&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-normal-poison", "groudon,garchomp,mewtwo,metagross,excadrill,espeon,rhyperior,alakazam&@mud s,@conf,@psycho c,@zen h,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@scor,@futu&!metagross,@psychic&!excadrill,@scor");
        expansions.put("raid-normal-psychic", "heracross,volcarona,tyranitar,hydreigon,pinsir,vikavolt,kleavor,kingambit&@fury,@bug bi,@bite,@snar&@megah,@bug bu,@brut,@dark p,@x-sc,@foul&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        expansions.put("raid-normal-rock", "lucario,kyogre,inteleon,sceptile,rillaboom,groudon,garchomp&@forc,@coun,@waterf,@water g,@fury,@razo,@mud s&@aura,@orig,@hydro c,@fren,@leaf b,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-normal-steel", "lucario,charizard,volcarona,groudon,garchomp,blaziken,chandelure,excadrill&@forc,@coun,@fire s,@mud s,@mud-s&@aura,@blas,@over,@prec,@earthq,@earth p,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-normal-water", "mewtwo,electivire,sceptile,rillaboom,lucario,magnezone&@psycho c,@thunder s,@fury,@razo,@forc,@volt&@thunderb,@wild c,@fren,@aura&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-poison-psychic", "groudon,garchomp,necrozma,gholdengo,tyranitar,hydreigon,rhyperior,mewtwo&@mud s,@psycho c,@shadow c,@hex,@bite,@mud-s&@prec,@earthq,@earth p,@moong,@shadow ba,@brut&!mewtwo,shadow,megaevolve,mega");
        expansions.put("raid-poison-rock", "groudon,garchomp,kyogre,inteleon,mewtwo,metagross,zacian,quaquaval,espeon,alakazam&@mud s,@waterf,@water g,@conf,@psycho c,@zen h,@meta&@prec,@earth p,@orig,@hydro c,@psyst,@psychic,@behemoth bl,@futu");
        expansions.put("raid-poison-steel", "groudon,garchomp,charizard,volcarona,excadrill,chandelure,rhyperior,cinderace,landorus&@mud s,@fire s,@mud-s&@prec,@earthq,@earth p,@blas,@over,@scor,@sands&!charizard,megaevolve,mega&!excadrill,@scor");
        expansions.put("raid-poison-water", "mewtwo,electivire,groudon,garchomp,metagross,magnezone,espeon,alakazam,zekrom&@psycho c,@conf,@thunder s,@mud s,@zen h,@volt,@charg&@thunderb,@psyst,@psychic,@wild c,@prec,@earth p&!electivire,@wild c");
        expansions.put("raid-psychic-rock", "kyogre,inteleon,sceptile,rillaboom,groudon,garchomp,volcarona&@waterf,@water g,@fury,@razo,@mud s,@bug bi&@orig,@hydro c,@fren,@leaf b,@prec,@earth p,@bug bu&!sceptile,megaevolve,mega&!rillaboom,@fren");
        expansions.put("raid-psychic-steel", "charizard,volcarona,groudon,garchomp,necrozma,gholdengo,hydreigon,chandelure&@fire s,@mud s,@psycho c,@hex,@bite&@blas,@over,@prec,@earthq,@earth p,@moong,@shadow ba,@brut&!charizard,megaevolve,mega");
        expansions.put("raid-psychic-water", "mewtwo,electivire,sceptile,rillaboom,volcarona&@psycho c,@thunder s,@fury,@razo,@bug bi&@thunderb,@wild c,@fren,@leaf b,@bug bu&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        expansions.put("raid-rock-steel", "lucario,groudon,garchomp,kyogre,inteleon,blaziken,excadrill&@forc,@coun,@mud s,@waterf,@water g,@mud-s&@aura,@prec,@earthq,@earth p,@orig,@hydro p,@hydro c,@scor&!inteleon,@hydro c&!excadrill,@scor");
        expansions.put("raid-rock-water", "sceptile,rillaboom,mewtwo,electivire,lucario&@fury,@razo,@psycho c,@thunder s,@forc,@coun&@fren,@leaf b,@thunderb,@wild c,@aura&!sceptile,megaevolve,mega&!mewtwo,megaevolve,mega&!electivire,@wild c");
        expansions.put("raid-steel-water", "mewtwo,electivire,lucario,groudon,garchomp,zeraora&@psycho c,@thunder s,@forc,@coun,@mud s,@volt&@thunderb,@wild c,@aura,@prec,@earth p,@plas&!mewtwo,megaevolve,mega&!electivire,@wild c&!zeraora,@plas");
        expansions.put("atk-normal", "regigigas,mewtwo,zacian,lopunny,porygon-z,meloetta&@hidd,@psycho c,@meta,@poun,@lock,@quic&@crus,@giga i,@hyper b&!mewtwo,shadow,megaevolve,mega&!zacian,@meta&!lopunny,megaevolve,mega");
        expansions.put("attkr-normal", "regigigas,mewtwo,zacian,lopunny,porygon-z,meloetta&@normal");
        expansions.put("atk-fire", "charizard,volcarona,chandelure,reshiram,cinderace,blaziken,heatran&@fire s,@fire f&@blas,@over,@fusion f,@magm&!charizard,megaevolve,mega&!blaziken,megaevolve,mega&!heatran,shadow");
        expansions.put("attkr-fire", "charizard,volcarona,chandelure,reshiram,cinderace,blaziken,blacephalon,heatran,moltres,delphox&@fire");
        expansions.put("atk-water", "kyogre,inteleon,quaquaval,swampert,primarina,blastoise&@waterf,@water g,@mud s&@orig,@hydro p,@hydro c&!inteleon,@hydro c&!swampert,shadow,megaevolve,mega&!blastoise,megaevolve,mega");
        expansions.put("attkr-water", "kyogre,inteleon,quaquaval,swampert,primarina,blastoise,gyarados,feraligatr,kingler,samurott&@water");
        expansions.put("atk-electric", "mewtwo,electivire,zeraora,magnezone,luxray&@psycho c,@thunder s,@volt,@spar&@thunderb,@wild c,@plas&!mewtwo,megaevolve,mega&!electivire,@wild c&!zeraora,@volt&!zeraora,@plas&!magnezone,@volt");
        expansions.put("attkr-electric", "mewtwo,electivire,zeraora,magnezone,raikou,luxray,zekrom,xurkitree,manectric,thundurus,zapdos&@electric");
        expansions.put("atk-grass", "sceptile,rillaboom,meowscarada,venusaur,roserade&@fury,@razo,@leafa,@vine,@magi&@fren,@leaf b,@grass k,@sola&!sceptile,megaevolve,mega&!venusaur,@vine&!venusaur,shadow,megaevolve,mega&!roserade,@magi");
        expansions.put("attkr-grass", "sceptile,rillaboom,meowscarada,venusaur,roserade,kartana,chesnaught,zarude,shaymin,tangrowth,victreebel&@grass");
        expansions.put("atk-ice", "kyurem,baxcalibur,mamoswine,mewtwo,darmanitan,gardevoir&@ice f,@dragon t,@powd,@psycho c,@charm&@ice bu,@free,@aval,@ice be,@trip&!mewtwo,shadow,megaevolve,mega&!gardevoir,megaevolve,mega");
        expansions.put("attkr-ice", "kyurem,baxcalibur,mamoswine,mewtwo,darmanitan,gardevoir,zamazenta,kyogre&@ice");
        expansions.put("atk-fighting", "lucario,blaziken,conkeldurr,keldeo,mewtwo,terrakion&@forc,@coun,@low k,@psycho c,@double k&@aura,@dynami,@secr,@focu,@sacred s&!blaziken,@aura&!conkeldurr,@dynami&!mewtwo,megaevolve,mega");
        expansions.put("attkr-fighting", "lucario,blaziken,conkeldurr,keldeo,mewtwo,heracross,terrakion&@fighting");
        expansions.put("atk-poison", "eternatus,roserade,gengar,overqwil,revavroom,beedrill,nihilego,naganadel&@poison j,@lick,@shadow c&@sludge b,@gunk&!gengar,megaevolve,mega&!beedrill,megaevolve,mega&!nihilego,@sludge b");
        expansions.put("attkr-poison", "eternatus,roserade,gengar,overqwil,revavroom,beedrill,nihilego,victreebel,naganadel,scolipede&@poison");
        expansions.put("atk-ground", "groudon,garchomp,excadrill,rhyperior,landorus,swampert&@mud s,@mud-s&@prec,@earthq,@earth p,@scor,@drill r,@sands&!excadrill,@scor&!swampert,megaevolve,mega");
        expansions.put("attkr-ground", "groudon,garchomp,excadrill,rhyperior,landorus,swampert&@ground");
        expansions.put("atk-flying", "rayquaza,salamence,moltres,toucannon,staraptor,enamorus,charizard,yveltal,articuno&@air s,@fire f,@wing,@peck,@gust,@fairy w,@psycho c&@dragon a,@fly,@beak,@blas,@obli,@hurr&!charizard,megaevolve,mega");
        expansions.put("attkr-flying", "rayquaza,salamence,moltres,toucannon,staraptor,enamorus,charizard,yveltal,articuno&@flying");
        expansions.put("atk-psychic", "mewtwo,metagross,espeon,alakazam,latios,gardevoir,gallade&@conf,@psycho c,@zen h&@psyst,@psychic,@futu&!latios,shadow,megaevolve,mega&!gardevoir,megaevolve,mega&!gallade,megaevolve,mega");
        expansions.put("attkr-psychic", "mewtwo,metagross,espeon,alakazam,latios,gardevoir,gallade&@psychic");
        expansions.put("atk-bug", "heracross,volcarona,pinsir,vikavolt,kleavor,scizor&@fury,@bug bi&@megah,@bug bu,@x-sc&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega&!scizor,shadow,megaevolve,mega");
        expansions.put("attkr-bug", "heracross,volcarona,pinsir,vikavolt,kleavor,scizor,beedrill,escavalier,metagross&@bug");
        expansions.put("atk-rock", "diancie,rhyperior,glimmora,rampardos,tyranitar,aerodactyl&@rock th,@smac&@rock sl,@rock w,@ston,@meteor b&!diancie,megaevolve,mega&!tyranitar,shadow,megaevolve,mega&!aerodactyl,megaevolve,mega");
        expansions.put("attkr-rock", "diancie,rhyperior,glimmora,rampardos,tyranitar,aerodactyl,gigalith,tyrantrum,rayquaza&@rock");
        expansions.put("atk-ghost", "necrozma,gholdengo,dragapult,mewtwo,chandelure,darkrai&@psycho c,@shadow c,@hex,@asto,@snar&@moong,@shadow ba&!gholdengo,@hex&!dragapult,@asto&!mewtwo,shadow,megaevolve,mega&!darkrai,shadow");
        expansions.put("attkr-ghost", "necrozma,gholdengo,dragapult,mewtwo,chandelure,gengar,darkrai,banette,lunala,kyurem&@ghost");
        expansions.put("atk-dragon", "rayquaza,baxcalibur,haxorus,eternatus,garchomp,kyurem&@dragon t,@ice f,@dragon b&@brea,@outr,@glai,@dynama,@free,@ice bu&!rayquaza,megaevolve,mega&!baxcalibur,@ice f&!baxcalibur,@glai&!garchomp,@brea");
        expansions.put("attkr-dragon", "rayquaza,baxcalibur,haxorus,eternatus,garchomp,kyurem,salamence,dialga,dragonite&@dragon");
        expansions.put("atk-dark", "tyranitar,hydreigon,kingambit,darkrai,absol,gengar,houndoom&@bite,@snar,@suck&@brut,@dark p,@foul,@shadow ba&!absol,shadow,megaevolve,mega&!gengar,megaevolve,mega&!houndoom,megaevolve,mega");
        expansions.put("attkr-dark", "tyranitar,hydreigon,kingambit,darkrai,absol,gengar,houndoom,salamence&@dark");
        expansions.put("atk-steel", "zacian,metagross,zamazenta,tinkaton,necrozma,lucario&@meta,@bullet p,@fairy w,@forc&@behemoth bl,@meteor m,@behemoth ba,@gigat,@suns,@moong");
        expansions.put("attkr-steel", "zacian,metagross,zamazenta,tinkaton,necrozma,lucario,dialga,excadrill&@steel");
        expansions.put("atk-fairy", "gardevoir,zacian,togekiss,enamorus,hatterene,alakazam,tapu lele,xerneas&@charm,@meta,@fairy w,@psycho c,@asto,@geom&@dazz,@play,@natu,@moonb&!enamorus,@fairy w&!alakazam,megaevolve,mega");
        expansions.put("attkr-fairy", "gardevoir,zacian,togekiss,enamorus,hatterene,alakazam,tapu lele,xerneas,tapu koko,tapu bulu,latias&@fairy");
        cpx.put("raid-fire", "kyogre,inteleon,groudon,garchomp,diancie,rhyperior&@waterf,@water g,@mud s,@rock th,@smac&@orig,@hydro p,@hydro c,@prec,@earth p,@rock sl,@rock w&!inteleon,@hydro c&!diancie,megaevolve,mega");
        cpx.put("raid-water", "mewtwo,electivire,sceptile,rillaboom,magnezone&@psycho c,@thunder s,@fury,@razo,@volt&@thunderb,@wild c,@fren,@leaf b&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-grass", "charizard,volcarona,kyurem,baxcalibur,eternatus,roserade,rayquaza&@fire s,@air s,@bug bi,@ice f,@poison j&@blas,@over,@bug bu,@ice bu,@aval,@sludge b,@dragon a&!charizard,megaevolve,mega");
        cpx.put("raid-ice", "charizard,volcarona,lucario,diancie,rhyperior,zacian&@fire s,@forc,@rock th,@smac,@meta&@blas,@over,@aura,@rock sl,@rock w,@behemoth bl&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-fighting", "rayquaza,salamence,mewtwo,metagross,gardevoir,moltres,toucannon,espeon,staraptor&@air s,@fire f,@conf,@psycho c,@zen h,@charm,@wing,@peck,@gust&@dragon a,@fly,@psyst,@psychic,@dazz,@beak");
        cpx.put("raid-poison", "groudon,garchomp,mewtwo,metagross,excadrill,espeon,rhyperior,alakazam&@mud s,@conf,@psycho c,@zen h,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@scor&!metagross,@psychic&!excadrill,@scor");
        cpx.put("raid-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,quaquaval&@waterf,@water g,@fury,@razo,@ice f&@orig,@hydro p,@aval,@hydro c,@fren,@ice bu&!inteleon,@hydro c&!sceptile,megaevolve,mega");
        cpx.put("raid-psychic", "heracross,volcarona,necrozma,gholdengo,tyranitar,kleavor,chandelure,darkrai&@fury,@bug bi,@psycho c,@hex,@bite,@snar&@megah,@bug bu,@moong,@shadow ba,@brut,@x-sc&!heracross,megaevolve,mega");
        cpx.put("raid-bug", "charizard,volcarona,rayquaza,salamence,diancie,rhyperior&@fire s,@air s,@fire f,@rock th,@smac&@blas,@over,@dragon a,@fly,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-rock", "kyogre,inteleon,sceptile,rillaboom,lucario,groudon,garchomp&@waterf,@water g,@fury,@razo,@forc,@coun,@mud s&@orig,@hydro c,@fren,@aura,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-ghost", "necrozma,gholdengo,tyranitar,hydreigon,dragapult,mewtwo,chandelure&@psycho c,@shadow c,@hex,@bite,@asto&@moong,@shadow ba,@brut&!gholdengo,@hex&!dragapult,@asto&!mewtwo,shadow,megaevolve,mega");
        cpx.put("raid-dragon", "kyurem,baxcalibur,rayquaza,gardevoir,mamoswine,haxorus,togekiss&@ice f,@dragon t,@charm,@powd&@ice bu,@free,@aval,@glai,@brea,@outr,@dazz&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        cpx.put("raid-dark", "lucario,heracross,volcarona,gardevoir,blaziken,pinsir,vikavolt&@forc,@coun,@fury,@bug bi,@charm&@aura,@megah,@bug bu,@dazz,@x-sc&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        cpx.put("raid-steel", "charizard,volcarona,lucario,groudon,garchomp,chandelure,blaziken,reshiram&@fire s,@forc,@coun,@mud s,@fire f&@blas,@over,@aura,@prec,@earthq,@earth p,@fusion f&!charizard,megaevolve,mega");
        cpx.put("raid-fairy", "eternatus,roserade,zacian,metagross,gengar,overqwil,zamazenta,tinkaton&@poison j,@meta,@bullet p,@lick,@fairy w&@sludge b,@behemoth bl,@meteor m,@behemoth ba,@gigat&!gengar,megaevolve,mega");
        cpx.put("raid-bug-dragon", "kyurem,baxcalibur,rayquaza,salamence,diancie,rhyperior&@ice f,@dragon t,@air s,@fire f,@rock th,@smac&@ice bu,@free,@aval,@glai,@dragon a,@fly,@rock sl,@rock w,@ston&!diancie,megaevolve,mega");
        cpx.put("raid-bug-electric", "charizard,volcarona,diancie,rhyperior,chandelure,glimmora,cinderace&@fire s,@rock th,@smac&@blas,@over,@rock sl,@rock w,@ston,@meteor b&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-bug-fairy", "charizard,volcarona,eternatus,roserade,rayquaza,salamence,rhyperior,chandelure&@fire s,@air s,@poison j,@fire f,@smac&@blas,@over,@sludge b,@dragon a,@fly,@rock w&!charizard,megaevolve,mega");
        cpx.put("raid-bug-fighting", "rayquaza,salamence,charizard,volcarona,mewtwo,gardevoir,staraptor&@air s,@fire f,@fire s,@conf,@psycho c,@charm,@gust,@wing&@dragon a,@fly,@blas,@over,@psyst,@dazz&!charizard,megaevolve,mega");
        cpx.put("raid-bug-fire", "diancie,rhyperior,kyogre,inteleon,rayquaza,salamence,glimmora&@rock th,@smac,@waterf,@water g,@air s,@fire f&@rock sl,@rock w,@orig,@hydro c,@dragon a,@fly,@meteor b&!diancie,megaevolve,mega");
        cpx.put("raid-bug-flying", "diancie,rhyperior,charizard,volcarona,mewtwo&@rock th,@smac,@fire s,@psycho c&@rock sl,@rock w,@blas,@over,@thunderb&!diancie,megaevolve,mega&!charizard,megaevolve,mega&!mewtwo,megaevolve,mega");
        cpx.put("raid-bug-ghost", "charizard,volcarona,rayquaza,salamence,diancie,rhyperior&@fire s,@air s,@fire f,@rock th,@smac&@blas,@over,@dragon a,@fly,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-bug-grass", "charizard,volcarona,rayquaza,salamence,kyurem,baxcalibur,eternatus&@fire s,@air s,@fire f,@ice f,@poison j&@blas,@over,@dragon a,@fly,@ice bu,@aval,@sludge b&!charizard,megaevolve,mega");
        cpx.put("raid-bug-ground", "charizard,volcarona,kyogre,inteleon,kyurem,baxcalibur,rayquaza,chandelure&@fire s,@air s,@waterf,@water g,@ice f&@blas,@over,@orig,@aval,@hydro c,@ice bu,@dragon a&!charizard,megaevolve,mega");
        cpx.put("raid-bug-ice", "charizard,volcarona,diancie,rhyperior,rayquaza,salamence&@fire s,@air s,@rock th,@smac,@fire f&@blas,@over,@rock sl,@rock w,@dragon a,@fly&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-bug-normal", "charizard,volcarona,rayquaza,salamence,diancie,rhyperior&@fire s,@air s,@fire f,@rock th,@smac&@blas,@over,@dragon a,@fly,@rock sl,@rock w&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-bug-poison", "charizard,volcarona,rayquaza,salamence,mewtwo,rhyperior,reshiram&@fire s,@air s,@fire f,@conf,@psycho c,@smac&@blas,@over,@dragon a,@fly,@psyst,@rock w,@fusion f&!charizard,megaevolve,mega");
        cpx.put("raid-bug-rock", "kyogre,inteleon,diancie,rhyperior,zacian,metagross,quaquaval&@waterf,@water g,@rock th,@smac,@meta,@bullet p&@orig,@hydro c,@rock sl,@rock w,@behemoth bl,@meteor m&!diancie,megaevolve,mega");
        cpx.put("raid-bug-water", "mewtwo,electivire,rayquaza,salamence,rhyperior,magnezone&@psycho c,@thunder s,@air s,@fire f,@smac,@volt&@thunderb,@wild c,@dragon a,@fly,@rock w&!mewtwo,megaevolve,mega&!electivire,@wild c");
        cpx.put("raid-dark-dragon", "gardevoir,kyurem,baxcalibur,lucario,heracross,volcarona,zacian,togekiss&@charm,@ice f,@forc,@fury,@bug bi,@meta&@dazz,@ice bu,@aval,@glai,@aura,@megah,@bug bu,@play&!heracross,megaevolve,mega");
        cpx.put("raid-dark-electric", "lucario,groudon,garchomp,heracross,volcarona,gardevoir,blaziken,vikavolt&@forc,@coun,@mud s,@fury,@bug bi,@charm&@aura,@prec,@earth p,@megah,@bug bu,@dazz,@x-sc&!heracross,megaevolve,mega");
        cpx.put("raid-dark-fairy", "eternatus,roserade,zacian,metagross,gardevoir,gengar&@poison j,@meta,@bullet p,@charm,@lick&@sludge b,@behemoth bl,@meteor m,@dazz&!roserade,@sludge b&!gengar,@sludge b&!gengar,megaevolve,mega");
        cpx.put("raid-dark-fighting", "gardevoir,lucario,rayquaza,salamence,zacian,togekiss,blaziken,moltres,hatterene&@charm,@forc,@coun,@air s,@fire f,@meta,@wing&@dazz,@aura,@dragon a,@fly,@play&!zacian,@meta&!togekiss,@charm");
        cpx.put("raid-dark-fire", "kyogre,inteleon,lucario,groudon,garchomp,diancie,rhyperior,quaquaval&@waterf,@water g,@forc,@mud s,@rock th,@smac&@orig,@hydro c,@aura,@prec,@earth p,@rock sl,@rock w&!diancie,megaevolve,mega");
        cpx.put("raid-dark-grass", "heracross,volcarona,charizard,kyurem,baxcalibur,lucario&@fury,@bug bi,@fire s,@ice f,@forc&@megah,@bug bu,@over,@blas,@ice bu,@aval,@aura&!heracross,megaevolve,mega&!charizard,megaevolve,mega");
        cpx.put("raid-dark-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,lucario,gardevoir&@waterf,@water g,@fury,@razo,@ice f,@forc,@charm&@orig,@aval,@hydro c,@fren,@ice bu,@aura,@dazz&!sceptile,megaevolve,mega");
        cpx.put("raid-dark-ice", "lucario,charizard,volcarona,heracross,rhyperior,blaziken&@forc,@coun,@fire s,@bug bi,@fury,@smac&@aura,@blas,@over,@bug bu,@megah,@rock w&!charizard,megaevolve,mega&!heracross,megaevolve,mega");
        cpx.put("raid-dark-normal", "lucario,heracross,volcarona,gardevoir,blaziken,pinsir,vikavolt&@forc,@coun,@fury,@bug bi,@charm&@aura,@megah,@bug bu,@dazz,@x-sc&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        cpx.put("raid-dark-psychic", "heracross,volcarona,gardevoir,pinsir,vikavolt,zacian,togekiss,kleavor&@fury,@bug bi,@charm,@meta&@megah,@bug bu,@dazz,@x-sc,@play&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        cpx.put("raid-dark-rock", "lucario,kyogre,inteleon,sceptile,rillaboom,groudon,garchomp&@forc,@coun,@waterf,@water g,@fury,@razo,@mud s&@aura,@orig,@hydro c,@fren,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-dark-steel", "lucario,charizard,volcarona,groudon,garchomp,blaziken,chandelure,conkeldurr,cinderace&@forc,@coun,@fire s,@mud s&@aura,@blas,@over,@prec,@earthq,@earth p,@dynami&!charizard,megaevolve,mega");
        cpx.put("raid-dark-water", "mewtwo,electivire,sceptile,rillaboom,lucario&@psycho c,@thunder s,@fury,@razo,@forc&@thunderb,@wild c,@fren,@leaf b,@aura&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-dragon-electric", "kyurem,baxcalibur,groudon,garchomp,rayquaza&@ice f,@dragon t,@mud s&@ice bu,@free,@aval,@glai,@prec,@earthq,@earth p,@brea&!baxcalibur,@aval,@glai&!groudon,@mud s&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-fairy", "kyurem,baxcalibur,eternatus,roserade,zacian,metagross,gardevoir&@ice f,@dragon t,@poison j,@meta,@bullet p,@charm&@ice bu,@free,@aval,@sludge b,@behemoth bl,@meteor m,@dazz&!roserade,@sludge b");
        cpx.put("raid-dragon-fighting", "gardevoir,kyurem,baxcalibur,rayquaza,salamence,mewtwo,metagross,espeon&@charm,@conf,@ice f,@air s,@fire f,@psycho c,@zen h&@dazz,@psychic,@ice bu,@aval,@glai,@dragon a,@fly,@psyst,@ice be");
        cpx.put("raid-dragon-fire", "groudon,garchomp,diancie,rhyperior,rayquaza&@mud s,@dragon t,@rock th,@smac&@prec,@earthq,@earth p,@brea,@rock sl,@rock w&!groudon,@mud s&!diancie,megaevolve,mega&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-flying", "kyurem,baxcalibur,diancie,rhyperior,rayquaza&@ice f,@dragon t,@rock th,@smac&@ice bu,@free,@aval,@rock sl,@rock w,@brea&!baxcalibur,@aval&!diancie,megaevolve,mega&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-ghost", "kyurem,baxcalibur,necrozma,gholdengo,rayquaza&@ice f,@dragon t,@shadow c,@psycho c,@hex&@ice bu,@free,@aval,@glai,@moong,@shadow ba,@brea&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-grass", "kyurem,baxcalibur,eternatus,roserade,rayquaza,salamence,heracross,overqwil&@ice f,@poison j,@air s,@fire f,@fury&@ice bu,@aval,@glai,@sludge b,@dragon a,@fly,@megah&!heracross,megaevolve,mega");
        cpx.put("raid-dragon-ground", "kyurem,baxcalibur,rayquaza,gardevoir,mamoswine,haxorus,togekiss&@ice f,@dragon t,@charm,@powd&@ice bu,@free,@aval,@glai,@brea,@outr,@dazz&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-ice", "lucario,diancie,rhyperior,rayquaza,baxcalibur&@forc,@rock th,@smac,@dragon t,@ice f&@aura,@rock sl,@rock w,@brea,@glai&!diancie,megaevolve,mega&!rayquaza,megaevolve,mega&!baxcalibur,@glai");
        cpx.put("raid-dragon-normal", "kyurem,baxcalibur,lucario,rayquaza,gardevoir,mamoswine&@ice f,@dragon t,@forc,@coun,@charm,@powd&@ice bu,@free,@aval,@glai,@aura,@brea,@dazz&!baxcalibur,@aval,@glai&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-poison", "kyurem,baxcalibur,groudon,garchomp,mewtwo,metagross,espeon&@ice f,@dragon t,@mud s,@conf,@psycho c,@zen h&@ice bu,@free,@aval,@glai,@prec,@earth p,@psyst,@ice be,@psychic&!groudon,@mud s");
        cpx.put("raid-dragon-psychic", "kyurem,baxcalibur,heracross,volcarona,necrozma,gholdengo&@ice f,@dragon t,@fury,@bug bi,@psycho c,@hex&@ice bu,@free,@aval,@glai,@megah,@bug bu,@moong,@shadow ba&!heracross,megaevolve,mega");
        cpx.put("raid-dragon-rock", "kyurem,baxcalibur,lucario,groudon,garchomp,rayquaza&@ice f,@dragon t,@forc,@mud s&@ice bu,@free,@aval,@aura,@prec,@earth p,@brea&!baxcalibur,@aval&!groudon,@mud s&!rayquaza,megaevolve,mega");
        cpx.put("raid-dragon-steel", "lucario,groudon,garchomp,blaziken,excadrill,conkeldurr,rhyperior,keldeo,landorus&@forc,@coun,@mud s,@mud-s,@low k&@aura,@prec,@earthq,@earth p,@scor,@dynami,@secr,@sands&!excadrill,@scor");
        cpx.put("raid-dragon-water", "rayquaza,baxcalibur,gardevoir,haxorus,zacian,togekiss,eternatus,hatterene&@dragon t,@ice f,@charm,@meta&@brea,@outr,@glai,@dazz,@play,@dynama&!rayquaza,megaevolve,mega&!baxcalibur,@glai");
        cpx.put("raid-electric-fairy", "eternatus,roserade,groudon,garchomp,gengar,overqwil,excadrill,rhyperior&@poison j,@mud s,@lick,@shadow c,@mud-s&@sludge b,@prec,@earthq,@earth p,@scor&!gengar,megaevolve,mega&!excadrill,@scor");
        cpx.put("raid-electric-fighting", "groudon,garchomp,mewtwo,metagross,gardevoir,espeon,zacian,togekiss,alakazam&@mud s,@conf,@psycho c,@zen h,@charm,@meta&@prec,@earthq,@earth p,@psyst,@psychic,@dazz,@play&!metagross,@psychic");
        cpx.put("raid-electric-fire", "groudon,garchomp,kyogre,inteleon,diancie,rhyperior,quaquaval&@mud s,@waterf,@water g,@rock th,@smac,@mud-s&@prec,@earthq,@earth p,@orig,@hydro c,@rock sl,@rock w,@ston&!diancie,megaevolve,mega");
        cpx.put("raid-electric-flying", "kyurem,baxcalibur,diancie,rhyperior,mamoswine,glimmora,rampardos&@ice f,@dragon t,@rock th,@smac,@powd&@ice bu,@free,@aval,@rock sl,@rock w,@ston,@meteor b&!diancie,megaevolve,mega");
        cpx.put("raid-electric-ghost", "groudon,garchomp,necrozma,gholdengo,tyranitar,hydreigon,rhyperior,chandelure,darkrai&@mud s,@psycho c,@shadow c,@hex,@bite,@mud-s,@snar&@prec,@earthq,@earth p,@moong,@shadow ba,@brut,@dark p");
        cpx.put("raid-electric-grass", "charizard,volcarona,kyurem,baxcalibur,eternatus,roserade,chandelure&@fire s,@bug bi,@ice f,@dragon t,@poison j&@blas,@over,@bug bu,@ice bu,@free,@aval,@sludge b&!charizard,megaevolve,mega");
        cpx.put("raid-electric-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,groudon,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@mud s&@orig,@aval,@hydro c,@fren,@leaf b,@ice bu,@prec&!sceptile,megaevolve,mega");
        cpx.put("raid-electric-ice", "charizard,volcarona,lucario,groudon,garchomp,diancie,chandelure&@fire s,@forc,@mud s,@rock th&@blas,@over,@aura,@prec,@earth p,@rock sl&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-electric-normal", "lucario,groudon,garchomp,blaziken,excadrill,conkeldurr,rhyperior,keldeo,landorus&@forc,@coun,@mud s,@mud-s,@low k&@aura,@prec,@earthq,@earth p,@scor,@dynami,@secr,@sands&!excadrill,@scor");
        cpx.put("raid-electric-poison", "groudon,garchomp,mewtwo,metagross,excadrill,espeon,rhyperior,alakazam&@mud s,@conf,@psycho c,@zen h,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@scor&!metagross,@psychic&!excadrill,@scor");
        cpx.put("raid-electric-psychic", "groudon,garchomp,heracross,volcarona,necrozma,gholdengo,hydreigon&@mud s,@fury,@bug bi,@psycho c,@hex,@bite&@prec,@earth p,@megah,@bug bu,@moong,@shadow ba,@brut&!heracross,megaevolve,mega");
        cpx.put("raid-electric-rock", "groudon,garchomp,kyogre,inteleon,sceptile,rillaboom,lucario&@mud s,@waterf,@water g,@fury,@razo,@forc,@coun&@prec,@earth p,@orig,@hydro c,@fren,@aura&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-electric-steel", "groudon,garchomp,charizard,volcarona,lucario,excadrill,chandelure&@mud s,@fire s,@forc,@coun,@mud-s&@prec,@earthq,@earth p,@blas,@over,@aura,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        cpx.put("raid-electric-water", "sceptile,rillaboom,groudon,garchomp,meowscarada,kartana&@fury,@razo,@mud s,@leafa&@fren,@leaf b,@prec,@earthq,@earth p&!sceptile,@fren,@leaf b&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-fairy-fighting", "eternatus,roserade,rayquaza,salamence,mewtwo,metagross,overqwil,espeon,alakazam&@poison j,@air s,@fire f,@conf,@psycho c,@zen h,@bullet p&@sludge b,@dragon a,@fly,@psyst,@psychic,@meteor m");
        cpx.put("raid-fairy-fire", "kyogre,inteleon,eternatus,roserade,groudon,garchomp,diancie,quaquaval&@waterf,@water g,@poison j,@mud s,@rock th&@orig,@hydro c,@sludge b,@prec,@earth p,@rock sl&!diancie,megaevolve,mega");
        cpx.put("raid-fairy-flying", "mewtwo,electivire,kyurem,baxcalibur,eternatus,roserade,overqwil&@psycho c,@thunder s,@ice f,@poison j&@thunderb,@wild c,@ice bu,@aval,@sludge b&!mewtwo,megaevolve,mega&!electivire,@wild c");
        cpx.put("raid-fairy-ghost", "necrozma,gholdengo,zacian,metagross,dragapult,zamazenta&@psycho c,@shadow c,@meta,@hex,@bullet p,@asto&@moong,@shadow ba,@behemoth bl,@meteor m,@behemoth ba&!gholdengo,@hex&!dragapult,@asto");
        cpx.put("raid-fairy-grass", "eternatus,roserade,charizard,volcarona,kyurem,baxcalibur,rayquaza,overqwil,chandelure&@poison j,@fire s,@air s,@ice f&@sludge b,@blas,@over,@ice bu,@aval,@dragon a&!charizard,megaevolve,mega");
        cpx.put("raid-fairy-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,zacian,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@meta&@orig,@aval,@hydro c,@fren,@ice bu,@behemoth bl&!sceptile,megaevolve,mega");
        cpx.put("raid-fairy-ice", "zacian,metagross,charizard,volcarona,eternatus,roserade,rhyperior&@meta,@bullet p,@fire s,@poison j,@smac&@behemoth bl,@meteor m,@blas,@over,@sludge b,@rock w,@ston&!charizard,megaevolve,mega");
        cpx.put("raid-fairy-normal", "eternatus,roserade,zacian,metagross,gengar,overqwil,zamazenta,tinkaton&@poison j,@meta,@bullet p,@lick,@fairy w&@sludge b,@behemoth bl,@meteor m,@behemoth ba,@gigat&!gengar,megaevolve,mega");
        cpx.put("raid-fairy-poison", "groudon,garchomp,mewtwo,metagross,zacian,espeon&@mud s,@conf,@psycho c,@zen h,@bullet p,@meta&@prec,@earthq,@earth p,@psyst,@psychic,@meteor m,@behemoth bl&!metagross,@psychic,@meteor m");
        cpx.put("raid-fairy-psychic", "eternatus,roserade,necrozma,gholdengo,zacian,metagross,overqwil,zamazenta&@poison j,@psycho c,@shadow c,@meta,@hex,@bullet p&@sludge b,@moong,@shadow ba,@behemoth bl,@meteor m,@behemoth ba");
        cpx.put("raid-fairy-rock", "zacian,metagross,kyogre,inteleon,sceptile,rillaboom,groudon&@meta,@bullet p,@waterf,@water g,@fury,@razo,@mud s&@behemoth bl,@meteor m,@orig,@hydro c,@fren,@prec&!sceptile,megaevolve,mega");
        cpx.put("raid-fairy-steel", "charizard,volcarona,groudon,garchomp,chandelure,excadrill,reshiram&@fire s,@mud s,@mud-s,@fire f&@blas,@over,@prec,@earthq,@earth p,@scor,@fusion f&!charizard,megaevolve,mega&!excadrill,@scor");
        cpx.put("raid-fairy-water", "mewtwo,electivire,sceptile,rillaboom,magnezone&@psycho c,@thunder s,@fury,@razo,@volt&@thunderb,@wild c,@fren,@leaf b&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-fighting-fire", "kyogre,inteleon,groudon,garchomp,rayquaza,salamence,mewtwo,quaquaval,espeon&@waterf,@water g,@mud s,@air s,@fire f,@conf,@psycho c&@orig,@hydro c,@prec,@earth p,@dragon a,@fly,@psyst,@psychic");
        cpx.put("raid-fighting-flying", "mewtwo,electivire,kyurem,baxcalibur,rayquaza,salamence,luxray&@psycho c,@thunder s,@ice f,@air s,@fire f,@spar&@thunderb,@psyst,@ice be,@wild c,@ice bu,@aval,@dragon a,@fly&!electivire,@wild c");
        cpx.put("raid-fighting-ghost", "rayquaza,salamence,mewtwo,metagross,necrozma,gholdengo,moltres,espeon,chandelure&@air s,@fire f,@conf,@psycho c,@zen h,@shadow c,@hex,@wing&@dragon a,@fly,@psyst,@psychic,@shadow ba,@moong");
        cpx.put("raid-fighting-grass", "rayquaza,salamence,charizard,volcarona,kyurem,baxcalibur,eternatus&@air s,@fire f,@fire s,@ice f,@poison j&@dragon a,@fly,@blas,@over,@ice bu,@aval,@sludge b&!charizard,megaevolve,mega");
        cpx.put("raid-fighting-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,rayquaza,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@air s&@orig,@aval,@hydro c,@fren,@ice bu,@dragon a&!sceptile,megaevolve,mega");
        cpx.put("raid-fighting-normal", "lucario,rayquaza,salamence,mewtwo,metagross,gardevoir,blaziken,moltres,espeon,alakazam&@forc,@coun,@air s,@fire f,@conf,@psycho c,@zen h,@charm,@wing&@aura,@dragon a,@fly,@psyst,@psychic,@dazz");
        cpx.put("raid-fighting-psychic", "rayquaza,salamence,necrozma,gholdengo,gardevoir,moltres&@air s,@fire f,@psycho c,@shadow c,@hex,@charm,@wing&@dragon a,@fly,@moong,@shadow ba,@dazz&!gholdengo,@shadow ba&!gardevoir,@dazz");
        cpx.put("raid-fighting-rock", "kyogre,inteleon,sceptile,rillaboom,lucario,groudon,garchomp&@waterf,@water g,@fury,@razo,@forc,@coun,@mud s&@orig,@hydro c,@fren,@aura,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-fighting-steel", "charizard,volcarona,lucario,groudon,garchomp,chandelure,blaziken,reshiram&@fire s,@forc,@coun,@mud s,@fire f&@blas,@over,@aura,@prec,@earthq,@earth p,@fusion f&!charizard,megaevolve,mega");
        cpx.put("raid-fighting-water", "mewtwo,electivire,sceptile,rillaboom,rayquaza,zacian&@psycho c,@thunder s,@fury,@razo,@air s,@meta&@thunderb,@psyst,@wild c,@fren,@dragon a,@play&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-fire-flying", "diancie,rhyperior,kyogre,inteleon,mewtwo,glimmora&@rock th,@smac,@waterf,@water g,@psycho c&@rock sl,@rock w,@orig,@hydro c,@thunderb,@meteor b&!diancie,megaevolve,mega&!mewtwo,megaevolve,mega");
        cpx.put("raid-fire-ghost", "kyogre,inteleon,groudon,garchomp,diancie,rhyperior,necrozma&@waterf,@water g,@mud s,@rock th,@smac,@psycho c&@orig,@hydro c,@prec,@earth p,@rock sl,@rock w,@moong&!diancie,megaevolve,mega");
        cpx.put("raid-fire-grass", "eternatus,roserade,rayquaza,salamence,diancie,rhyperior,overqwil,moltres&@poison j,@air s,@fire f,@rock th,@smac,@wing&@sludge b,@dragon a,@fly,@rock sl,@rock w,@ston&!diancie,megaevolve,mega");
        cpx.put("raid-fire-ice", "diancie,rhyperior,kyogre,inteleon,lucario,groudon,garchomp,quaquaval&@rock th,@smac,@waterf,@water g,@forc,@mud s&@rock sl,@rock w,@orig,@hydro c,@aura,@prec,@earth p&!diancie,megaevolve,mega");
        cpx.put("raid-fire-normal", "kyogre,inteleon,lucario,groudon,garchomp,diancie,rhyperior,quaquaval&@waterf,@water g,@forc,@mud s,@rock th,@smac&@orig,@hydro c,@aura,@prec,@earth p,@rock sl,@rock w&!diancie,megaevolve,mega");
        cpx.put("raid-fire-poison", "groudon,garchomp,kyogre,inteleon,mewtwo,metagross,rhyperior,quaquaval,espeon,alakazam&@mud s,@waterf,@water g,@conf,@psycho c,@zen h,@smac&@prec,@earth p,@orig,@hydro c,@psyst,@psychic,@rock w");
        cpx.put("raid-fire-psychic", "kyogre,inteleon,groudon,garchomp,diancie,rhyperior,necrozma&@waterf,@water g,@mud s,@rock th,@smac,@psycho c&@orig,@hydro c,@prec,@earth p,@rock sl,@rock w,@moong&!diancie,megaevolve,mega");
        cpx.put("raid-fire-rock", "kyogre,inteleon,groudon,garchomp,lucario,diancie,rhyperior,quaquaval&@waterf,@water g,@mud s,@forc,@rock th,@smac&@orig,@hydro c,@prec,@earth p,@aura,@rock sl,@rock w&!diancie,megaevolve,mega");
        cpx.put("raid-fire-steel", "groudon,garchomp,kyogre,inteleon,lucario,excadrill,quaquaval&@mud s,@waterf,@water g,@forc,@mud-s&@prec,@earthq,@earth p,@orig,@hydro p,@hydro c,@aura,@scor&!inteleon,@hydro c&!excadrill,@scor");
        cpx.put("raid-fire-water", "mewtwo,electivire,groudon,garchomp,diancie&@psycho c,@thunder s,@mud s,@rock th&@thunderb,@wild c,@prec,@earth p,@rock sl&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        cpx.put("raid-flying-grass", "kyurem,baxcalibur,charizard,volcarona,eternatus,roserade,rayquaza&@ice f,@dragon t,@fire s,@air s,@poison j&@ice bu,@free,@aval,@blas,@over,@sludge b,@dragon a,@anci&!charizard,megaevolve,mega");
        cpx.put("raid-flying-ground", "kyurem,baxcalibur,kyogre,inteleon,mamoswine,quaquaval,darmanitan,primarina&@ice f,@dragon t,@waterf,@water g,@powd&@ice bu,@free,@aval,@orig,@hydro p,@hydro c&!inteleon,@hydro c");
        cpx.put("raid-flying-ice", "diancie,rhyperior,charizard,volcarona,mewtwo&@rock th,@smac,@fire s,@psycho c&@rock sl,@rock w,@blas,@over,@thunderb&!diancie,megaevolve,mega&!charizard,megaevolve,mega&!mewtwo,megaevolve,mega");
        cpx.put("raid-flying-poison", "mewtwo,electivire,kyurem,baxcalibur,metagross,espeon,rhyperior&@psycho c,@conf,@thunder s,@ice f,@zen h,@smac&@thunderb,@psyst,@psychic,@wild c,@ice bu,@aval,@rock w,@ston&!electivire,@wild c");
        cpx.put("raid-flying-rock", "kyogre,inteleon,mewtwo,electivire,kyurem,baxcalibur&@waterf,@water g,@psycho c,@thunder s,@ice f&@orig,@aval,@hydro c,@thunderb,@wild c,@ice bu&!mewtwo,megaevolve,mega&!electivire,@wild c");
        cpx.put("raid-flying-steel", "charizard,volcarona,mewtwo,electivire,chandelure,cinderace&@fire s,@psycho c,@thunder s&@blas,@over,@thunderb,@wild c&!charizard,megaevolve,mega&!mewtwo,megaevolve,mega&!electivire,@wild c");
        cpx.put("raid-flying-water", "mewtwo,electivire,diancie,rhyperior,magnezone&@psycho c,@thunder s,@rock th,@smac,@volt&@thunderb,@wild c,@rock sl,@rock w&!mewtwo,megaevolve,mega&!electivire,@wild c&!diancie,megaevolve,mega");
        cpx.put("raid-ghost-grass", "charizard,volcarona,kyurem,baxcalibur,rayquaza,salamence,necrozma,chandelure&@fire s,@air s,@ice f,@fire f,@psycho c&@blas,@over,@ice bu,@aval,@dragon a,@fly,@moong&!charizard,megaevolve,mega");
        cpx.put("raid-ghost-ground", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,necrozma,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@psycho c&@orig,@aval,@hydro c,@fren,@ice bu,@moong&!sceptile,megaevolve,mega");
        cpx.put("raid-ghost-ice", "charizard,volcarona,diancie,rhyperior,necrozma,hydreigon&@fire s,@rock th,@smac,@psycho c,@bite&@blas,@over,@rock sl,@rock w,@moong,@brut&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-ghost-poison", "groudon,garchomp,mewtwo,metagross,necrozma,gholdengo,hydreigon,espeon,tyranitar&@mud s,@conf,@psycho c,@zen h,@shadow c,@hex,@bite&@prec,@earth p,@psyst,@psychic,@shadow ba,@moong,@brut");
        cpx.put("raid-ghost-psychic", "necrozma,gholdengo,tyranitar,hydreigon,dragapult,mewtwo,chandelure&@psycho c,@shadow c,@hex,@bite,@asto&@moong,@shadow ba,@brut&!gholdengo,@hex&!dragapult,@asto&!mewtwo,shadow,megaevolve,mega");
        cpx.put("raid-ghost-rock", "kyogre,inteleon,sceptile,rillaboom,groudon,garchomp,necrozma&@waterf,@water g,@fury,@razo,@mud s,@psycho c&@orig,@hydro c,@fren,@prec,@earth p,@moong&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-ghost-steel", "charizard,volcarona,groudon,garchomp,necrozma,gholdengo,hydreigon,chandelure&@fire s,@mud s,@psycho c,@hex,@bite&@blas,@over,@prec,@earth p,@moong,@shadow ba,@brut&!charizard,megaevolve,mega");
        cpx.put("raid-ghost-water", "mewtwo,electivire,sceptile,rillaboom,necrozma&@psycho c,@thunder s,@fury,@razo&@thunderb,@wild c,@fren,@leaf b,@moong&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-grass-ground", "kyurem,baxcalibur,charizard,volcarona,rayquaza,salamence&@ice f,@dragon t,@fire s,@air s,@bug bi,@fire f&@ice bu,@free,@aval,@blas,@over,@bug bu,@dragon a,@fly&!charizard,megaevolve,mega");
        cpx.put("raid-grass-ice", "charizard,volcarona,lucario,eternatus,roserade,rayquaza,salamence&@fire s,@air s,@bug bi,@forc,@poison j,@fire f&@blas,@over,@bug bu,@aura,@sludge b,@dragon a,@fly&!charizard,megaevolve,mega");
        cpx.put("raid-grass-normal", "charizard,volcarona,kyurem,baxcalibur,lucario,eternatus,roserade,rayquaza&@fire s,@air s,@ice f,@forc,@poison j&@blas,@over,@ice bu,@aval,@aura,@sludge b,@dragon a&!charizard,megaevolve,mega");
        cpx.put("raid-grass-poison", "charizard,volcarona,kyurem,baxcalibur,rayquaza,salamence,chandelure,cinderace&@fire s,@air s,@ice f,@dragon t,@fire f&@blas,@over,@ice bu,@free,@aval,@dragon a,@fly&!charizard,megaevolve,mega");
        cpx.put("raid-grass-psychic", "heracross,volcarona,charizard,kyurem,baxcalibur,vikavolt&@fury,@bug bi,@fire s,@ice f&@megah,@bug bu,@over,@blas,@ice bu,@aval,@x-sc&!heracross,megaevolve,mega&!charizard,megaevolve,mega");
        cpx.put("raid-grass-rock", "kyurem,baxcalibur,lucario,heracross,volcarona,zacian,metagross&@ice f,@forc,@fury,@bug bi,@meta,@bullet p&@ice bu,@aval,@aura,@meteor m,@megah,@bug bu,@behemoth bl&!heracross,megaevolve,mega");
        cpx.put("raid-grass-water", "eternatus,roserade,rayquaza,salamence,heracross,volcarona,overqwil,moltres&@poison j,@air s,@fire f,@fury,@bug bi,@wing&@sludge b,@dragon a,@fly,@megah,@bug bu&!heracross,megaevolve,mega");
        cpx.put("raid-ground-ice", "charizard,volcarona,kyogre,inteleon,sceptile,rillaboom,chandelure&@fire s,@waterf,@water g,@fury,@razo&@blas,@over,@orig,@hydro c,@fren&!charizard,megaevolve,mega&!sceptile,megaevolve,mega");
        cpx.put("raid-ground-normal", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,lucario,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@forc&@orig,@aval,@hydro c,@fren,@leaf b,@ice bu,@aura&!sceptile,megaevolve,mega");
        cpx.put("raid-ground-poison", "kyogre,inteleon,kyurem,baxcalibur,groudon,garchomp,mewtwo,quaquaval,espeon&@waterf,@water g,@ice f,@mud s,@conf,@psycho c&@orig,@aval,@hydro c,@ice bu,@prec,@earthq,@earth p,@psyst,@psychic");
        cpx.put("raid-ground-psychic", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,volcarona,quaquaval&@waterf,@water g,@fury,@razo,@ice f,@bug bi&@orig,@aval,@hydro c,@fren,@ice bu,@bug bu&!sceptile,megaevolve,mega");
        cpx.put("raid-ground-rock", "kyogre,inteleon,sceptile,rillaboom,kyurem,baxcalibur,lucario,groudon&@waterf,@water g,@fury,@razo,@ice f,@forc,@mud s&@orig,@aval,@hydro c,@fren,@ice bu,@aura,@prec&!sceptile,megaevolve,mega");
        cpx.put("raid-ground-steel", "charizard,volcarona,kyogre,inteleon,lucario,groudon,garchomp,chandelure&@fire s,@waterf,@water g,@forc,@coun,@mud s&@blas,@over,@orig,@hydro c,@aura,@prec,@earth p&!charizard,megaevolve,mega");
        cpx.put("raid-ground-water", "sceptile,rillaboom,meowscarada,venusaur,kartana,zarude&@fury,@razo,@leafa,@vine&@fren,@leaf b,@grass k,@sola,@power w&!sceptile,megaevolve,mega&!venusaur,@vine&!venusaur,shadow,megaevolve,mega");
        cpx.put("raid-ice-normal", "lucario,charizard,volcarona,diancie,rhyperior,zacian&@forc,@fire s,@rock th,@smac,@meta&@aura,@blas,@over,@rock sl,@rock w,@behemoth bl&!charizard,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-ice-poison", "charizard,volcarona,groudon,garchomp,mewtwo,metagross,espeon&@fire s,@mud s,@conf,@psycho c,@zen h&@blas,@over,@prec,@earth p,@psyst,@psychic&!charizard,megaevolve,mega&!volcarona,@over");
        cpx.put("raid-ice-psychic", "charizard,volcarona,heracross,diancie&@fire s,@bug bi,@fury,@rock th&@blas,@over,@bug bu,@megah,@rock sl&!charizard,megaevolve,mega&!heracross,megaevolve,mega&!diancie,megaevolve,mega");
        cpx.put("raid-ice-rock", "lucario,zacian,metagross,kyogre,inteleon,sceptile,rillaboom&@forc,@coun,@meta,@bullet p,@waterf,@water g,@fury,@razo&@aura,@meteor m,@behemoth bl,@orig,@hydro c,@fren&!sceptile,megaevolve,mega");
        cpx.put("raid-ice-steel", "charizard,volcarona,lucario,groudon,garchomp,chandelure,blaziken,reshiram&@fire s,@forc,@coun,@mud s,@fire f&@blas,@over,@aura,@prec,@earthq,@earth p,@fusion f&!charizard,megaevolve,mega");
        cpx.put("raid-ice-water", "mewtwo,electivire,sceptile,rillaboom,lucario&@psycho c,@thunder s,@fury,@razo,@forc&@thunderb,@wild c,@fren,@leaf b,@aura&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-normal-poison", "groudon,garchomp,mewtwo,metagross,excadrill,espeon,rhyperior,alakazam&@mud s,@conf,@psycho c,@zen h,@mud-s&@prec,@earthq,@earth p,@psyst,@psychic,@scor&!metagross,@psychic&!excadrill,@scor");
        cpx.put("raid-normal-psychic", "heracross,volcarona,tyranitar,hydreigon,pinsir,kleavor,kingambit&@fury,@bug bi,@bite,@snar&@megah,@bug bu,@brut,@dark p,@x-sc,@foul&!heracross,megaevolve,mega&!pinsir,shadow,megaevolve,mega");
        cpx.put("raid-normal-rock", "lucario,kyogre,inteleon,sceptile,rillaboom,groudon,garchomp&@forc,@coun,@waterf,@water g,@fury,@razo,@mud s&@aura,@orig,@hydro c,@fren,@prec,@earth p&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-normal-steel", "lucario,charizard,volcarona,groudon,garchomp,blaziken,chandelure,conkeldurr,cinderace&@forc,@coun,@fire s,@mud s&@aura,@blas,@over,@prec,@earthq,@earth p,@dynami&!charizard,megaevolve,mega");
        cpx.put("raid-normal-water", "mewtwo,electivire,sceptile,rillaboom,lucario&@psycho c,@thunder s,@fury,@razo,@forc&@thunderb,@wild c,@fren,@leaf b,@aura&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-poison-psychic", "groudon,garchomp,necrozma,gholdengo,tyranitar,hydreigon,rhyperior,chandelure,darkrai&@mud s,@psycho c,@shadow c,@hex,@bite,@mud-s,@snar&@prec,@earthq,@earth p,@moong,@shadow ba,@brut,@dark p");
        cpx.put("raid-poison-rock", "groudon,garchomp,kyogre,inteleon,mewtwo,metagross,zacian,quaquaval,espeon&@mud s,@waterf,@water g,@conf,@psycho c,@zen h,@meta&@prec,@earth p,@orig,@hydro c,@psyst,@psychic,@behemoth bl");
        cpx.put("raid-poison-steel", "groudon,garchomp,charizard,volcarona,excadrill,chandelure,rhyperior,cinderace&@mud s,@fire s,@mud-s&@prec,@earthq,@earth p,@blas,@over,@scor&!charizard,megaevolve,mega&!excadrill,@scor");
        cpx.put("raid-poison-water", "mewtwo,electivire,groudon,garchomp,metagross,espeon,luxray,alakazam&@psycho c,@conf,@thunder s,@mud s,@zen h,@spar&@thunderb,@psyst,@psychic,@wild c,@prec,@earth p,@futu&!electivire,@wild c");
        cpx.put("raid-psychic-rock", "kyogre,inteleon,sceptile,rillaboom,groudon,garchomp,volcarona&@waterf,@water g,@fury,@razo,@mud s,@bug bi&@orig,@hydro c,@fren,@prec,@earth p,@bug bu&!sceptile,megaevolve,mega&!rillaboom,@fren");
        cpx.put("raid-psychic-steel", "charizard,volcarona,groudon,garchomp,necrozma,gholdengo,hydreigon,chandelure&@fire s,@mud s,@psycho c,@hex,@bite&@blas,@over,@prec,@earth p,@moong,@shadow ba,@brut&!charizard,megaevolve,mega");
        cpx.put("raid-psychic-water", "mewtwo,electivire,sceptile,rillaboom,volcarona&@psycho c,@thunder s,@fury,@razo,@bug bi&@thunderb,@wild c,@fren,@bug bu&!mewtwo,megaevolve,mega&!electivire,@wild c&!sceptile,megaevolve,mega");
        cpx.put("raid-rock-steel", "lucario,groudon,garchomp,kyogre,inteleon,blaziken,excadrill,quaquaval,rhyperior&@forc,@coun,@mud s,@waterf,@water g,@mud-s&@aura,@prec,@earthq,@earth p,@orig,@hydro c,@scor&!excadrill,@scor");
        cpx.put("raid-rock-water", "sceptile,rillaboom,mewtwo,electivire,lucario&@fury,@razo,@psycho c,@thunder s,@forc&@fren,@leaf b,@thunderb,@wild c,@aura&!sceptile,megaevolve,mega&!mewtwo,megaevolve,mega&!electivire,@wild c");
        cpx.put("raid-steel-water", "mewtwo,electivire,lucario,groudon,garchomp,magnezone,blaziken&@psycho c,@thunder s,@forc,@coun,@mud s,@volt&@thunderb,@wild c,@aura,@prec,@earth p&!mewtwo,megaevolve,mega&!electivire,@wild c");
        cpx.put("atk-grass", "sceptile,rillaboom,meowscarada,venusaur,kartana,zarude&@fury,@razo,@leafa,@vine&@fren,@leaf b,@grass k,@sola,@power w&!sceptile,megaevolve,mega&!venusaur,@vine&!venusaur,shadow,megaevolve,mega");
        cpx.put("atk-flying", "rayquaza,salamence,moltres,toucannon,staraptor,enamorus,charizard,yveltal&@air s,@fire f,@wing,@peck,@gust,@fairy w&@dragon a,@fly,@beak,@blas,@obli,@hurr&!charizard,megaevolve,mega");
        cpx.put("atk-dragon", "rayquaza,baxcalibur,haxorus,eternatus,garchomp,kyurem&@dragon t,@ice f&@brea,@outr,@glai,@dynama,@free&!rayquaza,megaevolve,mega&!baxcalibur,@glai&!garchomp,@brea");
        /* ===== END GENERATED topByType ===== */

        /* Pattern to match @#keyword. or @#keyword[number] -- keyword may
         * contain hyphens for the raid command family. */
        Pattern pattern = Pattern.compile("@#([\\w-]+)(?:\\[(\\d+)\\]|\\.)");
        Matcher matcher = pattern.matcher(currentText);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String keyword = matcher.group(1).toLowerCase();
            String paramStr = matcher.group(2);

            /* Bracket parameter: DAYS for the static templates, CP floor
             * for raid/atk/attkr, the index for raidn. */
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

            /* Raid command family: precompiled lookups. */
            boolean isRaid = keyword.startsWith("raid-");
            boolean isRaidN = keyword.startsWith("raidn-");
            boolean isAtk = keyword.startsWith("atk-");
            boolean isAttkr = keyword.startsWith("attkr-");
            if (template == null && (isRaid || isRaidN || isAtk || isAttkr)) {
                String raidExpansion = null;
                String lookupKey = null;
                String seKey = null;

                if (isRaid || isRaidN) {
                    /* Canonicalize the type part: dual keys are baked with
                     * the two types sorted lexicographically. */
                    String typePart = keyword.substring(isRaidN ? 6 : 5);
                    String[] tp = typePart.split("-");
                    String canon = null;
                    if (tp.length == 1) {
                        canon = tp[0];
                    } else if (tp.length == 2) {
                        if (tp[0].compareTo(tp[1]) <= 0) {
                            canon = tp[0] + "-" + tp[1];
                        } else {
                            canon = tp[1] + "-" + tp[0];
                        }
                    }
                    if (canon != null) {
                        if (isRaidN) {
                            seKey = canon;
                        } else {
                            lookupKey = "raid-" + canon;
                        }
                    }
                } else {
                    lookupKey = keyword;
                }

                if (lookupKey != null) {
                    /* With a CP floor, prefer the repacked variant that
                     * reserves room for the &cpN- clause. */
                    String exp = null;
                    if (param > 0) {
                        exp = (String) cpx.get(lookupKey);
                    }
                    if (exp == null) {
                        exp = (String) expansions.get(lookupKey);
                    }
                    if (exp != null) {
                        if (param > 0) {
                            int cpv = (param > 9999) ? 9999 : param;
                            exp = exp + "&cp" + cpv + "-";
                        }
                        raidExpansion = exp;
                    }
                    /* Unknown key (bad type name): leave shortcut as-is. */
                }

                if (seKey != null) {
                    String se = (String) seTypes.get(seKey);
                    if (se != null) {
                        /* raidn-: pick the Nth entry from the interleaved
                         * buckets (round-robin across SE types; per round,
                         * best tier then budget tier). */
                        String[] seArr = se.split(" ");
                        ArrayList bestArrs = new ArrayList();
                        ArrayList budgetArrs = new ArrayList();
                        int maxRounds = 0;
                        int si = 0;
                        while (si < seArr.length) {
                            String b1 = (String) topByType.get(seArr[si]);
                            String b2 = (String) budgetByType.get(seArr[si]);
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
                            while (si < seArr.length) {
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
                        int idx = (param > 0) ? param : 1;
                        if (idx <= entryList.size()) {
                            String[] nparts = ((String) entryList.get(idx - 1)).split("~");
                            raidExpansion = nparts[0] + "&@" + nparts[1] + "&@" + nparts[2];
                            String ntag = (nparts.length > 3) ? nparts[3] : "b";
                            if (ntag.indexOf("b") < 0) {
                                /* Vouched only in boosted form(s). */
                                String nf = "";
                                if (ntag.indexOf("s") >= 0) { nf = nf + ",shadow"; }
                                if (ntag.indexOf("m") >= 0) { nf = nf + ",megaevolve,mega"; }
                                if (nf.length() > 0) {
                                    raidExpansion = raidExpansion + "&" + nf.substring(1);
                                }
                            }
                        }
                        /* Out-of-range index: leave the shortcut as-is. */
                    }
                }

                if (raidExpansion != null) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(raidExpansion));
                    continue;
                }
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
