# Setup Environment Action

A reusable GitHub Action that determines the environment and deployment settings based on the branch name.

## Description

This action analyzes the current branch name and automatically determines:
- The target environment (production, staging, development, uat, testing)
- Whether deployment should occur
- Boolean flags for each environment type

## Inputs

| Input | Description | Required | Default |
|-------|-------------|----------|---------|
| `branch_name` | The name of the branch to determine environment from | Yes | `${{ github.ref_name }}` |
| `deploy_on_main` | Whether to deploy when on main/master branch | No | `true` |
| `deploy_on_develop` | Whether to deploy when on develop branch | No | `true` |
| `deploy_on_staging` | Whether to deploy when on staging branch | No | `true` |

## Outputs

| Output | Description | Type |
|--------|-------------|------|
| `environment` | The determined environment name | string |
| `is_production` | Boolean indicating if this is production environment | boolean |
| `is_staging` | Boolean indicating if this is staging environment | boolean |
| `is_uat` | Boolean indicating if this is UAT environment | boolean |
| `is_development` | Boolean indicating if this is development environment | boolean |
| `is_testing` | Boolean indicating if this is testing environment | boolean |
| `deploy` | Boolean indicating if deployment should occur | boolean |
| `branch_name` | The normalized branch name used for calculations | string |

## Branch Mapping Rules

| Branch Pattern | Environment | Auto Deploy |
|----------------|-------------|-------------|
| `main`, `master` | production | Configurable (default: true) |
| `staging` | staging | Configurable (default: true) |
| `develop`, `development` | development | Configurable (default: true) |
| `uat`, `user-acceptance-testing` | uat | Always true |
| `test`, `testing` | testing | Always true |
| `release/*` | staging | Always true |
| `hotfix/*` | staging | Always true |
| `feature/*`, `bugfix/*`, `chore/*` | development | Always false |
| All others | development | Always false |

## Usage Examples

### Basic Usage

```yaml
- name: Setup Environment
  id: env
  uses: ./.github/actions/setup-environment

- name: Use Environment
  run: |
    echo "Environment: ${{ steps.env.outputs.environment }}"
    echo "Should deploy: ${{ steps.env.outputs.deploy }}"
```

### With Custom Branch Name

```yaml
- name: Setup Environment
  id: env
  uses: ./.github/actions/setup-environment
  with:
    branch_name: ${{ github.head_ref || github.ref_name }}

- name: Deploy to Environment
  if: steps.env.outputs.deploy == 'true'
  run: |
    echo "Deploying to ${{ steps.env.outputs.environment }}"
```

### Disable Auto Deploy for Certain Branches

```yaml
- name: Setup Environment
  id: env
  uses: ./.github/actions/setup-environment
  with:
    deploy_on_main: 'false'
    deploy_on_develop: 'false'

- name: Manual Deploy Decision
  if: steps.env.outputs.is_production == 'true'
  run: |
    echo "Production environment detected, manual approval required"
```

### Conditional Jobs Based on Environment

```yaml
jobs:
  setup:
    runs-on: ubuntu-latest
    outputs:
      environment: ${{ steps.env.outputs.environment }}
      deploy: ${{ steps.env.outputs.deploy }}
      is_production: ${{ steps.env.outputs.is_production }}
    steps:
      - uses: actions/checkout@v4
      - name: Setup Environment
        id: env
        uses: ./.github/actions/setup-environment

  deploy-staging:
    needs: setup
    if: needs.setup.outputs.environment == 'staging' && needs.setup.outputs.deploy == 'true'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: echo "Deploying to staging environment"

  deploy-production:
    needs: setup
    if: needs.setup.outputs.is_production == 'true' && needs.setup.outputs.deploy == 'true'
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to Production
        run: echo "Deploying to production environment"
```

## Environment Variables

The action also sets the following environment variables for use in subsequent steps:

- `ENVIRONMENT`: The determined environment name
- `DEPLOY`: Boolean string indicating if deployment should occur
- `BRANCH_NAME`: The normalized branch name

## Notes

- Branch names are automatically normalized (removes `refs/heads/` prefix if present)
- The action uses case-sensitive pattern matching
- Environment variables are set in addition to outputs for convenience
- The action includes proper logging for debugging purposes
