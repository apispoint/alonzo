# alonzo
Function as a Service (FaaS) Platform for Vert.x on EC2

This code is being released to the open source software (OSS) community because
it’s no longer used in our production projects. Rather than nuke the code we
opted to release it so others could benefit from it in some way, shape, or form.

Alonzo is a Java and Vert.x based platform designed to deploy microservices on
AWS EC2. The platform was initially developed in 2013 so it pre-dates AWS Lambda
by one (1) year.

On another note, the team ported all of our Alonzo based services to AWS Lambda
in less than 6 weeks. A testament to the framework!

You're probably thinking, if Alonzo was so good; why did we migrate to Lambda?

Well, it wasn't so much Alonzo or Lambda question. After 7 years tech debt was
adding up, server maintenance was adding up, and we had some much needed Java
code housekeeping that needed to be done. It just made sense to move forward
in a new but familar way.

Alonzo component dependencies:

    - Java 11
    - Vert.x 3.9.x
    - GSON
    - Jackson Json
    - Auth0 JWT
    - Bouncy Castle FIPS
    - mJson

### This code will not be maintained.
