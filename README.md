# simple-gl
Simple general ledger application.

## Run instructions

1.  Run `./gradlew build` command
2.  After successful build go to the `build/libs` directory 
3.  Then run app using following command `java -jar simple-gl-0.0.1-all.jar`

## Two modes

Application logic implemented twice within two similar approaches.

The first approach kinda `actor` based.
Programm state and all communications are handled by actors.

The second one is `eventstorming`.
I have used `axon` framework to get beautiful `DDD` and `CQRS` out of the box.

The `actor` approach is active by default but you can switch it using ``MODE`` environment variable.
Just set it to `axon` and run application.

## Project structure

Project code structured by following packages.

1.  ``actor`` - Kotlin actor based approach
2.  ``axon`` - Axon based approach
3.  ``core`` - API that implemented in packages above.
4.  ``rest`` - REST API facade
5.  ``utils`` - Utility functions

## Public API

Examples of all queries could be found in ``resources`` folder.

### account

 `POST /account` - Will create new account.
 
 `GET /account/{id}` - Will return account balance.
 
 `PUT /account/{id}/deposit` - Will deposit money to account.
 
 `PUT /account/{id}/withdraw` - Will withdraw money from account.
 
### transfer
 
 `POST /transfer` - Will create transfer with specified `{amount}` of money from `{from}` account id to `{to}` account id.
 
 `POST /transfer/{id}` - Will return transfer status.