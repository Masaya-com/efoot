package jp.co.sss.efoot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lineup_bench_assignment")
public class LineupBenchAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="lineup_id", nullable=false)
    private Long lineupId;

    @Column(name="bench_no", nullable=false)
    private Integer benchNo; // 1..12

    @Column(name="item_value", nullable=false, length=120)
    private String itemValue;
    
    // getters and setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLineupId() {
		return lineupId;
	}

	public void setLineupId(Long lineupId) {
		this.lineupId = lineupId;
	}

	public Integer getBenchNo() {
		return benchNo;
	}

	public void setBenchNo(Integer benchNo) {
		this.benchNo = benchNo;
	}

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}
    
    
}
