package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewStrategicArtifactService {

    private final StrategicArtifactAccessService accessService;
    private final StrategicArtifactRepository repository;

    public ReviewStrategicArtifactService(
            StrategicArtifactAccessService accessService,
            StrategicArtifactRepository repository
    ) {
        this.accessService =
                accessService;

        this.repository =
                repository;
    }

    public StrategicArtifactResult verify(
            Long storeId,
            Long artifactId,
            String verifiedBy
    ) {
        StrategicArtifact artifact =
                accessService.requireArtifact(
                        storeId,
                        artifactId
                );

        artifact.verify(
                verifiedBy
        );

        return StrategicArtifactResult.from(
                repository.save(
                        artifact
                )
        );
    }

    public StrategicArtifactResult reject(
            Long storeId,
            Long artifactId,
            String reason,
            String reviewedBy
    ) {
        StrategicArtifact artifact =
                accessService.requireArtifact(
                        storeId,
                        artifactId
                );

        artifact.reject(
                reason,
                reviewedBy
        );

        return StrategicArtifactResult.from(
                repository.save(
                        artifact
                )
        );
    }

    public StrategicArtifactResult requireReview(
            Long storeId,
            Long artifactId
    ) {
        StrategicArtifact artifact =
                accessService.requireArtifact(
                        storeId,
                        artifactId
                );

        artifact.requireReview();

        return StrategicArtifactResult.from(
                repository.save(
                        artifact
                )
        );
    }
}