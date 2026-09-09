# Feature provenance and scope

This map distinguishes inherited capabilities from additions/changes in Yii2 Insight.
The upstream comparison is `vxdy/Yii2-Support-Extended` at
`cd9cb4485e457f12a13faf2f0632ca861d8b2685`; it is not a claim that every historical
feature worked correctly on every IDE. Current behavior is limited by static resolution.

| Area | Upstream foundation | Yii2 Insight changes | Main source area |
| --- | --- | --- | --- |
| Views | Names/parameters, references, template creation, inspections, move/rename support | API migration, supporting alias/context fixes; expanded controller-to-view dataflow remains planned | `src/io/github/byscrimm/yii2insight/views` |
| Configurations/DI | Property completion in config arrays, constructors, widgets and createObject | Shared argument/array/local-value helpers and component/context fixes; general merge/require/DI analysis remains planned | `configurations`, `objectfactory`, `common` |
| Model/query types | Basic createObject and ActiveQuery one/all providers | Shared model resolver, local query values, named arguments, inherited/self/static relation targets and final asArray(false) handling | `relations/YiiModelResolver.java`, `typeprovider` |
| Relation strings | Database-oriented relation helpers | Completion and per-segment navigation in with/joinWith/innerJoinWith/getRelation/via, aliases/callback keys, model-aware Find Usages | `relations` |
| Relation refactoring | Native PHP refactoring is supplied by PhpStorm | Getter/local PHPDoc pairing, usage adapters, conflict checks and Preview; block starting Rename inside a relation string | `relations/RelationRenameHandler.java`, `RelationRenameProcessor.java`, `RelationSymbols.java` |
| Relation PHPDoc | Schema-based property assistance | Missing-relation class/getter inspection and one/all quick fixes, nullable/collection targets, property-read without setter | `relations/RelationPhpDocInspection.java`, `RelationPhpDocQuickFix.java` |
| GridView/DetailView | Configuration and attribute helpers | Shared widget contexts, model/provider resolution, nested relations, shorthand cursor handling, row/filter separation and first value-callback type inference | `widgetsconfig` |
| Forms, rules and labels | Model attribute and validation completion | Retained with shared compatibility/context improvements | `forms`, `validation`, `attributeLabels` |
| Database/SQL | Datasource/table-prefix/query/column support, inspections, property synchronization | Safer SQL placeholder parsing/fixes and SDK API replacement; general query SQL-alias completion remains planned | `database`, `properties`, `common` |
| Routes/i18n | Route/action and translation completion, parameter assistance | Alias/context and catalog handling fixes; controllerMap/REST and arbitrary translation-source configurations remain planned | `url`, `i18n` |
| Migrations | History and apply/undo/redo UI, output, remote CLI and schema sync | Project lifecycle/disposal, process/cancellation and current SDK handling | `migrations` |
| Delivery | Existing plugin/build history | Independent identity, Java 25/262 support, regression and PSI harnesses, Docker export, verification/CI/release tooling | `tools`, `tests`, `psi-tests`, `.github` |

All source paths in the final column are relative to `src/io/github/byscrimm/yii2insight`
unless stated otherwise. A retained capability is not a new invention of this fork.
AI-assisted implementation in this repository does not transfer upstream authorship.
No AI network service, automatic PHP execution or database connection is needed for
relation analysis. Explicit migration actions execute configured commands.

Reference and evidence: [upstream history](UPSTREAM_CHANGELOG.md), [authors](../AUTHORS.md),
[license](../LICENSE.md), [release evidence](verification/release-1.0.0.md),
[future work](../DEVELOPMENT_PLAN.md).
