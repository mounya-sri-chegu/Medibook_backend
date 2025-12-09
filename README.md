# MedVault Backend

A comprehensive Spring Boot backend for medical management system with role-based authentication (ADMIN, PATIENT, DOCTOR), OTP verification, file uploads, and admin approval workflow.

---

## ⚡ Quick Start

### Prerequisites
- Java 21
- PostgreSQL
- Maven
- Email SMTP credentials

### Setup in 4 Steps

1. **Create Database**
   ```bash
   createdb medvault
   psql -d medvault -f database_schema.sql
   ```

2. **Configure Application**
   - Copy `src/main/resources/application.properties.example` to `application.properties`
   - Update database and email credentials

3. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **First Admin Login**
   - **Email:** `admin@medvault.com`
   - **Password:** `Admin@123`

5. **Import Postman Collection**
   - Import `MedVault_Postman_Collection.json` into Postman
   - Set base_url variable to `http://localhost:8080`

---

## 🎯 Key Features

### ✅ Role-Based Authentication
- **ADMIN** - Manages and approves users
- **PATIENT** - Registers with OTP, uploads ID proof
- **DOCTOR** - Registers with OTP, uploads medical license & certificates

### ✅ OTP Verification System
- 5-digit OTP sent via email
- 10-minute validity
- One-time use with `used` flag
- Persistent storage in database

### ✅ File Upload Support
All profile completion APIs use `multipart/form-data`:
- **Patient:** ID Proof (PDF/JPEG) - Required
- **Doctor:** Medical License & Degree Certificates (PDF/JPEG) - Required  
- **Admin:** Certificate (PDF/JPEG) - Required

### ✅ Admin Approval Workflow
- Patients and Doctors require admin approval before login
- New admins require super admin approval
- Pending users/admins can be viewed and approved via API

### ✅ Secure Password Storage
- BCrypt password encoding
- JWT-based authentication
- 24-hour token validity

---

## 📚 Complete API Reference

### Base URL: `http://localhost:8080`

### 🔐 Authentication APIs

#### 1. Generate OTP (Patient/Doctor)
```http
POST /api/auth/generate-otp
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "role": "PATIENT"  // or "DOCTOR"
}

Response:
{
  "success": true,
  "message": "OTP sent to your email",
  "userId": 2,
  "role": "PATIENT"
}
```

#### 2. Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "userId": 2,
  "role": "PATIENT",  // or "DOCTOR"
  "otp": "12345"
}

Response:
{
  "success": true,
  "message": "OTP verified. Complete your profile.",
  "userId": 2,
  "role": "PATIENT"
}
```

#### 3. Register Admin (Invitation Flow)
```http
POST /api/auth/register-admin
Content-Type: application/json

{
  "fullName": "New Admin Name",
  "email": "newadmin@medvault.com"
}

Response:
{
  "success": true,
  "message": "Admin invitation sent successfully. Check email for login credentials.",
  "userId": 4
}
```

#### 4. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "PATIENT",
  "userId": 2
}
```

---

### 👤 Profile Completion APIs (Multipart/Form-Data)

#### 1. Complete Patient Profile
```http
PUT /api/profile/patient
Content-Type: multipart/form-data

Form Fields:
- userId: 2
- password: Patient@123
- dateOfBirth: 1990-01-01
- gender: Male
- bloodGroup: O+
- phone: 9876543210
- address: 123 Main St
- city: New York
- state: NY
- country: USA
- pincode: 10001
- idProof: (file - PDF/JPEG)

Response:
{
  "success": true,
  "message": "Profile saved. Waiting for admin verification."
}
```

#### 2. Complete Doctor Profile
```http
PUT /api/profile/doctor
Content-Type: multipart/form-data

Form Fields:
- userId: 3
- password: Doctor@123
- dateOfBirth: 1980-05-20
- gender: Female
- medicalRegistrationNumber: MED123456
- licensingAuthority: Medical Board
- specialization: Cardiology
- qualification: MBBS, MD
- experience: 10
- phone: 9988776655
- clinicHospitalName: City Hospital
- city: Boston
- state: MA
- country: USA
- pincode: 02115
- medicalLicense: (file - PDF/JPEG)
- degreeCertificates: (file - PDF/JPEG)
- profilePhoto: (file - optional)

Response:
{
  "success": true,
  "message": "Profile saved. Waiting for admin verification."
}
```

#### 3. Complete Admin Profile
```http
PUT /api/admin/profile
Content-Type: multipart/form-data

Form Fields:
- userId: 4
- password: Admin@123
- phone: 1234567890
- designation: Manager
- department: IT
- certificate: (file - PDF/JPEG)

Response:
{
  "success": true,
  "message": "Profile saved. Waiting for admin verification."
}
```

---

### 🔧 Admin Operations (Requires JWT Token)

#### 1. Get Pending Users
```http
GET /api/admin/pending-users
Authorization: Bearer {token}

Response:
[
  {
    "userId": 5,
    "name": "Jane Doe",
    "email": "jane@example.com",
    "role": "PATIENT",
    "status": "PENDING"
  }
]
```

#### 2. Approve User
```http
POST /api/admin/users/{userId}/approve
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "User approved successfully"
}
```

#### 3. Reject User
```http
POST /api/admin/users/{userId}/reject
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "User rejected successfully"
}
```

#### 4. Get Pending Admins
```http
GET /api/admin/role-verification
Authorization: Bearer {token}

Response:
[
  {
    "adminId": 4,
    "fullName": "New Admin",
    "email": "newadmin@medvault.com",
    "verificationStatus": "PENDING",
    "proofUrl": "/uploads/admin-certificates/uuid_certificate.pdf"
  }
]
```

#### 5. Approve Admin
```http
POST /api/admin/role-verification/{adminId}/approve
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Admin approved successfully"
}
```

---

## 🧪 Testing Guide (Step-by-Step)

### Complete testing guide is available in Postman Collection

1. **Import Collection:** `MedVault_Postman_Collection.json`
2. **Set Variables:** 
   - `base_url`: http://localhost:8080
   - `token`: (will be set after login)
3. **Follow the folder structure** in Postman for testing flow

### Quick Test Flow:

#### Patient Registration:
1. Generate OTP → 2. Verify OTP → 3. Complete Profile → 4. Admin Approves → 5. Login

#### Doctor Registration:
1. Generate OTP → 2. Verify OTP → 3. Complete Profile (with files) → 4. Admin Approves → 5. Login

#### Admin Registration:
1. Register Admin → 2. Complete Profile (with certificate) → 3. Super Admin Approves → 4. Login

---

## 🔒 Security Features

- **Password Encryption:** BCrypt with salt
- **JWT Authentication:** 24-hour token validity
- **Role-Based Access Control:** Endpoint protection based on user role
- **File Validation:** Type and size checks for uploads
- **CORS Configuration:** Configured for frontend integration
- **OTP Security:** One-time use, 10-minute expiry

---

## 📁 File Storage

Files are stored in configurable directories:
```
uploads/
├── patient-id-proofs/
├── doctor-licenses/
├── doctor-certificates/
├── doctor-photos/
└── admin-certificates/
```

Configure in `application.properties`:
```properties
medvault.upload.base-dir=uploads
medvault.upload.patient-id-proof-dir=${medvault.upload.base-dir}/patient-id-proofs
medvault.upload.doctor-license-dir=${medvault.upload.base-dir}/doctor-licenses
medvault.upload.doctor-certificate-dir=${medvault.upload.base-dir}/doctor-certificates
medvault.upload.doctor-photo-dir=${medvault.upload.base-dir}/doctor-photos
medvault.upload.admin-certificate-dir=${medvault.upload.base-dir}/admin-certificates
```

---

## 🗄️ Database Schema

### Main Tables:
- **users** - Base user table (id, name, email, password, role, status)
- **patient** - Patient-specific data + id_proof_path
- **doctor** - Doctor-specific data + license/certificate paths
- **admin** - Admin-specific data + certificate path
- **otp** - OTP codes with expiry and used flag
- **appointment** - Future appointments feature

Run `database_schema.sql` to create all tables.

---

## 🏗️ Project Structure

```
src/main/java/com/medibook/medibook_backend/
├── config/
│   ├── CorsConfig.java              # CORS configuration
│   ├── DataInitializer.java         # Super admin initialization
│   ├── FileStorageProperties.java   # File upload config
│   └── SecurityConfig.java          # JWT & Security config
├── controller/
│   ├── AdminController.java         # Admin operations
│   ├── AdminRoleVerificationController.java
│   ├── AuthController.java          # Auth endpoints
│   ├── DoctorController.java        # Doctor profile
│   └── PatientController.java       # Patient profile
├── dto/                             # Request/Response DTOs
├── entity/                          # JPA Entities
├── exception/                       # Global exception handling
├── repository/                      # Spring Data JPA repositories
├── security/
│   ├── JwtAuthFilter.java          # JWT filter
│   └── JwtService.java             # JWT generation/validation
└── service/
    ├── AdminService.java
    ├── AuthService.java            # Core auth logic
    ├── EmailService.java           # OTP email sending
    └── FileStorageService.java     # File upload handling
```

---

## ⚙️ Configuration

### Required Environment Variables (application.properties):

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/medvault
spring.datasource.username=your_username
spring.datasource.password=your_password

# Email (for OTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# JWT
jwt.secret=your_secret_key_min_256_bits
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

## 🐛 Troubleshooting

### Common Issues:

**1. "Email already in use"**
- Solution: Use different email or delete user from database

**2. "OTP not found" / "Invalid OTP"**
- OTP expires in 10 minutes
- Check email for correct 5-digit code
- Ensure userId and role match

**3. "User not approved"**
- Admin must approve user before login
- Check pending users: `GET /api/admin/pending-users`

**4. "Unauthorized" / "Access Denied"**
- Include JWT token in Authorization header: `Bearer {token}`
- Token expires after 24 hours - login again

**5. File upload errors**
- Only PDF, JPEG, PNG accepted
- Max file size: 10MB
- Use `multipart/form-data` content type

---

## 📊 Database Verification

**Check OTPs:**
```sql
SELECT * FROM otp ORDER BY created_at DESC;
```

**Check Users:**
```sql
SELECT id, name, email, role, status FROM users;
```

**Check Pending Users:**
```sql
SELECT u.id, u.name, u.email, u.role, u.status 
FROM users u 
WHERE u.status = 'PENDING';
```

---

## 🚀 Deployment

### Production Checklist:
- [ ] Update `jwt.secret` with strong random key
- [ ] Configure production database
- [ ] Set up email service (SendGrid, AWS SES, etc.)
- [ ] Configure file storage (AWS S3, Azure Blob, etc.)
- [ ] Enable HTTPS
- [ ] Set appropriate CORS origins
- [ ] Configure logging
- [ ] Set up monitoring

---

## 📝 Recent Updates & Fixes

### ✅ Hibernate Issues Resolved
- Implemented `Persistable<Long>` interface in Patient, Doctor, Admin entities
- Fixed "Row was updated or deleted by another transaction" error
- Proper handling of `@MapsId` relationships

### ✅ Admin Registration Enhanced
- Returns `userId` in registration response
- Simplified to single password field (no confirmPassword)
- Clear flow for profile completion

### ✅ File Upload Improvements
- All profile APIs use multipart/form-data
- Inline file validation
- UUID-based filenames to prevent conflicts

---

## 🛠️ Tech Stack

- **Framework:** Spring Boot 3.5.8
- **Language:** Java 21
- **Database:** PostgreSQL
- **Security:** Spring Security + JWT
- **Email:** JavaMailSender
- **Build Tool:** Maven
- **ORM:** Spring Data JPA (Hibernate)

---

## 📞 Support

For issues or questions:
1. Check Postman collection for API examples
2. Review database schema in `database_schema.sql`
3. Check application logs for detailed error messages

---

**Built with ❤️ using Spring Boot 3.5.8**
