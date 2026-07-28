# Deployment Index

This repository includes several deployment configurations. Only the first one below is
directly runnable from this repo without any external setup. The rest are **reference
configurations** for cloud deployment — they document a working approach but require you to
create and configure real infrastructure and secrets before they can run.

**Normal `git push` and pull requests only run CI** (`.github/workflows/ci.yml` — backend
tests, frontend tests, Docker image build). None of them deploy the application. The two
deployment workflows below only run when someone manually triggers them.

## 1. Local development — Docker Compose (primary)

The supported way to run the full stack locally.

```bash
docker compose up --build
```

See the [root README](../README.md) for prerequisites and details.

## 2. EC2 reference deployment (manual)

- Workflow: [`.github/workflows/deploy.yml`](../.github/workflows/deploy.yml) — trigger:
  `workflow_dispatch` only.
- Server setup script: [`deployment/ec2-setup.sh`](ec2-setup.sh)
- Reverse proxy: [`deployment/nginx/stockmanagement.conf`](nginx/stockmanagement.conf)
- Service definition: [`deployment/systemd/stockmanagement-backend.service`](systemd/stockmanagement-backend.service)

Deploys the backend JAR and frontend build to a single EC2 instance over SSH, run under
systemd behind nginx. Requires the `EC2_HOST`, `EC2_USER` and `EC2_SSH_KEY` repository
secrets, and a prepared EC2 instance with the setup script already applied.

## 3. AWS ECS / S3 / CloudFront reference deployment (manual)

- Workflow: [`.github/workflows/deploy-aws.yml`](../.github/workflows/deploy-aws.yml) —
  trigger: `workflow_dispatch` only.
- Task definition: [`.aws/task-definition.json`](../.aws/task-definition.json)
- Guide and script: [`deployment/aws/`](aws/)

Builds and pushes a backend image to Amazon ECR, deploys it to ECS Fargate, and deploys the
frontend build to S3 behind CloudFront. Requires the `AWS_ACCESS_KEY_ID`,
`AWS_SECRET_ACCESS_KEY`, `CLOUDFRONT_DISTRIBUTION_ID`, `REACT_APP_API_URL` and
`REACT_APP_ENCRYPTION_KEY` repository secrets, plus a pre-existing ECR repository, ECS
cluster/service and S3/CloudFront distribution matching the task definition.

## 4. Terraform

[`terraform/`](../terraform) contains infrastructure-as-code (ALB, ECS, RDS, S3/CloudFront)
that matches the ECS reference deployment above. It is **not invoked automatically** by CI or
by either deployment workflow — it must be reviewed, configured (`terraform.tfvars`) and run
manually before use.

## 5. Production Docker Compose

[`docker-compose.prod.yml`](../docker-compose.prod.yml) is a reference production Compose
file (backend image from ECR, external RDS) intended as a template for a container-based
production run. It is not currently invoked by any tracked workflow or script.

---

**None of the cloud configurations above prove that a live AWS environment currently
exists for this project** — they document a deployment approach, not a running one. Before
using any of them, review and configure costs, IAM permissions, domain names, TLS
certificates, and all required secrets and resources for your own AWS account.
