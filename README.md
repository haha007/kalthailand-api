# Introduction
This repository contains documentation and source code for the KAL APIs

APIs are designed to deliver back end utility endpoints to front end developer to manage / create quotes, policies, coverages, ...
KAL APIs design is inspired by myAxa API project for which swagger documentaion is available here: http://developers-api-axa-com.azurewebsites.net/

API documentation
-----------------
Deploy the project "kalthailand-api" project to your local server and swagger documentation will be available at http://localhost:8080/api-elife/swagger-ui.html

Maven build
-----------
- On local machine, if you are using Tomcat, then use command line:
  ```
  mvn clean install -DskipTests
  ```
- On production and UAT, it's using JBoss which is an JavaEE container, so we need to use command line:
  ````
  mvn clean install -P jee -DskipTests
  ````

Reason:
The artifact "javax.mail" in group "com.sun.mail" package is provided by JavaEE container. So with JBoss, it's already provided. But with Tomcat, we have to include it in libraries.
(You can see more detail in api-parent/pom.xml)

Note:
In the first time, you may get error when building admin-elife.
````
cd kalthailand-admin-elife
bower install
````
When bower asking for choosing the angular version , select v.1.5.11 for ng-spring-boot.
Then rebuild the project by maven command line.

You can check the information of current API in this link
---------------------------------------------------------
http://localhost:8080/api-elife/project-info

Initiate data
-------------
Before starting the project, you should import some init data into MongoDB:
Import data inside all *.json files in folder /json into MongoDB (don't import policyNumber_prd_xxx.json to your local machine)' by following command lines:
```
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection lineToken --file /SourceCode/kalthailand-api/json/lineToken.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection occupationType --file /SourceCode/kalthailand-api/json/occupationType.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection policyNumber --file /SourceCode/kalthailand-api/json/policyNumber.json --jsonArray --drop 
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection productIBeginRate --file /SourceCode/kalthailand-api/json/productIBeginRate.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection productIFineRate --file /SourceCode/kalthailand-api/json/productIFineRate.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection productIGenRate --file /SourceCode/kalthailand-api/json/productIGenRate.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection productIProtectDiscountRate --file /SourceCode/kalthailand-api/json/productIProtectDiscountRate.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection productIProtectRate --file /SourceCode/kalthailand-api/json/productIProtectRate.json --jsonArray --drop
mongoimport --host localhost --db elife --port 27117 -u elifeuser -p password --collection productPremiumRate --file /SourceCode/kalthailand-api/json/productPremiumRate.json --jsonArray --drop
```

Update LINE AccessToken in MongoDB
----------------------------------
Go to database and use the following commandline:

```
db.getCollection('lineToken').update(
    // query
    {},

    // update
    {$set: {'accessToken': 'NEW_ACCESS_TOKEN', 'refreshToken':'NEW_REFRESH_TOKEN', 'expireDate':'2017/XX/XX'}},

    // options
    {
        "multi" : true,  // update only one document
        "upsert" : false  // insert a new document, if no existing document match the query
    }
);
```

UAT & Production Deployment
---------------------------
To deploy, just copy *.war files to deployments folder, that's it!
```
/opt/EAP-6.4.0/standalone/deployments
```

If you need to restart the JBoss, use following commandline:
```
sudo service jboss-as-standalone.sh restart
```

Log folder:
````
cd /opt/elife/logs/
````
api-elife*.log

Project structure
-----------------
In the past, the project was structured by layers design.
But after that, I try to apply the Domain Driven Design (there's some reference links at the bottom), but it's not completed yet.
So the code is mixing between the old way and the new way, I hope you to continue with the design.
Besides that, you will see there are some bad code with a lot of //TODO comments, but I don't have enough time to refactor all of them. So please help me to continue.


Reference links:
Uncle Bob says something similar for years now, and calls for a use-case driven approach. Some of his blog posts provide details and are worth reading:
- https://blog.8thlight.com/uncle-bob/2011/09/30/Screaming-Architecture.html 
- https://blog.8thlight.com/uncle-bob/2012/08/13/the-clean-architecture.html

His use-case driven approach is heavily inspired by the following book, that I confess not having read myself: 
- Ivar Jacobson, *Object Oriented Software Engineering: A Use Case Driven Approach*

The notion of "tyranny of the dominant decomposition" first appeared in the following research paper which, among others, led to aspect oriented programming:
- Peri Tarr, Harold Ossher, William Harrison, and Stanley M. Sutton Jr.: *N Degrees of Separation: Multi- Dimensional Separation of Concerns, ICSE-21, 1999.*

Escaping the table -> repository -> service -> resource antipattern can be done by investing more massively at the resource layer and forcing independance to the data entities coming from the database model. In other words, strongly decoupling the resources from the database tables. 
The way to do so, as well as many other good patterns can be found in the Domain-Driven Design book:
- Eric Evans, *Domain-Driven Design: Tacking Complexity In the Heart of Software*

All good resources; I strongly invite engineers & developers to find some time to read them.
