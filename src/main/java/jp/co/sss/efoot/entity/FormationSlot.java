package jp.co.sss.efoot.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "formation_slot")
public class FormationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="formation_id", nullable=false)
    private Long formationId;

    @Column(nullable=false, length=16)
    private String label; // GK/RB/ST など

    @Column(name="x_pct", nullable=false, precision=5, scale=2)
    private BigDecimal xPct; // -35.00〜35.00

    @Column(name="y_pct", nullable=false, precision=5, scale=2)
    private BigDecimal yPct; // 10.00〜90.00

    @Column(name="ord", nullable=false)
    private Integer ord = 0;
    
    //getter/setter

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getFormationId() {
		return formationId;
	}

	public void setFormationId(Long formationId) {
		this.formationId = formationId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BigDecimal getxPct() {
		return xPct;
	}

	public void setxPct(BigDecimal xPct) {
		this.xPct = xPct;
	}

	public BigDecimal getyPct() {
		return yPct;
	}

	public void setyPct(BigDecimal yPct) {
		this.yPct = yPct;
	}

	public Integer getOrd() {
		return ord;
	}

	public void setOrd(Integer ord) {
		this.ord = ord;
	}
   
    
}

