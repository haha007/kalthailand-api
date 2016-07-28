# kalthailand-api
This repository contains documentation and source code for the KAL APIs

APIs are designed to deliver back end utility endpoints to front end developer to manage / create quotes, policies, coverages, ...
KAL APIs design is inspired by myAxa API project for which swagger documentaion is available here: http://developers-api-axa-com.azurewebsites.net/

# API documentation
Deploy the project "kalthailand-api-ebiz" project to your local server and swagger documentation will be available at http://localhost:8080/swagger-ui.html

# Main Objects
Quote: a wrapper to store informations coming from front end. It may contain some calculated fields (age from date of birth, premiums, ...) if there is enough data to calculate them. There is some validation of a quote but very little. Therefore, a Quote object might contain very little information.

Policy: very much similar to a Quote except that it has been validated by back end (all data has been checked and validated). A Policy will not be saved in database if it does not contain everything needed. Therefore a developer is assured that a Policy object is a valid Policy. It may contain a transaction ID when payment has been done online.

# Main APIs description
This part assumes the project has been deployed to a server available at the address: /ebiz

Quote API endpoints:
- **_/ebiz/quote_**, method **_POST_**: returns a quote. Front end developer may send a session ID and a session type (like 'LINE') if they want the quote to be saved and linked to current user. In case a previous quote has been found it will be returned as it was in its latest state. If no quote has been found or if no session id / type has been provided, a new empty quote will be returned, with its entire structure
- **_/ebiz/quote_**, method **_PUT_**: updates an existing quote. This endpoint is not linked to the UI / front end and may be called whenever user has provided some information. There is little validation (age and amount) and most fields are allowed as empty

Policy API endpoints:
- **_/ebiz/policy_**, method **_POST_**: creates a policy out of a quote if and only if all data has been validated
- **_/ebiz/policy_**, method **_PUT_**: updates a policy. Possibly to save an online transaction status (so far only LINE is supported)

Maven build:
- On local machine, if you are using Tomcat, then use command line:
  mvn clean install
- On production and UAT, it's using JBoss which is an JavaEE container, so we need to use command line:
  mvn clean install -P jee
Reason:
The artifact "javax.mail" in group "com.sun.mail" package is provided by JavaEE container. So with JBoss, it's already provided. But with Tomcat, we have to include it in libraries.
(You can see more detail in api-parent/pom.xml)

You can check the information of current API in this link:
http://localhost:8080/api-elife/project-info