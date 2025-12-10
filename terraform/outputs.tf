# =============================================================================
# Stock Management System - Terraform Outputs
# =============================================================================

# -----------------------------------------------------------------------------
# Summary Output
# -----------------------------------------------------------------------------

output "deployment_info" {
  description = "Deployment information summary"
  value = {
    environment = var.environment
    region      = var.aws_region
    project     = var.project_name
  }
}

# -----------------------------------------------------------------------------
# Application URLs
# -----------------------------------------------------------------------------

output "api_url" {
  description = "API URL"
  value       = "https://${aws_lb.main.dns_name}/api"
}

output "frontend_url" {
  description = "Frontend URL"
  value       = var.enable_cloudfront ? "https://${aws_cloudfront_distribution.frontend[0].domain_name}" : "https://${aws_lb.main.dns_name}"
}

output "swagger_url" {
  description = "Swagger UI URL (disabled in production)"
  value       = var.environment != "prod" ? "https://${aws_lb.main.dns_name}/swagger-ui.html" : "Disabled in production"
}

# -----------------------------------------------------------------------------
# Database Connection
# -----------------------------------------------------------------------------

output "database_connection" {
  description = "Database connection information"
  value = {
    endpoint = aws_db_instance.main.endpoint
    database = aws_db_instance.main.db_name
    username = aws_db_instance.main.username
    port     = aws_db_instance.main.port
  }
  sensitive = true
}

output "database_jdbc_url" {
  description = "JDBC connection URL"
  value       = "jdbc:mysql://${aws_db_instance.main.address}:3306/${var.db_name}?useSSL=true&requireSSL=true"
  sensitive   = true
}

# -----------------------------------------------------------------------------
# CI/CD Configuration
# -----------------------------------------------------------------------------

output "cicd_config" {
  description = "CI/CD configuration values for GitHub Actions"
  value = {
    ecr_repository_url    = aws_ecr_repository.main.repository_url
    ecs_cluster_name      = aws_ecs_cluster.main.name
    ecs_service_name      = aws_ecs_service.main.name
    s3_bucket_name        = aws_s3_bucket.frontend.id
    cloudfront_dist_id    = var.enable_cloudfront ? aws_cloudfront_distribution.frontend[0].id : null
    task_definition_family = aws_ecs_task_definition.main.family
  }
}

# -----------------------------------------------------------------------------
# Secrets ARNs (for ECS Task Definition)
# -----------------------------------------------------------------------------

output "secrets_arns" {
  description = "Secrets Manager ARNs"
  value = {
    jwt_secret  = aws_secretsmanager_secret.jwt_secret.arn
    db_password = aws_secretsmanager_secret.db_password.arn
  }
  sensitive = true
}

# -----------------------------------------------------------------------------
# Network Information
# -----------------------------------------------------------------------------

output "network_info" {
  description = "Network configuration"
  value = {
    vpc_id             = aws_vpc.main.id
    public_subnet_ids  = aws_subnet.public[*].id
    private_subnet_ids = aws_subnet.private[*].id
  }
}

# -----------------------------------------------------------------------------
# Monitoring
# -----------------------------------------------------------------------------

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group name"
  value       = aws_cloudwatch_log_group.ecs.name
}
