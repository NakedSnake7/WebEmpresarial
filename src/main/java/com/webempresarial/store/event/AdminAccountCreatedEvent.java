package com.webempresarial.store.event;

public record AdminAccountCreatedEvent(
        Long adminUserId,
        String email,
        String fullName,
        String storeName,
        String storeDomain,
        String activationToken
) {
}