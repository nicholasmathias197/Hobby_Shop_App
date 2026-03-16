terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-east-2"  # Ohio region
}

# EC2 Instance for Spring Boot
resource "aws_instance" "spring_boot_app" {
  ami           = "ami-0c55b159cbfafe1f0"  # Amazon Linux 2
  instance_type = "t2.micro"

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

# Get current account ID
data "aws_caller_identity" "current" {}
