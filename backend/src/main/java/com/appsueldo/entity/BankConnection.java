package com.appsueldo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_connections")
public class BankConnection extends BaseTimestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankProvider provider = BankProvider.MANUAL;

    @Column(length = 255)
    private String providerConnectionId;

    @Column(length = 120)
    private String institutionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankConnectionStatus status = BankConnectionStatus.ACTIVE;

    // Sensitive future field: store only an encrypted token or vault reference here, never raw bank credentials.
    @Column(columnDefinition = "text")
    private String accessTokenRef;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BankProvider getProvider() {
        return provider;
    }

    public void setProvider(BankProvider provider) {
        this.provider = provider;
    }

    public String getProviderConnectionId() {
        return providerConnectionId;
    }

    public void setProviderConnectionId(String providerConnectionId) {
        this.providerConnectionId = providerConnectionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public BankConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(BankConnectionStatus status) {
        this.status = status;
    }

    public String getAccessTokenRef() {
        return accessTokenRef;
    }

    public void setAccessTokenRef(String accessTokenRef) {
        this.accessTokenRef = accessTokenRef;
    }
}
