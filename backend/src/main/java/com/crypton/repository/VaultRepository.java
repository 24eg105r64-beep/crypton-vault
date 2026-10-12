package com.crypton.repository;

import com.crypton.model.User;
import com.crypton.model.Vault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultRepository extends JpaRepository<Vault, Long> {

    List<Vault> findByUser(User user);

    List<Vault> findByUserUserId(Long userId);

    Optional<Vault> findByVaultIdAndUser(Long vaultId, User user);
}
