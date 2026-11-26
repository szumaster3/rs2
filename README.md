# rs2

A RuneScape server based on [2009scape server](https://gitlab.com/2009scape/2009scape) (RuneScape build 578, 2009-12-21).

> Not affiliated with Jagex or the upstream 2009scape team. Upstream offers no support for self-hosted servers; neither does this fork beyond its issue tracker.

## Requirements

| Component | Version / note                                        |
|-----------|-------------------------------------------------------|
| JDK       | **11** (Adoptium).                                    |
| Maven     | Not required, the Maven Wrapper (`mvnw`) is included. |
| Git LFS   | Required: run `git lfs pull` once after cloning.      |

Windows users should enable Developer Mode before cloning to support symbolic links.

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

The server uses `data/private.pem` for RSA login authentication.

```bash
cd Server
java -jar target/Server-1.0.0-jar-with-dependencies.jar
```

## License

Licensed under [AGPL-3.0](LICENSE), inherited from upstream.

RuneScape, its cache, client and game assets belong to Jagex and are not covered by this license.