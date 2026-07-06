import io.reactivex.functions.Consumer;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Map;
import java.util.HashMap;
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

        /* Pattern to match @#keyword. or @#keyword[number] */
        Pattern pattern = Pattern.compile("@#(\\w+)(?:\\[(\\d+)\\]|\\.)");
        Matcher matcher = pattern.matcher(currentText);
        
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String keyword = matcher.group(1);
            String paramStr = matcher.group(2);
            
            /* Get template for this keyword. */
            String template = (String) templates.get(keyword);
            
            if (template == null) {
                /* Unknown keyword, leave it as-is. */
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }
            
            /* Process parameter if present. */
            int param = 0;
            if (paramStr != null) {
                try {
                    param = Integer.parseInt(paramStr);
                } catch (NumberFormatException e) {
                    param = 0;
                }
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
        }
    }
};

/* Trigger fires on . or ] after @# pattern */
replacementMap.put("@#", pogoSearchReplacer);
tasker.logAndToast("Added PoGo Search Expander module.");
