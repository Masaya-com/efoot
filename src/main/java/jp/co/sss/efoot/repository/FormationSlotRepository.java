package jp.co.sss.efoot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.efoot.entity.FormationSlot;

public interface FormationSlotRepository extends JpaRepository<FormationSlot, Long> {
    List<FormationSlot> findByFormationIdOrderByOrdAscIdAsc(Long formationId);
}
