package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import org.springframework.stereotype.Component;

@Component
public class StrategicTraceabilityTypeMapper {

    public TraceabilityNodeType map(
            StrategicArtifactType type
    ) {
        return switch (type) {
            case FINDING ->
                    TraceabilityNodeType.STRATEGIC_FINDING;

            case BUSINESS_PROBLEM ->
                    TraceabilityNodeType.BUSINESS_PROBLEM;

            case BUSINESS_OBJECTIVE ->
                    TraceabilityNodeType.BUSINESS_OBJECTIVE;

            case STRATEGIC_OPPORTUNITY ->
                    TraceabilityNodeType.STRATEGIC_OPPORTUNITY;

            case EXISTING_STRENGTH ->
                    TraceabilityNodeType.EXISTING_STRENGTH;

            case TRANSFORMATION_PRINCIPLE ->
                    TraceabilityNodeType.TRANSFORMATION_PRINCIPLE;
        };
    }
}