package com.webempresarial.store.controller;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Resena;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsPermissionIsolationContractTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    private Store store;
    private ResenaController controller;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeResolver.getCurrentStore(request)
        ).thenReturn(store);

        controller =
                new ResenaController(
                        cloudinaryService,
                        resenaRepository,
                        storeResolver,
                        featureAccessService,
                        currentAdminAuthorizationService
                );
    }

    @Test
    void createOnlyUserMustNotLoadExistingReviews() {

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.REVIEW_VIEW
                )
        ).thenReturn(false);

        String view =
                controller.formResena(
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo("admin/reviews");

        verify(
                resenaRepository,
                never()
        ).findByStoreOrderByEstrellasDesc(
                any(Store.class)
        );
    }

    @Test
    void createOnlyValidationErrorMustNotLoadExistingReviews() {

        when(
                currentAdminAuthorizationService.hasPermission(
                        AdminPermission.REVIEW_VIEW
                )
        ).thenReturn(false);

        Resena resena = new Resena();

        String view =
                controller.guardarResena(
                        resena,
                        model,
                        request
                );

        assertThat(view)
                .isEqualTo("admin/reviews");

        verify(
                resenaRepository,
                never()
        ).findByStoreOrderByEstrellasDesc(
                any(Store.class)
        );

        verifyNoInteractions(
                cloudinaryService
        );
    }
}
