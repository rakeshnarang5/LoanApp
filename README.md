# LoanApp

This is a simple loan app where a user can apply for loans and repay them.

Admin can approve and monitor the loans.

## Requirements
Install JDK 22 https://www.oracle.com/in/java/technologies/downloads/#jdk22-mac

Clone repo: -

git clone https://github.com/rakeshnarang5/LoanApp.git

Install Maven https://maven.apache.org/download.cgi

Build project: -

mvn clean install 

Run from terminal: -

java -jar target/LoanApp-1.0-SNAPSHOT.jar

Access the server at http://localhost:8080/

## Assumptions

1. User will create loan of minimum $100 (this value is configurable from application.properties) and for at least 1 week (this is done so that system is exploited for tiny loans like $1)

2. Rounding error will always be added to the last EWI (Equal Weekly Installments)

3. Database implementation is not done in this project but a repository is used which can later be replaced with a SQL / NoSQL DB implementation (since loan apps require ACID properties, **Relational database will be my choice**)

4. If a user makes more payment that required, then that extra amount will be kept as residual amount and to the user at the end of the loan term. It will not be adjusted against next EWI.

5. If user tries to make payment after due date, then that loan will go into Defaulted state and special handling can be done for it, which is kept open at the moment.

## Login via Postman

**This API is managed by Spring Security.**

http://localhost:8080/login

In body enter format data for regular users

Regular users: -

rohan => rohan@123

rajeev => rajeev @123

Admin user: -

alpha => alpha@123

![img.png](img.png)

## Logout via Postman

**This API is also managed by Spring Security**

If you want to change users, click http://localhost:8080/logout

And then login again with new user

## Create loan via Postman

**This API can be used by a logged in, authenticated user to request for a loan.**

http://localhost:8080/loans/create

Request: -

{
"amount": 10000,
"term": 2
}

Response: -

{
"loanRequest": {
"amount": 10000,
"term": 2
},
"loan": {
"loanId": 7,
"amount": 10000,
"tenure": 2,
"loaner": "rajeev",
"status": "PENDING",
"scheduledPayments": [
{
"loanId": 7,
"paymentAmount": 5000,
"paymentDueDate": "2024-08-19",
"paymentStatus": "PENDING",
"paidOnDate": null
},
{
"loanId": 7,
"paymentAmount": 5000,
"paymentDueDate": "2024-08-26",
"paymentStatus": "PENDING",
"paidOnDate": null
}
],
"residualAmount": 0,
"paidOnDate": 0
}
}

## View loan via Postman

**By hitting this API, a user can get a list of loans that are tagged to his/her account.**

http://localhost:8080/loans/view

This will only show loans of the logged in user 

## View pending loans via Postman

Only admin user can view these loans

http://localhost:8080/loans/pending

If a regular user will hit this api, then nothing will be returned by the server

## Approve pending loans

http://localhost:8080/loans/approve/7

Admin can approve loans via this api

## Repay loans 

http://localhost:8080/loans/repay/1/3400

A user can repay his/her loans via this API

## Tests

![img_1.png](img_1.png)