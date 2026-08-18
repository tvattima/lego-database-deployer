# lego-database-deployer
Lego MySQL Database Deployer via Liquibase

## Current Schema Notes

- `marketplace_listing.unit_price` is nullable. A local non-fixed `DRAFT` may enter Pricing Plane onboarding without a seed price; the Pricing Plane must apply a positive initial price before a BrickLink `LISTING_CREATE` request is sent.
- Existing environments receive this change through `1.0.3-marketplace-listing-unit-price-nullable.yaml`; fresh databases receive the nullable definition from `1.0.0-gen-2-db-initialize.yaml`.

Edit postprocessors in `application.yml`:

```yaml
postprocessors:
    groups:
        migration-1.0-to-2.0:
        - InitialMigrationPostProcessor
        - CategoryMigrator
        - ConditionMigrator
        - TransactionTypeMigrator
        - CarrierMigrator
        - TransactionPlatformMigrator
        - CostTypeMigrator
        - PartyMigrator
```
