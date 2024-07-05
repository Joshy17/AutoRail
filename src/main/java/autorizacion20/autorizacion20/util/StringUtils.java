/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autorizacion20.autorizacion20.util;

/**
 *
 * @author XPC
 */
public class StringUtils {
    
        public static String padLeft(String input, int length, char padChar) {
        if (input.length() >= length) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - input.length()) {
            sb.append(padChar);
        }
        sb.append(input);
        return sb.toString();
    }
}
