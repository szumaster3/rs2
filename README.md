<div align="center">

# <a href="https://2009scape.org"><img src="https://imgur.com/CUFKvNo.png" alt="2009Scape"/></a>

**An open-source RuneScape 2009 recreation focused on authenticity.**

A fork of [2009Scape](https://gitlab.com/2009scape/2009scape), licensed under **AGPL-3.0**.

</div>

---

## About

**2009Scape** is an open-source recreation of RuneScape as it existed around 2009.

The project aims to preserve the original gameplay, mechanics, interfaces, content, and overall experience of the 2009 era rather than introducing modern RuneScape or typical RSPS-style modifications.

The goal is to provide an authentic experience while maintaining a clean, maintainable, and actively developed codebase.

### Project Goals

* Faithful recreation of the 2009 RuneScape experience
* Authentic gameplay mechanics and interactions
* Accurate quests, skills, items, NPCs, interfaces, and maps
* Compatibility with the original 2009-era client
* Clean and maintainable server code
* Open-source development and community contributions
* Avoid unnecessary gameplay modifications and custom RSPS mechanics

---

## Features

### Gameplay

* 2009-era RuneScape gameplay
* Skills and skilling activities
* Combat and NPC interactions
* Quests and quest dialogue
* Random events
* Shops and item storage
* Minigames and activities
* Authentic item and NPC interactions
* Original-style interfaces and mechanics

### World

* RuneScape 2009-era maps and regions
* NPCs and scenery
* Dynamic regions where required
* Authentic object and ground-item interactions
* Region-based gameplay systems

### Server

* Java 11 server
* Kotlin-based content and systems
* Maven build system
* Event/listener-based interaction system
* Dialogue system
* Packet/context architecture
* Persistent player data
* Timers and scheduled game tasks

---

## Prerequisites

Before setting up the project, make sure you have the following installed.

### Java 11

The project targets **Java 11**.

Recommended distributions:

* [Eclipse Adoptium Temurin 11](https://adoptium.net/temurin/releases/?version=11)
* [Oracle Java 11](https://www.oracle.com/java/technologies/downloads/#java11)

Verify your installation:

```bash
java -version
```

The output should report Java 11.

### IntelliJ IDEA

[Download IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

IntelliJ IDEA is recommended for development because the project uses Maven and Kotlin.

### Git

Install Git if it is not already available:

[Download Git](https://git-scm.com/downloads)

Verify the installation:

```bash
git --version
```

### Windows Users

Enable **Developer Mode** in Windows before continuing.

This allows symbolic links used by the repository to work without additional permission configuration.

---

## Fork & Clone

### 1. Fork the repository

Create a fork of the repository on GitLab.

### 2. Clone your fork

Using SSH:

```bash
git clone git@gitlab.com:<your-username>/<your-project>.git
```

Or using HTTPS:

```bash
git clone https://gitlab.com/<your-username>/<your-project>.git
```

### 3. Enter the project directory

```bash
cd <your-project-folder>
```

### Windows: Long File Paths

Windows may encounter path-length limitations when cloning the repository.

If `git clone` fails because of long paths, enable Git long-path support:

```bash
git config --global core.longpaths true
```

Then clone the repository again.

---

## Git & SSH Setup

If you plan to contribute to the project using SSH, generate an SSH key if you do not already have one:

```bash
ssh-keygen -t ed25519 -C "example@example.com"
```

Add the generated public key to your GitLab account.

Configure your Git identity:

```bash
git config --global user.name "Your Name"
git config --global user.email "example@example.com"
```

Verify your configuration:

```bash
git config --global --list
```

---

## Import into IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Select **File → Open...**.
3. Select the root directory of the repository.
4. IntelliJ should automatically detect the `pom.xml` file.
5. Allow IntelliJ to import the Maven project.
6. Set the **Project SDK** to **Java 11**.
7. Allow Maven to finish downloading the required dependencies.

If IntelliJ reports an incorrect Java version, check:

**File → Project Structure → Project → SDK**

and select Java 11.

---

## Project Structure

The repository is organized into several major components.

```text
2009scape/
├── Client/          # 2009-era game client
├── Server/          # Game server
├── Data/            # Game/cache/data resources
├── Scripts/         # Build and utility scripts
├── .gitlab-ci.yml   # GitLab CI configuration
├── pom.xml          # Maven project configuration
└── README.md
```

The exact structure may change as the project develops.

---

## Build

From the project root, run:

```bash
mvn clean install
```

This cleans previous build output, compiles the project, runs the configured build steps, and packages the required artifacts.

If Maven reports dependency or Java-version errors, verify that Java 11 is being used:

```bash
java -version
mvn -version
```

---

## Running the Server

The server can be started through Maven:

```bash
mvn exec:java -f pom.xml
```

Alternatively, use the provided project scripts/configuration where applicable.

Before starting the server, make sure the required project data and configuration files are present.

---

## Running from IntelliJ IDEA

For development, the server can also be launched directly from IntelliJ IDEA.

1. Import the Maven project.
2. Wait for Maven dependencies to finish resolving.
3. Locate the server entry point.
4. Create or select the appropriate **Run Configuration**.
5. Make sure Java 11 is selected.
6. Start the server.

When developing server content, running directly from IntelliJ is recommended because it provides easier debugging and breakpoint support.

---

## Configuration

Server configuration is stored in the project's configuration files.

The default world configuration is located under:

```text
worldprops/default.conf
```

Do not commit personal or machine-specific configuration changes unless they are intentionally part of the project.

---

## Client

2009Scape uses a 2009-era RuneScape client.

The project aims to preserve the original client experience rather than introducing unnecessary client modifications such as:

* Custom XP-rate interfaces
* Modern RuneScape mechanics
* Unauthentic gameplay systems
* Unnecessary camera or zoom modifications
* RSPS-style gameplay shortcuts

Client changes should be made only when they are required to reproduce or restore authentic 2009 functionality.

---

## Development

The server is primarily written using:

* **Java 11**
* **Kotlin**
* **Maven**

Kotlin code should follow the existing project architecture and conventions.

When implementing new content, prefer existing systems and abstractions over introducing duplicate functionality.

Examples include:

* Interaction listeners
* Dialogue files
* Map areas
* Queue scripts
* Existing combat systems
* Existing skill systems
* Existing packet contexts
* Existing persistence mechanisms

Before introducing a new framework or abstraction, check whether an existing project system already provides the required functionality.

---

## Contributing

Contributions are welcome.

### Create a branch

```bash
git checkout -b feature/my-feature
```

or:

```bash
git checkout -b fix/my-fix
```

### Make your changes

Keep changes focused and avoid unrelated modifications.

### Commit your changes

Use a clear commit message:

```bash
git add .
git commit -m "Describe your changes"
```

### Push your branch

```bash
git push -u origin feature/my-feature
```

Then open a Merge Request on GitLab.

### Contribution Guidelines

When contributing:

* Preserve 2009-era authenticity.
* Avoid unnecessary custom mechanics.
* Reuse existing systems where possible.
* Keep code readable and maintainable.
* Avoid unrelated changes in the same commit.
* Test changes before submitting a Merge Request.
* Include relevant information when fixing bugs.
* Keep commit messages concise and descriptive.

---

## Testing

Before submitting changes, build the project:

```bash
mvn clean install
```

If you are modifying gameplay systems, test the affected content in-game as well.

For example, when changing:

* Quests → test quest progression and completion.
* Skills → test success rates, XP, tools, and resource depletion.
* Combat → test animations, damage, special attacks, and cooldowns.
* Interfaces → test fixed and fullscreen interfaces where applicable.
* Random events → test spawning, interaction, completion, logout, and area transitions.
* Dialogue → test every relevant dialogue option and progression state.

---

## Troubleshooting

### Java version

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

Both should use Java 11.

### Maven cannot resolve dependencies

Try:

```bash
mvn clean install -U
```

This forces Maven to update dependencies.

### Git clone fails on Windows

Enable long paths:

```bash
git config --global core.longpaths true
```

Then clone the repository again.

### Symbolic links do not work on Windows

Make sure **Windows Developer Mode** is enabled.

You may also need to restart your terminal or IDE after enabling it.

### IntelliJ shows incorrect Java version

Open:

```text
File → Project Structure → Project
```

and set the Project SDK to **Java 11**.

Also check the Maven runner/importer JDK under:

```text
Settings → Build, Execution, Deployment → Maven
```

### Build fails unexpectedly

First try a clean build:

```bash
mvn clean install
```

Then verify:

```bash
java -version
mvn -version
git status
```

If the problem persists, include the complete error output when reporting the issue.

---

## Reporting Issues

When reporting a bug, include:

* A clear description of the problem
* Steps to reproduce it
* Expected behaviour
* Actual behaviour
* Relevant NPC/item/object IDs
* Quest or skill involved, if applicable
* Any relevant console errors or stack traces
* Your operating system
* Java version
* The commit/version you are using

For example:

```text
### Description

Mining an iron rock does not correctly change it into its depleted state.

### Steps to Reproduce

1. Equip a pickaxe.
2. Find an iron rock.
3. Mine the rock.
4. Observe the resulting object.

### Expected Behaviour

The rock should change to its appropriate depleted object.

### Actual Behaviour

The object remains unchanged.

### Environment

Java: 11
OS: Windows 11
Commit: <commit hash>
```

---

## License

All modules in this repository are licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)** unless otherwise specified.

See the [LICENSE](LICENSE) file or the [GNU AGPL-3.0](https://www.gnu.org/licenses/agpl-3.0.html) for the full license text.

---

## Credits

This project is based on the open-source **2009Scape** project.

Original project:

[2009Scape on GitLab](https://gitlab.com/2009scape/2009scape)

The project would not be possible without the work of the original developers, contributors, and the RuneScape preservation community.

---

## Disclaimer

2009Scape is an independent, open-source project and is not affiliated with, endorsed by, or sponsored by Jagex.

RuneScape and related trademarks are property of their respective owners.

This project is intended for preservation, research, and educational purposes.

---

<div align="center">
</div>
