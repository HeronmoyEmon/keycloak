/* Suggested-pace roadmap. Schema: CLAUDE.md §9 / §11.
   { part, weekRange, chapters: [slug, ...], deliverable, gate } */
window.ROADMAP = [
  { part: "Part 0 — Orientation", weekRange: "Week 1",
    chapters: ["how-to-use-this-course"],
    deliverable: "Environment ready: Docker, a JDK, curl/Postman.", gate: null },

  { part: "Part I — IAM Foundations", weekRange: "Weeks 1–4",
    chapters: ["what-is-iam","identity-fundamentals","authn-authz-accounting","directory-services",
      "authentication-factors-mfa","authorization-models","sso-and-federation","tokens-and-sessions",
      "iam-threat-landscape"],
    deliverable: "Can explain AuthN/AuthZ, MFA, SSO/federation, and tokens without naming a vendor.",
    gate: "GATE-2" },

  { part: "Part II — Governance & Compliance", weekRange: "Weeks 5–6",
    chapters: ["identity-governance","compliance-for-iam","iam-market-landscape"],
    deliverable: "Can place Keycloak correctly against Okta/Auth0/Entra/Ping and name what GDPR/HIPAA/PCI-DSS require.",
    gate: null },

  { part: "Part III — Protocols", weekRange: "Weeks 7–10",
    chapters: ["oauth2-core","oauth2-extended","openid-connect","jwt-and-crypto","saml2","oauth-oidc-security"],
    deliverable: "Can run the authorization code + PKCE flow by hand with curl and read a JWT's claims.",
    gate: "GATE-3" },

  { part: "Part IV — Keycloak Core Administration", weekRange: "Weeks 11–14",
    chapters: ["keycloak-architecture","installing-keycloak","realms","users-and-credentials",
      "roles-and-groups","clients-and-scopes","authentication-flows","themes-and-branding"],
    deliverable: "Can stand up Keycloak locally and administer a realm end to end.", gate: "GATE-4" },

  { part: "Part V — Keycloak Operations", weekRange: "Weeks 15–16",
    chapters: ["admin-api-and-cli","events-and-auditing","sessions-deep-dive","password-policies-and-mfa"],
    deliverable: "Can automate realm setup and read the audit trail.", gate: null },

  { part: "Part VI — Federation & Integration", weekRange: "Weeks 17–20",
    chapters: ["identity-brokering","user-federation-ldap","custom-user-storage-spi",
      "securing-a-backend-api","securing-a-frontend-spa","service-to-service-auth"],
    deliverable: "Can secure a real backend, frontend, and service account against Keycloak.", gate: null },

  { part: "Part VII — Fine-Grained Authorization", weekRange: "Weeks 21–22",
    chapters: ["keycloak-authorization-services","fine-grained-access-patterns"],
    deliverable: "Can design UMA 2.0 resources/policies/permissions for a real scenario.", gate: "GATE-5" },

  { part: "Part VIII — Extending Keycloak", weekRange: "Weeks 23–24",
    chapters: ["custom-spis","custom-idp-and-required-actions"],
    deliverable: "Can write and deploy a custom SPI jar.", gate: null },

  { part: "Part IX — Production, Scale & Hardening", weekRange: "Weeks 25–28",
    chapters: ["high-availability-clustering","keycloak-on-kubernetes","performance-tuning",
      "backup-and-disaster-recovery","security-hardening-and-upgrades"],
    deliverable: "Can argue for a specific HA/scaling/hardening design, not just follow a checklist.", gate: null },

  { part: "Part X — Case Studies & Capstone", weekRange: "Weeks 29–32",
    chapters: ["case-study-northwind","case-study-riverside","case-study-meridian"],
    deliverable: "A defensible capstone build, presentable in an interview.", gate: "Final QA gate" },

  { part: "Part XI — Career & Reference", weekRange: "Week 33+",
    chapters: ["becoming-an-iam-specialist","master-glossary-and-cheatsheets"],
    deliverable: "Ongoing reference; revisit the interview question bank before interviews.", gate: null }
];
