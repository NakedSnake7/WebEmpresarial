package com.webempresarial.store.controller;

import com.webempresarial.store.dto.CloudinaryUploadResult;
import com.webempresarial.store.dto.ResenaEditDTO;
import com.webempresarial.store.entity.ResenaEntity;
import com.webempresarial.store.mapper.ResenaMapper;
import com.webempresarial.store.model.Resena;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.CloudinaryService;
import com.webempresarial.store.theme.StoreResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    public ResenaController(
            CloudinaryService cloudinaryService,
            ResenaRepository resenaRepository,
            StoreResolver storeResolver
    ) {
        this.cloudinaryService = cloudinaryService;
        this.resenaRepository = resenaRepository;
        this.storeResolver = storeResolver;
    }

    @ModelAttribute("resena")
    public Resena initResena() {
        return new Resena();
    }

    @GetMapping("/nueva")
    public String formResena(
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        Resena resena = new Resena();
        resena.setEstrellas(null);

        model.addAttribute("resena", resena);

        model.addAttribute(
                "resenas",
                resenaRepository.findByStoreOrderByEstrellasDesc(store)
                        .stream()
                        .map(ResenaMapper::toModel)
                        .toList()
        );

        return "admin/reviews";
    }

    @PostMapping("/nueva")
    public String guardarResena(
            @ModelAttribute Resena resena,
            Model model,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        try {
            if (resena.getImagen() == null ||
                    resena.getImagen().isEmpty()) {

                throw new IllegalArgumentException(
                        "La imagen es obligatoria"
                );
            }

            CloudinaryUploadResult upload =
                    cloudinaryService.subirImagen(
                            resena.getImagen()
                    );

            resena.setImagenUrl(upload.getSecureUrl());
            resena.setPublicId(upload.getPublicId());

            ResenaEntity entity =
                    ResenaMapper.toEntity(resena);

            entity.setStore(store);

            resenaRepository.save(entity);

            return "redirect:/resenas/nueva?success";

        } catch (IllegalArgumentException e) {

            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("resena", resena);

            model.addAttribute(
                    "resenas",
                    resenaRepository.findByStoreOrderByEstrellasDesc(store)
                            .stream()
                            .map(ResenaMapper::toModel)
                            .toList()
            );

            return "admin/reviews";

        } catch (Exception e) {
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
        Store store = storeResolver.getCurrentStore(request);

        Map<String, Object> response = new HashMap<>();

        return resenaRepository.findByIdAndStore(id, store)
                .map(resena -> {

                    if (resena.getPublicId() != null) {
                        cloudinaryService.eliminarImagen(
                                resena.getPublicId()
                        );
                    }

                    resenaRepository.delete(resena);

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
            @RequestBody ResenaEditDTO dto,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

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
        Store store = storeResolver.getCurrentStore(request);

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
        Store store = storeResolver.getCurrentStore(request);

        ResenaEntity resena =
                resenaRepository.findByIdAndStore(id, store)
                        .orElseThrow(() ->
                                new RuntimeException("No encontrada")
                        );

        try {
            if (resena.getPublicId() != null) {
                cloudinaryService.eliminarImagen(
                        resena.getPublicId()
                );
            }

            CloudinaryUploadResult upload =
                    cloudinaryService.subirImagen(imagen);

            resena.setImagenUrl(upload.getSecureUrl());
            resena.setPublicId(upload.getPublicId());

            return resenaRepository.save(resena);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir imagen");
        }
    }

    @DeleteMapping("/{id}/imagen")
    @ResponseBody
    public ResponseEntity<?> eliminarImagen(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Store store = storeResolver.getCurrentStore(request);

        ResenaEntity resena =
                resenaRepository.findByIdAndStore(id, store)
                        .orElseThrow(() ->
                                new RuntimeException("No encontrada")
                        );

        if (resena.getPublicId() != null) {
            cloudinaryService.eliminarImagen(
                    resena.getPublicId()
            );
        }

        resena.setImagenUrl(null);
        resena.setPublicId(null);

        resenaRepository.save(resena);

        return ResponseEntity.ok().build();
    }
}