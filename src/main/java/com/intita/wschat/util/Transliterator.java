package com.intita.wschat.util;
import java.util.HashMap;
import java.util.Map;

public class Transliterator {

    private static final Map<Character, String> charMap = new HashMap<Character, String>();

    static {
        charMap.put('À', "A");
        charMap.put('Á', "B");
        charMap.put('Â', "V");
        charMap.put('Ã', "G");
        charMap.put('Ä', "D");
        charMap.put('Å', "E");
        charMap.put('¨', "E");
        charMap.put('Æ', "Zh");
        charMap.put('Ç', "Z");
        charMap.put('È', "I");
        charMap.put('²', "I");
        charMap.put('¯', "I");
        charMap.put('É', "I");
        charMap.put('Ê', "K");
        charMap.put('Ë', "L");
        charMap.put('Ì', "M");
        charMap.put('Í', "N");
        charMap.put('Î', "O");
        charMap.put('Ï', "P");
        charMap.put('Ğ', "R");
        charMap.put('Ñ', "S");
        charMap.put('Ò', "T");
        charMap.put('Ó', "U");
        charMap.put('Ô', "F");
        charMap.put('Õ', "H");
        charMap.put('Ö', "C");
        charMap.put('×', "Ch");
        charMap.put('Ø', "Sh");
        charMap.put('Ù', "Sh");
        charMap.put('Ú', "'");
        charMap.put('Û', "Y");
        charMap.put('Ü', "'");
        charMap.put('İ', "E");
        charMap.put('Ş', "U");
        charMap.put('ß', "Ya");
        charMap.put('à', "a");
        charMap.put('á', "b");
        charMap.put('â', "v");
        charMap.put('ã', "g");
        charMap.put('ä', "d");
        charMap.put('å', "e");
        charMap.put('¸', "e");
        charMap.put('æ', "zh");
        charMap.put('ç', "z");
        charMap.put('è', "i");
        charMap.put('³', "i");
        charMap.put('¿', "i");
        charMap.put('é', "y");
        charMap.put('ê', "k");
        charMap.put('ë', "l");
        charMap.put('ì', "m");
        charMap.put('í', "n");
        charMap.put('î', "o");
        charMap.put('ï', "p");
        charMap.put('ğ', "r");
        charMap.put('ñ', "s");
        charMap.put('ò', "t");
        charMap.put('ó', "u");
        charMap.put('ô', "f");
        charMap.put('õ', "h");
        charMap.put('ö', "c");
        charMap.put('÷', "ch");
        charMap.put('ø', "sh");
        charMap.put('ù', "sh");
        charMap.put('ú', "'");
        charMap.put('û', "y");
        charMap.put('ü', "'");
        charMap.put('ı', "e");
        charMap.put('ş', "u");
        charMap.put('ÿ', "ya");

    }

    public static String transliterate(String string) {
        StringBuilder transliteratedString = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            Character ch = string.charAt(i);
            String charFromMap = charMap.get(ch);
            if (charFromMap == null) {
                transliteratedString.append(ch);
            } else {
                transliteratedString.append(charFromMap);
            }
        }
        return transliteratedString.toString();
    }
}