package dk.skat.ict.sspts.batch.business;

import dk.skat.ict.sspts.batch.enums.AfregningType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 *
 */
@Entity
@Table(indexes = { @Index(name = "IDX_POSTERING", columnList = "angivelseId, type") })
public class Afregning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "angivelseId")
    private Angivelse angivelse;

    @Column(nullable = false)
    private String kontoNummer;

    @Column(nullable = false)
    private long beloeb;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AfregningType type;

    @ManyToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name = "leveranceId")
    private Leverance leverance;

    @Column
    @CreationTimestamp
    private Date createDateTime;

    @Column
    @UpdateTimestamp
    private Date updateDateTime;

    public Afregning() {}

    public Afregning(Angivelse angivelse, String kontoNummer, long beloeb, AfregningType type) {
        this.setAngivelse(angivelse);
        this.setKontoNummer(kontoNummer);
        this.setBeloeb(beloeb);
        this.setType(type);
    }

    public Angivelse getAngivelse() {
        return angivelse;
    }

    public String getKontoNummer() {
        return kontoNummer;
    }

    public long getBeloeb() {
        return beloeb;
    }

    public Leverance getLeverance() {
        return leverance;
    }

    public void setLeverance(Leverance leverance) {
        this.leverance = leverance;
        leverance.getAfregningList().add(this);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAngivelse(Angivelse angivelse) {
        this.angivelse = angivelse;
    }

    @Enumerated(EnumType.STRING)
    public AfregningType getType() {
        return type;
    }

    public void setType(AfregningType type) {
        this.type = type;
    }

    public void setKontoNummer(String kontoNummer) {
        this.kontoNummer = kontoNummer;
    }

    public void setBeloeb(long beloeb) {
        this.beloeb = beloeb;
    }
}
