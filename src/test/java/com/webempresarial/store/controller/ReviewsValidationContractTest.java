package com.webempresarial.store.controller;

import com.webempresarial.store.dto.ResenaEditDTO;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Resena;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.WebDataBinder;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.view;

class ReviewsValidationContractTest {

    private CloudinaryService cloudinaryService;
    private ResenaRepository resenaRepository;
    private StoreResolver storeResolver;
    private FeatureAccessService featureAccessService;
    private CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    private ResenaController controller;
    private MockMvc mockMvc;
    private Validator validator;
    private Store store;

    @BeforeEach
    void setUp() {

        cloudinaryService =
                mock(CloudinaryService.class);

        resenaRepository =
                mock(ResenaRepository.class);

        storeResolver =
                mock(StoreResolver.class);

        featureAccessService =
                mock(FeatureAccessService.class);

        currentAdminAuthorizationService =
                mock(
                        CurrentAdminAuthorizationService.class
                );

        controller =
                new ResenaController(
                        cloudinaryService,
                        resenaRepository,
                        storeResolver,
                        featureAccessService,
                        currentAdminAuthorizationService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();

        validator =
                Validation
                        .buildDefaultValidatorFactory()
                        .getValidator();

        store = mock(Store.class);

        when(
                storeResolver.getCurrentStore(any())
        ).thenReturn(store);

        when(
                currentAdminAuthorizationService
                        .hasPermission(
                                AdminPermission.REVIEW_VIEW
                        )
        ).thenReturn(false);
    }

    @Test
    void createModelShouldRejectInvalidInput() {

        Resena resena = new Resena();

        resena.setNombre(" ");
        resena.setComentario(" ");
        resena.setEstrellas(6);

        Set<String> invalidProperties =
                validator.validate(resena)
                        .stream()
                        .map(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                        )
                        .collect(Collectors.toSet());

        assertThat(invalidProperties)
                .contains(
                        "nombre",
                        "comentario",
                        "estrellas"
                );
    }

    @Test
    void editDtoShouldRejectInvalidInput() {

        ResenaEditDTO dto =
                new ResenaEditDTO();

        dto.setComentario(" ");
        dto.setEstrellas(0);

        Set<String> invalidProperties =
                validator.validate(dto)
                        .stream()
                        .map(violation ->
                                violation
                                        .getPropertyPath()
                                        .toString()
                        )
                        .collect(Collectors.toSet());

        assertThat(invalidProperties)
                .contains(
                        "comentario",
                        "estrellas"
                );
    }

    @Test
    void invalidCreateShouldNotUploadOrPersist()
            throws Exception {

        mockMvc.perform(
                multipart("/resenas/nueva")
                        .param(
                                "nombre",
                                " "
                        )
                        .param(
                                "comentario",
                                "Comentario válido"
                        )
                        .param(
                                "estrellas",
                                "5"
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                view().name(
                        "admin/reviews"
                )
        );

        verifyNoInteractions(
                cloudinaryService
        );

        verify(
                resenaRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void invalidEditShouldReturnBadRequestBeforeRepository()
            throws Exception {

        mockMvc.perform(
                put("/resenas/7")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                """
                                {
                                  "comentario": " ",
                                  "estrellas": 9
                                }
                                """
                        )
        )
        .andExpect(
                status().isBadRequest()
        );

        verifyNoInteractions(
                resenaRepository
        );
    }

    @Test
    void serverControlledFieldsShouldBeSuppressedFromBinding() {

        Resena resena =
                new Resena();

        WebDataBinder binder =
                new WebDataBinder(
                        resena,
                        "resena"
                );

        controller.initResenaBinder(
                binder
        );

        MutablePropertyValues values =
                new MutablePropertyValues();

        values.add(
                "id",
                "999"
        );

        values.add(
                "imagenUrl",
                "https://attacker.test/image.jpg"
        );

        values.add(
                "publicId",
                "attacker-public-id"
        );

        values.add(
                "nombre",
                "Cliente válido"
        );

        values.add(
                "verificado",
                "true"
        );

        binder.bind(values);

        assertThat(resena.getId())
                .isNull();

        assertThat(resena.getImagenUrl())
                .isNull();

        assertThat(resena.getPublicId())
                .isNull();

        /*
         * verificado sí es un campo administrativo permitido.
         */
        assertThat(resena.isVerificado())
                .isTrue();

        assertThat(
                binder
                        .getBindingResult()
                        .getSuppressedFields()
        ).containsExactlyInAnyOrder(
                "id",
                "imagenUrl",
                "publicId"
        );
    }
}
