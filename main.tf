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
    key    = "terraform/state"
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

# EC2 Instance for Spring Boot
resource "aws_instance" "spring_boot_app" {
  ami                    = "ami-0c55b159cbfafe1f0"
  instance_type          = "t3.micro"
  vpc_security_group_ids = [aws_security_group.spring_boot_sg.id]

  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Install Java 21
    dnf install -y java-21-amazon-corretto-headless

    # Install MySQL
    dnf install -y mysql-server
    systemctl enable mysqld
    systemctl start mysqld

    # Set up MySQL database and user
    mysql -e "CREATE DATABASE IF NOT EXISTS hobby_shop_db;"
    mysql -e "CREATE USER IF NOT EXISTS 'root'@'localhost' IDENTIFIED BY 'Postgres1';"
    mysql -e "GRANT ALL PRIVILEGES ON hobby_shop_db.* TO 'root'@'localhost';"
    mysql -e "FLUSH PRIVILEGES;"

    # Download JAR from S3
    aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobby-shop-backend.jar /opt/hobby-shop-backend.jar

    # Create systemd service
    cat > /etc/systemd/system/hobby-shop.service <<SERVICE
    [Unit]
    Description=Hobby Shop Spring Boot App
    After=network.target mysqld.service

    [Service]
    ExecStart=/usr/bin/java -jar /opt/hobby-shop-backend.jar
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
