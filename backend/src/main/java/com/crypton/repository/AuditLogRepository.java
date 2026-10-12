package com.crypton.repository;

import com.crypton.model.AuditLog;
import com.crypton.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserOrderByTimestampDesc(User user);

    List<AuditLog> findByUserUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByUserAndAction(User user, String action);
}
