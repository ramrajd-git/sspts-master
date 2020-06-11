package dk.skat.ict.sspts.batch.business;

import dk.skat.ict.sspts.batch.enums.DeliveryStatus;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mns on 07-03-2017.
 */
@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"system", "leverancenummer"})
})
public class Leverance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="leverancenummer")
    private int leveranceNummer;
    private Date leveranceDato;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus leveranceStatus;
    private String leveranceFilNavn;
    @Column(name="system")
    @Enumerated(EnumType.STRING)
    private SourceSystem system;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "leverance", cascade = CascadeType.ALL)
    private List<Afregning> afregningList;

    @Lob
    private byte[] leveranceFil;

    @Column
    @CreationTimestamp
    private Date createDateTime;

    @Column
    @UpdateTimestamp
    private Date updateDateTime;

    public Leverance() {
    }

    public Leverance(int leveranceNummer, SourceSystem system) {
        this.leveranceNummer = leveranceNummer;
        this.leveranceFilNavn = system.getFilename();
        this.leveranceStatus = DeliveryStatus.OPRETTET;
        this.setSystem(system);
        this.afregningList = new ArrayList<Afregning>();
    }

    public int getLeveranceNummer() {
        return leveranceNummer;
    }

    public void setLeveranceNummer(int leveranceNummer) {
        this.leveranceNummer = leveranceNummer;
    }

    public Date getLeveranceDato() {
        return leveranceDato;
    }

    public void setLeveranceDato(Date leveranceDato) {
        this.leveranceDato = leveranceDato;
    }

    public DeliveryStatus getLeveranceStatus() {
        return leveranceStatus;
    }

    public void setLeveranceStatus(DeliveryStatus leveranceStatus) {
        this.leveranceStatus = leveranceStatus;
    }

    public byte[] getLeveranceFil() {
        return leveranceFil;
    }

    public void setLeveranceFil(byte[] leveranceFil) {
        this.leveranceFil = leveranceFil;
    }

    public String getLeveranceFilNavn() {
        return leveranceFilNavn;
    }

    public void setLeveranceFilNavn(String leveranceFilNavn) {
        this.leveranceFilNavn = leveranceFilNavn;
    }

    public SourceSystem getSystem() {
        return system;
    }

    public void setSystem(SourceSystem system) {
        this.system = system;
    }

    public List<Afregning> getAfregningList() {
        return afregningList;
    }

    public void setAfregningList(List<Afregning> afregningList) {
        this.afregningList = afregningList;
    }
}
