package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationAudit;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;

public interface StrategicInterpretationTraceabilityRecorder {

    void record(
            StrategicSynthesis sourceSynthesis,
            StrategicSynthesis aiSynthesis,
            StrategicInterpretationAudit audit
    );
}