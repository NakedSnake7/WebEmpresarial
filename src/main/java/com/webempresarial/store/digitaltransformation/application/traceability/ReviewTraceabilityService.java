package com.webempresarial.store.digitaltransformation.application.traceability;

import com.webempresarial.store.digitaltransformation.domain.traceability.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewTraceabilityService {

    private final TraceabilityAccessService accessService;
    private final TraceabilityNodeRepository nodeRepository;
    private final TraceabilityLinkRepository linkRepository;
    private final ProvenanceRecordRepository provenanceRepository;

    public ReviewTraceabilityService(
            TraceabilityAccessService accessService,
            TraceabilityNodeRepository nodeRepository,
            TraceabilityLinkRepository linkRepository,
            ProvenanceRecordRepository provenanceRepository
    ) {
        this.accessService = accessService;
        this.nodeRepository = nodeRepository;
        this.linkRepository = linkRepository;
        this.provenanceRepository = provenanceRepository;
    }

    public TraceabilityNodeResult verifyNode(
            Long storeId,
            Long nodeId,
            String verifiedBy
    ) {
        TraceabilityNode node =
                accessService.requireNode(
                        storeId,
                        nodeId
                );

        node.verify(verifiedBy);

        TraceabilityNode saved =
                nodeRepository.save(node);

        provenanceRepository.save(
                ProvenanceRecord.forNode(
                        node.getProject(),
                        saved,
                        ProvenanceAction.VERIFIED,
                        TraceabilityOrigin.MANUAL,
                        verifiedBy,
                        "USER",
                        "ReviewTraceabilityService",
                        "Nodo verificado manualmente"
                )
        );

        return TraceabilityNodeResult.from(saved);
    }

    public TraceabilityLinkResult verifyLink(
            Long storeId,
            Long linkId,
            String verifiedBy
    ) {
        TraceabilityLink link =
                requireLink(storeId, linkId);

        link.verify(verifiedBy);

        TraceabilityLink saved =
                linkRepository.save(link);

        provenanceRepository.save(
                ProvenanceRecord.forLink(
                        link.getProject(),
                        saved,
                        ProvenanceAction.VERIFIED,
                        TraceabilityOrigin.MANUAL,
                        verifiedBy,
                        "USER",
                        "ReviewTraceabilityService",
                        "Relación verificada manualmente"
                )
        );

        return TraceabilityLinkResult.from(saved);
    }

    private TraceabilityLink requireLink(
            Long storeId,
            Long linkId
    ) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (linkId == null || linkId <= 0) {
            throw new IllegalArgumentException(
                    "El linkId debe ser válido"
            );
        }

        return linkRepository
                .findByIdAndProjectStoreId(
                        linkId,
                        storeId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró la relación " +
                                linkId +
                                " para el store " +
                                storeId
                        )
                );
    }
}