package br.senai.portaria.util;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Datas {
    private Datas() { }

    public static String hojeIso() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }
    public static String iso(int ano, int mesZeroBase, int dia) {
        return String.format(Locale.US, "%04d-%02d-%02d", ano, mesZeroBase + 1, dia);
    }
    public static String paraTela(String iso) {      // 2026-12-03 -> 03/12/2026
        String[] p = iso.split("-");
        return p[2] + "/" + p[1] + "/" + p[0];
    }
    public static String hora(Timestamp t) {          // null logo após gravar
        return t == null ? "agora"
                : new SimpleDateFormat("HH:mm", new Locale("pt", "BR")).format(t.toDate());
    }
}
