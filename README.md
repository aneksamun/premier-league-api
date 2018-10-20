# Premier League API

An API for determining teams standings within Premier League Table. 

## Endpoints


| Path         | Method | Response | Details                                 |
| ------------ | ------ | -------- | --------------------------------------- |
| /games       | POST   | 201/400  | Stores a football match to the database |
| /games/:week | GET    | 200      | Retrieves game resuts for given week    |
| /table       | GET    | 200      | Calculates team standings               |

![table](doc/images/table.png?raw=true "Premier League Table")
![nothing](doc/images/nothing.png?raw=true "Nothing to show")
![error](doc/images/error.png?raw=true "Error page")
