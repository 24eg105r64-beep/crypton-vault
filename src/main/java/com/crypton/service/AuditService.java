package com.crypton.service;

import com.crypton.model.AuditLog;
import com.crypton.model.User;

import java.util.List;

public interface AuditService {

    AuditLog logAction(User user, String action, String deviceInfo);

    AuditLog logAction(User user, String action);

    List<AuditLog> getUserAuditLogs(User user);
}
