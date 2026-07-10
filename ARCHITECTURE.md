# WebEmpresarial Architecture

WebEmpresarial is a multi-tenant SaaS platform composed of independent business modules.

The platform is organized around bounded contexts, not only technical layers.

## Core Principles

1. Tenant isolation is mandatory.
2. Subscription is the source of truth for commercial access.
3. Store.plan is legacy/cache, not the permission authority.
4. Every tenant-owned resource must be accessed through store-scoped queries.
5. Controllers must be thin.
6. Services must represent business use cases.
7. Infrastructure integrations must not leak into domain logic.
8. Features must be resolved through FeatureAccessService.
9. Stripe state must be synchronized through billing services only.
10. New modules must declare their features in the Platform Kernel.

## Store.plan

`Store.plan` is a compatibility cache.

The source of truth for permissions is `Subscription.plan` + `Subscription.status`.

New business logic must never authorize features directly using `Store.plan`.

`Store.plan` exists only for:

- legacy compatibility
- reporting
- search optimization
- fallback when no subscription exists

All feature authorization must go through:

`PlatformAccessService`
`FeatureAccessService`
`PlatformKernel`