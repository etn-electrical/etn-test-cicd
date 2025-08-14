# Version Management Action

A reusable GitHub Action that calculates version numbers, determines release status, and automatically creates Git tags based on advanced Git Flow branching patterns and pull request contexts.

## Description

This action provides sophisticated version calculation by:
- Analyzing Git tags to determine base versions
- Implementing Git Flow-aware branching logic
- Handling pull request contexts with source/target branch analysis
- Supporting release candidates, hotfixes, and development builds
- Automatically creating Git tags for release builds
- Generating semantic versions with build metadata
- Validating version formats and consistency

## Inputs

| Input | Description | Required | Default |
|-------|-------------|----------|---------|
| `java-version` | Java version to use for the build | No | `'17'` |
| `maven-options` | Additional Maven options for version calculation | No | `''` |
| `version-pattern` | Pattern for version extraction (regex) | No | `'^v?([0-9]+\.[0-9]+\.[0-9]+)'` |
| `release-branch-pattern` | Pattern to identify release branches for tagging | No | `'^(main\|master)$'` |

## Outputs

| Output | Description | Type |
|--------|-------------|------|
| `version` | Calculated version for the build | string |
| `is-release` | Whether this is a release build | boolean |
| `maven-version` | Version from Maven POM file | string |
| `git-tag` | Latest Git tag if available | string |
| `build-number` | Build number based on commit count | string |
| `semantic-version` | Semantic version with build metadata | string |
| `tag-created` | Whether a new Git tag was created | boolean |
| `tag-name` | Name of the Git tag that was created | string |

## Advanced Version Calculation Logic

The action uses sophisticated branching logic that handles various Git Flow scenarios:

### Main Branch (Production)
- **Direct Push**: Increments patch version (e.g., `1.2.3` → `1.2.4`)
- **Release PR**: Uses release branch version with minor increment
- **Hotfix PR**: Creates hotfix version with patch increment
- **Other PR**: Creates patch increment version
- **Auto-tagging**: ✅ Creates `v{version}` tags

### Release Branches
- **Named Release** (`release/1.3.0`): Uses exact version from branch name
- **PR to Release**: Adds `-pr.{number}` suffix
- **Release to Main PR**: Adds `-final.pr.{number}` suffix  
- **Direct Push**: Creates release candidate `-rc.{count}`
- **Auto-tagging**: ❌ No automatic tagging

### Develop Branch
- **All builds**: Base version + `-dev.{build-number}`
- **Example**: `1.2.3-dev.42`
- **Auto-tagging**: ❌ No automatic tagging

### Feature/Hotfix/Other Branches
- **Direct Push**: `{base}-{clean-branch}.{commit-hash}`
- **PR Context**: `{base}-{clean-source}.to-{target}.pr.{number}`
- **Auto-tagging**: ❌ No automatic tagging

### Version Suffix Types
- `-pr.{number}`: Regular pull requests
- `-final.pr.{number}`: Release to main pull requests
- `-hotfix.pr.{number}`: Hotfix pull requests
- `-rc.{count}`: Release candidates
- `-dev.{count}`: Development builds
- `-{branch}.{hash}`: Feature branch builds

## Branch-Specific Examples

| Branch/Context | Example Version | Description |
|----------------|-----------------|-------------|
| `main` (push) | `1.2.4` | Patch increment from base `1.2.3` |
| `release/1.3.0` (push) | `1.3.0-rc.15` | Release candidate |
| `release/1.3.0` → `main` (PR) | `1.3.0-final.pr.123` | Final release PR |
| `develop` (push) | `1.2.3-dev.42` | Development build |
| `feature/new-api` (push) | `1.2.3-feature-new-api.abc123` | Feature branch |
| `feature/new-api` → `develop` (PR) | `1.2.3-feature-new-api.to-dev.pr.45` | Feature to develop PR |
| `hotfix/security` → `main` (PR) | `1.2.4-hotfix.pr.67` | Hotfix PR |

## Usage Examples

### Basic Usage with Automatic Tagging

```yaml
- name: Calculate Version and Create Tags
  id: version
  uses: ./.github/actions/version-management

- name: Display Results
  run: |
    echo "Version: ${{ steps.version.outputs.version }}"
    echo "Is Release: ${{ steps.version.outputs.is-release }}"
    echo "Tag Created: ${{ steps.version.outputs.tag-created }}"
    echo "Tag Name: ${{ steps.version.outputs.tag-name }}"
```

### Push Tags to Remote (if created)

```yaml
- name: Calculate Version
  id: version
  uses: ./.github/actions/version-management

- name: Push new tags to remote
  if: steps.version.outputs.tag-created == 'true'
  run: |
    git push origin ${{ steps.version.outputs.tag-name }}
    echo "Pushed tag: ${{ steps.version.outputs.tag-name }}"
```

### Custom Configuration

```yaml
- name: Calculate Version
  id: version
  uses: ./.github/actions/version-management
  with:
    java-version: '21'
    maven-options: '-Dmaven.repo.local=.m2/repository'
    version-pattern: '^v?([0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?)'

- name: Build with Calculated Version
  run: |
    mvn clean package -Drevision=${{ steps.version.outputs.version }}
```

### Conditional Deployment Based on Branch Type

```yaml
jobs:
  version:
    runs-on: ubuntu-latest
    outputs:
      version: ${{ steps.calc.outputs.version }}
      is-release: ${{ steps.calc.outputs.is-release }}
      tag-created: ${{ steps.calc.outputs.tag-created }}
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - name: Calculate Version
        id: calc
        uses: ./.github/actions/version-management

  deploy-release:
    needs: version
    if: needs.version.outputs.is-release == 'true'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy Release
        run: |
          echo "Deploying release version: ${{ needs.version.outputs.version }}"
          
  deploy-staging:
    needs: version
    if: needs.version.outputs.is-release == 'false'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: |
          echo "Deploying development version: ${{ needs.version.outputs.version }}"
```

## Git Tag Management

The action automatically creates Git tags for release builds:

### Tag Creation Rules
- **Creates tags**: Only for builds where `is-release` is `true` (main/master branches)
- **Tag format**: `v{version}` (e.g., `v1.2.3`)
- **Tag type**: Annotated tags with rich metadata
- **Duplicate handling**: Skips creation if tag already exists

### Tag Metadata
Each created tag includes:
- Release version number
- Build number (commit count)
- Source branch name
- Commit SHA
- Semantic version
- GitHub Actions attribution

### Example Tag Message
```
Release version 1.2.3

Created by GitHub Actions
Build: 42
Branch: main
Commit: abc123def456
Semantic Version: 1.2.3
### Version Property Integration
  
### Workflow Integration
- name: Calculate Version
  id: version
  uses: ./.github/actions/version-management

- name: Build with Dynamic Version
  run: |
    mvn clean package -Drevision=${{ steps.version.outputs.version }}
    
- name: Update Maven Version
  if: steps.version.outputs.is-release == 'true'
  run: |
    mvn versions:set -DnewVersion=${{ steps.version.outputs.version }}
The action sets these environment variables for subsequent steps:
- `IS_RELEASE`: Boolean indicating release status  
- `TAG_CREATED`: Whether a tag was created (true/false)
- `TAG_NAME`: The name of the created tag
- **Git History**: Repository must be checked out with full history (`fetch-depth: 0`)
- **Git Tags**: Uses existing version tags in `v*.*.*` format for base version calculation
- **Java Environment**: Automatically set up by the action for Maven operations
- **Maven Project**: Optional - provides additional version information if `pom.xml` exists
## Validation and Error Handling
The action includes comprehensive validation:
- **Version Format**: Warns about non-semantic versioning patterns
- **Release Consistency**: Prevents SNAPSHOT versions in release builds  
- **Tag Conflicts**: Safely handles existing tags without errors
- **Missing Dependencies**: Gracefully handles missing Maven files
## Git Flow Compatibility
This action is designed to work seamlessly with Git Flow branching strategies:

- ✅ **Feature branches** (`feature/*`) → Development versions
- ✅ **Release branches** (`release/*`) → Release candidates  
- ✅ **Hotfix branches** (`hotfix/*`) → Hotfix versions
- ✅ **Develop branch** → Development builds
- ✅ **Main/Master** → Production releases with auto-tagging
- ✅ **Pull Request contexts** → PR-specific versioning

The action sets these environment variables for use in subsequent steps:

- `VERSION`: The calculated version
- `IS_RELEASE`: Boolean indicating release status
- `BUILD_NUMBER`: The build number (commit count)
- `SEMANTIC_VERSION`: The full semantic version

## Validation

The action includes automatic validation:
- Warns if version doesn't follow semantic versioning
- Fails if release builds contain 'SNAPSHOT'
- Validates version format consistency

## Prerequisites

- Repository must be checked out with full Git history (`fetch-depth: 0`)
- Maven project with valid `pom.xml` (for Maven-based version detection)
- Java environment (automatically set up by the action)

## Examples by Branch Type

| Branch | Maven Version | Git Tag | Calculated Version | Is Release |
|--------|---------------|---------|-------------------|------------|
| `main` | `1.2.3-SNAPSHOT` | `v1.2.2` | `1.2.3` | `true` |
| `develop` | `1.2.3-SNAPSHOT` | `v1.2.2` | `1.2.3-SNAPSHOT` | `false` |
| `feature/new` | `1.2.3-SNAPSHOT` | `v1.2.2` | `1.2.3-SNAPSHOT` | `false` |
| `release/1.3.0` | `1.3.0-SNAPSHOT` | `v1.2.2` | `1.3.0` | `true` |
| `main` | - | `v2.0.0` | `2.0.0` | `true` |
| `develop` | - | `v2.0.0` | `2.0.0-dev.42` | `false` |

