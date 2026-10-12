package com.crypton.service.impl;

import com.crypton.exception.AccessDeniedException;
import com.crypton.exception.ResourceNotFoundException;
import com.crypton.model.User;
import com.crypton.model.Vault;
import com.crypton.repository.VaultRepository;
import com.crypton.service.AuditService;
import com.crypton.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultRepository vaultRepository;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public Vault createVault(String vaultName, User user) {
        if (vaultName == null || vaultName.isBlank()) {
            throw new IllegalArgumentException("Vault name cannot be empty");
        }

        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);
        String encryptionKeyRef = Base64.getEncoder().encodeToString(keyBytes);

        Vault vault = Vault.builder()
                .vaultName(vaultName.trim())
                .encryptionKeyRef(encryptionKeyRef)
                .user(user)
                .build();

        Vault saved = vaultRepository.save(vault);
        auditService.logAction(user, "VAULT_CREATED");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vault> getUserVaults(User user) {
        if (user == null) {
            throw new AccessDeniedException("User must be authenticated");
        }
        return vaultRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Vault getVaultById(Long vaultId, User user) {
        return findAndValidateOwnership(vaultId, user);
    }

    @Override
    @Transactional
    public Vault renameVault(Long vaultId, String newName, User user) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("New vault name cannot be empty");
        }

        Vault vault = findAndValidateOwnership(vaultId, user);
        vault.setVaultName(newName.trim());
        return vaultRepository.save(vault);
    }

    @Override
    @Transactional
    public void deleteVault(Long vaultId, User user) {
        Vault vault = findAndValidateOwnership(vaultId, user);
        vaultRepository.delete(vault);
        auditService.logAction(user, "VAULT_DELETED");
    }

    private Vault findAndValidateOwnership(Long vaultId, User user) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Vault not found with id: " + vaultId));

        if (user == null || vault.getUser() == null || !vault.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("Access denied: You do not own vault with id " + vaultId);
        }
        return vault;
    }
}
