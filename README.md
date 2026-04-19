# Order-Inventory Management System

A comprehensive, modularized Spring Boot application designed for professional E-commerce inventory and order orchestration. The system provides a centralized backbone for managing multi-store inventories, complex order lifecycles, and secure administrative access.

## 🏗️ System Architecture

The application follows a **Monolithic Modular** design, ensuring that while the codebase is unified, each business concern is decoupled into its own service layer:

- **Security Module**: Handles stateless authentication using JWT and bcrypt-encrypted administrative credentials.
- **Customer Service**: Manages customer profiles, address books, and identity validation.
- **Product Service**: A robust catalog management system with filtering by brand, category, and physical attributes.
- **Store & Inventory Service**: Orchestrates stock levels across multiple physical locations, supporting atomic stock reduction during checkout.
- **Order Service**: Coordinates the transition of items from inventory to customer ownership, managing various statuses (OPEN, PAID, SHIPPED).
- **Shipment Service**: Tracks the logistics component of orders, providing real-time status updates from creation to delivery.

---

## 🛡️ Security Model

The system utilizes a **High-Security Stateless Configuration**:
- **Authentication**: JWT (JSON Web Tokens) with a 256-bit HS256 signature.
- **Access Control**: Role-based access (ADMIN) enforced via a standard Spring Security Filter Chain.
- **Session Management**: Completely stateless; no server-side sessions are maintained, facilitating horizontal scalability.
- **Token Policy**: Short-lived access tokens (5-minute expiration) to minimize the attack window.

---

## 🛣️ API Catalog (by Service)

### 1. security
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/auth/signup` | Register a new Admin User |
| `POST` | `/api/v1/auth/login` | Login to receive JWT Token |

### 2. customerservice
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/customers` | List all registered customers |
| `GET` | `/api/v1/customers/{id}` | Get specific customer by ID |
| `GET` | `/api/v1/customers/email/{email}` | Find customer by email address |
| `POST` | `/api/v1/customers` | Create a new customer profile |
| `PUT` | `/api/v1/customers/{id}` | Update existing customer details |
| `DELETE` | `/api/v1/customers/{id}` | Remove a customer record |
| `GET` | `/api/v1/customers/validate/{id}` | Check if a customer exists |

### 3. productservice
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/products` | List all products (supports filters: `name`, `brand`, `colour`, `size`) |
| `GET` | `/api/v1/products/{id}` | Get detailed product specifications |
| `POST` | `/api/v1/products` | Add a new product to the catalog |
| `PATCH` | `/api/v1/products/{id}` | Update product attributes |
| `DELETE` | `/api/v1/products/{id}` | Remove a product from the catalog |

### 4. storeservice
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/stores` | List all physical store locations |
| `GET` | `/api/v1/stores/{id}` | Get specific store details |
| `GET` | `/api/v1/stores/search/address` | Search stores by physical address |
| `POST` | `/api/v1/stores` | Create a new store location |
| `PUT` | `/api/v1/stores/{id}` | Update store information |
| `DELETE` | `/api/v1/stores/{id}` | Remove a store |
| `GET` | `/api/v1/inventory/product/{pid}` | Get stock availability across all stores |
| `GET` | `/api/v1/inventory/store/{sid}` | Get all product stock within a specific store |
| `POST` | `/api/v1/inventory` | Initialize or update a stock record |
| `PATCH` | `/api/v1/inventory/add` | Replenish stock quantity |
| `PATCH` | `/api/v1/inventory/reduce` | Subtract stock quantity |
| `DELETE` | `/api/v1/inventory/store/{sid}/product/{pid}` | Remove a product from a store's inventory |

### 5. orderservice
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/orders` | List all system orders |
| `GET` | `/api/v1/orders/{id}` | Get specific order and line items |
| `GET` | `/api/v1/orders/customer/{cid}` | Retrieve order history for a customer |
| `GET` | `/api/v1/orders/store/{sid}` | Retrieve orders processed by a specific store |
| `POST` | `/api/v1/orders` | Place a new order (Atomic Checkout) |
| `PATCH` | `/api/v1/orders/{id}/status` | Update order status |
| `PATCH` | `/api/v1/orders/{id}/shipment` | Link a shipment record to an order |
| `DELETE` | `/api/v1/orders/{id}` | Cancel/Delete an order record |

### 6. shippingservice
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/shipments` | List all active shipments |
| `GET` | `/api/v1/shipments/{id}` | Get shipment tracking and status |
| `GET` | `/api/v1/shipments/customer/{cid}` | Get all shipments for a specific customer |
| `GET` | `/api/v1/shipments/store/{sid}` | Get all shipments dispatched from a specific store |
| `POST` | `/api/v1/shipments` | Manually initialize a shipment |
| `PATCH` | `/api/v1/shipments/{id}/status` | Update delivery tracking status |
| `DELETE` | `/api/v1/shipments/{id}` | Remove a shipment record |

---

## 🧪 Quality Assurance

The application is validated by a comprehensive **32-test JUnit suite**:
- **Unit Tests**: Ensure individual controller logic is sound.
- **Integration Tests**: Verify service-to-service communication.
- **System Flow Tests**: Validate a complete "Signup-to-Shipment" lifecycle under full security constraints.

---

## 👥 Team Contributions

The OIMS platform is the result of a coordinated effort by a multidisciplinary team. Each member owning a core service while contributing to the overall system stability.

### 1. Vishal Gavali
**Role:** Customer Service Lead  
**Module:** Customer Identity & Profile Management
- **Key Contributions:**
  - Implemented stateless customer validation logic.
  - Designed the unified customer profile API schema.
  - Integrated secure identity verification patterns.
- **API Endpoints:**
  - `GET /api/v1/customers` - List all customers
  - `GET /api/v1/customers/{id}` - Get customer details
  - `POST /api/v1/customers` - Create profile
  - `GET /api/v1/customers/validate/{id}` - Verification

### 2. Narayani Gupta
**Role:** Product Service Lead  
**Module:** Catalog Management & Search
- **Key Contributions:**
  - Developed performance-optimized product filtering engine.
  - Created a robust catalog management system with brand/size tracking.
  - Implemented dynamic attribute updates via PATCH.
- **API Endpoints:**
  - `GET /api/v1/products` - Filtered product list
  - `GET /api/v1/products/{id}` - Specification retrieval
  - `POST /api/v1/products` - Catalog expansion
  - `PATCH /api/v1/products/{id}` - Attribute updates

### 3. Samarth Bedare
**Role:** Order Service Lead  
**Module:** Order Orchestration & Lifecycle
- **Key Contributions:**
  - Designed the Atomic Checkout logic for synchronized inventory reduction.
  - Managed complex state machines for order status transitions.
  - Implemented comprehensive order history retrieval services.
- **API Endpoints:**
  - `GET /api/v1/orders` - Master order log
  - `POST /api/v1/orders` - Atomic Checkout
  - `PATCH /api/v1/orders/{id}/status` - Status orchestration
  - `GET /api/v1/orders/customer/{cid}` - History tracking

### 4. Priya Chavan
**Role:** Store & Inventory Service Lead  
**Module:** Global Inventory Management
- **Key Contributions:**
  - Implemented multi-store inventory tracking across physical locations.
  - Developed replenishment logic and atomic stock reduction patterns.
  - Designed the store search and location services.
- **API Endpoints:**
  - `GET /api/v1/stores` - Store locator
  - `GET /api/v1/inventory/product/{pid}` - Global stock check
  - `PATCH /api/v1/inventory/add` - Stock replenishment
  - `PATCH /api/v1/inventory/reduce` - Stock consumption

### 5. Rohan Kumbhar
**Role:** Shipping Service Lead  
**Module:** Logistics & Shipment Tracking
- **Key Contributions:**
  - Implemented real-time shipment tracking and status events.
  - Designed delivery status update logic and logistics coordination.
  - Integrated shipment records with the core order system.
- **API Endpoints:**
  - `GET /api/v1/shipments` - Active shipment tracking
  - `POST /api/v1/shipments` - Shipment initialization
  - `PATCH /api/v1/shipments/{id}/status` - Carrier status updates
  - `GET /api/v1/shipments/customer/{cid}` - Delivery history

---

All other core infrastructure (Security, Global Exception Handling, API Gateway patterns, and Cross-Cutting Concerns) were developed collaboratively by the **Entire Team**.
