// src/main/java/jp/co/sss/efoot/controller/TopController.java
package jp.co.sss.efoot.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.sss.efoot.form.TopForm; // ★ 追加

/**
 * Top画面（縛り入力）のコントローラ。
 * - GET "/" : 画面表示
 * - POST "/constraints/next" : 入力をセッションへ保持しフォーメーション画面へ
 */
@Controller
public class TopController {

    /** セッション格納キー */
    public static final String SESSION_KEY_TITLE = "constraintTitle";
    public static final String SESSION_KEY_TYPE = "constraintType";   // nationality | team | league
    public static final String SESSION_KEY_ITEMS = "constraintItems"; // List<String>

    private static final Set<String> ALLOWED_TYPES = Set.of("nationality", "team", "league");
    private static final int MAX_ITEMS = 100;
    private static final int MAX_ITEM_LENGTH = 120;

    /** 画面表示 */
    @GetMapping({"/", "/top"})
    public String showTop(Model model, HttpSession session) {
        TopForm form = new TopForm();
        Object t = session.getAttribute(SESSION_KEY_TITLE);
        Object ty = session.getAttribute(SESSION_KEY_TYPE);
        @SuppressWarnings("unchecked")
        List<String> it = (List<String>) session.getAttribute(SESSION_KEY_ITEMS);

        form.setTitle(t instanceof String s && !s.isBlank() ? s : "");
        form.setType(ty instanceof String s && ALLOWED_TYPES.contains(s) ? s : "nationality");
        form.setItems(it != null ? new ArrayList<>(it) : new ArrayList<>());

        // HTMLは個別nameを使っているため、単体属性で渡す
        model.addAttribute("title", form.getTitle());
        model.addAttribute("type", form.getType());
        model.addAttribute("items", form.getItems());
        model.addAttribute("projectTitle", form.getTitle().isBlank() ? "無題プロジェクト" : form.getTitle());

        // （任意）Thymeleafの#fieldsを使うなら、フォーム本体も渡しておく
        model.addAttribute("topForm", form);

        return "top"; // /templates/top.html
    }

    /** 決定：フォーメーションへ */
    @PostMapping("/constraints/next")
    public String submitTop(
            @Validated @ModelAttribute TopForm form,
            BindingResult br,
            HttpSession session,
            RedirectAttributes ra,
            Model model) {

        if (!ALLOWED_TYPES.contains(form.getType())) {
            br.rejectValue("type", "invalid.type", "縛りの種類が不正です。");
        }

        // items 整形
        List<String> normalized = normalizeItems(form.getItems());
        if (normalized.size() > MAX_ITEMS) {
            br.rejectValue("items", "too.many", "登録できるのは最大100件です。");
        }
        if (normalized.stream().anyMatch(s -> s.length() > MAX_ITEM_LENGTH)) {
            br.rejectValue("items", "too.long", "各値は120文字以内で入力してください。");
        }

        if (form.getTitle() == null || form.getTitle().isBlank()) {
            br.rejectValue("title", "required", "タイトルを入力してください。");
        }

        if (br.hasErrors()) {
            model.addAttribute("title", form.getTitle());
            model.addAttribute("type", form.getType());
            model.addAttribute("items", normalized);
            model.addAttribute("projectTitle", form.getTitle().isBlank() ? "無題プロジェクト" : form.getTitle());
            model.addAttribute("topForm", form); // （#fields用に）
            return "top";
        }

        // セッションへ保存
        session.setAttribute(SESSION_KEY_TITLE, form.getTitle().trim());
        session.setAttribute(SESSION_KEY_TYPE, form.getType());
        session.setAttribute(SESSION_KEY_ITEMS, normalized);

        if (normalized.isEmpty()) {
            ra.addFlashAttribute("info", "値が未入力のため、フリースタイルで進みます。");
        }

        return "redirect:/formation";
    }

    /** items 正規化：trim → 空白正規化 → 大文字小文字無視の重複除去 → 空要素除外 */
    private static List<String> normalizeItems(List<String> src) {
        if (src == null) return List.of();
        List<String> cleaned = new ArrayList<>();
        for (String s : src) {
            if (s == null) continue;
            String v = s.trim().replaceAll("\\s+", " "); // ★ タイポ修正
            if (!v.isEmpty()) cleaned.add(v);
        }
        // 重複除去（case-insensitive、入力順維持）
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        for (String v : cleaned) {
            String key = v.toLowerCase(Locale.ROOT);
            if (seen.add(key)) out.add(v);
        }
        // 長さ上限で切り詰め（安全サイド）
        out.replaceAll(x -> x.length() > MAX_ITEM_LENGTH ? x.substring(0, MAX_ITEM_LENGTH) : x);
        // 最大件数制限
        if (out.size() > MAX_ITEMS) {
            return new ArrayList<>(out.subList(0, MAX_ITEMS));
        }
        return out;
    }
}
