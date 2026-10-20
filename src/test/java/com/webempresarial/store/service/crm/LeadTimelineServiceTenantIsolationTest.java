package com.webempresarial.store.service.crm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.webempresarial.store.repository.LeadActivityRepository;
import com.webempresarial.store.repository.LeadAuditLogRepository;

class LeadTimelineServiceTenantIsolationTest {

    @Test
    void timelineShouldScopeActivitiesAndAuditsByStore() {

        LeadActivityRepository activityRepository =
                mock(LeadActivityRepository.class);

        LeadAuditLogRepository auditLogRepository =
                mock(LeadAuditLogRepository.class);

        when(
                activityRepository
                        .findByLeadIdAndLeadStoreIdOrderByCreatedAtDesc(
                                55L,
                                10L
                        )
        ).thenReturn(List.of());

        when(
                auditLogRepository
                        .findByLeadIdAndStoreIdOrderByCreatedAtDesc(
                                55L,
                                10L
                        )
        ).thenReturn(List.of());

        LeadTimelineService service =
                new LeadTimelineService(
                        activityRepository,
                        auditLogRepository
                );

        assertThat(
                service.getTimeline(
                        55L,
                        10L
                )
        ).isEmpty();

        verify(activityRepository)
                .findByLeadIdAndLeadStoreIdOrderByCreatedAtDesc(
                        55L,
                        10L
                );

        verify(activityRepository, never())
                .findByLeadIdOrderByCreatedAtDesc(
                        anyLong()
                );

        verify(auditLogRepository)
                .findByLeadIdAndStoreIdOrderByCreatedAtDesc(
                        55L,
                        10L
                );
    }
}
