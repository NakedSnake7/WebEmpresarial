package com.webempresarial.store.service.crm;

import com.webempresarial.store.entity.LeadBudgetRange;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.LeadBudgetRangeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LeadBudgetRangeService {

    private final LeadBudgetRangeRepository repository;

    public LeadBudgetRangeService(LeadBudgetRangeRepository repository) {
        this.repository = repository;
    }

    public List<LeadBudgetRange> getRanges(Store store) {
        ensureDefaults(store);
        return repository.findByStoreIdAndActiveTrueOrderBySortOrderAsc(store.getId());
    }

    @Transactional
    public void ensureDefaults(Store store) {
        if (!repository.findByStoreIdAndActiveTrueOrderBySortOrderAsc(store.getId()).isEmpty()) {
            return;
        }

        create(store, "por-definir", "Sin definir", 0, 0, 0, 0, 0);
        create(store, "menos-10k", "Menos de $10,000", 0, 10000, 5000, 2, 10);
        create(store, "10k-25k", "$10,000 - $25,000", 10000, 25000, 17500, 8, 20);
        create(store, "25k-50k", "$25,000 - $50,000", 25000, 50000, 37500, 15, 30);
        create(store, "50k-plus", "Más de $50,000", 50000, null, 60000, 25, 40);
    }

    private void create(Store store, String code, String label, Integer min, Integer max,
                        Integer estimated, Integer scoreWeight, Integer sortOrder) {
        LeadBudgetRange range = new LeadBudgetRange();

        range.setStore(store);
        range.setCode(code);
        range.setLabel(label);
        range.setMinAmount(min != null ? BigDecimal.valueOf(min) : null);
        range.setMaxAmount(max != null ? BigDecimal.valueOf(max) : null);
        range.setEstimatedAmount(BigDecimal.valueOf(estimated));
        range.setScoreWeight(scoreWeight);
        range.setSortOrder(sortOrder);
        range.setActive(true);

        repository.save(range);
    }
    @Transactional
    public LeadBudgetRange create(
            Store store,
            String code,
            String label,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal estimatedAmount,
            Integer scoreWeight,
            Integer sortOrder
    ) {
        LeadBudgetRange range = new LeadBudgetRange();

        range.setStore(store);
        range.setCode(code.trim());
        range.setLabel(label.trim());
        range.setMinAmount(minAmount);
        range.setMaxAmount(maxAmount);
        range.setEstimatedAmount(estimatedAmount != null ? estimatedAmount : BigDecimal.ZERO);
        range.setScoreWeight(scoreWeight != null ? scoreWeight : 0);
        range.setSortOrder(sortOrder != null ? sortOrder : 0);
        range.setActive(true);

        return repository.save(range);
    }

    @Transactional
    public void update(
            Long id,
            Store store,
            String label,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal estimatedAmount,
            Integer scoreWeight,
            Integer sortOrder,
            boolean active
    ) {
        LeadBudgetRange range = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rango no encontrado"));

        if (!range.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Rango no pertenece a esta tienda");
        }

        range.setLabel(label.trim());
        range.setMinAmount(minAmount);
        range.setMaxAmount(maxAmount);
        range.setEstimatedAmount(estimatedAmount != null ? estimatedAmount : BigDecimal.ZERO);
        range.setScoreWeight(scoreWeight != null ? scoreWeight : 0);
        range.setSortOrder(sortOrder != null ? sortOrder : 0);
        range.setActive(active);

        repository.save(range);
    }

    @Transactional
    public void delete(Long id, Store store) {
        LeadBudgetRange range = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rango no encontrado"));

        if (!range.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("Rango no pertenece a esta tienda");
        }

        repository.delete(range);
    }
}