# Hobby Shop Speaker Notes

## Opening

This project is U197 Hobbies, a full-stack e-commerce application for hobby products like Gundam models, tools, and paints. The main technical themes I want to highlight are the cart experience, especially guest-to-user cart merging, and the AWS deployment pipeline.

---

## 1. Problem & Solution (1 min)

The business problem was building an online shopping experience that feels smooth from browsing to checkout.

One key issue is that many users shop as guests first, then log in later. If the cart disappears during login, that creates friction and can hurt conversion.

The second problem was deployment. I needed a reliable way to push both frontend and backend changes to AWS without doing everything manually.

My solution was a React frontend, a Spring Boot backend, a MariaDB database, and an AWS deployment pipeline. I built the cart to support both guest sessions and authenticated users, then merged the guest cart into the user cart during login or registration.

---

## 2. Architecture Overview (2 min)

The frontend is a React single-page application built with Vite and hosted on S3 as a static website.

The backend is a Spring Boot API running on an EC2 instance. That same instance also runs MariaDB, which was a practical budget-conscious choice for this project.

The frontend talks to the backend over REST APIs. Authentication uses JWT. Guest cart support uses a session ID passed in the request header.

On the frontend, cart state is centralized in a React context provider. That keeps the navbar cart badge, product pages, and cart page synchronized.

On the backend, the code follows a controller-service-repository pattern. That kept the business logic, especially cart merging, easier to reason about.

For deployment, GitHub pushes trigger AWS CodePipeline. That pipeline runs code analysis, builds the frontend and backend, uploads artifacts to S3, runs Terraform, and restarts the backend using AWS Systems Manager.

---

## 3. Live Demo (3 min)

I’ll start by opening the storefront and browsing products.

Next, I’ll add a few items to the cart and show that the navbar cart badge updates immediately.

Then I’ll open the cart page and show quantity updates, item removal, subtotal calculation, and the guest warning banner.

At this point I’ll explain that the user is still a guest, and the cart is being tracked by a session ID rather than by an account.

Then I’ll log in or register.

After authentication, I’ll return to the cart and show that the guest cart was preserved and merged into the user’s account cart.

That is the most important user experience point in the demo, because it shows continuity instead of forcing the user to start over.

If there is time, I can continue into checkout or order history to show the rest of the purchase flow.

---

## 4. Technical Deep Dive (2 min)

<a id="cart-merge"></a>
### Cart Merge Highlight

The most interesting challenge was cart merging.

The frontend stores a session ID in local storage for guest users. The Axios layer sends that session ID in an `X-Session-ID` header only when the user is not authenticated.

> **Code:** `Client/hobby-shop-frontend/src/services/api.js` — request interceptor reads `sessionId` from `localStorage` and attaches the `X-Session-ID` header when no auth token is present.

On the backend, the cart controller checks for a cart session using a cookie first, then the request header, and if neither exists it creates a new UUID.

> **Code:** `Server/src/main/java/com/hobby/shop/controller/CartController.java` — `POST /api/cart/merge` endpoint, `mergeCarts()` method.

During login or registration, the frontend includes the session ID with the auth request. The backend then merges the guest cart into the authenticated cart.

> **Code:** `Client/hobby-shop-frontend/src/services/authService.js` — `login()` and `register()` both pull `sessionId` from `localStorage` and pass it as a query param and header to the auth endpoint.
>
> **Code:** `Server/src/main/java/com/hobby/shop/controller/AuthController.java` — after successful login or registration, calls `cartService.mergeCarts(email, sessionId)`.

The merge logic checks whether the same product already exists in the user cart. If it does, it increases the quantity. If not, it creates a new cart item. Then it deletes the guest cart.

> **Code:** `Server/src/main/java/com/hobby/shop/service/impl/CartServiceImpl.java` — `mergeCarts()` method (lines ~398–450): iterates session cart items, increases quantity for duplicates or adds new `CartItem`, then calls `cartRepository.delete(sessionCart)`.
>
> **Code:** `Server/src/main/java/com/hobby/shop/repository/CartRepository.java` — `findBySessionId(String sessionId)` is the key query that locates the guest cart.

The reason this was challenging is that the cart has to survive a change in identity, from anonymous session to authenticated user, without breaking the user experience.

> **Code:** `Client/hobby-shop-frontend/src/contexts/CartProvider.jsx` — `useEffect` on `user` state change triggers `loadCart()`, so the merged cart is immediately reflected in the UI after login.

<a id="aws-pipeline"></a>
### AWS Pipeline Highlight

The second technical challenge was deployment automation. The pipeline had to build both applications, inject the live API URL into the frontend build, apply Terraform to AWS resources, and restart the backend remotely.

> **Code:** `buildspec.yml` — the main CodeBuild spec. The frontend build step dynamically looks up the running EC2 instance's public IP via the AWS CLI and injects it as `VITE_API_BASE_URL` at `npm run build` time, so the compiled React app always points to the live backend even if the IP changes.
>
> **Code:** `main.tf` — Terraform configuration defining the EC2 instance, S3 bucket (frontend hosting + artifact storage), IAM roles, security group, and SSM policy attachment that enables remote restarts without SSH.
>
> **Code:** `scripts/ssm-restart.sh` — after the new JAR is uploaded to S3, this script uses AWS SSM `send-command` to restart the Spring Boot service on the EC2 instance remotely.
>
> **Code:** `buildspec-sonar.yml` — separate CodeBuild spec that runs SonarQube analysis before the main build stage.

---

## 5. AWS Infrastructure (1 min)

The frontend is hosted in an S3 bucket configured as a static website.

The backend runs on an EC2 instance, and MariaDB also runs on that same instance.

Terraform defines the infrastructure, including the EC2 instance, S3 bucket, IAM roles, security group, CloudWatch alarms, SNS alerts, and budget monitoring.

For CI/CD, CodePipeline is connected to GitHub. CodeBuild handles the actual build and deployment steps.

After the new backend artifact is uploaded, AWS Systems Manager is used to restart the Spring Boot service on the EC2 instance.

---

## 6. Lessons Learned (1 min)

The biggest lesson was that cart logic is not just a frontend feature. It is a full workflow that spans browser state, API design, authentication, and database structure.

I also learned that deployment work is real engineering work. Getting code live involved debugging infrastructure drift, artifact paths, IAM permissions, environment variables, and restart automation.

For the cart merge problem, the main breakthrough was treating the guest session as a first-class identity and doing the merge on the backend instead of trying to solve it only in the browser.

For AWS delivery, the main lesson was that repeatable automation is worth the effort because manual deployment steps are fragile.

---

## Closing

The strongest part of this project is continuity.

Users can shop as guests, authenticate later, and keep their cart.

From an engineering perspective, I also built continuity into delivery by automating the AWS deployment pipeline with infrastructure as code.

---

## Quick Delivery Reminders

- Slow down during the cart merge explanation.
- Spend the most time on the live demo and technical deep dive.
- If short on time, compress the architecture section and keep the demo plus lessons learned.
- If the live environment is slow, use the cart page and code structure as backup evidence.