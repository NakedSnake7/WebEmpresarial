package com.webempresarial.store.service;

import com.webempresarial.store.repository.AdminUserRepository;
import jakarta.persistence.LockModeType;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import static org.junit.jupiter.api.Assertions.*;

class AdminPasswordChangeLockContractTest {

    @Test
    void credentialLookupUsesPessimisticWriteLock()
            throws Exception {

        var method = AdminUserRepository.class.getMethod(
                "findByEmailForUpdate",
                String.class
        );

        Lock lock = method.getAnnotation(Lock.class);
        Query query = method.getAnnotation(Query.class);

        assertNotNull(lock);
        assertEquals(
                LockModeType.PESSIMISTIC_WRITE,
                lock.value()
        );

        assertNotNull(query);
        assertTrue(
                query.value().contains("AdminUser")
        );
    }
}
