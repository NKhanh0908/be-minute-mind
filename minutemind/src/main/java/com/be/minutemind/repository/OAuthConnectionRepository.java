package com.be.minutemind.repository;

import com.be.minutemind.entities.OAuthConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, Long> {
    Optional<OAuthConnection> findByProviderAndProviderUserId(String provider, String providerUserId);
}
