# Buyer Portal Implementation

## Overview
This implementation creates a separate authentication system for online buyers without affecting the existing system user functionality.

## What Was Created

### 1. Database Model
- **Table**: `online_buyer_login`
- **Entity**: `OnlineBuyerLogin.java` (src/main/java/com/clothsphere/model/SOM/)
- **Purpose**: Separate table for online buyer authentication (OBY prefix) vs local buyers (LBY prefix)

### 2. Repository Layer
- **File**: `OnlineBuyerLoginRepository.java` (src/main/java/com/clothsphere/repository/SOM/)
- **Methods**:
  - findByUsername
  - findByEmail
  - insertOnlineBuyer (manual query)
  - updateLogCount
  - updatePassword
  - checkUsernameExists
  - checkEmailExists
  - and more...

### 3. Service Layer
- **File**: `OnlineBuyerLoginService.java` (src/main/java/com/clothsphere/service/SOM/)
- **Features**:
  - Buyer validation with BCrypt password encryption
  - Registration with auto-generated OBY IDs
  - Login count tracking
  - Password management
  - Profile updates

### 4. Controller Layer
- **File**: `BuyerController.java` (src/main/java/com/clothsphere/controller/)
- **Endpoints**:
  - `GET /buyer/login` - Show login/register page
  - `POST /buyer/login` - Process buyer login
  - `POST /buyer/register` - Process buyer registration
  - `GET /buyer/dashboard` - Buyer dashboard
  - `GET /buyer/logout` - Logout buyer

### 5. View Templates
- **buyerLogin.html** - Login and registration form with tabs
- **buyerDashboard.html** - Buyer dashboard after login
- Both use Thymeleaf for server-side rendering

### 6. Updated Files
- **LoginController.java**: Updated `/buyerLogin` endpoint to redirect to `/buyer/login`
- **getstart.html**: Updated Buyer Login button to use `/buyerLogin` mapping

## Database Table Structure

```sql
online_buyer_login (
    buyer_id VARCHAR(10) PRIMARY KEY,      -- OBY01, OBY02, etc.
    username VARCHAR(50) UNIQUE,
    password VARCHAR(100),                  -- BCrypt encrypted
    email VARCHAR(100) UNIQUE,
    customer_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    company VARCHAR(100),
    role VARCHAR(20) DEFAULT 'buyer',
    log_count INT DEFAULT 0,
    created_at DATETIME,
    is_active BIT DEFAULT 1
)
```

## How It Works

### Registration Flow
1. User clicks "Buyer Login" on getstart.html
2. Redirected to `/buyer/login` (buyerLogin.html)
3. User switches to "Register" tab
4. Fills registration form
5. Form submits to `POST /buyer/register`
6. BuyerController validates and creates new buyer
7. Auto-generates buyer ID (OBY01, OBY02, etc.)
8. Password encrypted with BCrypt
9. Success message shown, user can login

### Login Flow
1. User enters username and password
2. Form submits to `POST /buyer/login`
3. BuyerController validates credentials
4. Session created with buyer information
5. Log count incremented
6. Redirected to buyer dashboard

### Session Management
- Session attributes:
  - `buyerUser`: OnlineBuyerLogin object
  - `buyerId`: Buyer ID (OBY01, etc.)
  - `buyerUsername`: Username
  - `buyerEmail`: Email

## Separation from System Users

This implementation is completely separate from the system user authentication:

| Feature | System Users | Buyers |
|---------|-------------|---------|
| Table | `system_user_login_details` | `online_buyer_login` |
| Login Endpoint | `/systemUserLogin` | `/buyer/login` |
| Controller | `LoginController` | `BuyerController` |
| Session Key | `currentUser` | `buyerUser` |
| ID Prefix | N/A | OBY (Online Buyer) |
| Roles | HR-Manager, Employee, etc. | buyer |

## Testing

### Manual Database Table Creation
If Hibernate auto-update doesn't create the table, run:
```bash
database/create_online_buyer_login_table.sql
```

### Test Registration
1. Start the application
2. Navigate to `http://localhost:8080/getstart`
3. Click "Buyer Login"
4. Click "Register" tab
5. Fill in the form and submit
6. Login with created credentials

## URLs

- Landing page: `http://localhost:8080/getstart`
- Buyer login: `http://localhost:8080/buyer/login`
- Buyer dashboard: `http://localhost:8080/buyer/dashboard`
- Buyer logout: `http://localhost:8080/buyer/logout`

## Security Features

1. **Password Encryption**: BCrypt hashing for all passwords
2. **Session Management**: Separate sessions for buyers
3. **Account Status**: is_active flag to enable/disable accounts
4. **Unique Constraints**: Username and email must be unique
5. **Login Tracking**: Log count tracks number of successful logins

## Future Enhancements

- Password reset functionality
- Email verification on registration
- Profile editing page
- Shopping cart integration
- Order management
- Product browsing
