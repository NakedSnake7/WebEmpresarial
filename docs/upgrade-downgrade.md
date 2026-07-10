Upgrade:
- inmediato
- misma Stripe Subscription
- cambio de SubscriptionItem
- prorrateo y cobro inmediato
- webhook confirma el nuevo plan

Downgrade:
- mantiene el plan actual
- crea o actualiza Subscription Schedule
- pendingPlan guarda el plan futuro
- pendingPlanEffectiveAt guarda la fecha
- customer.subscription.updated materializa el cambio