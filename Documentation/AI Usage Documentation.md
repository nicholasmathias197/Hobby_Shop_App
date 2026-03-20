# AI Usage Documentation

## Overview

This project used AI as a development assistant to support planning, implementation, debugging, documentation, and deployment. The AI was not used to replace engineering decisions. It was used to accelerate research, explain patterns, suggest implementation steps, and help troubleshoot issues during development.

## AI Tools Used

- GitHub Copilot in VS Code
- Amazon Q for AWS and Terraform guidance
- Model used for the current Copilot-based documentation and assistance in this workspace: GPT-5.4

## How AI Was Used in This Project

AI support was used in the following main areas:

### 1. Frontend Development

AI was used to help with:

- structuring the React frontend into pages, components, hooks, contexts, and services
- designing the cart flow and shared cart state using React Context
- organizing route handling with public, protected, and admin routes
- improving component structure for pages such as products, cart, checkout, profile, and admin screens
- debugging frontend integration issues between React and the backend API

Examples of frontend guidance that AI helped with:

- how to structure a React app using `App.jsx`, layout components, route guards, and page folders
- how to build reusable cart-related components like `CartItemRow`, `ProductGrid`, and `ProductCard`
- how to organize API access using a shared Axios instance and service files
- how to manage authentication state and cart state with custom hooks and context providers

### 2. Backend Security

GitHub Copilot was used to help walk through backend security step by step, including:

- setting up JWT-based authentication in Spring Boot
- configuring Spring Security filters and request authorization rules
- protecting user-only and admin-only endpoints
- hashing passwords using BCrypt
- designing secure login and registration flows
- handling guest cart merge behavior during authentication

Examples of backend security topics AI helped explain:

- how `SecurityConfig` should be structured
- how JWT tokens should be generated and validated
- how to protect routes with role-based authorization
- how to configure `PasswordEncoder` and avoid storing plain-text passwords
- how to carry guest session information during login and registration

### 3. AWS Deployment and Terraform

Amazon Q was used to guide the deployment process step by step, especially for:

- understanding how to provision infrastructure with Terraform
- defining AWS resources such as EC2, S3, IAM roles, CloudWatch alarms, SNS alerts, and budget monitoring
- setting up CI/CD flow using AWS CodePipeline and CodeBuild
- uploading frontend and backend artifacts to S3
- restarting the backend on EC2 using AWS Systems Manager
- troubleshooting issues in the pipeline and Terraform apply process

Examples of AWS and Terraform help from AI:

- how to write and organize `main.tf`, `outputs.tf`, and `variables.tf`
- how to store Terraform state in S3
- how to build a deployment pipeline that compiles React and Spring Boot in the same workflow
- how to inject environment-specific values such as the backend API URL into the frontend build
- how to use SSM to restart the Spring Boot service after deployment

## Example Prompt Categories Used During Development

Below are representative examples of the kinds of prompts used while building the project.

### Frontend Prompt Examples

These were primarily done with GitHub Copilot.

- "Help me structure my React frontend with pages, shared components, contexts, and services."
- "Show me how to build a cart provider using React Context and hooks."
- "Help me protect checkout and profile routes in React Router."
- "Explain how to organize API calls using Axios and reusable service files."

### Backend Security Prompt Examples

These were primarily done with GitHub Copilot.

- "Walk me through setting up JWT authentication in Spring Boot step by step."
- "How do I configure Spring Security for public, authenticated, and admin routes?"
- "Show me how to hash passwords correctly with BCrypt in Java."
- "Help me handle guest cart merging when a user logs in."

### AWS and Terraform Prompt Examples

These were primarily done with Amazon Q.

- "Walk me through deploying a Spring Boot backend and React frontend on AWS."
- "Help me write Terraform for EC2, S3, IAM, CloudWatch, and SNS."
- "How do I create a CodePipeline and CodeBuild workflow for this full-stack app?"
- "Explain how to restart my backend on EC2 using AWS SSM after deployment."

## AI-Assisted Workflow

The AI-assisted workflow for this project generally followed this pattern:

1. Define the feature or problem to solve.
2. Ask AI for explanation, implementation guidance, or debugging support.
3. Review the generated suggestions and adapt them to the project requirements.
4. Implement the code manually or with AI-assisted editing.
5. Test the behavior in the application.
6. Refine the result based on runtime behavior, errors, and project goals.

## Human Responsibility and Verification

All final design and implementation decisions remained human-reviewed.

AI suggestions were:

- reviewed before being accepted
- adapted to match the actual project architecture
- tested in the local and deployed environments
- corrected when they did not fit the business requirements or technical constraints

This is especially important in security and deployment work, where AI can be helpful for explanation and workflow guidance, but the final implementation still requires validation.

## Benefits of Using AI in This Project

Using AI provided the following benefits:

- faster learning for unfamiliar implementation areas
- step-by-step guidance for Spring Security and AWS deployment
- quicker troubleshooting during frontend and backend integration
- help generating documentation, diagrams, and presentation materials
- improved productivity when organizing code and refining architecture

## Limitations and Caution

AI-generated suggestions were useful, but they were not automatically trusted.

The main limitations were:

- some suggestions required adaptation to fit the project’s actual structure
- deployment and infrastructure guidance still needed manual verification
- security-related recommendations had to be checked carefully before use
- code examples sometimes needed refactoring to match the rest of the codebase

## Tool-Specific Summary

### GitHub Copilot

GitHub Copilot was used mainly for:

- frontend guidance and component organization
- React state management ideas
- backend security setup walkthroughs
- JWT and Spring Security implementation help
- documentation and presentation support

### Amazon Q

Amazon Q was used mainly for:

- AWS deployment guidance
- Terraform resource planning and implementation help
- CodePipeline and CodeBuild workflow support
- EC2, S3, IAM, CloudWatch, SNS, and SSM setup guidance
- troubleshooting AWS deployment steps

## Academic and Professional Integrity Statement

AI was used as an assistive engineering tool for learning, planning, implementation support, troubleshooting, and documentation. The project still required human design decisions, testing, debugging, and integration work. All final project outcomes were reviewed and validated in the codebase and deployed environment.

## Short Version for Presentation

If asked during the presentation how AI was used, the short answer is:

"I used GitHub Copilot mainly for frontend development and backend security, especially JWT and role-based access control. I used Amazon Q mainly for AWS deployment and Terraform, where it walked me step by step through infrastructure and pipeline setup. I still reviewed, tested, and adjusted the final implementation myself." 