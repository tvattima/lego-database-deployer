# lego-database-deployer
Lego MySQL Database Deployer via Liquibase

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
