package org.esp.domain.blueprint;

import it.jrc.domain.auth.Role;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(schema = "blueprint", name = "message")
public class Message{
    
    private Long id;

    private String text;
    
    private Message feedback;
    
    private Role author;
    
    private Date dateCreated;
    
    private Boolean opened;
    
    private EcosystemServiceIndicator ecosystemServiceIndicator;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "blueprint.status_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @OneToOne(cascade=CascadeType.ALL)
    public Message getFeedback() {
        return feedback;
    }

    public void setFeedback(Message feedback) {
        this.feedback = feedback;
    }

    public Boolean getOpened() {
        return opened;
    }

    public void setOpened(Boolean opened) {
        this.opened = opened;
    }

    @Column(name = "date_created")
    @Generated(value = GenerationTime.INSERT)
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @OneToOne
    public Role getAuthor() {
        return author;
    }

    public void setAuthor(Role author) {
        this.author = author;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public EcosystemServiceIndicator getEcosystemServiceIndicator() {
        return ecosystemServiceIndicator;
    }

    public void setEcosystemServiceIndicator(EcosystemServiceIndicator ecosystemServiceIndicator) {
        this.ecosystemServiceIndicator = ecosystemServiceIndicator;
    }
    
}
