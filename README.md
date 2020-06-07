# alonzo
Function as a Service (FaaS) Platform for Vert.x on EC2

Alonzo is AWS Lambda before there was an AWS Lambda. The platform was initially
developed in 2013!

This code is being released to the open source software (OSS) community because
it’s no longer used in our production projects. Rather than nuke the code we
opted to release it so it could help others in some way, shape, or form.

Believe it or not the team ported all of our Alonzo based services to AWS
Lambda in less than 6 weeks. A testament to the framework!

You're probably thinking, if Alonzo was so good; why did we migrate to Lambda?

That’s simple... Tech debt was adding up, server maintenance was adding up, and
we thought it was time to streamline operations and cost. We also had some much
needed Java code housekeeping to do. We had a mix of Java 6 to Java 11 code
peppered around everywhere.

Alonzo component dependencies:

    - Java 11
    - Vert.x 3.9.x
    - GSON
    - Jackson Json
    - Auth0 JWT
    - Bouncy Castle FIPS
    - mJson

### This code will not be maintained.
