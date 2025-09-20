// src/main/java/jp/co/sss/efoot/controller/ConfirmController.java
package jp.co.sss.efoot.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.co.sss.efoot.entity.ConstraintItem;
import jp.co.sss.efoot.entity.ConstraintSet;
import jp.co.sss.efoot.entity.Formation;
import jp.co.sss.efoot.entity.FormationSlot;
import jp.co.sss.efoot.entity.Lineup;
import jp.co.sss.efoot.entity.LineupBenchAssignment;
import jp.co.sss.efoot.entity.LineupSlotAssignment;
import jp.co.sss.efoot.repository.ConstraintItemRepository;
import jp.co.sss.efoot.repository.ConstraintSetRepository;
import jp.co.sss.efoot.repository.FormationRepository;
import jp.co.sss.efoot.repository.FormationSlotRepository;
import jp.co.sss.efoot.repository.LineupBenchAssignmentRepository;
import jp.co.sss.efoot.repository.LineupRepository;
import jp.co.sss.efoot.repository.LineupSlotAssignmentRepository;

@Controller
public class ConfirmController {

    public static final String SESSION_KEY_SEED = "lineupSeed";

    private final ConstraintSetRepository setRepo;
    private final ConstraintItemRepository itemRepo;
    private final FormationRepository formationRepo;
    private final FormationSlotRepository slotRepo;
    private final LineupRepository lineupRepo;
    private final LineupSlotAssignmentRepository lsaRepo;
    private final LineupBenchAssignmentRepository lbaRepo;

    public ConfirmController(
            ConstraintSetRepository setRepo,
            ConstraintItemRepository itemRepo,
            FormationRepository formationRepo,
            FormationSlotRepository slotRepo,
            LineupRepository lineupRepo,
            LineupSlotAssignmentRepository lsaRepo,
            LineupBenchAssignmentRepository lbaRepo) {
        this.setRepo = setRepo;
        this.itemRepo = itemRepo;
        this.formationRepo = formationRepo;
        this.slotRepo = slotRepo;
        this.lineupRepo = lineupRepo;
        this.lsaRepo = lsaRepo;
        this.lbaRepo = lbaRepo;
    }

    /** 表示：セッションのフォーメーション＆縛りから割当をプレビュー */
    @GetMapping("/confirm")
    public String show(Model model, HttpSession session, RedirectAttributes ra) {

        // 1) 前画面のデータを確認
        @SuppressWarnings("unchecked")
        List<FormationController.SlotVM> slots =
                (List<FormationController.SlotVM>) session.getAttribute(FormationController.SESSION_KEY_FORMATION_SLOTS);
        String fName = (String) session.getAttribute(FormationController.SESSION_KEY_FORMATION_NAME);

        if (slots == null || slots.size() != 11) {
            ra.addFlashAttribute("error", "フォーメーションが未設定です。");
            return "redirect:/formation";
        }
        if (fName == null || fName.isBlank()) fName = "CUSTOM";

        String title = (String) session.getAttribute(TopController.SESSION_KEY_TITLE);
        String type = (String) session.getAttribute(TopController.SESSION_KEY_TYPE);
        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) session.getAttribute(TopController.SESSION_KEY_ITEMS);
        if (items == null) items = List.of();

        // 2) シード（固定すれば画面リロードで結果が変わらない）
        Long seed = (Long) session.getAttribute(SESSION_KEY_SEED);
        if (seed == null) {
            seed = ThreadLocalRandom.current().nextLong();
            session.setAttribute(SESSION_KEY_SEED, seed);
        }

        // 3) ランダム割当（11+ベンチ12）
        Random rng = new Random(seed);
        List<String> usedForMain = pickFor(slots.size(), items, rng);
        List<String> bench = pickFor(12, items, rng);

        // 4) ビューへ
        model.addAttribute("formationName", fName);
        model.addAttribute("slots", slots);
        model.addAttribute("assigned", usedForMain);
        model.addAttribute("bench", bench);
        model.addAttribute("constraintTitle", (title == null || title.isBlank()) ? "（未入力）" : title);
        model.addAttribute("constraintType", type == null ? "-" : typeLabel(type));
        model.addAttribute("seed", seed);

        return "confirm";
    }

    /** もう一度抽選（シード更新） */
    @PostMapping("/confirm/reroll")
    public String reroll(HttpSession session) {
        session.setAttribute(SESSION_KEY_SEED, ThreadLocalRandom.current().nextLong());
        return "redirect:/confirm";
    }

    /** 保存：formation/slots, constraint_set/items, lineup, assignments を一気に保存 */
    @PostMapping("/confirm/save")
    @Transactional
    public String save(HttpSession session, RedirectAttributes ra) {

        // 1) 取り出し
        @SuppressWarnings("unchecked")
        List<FormationController.SlotVM> slots =
                (List<FormationController.SlotVM>) session.getAttribute(FormationController.SESSION_KEY_FORMATION_SLOTS);
        String fName = (String) session.getAttribute(FormationController.SESSION_KEY_FORMATION_NAME);
        String title = (String) session.getAttribute(TopController.SESSION_KEY_TITLE);
        String type = (String) session.getAttribute(TopController.SESSION_KEY_TYPE);
        @SuppressWarnings("unchecked")
        List<String> items = (List<String>) session.getAttribute(TopController.SESSION_KEY_ITEMS);
        Long seed = (Long) session.getAttribute(SESSION_KEY_SEED);
        if (seed == null) seed = Instant.now().toEpochMilli();

        if (slots == null || slots.size() != 11) {
            ra.addFlashAttribute("error", "フォーメーションが未設定です。");
            return "redirect:/formation";
        }
        if (fName == null || fName.isBlank()) fName = "CUSTOM";
        if (items == null) items = List.of();
        if (type == null) type = "nationality";
        if (title == null || title.isBlank()) title = "Untitled";

        // 2) Formation 保存
        Formation formation = new Formation();
        formation.setName(fName);
        Formation savedFormation = formationRepo.save(formation);

        // slots 保存（ord 1..11）
        List<FormationSlot> slotRows = new ArrayList<>(11);
        int ord = 1;
        for (FormationController.SlotVM s : slots) {
            FormationSlot row = new FormationSlot();
            row.setFormationId(savedFormation.getId());
            row.setLabel(s.getLabel());
            row.setxPct(s.getXPct()); // BigDecimal
            row.setyPct(s.getYPct()); // BigDecimal
            row.setOrd(ord++);
            slotRows.add(row);
        }
        List<FormationSlot> savedSlots = slotRepo.saveAll(slotRows);

        // 3) ConstraintSet 保存（Topからの下書きをそのまま保存）
        ConstraintSet cs = new ConstraintSet();
        cs.setTitle(title.trim());
        cs.setTypeCode(switch (type) {
            case "nationality" -> 1;
            case "team" -> 2;
            case "league" -> 3;
            default -> 1;
        });
        ConstraintSet savedSet = setRepo.save(cs);

        if (!items.isEmpty()) {
            List<ConstraintItem> ciRows = new ArrayList<>(items.size());
            int i = 1;
            for (String v : items) {
                if (v == null || v.isBlank()) continue;
                ConstraintItem ci = new ConstraintItem();
                ci.setSetId(savedSet.getId());
                ci.setValue(v.trim());
                ci.setOrd(i++);
                ciRows.add(ci);
            }
            if (!ciRows.isEmpty()) itemRepo.saveAll(ciRows);
        }

        // 4) Lineup 保存
        Lineup lu = new Lineup();
        lu.setSetId(savedSet.getId());
        lu.setFormationId(savedFormation.getId());
        lu.setSeed(seed);
        Lineup savedLineup = lineupRepo.save(lu);

        // 5) 割当をもう一度（サーバ側で確定）
        Random rng = new Random(seed);
        List<String> mainAssigned = pickFor(11, items, rng);
        List<String> bench = pickFor(12, items, rng);

        // 6) Main assignments 保存（slotId は保存済み順序の対応で）
        List<LineupSlotAssignment> lsa = new ArrayList<>(11);
        for (int i2 = 0; i2 < 11; i2++) {
            LineupSlotAssignment r = new LineupSlotAssignment();
            r.setLineupId(savedLineup.getId());
            r.setSlotId(savedSlots.get(i2).getId().intValue());
            r.setSlotNumber(i2 + 1);     
            r.setItemValue(mainAssigned.get(i2));
            lsa.add(r);
        }
        lsaRepo.saveAll(lsa);

        // 7) Bench 保存
        List<LineupBenchAssignment> lba = new ArrayList<>(12);
        for (int i2 = 0; i2 < 12; i2++) {
            LineupBenchAssignment b = new LineupBenchAssignment();
            b.setLineupId(savedLineup.getId());
            b.setBenchNo(i2 + 1);
            b.setItemValue(bench.get(i2));
            lba.add(b);
        }
        lbaRepo.saveAll(lba);

        ra.addFlashAttribute("success", "確定メンバーを保存しました（Lineup ID: " + savedLineup.getId() + "）");
        return "redirect:/confirm";
    }

    // ----------------- ユーティリティ -----------------

    /** n件を items からランダム選択。items が足りれば重複なし、足りなければ重複ありで埋める。空なら「FREE」。 */
    private static List<String> pickFor(int n, List<String> items, Random rng) {
        List<String> out = new ArrayList<>(n);
        if (items == null || items.isEmpty()) {
            for (int i = 0; i < n; i++) out.add("FREE");
            return out;
        }
        if (items.size() >= n) {
            List<String> copy = new ArrayList<>(items);
            Collections.shuffle(copy, rng);
            out.addAll(copy.subList(0, n));
        } else {
            for (int i = 0; i < n; i++) {
                out.add(items.get(rng.nextInt(items.size())));
            }
        }
        return out;
    }

    private static String typeLabel(String type) {
        return switch (type) {
            case "nationality" -> "国籍";
            case "team" -> "チーム";
            case "league" -> "リーグ";
            default -> "-";
        };
    }
}
