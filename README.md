# lego-database-deployer
Lego MySQL Database Deployer via Liquibase

## Current Schema Notes

- `marketplace_listing.unit_price` is nullable. A local non-fixed `DRAFT` may enter Pricing Plane onboarding without a seed price; the Pricing Plane must apply a positive initial price before a BrickLink `LISTING_CREATE` request is sent.
- Existing environments receive this change through `1.0.3-marketplace-listing-unit-price-nullable.yaml`; fresh databases receive the nullable definition from `1.0.0-gen-2-db-initialize.yaml`.

## BrickLink SET color repair

Change set `1.0.2-bricklink-set-color-not-applicable` repairs existing BrickLink SET marketplace drafts whose
`color_id` is null. BrickLink requires SET inventory creates to send `color_id=0` (Not Applicable). The change is
idempotent and intentionally leaves all other item types unchanged.

Deploy this database change after the shared `lego-data` color policy and before retrying failed
`LISTING_CREATE` requests. The change set does not reset or requeue sync requests.

The change set is included automatically by `db/changelog/db.changelog-master.yaml` through `includeAll`. Confirm
the repair query is empty before retrying a failed SET request; terminal sync requests require an explicit operator
reset or recreation through the normal workflow.

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
