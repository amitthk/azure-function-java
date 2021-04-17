package com.amitthk.azure.java.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {
    public static String printStackTraceToString(Exception exc){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exc.printStackTrace(pw);
        return(sw.toString());
    }
}
