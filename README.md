# NuCredit

## How to Run

You can run this application using REPL, in this case I'm using Leiningen REPL.
To run the application, go to the folder containing **web.clj** and run:

```
lein repl
```

Inside repl, to start the server, run:

```
> (require 'nucredit.web)
> (def server (nucredit.web/-main))
```

The default port that the application will be listening is **5000**, if you want to change this port to another just pass the new port value to the **-main** function:

```
> (def server (nucredit.web/-main <port>))
```

If you dont want to run locally, the application is hosted on Heroku on ***https://nucredit.herokuapp.com***.

## Endpoints

In order to call a endpoint you just need to send the correct http request (GET or PUT) containing a JSON with the required parameters.

One option to do this is using CURL. In order to send a http request via CURL to an application running on ***localhost*** port ***5000***, use the following command:

```
curl -X <GET/PUT> -H "Content-Type: application/json" \
-d '<JSON FILE>' \
http://localhost:5000/<DESIRED OPERATION>
```

### create-account [name]

This endpoint receives a string and creates a new account with the ***name*** argument. If the account creation was successful this will return the ***account-number*** of the new account.
 
 Example:
 ```
 curl -X POST -H "Content-Type: application/json" \
 -d '{"name": "foo"}' \
 http://localhost:5000/create-account
 ```
 
 Should return:
 ```
 {
    "account-number": 1, 
    "name": "foo"
 }
 ```

### operate [party, (optional) counter-party, amount, (optional) offset]

Create a operation between a ***party*** and a ***counter-party***, if ***counter-party*** is null than it is assumed that this is a self-target operation, like an deposit or withdraw. ***party*** and ***counter-party*** on this scope are the account numbers and/or the name of the target institution.

***amount*** is the value to be operated and ***offset*** is how many days from the date that the endpoint was called should the operation be evaluated. For an instance, passing 2 as ***offset*** will cause an operation to be evaluated on D+2. A null offset will just cause the operation to be evaluated on D0.

If the operation was done correctly this should return a JSON containing the account name and the current balance of the account. 

Example:
```
 curl -X POST -H "Content-Type: application/json" \
 -d '{"party": 1, "counter-party": "Uber", "amount": -100, "offset": 5}' \
 http://localhost:5000/operate
 ```
 
 This corresponds as a ***$100,00*** purchase on ***Uber*** from the account with number ***1*** to be evaluated at ***D+5***
 
 Should return:
 ```
 {
    "name": "foo", 
    "balance": -100
 }
 ```
 
 ### get-balance [account-number]
 
 Receives an ***account-number*** and return its current balance.
 
 Example:
 ```
 curl -X GET -H "Content-Type: application/json" \
  http://localhost:5000/get-balance/1
 ```
 
 Should return:
 ```
 {
     "name": "Bruno",
     "balance": -100
 }
 ```
 
 ### get-statement [account-number]
 
 Return the list of operations of a given account grouped by date of occourence. Also contains the balance for that specific date.
 
 Example:
 ```
  curl -X GET -H "Content-Type: application/json" \
   http://localhost:5000/get-statement/1
  ```
  
  Should Return:
  
  ```
    {
        "2017-08-22":
            [
                {
                    "balance":-100
                },
                {
                    "party":1,
                    "counter-party":"Uber",
                    "amount":-100
                }
            ]
    }
  ```
 
### get-debt-periods [account-number]

Return the periods which a given account had negative balance. Returns a JSON containing a list of documents containing the ***principal*** (amount that the account is in debt), ***start*** (the date when debt started) and ***end*** (date when the debt was paid, contains null if the debt is still active).

Example:

```
  curl -X GET -H "Content-Type: application/json" \
   http://localhost:5000/get-debt-periods/1
  ```
  
Should return:
```
[
    {
        "principal":100,
        "start":"2017-08-22",
        "end":null
    }
]
```
 
# Tests

In order to run tests just run the ***run-tests.sh***. This will run both ledger and services unit test and the integrated tests too. Make sure port 5001 to 5005 are available in order to integrated tests to work.