package com.webempresarial.store.controller;

import com.webempresarial.store.dto.CloudinaryUploadResult;
import com.webempresarial.store.entity.ResenaEntity;
import com.webempresarial.store.model.Resena;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsImageLifecycleContractTest {

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
    void replacementShouldPersistNewImageBeforeDeletingOldImage()
            throws Exception {

        ResenaEntity entity =
                reviewWithImage(
                        "old-url",
                        "old-public-id"
                );

        MockMultipartFile image =
                image();

        CloudinaryUploadResult upload =
                new CloudinaryUploadResult(
                        "new-url",
                        "new-public-id"
                );

        when(
                resenaRepository.findByIdAndStore(
                        1L,
                        store
                )
        ).thenReturn(
                Optional.of(entity)
        );

        when(
                cloudinaryService.subirImagen(image)
        ).thenReturn(upload);

        when(
                resenaRepository.saveAndFlush(entity)
        ).thenReturn(entity);

        ResenaEntity result =
                controller.actualizarImagen(
                        1L,
                        image,
                        request
                );

        assertThat(result.getImagenUrl())
                .isEqualTo("new-url");

        assertThat(result.getPublicId())
                .isEqualTo("new-public-id");

        InOrder order =
                inOrder(
                        cloudinaryService,
                        resenaRepository
                );

        order.verify(cloudinaryService)
                .subirImagen(image);

        order.verify(resenaRepository)
                .saveAndFlush(entity);

        order.verify(cloudinaryService)
                .eliminarImagen(
                        "old-public-id"
                );
    }

    @Test
    void failedReplacementUploadShouldKeepPreviousRemoteImage()
            throws Exception {

        ResenaEntity entity =
                reviewWithImage(
                        "old-url",
                        "old-public-id"
                );

        MockMultipartFile image =
                image();

        when(
                resenaRepository.findByIdAndStore(
                        1L,
                        store
                )
        ).thenReturn(
                Optional.of(entity)
        );

        when(
                cloudinaryService.subirImagen(image)
        ).thenThrow(
                new IOException("upload failed")
        );

        assertThatThrownBy(
                () ->
                        controller.actualizarImagen(
                                1L,
                                image,
                                request
                        )
        )
        .isInstanceOf(RuntimeException.class);

        verify(
                resenaRepository,
                never()
        ).saveAndFlush(any());

        verify(
                cloudinaryService,
                never()
        ).eliminarImagen(
                "old-public-id"
        );

        assertThat(entity.getImagenUrl())
                .isEqualTo("old-url");

        assertThat(entity.getPublicId())
                .isEqualTo("old-public-id");
    }

    @Test
    void failedReplacementPersistenceShouldCleanNewUploadAndKeepOldRemote()
            throws Exception {

        ResenaEntity entity =
                reviewWithImage(
                        "old-url",
                        "old-public-id"
                );

        MockMultipartFile image =
                image();

        CloudinaryUploadResult upload =
                new CloudinaryUploadResult(
                        "new-url",
                        "new-public-id"
                );

        when(
                resenaRepository.findByIdAndStore(
                        1L,
                        store
                )
        ).thenReturn(
                Optional.of(entity)
        );

        when(
                cloudinaryService.subirImagen(image)
        ).thenReturn(upload);

        when(
                resenaRepository.saveAndFlush(entity)
        ).thenThrow(
                new RuntimeException("database failed")
        );

        assertThatThrownBy(
                () ->
                        controller.actualizarImagen(
                                1L,
                                image,
                                request
                        )
        )
        .isInstanceOf(RuntimeException.class);

        verify(cloudinaryService)
                .eliminarImagen(
                        "new-public-id"
                );

        verify(
                cloudinaryService,
                never()
        ).eliminarImagen(
                "old-public-id"
        );

        assertThat(entity.getImagenUrl())
                .isEqualTo("old-url");

        assertThat(entity.getPublicId())
                .isEqualTo("old-public-id");
    }

    @Test
    void deleteImageShouldPersistNullReferenceBeforeRemoteCleanup() {

        ResenaEntity entity =
                reviewWithImage(
                        "old-url",
                        "old-public-id"
                );

        when(
                resenaRepository.findByIdAndStore(
                        1L,
                        store
                )
        ).thenReturn(
                Optional.of(entity)
        );

        when(
                resenaRepository.saveAndFlush(entity)
        ).thenReturn(entity);

        controller.eliminarImagen(
                1L,
                request
        );

        assertThat(entity.getImagenUrl())
                .isNull();

        assertThat(entity.getPublicId())
                .isNull();

        InOrder order =
                inOrder(
                        resenaRepository,
                        cloudinaryService
                );

        order.verify(resenaRepository)
                .saveAndFlush(entity);

        order.verify(cloudinaryService)
                .eliminarImagen(
                        "old-public-id"
                );
    }

    @Test
    void deleteReviewShouldFlushDatabaseBeforeRemoteCleanup() {

        ResenaEntity entity =
                reviewWithImage(
                        "old-url",
                        "old-public-id"
                );

        when(
                resenaRepository.findByIdAndStore(
                        1L,
                        store
                )
        ).thenReturn(
                Optional.of(entity)
        );

        controller.eliminarResena(
                1L,
                request
        );

        InOrder order =
                inOrder(
                        resenaRepository,
                        cloudinaryService
                );

        order.verify(resenaRepository)
                .delete(entity);

        order.verify(resenaRepository)
                .flush();

        order.verify(cloudinaryService)
                .eliminarImagen(
                        "old-public-id"
                );
    }

    @Test
    void failedCreatePersistenceShouldCleanUploadedImage()
            throws Exception {

        MockMultipartFile image =
                image();

        Resena review =
                new Resena();

        review.setNombre("Cliente");
        review.setComentario("Excelente");
        review.setEstrellas(5);
        review.setImagen(image);

        CloudinaryUploadResult upload =
                new CloudinaryUploadResult(
                        "new-url",
                        "new-public-id"
                );

        when(
                cloudinaryService.subirImagen(image)
        ).thenReturn(upload);

        when(
                resenaRepository.saveAndFlush(
                        any(ResenaEntity.class)
                )
        ).thenThrow(
                new RuntimeException("database failed")
        );

        String result =
                controller.guardarResena(
                        review,
                        new org.springframework.validation
                                .BeanPropertyBindingResult(
                                        review,
                                        "resena"
                                ),
                        new ConcurrentModel(),
                        request
                );

        assertThat(result)
                .isEqualTo(
                        "redirect:/resenas/nueva?error"
                );

        verify(cloudinaryService)
                .eliminarImagen(
                        "new-public-id"
                );
    }

    private ResenaEntity reviewWithImage(
            String imageUrl,
            String publicId
    ) {

        ResenaEntity entity =
                new ResenaEntity();

        entity.setStore(store);
        entity.setImagenUrl(imageUrl);
        entity.setPublicId(publicId);

        return entity;
    }

    private MockMultipartFile image() {

        return new MockMultipartFile(
                "imagen",
                "review.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3}
        );
    }
}
