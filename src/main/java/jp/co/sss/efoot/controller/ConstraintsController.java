// src/main/java/jp/co/sss/efoot/controller/ConstraintsController.java
package jp.co.sss.efoot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.sss.efoot.entity.ConstraintItem;
import jp.co.sss.efoot.entity.ConstraintSet;
import jp.co.sss.efoot.repository.ConstraintItemRepository;
import jp.co.sss.efoot.repository.ConstraintSetRepository;

@Controller
@RequestMapping("/constraints")
public class ConstraintsController {

    private final ConstraintSetRepository setRepo;
    private final ConstraintItemRepository itemRepo;

    public ConstraintsController(ConstraintSetRepository setRepo, ConstraintItemRepository itemRepo) {
        this.setRepo = setRepo;
        this.itemRepo = itemRepo;
    }

    /** 一覧（保存・読み込み・削除） */
    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "type", required = false) String type, // nationality|team|league
            Model model,
            HttpSession session) {

        // 検索フィルタは簡易に：全件→メモリフィルタ（必要ならRepositoryに検索メソッド追加）
        List<ConstraintSet> all = setRepo.findAll(); // 並び替えたいなら Sort.by("updatedAt").descending()
        String q2 = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        Integer typeCode = toTypeCodeNullable(type);

        List<RowVM> rows = new ArrayList<>();
        for (ConstraintSet s : all) {
            if (!q2.isEmpty() && !s.getTitle().toLowerCase(Locale.ROOT).contains(q2)) continue;
            if (typeCode != null && !typeCode.equals(s.getTypeCode())) continue;
            long count = itemRepo.countBySetId(s.getId());
            rows.add(new RowVM(s.getId(), s.getTitle(), s.getTypeCode(), count, s.getUpdatedAt() == null ? null : s.getUpdatedAt().toString()));
        }

        model.addAttribute("rows", rows);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("type", type == null ? "" : type);

        // セッション中の下書き（Topから引き継いだものをプレビュー表示）
        @SuppressWarnings("unchecked")
        List<String> sessionItems = (List<String>) session.getAttribute(TopController.SESSION_KEY_ITEMS);
        model.addAttribute("draftTitle", (String) session.getAttribute(TopController.SESSION_KEY_TITLE));
        model.addAttribute("draftType", (String) session.getAttribute(TopController.SESSION_KEY_TYPE));
        model.addAttribute("draftItems", sessionItems == null ? List.of() : sessionItems);

        return "constraints"; // /templates/constraints.html
    }

    /** セッションの下書きをDBへ保存 */
    @PostMapping("/save")
    @Transactional
    public String saveFromSession(HttpSession session, RedirectAttributes ra) {
        String title = (String) session.getAttribute(TopController.SESSION_KEY_TITLE);
        String type = (String) session.getAttribute(TopController.SESSION_KEY_TYPE);
        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) session.getAttribute(TopController.SESSION_KEY_ITEMS);

        if (title == null || title.isBlank() || type == null) {
            ra.addFlashAttribute("error", "Top画面でタイトル/種別/値を入力してください。");
            return "redirect:/constraints";
        }

        // ConstraintSet 保存
        ConstraintSet set = new ConstraintSet();
        set.setTitle(title.trim());
        set.setTypeCode(toTypeCode(type));
        ConstraintSet saved = setRepo.save(set);

        // Item 保存（ord採番）
        if (items != null && !items.isEmpty()) {
            List<ConstraintItem> rows = new ArrayList<>(items.size());
            int ord = 1;
            for (String v : items) {
                if (v == null || v.isBlank()) continue;
                ConstraintItem ci = new ConstraintItem();
                ci.setSetId(saved.getId());
                ci.setValue(v.trim());
                ci.setOrd(ord++);
                rows.add(ci);
            }
            if (!rows.isEmpty()) itemRepo.saveAll(rows);
        }

        ra.addFlashAttribute("success", "縛りを保存しました（ID: " + saved.getId() + "）");
        return "redirect:/constraints";
    }

    /** 保存済みセットをセッションに読み込んで Top へ戻す */
    @PostMapping("/apply/{id}")
    public String applyToSession(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        ConstraintSet set = setRepo.findById(id).orElse(null);
        if (set == null) {
            ra.addFlashAttribute("error", "指定の縛りが見つかりません。");
            return "redirect:/constraints";
        }
        List<ConstraintItem> items = itemRepo.findBySetIdOrderByOrdAscIdAsc(id);
        List<String> values = items.stream().map(ConstraintItem::getValue).toList();

        session.setAttribute(TopController.SESSION_KEY_TITLE, set.getTitle());
        session.setAttribute(TopController.SESSION_KEY_TYPE, toTypeString(set.getTypeCode()));
        session.setAttribute(TopController.SESSION_KEY_ITEMS, values);

        ra.addFlashAttribute("success", "縛りを読み込みました。Top画面に反映しています。");
        return "redirect:/top";
    }

    /** 保存済みセットを削除（子も先に削除） */
    @PostMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        itemRepo.deleteBySetId(id);
        setRepo.deleteById(id);
        ra.addFlashAttribute("success", "縛りを削除しました（ID: " + id + "）");
        return "redirect:/constraints";
    }

    // ---- ユーティリティ ----

    private static int toTypeCode(String type) {
        return switch (type) {
            case "nationality" -> 1;
            case "team" -> 2;
            case "league" -> 3;
            default -> throw new IllegalArgumentException("unknown type: " + type);
        };
    }
    private static Integer toTypeCodeNullable(String type) {
        if (type == null || type.isBlank()) return null;
        return toTypeCode(type);
    }

    private static String toTypeString(Integer code) {
        if (code == null) return "nationality";
        return switch (code) {
            case 1 -> "nationality";
            case 2 -> "team";
            case 3 -> "league";
            default -> "nationality";
        };
    }

    /** 一覧表示用VM */
    public record RowVM(Long id, String title, Integer typeCode, long itemCount, String updatedAt) {
        public String typeLabel() {
            return switch (typeCode == null ? -1 : typeCode) {
                case 1 -> "国籍";
                case 2 -> "チーム";
                case 3 -> "リーグ";
                default -> "-";
            };
        }
    }
}
