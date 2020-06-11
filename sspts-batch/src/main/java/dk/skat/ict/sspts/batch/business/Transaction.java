package dk.skat.ict.sspts.batch.business;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by MNS on 15-03-2017.
 */
@Entity
@Table(
        indexes = {@Index(name = "trans_id", columnList = "id", unique = true),@Index(name = "ang_uni", columnList = "referenceNummer,system", unique = false)})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(length = 10, columnDefinition = "NUMBER(10)")
    private Integer seNummer;
    @Column(length = 10, columnDefinition = "NUMBER(10)")
    private Integer registratorSeNummer;
    @Column(length = 10, columnDefinition = "NUMBER(13)")
    private long referenceNummer;
    @Temporal(TemporalType.DATE)
    private Date periodeDato;
    @Temporal(TemporalType.DATE)
    private Date antagelseDato;
    @Column(length = 2, columnDefinition = "NCHAR(2)")
    private String status;
    @Column(length = 10, columnDefinition = "NVARCHAR2(10)")
    private String toldprocedure;
    @Column(length = 2, columnDefinition = "NCHAR(2)")
    private String system;

    @Column
    @CreationTimestamp
    private Date createDateTime;

    @Column
    @UpdateTimestamp
    private Date updateDateTime;

    public Transaction() {

    }

    public Transaction(Integer seNummer, Integer registratorSeNummer, long referenceNummer, Date periodeDato, Date antagelseDato, String status, String toldprocedure, String system) {
        this.seNummer = seNummer;
        this.registratorSeNummer = registratorSeNummer;
        this.referenceNummer = referenceNummer;
        this.periodeDato = periodeDato;
        this.antagelseDato = antagelseDato;
        this.status = status;
        this.toldprocedure = toldprocedure;
        this.system = system;
    }

    public long getId() {
        return id;
    }

    public int getSeNummer() {
        return seNummer;
    }

    public void setSeNummer(int seNummer) {
        this.seNummer = seNummer;
    }

    public int getRegistratorSeNummer() {
        return registratorSeNummer;
    }

    public void setRegistratorSeNummer(int registratorSeNummer) {
        this.registratorSeNummer = registratorSeNummer;
    }

    public long getReferenceNummer() {
        return referenceNummer;
    }

    public void setReferenceNummer(Long referenceNummer) {
        this.referenceNummer = referenceNummer;
    }

    public Date getPeriodeDato() {
        return periodeDato;
    }

    public void setPeriodeDato(Date periodeDato) {
        this.periodeDato = periodeDato;
    }

    public Date getAntagelseDato() {
        return antagelseDato;
    }

    public void setAntagelseDato(Date antagelseDato) {
        this.antagelseDato = antagelseDato;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToldprocedure() {
        return toldprocedure;
    }

    public void setToldprocedure(String toldprocedure) {
        this.toldprocedure = toldprocedure;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
}
