# Technical Review Summary

> **Historical document — not a description of the current codebase.**
>
> This summary captures the portfolio-readiness review that produced the plan for
> pull requests #13–#18. The items it lists have since been addressed or
> deliberately deferred; it is retained as a record of the plan, not as a current
> status report.
>
> For what the system guarantees today, see [README.md](../README.md).

## Purpose

This document summarizes a portfolio-readiness review of the project and the plan for acting on it.
Exact credentials, personal identifiers and environment-specific values are intentionally omitted.

## Current Architecture

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Security
- MySQL
- React
- Docker Compose
- GitHub Actions

## Review Areas

- Documentation and configuration hygiene
- Build and automated testing
- Transaction consistency
- Authentication and authorization
- API design
- Frontend token handling
- Container and CI configuration

## Configuration Findings

- Hardcoded development credentials were present in documentation and example files.
- Personal student information was present in the public README and Actuator metadata.
- Production configuration contained an environment-specific fallback origin.
- Several documentation claims were not supported by the current implementation.

Exact sensitive values are intentionally omitted from this public report.

## Roadmap

### PR1 — Documentation and configuration hygiene — Completed
- Removed personal identifiers
- Removed printed demo credentials
- Replaced example values with placeholders
- Corrected unsupported documentation claims
- Repaired broken documentation links

### PR2 — Build and testing — Completed
- Established a reproducible JDK 21 build
- Restored and extended automated tests
- Ensured CI exposes failures

### PR3 — Transaction consistency
- Validate stock operations
- Review transaction boundaries
- Address concurrent stock updates

### PR4 — Security hardening
- Remove runtime credential fallbacks
- Restrict operational endpoints
- Review authentication and authorization behavior

### PR5 — API and frontend cleanup
- Standardize API responses
- Review frontend token handling
- Resolve or remove incomplete MVC routes

## Status

PR1 and PR2 are complete: documentation, metadata, guides and example-configuration hygiene are
addressed, and the backend now has an established, CI-verified baseline of 54 passing automated tests.
Runtime security, transaction consistency and API/frontend improvements remain planned as separate,
reviewable pull requests.
