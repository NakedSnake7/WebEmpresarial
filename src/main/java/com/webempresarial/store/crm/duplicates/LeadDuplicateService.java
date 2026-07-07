package com.webempresarial.store.crm.duplicates;

import com.webempresarial.store.dto.LeadRequestDTO;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.LeadRepository;

import org.springframework.stereotype.Service;

@Service
public class LeadDuplicateService {

    private final LeadRepository leadRepository;

    public LeadDuplicateService(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public DuplicateCheckResult check(
            LeadRequestDTO dto,
            Store store
    ) {
        Long storeId = store.getId();

        String whatsapp = clean(dto.getWhatsapp());
        String instagram = clean(dto.getInstagram());
        String nombre = clean(dto.getNombre());
        String empresa = clean(dto.getEmpresa());

        if (whatsapp != null) {
            var existing = leadRepository
                    .findFirstByStoreIdAndWhatsapp(storeId, whatsapp);

            if (existing.isPresent()) {
                return DuplicateCheckResult.found(
                        existing.get(),
                        DuplicateReason.WHATSAPP
                );
            }
        }

        if (instagram != null) {
            var existing = leadRepository
                    .findFirstByStoreIdAndInstagramIgnoreCase(
                            storeId,
                            instagram
                    );

            if (existing.isPresent()) {
                return DuplicateCheckResult.found(
                        existing.get(),
                        DuplicateReason.INSTAGRAM
                );
            }
        }

        if (nombre != null && empresa != null) {
            var existing = leadRepository
                    .findFirstByStoreIdAndNombreIgnoreCaseAndEmpresaIgnoreCase(
                            storeId,
                            nombre,
                            empresa
                    );

            if (existing.isPresent()) {
                return DuplicateCheckResult.found(
                        existing.get(),
                        DuplicateReason.NAME_AND_COMPANY
                );
            }
        }

        return DuplicateCheckResult.none();
    }

    private String clean(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}