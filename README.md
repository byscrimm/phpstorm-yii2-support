# Yii2 Insight

English | [Русский](README.ru.md)

**Yii2 development tools for PhpStorm: completion, navigation, relation refactoring and PHPDoc assistance.**

Yii2 Insight 1.0.0 is maintained by **by_scrimm**. It builds on [Vxdy’s Yii2 Support Extended](https://github.com/vxdy/Yii2-Support-Extended), originally based on [NVlad’s Yii2 Support](https://github.com/nvlad/yii2support). Original authorship and the BSD license are preserved. All changes in this fork are developed with AI assistance; using the plugin requires no AI account, API key or external AI service.

## Compatibility

- **PhpStorm 2026.2.2**, build **262.10315.130**, through the **2026.2.x** branch.
- Java 25, supplied by the supported IDE. Earlier PhpStorm versions are not supported by this build.
- Plugin ID: `io.github.byscrimm.yii2insight`; independent settings: `yii2-insight.xml`.
- IntelliJ IDEA with PHP and Database Tools is not separately validated or advertised as a tested target.

## What it does

| Area | Capabilities |
| --- | --- |
| ActiveRecord relations | Complete relation names in `with`, `joinWith`, `innerJoinWith`, `getRelation`, `via`; follow dotted paths, JOIN aliases, callback keys, named arguments and supported local query variables |
| Navigation and usages | Navigate each relation segment to its getter; find relation-string usages from getter or local PHPDoc property; getter search also shows its PHPDoc property |
| Relation refactoring | Rename from getter or PHPDoc declaration coordinates method/property names and matched usages, preserving path suffixes and JOIN aliases; preview/Undo use IDE refactoring; initiating Rename inside a string is blocked |
| Relation documentation | Highlight missing class PHPDoc properties; add one relation from its getter or all missing local relations from the class; use `@property-read` without a public setter, nullable model types for `hasOne`, arrays for `hasMany` |
| Query model types | Resolve supported ActiveRecord/query chains and local values, explicit `ActiveQuery`, and `one()`/`all()`; respect array mode and a final `asArray(false)` |
| GridView and DetailView | Complete model attributes and nested relations; understand `attribute:format:label`; infer the first `value` callback parameter from the provider’s model, preserving explicit/PHPDoc parameter types; keep filter-model and row-model contexts separate |
| Views | Complete template names/parameters, navigate to templates and render calls, inspect missing templates and required/unused parameters, offer associated quick fixes and view templates |
| Configuration and components | Complete/reference properties in supported configuration arrays, constructor configs, `Yii::createObject`, widget/ActiveField configs and application components |
| Forms and models | Complete model attributes in ActiveForm and active HTML helpers, validation rules and attribute labels |
| Database and SQL | With an IDE datasource: table/column and query-condition completion, table-prefix support, ActiveRecord/table/property inspections, schema-assisted property suggestions and SQL parameter checks/fixes |
| Routes and translations | Route/action-parameter completion and controller/action navigation in supported layouts; category/message completion from PHP translation catalogs and translation parameter assistance |
| Migrations | Browse migration history; explicitly apply/undo/redo configured Yii migration commands; inspect command output and synchronize the selected IDE datasource; supported remote-interpreter integrations depend on IDE plugins/configuration |

Configure the project under **Settings → PHP → Yii2 Insight**, including the Yii root, view options, datasource and migration commands when needed. The plugin does not execute PHP or SQL to infer relations; migration actions explicitly execute your configured command.

## What came from upstream, and what we added

The upstream plugin already provided views, configuration arrays, forms, rules, database helpers, basic query type providers and migration tools. GridView configuration support also existed. These capabilities are inherited, not presented as new work by this fork.

Yii2 Insight adds the shared ActiveRecord relation resolver, nested relation completion/navigation and model-aware usage/refactoring integration; coordinated getter/PHPDoc handling; missing-relation documentation actions; improved GridView/DetailView model, shorthand and callback analysis; modern PhpStorm API integration; safer local-value, alias, SQL-fix and migration handling; and new tests/build/release infrastructure. The independent name/ID/settings prevent accidental overwriting of legacy plugin settings.

See the [feature provenance map](docs/FEATURES.md) and [changelog](CHANGELOG.md). [Vxdy](https://github.com/vxdy) and [NVlad](https://github.com/nvlad) retain credit for their work; maintenance of this fork is AI-assisted, not a claim of authorship of inherited code.

## Examples

```php
City::find()->with('region.cities');
City::find()->joinWith(['region r']);

GridView::widget([
    'dataProvider' => new ActiveDataProvider(['query' => City::find()]),
    'columns' => ['name:text:City', 'region.name',
        ['value' => fn($model) => $model->name]],
]);
```

Navigate `region` to `City::getRegion()`. Start Rename on `getRegion()` or its PHPDoc property to update matched references; starting Rename inside the relation string is deliberately disabled. Missing relation documentation can be added with **Alt+Enter** without replacing existing comments or unrelated properties.

## Installation

1. Download the installable `yii2-insight-1.0.0.zip` from [GitHub Releases](https://github.com/byscrimm/phpstorm-yii2-support/releases). The repository’s source ZIP is not an installable plugin.
2. Select **Settings → Plugins → ⚙ → Install Plugin from Disk**, choose the ZIP and restart PhpStorm.
3. Configure **Settings → PHP → Yii2 Insight** for your project.

Yii2 Insight is a separate plugin. Disable conflicting Yii2 Support plugins before enabling it. Existing legacy settings, inspection selections and shortcuts are not automatically migrated. A Marketplace listing becomes available only after submission and JetBrains approval.

## Reliability and limits

Main workflows were manually exercised in an isolated PhpStorm 2026.2.2 profile during release-candidate testing, alongside automated logic/PHP PSI regressions. **Bugs may remain.** Report the IDE/plugin versions and a minimal reproducible example through [GitHub Issues](https://github.com/byscrimm/phpstorm-yii2-support/issues).

Analysis is intentionally conservative: arbitrary PHP execution, dynamic configuration, custom Query scopes/factories, provider mutations and some widget configurations may remain unresolved. Renaming coordinates local getter/PHPDoc declarations; synchronization of every subclass’s documentation is not guaranteed. Automatic relation PHPDoc generation skips ambiguous targets, mixed cardinality and array-mode relations.

SQL-alias attribute completion, precise selected `asArray()` result keys, expanded controller-to-view flow and full configuration merging/DI are future work. Database-dependent and migration capabilities require explicit setup. Consult [the plan](DEVELOPMENT_PLAN.md) and [artifact verification](docs/verification/release-1.0.0.md) for scope and technical evidence.

## Build with Docker

Docker with BuildKit and dependency-repository access is required:

```sh
make docker
```

The container supplies Java 25, Gradle and the PhpStorm SDK; a host JDK or IDE is
not required. The first build downloads the image and SDK and can take several
minutes. BuildKit caches Gradle downloads between builds.

Output in `build/distributions`: the installable ZIP, `SHA256SUMS`, and JUnit
HTML/XML reports under `reports/`. Failed tests stop ZIP export. To choose another
output directory, use `make docker OUTPUT_DIR=build/docker-artifacts`.
For detailed dependency/task logs, use `make docker GRADLE_ARGS=--info`.
Plugin Verifier is a separate `make verify` step.

## Build using installed PhpStorm

With Python 3, PhpStorm 2026.2.2 and JDK 25, the offline path uses the installed SDK:

```sh
python3 tools/build-offline.py --test --psi-test
python3 tools/verify-offline.py
```

To select another installation:

```sh
python3 tools/build-offline.py --ide /path/to/PhpStorm --test --psi-test
python3 tools/verify-offline.py --ide /path/to/PhpStorm
```

`PHPSTORM_HOME` can also select the SDK. If the bundled runtime lacks `javac`, use
`--java-home /path/to/jdk-25` for the builder. The PSI runner uses `JAVA_HOME` or the
IDE's bundled JDK. Requested tests must pass before the builder publishes its ZIP.
SHA-256 and `build-info-<version>.json` are written beside the archive.

## Gradle and CI

The configured Gradle Wrapper is 9.1.0 and requires JDK 25:

```sh
./gradlew --no-daemon check verifyPlugin buildPlugin
```

With a local SDK, pass `-PlocalIdePath=/path/to/PhpStorm`. Gradle plugins, test
frameworks and Plugin Verifier can still require downloads. GitHub Actions is
configured to run this check/build path and store ZIP and verification reports.
A prepared workflow is not evidence of a successful CI run.

## Development

[Contributing](CONTRIBUTING.md) · [Agent instructions](AGENTS.md) ·
[Project memory](docs/PROJECT_MEMORY.md) · [Roadmap](DEVELOPMENT_PLAN.md) ·
[Changelog](CHANGELOG.md)

Bug reports should contain the IDE build, plugin/Yii/PHP versions and a minimal
reproducible example. The repository includes issue and pull request templates.

## Credits and license

Maintained by [byscrimm](https://github.com/byscrimm), with AI-assisted development.
Thanks to [Vxdy and contributors](https://github.com/vxdy/Yii2-Support-Extended) and
[Vladislav Nikishin / NVlad and contributors](https://github.com/nvlad/yii2support)
for the work this fork builds upon.

The original BSD license and copyright notices are preserved in [LICENSE.md](LICENSE.md).
Earlier release history is preserved in [the upstream changelog](docs/UPSTREAM_CHANGELOG.md).
This is a community fork, not an official Yii or JetBrains product.

## Maintenance and releases

All fork updates are developed with AI assistance; original authorship is retained
in [AUTHORS.md](AUTHORS.md) and [NOTICE.md](NOTICE.md). Release ZIPs include the BSD license.

- [Repository governance](docs/GOVERNANCE.md): stable master, branches, commits and PRs.
- [Release operations](docs/RELEASING.md): RC/stable tags, draft Releases, checksums and provenance.
- [Testing and isolation](docs/TESTING.md): evidence boundaries and protection of working IDEs.
- [Metrics](docs/METRICS.md): actual test/build measurements; unmeasured values are explicit.
- [Security reporting](SECURITY.md) and [GitHub configuration](docs/GITHUB_SETUP.md).
