# lego-database-deployer
Lego MySQL Database Deployer via Liquibase

## Current Schema Notes

- `bricklink_marketplace_listing.stock_room_id` is nullable. Production BrickLink inventory can be publicly visible only when it is not assigned to a stockroom; sandbox and dev guardrails are enforced by application safety checks and configured stockroom values, not by a database `NOT NULL` constraint.

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
