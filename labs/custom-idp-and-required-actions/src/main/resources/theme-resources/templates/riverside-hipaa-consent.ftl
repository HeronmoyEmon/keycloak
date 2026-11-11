<#--
  Riverside Health's one-time HIPAA data-handling acknowledgment page, rendered by
  RiversideHipaaAcknowledgmentRequiredAction's requiredActionChallenge().

  Deployed as a THEME RESOURCE, not a whole custom theme: Keycloak's built-in "classpath" Theme
  Resource Provider (org.keycloak.theme.ClasspathThemeResourceProviderFactory, confirmed live for
  26.8.2 via javap against the running image's keycloak-services jar) automatically scans every
  provider jar's classpath for a theme-resources/templates/ directory and merges whatever it finds
  into whichever theme is currently active -- no META-INF/services registration needed for this part,
  and no need to fork/replace the realm's whole login theme just to add one new page. Modeled
  structurally on Keycloak's own built-in terms.ftl (decompiled from the running image's bundled
  keycloak-themes jar as a reference for the correct <#import>/layout macro usage -- the TEXT here is
  original to this course).
-->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        HIPAA Data Handling Acknowledgment
    <#elseif section = "form">
    <div id="kc-hipaa-ack-text">
        <p>
            <#if user?? && user.firstName??>Hello ${user.firstName}, <#else>Hello, </#if>
            before you can continue, Riverside Health requires every clinician to acknowledge the
            following for this login session:
        </p>
        <ul>
            <li>Patient records accessed through this session are covered by HIPAA and Riverside
                Health's internal data-handling policy.</li>
            <li>Access to a patient's record must be for a legitimate treatment, payment, or
                operations purpose &mdash; not out of curiosity, even for a public figure or a
                coworker.</li>
            <li>This session may be reviewed as part of Riverside Health's routine HIPAA audit
                trail (chapter 11's compliance chapter, chapter 28's event-auditing chapter).</li>
        </ul>
        <p>You must acknowledge this notice before your login can complete. This is a Required
            Action (chapter 25) &mdash; it runs after your password/OTP has already been verified,
            not as part of verifying them.</p>
    </div>
    <form class="form-actions" action="${url.loginAction}" method="POST">
        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
               name="submitAction" id="kc-accept" type="submit" value="I Acknowledge"/>
        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}"
               name="cancel" id="kc-decline" type="submit" value="Decline"/>
    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
