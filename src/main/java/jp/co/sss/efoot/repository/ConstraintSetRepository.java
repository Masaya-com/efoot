package jp.co.sss.efoot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.efoot.entity.ConstraintSet;

public interface ConstraintSetRepository extends JpaRepository<ConstraintSet, Long> {
    long countByTitleContaining(String keyword);
}
