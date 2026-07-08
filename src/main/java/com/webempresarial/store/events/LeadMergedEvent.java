package com.webempresarial.store.events;

import com.webempresarial.store.crm.merge.MergeStrategy;
import com.webempresarial.store.entity.Lead;

public class LeadMergedEvent {

    private final Lead source;
    private final Lead target;
    private final MergeStrategy strategy;

    public LeadMergedEvent(
            Lead source,
            Lead target,
            MergeStrategy strategy
    ) {
        this.source = source;
        this.target = target;
        this.strategy = strategy;
    }

    public Lead getSource() {
        return source;
    }

    public Lead getTarget() {
        return target;
    }

    public MergeStrategy getStrategy() {
        return strategy;
    }
}