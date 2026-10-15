package com.webempresarial.store.controller;

import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Resena;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsFeatureAccessContractTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private FeatureAccessService featureAccessService;

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
                        featureAccessService
                );

        doThrow(
                new FeatureLockedException(
                        Feature.REVIEWS
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.REVIEWS
        );
    }

    @Test
    void reviewRoutesShouldRequireReviewsFeature() {

        Resena resena =
                new Resena();

        MockMultipartFile image =
                new MockMultipartFile(
                        "imagen",
                        "review.jpg",
                        "image/jpeg",
                        new byte[] {1}
                );

        assertThatThrownBy(
                () ->
                        controller.formResena(
                                model,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        controller.guardarResena(
                                resena,
                                model,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        controller.eliminarResena(
                                1L,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        controller.editarResena(
                                1L,
                                null,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        controller.listarResenas(
                                model,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        controller.actualizarImagen(
                                1L,
                                image,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        assertThatThrownBy(
                () ->
                        controller.eliminarImagen(
                                1L,
                                request
                        )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(
                featureAccessService,
                times(7)
        )
        .requireFeature(
                store,
                Feature.REVIEWS
        );

        verifyNoInteractions(
                cloudinaryService,
                resenaRepository
        );
    }
}
