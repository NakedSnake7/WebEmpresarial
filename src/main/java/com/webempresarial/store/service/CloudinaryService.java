package com.webempresarial.store.service;

import com.cloudinary.Cloudinary; 
import com.cloudinary.utils.ObjectUtils;
import com.webempresarial.store.dto.CloudinaryUploadResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    // =========================================
    // SUBIR IMAGEN (MEJORADO)
    // =========================================
    public CloudinaryUploadResult subirImagen(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }

        // =========================================
        // VALIDACIÓN SOLO POR EXTENSIÓN (ROBUSTO)
        // =========================================
        String nombreOriginal = file.getOriginalFilename();

        if (nombreOriginal == null) {
            throw new IllegalArgumentException("Archivo inválido");
        }

        String nombre = nombreOriginal.toLowerCase();

        boolean extensionValida =
                nombre.endsWith(".jpg") ||
                nombre.endsWith(".jpeg") ||
                nombre.endsWith(".png") ||
                nombre.endsWith(".webp");

        if (!extensionValida) {
            throw new IllegalArgumentException(
                    "Formato no permitido. Solo JPG, JPEG, PNG y WEBP."
            );
        }

        // =========================================
        // ARCHIVO TEMPORAL SEGURO
        // =========================================
        File tempFile = File.createTempFile("upload-", ".tmp");
        file.transferTo(tempFile);

        try {

            // =========================================
            // UPLOAD OPTIMIZADO
            // =========================================
            Map<?, ?> result = cloudinary.uploader().upload(
                    tempFile,
                    ObjectUtils.asMap(
                            "folder", "productos",
                            "resource_type", "image",

                            // optimización automática moderna
                            "quality", "auto:good",
                            "format", "webp",

                            // control de tamaño
                            "width", 1000,
                            "height", 1000,
                            "crop", "limit"
                    )
            );

            return new CloudinaryUploadResult(
                    result.get("secure_url").toString(),
                    result.get("public_id").toString()
            );

        } finally {
            // =========================================
            // LIMPIEZA SEGURA
            // =========================================
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
 // =========================================
 // SUBIR LOGO DE TIENDA / BRANDING
 // =========================================
 public CloudinaryUploadResult subirLogoTienda(
         MultipartFile file,
         Long storeId
 ) throws IOException {

     if (file == null || file.isEmpty()) {
         throw new IllegalArgumentException("Archivo vacío");
     }

     String nombreOriginal = file.getOriginalFilename();

     if (nombreOriginal == null) {
         throw new IllegalArgumentException("Archivo inválido");
     }

     String nombre = nombreOriginal.toLowerCase();

     boolean extensionValida =
             nombre.endsWith(".jpg") ||
             nombre.endsWith(".jpeg") ||
             nombre.endsWith(".png") ||
             nombre.endsWith(".webp");

     if (!extensionValida) {
         throw new IllegalArgumentException(
                 "Formato no permitido. Solo JPG, JPEG, PNG y WEBP."
         );
     }

     File tempFile = File.createTempFile("store-logo-", ".tmp");
     file.transferTo(tempFile);

     try {
         Map<?, ?> result = cloudinary.uploader().upload(
                 tempFile,
                 ObjectUtils.asMap(
                         "folder", "webempresarial/stores/" + storeId + "/branding",
                         "resource_type", "image",
                         "quality", "auto:good",
                         "format", "webp",
                         "width", 600,
                         "height", 300,
                         "crop", "limit",
                         "overwrite", true
                 )
         );

         return new CloudinaryUploadResult(
                 result.get("secure_url").toString(),
                 result.get("public_id").toString()
         );

     } finally {
         if (tempFile.exists()) {
             tempFile.delete();
         }
     }
 }
    // =========================================
    // ELIMINAR IMAGEN
    // =========================================
    public boolean eliminarImagen(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "type", "upload",
                            "invalidate", true
                    )
            );

            return "ok".equals(result.get("result"));

        } catch (IOException e) {
            return false;
        }
    }

    // =========================================
    // EXTRAER PUBLIC ID
    // =========================================
    public String extraerPublicIdDesdeUrl(String url) {
        if (url == null || url.isEmpty()) return null;

        int startIndex = url.indexOf("/upload/");
        if (startIndex == -1) return null;

        String path = url.substring(startIndex + 8);

        return path.replaceFirst("\\.[a-zA-Z0-9]+$", "");
    }
}