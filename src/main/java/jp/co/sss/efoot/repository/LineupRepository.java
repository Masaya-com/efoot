package jp.co.sss.efoot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.sss.efoot.entity.Lineup;

public interface LineupRepository extends JpaRepository<Lineup, Long> {

    
}
