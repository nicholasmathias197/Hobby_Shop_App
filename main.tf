# ==================================================
# Terraform Configuration for Gundam Hobby Shop
# ==================================================
# This Terraform configuration deploys the complete infrastructure for the
# Gundam Hobby Shop application, including:
# - EC2 instance running Spring Boot backend with MySQL
# - S3 bucket hosting React frontend as a static website
# - Security groups, IAM roles, and monitoring
# ==================================================

terraform {
  # Terraform version constraint - ensures compatibility
  required_version = ">= 1.0"
  
  # Required providers for this infrastructure
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"  # Uses AWS provider version 5.x
    }
  }
  
  # Backend configuration for storing Terraform state in S3
  # This enables team collaboration and state locking
  backend "s3" {
    bucket = "gundam-hobby-shop-frontend-911784620581"  # S3 bucket for state storage
    key    = "tfstate"                                   # Path to state file in bucket
    region = "us-east-2"                                 # AWS region for state bucket
  }
}

# ==================================================
# AWS Provider Configuration
# ==================================================
provider "aws" {
  region = "us-east-2"  # All resources will be deployed in Ohio region
}

# ==================================================
# Data Sources
# ==================================================
# Get current AWS account ID for dynamic resource naming
data "aws_caller_identity" "current" {}

# ==================================================
# Security Group Configuration
# ==================================================
# Security group for the Spring Boot EC2 instance
# Controls inbound and outbound traffic
resource "aws_security_group" "spring_boot_sg" {
  name        = "gundam-hobby-shop-sg"
  description = "Allow HTTP, app, and SSH traffic"

  # Inbound Rules:
  # SSH access for administration
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Allow from anywhere (consider restricting in production)
  }

  # Application port for Spring Boot
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # Allow from anywhere
  }

  # Outbound Rules:
  # Allow all outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "gundam-hobby-shop-sg"
    Environment = "production"
    Project     = "capstone"
  }
}

# ==================================================
# IAM Roles and Policies
# ==================================================
# IAM role for EC2 instance to access AWS services
resource "aws_iam_role" "ec2_role" {
  name = "gundam-hobby-shop-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

# Attach S3 read-only access policy to EC2 role
# Allows instance to download application files from S3
resource "aws_iam_role_policy_attachment" "ec2_s3" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
}

# Attach SSM policy for Systems Manager access
# Enables secure shell access without SSH keys
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Instance profile to attach IAM role to EC2
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "gundam-hobby-shop-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# ==================================================
# EC2 Instance for Spring Boot Backend
# ==================================================
# Main application server running Spring Boot and MySQL
resource "aws_instance" "spring_boot_app" {
  # Amazon Linux 2023 AMI (us-east-2)
  ami                    = "ami-0b0b78dcacbab728f"
  instance_type          = "t3.micro"  # Free tier eligible
  key_name               = "hobby-shop-key"  # SSH key pair name
  vpc_security_group_ids = [aws_security_group.spring_boot_sg.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  # Replace instance if user_data changes
  user_data_replace_on_change = true

  # User data script runs on instance launch
  # Sets up Java, MySQL, and deploys the application
  user_data = <<-EOF
#!/bin/bash
exec > /var/log/user-data.log 2>&1

# Install Java 21 (Corretto distribution)
dnf install -y java-21-amazon-corretto-headless

# Install MySQL server
dnf install -y mariadb105-server

# Install AWS Systems Manager agent for remote management
dnf install -y amazon-ssm-agent

# Enable and start SSM agent
systemctl enable amazon-ssm-agent
systemctl start amazon-ssm-agent

# Configure and start MySQL
systemctl enable mariadb
systemctl start mariadb
sleep 5

# Set MySQL root password and create application database
mysql <<'SQLEOF'
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('Postgres1');
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS hobby_shop_db;
SQLEOF

# Create application directory
mkdir -p /opt/hobby-shop

# Download database schema from S3
aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobbyshop.sql /opt/hobby-shop/hobbyshop.sql

# Import database schema
mysql -u root -pPostgres1 hobby_shop_db < /opt/hobby-shop/hobbyshop.sql

# Download Spring Boot application JAR from S3
aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobby-shop-backend.jar /opt/hobby-shop/hobby-shop-backend.jar

# Create systemd service for automatic startup and management
cat > /etc/systemd/system/hobby-shop.service <<SERVICE
[Unit]
Description=Hobby Shop Spring Boot App
After=network.target mariadb.service

[Service]
ExecStart=/usr/bin/java -jar /opt/hobby-shop/hobby-shop-backend.jar
Restart=always
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
SERVICE

# Enable and start the application service
systemctl daemon-reload
systemctl enable hobby-shop
systemctl start hobby-shop
  EOF

  tags = {
    Name        = "gundam-hobby-shop-backend"
    Environment = "production"
    Project     = "capstone"
  }
}

# ==================================================
# SNS Topics for Monitoring Alerts
# ==================================================
# SNS topic for CloudWatch alarm notifications
resource "aws_sns_topic" "alerts" {
  name = "gundam-hobby-shop-alerts"
}

# Email subscription for SNS alerts
resource "aws_sns_topic_subscription" "email" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = "nicholas.mathias@peopleshores.com"  # Alert recipient
}

# ==================================================
# CloudWatch Monitoring Alarms
# ==================================================
# Alarm for high CPU utilization
resource "aws_cloudwatch_metric_alarm" "ec2_cpu_high" {
  alarm_name          = "gundam-hobby-shop-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2  # 2 consecutive periods
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 300  # 5-minute periods
  statistic           = "Average"
  threshold           = 80  # Alert when CPU > 80%
  alarm_description   = "EC2 CPU utilization exceeded 80% for 10 minutes"
  alarm_actions       = [aws_sns_topic.alerts.arn]
  ok_actions          = [aws_sns_topic.alerts.arn]

  dimensions = {
    InstanceId = aws_instance.spring_boot_app.id
  }

  tags = {
    Name        = "gundam-hobby-shop-cpu-alarm"
    Environment = "production"
    Project     = "capstone"
  }
}

# Alarm for EC2 instance status check failures
resource "aws_cloudwatch_metric_alarm" "ec2_status_check" {
  alarm_name          = "gundam-hobby-shop-status-check"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = 60  # 1-minute checks
  statistic           = "Maximum"
  threshold           = 0  # Alert on any failure
  alarm_description   = "EC2 instance failed status check"
  alarm_actions       = [aws_sns_topic.alerts.arn]
  ok_actions          = [aws_sns_topic.alerts.arn]

  dimensions = {
    InstanceId = aws_instance.spring_boot_app.id
  }

  tags = {
    Name        = "gundam-hobby-shop-status-alarm"
    Environment = "production"
    Project     = "capstone"
  }
}

# ==================================================
# AWS Budget Alert
# ==================================================
# Monthly budget to control costs
resource "aws_budgets_budget" "monthly" {
  name         = "gundam-hobby-shop-monthly-budget"
  budget_type  = "COST"
  limit_amount = "20"  # $20 monthly limit
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  # Alert at 80% of budget
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = ["nicholas.mathias@peopleshores.com"]
  }

  # Alert for forecasted overage
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = ["nicholas.mathias@peopleshores.com"]
  }
}

# ==================================================
# S3 Bucket for React Frontend Hosting
# ==================================================
# S3 bucket for static website hosting of React app
resource "aws_s3_bucket" "react_frontend" {
  # Dynamic bucket name includes account ID for uniqueness
  bucket = "gundam-hobby-shop-frontend-${data.aws_caller_identity.current.account_id}"

  tags = {
    Name        = "gundam-hobby-shop-frontend"
    Environment = "production"
    Project     = "capstone"
  }
}

# Configure bucket for static website hosting
resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = aws_s3_bucket.react_frontend.id

  index_document {
    suffix = "index.html"  # Default page
  }

  error_document {
    key = "index.html"  # SPA routing - redirect errors to index
  }
}

# Allow public access for website hosting
resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.react_frontend.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# Bucket policy allowing public read access
resource "aws_s3_bucket_policy" "frontend" {
  bucket     = aws_s3_bucket.react_frontend.id
  depends_on = [aws_s3_bucket_public_access_block.frontend]

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = "*"
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.react_frontend.arn}/*"
    }]
  })
}