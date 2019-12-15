package com.a51work6.myaudioplayer.ui;

public class Util {

    public static String timeToString(long duration) {
        if (duration < 0)
            return "00:00";
        StringBuffer sb = new StringBuffer();
        long m = duration / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (duration % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }

}
