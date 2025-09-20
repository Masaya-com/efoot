// src/main/java/jp/co/sss/efoot/controller/FormationController.java
package jp.co.sss.efoot.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * フォーメーション作成画面
 * - GET /formation     : 画面表示（セッションにあれば復元、なければ4-3-3で初期化）
 * - POST /formation/next : 配置とラベルをセッションへ保存し、確定画面へ
 */
@Controller
public class FormationController {

    public static final String SESSION_KEY_FORMATION_NAME  = "formationName";
    public static final String SESSION_KEY_FORMATION_SLOTS = "formationSlots"; // List<SlotVM>

    /** 表示 */
    @GetMapping("/formation")
    public String show(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<SlotVM> slots = (List<SlotVM>) session.getAttribute(SESSION_KEY_FORMATION_SLOTS);
        if (slots == null || slots.size() != 11) {
            slots = defaults433();
        }
        String fname = (String) session.getAttribute(SESSION_KEY_FORMATION_NAME);
        if (fname == null || fname.isBlank()) fname = "4-3-3";

        model.addAttribute("formationName", fname);
        model.addAttribute("slots", slots); // 11枠（label, xPct(-35..35), yPct(10..90)）

        return "formation";
    }

    /** 決定 → セッション保存 → 確定画面へ */
    @PostMapping("/formation/next")
    public String next(
            @RequestParam(name="formationName", required=false) String formationName,
            @RequestParam("label") List<String> labels,
            @RequestParam("xPct") List<String> xPcts,
            @RequestParam("yPct") List<String> yPcts,
            HttpSession session,
            RedirectAttributes ra) {

        int n = Math.min(labels.size(), Math.min(xPcts.size(), yPcts.size()));
        if (n < 11) {
            ra.addFlashAttribute("error", "スロットが不足しています。");
            return "redirect:/formation";
        }

        List<SlotVM> out = new ArrayList<>(11);
        for (int i = 0; i < 11; i++) {
            String label = safe(labels.get(i));
            BigDecimal x = parseBD(xPcts.get(i));
            BigDecimal y = parseBD(yPcts.get(i));
            x = clampBD(x, bd(-35), bd(35));
            y = clampBD(y, bd(10), bd(90));
            out.add(new SlotVM(label.isBlank() ? defaultLabel(i) : label, scale2(x), scale2(y)));
        }

        session.setAttribute(SESSION_KEY_FORMATION_NAME,
                (formationName == null || formationName.isBlank()) ? "CUSTOM" : formationName.trim());
        session.setAttribute(SESSION_KEY_FORMATION_SLOTS, out);

        return "redirect:/confirm";
    }

    // ---------- ユーティリティ ----------

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static BigDecimal parseBD(String s) {
        try { return new BigDecimal(s.trim()); } catch (Exception e) { return bd(0); }
    }
    private static BigDecimal bd(double d) { return BigDecimal.valueOf(d); }
    private static BigDecimal clampBD(BigDecimal v, BigDecimal min, BigDecimal max) {
        if (v.compareTo(min) < 0) return min;
        if (v.compareTo(max) > 0) return max;
        return v;
    }
    private static BigDecimal scale2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    /** デフォルトは 4-3-3 */
    private static List<SlotVM> defaults433() {
        List<SlotVM> s = new ArrayList<>(11);
        // フォワード列（上段：y=35）
        s.add(new SlotVM("RW", bd(20), bd(32)));
        s.add(new SlotVM("CF", bd(0),  bd(26)));
        s.add(new SlotVM("LW", bd(-20),bd(32)));
        // 中盤（中段：y=55〜60）
        s.add(new SlotVM("ROMF", bd(10), bd(44)));
        s.add(new SlotVM("DMF", bd(0),  bd(58)));
        s.add(new SlotVM("LOMF", bd(-10),bd(44)));
        // DF（下段：y=75）
        s.add(new SlotVM("RB", bd(25), bd(67)));
        s.add(new SlotVM("RCB",bd(10), bd(70)));
        s.add(new SlotVM("LCB",bd(-10),bd(70)));
        s.add(new SlotVM("LB", bd(-25),bd(67)));
        // GK（最下段：y=90）
        s.add(new SlotVM("GK", bd(0), bd(79)));
        return s;
    }

    private static String defaultLabel(int i) {
        return switch (i) {
            case 0 -> "RW"; case 1 -> "CF"; case 2 -> "LW";
            case 3 -> "ROMF"; case 4 -> "DMF"; case 5 -> "LOMF";
            case 6 -> "RB"; case 7 -> "RCB"; case 8 -> "LCB"; case 9 -> "LB";
            default -> "GK";
        };
    }

    /** 画面表示用のシンプルVM */
    public static class SlotVM {
        private String label;        // 例: GK/RB/ST
        private BigDecimal xPct;     // -35..35 （左マイナス/右プラス）
        private BigDecimal yPct;     // 10..90  （上→下）

        public SlotVM() {}
        public SlotVM(String label, BigDecimal xPct, BigDecimal yPct) {
            this.label = label; this.xPct = xPct; this.yPct = yPct;
        }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public BigDecimal getXPct() { return xPct; }
        public void setXPct(BigDecimal xPct) { this.xPct = xPct; }
        public BigDecimal getYPct() { return yPct; }
        public void setYPct(BigDecimal yPct) { this.yPct = yPct; }
    }
}
