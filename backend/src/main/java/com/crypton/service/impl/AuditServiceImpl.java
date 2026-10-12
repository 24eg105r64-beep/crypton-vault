package com.crypton.service.impl;

import com.crypton.model.AuditLog;
import com.crypton.model.User;
import com.crypton.repository.AuditLogRepository;
import com.crypton.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLog logAction(User user, String action, String deviceInfo) {
        if (deviceInfo == null || deviceInfo.isBlank()) {
            deviceInfo = resolveCurrentDeviceInfo();
        }

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .deviceInfo(deviceInfo)
                .timestamp(LocalDateTime.now())
                .build();

        return auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public AuditLog logAction(User user, String action) {
        return logAction(user, action, resolveCurrentDeviceInfo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(User user) {
        return auditLogRepository.findByUserOrderByTimestampDesc(user);
    }

    private String resolveCurrentDeviceInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && !userAgent.isBlank()) {
                    return userAgent;
                }
            }
        } catch (Exception ignored) {
            // Outside of active web request context
        }
        return "Unknown Device";
    }
}
