# PhpStorm Yii2 Support

English | [Русский](README.ru.md)

**Yii2 support for PhpStorm / IntelliJ IDEA:** completion, navigation, ActiveRecord
relations, views, configuration, routes, translations and migrations.

This repository is a fork of [vxdy/Yii2-Support-Extended](https://github.com/vxdy/Yii2-Support-Extended),
which is based on [nvlad/yii2support](https://github.com/nvlad/yii2support).
**All updates, fixes, enhancements and documentation changes in this fork are
developed with AI assistance under the direction of the repository maintainer.**
See [AI-assisted development](AI_DEVELOPMENT.md) for the development process and attribution.

AI assistance is used to maintain the project. The plugin's Yii2 analysis does not
require an AI service or an AI API key.

## Compatibility and status

| Item | Current target |
| --- | --- |
| Primary IDE | PhpStorm 2026.2, platform `262.*` |
| Local SDK used | PhpStorm 2026.2.2, build `262.10315.130` |
| Runtime / build JDK | Java 25 |
| Plugin version | `1.1.0-rc.3` |
| IntelliJ IDEA | Requires compatible PHP and Database Tools plugins; not separately verified |

The runtime plugin name remains **Yii2 Support Extended**, with ID
`com.yii2supportExtended`. The repository name does not change installed-plugin identity.
This is a release candidate. Official Plugin Verifier passed for 262.10315.130;
full IDE integration testing is still pending;
see [verification](docs/verification/1.1.0-rc.3.md) and [the development plan](DEVELOPMENT_PLAN.md).

## Features

| Area | Available support |
| --- | --- |
| ActiveRecord relations | Completion in `with`, `joinWith`, `innerJoinWith`, `getRelation` and `via`; dotted paths, JOIN aliases and callback keys |
| Relation navigation | References to each relation getter in a path, segment rename handling and a getter usage-search contributor |
| Query types | Model resolution in supported query chains, local variables and explicit `ActiveQuery`; `one()` / `all()` type support and `asArray` mode handling |
| Views | Template name/parameter completion, navigation, missing-view and parameter inspections and quick fixes |
| Configuration | Property completion/references in supported config arrays, `Yii::createObject()`, application components and widget options |
| Forms and models | Model attributes in ActiveForm/HTML helpers, validation `rules()` and `attributeLabels()` completion |
| Database | Schema-assisted table/column completion, ActiveRecord inspections and SQL parameter completion/checks |
| Routes | Route and action-parameter completion, controller/action navigation in supported layouts |
| Translations | Category/message completion from PHP translation catalogs |
| Migrations | History, apply/undo/redo actions, command output and selected datasource synchronization |

Database features need a configured IDE datasource. Configure the Yii root, views,
datasource and migration commands in **Settings → PHP → Yii2 Support**.
Some features are inherited and still need full IDE regression coverage. Dynamic
PHP constructs may remain unresolved rather than receiving guessed results.

### Relations in 1.1

```php
User::find()->with(['orders.items.product']);

User::find()->joinWith([
    'orders AS o' => function ($query) {
        $query->andWhere(['o.status' => 1]);
    },
]);

$query = User::find()->where(['active' => 1]);
$query->with('orders');
```

The resolver follows supported query chains, local assignments, inherited relation
getters and explicit `new ActiveQuery(User::class)`. It recognizes named arguments
in relation contexts and `self::class` / `static::class` targets. Suggestions are
based on public relation getters using `hasOne` or `hasMany`; SQL strings and JOIN
mode arguments are excluded. Renaming one segment preserves the rest of the path
and any JOIN alias.

### GridView and DetailView in 1.1.0

```php
$query = User::find()->where(['active' => 1]);
$provider = new ActiveDataProvider(['query' => $query]);

GridView::widget([
    'dataProvider' => $provider,
    'columns' => [
        'email:email:Email address',
        'profile.city',
        ['value' => fn($model) => $model->email],
    ],
]);
```

Column completion now follows `ActiveDataProvider.query`, including local provider,
query and provider-config variables, named arguments and widget/provider subclasses.
It offers public/PHPDoc properties and readable getters, follows ActiveRecord relations
and distinguishes attribute, formatter and label portions of shorthand strings.
`filterAttribute` uses the filter model; row callbacks use the provider's model.
DetailView uses its `model`, including non-ActiveRecord objects.

Only the first `value` callback parameter receives an inferred model type. Closure
and arrow-function parameters with explicit type declarations or `@param` annotations
are preserved. `asArray(true)` produces array rows; a final `asArray(false)` restores
object rows. Dynamic flags remain unresolved. HTML options, labels, unrelated widgets
and non-DataColumn classes are excluded. Column-option completion supplies names
without inserting a closure or overwriting an existing configured value.

## Installation

1. Build the plugin using one of the methods below, or use a matching ZIP from a
   published release of this fork when available. GitHub **Code → Download ZIP**
   downloads source code, not an installable plugin.
2. Open **Settings → Plugins → ⚙ → Install Plugin from Disk…**.
3. Select `build/distributions/yii2-support-extended-1.1.0-rc.3.zip` and restart the IDE.
   Do not unpack the ZIP.

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

With Python 3, PhpStorm 2026.2 and JDK 25, the offline path uses the installed SDK:

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

## Verification and known limits

The current candidate passed **148 pure-logic checks** and **119 PHP PSI checks** both
locally and in Docker. The offline JAR also passed static JVM reference checks
for **190 plugin classes** against **1272 SDK jars**.
The [dated artifact report](docs/verification/1.1.0-rc.3.md) records the exact ZIP and hash.

- PHP PSI tests use the real PhpStorm parser and reference manipulator with a supplied
  class table. They do not run the full PHP index, completion UI or complete IDE
  Find Usages/Rename workflow.
- `verify-offline.py` checks JVM classes/members and plugin descriptors. It does not
  replace JetBrains Plugin Verifier or runtime testing in an activated IDE profile.
- Docker/Gradle build and both regression suites passed on Linux arm64.
- Official Plugin Verifier passed for PhpStorm 262.10315.130 with deprecated API warnings.
- Full IDE integration remains unverified.
- Arbitrary custom Query scopes, ambiguous assignments and dynamic factories may not resolve.
- Providers returned by arbitrary factories/search methods, post-construction provider/query mutations,
  widget configs held in variables and custom Query defaults/scopes need further analysis.
- Expanded controller-to-view type flow, config merging/DI and configurable i18n sources remain planned work.

The plugin analyzes PHP statically. Migration actions are explicit user actions
that execute the configured Yii command.

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

CI definitions are not evidence of successful GitHub execution. Consult the actual
[Actions runs](https://github.com/byscrimm/phpstorm-yii2-support/actions) and release report.
Marketplace identity migration and asset/license review remain prerequisites for
publishing this fork as a separate Marketplace plugin.
