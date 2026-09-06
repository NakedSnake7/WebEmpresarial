package com.webempresarial.store.digitaltransformation.application.project.api;

import java.util.List;

public interface TransformationProjectSelectionQuery {

    List<TransformationProjectOptionResponse> findAvailableProjects(
            Long storeId
    );
}