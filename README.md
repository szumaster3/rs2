# rs2

Singleplayer-only fork of the [2009scape server](https://gitlab.com/2009scape/2009scape) (RuneScape build 530, January 2009). One local player, one process, no public hosting.

> Not affiliated with Jagex or the upstream 2009scape team. Upstream offers no support for self-hosted servers; neither does this fork beyond its issue tracker.

## Scope

- **In:** local single-player server, content development (Kotlin), local save data.
- **Out:** hosting, login/auth against a database, public world, client distribution.

## Requirements

| Component | Version / note                                        |
|-----------|-------------------------------------------------------|
| JDK       | **11** (Adoptium).                                    |
| Maven     | Not required, the Maven Wrapper (`mvnw`) is included. |
| Git LFS   | Required: run `git lfs pull` once after cloning.      |
| Database  | **None.**                                             |

Windows: enable developer mode before cloning (symlinks).

## Setup

```bash
git clone https://gitlab.com/single-player/rs2.git
cd rs2
git lfs pull # once
```

## Build

```bash
cd Server
../mvnw clean package -DskipTests # Windows: ..\mvnw.cmd
```

Output: `Server/target/Server-1.0.0-jar-with-dependencies.jar`.

## Run

Run from the `Server/` directory, since `worldprops/default.conf` and `data/` are resolved relative to the working directory:

```bash
cd Server
java -jar target/Server-1.0.0-jar-with-dependencies.jar
```

## Connect the client

Client and server must agree on `secret_key` (`Server/worldprops/default.conf`, default `2009scape_development`). With `use_auth = false` any password is accepted.

## Configuration

`Server/worldprops/default.conf`. Keys relevant to singleplayer:

| Key                        | Default                 | Effect                                                                                                 |
|----------------------------|-------------------------|--------------------------------------------------------------------------------------------------------|
| `use_auth`                 | `false`                 | Passwords are not checked against a DB.                                                                |
| `persist_accounts`         | `false`                 | Account data (credits, playtime) is not written to a DB. Save data (stats, inventory) is still stored. |
| `noauth_default_admin`     | `true`                  | Every player gets admin privileges (only when `use_auth = false` and `dev = true`).                    |
| `secret_key`               | `2009scape_development` | Must equal the client's key.                                                                           |
| `preload_map`              | `false`                 | `true` loads the map into memory at startup.                                                           |
| `default_xp_rate`          | `1.0`                   | XP multiplier.                                                                                         |
| `autostock_ge`             | `false`                 | Automatically stocks the Grand Exchange (configured in `data/eco/ge_autostock.json`).                  |
| `i_want_to_cheat`          | `false`                 | Enables GE auto-buy/auto-sell.                                                                         |
| `log_level` / `write_logs` | `verbose` / `true`      | Log verbosity and file logging.                                                                        |
| `data_path`                | `data`                  | Base dir for cache, saves, configs.                                                                    |

All keys are documented inline in `default.conf`.

## Data and saves

Paths are relative to the working directory (`Server/`) and set in the `[paths]` section of `default.conf`.

| Path            | Contents                                                 |
|-----------------|----------------------------------------------------------|
| `data/players/` | Player saves                                             |
| `data/eco/`     | Grand Exchange state                                     |
| `data/botdata/` | Bot data (Deprecated)                                    |
| `data/configs/` | JSON game configs (shops, NPCs, items, drop tables, ...) |
| `data/logs/`    | Logs                                                     |

## Repository layout

```
.
├── Assets/                  # from rs2-filestore
├── Filestore/               # from rs2-filestore
├── Scripts/                 # from rs2-filestore
├── Proto/
├── References/
├── Server/
│   ├── src/main/core/       # engine (config parser, game world, plugin scanning)
│   ├── src/main/content/    # game content: content.region.*, content.global.*, content.minigame.*
│   ├── src/test/            # Kotlin tests
│   ├── worldprops/          # default.conf
│   ├── data/                # cache-derived data, configs
│   └── pom.xml
├── Tools/
├── .gitlab-ci.yml
├── mvnw, mvnw.cmd
└── LICENSE
```

`Assets/`, `Filestore/` and `Scripts/` come from the separate
[rs2-filestore](https://gitlab.com/single-player/rs2-filestore) project and are
not maintained in this repository.

## License

[AGPL-3.0](LICENSE), inherited from upstream. Modified versions made available over a network must publish their source under the same license. Game cache and assets belong to Jagex and are not licensed by this project.