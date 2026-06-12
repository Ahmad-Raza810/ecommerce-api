# Upgrade Plan: ecommerce-api (20260611171011)

- **Generated**: 2026-06-11 17:10:11 UTC
- **HEAD Branch**: N/A (Git not available)
- **HEAD Commit ID**: N/A (Git not available)

## Available Tools

**JDKs**
- JDK 17.0.19: C:\Users\Ahmad Raza\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\17\bin (available for baseline)
- JDK 21.0.3: C:\Program Files\Java\jdk-21\bin (current project JDK, used by baseline step)
- JDK 25.0.3: C:\Users\Ahmad Raza\AppData\Roaming\Code\User\globalStorage\pleiades.java-extension-pack-jdk\java\25\bin (required by upgrade steps)

**Build Tools**
- Maven 3.9.9: C:\apache-maven-3.9.9\bin (modern version, fully compatible with Java 25)

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260611171011
- Run tests before and after the upgrade: true

## Upgrade Goals

- **Java Runtime**: 21 → 25 (Latest LTS)
- **Framework Alignment**: Maintain Spring Boot 3.3.5 (already compatible with Java 25)

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible/Compatibility Status |
| --- | --- | --- | --- |
| Java | 21 | 25 | User requested upgrade to latest LTS |
| Spring Boot | 3.3.5 | 3.3.5 | Full Java 25 support; no upgrade needed |
| Spring Framework | 6.1.x (via SB) | 6.1.x | Full Java 25 support via Spring Boot parent |
| Maven | 3.9.9 | 3.9.0+ | Excellent; modern version with full Java 25 support |
| Jakarta EE | 10.0 (via SB) | 10.0 | Already migrated; no changes needed |
| MapStruct | 1.6.3 | 1.6.3 | Full Java 25 support |
| JJWT | 0.12.6 | 0.12.6 | Full Java 25 support |
| SpringDoc OpenAPI | 2.6.0 | 2.6.0 | Full Java 25 support |
| Resilience4j | 2.2.0 | 2.2.0 | Full Java 25 support |
| mysql-connector-j | 8.x | 8.x | Full Java 25 support |
| Flyway | 9.x (via SB) | 9.x | Full Java 25 support |
| JUnit 5 | 5.x (via SB) | 5.x | Full Java 25 support |

## Derived Upgrades

**No derived upgrades required.** The project is already on Spring Boot 3.3.5, which provides full compatibility with Java 25. All dependencies are modern and compatible with the target Java version.

## Impact Analysis

### Subsection: Dependency Changes

**No pom.xml changes required** — all current versions are compatible with Java 25. The property `<java.version>25</java.version>` in pom.xml is the only required change.

| File | Property | Current | Action | Target | Reason |
| --- | --- | --- | --- | --- | --- |
| pom.xml | java.version | 21 | upgrade | 25 | User requested upgrade to Java 25 LTS |

### Subsection: Source Code Changes

**No breaking changes in source code.** The project already uses:
- Jakarta EE (jakarta.servlet, jakarta.persistence, jakarta.validation) — compatible with Java 25
- Java 21 features (records in UserRequestDto.java, UserResponseDto.java) — fully compatible with Java 25
- Spring Boot 3.x APIs — full Java 25 support

No source code modifications are required. The upgrade is purely a configuration change.

### Subsection: Configuration Changes

No application.properties or application.yml changes required. The application is framework-version stable and requires only the JDK version bump.

### Subsection: CI/CD Changes

No CI/CD files detected in the repository (no Dockerfile, Azure Pipelines, GitHub Actions, etc.). If Docker containers or CI/CD systems are used externally, they should be updated to use JDK 25 image bases and compiler settings.

### Subsection: Risks & Warnings

**Potential Runtime Behavior Changes** (low risk — common knowledge items):
- **Module system enforcement (Java 17+)**: Java 25 enforces strong encapsulation of internal APIs. If code accesses internal `sun.*` or `jdk.internal.*` classes, it will fail at runtime. Mitigation: The provided code does not use internal APIs; however, transitive dependencies may. We'll verify during the Final Validation step.
- **Class loading behavior**: Java 25 may have subtle differences in class loader behavior and timing. Mitigation: Full test suite execution in the Final Validation step will catch any issues.
- **Deprecation removals**: APIs deprecated and removed in Java 25 (none in this codebase detected). Mitigation: Compilation success on Java 25 javac confirms no deprecated-and-removed APIs are used.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Verify and prepare the build environment for Java 25 compilation and testing.
  - **Changes to Make**: Verify JDK 25.0.3 is available and accessible.
  - **Verification**: Command: `javac -version` (using JDK 25.0.3 path), Expected Result: `javac 25.0.3`

- **Step 2: Setup Baseline (Optional)**
  - **Rationale**: Establish baseline test pass rate on Java 21 before upgrade, to ensure we meet or exceed it on Java 25.
  - **Changes to Make**: None; run tests with current JDK 21.
  - **Verification**: Command: `mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=warn clean test -q`, Expected Result: All tests PASS, record pass rate and any failures.

- **Step 3: Upgrade to Java 25**
  - **Rationale**: Update pom.xml property to target Java 25. The project's dependencies are already compatible.
  - **Changes to Make**: Update `<java.version>25</java.version>` in pom.xml.
  - **Verification**: Command: `mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=warn clean compile test-compile -q`, Expected Result: Compilation SUCCESS (both main and test code).

- **Step 4: Final Validation**
  - **Rationale**: Verify all upgrade goals are met: compilation succeeds, full test suite passes, no runtime errors.
  - **Changes to Make**: None; run full test and integration suite.
  - **Verification**: 
    - Command 1: `mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=warn clean verify -q` 
    - Expected Result: **100% test pass rate** (match or exceed baseline from Step 2).
    - If any test fails, fix the issue and re-run until 100% pass rate achieved.

## Summary

**Java 25 Upgrade** for ecommerce-api is a straightforward, low-risk operation because:
1. ✅ Project already uses Spring Boot 3.3.5 (full Java 25 support)
2. ✅ All dependencies are modern and Java 25 compatible
3. ✅ Code already uses Jakarta EE (javax → jakarta migration complete)
4. ✅ No breaking API changes in application code
5. ✅ Only configuration change required: `<java.version>21</java.version>` → `<java.version>25</java.version>`

**Estimated effort**: <5 minutes for execution (all steps).
