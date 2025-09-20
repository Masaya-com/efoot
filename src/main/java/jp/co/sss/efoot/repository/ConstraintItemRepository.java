package jp.co.sss.efoot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.efoot.entity.ConstraintItem;

public interface ConstraintItemRepository extends JpaRepository<ConstraintItem, Long> {
    List<ConstraintItem> findBySetIdOrderByOrdAscIdAsc(Long setId);
    long deleteBySetId(Long setId);
    
    long countBySetId(Long setId);

    boolean existsBySetIdAndValue(Long setId, String value);
}