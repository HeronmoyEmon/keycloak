# Keycloak Learning Guide (seed notes)

> **Status: superseded as the project's README, kept as source material.**
> This was the original single-file README for this repo. The project has since grown into a full
> IAM + Keycloak course — see [`CLAUDE.md`](../CLAUDE.md) for the plan and [`BUILD_STATUS.md`](../BUILD_STATUS.md)
> for progress. This file is not deleted because it's a solid first draft of the Keycloak
> administration material: Part IV ("Keycloak Core Administration") and Part V ("Keycloak Operations")
> chapters should mine it for content rather than starting from a blank page, then fold its definitions
> into the real glossary/quiz data files instead of duplicating them here. Once a section's content has
> been absorbed into a built chapter, note that in `BUILD_STATUS.md` rather than deleting the section —
> leave it as a diff-able source of truth until the whole file has been absorbed.

A comprehensive guide for mastering Keycloak. By reading and memorizing this document, you will have solid knowledge of Keycloak's architecture, concepts, and functionality.

---

## What is Keycloak?

**Keycloak** is an open-source **Identity and Access Management (IAM)** solution developed by Red Hat. It provides centralized authentication and authorization for applications and services.

### In Simple Terms

Imagine you have 10 different applications in your organization. Without Keycloak, each application would need to:
- Implement its own login page
- Store and manage user passwords
- Handle password reset, account lockout, etc.
- Implement "Login with Google" separately

With Keycloak, you have ONE central place that handles all authentication. Users log in once, and they're authenticated across all your applications (**Single Sign-On**).

### Why Keycloak Exists

| Problem | Keycloak Solution |
|---------|-------------------|
| Each app needs its own login system | Centralized authentication for all apps |
| Users have multiple passwords | Single Sign-On (SSO) - one login for all apps |
| Security is hard to implement correctly | Battle-tested security out of the box |
| "Login with Google/Facebook" is complex | Built-in social login support |
| Enterprise needs LDAP/AD integration | Built-in user federation |
| Compliance requires audit trails | Built-in event logging |
| Mobile apps need secure token-based auth | OAuth 2.0 / OpenID Connect support |

### Key Capabilities

- **Single Sign-On (SSO)**: Users log in once and access multiple applications without re-authenticating
- **Identity Brokering**: Authenticate via external providers (Google, Facebook, corporate SAML)
- **User Federation**: Connect to existing user directories (LDAP, Active Directory, custom databases)
- **Standard Protocols**: Supports OpenID Connect, OAuth 2.0, and SAML 2.0
- **Fine-grained Authorization**: Role-based and attribute-based access control
- **Multi-factor Authentication**: OTP, WebAuthn, custom authenticators
- **Self-service**: Users can manage their own accounts, reset passwords, update profiles
- **Admin Console**: Web-based UI for complete management
- **Extensibility**: Plugin architecture (SPIs) for customization

### Pros and Cons

**Pros:**
- Free and open-source (Apache 2.0 license)
- Enterprise-grade security without the enterprise price
- Active community and Red Hat backing
- Highly customizable via themes and SPIs
- Supports all major authentication protocols
- Reduces development time (no need to build auth from scratch)
- Centralized user management
- Comprehensive audit logging
- Scales horizontally for high availability
- Well-documented with extensive guides

**Cons:**
- Learning curve for beginners
- Can be resource-intensive (Java-based, needs decent RAM)
- Complex initial setup for production environments
- Customization requires Java knowledge for SPIs
- UI can feel overwhelming with many options
- Database migrations during upgrades need care
- Some advanced features require deep understanding

### When to Use Keycloak

**Use Keycloak when:**
- You have multiple applications needing authentication
- You need Single Sign-On across applications
- You want to integrate with social logins or enterprise identity providers
- You need compliance-ready authentication (audit logs, MFA)
- You want to avoid building authentication from scratch
- You need centralized user management

**Consider alternatives when:**
- You have a single, simple application (might be overkill)
- You need extremely lightweight solution (Keycloak needs resources)
- You're on serverless/edge computing (better suited for cloud IAM services)

---

## 1. Core Concepts

Understanding these concepts is fundamental. Everything in Keycloak revolves around these building blocks.

### The Big Picture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            KEYCLOAK SERVER                              │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                           REALM                                   │  │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌────────────────────┐    │  │
│  │  │  USERS  │  │  ROLES  │  │ GROUPS  │  │      CLIENTS       │    │  │
│  │  └─────────┘  └─────────┘  └─────────┘  │  (Applications)    │    │  │
│  │                                         └────────────────────┘    │  │
│  │  ┌───────────────────┐  ┌───────────────────┐                     │  │
│  │  │ IDENTITY PROVIDERS│  │  USER FEDERATION  │                     │  │
│  │  │ (Google, SAML)    │  │  (LDAP, Custom)   │                     │  │
│  │  └───────────────────┘  └───────────────────┘                     │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### 1.1 Realm

A **Realm** is the top-level container in Keycloak. Think of it as a **tenant** or **namespace** that completely isolates one set of users, applications, and configurations from another.

**Key Points:**
- Each realm has its own users, roles, groups, and clients
- Realms are completely isolated - a user in Realm A cannot access Realm B
- You typically create one realm per organization or project
- The **master** realm is special - it's used to manage Keycloak itself (create other realms, manage admins)
- Never use the master realm for your applications

**Real-world Example:**
```
Keycloak Server
├── master (for Keycloak administration only)
├── company-internal (for internal employee apps)
├── customer-portal (for customer-facing apps)
└── partner-network (for partner apps)
```

**What a Realm Contains:**
- Users and their credentials
- Roles (permissions)
- Groups (user collections)
- Clients (applications)
- Identity Providers (social login, SAML)
- User Federation (LDAP connections)
- Authentication Flows
- Events/Audit Logs
- Themes (login page customization)

| Concept | Description |
|---------|-------------|
| **What** | A tenant/namespace that manages users, applications, and permissions |
| **Isolation** | Realms are completely isolated from each other |
| **Master Realm** | Default realm for managing Keycloak itself (not for applications) |
| **Best Practice** | Create separate realms for different environments or tenants |

> Deep Dive: [Configuring Realms](https://www.keycloak.org/docs/latest/server_admin/#configuring-realms)

---

### 1.2 Users

A **User** is any entity that can authenticate with Keycloak. Users have identities, credentials, and can be assigned roles and group memberships.

**Key Points:**
- Users belong to exactly one realm
- Each user has a unique username within their realm
- Users can have multiple credentials (password, OTP, certificates)
- Users can be created manually, self-registered, or federated from external sources
- Users can have custom attributes (phone number, employee ID, etc.)

**User Lifecycle:**
```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Created    │ ──► │   Active     │ ──► │   Disabled   │ ──► │   Deleted    │
│ (Manual/Self │     │ (Can login)  │     │ (Cannot      │     │ (Removed)    │
│  Register)   │     │              │     │  login)      │     │              │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
```

**User Attributes:**

| Attribute Type | Examples |
|----------------|----------|
| **Built-in** | username, email, firstName, lastName, enabled |
| **Custom** | phoneNumber, employeeId, department, any key-value pair |

**Credentials:**

| Credential Type | Description |
|-----------------|-------------|
| **Password** | Traditional password, stored hashed (PBKDF2, bcrypt) |
| **OTP** | One-Time Password (TOTP/HOTP) for MFA |
| **WebAuthn** | Hardware security keys, biometrics |
| **X.509 Certificate** | Client certificate authentication |

**Required Actions:**

Required actions are tasks a user must complete before proceeding. They're enforced at login.

| Action | When Used |
|--------|-----------|
| **Update Password** | Force password change on first login or after admin reset |
| **Verify Email** | Confirm email address ownership |
| **Configure OTP** | Set up two-factor authentication |
| **Update Profile** | Complete missing profile information |
| **Terms and Conditions** | Accept legal terms |

**User Sources:**

| Source | Description |
|--------|-------------|
| **Local** | Created directly in Keycloak database |
| **Self-Registered** | User signs up via registration form |
| **Federated** | Imported/linked from LDAP, AD, or custom provider |
| **Identity Brokered** | Created when user logs in via Google, SAML, etc. |

> Deep Dive: [Managing Users](https://www.keycloak.org/docs/latest/server_admin/#assembly-managing-users_server_administration_guide)

---

### 1.3 Roles

**Roles** define what a user can do. They represent permissions or access levels. Roles are the primary mechanism for authorization in Keycloak.

**Key Points:**
- Roles are just labels/tags assigned to users
- Your application reads roles from tokens and makes authorization decisions
- Keycloak doesn't enforce what roles mean - that's your application's job
- Roles can be assigned directly to users or inherited through groups

**Types of Roles:**

| Role Type | Scope | Example |
|-----------|-------|---------|
| **Realm Roles** | Available to all clients in the realm | `admin`, `user`, `manager` |
| **Client Roles** | Specific to one client/application | `myapp:editor`, `myapp:viewer` |
| **Composite Roles** | Contains other roles (inheritance) | `admin` contains `user` + `manager` |

**How Roles Work:**

```
┌─────────────────────────────────────────────────────────────────┐
│                        REALM: mycompany                         │
│                                                                 │
│  Realm Roles:          Client Roles (app1):   Client Roles (app2):
│  ├── admin             ├── editor             ├── read-only    │
│  ├── user              ├── viewer             └── read-write   │
│  └── manager           └── approver                            │
│                                                                 │
│  User "john" has:                                               │
│  - Realm role: user                                             │
│  - Client role: app1:editor                                     │
│  - Client role: app2:read-only                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Composite Roles Example:**

```
admin (composite role)
├── user (inherited)
├── manager (inherited)
└── report-viewer (inherited)

When you assign "admin" to a user, they automatically get all three roles.
```

**Role Mapping:**

| Mapping Level | Description |
|---------------|-------------|
| **Direct** | Role assigned directly to user |
| **Group** | Role assigned to group, user inherits via membership |
| **Composite** | Role inherited from another role |

**Default Roles:**

You can configure roles that are automatically assigned to new users. This is useful for giving all users a base set of permissions.

> Deep Dive: [Assigning Permissions](https://www.keycloak.org/docs/latest/server_admin/#assigning-permissions-using-roles-and-groups)

---

### 1.4 Groups

**Groups** are collections of users. They simplify user management by allowing you to assign attributes and roles to many users at once.

**Key Points:**
- Groups can have a hierarchical structure (parent-child)
- Members inherit the group's attributes and role mappings
- A user can belong to multiple groups
- Groups are realm-specific

**Why Use Groups:**

| Without Groups | With Groups |
|----------------|-------------|
| Assign roles to each user individually | Assign roles to group, all members get them |
| Update 100 users when policy changes | Update one group, all 100 users updated |
| Hard to organize users logically | Clear organizational structure |

**Group Hierarchy:**

```
Organization
├── Engineering
│   ├── Backend Team
│   │   └── (users inherit Engineering + Backend roles)
│   └── Frontend Team
│       └── (users inherit Engineering + Frontend roles)
├── Marketing
│   └── (users inherit Marketing roles)
└── Finance
    └── (users inherit Finance roles)
```

**Group Attributes:**

Groups can have custom attributes that are inherited by members:

```
Group: Premium-Customers
├── Attribute: subscription-tier = "premium"
├── Attribute: support-level = "priority"
└── Role: premium-features

All members automatically get these attributes and roles.
```

| Feature | Description |
|---------|-------------|
| **Inheritance** | Members inherit group's roles and attributes |
| **Hierarchy** | Subgroups inherit from parent groups |
| **Multiple Membership** | Users can belong to multiple groups |
| **Dynamic** | Add/remove users from groups to change their access |

> Deep Dive: [Groups](https://www.keycloak.org/docs/latest/server_admin/#proc-managing-groups_server_administration_guide)

---

### 1.5 Clients

A **Client** is an application or service that wants to use Keycloak for authentication. Every application that needs Keycloak protection must be registered as a client.

**Key Points:**
- Clients are how applications integrate with Keycloak
- Each client has a unique Client ID within the realm
- Clients can have their own roles (client roles)
- Clients define redirect URIs (where users go after login)
- Clients specify what authentication flows they support

**What Clients Represent:**

| Client Type | Example |
|-------------|---------|
| **Web Application** | React SPA, Angular app, traditional web app |
| **Mobile Application** | iOS app, Android app |
| **Backend Service** | REST API, microservice |
| **CLI Tool** | Command-line application |

**Client Types (Access Types):**

| Type | Description | Credential Storage | Example |
|------|-------------|-------------------|---------|
| **Confidential** | Can securely store secrets | Server-side | Backend API, traditional web app |
| **Public** | Cannot store secrets securely | None | SPA, mobile app |
| **Bearer-only** | Only validates tokens, no login | N/A | API that receives tokens |

**Supported Protocols:**

| Protocol | Description | Use Case |
|----------|-------------|----------|
| **OpenID Connect (OIDC)** | Modern, JSON/REST-based | Recommended for new apps |
| **SAML 2.0** | XML-based, enterprise standard | Legacy enterprise apps |

**Client Authentication Methods:**

For confidential clients, how do they prove their identity?

| Method | Description |
|--------|-------------|
| **Client ID and Secret** | Like username/password for the client |
| **Signed JWT** | Client signs a JWT with its private key |
| **X.509 Certificate** | Mutual TLS with client certificate |

**Important Client Settings:**

| Setting | Description |
|---------|-------------|
| **Client ID** | Unique identifier (like `myapp` or `mobile-app`) |
| **Root URL** | Base URL of your application |
| **Valid Redirect URIs** | Allowed URLs to redirect after login (security!) |
| **Web Origins** | Allowed CORS origins |
| **Standard Flow** | Authorization Code flow (recommended for most) |
| **Direct Access Grants** | Allow username/password in API call |
| **Service Accounts** | Client can get tokens without user |

**Client Flow Visualization:**

```
┌──────────────────┐                      ┌──────────────────┐
│  Your App        │                      │    Keycloak      │
│  (Client)        │                      │                  │
└────────┬─────────┘                      └────────┬─────────┘
         │                                         │
         │  1. User clicks "Login"                 │
         │────────────────────────────────────────►│
         │                                         │
         │  2. Redirect to Keycloak login page     │
         │◄────────────────────────────────────────│
         │                                         │
         │  3. User enters credentials             │
         │────────────────────────────────────────►│
         │                                         │
         │  4. Redirect back with authorization code
         │◄────────────────────────────────────────│
         │                                         │
         │  5. Exchange code for tokens            │
         │────────────────────────────────────────►│
         │                                         │
         │  6. Return tokens (ID, Access, Refresh) │
         │◄────────────────────────────────────────│
         │                                         │
```

> Deep Dive: [Managing Clients](https://www.keycloak.org/docs/latest/server_admin/#assembly-managing-clients_server_administration_guide)

---

### 1.6 Client Scopes

**Client Scopes** are reusable configurations that define what information (claims) should be included in tokens. They help avoid repetitive configuration across clients.

**Key Points:**
- Scopes control what user information appears in tokens
- Scopes can be shared across multiple clients
- Two types: Default (always applied) and Optional (on request)
- Standard OIDC scopes are built-in

**Why Client Scopes Exist:**

Without scopes, you'd configure protocol mappers for each client separately. With scopes, you define once and reuse.

**Scope Types:**

| Type | Description | Example |
|------|-------------|---------|
| **Default Scopes** | Always included in tokens | `openid`, `profile` |
| **Optional Scopes** | Only when explicitly requested | `address`, `phone` |

**Standard OIDC Scopes:**

| Scope | Claims Included |
|-------|-----------------|
| `openid` | **Required for OIDC**. Includes `sub` (subject/user ID) |
| `profile` | `name`, `family_name`, `given_name`, `middle_name`, `nickname`, `preferred_username`, `picture`, `website`, `gender`, `birthdate`, `zoneinfo`, `locale`, `updated_at` |
| `email` | `email`, `email_verified` |
| `address` | `address` (JSON object with street, city, etc.) |
| `phone` | `phone_number`, `phone_number_verified` |
| `offline_access` | Enables refresh tokens for offline use |
| `roles` | User's roles |

**How Scopes Work:**

```
Client: mobile-app
├── Default Scopes: openid, profile, email
│   └── Tokens ALWAYS include these claims
└── Optional Scopes: address, phone
    └── Tokens include these only if requested

Request: /auth?...&scope=openid profile address
Result: Token includes openid + profile (default) + address (requested optional)
```

> Deep Dive: [Client Scopes](https://www.keycloak.org/docs/latest/server_admin/#_client_scopes)

---

### 1.7 Protocol Mappers

**Protocol Mappers** configure exactly what claims (data fields) appear in tokens and how they're formatted. They transform user data into token claims.

**Key Points:**
- Mappers are attached to client scopes or directly to clients
- They define which user attributes become token claims
- You can create custom mappers for any data
- Different mapper types for different data sources

**Mapper Types:**

| Mapper Type | Source | Example Use |
|-------------|--------|-------------|
| **User Attribute** | Custom user attributes | Add `department` claim from user attribute |
| **User Property** | Built-in user fields | Add `email` to token |
| **User Realm Role** | User's realm roles | Include roles in token |
| **User Client Role** | User's client roles | Include app-specific roles |
| **Group Membership** | User's groups | Include groups in token |
| **Hardcoded Claim** | Static value | Add `iss` or custom static claim |
| **Audience** | Token metadata | Specify token audience |

**Mapper Configuration:**

| Setting | Description |
|---------|-------------|
| **Claim Name** | Name of the claim in the token |
| **Token Claim Name** | JSON path in token (e.g., `address.street`) |
| **Claim JSON Type** | String, long, int, boolean, JSON |
| **Add to ID Token** | Include in ID token? |
| **Add to Access Token** | Include in access token? |
| **Add to Userinfo** | Include in userinfo endpoint response? |

**Example:**

```
Mapper: department-mapper
├── Type: User Attribute
├── User Attribute: department
├── Token Claim Name: department
├── Add to ID Token: Yes
├── Add to Access Token: Yes

Result: User with department="Engineering" gets token with:
{
  "sub": "user-123",
  "department": "Engineering",
  ...
}
```

> Deep Dive: [Protocol Mappers](https://www.keycloak.org/docs/latest/server_admin/#_protocol-mappers)

---

## 2. Authentication & Authorization

This section explains how Keycloak authenticates users and issues tokens that applications use for authorization.

### 2.1 How Authentication Works

Keycloak uses **redirect-based authentication**. Users never share their credentials with your application - they only enter them on Keycloak's login page. Your application receives cryptographically signed tokens proving the user's identity.

**The Complete Flow:**

```
┌──────────┐                    ┌─────────────┐                    ┌──────────────┐
│   User   │                    │ Application │                    │   Keycloak   │
│ (Browser)│                    │  (Client)   │                    │              │
└────┬─────┘                    └──────┬──────┘                    └──────┬───────┘
     │                                 │                                  │
     │  1. Access protected resource   │                                  │
     │────────────────────────────────►│                                  │
     │                                 │                                  │
     │                                 │  2. No session? Redirect to KC   │
     │◄────────────────────────────────│                                  │
     │                                 │                                  │
     │  3. Browser redirects to Keycloak login page                       │
     │───────────────────────────────────────────────────────────────────►│
     │                                 │                                  │
     │  4. Keycloak shows login form   │                                  │
     │◄───────────────────────────────────────────────────────────────────│
     │                                 │                                  │
     │  5. User enters username/password                                  │
     │───────────────────────────────────────────────────────────────────►│
     │                                 │                                  │
     │                                 │     6. Keycloak validates creds  │
     │                                 │        (local DB, LDAP, etc.)    │
     │                                 │                                  │
     │  7. Redirect back to app with authorization code                   │
     │◄───────────────────────────────────────────────────────────────────│
     │                                 │                                  │
     │  8. Browser follows redirect    │                                  │
     │────────────────────────────────►│                                  │
     │                                 │                                  │
     │                                 │  9. Exchange code for tokens     │
     │                                 │─────────────────────────────────►│
     │                                 │                                  │
     │                                 │  10. Return tokens               │
     │                                 │◄─────────────────────────────────│
     │                                 │                                  │
     │  11. App validates tokens, creates session, grants access          │
     │◄────────────────────────────────│                                  │
     │                                 │                                  │
```

**Security Benefits:**
- Application never sees user's password
- Tokens are cryptographically signed (can't be forged)
- Tokens have short lifetimes (limits damage if stolen)
- Single point for security updates

---

### 2.2 Tokens

Keycloak issues three types of tokens. Understanding them is crucial.

**Token Types:**

| Token | Purpose | Lifetime | Contains |
|-------|---------|----------|----------|
| **ID Token** | Proves WHO the user is | Short (minutes) | User identity info |
| **Access Token** | Proves WHAT user can do | Short (minutes) | Roles, permissions |
| **Refresh Token** | Gets new tokens | Long (hours/days) | Session reference |

**ID Token (Identity):**

The ID Token is for your application to know who logged in. It's a JWT containing user information.

```json
{
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "John Doe",
  "preferred_username": "john.doe",
  "email": "john@example.com",
  "email_verified": true,
  "given_name": "John",
  "family_name": "Doe",
  "iat": 1704067200,
  "exp": 1704070800,
  "iss": "https://keycloak.example.com/realms/myrealm",
  "aud": "my-client"
}
```

**Access Token (Authorization):**

The Access Token is sent to APIs/services to prove the user's permissions.

```json
{
  "sub": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "preferred_username": "john.doe",
  "realm_access": {
    "roles": ["user", "manager"]
  },
  "resource_access": {
    "my-client": {
      "roles": ["editor", "viewer"]
    }
  },
  "scope": "openid profile email",
  "iat": 1704067200,
  "exp": 1704070800
}
```

**Refresh Token:**

The Refresh Token is used to get new ID and Access tokens without re-authentication.

```
┌─────────────┐                              ┌──────────────┐
│ Application │                              │   Keycloak   │
└──────┬──────┘                              └──────┬───────┘
       │                                            │
       │  Access token expired!                     │
       │                                            │
       │  POST /token                               │
       │  grant_type=refresh_token                  │
       │  refresh_token=xxx                         │
       │───────────────────────────────────────────►│
       │                                            │
       │  New ID token + Access token               │
       │◄───────────────────────────────────────────│
       │                                            │
       │  (User didn't have to log in again!)       │
```

**Token Lifetimes (Configurable):**

| Token | Typical Lifetime | Why |
|-------|------------------|-----|
| **Access Token** | 5-15 minutes | Short = less damage if stolen |
| **ID Token** | 5-15 minutes | Same as access token |
| **Refresh Token** | 30 days (or session) | Convenience vs security tradeoff |
| **SSO Session** | 8-24 hours | How long until user must re-login |

> Deep Dive: [Using OpenID Connect](https://www.keycloak.org/docs/latest/server_admin/#_oidc)

---

### 2.3 Authentication Flows

**Authentication Flows** are configurable workflows that define what steps a user must complete during authentication. They're like flowcharts that Keycloak executes.

**Key Points:**
- Flows are made up of "executions" (steps)
- Each execution can be Required, Alternative, Conditional, or Disabled
- You can create custom flows for different scenarios
- Different flows for browser login, API login, registration, password reset

**Built-in Flows:**

| Flow | Purpose |
|------|---------|
| **Browser** | Standard web login (username/password, optional MFA) |
| **Direct Grant** | API-based login (send credentials, get tokens directly) |
| **Registration** | User self-registration |
| **Reset Credentials** | Password reset flow |
| **First Broker Login** | What happens when user first logs in via external IDP |

**Flow Structure Example (Browser Flow):**

```
Browser Flow
├── Cookie [ALTERNATIVE]
│   └── Check if user has valid SSO session cookie
├── Kerberos [DISABLED]
│   └── Windows domain authentication (usually disabled)
└── Forms [ALTERNATIVE]
    ├── Username Password Form [REQUIRED]
    │   └── Show login form, validate credentials
    └── OTP Form [CONDITIONAL]
        └── If user has OTP configured, require it
```

**Execution Requirements:**

| Requirement | Behavior |
|-------------|----------|
| **REQUIRED** | Must succeed. If fails, authentication fails. |
| **ALTERNATIVE** | One of the alternatives must succeed. |
| **CONDITIONAL** | Only executes if condition is met. |
| **DISABLED** | Skipped entirely. |

**Custom Flow Example (Step-up Authentication):**

```
Banking Flow
├── Username Password Form [REQUIRED]
│   └── Basic login
├── OTP Form [REQUIRED]
│   └── Always require OTP for banking
└── Device Verification [CONDITIONAL]
    └── If new device, require additional verification
```

**Creating Custom Flows:**

1. Go to Authentication → Flows
2. Copy an existing flow or create new
3. Add/remove/reorder executions
4. Configure requirements for each step
5. Bind flow to a client or make it the realm default

> Deep Dive: [Authentication Flows](https://www.keycloak.org/docs/latest/server_admin/#_authentication-flows)

---

### 2.4 Sessions

**Sessions** track user login state. Keycloak manages sessions to enable Single Sign-On and control user access.

**Session Types:**

| Session Type | Description |
|--------------|-------------|
| **SSO Session** | Master session created at login, enables SSO across clients |
| **Client Session** | Per-client session tracking which clients the user has accessed |
| **Offline Session** | Persistent session for offline token refresh |

**How SSO Works via Sessions:**

```
User logs into App A
        │
        ▼
┌─────────────────────────────────────────┐
│  Keycloak creates SSO Session           │
│  Sets session cookie in browser         │
└─────────────────────────────────────────┘
        │
        ▼
User accesses App B
        │
        ▼
┌─────────────────────────────────────────┐
│  App B redirects to Keycloak            │
│  Keycloak sees session cookie           │
│  User already authenticated!            │
│  Redirect back to App B with tokens     │
│  (No login form shown)                  │
└─────────────────────────────────────────┘
```

**Session Settings:**

| Setting | Description |
|---------|-------------|
| **SSO Session Idle** | How long until idle session expires |
| **SSO Session Max** | Maximum session lifetime regardless of activity |
| **Client Session Idle** | Per-client idle timeout |
| **Offline Session Idle** | Idle timeout for offline sessions |
| **Remember Me** | Extended session for "remember me" checkbox |

**Session Management:**

Administrators can:
- View all active sessions
- See which clients each user is logged into
- Terminate sessions (force logout)
- See session metadata (IP, login time, etc.)

**Logout Types:**

| Logout Type | Description |
|-------------|-------------|
| **Single Logout** | Logout from one app logs out from all apps |
| **Backchannel Logout** | Keycloak notifies all clients to logout |
| **Frontchannel Logout** | Browser visits each client's logout URL |

> Deep Dive: [Managing User Sessions](https://www.keycloak.org/docs/latest/server_admin/#managing-user-sessions)

---

## 3. Identity Brokering

**Identity Brokering** allows Keycloak to delegate authentication to external Identity Providers. Instead of managing credentials itself, Keycloak trusts another system to authenticate the user.

**Key Points:**
- Users can "Login with Google/Facebook/etc."
- Enterprise users can login via their corporate SAML/OIDC provider
- Keycloak acts as a middleman (broker) between your apps and external IDPs
- Users can link multiple external identities to one Keycloak account

**How It Works:**

```
┌──────────┐     ┌──────────────┐     ┌───────────────────┐
│   User   │ ──► │   KEYCLOAK   │ ──► │ External Identity │
│          │     │   (Broker)   │     │    Provider       │
│          │ ◄── │              │ ◄── │ (Google, Okta)    │
└──────────┘     └──────────────┘     └───────────────────┘
                        │
                        ▼
                 Creates/links user
                 in Keycloak
```

**Detailed Flow:**

```
1. User clicks "Login with Google" on your app
2. App redirects to Keycloak
3. Keycloak redirects to Google
4. User authenticates with Google
5. Google redirects back to Keycloak with identity info
6. Keycloak creates/updates local user
7. Keycloak redirects to your app with tokens
8. Your app only deals with Keycloak tokens (not Google tokens)
```

**Supported Identity Providers:**

| Category | Providers |
|----------|-----------|
| **Social** | Google, Facebook, GitHub, Twitter, LinkedIn, Microsoft, Instagram, PayPal, GitLab, Bitbucket, Stack Overflow |
| **OpenID Connect** | Any OIDC-compliant provider |
| **OAuth 2.0** | Any OAuth 2.0 provider |
| **SAML 2.0** | Okta, Azure AD, ADFS, any SAML provider |

**First Login Flow:**

When a user logs in via external IDP for the first time, Keycloak runs a "first login flow" to handle user creation:

| Step | Action |
|------|--------|
| **Review Profile** | User confirms/updates profile info |
| **Create User** | Create new Keycloak user from IDP data |
| **Link Existing** | Or link to existing Keycloak account |
| **Verify Email** | Optionally verify email |

**Identity Provider Mappers:**

Map claims from external IDP to Keycloak user attributes:

```
Google sends: { "picture": "https://..." }
                    │
                    ▼
Mapper: Attribute Importer
├── Source: picture
└── Target: profilePicture (user attribute)
```

**Account Linking:**

Users can link multiple external accounts to one Keycloak account:

```
Keycloak User: john@example.com
├── Linked: Google (john@gmail.com)
├── Linked: GitHub (johndoe)
└── Linked: Corporate SAML (john@corp.com)

User can login with any of these!
```

> Deep Dive: [Identity Brokering](https://www.keycloak.org/docs/latest/server_admin/#_identity_broker)

---

## 4. User Federation

**User Federation** connects Keycloak to external user storage systems. Instead of migrating users into Keycloak, you can validate credentials against existing systems.

**Key Points:**
- Authenticate users against LDAP/Active Directory without migration
- Create custom providers for any user store (databases, APIs)
- Users can be "imported" or validated in real-time
- Supports attribute and role synchronization

**Federation vs. Brokering:**

| Feature | User Federation | Identity Brokering |
|---------|-----------------|-------------------|
| **Purpose** | Connect to user database | Delegate to another IDP |
| **Credentials** | Stored in external system | Managed by external IDP |
| **Protocol** | LDAP, custom SPI | OIDC, SAML, OAuth |
| **User Creation** | Users exist in external store | IDP manages user lifecycle |
| **Example** | LDAP, custom PostgreSQL | Google, Azure AD |

**How Federation Works:**

```
┌──────────────┐                    ┌────────────────────┐
│   KEYCLOAK   │ ◄───────────────►  │   LDAP / Active    │
│              │   1. Lookup user   │   Directory        │
│              │   2. Validate pwd  └────────────────────┘
│              │   3. Sync attrs
│              │
│              │ ◄───────────────►  ┌────────────────────┐
│              │   Custom Provider  │   Legacy Database  │
└──────────────┘                    │   (PostgreSQL)     │
                                    └────────────────────┘
```

**LDAP/Active Directory Integration:**

| Feature | Description |
|---------|-------------|
| **User Import** | Copy users into Keycloak DB (one-time or periodic sync) |
| **On-demand** | Query LDAP at login time only |
| **Password Validation** | Validate passwords against LDAP |
| **Attribute Mapping** | Map LDAP attributes to Keycloak |
| **Role Mapping** | Map LDAP groups to Keycloak roles |
| **Writeback** | Optionally sync changes back to LDAP |

**LDAP Mappers:**

| Mapper | Purpose |
|--------|---------|
| **User Attribute Mapper** | Map LDAP attr to user attr |
| **Full Name Mapper** | Parse full name into first/last |
| **Group Mapper** | Map LDAP groups to Keycloak groups |
| **Role Mapper** | Map LDAP groups to Keycloak roles |
| **MSAD Mapper** | Microsoft AD specific mappings |

**Custom User Storage SPI:**

For non-LDAP systems, implement User Storage SPI:

```java
public class MyUserStorageProvider implements
    UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator {

    // Look up user by username
    UserModel getUserByUsername(String username, RealmModel realm);

    // Validate password
    boolean isValid(RealmModel realm, UserModel user, CredentialInput input);
}
```

**Use Cases:**
- Authenticate against legacy database
- Integrate with HR system
- Connect to custom identity store
- Gradual migration from legacy system

> Deep Dive: [User Federation](https://www.keycloak.org/docs/latest/server_admin/#_user-storage-federation)

---

## 5. Security Features

Keycloak provides comprehensive security features out of the box.

### 5.1 Password Policies

Configure rules for user passwords at the realm level.

**Available Policies:**

| Policy | Description | Example |
|--------|-------------|---------|
| **Length** | Minimum password length | Minimum 8 characters |
| **Digits** | Required number of digits | At least 1 digit |
| **Lowercase** | Required lowercase letters | At least 1 lowercase |
| **Uppercase** | Required uppercase letters | At least 1 uppercase |
| **Special Characters** | Required special characters | At least 1 of !@#$%^&* |
| **Not Username** | Password can't contain username | Prevents `john123` for user `john` |
| **Not Email** | Password can't contain email | Additional security |
| **Password History** | Prevent reuse | Can't reuse last 5 passwords |
| **Regex Pattern** | Custom regex validation | Any pattern you define |
| **Expire Password** | Force periodic change | Every 90 days |
| **Hashing Algorithm** | How passwords are stored | pbkdf2-sha256, pbkdf2-sha512 |
| **Hashing Iterations** | Computational cost | Higher = more secure, slower |

**Example Policy:**

```
Password must:
✓ Be at least 12 characters
✓ Contain at least 1 uppercase letter
✓ Contain at least 1 lowercase letter
✓ Contain at least 1 digit
✓ Contain at least 1 special character
✓ Not contain username
✓ Not match last 5 passwords
```

> Deep Dive: [Password Policies](https://www.keycloak.org/docs/latest/server_admin/#_password-policies)

---

### 5.2 Brute Force Protection

Protect against password guessing attacks by detecting and blocking repeated failed login attempts.

**How It Works:**

```
Login Attempt #1: Failed
Login Attempt #2: Failed
Login Attempt #3: Failed
Login Attempt #4: Failed
Login Attempt #5: Failed
────────────────────────────
ACCOUNT LOCKED for 30 minutes
```

**Configuration Options:**

| Setting | Description |
|---------|-------------|
| **Max Login Failures** | Failures before lockout (e.g., 5) |
| **Wait Increment** | Initial lockout duration (e.g., 60 seconds) |
| **Max Wait** | Maximum lockout duration (e.g., 15 minutes) |
| **Failure Reset Time** | Time to reset failure count |
| **Quick Login Check** | Detect rapid attempts |
| **Minimum Quick Login Wait** | Delay for rapid attempts |

**Lockout Strategies:**

| Strategy | Description |
|----------|-------------|
| **Permanent Lockout** | Account locked until admin unlocks |
| **Temporary Lockout** | Locked for increasing duration |
| **IP-based** | Lock specific IP address |

> Deep Dive: [Brute Force Detection](https://www.keycloak.org/docs/latest/server_admin/#password-guess-brute-force-attacks)

---

### 5.3 Multi-Factor Authentication (MFA)

Add additional verification beyond passwords.

**Supported MFA Methods:**

| Method | Description |
|--------|-------------|
| **TOTP** | Time-based One-Time Password (Google Authenticator, Authy) |
| **HOTP** | Counter-based One-Time Password |
| **WebAuthn** | Hardware security keys (YubiKey), biometrics |
| **Custom** | SMS OTP, Email OTP via custom authenticators |

**OTP Configuration:**

| Setting | Description |
|---------|-------------|
| **OTP Type** | TOTP or HOTP |
| **Algorithm** | SHA1, SHA256, SHA512 |
| **Digits** | 6 or 8 digit codes |
| **Period** | How often TOTP changes (30 seconds) |
| **Look Ahead Window** | Clock drift tolerance |

**Conditional MFA:**

You can require MFA only in certain conditions:
- For specific clients
- For users with certain roles
- When accessing from new device
- Based on user attributes

**WebAuthn/Passkeys:**

| Feature | Description |
|---------|-------------|
| **Passwordless** | Login with just security key/biometric |
| **2FA** | Use as second factor after password |
| **Resident Keys** | Store credentials on device |
| **Attestation** | Verify authenticator type |

> Deep Dive: [OTP Policies](https://www.keycloak.org/docs/latest/server_admin/#otp-policies)

---

### 5.4 SSL/HTTPS Requirements

| Mode | Description |
|------|-------------|
| **External Requests** | HTTPS required for external, HTTP ok for localhost |
| **None** | HTTP allowed (development only!) |
| **All Requests** | HTTPS required for everything |

---

## 6. Administration

### 6.1 Admin Console

The web-based UI for managing Keycloak (accessible at `/admin`).

**Main Sections:**

| Section | Purpose |
|---------|---------|
| **Realm Settings** | Configure realm-level settings |
| **Clients** | Manage applications |
| **Client Scopes** | Configure reusable scope settings |
| **Roles** | Define realm roles |
| **Users** | Manage users |
| **Groups** | Manage groups |
| **Sessions** | View/manage active sessions |
| **Events** | View login/admin events |
| **Identity Providers** | Configure social/SAML login |
| **User Federation** | Configure LDAP/custom providers |
| **Authentication** | Configure authentication flows |

### 6.2 Admin CLI (kcadm)

Command-line tool for scripting and automation.

```bash
# Login
kcadm.sh config credentials --server http://localhost:8080 \
  --realm master --user admin --password admin

# Create realm
kcadm.sh create realms -s realm=myrealm -s enabled=true

# Create user
kcadm.sh create users -r myrealm -s username=john -s enabled=true

# Set password
kcadm.sh set-password -r myrealm --username john --new-password secret

# Create client
kcadm.sh create clients -r myrealm -s clientId=myapp -s enabled=true
```

### 6.3 Admin REST API

Programmatic access to all admin functions.

```bash
# Get access token
TOKEN=$(curl -s -X POST \
  "http://localhost:8080/realms/master/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" | jq -r '.access_token')

# List users
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/admin/realms/myrealm/users"
```

> Deep Dive: [Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/index.html)

---

### 6.4 Events & Auditing

Keycloak logs two types of events for audit and monitoring.

**Login Events:**

| Event | Description |
|-------|-------------|
| `LOGIN` | Successful login |
| `LOGIN_ERROR` | Failed login |
| `LOGOUT` | User logout |
| `REGISTER` | User registration |
| `CODE_TO_TOKEN` | Authorization code exchanged |
| `REFRESH_TOKEN` | Token refreshed |

**Admin Events:**

| Event | Description |
|-------|-------------|
| `CREATE` | Resource created (user, client, etc.) |
| `UPDATE` | Resource updated |
| `DELETE` | Resource deleted |
| `ACTION` | Action performed (reset password, etc.) |

**Event Configuration:**

| Setting | Description |
|---------|-------------|
| **Save Events** | Store events in database |
| **Expiration** | How long to keep events |
| **Event Listeners** | Send events to external systems |

**Event Listeners:**

- **jboss-logging**: Log to server log
- **email**: Send email notifications
- **Custom**: Implement your own listener

> Deep Dive: [Auditing and Events](https://www.keycloak.org/docs/latest/server_admin/#auditing-and-events)

---

## 7. Extension Points (SPIs)

Keycloak is highly extensible via Service Provider Interfaces (SPIs). You can customize almost every aspect.

**Common SPIs:**

| SPI | Purpose | Example Use |
|-----|---------|-------------|
| **Authenticator** | Custom authentication steps | SMS OTP, custom login logic |
| **User Storage** | Custom user federation | Connect to legacy database |
| **Event Listener** | React to events | Send Slack notification on login |
| **Protocol Mapper** | Custom token claims | Add custom data to tokens |
| **Theme** | Custom UI | Brand login pages |
| **Required Action** | Custom post-login actions | Custom terms acceptance |
| **Identity Provider** | Custom IDP integration | Proprietary SSO system |

**Creating an SPI:**

1. Implement the Provider interface
2. Implement the ProviderFactory interface
3. Register in `META-INF/services/`
4. Deploy JAR to Keycloak

**Example Structure:**

```
my-spi.jar
├── com/example/MyAuthenticator.java
├── com/example/MyAuthenticatorFactory.java
└── META-INF/services/
    └── org.keycloak.authentication.AuthenticatorFactory
```

> Deep Dive: [Server Developer Guide](https://www.keycloak.org/docs/latest/server_development/)

---

## 8. Quick Start (Docker)

### Start Keycloak

```bash
docker run -p 8080:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

### Access Points

| URL | Purpose |
|-----|---------|
| `http://localhost:8080/admin` | Admin Console |
| `http://localhost:8080/realms/{realm}/account` | User Account Console |
| `http://localhost:8080/realms/{realm}/.well-known/openid-configuration` | OIDC Discovery |

### Setup Checklist

| Step | Action |
|------|--------|
| 1 | Access Admin Console at `/admin` |
| 2 | Create new realm (don't use master) |
| 3 | Create a test user with password |
| 4 | Create a client for your application |
| 5 | Configure redirect URIs for your app |
| 6 | Test login flow |

> Deep Dive: [Getting Started](https://www.keycloak.org/getting-started/getting-started-docker)

---

## 9. Important Endpoints Reference

### OIDC Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/.well-known/openid-configuration` | Discovery document (lists all endpoints) |
| `/protocol/openid-connect/auth` | Start authorization (login) |
| `/protocol/openid-connect/token` | Exchange code for tokens |
| `/protocol/openid-connect/userinfo` | Get user info with access token |
| `/protocol/openid-connect/logout` | Logout |
| `/protocol/openid-connect/certs` | Public keys for token verification |
| `/protocol/openid-connect/token/introspect` | Validate token |

All endpoints are prefixed with `/realms/{realm-name}`

### Admin API Endpoints

| Endpoint | Purpose |
|----------|---------|
| `GET /admin/realms` | List realms |
| `GET /admin/realms/{realm}/users` | List users |
| `POST /admin/realms/{realm}/users` | Create user |
| `GET /admin/realms/{realm}/clients` | List clients |
| `GET /admin/realms/{realm}/roles` | List realm roles |
| `GET /admin/realms/{realm}/groups` | List groups |

---

## 10. Quick Reference Card

### Terminology Cheat Sheet

| Term | One-Line Definition |
|------|---------------------|
| **Realm** | Isolated tenant containing users, clients, roles |
| **Client** | Application registered to use Keycloak |
| **User** | Entity that can authenticate |
| **Role** | Permission label assigned to users |
| **Group** | Collection of users with shared roles/attributes |
| **Scope** | Set of claims to include in tokens |
| **Flow** | Configurable authentication workflow |
| **IDP** | External identity provider (Google, SAML) |
| **Federation** | Connection to external user store (LDAP) |
| **Mapper** | Transforms data into token claims |
| **Session** | Login state enabling SSO |
| **SPI** | Plugin interface for customization |

### Common Tasks Quick Reference

| Task | Navigation |
|------|------------|
| Create realm | Admin Console → Create Realm |
| Create user | Realm → Users → Create user |
| Reset password | Users → Select user → Credentials → Reset |
| Assign role to user | Users → Select user → Role mapping |
| Create client | Realm → Clients → Create client |
| Configure MFA | Realm → Authentication → Required actions → Configure OTP |
| Add social login | Realm → Identity providers → Add provider |
| Connect LDAP | Realm → User federation → Add LDAP |
| Customize login page | Realm → Realm settings → Themes |
| View active sessions | Realm → Sessions |
| View login events | Realm → Events → Login events |
| Create custom flow | Realm → Authentication → Create flow |

---

## Official Documentation Links

| Resource | URL |
|----------|-----|
| **Main Documentation** | https://www.keycloak.org/documentation |
| **Server Admin Guide** | https://www.keycloak.org/docs/latest/server_admin/ |
| **Server Developer Guide** | https://www.keycloak.org/docs/latest/server_development/ |
| **REST API Reference** | https://www.keycloak.org/docs-api/latest/rest-api/index.html |
| **Getting Started** | https://www.keycloak.org/getting-started/getting-started-docker |
| **Authorization Services** | https://www.keycloak.org/docs/latest/authorization_services/ |
| **Release Notes** | https://www.keycloak.org/docs/latest/release_notes/ |
| **GitHub Repository** | https://github.com/keycloak/keycloak |
