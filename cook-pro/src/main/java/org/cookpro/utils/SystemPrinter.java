package org.cookpro.utils;

import java.nio.charset.Charset;

public class SystemPrinter {



    private static void println(String message) {
        message = new String(message.getBytes(), Charset.forName("GBK"));
        System.out.println(message);
    }

    public static void println(Object message){
        if (message instanceof String strMessage) println(strMessage);
        else System.out.println(message);
    }

    public static void println(){
        System.out.println();
    }

}
