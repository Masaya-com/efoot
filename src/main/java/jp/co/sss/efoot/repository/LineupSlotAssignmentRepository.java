package jp.co.sss.efoot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.efoot.entity.LineupSlotAssignment;

public interface LineupSlotAssignmentRepository extends JpaRepository<LineupSlotAssignment, Long> {
    List<LineupSlotAssignment> findByLineupId(Long lineupId);
    long deleteByLineupId(Long lineupId);
    boolean existsByLineupIdAndSlotId(Long lineupId, Integer slotId);
}
