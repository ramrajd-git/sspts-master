package dk.skat.ict.sspts.batch.business;

import dk.skat.ict.sspts.batch.enums.AfregningType;
import dk.skat.ict.sspts.batch.enums.SourceSystem;
import dk.skat.ict.sspts.batch.enums.SourceSystemConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by mns on 07-03-2017.
 */
@Entity
@Table(
        uniqueConstraints={@UniqueConstraint(columnNames = {"system", "referenceNummer", "procedure"})},
        indexes = {@Index(name = "ang_id", columnList = "id", unique = true)})
public class Angivelse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String seNummer;

    @Column(nullable = false)
    private String registratorSeNummer;

    @Column(nullable = false)
    private long referenceNummer;

    @Column(nullable = false)
    private Date periodeDato;

    @Column(nullable = false)
    private Date antagelseDato;

    @Column(nullable = false)
    private int status;

    @Column(nullable = false)
    @Convert(converter = SourceSystemConverter.class)
    private SourceSystem system;

    @Column(nullable = false)
    private String procedure;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "angivelse")
    @Where(clause = "type='POSTERING'")
    private List<Afregning> postering;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "angivelse")
    @Where(clause = "type='MODPOSTERING'")
    private List<Afregning> modpostering;

    @Column
    @CreationTimestamp
    private Date createDateTime;

    @Column
    @UpdateTimestamp
    private Date updateDateTime;

    public Angivelse() {}

    public Angivelse(String seNummer, String registratorSeNummer, long referenceNummer, Date periodeDato, Date antagelseDato, int status, SourceSystem system, String procedure) {
        this.seNummer = seNummer;
        this.registratorSeNummer = registratorSeNummer;
        this.referenceNummer = referenceNummer;
        this.periodeDato = periodeDato;
        this.antagelseDato = antagelseDato;
        this.status = status;
        this.system = system;
        this.procedure = procedure;
    }

    public String getSeNummer() {
        return seNummer;
    }

    public String getRegistratorSeNummer() {
        return registratorSeNummer;
    }

    public long getReferenceNummer() {
        return referenceNummer;
    }

    public Date getPeriodeDato() {
        return periodeDato;
    }

    public Date getAntagelseDato() {
        return antagelseDato;
    }

    public int getStatus() {
        return status;
    }

    public SourceSystem getSystem() { return system; }

    public String getProcedure() {
        return procedure;
    }

    public Afregning getPostering() {
        if(postering.isEmpty())  {
            return null;
        }
        return postering.get(0);
    }

    public Afregning getModpostering() {
        if(modpostering.isEmpty())  {
            return null;
        }
        return modpostering.get(0);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public boolean hasPostering() {
        return getPostering() != null;
    }

    public boolean hasModpostering() {
        return getModpostering() != null;
    }
}
