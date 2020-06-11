package dk.skat.ict.sspts.batch.business;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by mns on 07-03-2017.
 */
@Entity
public class Konfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String kontoNummer;
    private String kontoArt;
    private long beloeb;
    private String hjemDistrikt;
    private String tekstKode;
    private String oprettelse;
    private String sletning;
    private Date startDato;

    @Column
    @CreationTimestamp
    private Date createDateTime;

    @Column
    @UpdateTimestamp
    private Date updateDateTime;

    public Konfiguration() {}

    public Konfiguration(String kontoNummer, String kontoArt, long beloeb, String hjemDistrikt, String tekstKode, String oprettelse, String sletning, Date startDato) {
        this.kontoNummer = kontoNummer;
        this.kontoArt = kontoArt;
        this.beloeb = beloeb;
        this.hjemDistrikt = hjemDistrikt;
        this.tekstKode = tekstKode;
        this.oprettelse = oprettelse;
        this.sletning = sletning;
        this.startDato = startDato;
    }

    public Konfiguration(boolean test) {
        if(test){
            this.kontoNummer = "1450";
            this.kontoArt = "505";
            this.beloeb = 10;
            this.hjemDistrikt = "19";
            this.tekstKode = "020";
            this.oprettelse = "22200";
            this.sletning = "22500";
        }
    }

    public String getKontoNummer() {
        return kontoNummer;
    }

    public String getKontoArt() {
        return kontoArt;
    }

    public long getBeloeb() {
        return beloeb;
    }

    public String getHjemDistrikt() {
        return hjemDistrikt;
    }

    public String getTekstKode() {
        return tekstKode;
    }

    public String getOprettelse() {
        return oprettelse;
    }

    public String getSletning() {
        return sletning;
    }

    public Date getStartDato() {
        return startDato;
    }

    public Long getId() {
        return id;
    }
}
