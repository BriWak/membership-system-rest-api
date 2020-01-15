# First Catering Ltd RESTful API for use by Bows Formula One

## Information

This is a Scala web application built using SBT and Play Framework. It is built to handle Json API calls to a Mongo database using Reactive Mongo.

To run this project you will require an instance of Mongo running and the project can be run locally on port 9000 using "sbt run" at the command line.

You can test the functionality using an API Client such as Postman.

To call the Endpoints below, please call them using http://localhost:9000 followed by the relevant Endpoint.

## Routes

|User Action|Request Type|Description|Endpoint|
|------|------|-----------|--------|
|Present Card|GET|Starts session and welcomes the user/deletes session and says Goodbye to the user or returns an error message (takes an optional pin parameter, i.e. /present-card/{CARD_ID}?pin={PIN})|/present-card/:card|
|Find member|GET|Retrieves a members information or returns an error message|/find-member/:card|
|Register a member (with Json body)|POST|Registers a member (with the Json data provided in the body of the request) or returns an error message|/register-member|
|Remove a member|POST|Deletes a member from database or returns an error message|/remove-member/:card|
|Check funds|GET|Returns the total funds for a member or returns an error message|/check-funds:card|
|Add funds|POST|Increases the total funds for a member or returns an error message|/add-funds/:card/:funds|
|Purchase Goods|POST|Decreases the total funds for a member or returns an error message|/purchase/:card/:cost|
|Update name|POST|Updates a members name or returns an error message|/update/name/:card/:newName|
|Update mobile number|POST|Updates a members mobile number or returns an error message|/update/mobile/:card/:newNumber|

