terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  backend "s3" {
    bucket = "gundam-hobby-shop-frontend-911784620581"
    key    = "tfstate"
    region = "us-east-2"
  }
}

provider "aws" {
  region = "us-east-2"
}

# Get current account ID
data "aws_caller_identity" "current" {}

# Security Group
resource "aws_security_group" "spring_boot_sg" {
  name        = "gundam-hobby-shop-sg"
  description = "Allow HTTP, app, and SSH traffic"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

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

# IAM role for EC2 to access S3
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

resource "aws_iam_role_policy_attachment" "ec2_s3" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "gundam-hobby-shop-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# EC2 Instance for Spring Boot
resource "aws_instance" "spring_boot_app" {
  ami                    = "ami-0b0b78dcacbab728f"
  instance_type          = "t3.micro"
  key_name               = "hobby-shop-key"
  vpc_security_group_ids = [aws_security_group.spring_boot_sg.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  user_data_replace_on_change = true

  user_data = <<-EOF
#!/bin/bash
exec > /var/log/user-data.log 2>&1

dnf install -y java-21-amazon-corretto-headless
dnf install -y mariadb105-server
dnf install -y amazon-ssm-agent

systemctl enable amazon-ssm-agent
systemctl start amazon-ssm-agent

systemctl enable mariadb
systemctl start mariadb
sleep 5

mysql <<'SQLEOF'
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('Postgres1');
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS hobby_shop_db;
SQLEOF

mkdir -p /opt/hobby-shop
aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobbyshop.sql /opt/hobby-shop/hobbyshop.sql
mysql -u root -pPostgres1 hobby_shop_db < /opt/hobby-shop/hobbyshop.sql

aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobby-shop-backend.jar /opt/hobby-shop/hobby-shop-backend.jar

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

# SNS Topic for CloudWatch Alarms
resource "aws_sns_topic" "alerts" {
  name = "gundam-hobby-shop-alerts"
}

resource "aws_sns_topic_subscription" "email" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = "nicholas.mathias@peopleshores.com"
}

# CloudWatch Alarm — EC2 High CPU
resource "aws_cloudwatch_metric_alarm" "ec2_cpu_high" {
  alarm_name          = "gundam-hobby-shop-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 300
  statistic           = "Average"
  threshold           = 80
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

# CloudWatch Alarm — EC2 Status Check Failed
resource "aws_cloudwatch_metric_alarm" "ec2_status_check" {
  alarm_name          = "gundam-hobby-shop-status-check"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
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

# AWS Budget — $20/month with alerts
resource "aws_budgets_budget" "monthly" {
  name         = "gundam-hobby-shop-monthly-budget"
  budget_type  = "COST"
  limit_amount = "20"
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = ["nicholas.mathias@peopleshores.com"]
  }

  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = ["nicholas.mathias@peopleshores.com"]
  }
}

# S3 Bucket for React Frontend
resource "aws_s3_bucket" "react_frontend" {
  bucket = "gundam-hobby-shop-frontend-${data.aws_caller_identity.current.account_id}"

  tags = {
    Name        = "gundam-hobby-shop-frontend"
    Environment = "production"
    Project     = "capstone"
  }
}

resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = aws_s3_bucket.react_frontend.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "index.html"
  }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.react_frontend.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

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
