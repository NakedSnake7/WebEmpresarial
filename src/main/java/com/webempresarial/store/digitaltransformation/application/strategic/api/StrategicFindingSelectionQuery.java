package com.webempresarial.store.digitaltransformation.application.strategic.api;

import java.util.List;

public interface StrategicFindingSelectionQuery {

    List<StrategicFindingOptionResponse> findAvailableFindings(
            Long storeId,
            Long projectId
    );
}