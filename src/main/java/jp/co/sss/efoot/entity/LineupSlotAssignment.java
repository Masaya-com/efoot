package jp.co.sss.efoot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lineup_slot_assignment")
public class LineupSlotAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="lineup_id", nullable=false)
    private Long lineupId;
    
    @Column(name="slot_id", nullable=false)
    private Integer slotId;

    @Column(name="item_value", nullable=false, length=120)
    private String itemValue;
    
    @Column(name="slot_number", nullable=false)
    private Integer slotNumber;
    
    // Getter and Setter

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

	public Integer getSlotId() {
		return slotId;
	}

	public void setSlotId(Integer slotId) {
		this.slotId = slotId;
	}

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}

	public Integer getSlotNumber() {
		return slotNumber;
	}

	public void setSlotNumber(Integer slotNumber) {
		this.slotNumber = slotNumber;
	}
	
    
    
}

