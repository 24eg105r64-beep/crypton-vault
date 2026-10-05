package com.crypton.service;

import com.crypton.exception.AccessDeniedException;
import com.crypton.exception.ResourceNotFoundException;
import com.crypton.model.User;
import com.crypton.model.Vault;
import com.crypton.repository.VaultRepository;
import com.crypton.service.impl.VaultServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VaultServiceTest {

    @Mock
    private VaultRepository vaultRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private VaultServiceImpl vaultService;

    private User owner;
    private User otherUser;
    private Vault ownerVault;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .userId(1L)
                .name("Alice")
                .email("alice@crypton.local")
                .build();

        otherUser = User.builder()
                .userId(2L)
                .name("Bob")
                .email("bob@crypton.local")
                .build();

        ownerVault = Vault.builder()
                .vaultId(100L)
                .vaultName("Alice Savings")
                .encryptionKeyRef("base64EncodedKey123==")
                .user(owner)
                .build();
    }

    @Test
    @DisplayName("Create vault generates random Base64 encryptionKeyRef and assigns user")
    void testCreateVault() {
        when(vaultRepository.save(any(Vault.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vault created = vaultService.createVault("Primary Vault", owner);

        assertThat(created).isNotNull();
        assertThat(created.getVaultName()).isEqualTo("Primary Vault");
        assertThat(created.getUser()).isEqualTo(owner);
        assertThat(created.getEncryptionKeyRef()).isNotBlank();

        // Verify it is a valid Base64 encoded string of 32 bytes
        byte[] decodedKey = Base64.getDecoder().decode(created.getEncryptionKeyRef());
        assertThat(decodedKey).hasSize(32);

        verify(vaultRepository).save(any(Vault.class));
        verify(auditService).logAction(owner, "VAULT_CREATED");
    }

    @Test
    @DisplayName("Get vault by id succeeds when user owns the vault")
    void testGetVaultByIdOwnerSuccess() {
        when(vaultRepository.findById(100L)).thenReturn(Optional.of(ownerVault));

        Vault found = vaultService.getVaultById(100L, owner);

        assertThat(found).isNotNull();
        assertThat(found.getVaultId()).isEqualTo(100L);
        assertThat(found.getUser().getUserId()).isEqualTo(owner.getUserId());
    }

    @Test
    @DisplayName("Get vault by id throws AccessDeniedException when accessing another user's vault")
    void testGetVaultByIdAnotherUserThrowsAccessDenied() {
        when(vaultRepository.findById(100L)).thenReturn(Optional.of(ownerVault));

        assertThatThrownBy(() -> vaultService.getVaultById(100L, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied: You do not own vault with id 100");
    }

    @Test
    @DisplayName("Rename vault throws AccessDeniedException when accessing another user's vault")
    void testRenameVaultAnotherUserThrowsAccessDenied() {
        when(vaultRepository.findById(100L)).thenReturn(Optional.of(ownerVault));

        assertThatThrownBy(() -> vaultService.renameVault(100L, "Hacked Vault Name", otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied: You do not own vault with id 100");
    }

    @Test
    @DisplayName("Delete vault throws AccessDeniedException when accessing another user's vault")
    void testDeleteVaultAnotherUserThrowsAccessDenied() {
        when(vaultRepository.findById(100L)).thenReturn(Optional.of(ownerVault));

        assertThatThrownBy(() -> vaultService.deleteVault(100L, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied: You do not own vault with id 100");
    }

    @Test
    @DisplayName("Delete vault succeeds for owner and records audit log")
    void testDeleteVaultOwnerSuccess() {
        when(vaultRepository.findById(100L)).thenReturn(Optional.of(ownerVault));

        vaultService.deleteVault(100L, owner);

        verify(vaultRepository).delete(ownerVault);
        verify(auditService).logAction(owner, "VAULT_DELETED");
    }
}
