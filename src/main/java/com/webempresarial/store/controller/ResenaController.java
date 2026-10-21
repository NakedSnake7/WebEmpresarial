package com.webempresarial.store.controller;

import com.webempresarial.store.dto.CloudinaryUploadResult;
import com.webempresarial.store.dto.ResenaEditDTO;
import com.webempresarial.store.entity.ResenaEntity;
import com.webempresarial.store.mapper.ResenaMapper;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Resena;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.service.CurrentAdminAuthorizationService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/resenas")
public class ResenaController {

    private final CloudinaryService cloudinaryService;
    private final ResenaRepository resenaRepository;
    private final StoreResolver storeResolver;
    private final FeatureAccessService featureAccessService;
    private final CurrentAdminAuthorizationService
            currentAdminAuthorizationService;

    public ResenaController(
            CloudinaryService cloudinaryService,
            ResenaRepository resenaRepository,
            StoreResolver storeResolver,
            FeatureAccessService featureAccessService,
            CurrentAdminAuthorizationService
                    currentAdminAuthorizationService
    ) {
        this.cloudinaryService = cloudinaryService;
        this.resenaRepository = resenaRepository;
        this.storeResolver = storeResolver;
        this.featureAccessService = featureAccessService;
        this.currentAdminAuthorizationService =
                currentAdminAuthorizationService;
    }

    @ModelAttribute("resena")
    public Resena initResena() {
        return new Resena();
    }

    /*
     * id, imagenUrl y publicId pertenecen al servidor.
     *
     * Aunque Resena también se usa como modelo de vista,
     * estos campos nunca deben poder ser establecidos desde
     * parámetros del request.
     */
    @InitBinder("resena")
    void initResenaBinder(
            WebDataBinder binder
    ) {
        binder.setDisallowedFields(
                "id",
                "imagenUrl",
                "publicId"
        );
    }

    @GetMapping("/nueva")
    public String formResena(
            Model model,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        Resena resena = new Resena();
        resena.setEstrellas(null);

        model.addAttribute("resena", resena);

        addReviewsIfAllowed(
                model,
                store
        );

        return "admin/reviews";
    }

    @PostMapping("/nueva")
    public String guardarResena(
            @Valid
            @ModelAttribute("resena")
            Resena resena,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        /*
         * La validación debe ocurrir antes de Cloudinary y
         * antes de cualquier persistencia.
         */
        if (bindingResult.hasErrors()) {

            String errorMsg =
                    bindingResult.getFieldError() != null
                            ? bindingResult
                                    .getFieldError()
                                    .getDefaultMessage()
                            : "Revisa los datos de la reseña";

            model.addAttribute(
                    "errorMsg",
                    errorMsg
            );

            addReviewsIfAllowed(
                    model,
                    store
            );

            return "admin/reviews";
        }

        CloudinaryUploadResult upload = null;

        try {
            if (resena.getImagen() == null ||
                    resena.getImagen().isEmpty()) {

                throw new IllegalArgumentException(
                        "La imagen es obligatoria"
                );
            }

            upload =
                    cloudinaryService.subirImagen(
                            resena.getImagen()
                    );

            resena.setImagenUrl(upload.getSecureUrl());
            resena.setPublicId(upload.getPublicId());

            ResenaEntity entity =
                    ResenaMapper.toEntity(resena);

            entity.setStore(store);

            resenaRepository.saveAndFlush(entity);

            return "redirect:/resenas/nueva?success";

        } catch (IllegalArgumentException e) {

            if (upload != null) {
                eliminarImagenRemotaBestEffort(
                        upload.getPublicId()
                );
            }

            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("resena", resena);

            addReviewsIfAllowed(
                    model,
                    store
            );

            return "admin/reviews";

        } catch (Exception e) {

            if (upload != null) {
                eliminarImagenRemotaBestEffort(
                        upload.getPublicId()
                );
            }

            e.printStackTrace();
            return "redirect:/resenas/nueva?error";
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public Map<String, Object> eliminarResena(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        Map<String, Object> response = new HashMap<>();

        return resenaRepository.findByIdAndStore(id, store)
                .map(resena -> {

                    String publicId =
                            resena.getPublicId();

                    resenaRepository.delete(resena);
                    resenaRepository.flush();

                    eliminarImagenRemotaBestEffort(
                            publicId
                    );

                    response.put("success", true);
                    return response;

                }).orElseGet(() -> {
                    response.put("success", false);
                    return response;
                });
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResenaEntity editarResena(
            @PathVariable Long id,
            @Valid
            @RequestBody
            ResenaEditDTO dto,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        ResenaEntity resena =
                resenaRepository.findByIdAndStore(id, store)
                        .orElseThrow(() ->
                                new RuntimeException("No encontrada")
                        );

        resena.setComentario(dto.getComentario());
        resena.setEstrellas(dto.getEstrellas());

        return resenaRepository.save(resena);
    }

    @GetMapping
    public String listarResenas(
            Model model,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        model.addAttribute("resena", new Resena());

        model.addAttribute(
                "resenas",
                resenaRepository.findByStoreOrderByEstrellasDesc(store)
                        .stream()
                        .map(ResenaMapper::toModel)
                        .toList()
        );

        return "admin/reviews";
    }

    @PutMapping("/{id}/imagen")
    @ResponseBody
    public ResenaEntity actualizarImagen(
            @PathVariable Long id,
            @RequestParam("imagen") MultipartFile imagen,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        ResenaEntity resena =
                resenaRepository.findByIdAndStore(id, store)
                        .orElseThrow(() ->
                                new RuntimeException("No encontrada")
                        );

        String previousImageUrl =
                resena.getImagenUrl();

        String previousPublicId =
                resena.getPublicId();

        CloudinaryUploadResult upload;

        try {
            upload =
                    cloudinaryService.subirImagen(imagen);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al subir imagen",
                    e
            );
        }

        resena.setImagenUrl(
                upload.getSecureUrl()
        );
        resena.setPublicId(
                upload.getPublicId()
        );

        ResenaEntity saved;

        try {
            saved =
                    resenaRepository.saveAndFlush(resena);

        } catch (Exception e) {

            resena.setImagenUrl(
                    previousImageUrl
            );
            resena.setPublicId(
                    previousPublicId
            );

            eliminarImagenRemotaBestEffort(
                    upload.getPublicId()
            );

            throw new RuntimeException(
                    "Error al guardar imagen",
                    e
            );
        }

        if (previousPublicId != null &&
                !previousPublicId.equals(
                        upload.getPublicId()
                )) {

            eliminarImagenRemotaBestEffort(
                    previousPublicId
            );
        }

        return saved;
    }

    @DeleteMapping("/{id}/imagen")
    @ResponseBody
    public ResponseEntity<?> eliminarImagen(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = currentStoreWithReviews(request);

        ResenaEntity resena =
                resenaRepository.findByIdAndStore(id, store)
                        .orElseThrow(() ->
                                new RuntimeException("No encontrada")
                        );

        String previousPublicId =
                resena.getPublicId();

        resena.setImagenUrl(null);
        resena.setPublicId(null);

        resenaRepository.saveAndFlush(resena);

        eliminarImagenRemotaBestEffort(
                previousPublicId
        );

        return ResponseEntity.ok().build();
    }

    private void eliminarImagenRemotaBestEffort(
            String publicId
    ) {

        if (publicId == null ||
                publicId.isBlank()) {
            return;
        }

        try {
            cloudinaryService.eliminarImagen(
                    publicId
            );
        } catch (RuntimeException ignored) {
            // La consistencia local ya fue establecida.
            // La limpieza remota es best-effort.
        }
    }

    private void addReviewsIfAllowed(
            Model model,
            Store store
    ) {

        if (!currentAdminAuthorizationService.hasPermission(
                AdminPermission.REVIEW_VIEW
        )) {
            return;
        }

        model.addAttribute(
                "resenas",
                resenaRepository.findByStoreOrderByEstrellasDesc(store)
                        .stream()
                        .map(ResenaMapper::toModel)
                        .toList()
        );
    }

    private Store currentStoreWithReviews(
            HttpServletRequest request
    ) {
        Store store =
                storeResolver.getCurrentStore(request);

        featureAccessService.requireFeature(
                store,
                Feature.REVIEWS
        );

        return store;
    }
}
