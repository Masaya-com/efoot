package jp.co.sss.efoot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.efoot.entity.LineupBenchAssignment;

public interface LineupBenchAssignmentRepository extends JpaRepository<LineupBenchAssignment, Long> {
    List<LineupBenchAssignment> findByLineupIdOrderByBenchNoAsc(Long lineupId);
    long deleteByLineupId(Long lineupId);
    boolean existsByLineupIdAndBenchNo(Long lineupId, Integer benchNo);
}