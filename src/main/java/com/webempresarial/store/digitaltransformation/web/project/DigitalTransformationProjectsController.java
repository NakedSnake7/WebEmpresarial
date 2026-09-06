package com.webempresarial.store.digitaltransformation.web.project;

import com.webempresarial.store.digitaltransformation.application.project.api.TransformationProjectOptionResponse;
import com.webempresarial.store.digitaltransformation.application.project.api.TransformationProjectSelectionQuery;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin/digital-transformation/projects")
public class DigitalTransformationProjectsController {

    private final TransformationProjectSelectionQuery
            projectSelectionQuery;

    private final StoreResolver
            storeResolver;

    public DigitalTransformationProjectsController(
            TransformationProjectSelectionQuery projectSelectionQuery,
            StoreResolver storeResolver
    ) {
        this.projectSelectionQuery =
                Objects.requireNonNull(
                        projectSelectionQuery,
                        "TransformationProjectSelectionQuery es obligatorio"
                );

        this.storeResolver =
                Objects.requireNonNull(
                        storeResolver,
                        "StoreResolver es obligatorio"
                );
    }

    @GetMapping
    public String projects(
            HttpServletRequest request,
            Model model
    ) {
        Store store =
                resolveStore(
                        request
                );

        List<TransformationProjectOptionResponse> projects =
                Objects.requireNonNull(
                        projectSelectionQuery.findAvailableProjects(
                                store.getId()
                        ),
                        "TransformationProjectSelectionQuery devolvió una lista nula"
                );

        model.addAttribute(
                "pageTitle",
                "Digital Transformation"
        );

        model.addAttribute(
                "pageDescription",
                "Selecciona un proyecto para acceder a su inteligencia estratégica"
        );

        model.addAttribute(
                "transformationProjects",
                projects
        );

        model.addAttribute(
                "digitalTransformationPage",
                true
        );

        return "admin/digital-transformation/projects/index";
    }

    private Store resolveStore(
            HttpServletRequest request
    ) {
        Objects.requireNonNull(
                request,
                "HttpServletRequest es obligatorio"
        );

        final Store store;

        try {
            store =
                    storeResolver.getCurrentStore(
                            request
                    );
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "No fue posible resolver la tienda de la solicitud",
                    exception
            );
        }

        if (store == null || store.getId() == null) {
            throw new IllegalStateException(
                    "No fue posible resolver la tienda de la solicitud"
            );
        }

        if (!store.isActiva()) {
            throw new IllegalStateException(
                    "La tienda se encuentra inactiva"
            );
        }

        return store;
    }
}