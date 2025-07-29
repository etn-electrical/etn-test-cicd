# Requirements

* We're using git flow as the release process.
* Branches involved, feature/**, release/**, develop, main, hotfix/**.
* We need to create some github workflow files under .github/workflows for the CICD pipeline on main branch, and it should work for all the branches as well.

# Basically, the files will be mainly focus on:
* "handle-push.yaml" will be used for hanlding all the push events happend on all the branches;
* "handle-pre-pr.yaml" will be used for open/reopen/synchronizing a PR.
* "handle-post-pr.yml" will be used for close/merged PR.


# Typical actions we have:
1. push feature/** 
2. push hotfix/**
3. pull request from feature/** to develop
4. pull request from develop to release/**
5. pull request from release/** to main
6. pull request from hotfix/** to develop
7. pull request from hotfix/** to main


# Typical jobs/steps we required:
* Decide environment from branches -- develop = development, release/** = staging, main = production
* Calculate the release/build version number from semantical tag number (v1.0.0) , which will be auto increased.
* Maven buid with JDK17
* Maven unit test, jaccoco, sonarqube. The report is also required for PR review.
* Sonarqube scan will be request to run on a self-hosted runner.


