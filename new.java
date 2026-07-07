/* PoGo Search Expander for Tasker. Generated -- regen with:
 * python3 transcode.py --apply new.java
 * Docs: github.com/0xjams/Pogo-Search-Generator */
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
        /* PRECOMPILED by transcode.py: all command expansions plus the
         * raidn- data, as gzip+base64 TSV (rows: kind<TAB>key<TAB>value;
         * kinds e=expansions c=cpx s=seTypes t=topByType b=budgetByType).
         * Human-readable twin: baked.tsv in the repo. */
        String DATA_B64 =
              "H4sIAAAAAAAC/909S3rjOI/rqlOkNlq575CryLZiayJbLslOKln47AMS4BsgKSU90+Vv/m86JdEUQYB4EY/5x3mcTu3w46U/"
            + "HK/9+fBzhj+n7sd7e+2mp8M03s77p2ncvcIL/exHN3S769Tv4GU7z/DYPPiBo+GJfqPneep33dNl7Ofx/PQyfMAHnrY39RF4"
            + "jgPMh/VHnuZr1w16Dfj0B/3oMn/sjvDNl7afPuA9TklfNG/1l9UDu3pYhlqBmlDP4xav1kVg0a9/wMKeDsdxvj7t20m9gAe0"
            + "RlwEjVf/Cb5gQaDlGCD0ZD+CKfdTe4B1q6/jnxYiNcCCrTbJvtHTRZtlt1oPou2wH4Zf/0PzudWrKfUem3n1KLceD8h4bWqk"
            + "xbKek7ZCvcAV6KchogOE6pERUvE3EWZxIFCg/jluM443LxGTCIqaIcBpMBQREKMwQfI/Hr3SOI9szb7ZsZrC9GBcXfJZS9u4"
            + "RpzR3wh75hLaUm+Jtv23ZpP8UUS1LIoTCD2aTdDi6IseROc8XKGa1JCNxhnC7xMuvjVjPcIJj4lP43pkSsvRe0s/+rt2Prv5"
            + "NMqyr/iLPggSQwg/SRQUPNLEohdvGJz9TngAop9xnOmJPfH6B0hFBkT1KYteZm8iNp5OF3BM+9BjfcFoTS92Mn/RHvKYZRha"
            + "8pcdfpITIsIuaDJy9KO2i74e804cifTjHd1ogTQqpCKP9ZlT5ujX/AIoyiOhdADSkiUhfnlISxoKPJ44Su1KMlTTGCs7pcmR"
            + "vDjBggMCcvJXyW4SkVNAofy8RFcecqRtNMSmhnqyVt4FTYTBEkL0syuPKNARH75G8otAMSQWciD7U/fakE4EaTqPRzGO6wYj"
            + "QpLxX5HagL8OOLkbwipYqFl5g1iOk4KV6mIhv7QDYyaTTiXpZW4AEYHHRngQNfKDLQxZhT8WcU6DA0j8UYh6n4sZ/ckhNjxu"
            + "Br8hoZmfWDET0IvZNBrFSBkaHuq7erSvMEZvNMLN6qzcCVULM5RFezhIoZyg8hS3YOmE7Ei1xHcsmuMxhGmzOwxUGse4jFTE"
            + "2FGe/hu9YWUJfs+zIf5JDZoQv8FYBmGCDWJ/Q0pCOArhTcfiyfUUYI8k5C8IykNhYenBzhO2+V18zkvfIXKwoPubHIwzOivO"
            + "J2xR0b4qrD5vL5lRvB4iATp1gd2BqzM/NiOQDhLLOWRwODI4zoF+Te8DfNu1wyuFU980YiCEUYTBrBqsB4YnmfhZaG3gOEJd"
            + "DXQBAokne4qHGRZw7XClZkiCpGD9et9o11M9PlwXjbV6lWCv+kOtQqVX4d5YDITWp2VtOIowwDkcaARtfSgboq2nkbT5dUB6"
            + "25/+IFqld1Z8asaX0e7TmvQnYw0kONG0HDOQ10LSkaHZ7AaE/hSzscFPeXnEfMOqH+mrhO0kGli0kEQucR9k2Uw4Tj3yyS2y"
            + "6O2QlJunxkGAY/yZ72zIGKv+l3wPRCpn3DDfeuSdfQykqVZvbUk7hmHh9h3uKL94tYdun1gZ4j6kBweMMjXL3UDaE+fyEXyP"
            + "Cac0UyaqLo1P6I6zeKJVe/sjiQEa6Sm96HVNhbs9cuYjapRPAtGxSsf6rijGBxUMDj0LkvJnmI36RVaeqwF5lwLNhGCLBop5"
            + "7ZE0oYneFD0iBkgan/WE0Jjswq2XO7aZAiKiIam1FCvlNDAxldwrXti6LaK/REYZErj7MP2swC7NMHZPkrOj1sCb+lZa6yG+"
            + "zeccmgkV6ZkSv2c07GruSqbu0MP/tfP92O/39910m+/beRM9Vn8+9fft5tS9X9/HuwZwfNrdjx8X2LrtfT5tPttd357vp+7a"
            + "uvHDeLmdzx/3C3zVDj5tLuP0oTwYn/cBILMv4MOnbhi7K0zx+9a76bc/0fX9Y3dsp/6znfZ3vfmw1gHWeNokz8e3boLnUzcf"
            + "+6k94eOX+8tt7hWrRRjDd/on2w3M+Nm/dufoC/Fj+gA83nWXYzuM53t/3vX3U3/ewyzHrr1OrR19ag+nO0A3Dlf4bDDHvNl3"
            + "w+U4/gk+OCuQzvDmNnXh8J+klv94/RgP8FL/A1Y/9QeA6rQJHx8/9tP4dIEVze/tCXbzeidyp1c7hTv77nSDw++/UYu5wpnq"
            + "kp/J7y7w7vDRTu1+nON1nDYv3dQOQB/XKV2J/PICL1+BgAeFo9t2O3RAqy0Q3mZuT7dpvF7DL+3EN2qmy9SfgGDObbJNP536"
            + "/SMm9usRjk43KQL+hHWOU3t/A4TeLwph283U9q/jzYyCbXzvh71eyGf3Oo2nuyLSg3m63fy5Ta89kEPn/WTfz+rVqT3jGuyr"
            + "F/O700Y/uqmDqr9uVwXfaS9qy9106Ttcg/ccKPPcfY7nDmcza/55pZvPGcj72sN2v9ymj/vLBCcA6CV4OHTtiz7Wb935Nre3"
            + "6f7Ww3x67Bw/ncehhaGv7XRtYf+n9nM0E2yB5rv53N6AV/lTwNqn277DR5fxHUB7VwR9bD/gtKmz1d+RLb7CY5gWmNz79RiO"
            + "nzdv/U7t9rYb8Cd22VM/DO12BAzp1eiPbuOn9gM/td8bTh+czNNdKzh31IngR/SUfLJXNVfHsE09vlObc2pP4/xOC93f27d2"
            + "gKUegJN1b2M/aXyd7kAK6uh8tqf2szsDd8TPbrtjdxqvx6dtqxhX+2cHh2cLO42v9WTbiCHohyfNT8lPONx2cBYA7nHa3QFP"
            + "MNdpYx7uFOPGh44FuoeK33TDvgMcju+wO3O3mxh4X8bdDdZ/hFOzm8Z5xhl2w6h593hWc9ymCZew/zi3p15xQPdCj6cXgOJu"
            + "mtpXYOT3/XgDVqA+DBN3inexb/WXFOaMnQpjzu0VjhDp3v9zn4fb/tBpKjx0Z0DAfehBNNnHJ/MYyG4PsO78V9uu2yt6YaY7"
            + "bc79EY7KYWS/5RElyM+9/zvF7n/DeWR+N2/O7QGOD8gHdlbYY9jqfce/7N7at0kRtn17uJ3V0Zl349BfOvZ3yBG0Yaf+s4ft"
            + "RWFxmbqdFjzh4w4O+PE30jIQweniP1dM+MS+Ub8YQPCNir/h8xn+qfAaPbYTsS9+ww86OA+IFnjxD0y0G5W4nbRW0cPf+FgP"
            + "eVJvIjFIE+nDQg6z9uP3rf1s722vuTUec31gjGiHs3y4w3ClYczt0J66866z6gc8B8A7oGO9YLy2e8fxnhKD05Pm8fHWDVc4"
            + "tIfbfL2P20GdgODZUZ2Q7QZW2+9uZ//Y6WnnK4jiyxXgdWtTR4E0cTqrcNhe1C9hvlNyfvG5Vs+SF2qSu31jptFPTxvYgFfY"
            + "r1P++cvtqtjD0F57OKfA4Z6ObuaTzw3DWQ4tsGig1uixUkEPms2EUylEqjgYx4a0+Dp1h/aolNL+PMM39LM//8z60/RM6ezb"
            + "3j6dd/3nGI18619bLUGDsRt4BIx0BElHz/V/lKiwPMP/AdDGvFMsuu/sV3F9swcWLfvajdPTSZ9MbcfsQf/e9d1dm6wg/9C4"
            + "HZSEsyQ/n9odvnjXOnD4AtQ5JW+vH6C69tc2eApI66Zx3+6uHwPzCaXyDz080ho9LU5xqvCF+QF95DrdTnYy70egll8A60AN"
            + "bsH6V/YEWhHbaq37J901/QDFaRo/T62j0dM4AtlvN/aN5eDmTUzVNGCr5VsgDewLRhx475RlB+rgHZSZyZ/NV+qP3Z/g1RZ0"
            + "vuu14yccbmc4Mtw7q3PYd6hzwIZQZEu6ZdupU5OmL8bbVRk1TjzaN1r2trKGY3l5+BFtj/g/2DpdyXFH9xU4mEpPaP9o9hjN"
            + "tQECHw6tm0oPh8f63/01nSh5sxXfGODVvqlwM3cGtmrUdrpdFTTKUOh6+IH/OH6qfQZKMgl0AAev3YIijM/N1N4jmsAR2S0i"
            + "vqMSxEqE6+Ev400dQbehbm0KHvQt+Pa5UxyVgujUyuhl658a/W6+nWd9YAwr2t6GobuqtRp2pFSBQKW0b04GgXqqHrjik+Jr"
            + "npgOnodfprP6kyKYfsQq8r79/IzdEGCYKQGYiFs9duuEkD34+gVYWO3l9jSAHXhXBu5dHQVlr8Gh6EAiHzrYd7WcrbY2YOjr"
            + "+Dqiw4KG6qewNTezP7N5o2RcO9OaieK2xhkDf2kvhxMavuW/lVwC8KJX5ly76wIfwhbmQ1dBf74CNECescW93QAHgP8poyB9"
            + "J5jJOz2zNZP1H/0bfDi1fbeCfQkbcfsD/Oc+A5u3D2FWNDp5i+zUje8gHUGxbbXx1lpTzai7oSkI0ylLTbKLONNrq47sSR/8"
            + "czBao0Y2mDjbaCtZNmoyMkSyinrGAMho8XoTUU/nNW9BMd4mirFRpBXsqP+yCi2Q+wg7cj4DfV06YFTbrn0N9E6tpaLeufX0"
            + "Tl5Lg+XNF0WrgVa3FVRJNaPS6WQ9i1XMQI4BBb0FSpyaSutRgra0OQz96aS8PonCspUUFo0NEwkDdvIZzMBQ8G+1KAJ+ASvU"
            + "3MZ/JaoLal4S7wl5H4ZWmQi8EBUltZ5RCT5Owm03nDjcao9ce4InnhxSE6HEyUqJDRyl11ZploYrK0XxqjkDJzw0FcqcXxHh"
            + "oXvt5zl8emxBpwIu0QWPf3agFfX2xoy+aA+xd3DJvUEqovMtNM9qeZtnddw3z9r5sXk2cmTzbDwPzbNiBfBvffI3z8o/snlW"
            + "7hD4m1wWzS/7XT26+eV9nn7Z/KIFKGMAgB/eOv2nAUSLDXT0bAyzNwa5xfiGDAR30B33b56RxW+eSQxsnjXX2DwTscN6gbCb"
            + "Z+Vn3jyTv9T8ARArN8DmmXgM/Woe6I/35pddlflJ88ssR4AJRRfB7YSMdT46D51xxzYeBqwogu2GIw4LAUmyeVacoDEvtwCt"
            + "Fj0wZlLbrzy50lY3v7w10O+aX3YxyWhaE05qYArySwLcWJ7sYcc4NqxjojFI0Qy68Tf9t7f5ip8D5aBfQxHaeQ9guS/oAbB2"
            + "mlVCAMpi65Vwtizp9J7H0ZkMVhhb88LppXBotHoCdN5rzGiGBTtEEmyD71+aZ6W/bJ6VAMRBWzgvikxh6SQCFXzofYFfDR9w"
            + "aOw6BXCULsABY05/ejpQl/S4sAMAT39wNgArwPDCtePpjw7D5tnTvzMLL54Qq5GkW72xREw82HlRjKvKiW2SuJaFNgY/iA7F"
            + "5c4vPnvTEhseK5aqzsIZWIIS/U2IFP2T+Uq/7BVfBParwG9fm1/mazSPAcq71Q4OSAKQo2davkOc0RXseREAyB+jePF4rJSz"
            + "SvEIswzzPjlg7hhpbSzmzgwbS49Vjj/7bE0fpIQ544mxLBp5HF57mGPFMmaJqVm6Q20wZc0pAETBAmem80/HiOPL4eGng7SM"
            + "RRcOkdFHrVfQYw3GBN1Y7c0pQs7oN7pkyCg0dlDn9CkP9Dh4DDoU0KZy7NEYAFFbuEA6RsmDN6BlqTOjnAGbZ6WkNr/cMgV4"
            + "lDbMsTmGR6QczxqSCas2rCBSB3xmF539RAVYz+i0Wl5xgAwrT5hH7TlCtq6ZRnia7PFBjh5oOzktwK1N/97yhMBXWSAvayAY"
            + "HujTmaMs4wD0aAyWCyZFUyCt5pf7vvotoMN+EScw5w0nkLBEBknKBCzpeTLIGN/GTiFh2xieYKwTK2WUkW5ErOEKyvloeIMy"
            + "exRcHUCnHCtG2CgnEKjYbjXeeMCPWZoEkzKJDFlxLMKBZLV4vDSwhqcnVn2bIWQQGkhjLUSMAeEoHP/mF302jyO0zHJqUHJ2"
            + "HLU5EK2oizUihC3UUlOFKKO3ZtmEIGHRLmRUUFLgPCVI+1OtZ8W5Pq052njaqPqdQgMaryA6+92rf87Icm08pdTT7PTvtfHq"
            + "P4YJtaELhw7XIrNxJL5KXu4Ig1NXiYkbajPMXNFgiB9LcyEv1xSZRU2NXKrgEBnhZFkDAeM4hCCbQt3BQYQ8gpdRMZ8oiqYg"
            + "n5zDFSNk3YEyLiUbjOZQFtkWnGWU2BXGIaUwi4FuX5G6LiOeg6vK4uOhNscvoc/YEOS0DMEArFAzArgyphMHLQnA1IiyXs7E"
            + "aDKwxbZHYDUp12hiNfngkhGiZEA1aFP3I6W6WH1iALfU6KlLERkm2lMAdJOSZKREJXwFCbaOFtHsYA+UgLCGPUazQwezYnXd"
            + "HeLAmCXiKrOnLO+5c7UdHk1pd9UoKiGr8SqJIEaupCyQvEep8niROc+7xcIjlpFzHjf0DlsEW3LYyMz3QcPzFVn6BkADdyVo"
            + "kocsJTmOa4pUuEaS1Tr3qqiQ3PyPeMDIW1YJGrEiViinGkjBCVgElUSXQWmFShJARn6ZR9J/WT+G6Hd2ZotlEe5KnnFkMN5o"
            + "Z70k7oycU9qZLlVEKBuVDH250+TMScqW8IxJhkcY+oHFtYdTFhl2YgZN+ktkJftASNdNMocQnJo8a+Ccmzxr+Fbvpl/iyCmv"
            + "qWDKuTiIHJ0bgzRYErmyUwOdNei+CGUuOmtYbwf6a4rHKazHJDov6lw2Dee9yDhoBP8F47fxITFcpA62BU4NCw+6FHIuDEKd"
            + "8mSIbguFAd95gXDY73sKE7kw0idZwGzlGbtugz7mrEXE53iGMcBMdJclSx+V0VnE3UDLyzpvkD7pnj6SYJoWaQlIztK1lavX"
            + "FfN3kTaXXcczFJq5mt+xnmpG/ajhHw913+PVQnMEGFOZISoXP2LvFF10oIkItJSH5EWE5atKyokOTK8bT43nld48q5ONFz9w"
            + "dOw3LWn+st/MwqJtLI7ROQkpMvz4osoI4IS3J9dVpC9pAR1iDYNYMp7qov7nV5lbdW9qzpzdi5r7UwMu7kXDm1Z4C2QB1mcM"
            + "t6R8b2pr4ZnlcSqT2zfHFyyhGkFl0GTRpmFBvS+QVJxuS4i0plbetVQrr6JYqge4K4lrDf7X43WCMoj5/Y+3PeZ/5kbbsr+G"
            + "w4HR8EQcFLW5JZjQxpMhrwquEKMsJKksO6ALYKQw9hrYRFF89SLYqzSZYwoJ+blQRWdh+Zdy/uEhXhGSYsoevvlSzquQWRXD"
            + "Z8C3AeMV0XwIZj6mD6XRd8T0hffczvhIxU+CrvQG3N5mEVqi++6GNZgiZQ4vspB/SxfcZilE0+ULb7/+aFWcn2yPMJf5BcuE"
            + "v9r/op0Sg5baH1UXkkkAmrVAdPiZ1QLRTxbeVRoDJIlAU7aI0QlVlrcXbsYRAOdhS8Cjep0FW8NA2KQUGDuSssILiTCxK2K6"
            + "E29OagkSbRDR4mAhS8hPuCFO40hS7xiAKZ+yr0KH9kgKHBMhZGe0AULcMePizWRoufigjqKEvhA74xcAXhky7PTILDOJr161"
            + "X1eGV7xDrvbQBJWKHyviydVYliOj7STuK6HSEbEQBxtdYUmB0R4wK09WCLmaJIKMDBXZaMwiz7kME7T58HsozCGPtLAKOV6J"
            + "NjJXKlSSNJ4ao6hdAhwDYi6cOgdnpLjQzRCWQfEiqEOhEYNGlk0KW2XobsowK4J0OSlcFbhbz0XoVkgiR1GZFEmQzJcS2THa"
            + "5LfrkaFpI5sxzojwkp+84BXMwjIGdiNGG1IuVlMONHRJWc7w1tlZvPkdwYWGDcMGPaYR8nnPuWgFnzNNIwYZGdgxW9wEXkTM"
            + "zV/CEQGdBgN67jg9Sb58SDAYR1R6fhGToRrcRyCu4khKUjGdXM4iTiW6SlcNIuL4yvd5luhwGSeblDJLDPoWJZggUtHgXpBg"
            + "kpbqD6Aq3jdbWPI3yiIYUkrgN+QAxj0GatR/J6ttPFtWpkWqiiyyhUixYgwbvLDfx7kS8FyfhAB1lQkCHuwU9UYILSQH5KlT"
            + "EmtVuQBRe4fVSYSe+h9dTXixokkUh5Qy+A1hHHFLilXXE4TouksJJhvlUriWUBhdldkV9NFYlCXAcJUwM4C/s8zmBrAXluvj"
            + "peLeHw+klXBNSx4je5LttlIM8chr/2yAR5ZXNjFZfpcJkPaIycrujN/cix2QxHnsOk+gEjPeVtxsMB1t4rQikbt45y5NPnL4"
            + "4zOPcr7KhNusvd2IOvFUXD/55WsCLhnfO+HlUmthES+cfnMXT8HwFWiLmgjVOekS7kImj9GVRUedxFm01SN66WLWktwKhDE7"
            + "MWhMjE6NpcNIuOSEJYZOPkSHtXfWhOkEHZpq4nQYgEsXb4neJd27SeH19XdwIVhismxy8+TyVl1iXylZFu+hkNPbyygxbbaY"
            + "3ldMpo1g0+oxg406jZmrsRElUSXVNkxsrXerJqrOS2tsBG27VqnGhNNFhQ8Mw8gE7IRVD6IKHMV6B7bPWEJxtZlxTMR9k1yB"
            + "2uQki0FSkgWqY7PiqtPggtZoj5U8m3R2y+vCgbSqVn8daEh9lXowB9cgK8buBxFwpo9SirgaTplGCjv4WI4YRwxX8kYXI6wn"
            + "qMKbVoWTFS+I+PFV4YgKa8J+BDJklWO0tOsYiJwmkcu59x2qlEHhGZxMbA+fPZG1OauyhXPJ90sCfSzFVgT4uHQ+KbxnjXpR"
            + "gbCg32KxsJqsErvMHaMLS3XWsppxojkK6Mwqyilw9foiA6FV7l1ms15zqjJGOcIOlTFv5DXISAnJqIthF8saQ4XhlWFYTybB"
            + "W5ALkfsgk+eduEgcM9XsNYULq6PWpazXpdMKSetJOm0mRX11Om3cE3SVwsipwDUqY0SUFcHeER1W85CSL1WmU08MZHJQ41hP"
            + "hnukjg+OFJegLPKhLqtAlybxJGctk8VTU40Oz5sManTrloBHumMKQwXrJ2AItibjPmX4PgYExmusYfec9pjCRWojs2yGFWZq"
            + "BjZyaKNme+XagKkG6VcH9KxoO0JfW9OCeLQ9XKGwqBPwoxVuCjsYV6mPlnJN+FxRiSRCRSstUT5CddL4CRytoj9xnRLpWi2X"
            + "67GYCjN0Z12uvFIsYRKRpYO84sa6XG7YxqwuLjpsGc3S6sN1+jDjPyVmU1FQ1Ta0rnN8p8ClhnWuVERqV5fKRYgYK1jWXivu"
            + "Iso8o8WeW1t4IOv/DvlHRa3olIswt9Yi96Ae4uXDJfLI+uMWcPxSmSP2Kru62JHf9vyhsonjNu3Zu85E92IgE312CcCSsRYH"
            + "NjHXoLEy5nPZEDIbofpoPJHVqxKw5HjxCqiYitq1Xg82mLwOMOFyWjp1jhPVXLdX30tLjNK/o17IFyWNSqJFQY8KSY5zwwkg"
            + "/Rup+6hLkeLxGNUIDEhCjszqwotNcNsnV1iMA9wrKi3WeAkMVEJ6TMLqbcim4+xezKTTPeyVExPHyVzGS5Gc0nkL7nGTAxdB"
            + "x2sef3MtQgKMNI+HOmChPycLk5fgaSIiuNyYstubVIw4TIJPfuH3JOvtJsiCPp4Pgi5W36iBMKdoyEAL7l9pA1aFsiBgshsn"
            + "hc7z33ixOTHXCKBBWuP4hcPAYvZQAZakbXDR+fkoo4hHSjFGLKf8VkrUWsai8PWcT7Wm0GIQnyN57zNXY1ajL8pnA9v6WxcL"
            + "mBdWW3Pr4nCOYbUV1y7kJ8aA2pLPDSGrL2SaOr3FCtyBg7tQvpQyQyIP91eC1xEwEs1sExLTi810HcWGoiZqx3QMbSj3QzVs"
            + "g/9/U96nsM+I6uIWLBrn4fM95DQs88E8QNUxOrmIbtu/yG5LEGWVjeKpCfYu3dMicUaAkVh+sB4fCFztNYuY0NRUBOJkPBuZ"
            + "GjyWCa7rybIkSCdLklwV2iQryyO5ilgdKcmgkt0vuGOxkC0pxpMvw0NY+c4gHS2bZSOzsnCwj6g0nzpXP5i3LYvVhGsQpiEr"
            + "xQ2s67lnYfC8U/kGfAYK8kat9gogVJli3HIOfAZUUY1i4MupUhJ01YBlSnEvqS8kB3pE+mEuxiOrJdbGehBYmYJyuQKTXsX+"
            + "XIVJh5rq+pIuwugrNSYRukLthUw5ZKuaNHWFkP1S3BzluUDTaCOCUPViQhmCVY4W4IIDmBLdSSdcPm7A0CSlOaaBA654t98K"
            + "txpNKLsWVgzyZXfpVjbEV+FSNkBQlgrzl7IoukQ+Xx/H4pBSkfOXdo9gE/4ypkpZMGu4hBvMJQVc68u2LkuQRhBXJEgTaLbM"
            + "TqnhR23ZnUZo9lEquZNt/1FVdSeGTLjDrMKal61f3640b/xjxr6szFcwRA1XrclSrsOzMDWJiRSrKClcl1dAsMncvjp6IN+P"
            + "phw+kGUrbChcBefXwIkJtn4+7Vt3vs0to3UgKiih9k3dMKnuEX2cVauljGrhPo9DNtnZfkhP5v1bco+4cpTqu14v6jiCts5f"
            + "ZYLeuAKvbDfqtM6rdMn0PT2pFWQZTbi6DBmjBuc5oWBAp/6bLHRupXqqAKpMQxrHhcz2cPZ/WK3b3rOUanTPw7qSJVW4Cqoq"
            + "r0i6iope1KdbNYneG+RdZTw+Zb6oAHvIgFkF2EPWVEZm+HjlSwxcGSOacwfH9dlNPXbr5I+taufUTwzptIs4+vi/XpadgHvI"
            + "wuwE20OWZifYHpKRIAeRqwX9vSXVCLK6QkFSjvv3Rc4mSe8VkbNJxnsE24JSQWyJIKZlo5csHZUNqi8VhLyfKnAtP24EXHVE"
            + "ZiYAyTYE+uxeJ11ZU45CInC9KCTMyjrUZOBG7FMOAKFfrr8SrG3RQ+CEzXnqbgRL/XlEIWCAe8RLQQPbAingoarI/Q2eJM5v"
            + "UGVw850yQNFiqfJwTKqswp8q+lz0upPZC8O7lwZ2a7gy/pAYhVGPLyqpwGLO6/MV4ifFX6ke3PIgMo0qkRBFFH4Cg9epOwWQ"
            + "LPIkXYTNbbnA2VtGlrQe/CnA1l5tV6ypO/Sqcs1ssETieBgvt/P5Y3MZpw9VVvoT3g9jd1Wi9djv92lZlosGaBhVMZ3fN7A8"
            + "nne6Z5ya/alXNAQS0CuHIfmlwqaGZh3pgWqv19fpK2DgT2k7dDmNv7NRLG7EVwGAP+DgHNthtMDY3Gr4+eU4/kHgaMPwUMTM"
            + "ymlxtieYi5FXW3EFlaOTcm2kxDz+QsR8QCIk+zmReL4Cw+bwoTy847x5gZ0cVPEntIIHwPTcnm7TeL0SoLRlti9TykrMCXV6"
            + "1XD7M7UfjRxNCxz4ooxqJvFlPYPQjCjkFzCTXZR+b7dvETzAT1/HG4FFWuLmz2167a8qRuXUnnGyjYbnhuXpL7C/zbP5Dm0j"
            + "Rr4+jt8dN3MdVJtXkAqtPuXdfG5vh6OqMDjd4A18/+PUnzfwGgTU+/W4eet3aq+3HWgQ+nO0oeqal8k2sKkvhF4v7yXfSOs9"
            + "kA6Z9lnmwk/1lyrKBa/kg3CcvwCHVzIO2YFesJUNVHPUCHznSImDEMw34NBP7Ws/RhciOgrB35z9eNsOnSvA7McgKE1hd1Pm"
            + "3W7qdGsL+11zE+4+T7/MJNIYMbEYFueM86GieWiLyD3KVSuNavPZ5gubbdehKnnuj0DyYI+c2wPQ8l7Rp4uHCMvz+cEP+W4L"
            + "dvrkjf2e34gNt+d74HAHzYEEZss49JdubwthWnamwwT/4308DZP60lrx5+ZUYf0JLt4sLuniyu+4/sdO1fl464YrsBMAtt/d"
            + "zmPDVa7BUi9YfofphJyWkeraV+tr2Q5Kf4WTkbde6YT9e3Dh1ObIhelVyT2l9cgM7bUfg2bsLcgWRYfChWXsesG7BZpmMXtW"
            + "r/T3xB37BkhcyIjaGjDd2QsN6fJi3vWfruK1cRBEtxVfa92rVAj1EdmVinuxbumOB3WgKoBs7bvJjwOEWWlrtDeMKQ5iGpKA"
            + "dQBapVJrnfO57aZx3+6uH0MTp8Gv7zzippc2zH1W3K2vQbNRFuPQAzT67XW6nbzgXTW3YdFSnedMcoRJdskkSWDzdboaSzJy"
            + "vpYWoVu76GSbyE5cBQsJPzPlFpT161XZKGc4mxSZBOxdTU1bhuyU7UNluk+l3absTEnTKXqwZTtO6S5TRrM0iZB1Pacw3qum"
            + "DRXu3lfACqvzDId2g7Nphyr+aTYP9vlvT6Wi/foiJH66gBpIG4Qu1CQoJK377Lze1uWY1LY2ugAFodaWelYbe55dQUwEeP3K"
            + "DFF4bl49mVGXdOMz32CJercZBcJ2Rndy8wrn+mkAM3rzB2iza+fG7+LGqEOGNx06RUx+SzdF2QgzMHf7TVsz+5f9pqgefScc"
            + "+OR1fB3xL8DqTWsJCkT9pZ87V3pkRfmbylo3VRWl2LpECzqPESQLrkW+L0nKxDd9y2XIzgugX9+AS0yzMZEnSR4HF84mNeSq"
            + "SbLZ2dCnbDRXJlYyiNtioyTTwK20Ne0X4yN3YcnDH8sqpqZGzdryxElhzsrixK44pwHlvx2sVRemtfNjjdfFhy+KB1+SprAs"
            + "QWEXXOv+qOyK5ZQGY+pwunWxT64QeiblJBeMPAOLstIqszgz7edy9RfDk1+qvbj+1H+1Lm1VNdogHP/fqEm789KJ/78T27+c"
            + "0L7z+/mubeFu9Kl8E/cl7dtXdAA3kCgjIJe4yNTfjvwdTSaHkS4YTNB2cM5RNnzNdWPA+Ka47bTKChMQwN0eZyM5KhplWEEv"
            + "tVD+V/rq/CvddBwbrjgmWV6cnI48U5aPS55FmzJABW6sALK3qnV5N76Xxri9mBSVjHAp+vDWSxcFD5Lb6nqBQoeqQvVADs58"
            + "KeP3ipOjwcmoyZnqWY7FyXqxgSnWNIlh49UFluHPNL0glRNZdiVIU02pYgbg+iLFUVOLUpniJK+5VKDYB0cqbP69FRD/9dqH"
            + "Hkio2DyGxqnhEU1/Bp6MN0CELGv7R7At7xITQKLts5oU14yIynA2Pvuay2oVcq0XeDIUQPUVyipKunCn6qUgfWqLt1QRWqam"
            + "y995cjIZrLLfxsOZVOJP8s0U4SNxY7C3QA9V4LDmp+Qw8zTTnGuD8aP5JU4iSZNzp/ndAitwI3lms0pNyUcb4SdffFJQbBYE"
            + "Pnsmm1GtneqyqhyNs0RJjwnKD6TlaMx9Q8iWUclmbTzsg1L02WiQrHItmmyV9ilvmLL92wP7NN+tvcpcDQBaYMy5qImk5kxs"
            + "uhGm4mozguGGqqaLa3Qi1LTYTJ5kQTIatltxpplXfH+UdPCy90iWADMdvHAjSMfWyOA7dSWtvJtfdgX4mQgk5vbpWxtm/B+0"
            + "yvABQgVuaeWtKO1DKLyF97DVRbcwFHJVwa0QovWOdotJFxZbX4CF2EN1ARY8bSVvu4ZJ6XK5DGG3aY7UzPnh84HDQhFIZGlW"
            + "MFcr4n1dqYgAnqgwyd/rStTQ5C5EHBhx4FfM7+h2pOEgMQJVhKQoPJfA85/Ktv/qVYKG6Jty7L3Iat8zJ6XbL8izN0HfdSxu"
            + "eVp9ZTJ9OZnyG9Ppg+sRp9BVVDhzEQOJ15c2XnbvSsnYU9fKFyNmBTR9+aIEYUKdrioIQtby2MyMrL7Hg79a+4uBSrW6JcVH"
            + "07J0xOHQYOVa5zKRzRTJZG7jORODN28xUyUBCbS6Cu3N0R1paQ4lkb6WoTJGUYvJS/Q91tIdOk7F7hwsSAmVVdyYpDY3e4y+"
            + "DpHpBCk2CXBXtRmI3BVwVGlfZhfc1XCOU9TCI3RjWlp/1NzoNbGjlC0/2rAHJV+EtEalMzAJZbL/vntuBMjXujNeU/eVhg8A"
            + "i4KgGzH+SwOAS1x8YNLY5wgaWytVMnpyaOIKl3tqTyWGSB3SYH4LhmyR1PVFUXkNQqwBWKVYyKVQY14fw2NLo4p1T7PBVhya"
            + "5MCqLN/Lh1nVc4VC+eslul1Q55RbOusWkUTSSp2uUPQjLfLhWw3enSxmZrrSQVLJP1vxuljtL0465ZMDI3DQlODSIBwXMCw6"
            + "ifC2wspzzSUZH4EByyR9eDHpmACyhscZ60F2o6Z9eaOYGCb1MfCsIk7CJFYbNepkaLaQmpR+IeDIQWV17iJfc2ijSNgYa7Xx"
            + "sAHeKqNiHTK52FgGLE7vLt8iVXRGje+Oqpuj7taG/Dio6rVvpwalmW4ljTwnbtdm8yWwoN799RJ3XMjvgnJ3Jvh3UYUqkySV"
            + "AvW1fIVc3V3RVM8nLXB2eoUzyINovc97ZdHxcrlx4zLya8WX3UEWplI2hmivc/jh7lYKQZlBi+H1d/0Wnsjn/fdqDhaiB8rO"
            + "cDBJVT0rsx28ZoXsHTJbhC9/lyxFpxc1cAtUXUHPnPdYbEDOpgiUinb6lVkXO/QtVAtKeabN1Zn2fUIUd2UFT7+R+oICnjFQ"
            + "Yu095pS5ckJUPKjhylnqCkmNXL7yN3e7UlkNXUSSVsOd2lrn1kr4htPN40qyoptLYB8uakj0dsU8xDjGY5CYAIAac8OIiWz7"
            + "4NjasJIqOkCsuREIrFKOIAKDmmpN12MGROd7zPV6dgpQqdmzU3yWhzkhPGLaUHK74jJ5vH7vnCaKFyzIq+0ti5g1VMyHKOYU"
            + "RQBpTZXXQZe0gLMHKUiNCkPtoy5wQbhQse9bbbbKlzTVuMB0paKqDdiKqIwg/bakpiI0SkdNaKs2Q8J5GZJKBiZo3WKIojcF"
            + "+mJzI0LjtRI9pKQ+Qk4RAlStoxJ8xi2c10YdHIvcI2wjEk5PlaSP0U4Z/NRwvRRXPooq2Z+PrDUsMIRIq6YrmsIYAdssaQjj"
            + "OknxqxQDUGr5wYIq3lzrBiaqOzTouLj7rF1XEcUt6qQI0sNUqTDKaGUFEVk/jRscyKVFsrppotRFqlxWPU2BqlflGMiyNWr5"
            + "kIy4Zm1038WreFFKSEafM3ChSldjLDAM0EQOm4ZfnvJTZzZEHkk2Za/Uuj0FSSt1lVmH2ZwpIe0w0bwzSYarc6Y8cNZrdHYT"
            + "Fup0GvAqpc4pqNX8IXLULauikkStJ6ZrJmi9XE+lEcLYmXuYBC6jPDAAMWcnKQzTyEFZ0TEJ1hvHlCQaAwYCeGaRXzrPLoIH"
            + "6aGKXlioHqY+gQFogQ5hidOUVipqEkSVqIgn8ijUKxwx4h3lOj1i6uTUZKFLUn1+dSZROaI9B2tFlc58arKGycTYLS2tZrlH"
            + "ZY21ehUo2QHNPSoKYCloJNdJTbqos5Qs8xM9jjFCNdfLlyhYfLesIVLOhjLBiSzCE7ElGgwCbUpkyDK/GniE5t1/aTaVBkny"
            + "NhR7pzEwfbl9WhKJXNE+DYENYRLadf+tjIHVGRJg5LjPHNEZoZpGgZYgYkNC6wAS7sGkc+Ws/YowGnuKKqNnLsmxqu5A5YNU"
            + "3ciO9kdQE+JbFKbDinB9ssTxUMKRVhXEiO+F9XS4sEi+gg4fGVSqo6NAqVHtDFBCyHfC8Gygk+NvXvsO25aHAY+5JZPCnqqb"
            + "DkVg8JL176wyQyCRJKrxByVl+v34R7n5IyEqEDblro+hvyFUgjLuIASKZd0VIDaymp2CJLgVpNvLVbeVCI5s56Uw+VmIaVEw"
            + "BpqGpTjHtRYTWAVAEtvmAh/zruLofEne4kRefiPf1ubQoljBnGMlUxjIJJFHbpWm4trVd6pUcG0D0nqHnYVnocMuVuyyPjsC"
            + "p1h2WkNTX+bIrt2L0WKr6vnnKe2SyuuexaaolfRWfV+Z7QJLjNzG5JbuM5nI20ZIaBH93LgDETRkOzxIJVcEanUf4ow5xMRt"
            + "ibYQkxdfPDP5Srv/te7D67oOBxAtcDk6tBR8jeXseMLCtxfXlxXtuvphZashKOHyUrYdbAmKyiJvITil2O4qt53YLwCN1riS"
            + "KB+BT0Yq766rhidTnU5OfVvSCiEoNMPnFKTFdFa2QUCQMvXpqjSfvMIaghVR5QsPmE+K69CUK9SSK3fkVZyR6x0tqHRUrD1T"
            + "CU8htzJTy81rKVao4uYH/nAk5qJnIvCDEKdirDoChFx7aRK8g26B2z6EtOC1D0Crgkaxa5HNVSgPnCTNaA1cGdFAYchqpmUZ"
            + "pMERHPaLKmsttBuwKM2CJKN8UkEEkE0fL9V3ldTxilAgP11KCgESCr5WRRXGIAne+iokOQpdiCbTabEmypbOUCWKapXtYob5"
            + "kkpu5dy2sJJbXYggQSTr2tU3YXXMIQ1eyPAI9havQgxpkMSkFbbltel0je2txXbe2Ube2gsMcL5/Y0dvrw1UHANU52Cw3aBQ"
            + "lPKuBYxTSAuD/VvdoBQ0GV2uvlBGZM8J9TEyFl3It7IwufXpqQJYiMHlyyDaeyG+3iGbAcoVOyzkfmZUuAq0BHX2VsRAR6mf"
            + "9XHQTaK7BQHRrPpQ4m4KoEcJXFKwPFSNPWRnj5SwayDKWHSch5GKYJpOZLZ5aWzZZTqOhf1Xsfim6sL69eKbBNNDld8kmB6n"
            + "ACcB9FDsAfmCnPv+V5b7IKDqct+ldLDUwFsYhJXkh1UEYXnCOAJmQco7l+ruY8gdnyj9vT7lfW2yOwFTHeSTBChguoqfDi6H"
            + "KRB05qKLz1pJwhQihL3crrfc/Tf9fv3VkMVb3d1QaG/nb4ZKBrfIug1Qj3Q5ZGBawL491BQZuMGLxMINLr6TeSuaK1Wli0mS"
            + "qTXjOJ3fNCfVu7mYx3/mQpGZuHyBZhwCd9DwZDwKMcqiXgSICB5TWryGwjTFVKksyfLoGI0ckeREpNkYGa+UvwhXhCUpYIZ1"
            + "8yyCSLWwx6CYB/D3KGAolYNL80o6YtuWf667vWM5H2/dcFUe0+imIe6D7aW9x4l5XftqRex2ADvkCKpunq0pEKi/D1fF0dRu"
            + "dHcqHlNQLtMmLU/LFGnUhRnxCm5JeUadwYVf07P+L3uIrWpBVQEA";
        /* ===== END GENERATED topByType ===== */

        /* Inflate the baked data: base64 -> gzip -> TSV rows of
         * kind<TAB>key<TAB>value, fanned out into the five maps. */
        byte[] rawData = android.util.Base64.decode(DATA_B64, android.util.Base64.DEFAULT);
        java.util.zip.GZIPInputStream gzIn =
            new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(rawData));
        java.io.ByteArrayOutputStream dataBos = new java.io.ByteArrayOutputStream();
        byte[] dataBuf = new byte[4096];
        int dataN = gzIn.read(dataBuf);
        while (dataN != -1) {
            if (dataN > 0) { dataBos.write(dataBuf, 0, dataN); }
            dataN = gzIn.read(dataBuf);
        }
        String[] dataRows = new String(dataBos.toByteArray(), "UTF-8").split("\n");
        int dri = 0;
        while (dri < dataRows.length) {
            String[] kv = dataRows[dri].split("\t");
            if (kv.length == 3) {
                if (kv[0].equals("e")) { expansions.put(kv[1], kv[2]); }
                else if (kv[0].equals("c")) { cpx.put(kv[1], kv[2]); }
                else if (kv[0].equals("s")) { seTypes.put(kv[1], kv[2]); }
                else if (kv[0].equals("t")) { topByType.put(kv[1], kv[2]); }
                else if (kv[0].equals("b")) { budgetByType.put(kv[1], kv[2]); }
            }
            dri = dri + 1;
        }

        /* Pattern to match @#keyword. or @#keyword[number] -- keyword may
         * contain hyphens for the raid command family. */
        Pattern pattern = Pattern.compile("@#([\\w-]+)(?:\\[(\\d+)\\]|\\.)");
        Matcher matcher = pattern.matcher(currentText);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            /* Underscores are aliases for hyphens: @#raid_ice. works around
             * keyboards whose autocorrect eats a hyphen before the dot. */
            String keyword = matcher.group(1).toLowerCase().replace('_', '-');
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
