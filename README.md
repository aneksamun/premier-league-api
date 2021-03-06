# Premier League API

An API for determining teams standings within Premier League Table. 

## Endpoints


| Path         | Method | Response | Details                                 |
| ------------ | ------ | -------- | --------------------------------------- |
| /games       | POST   | 201/400  | Stores a football match to the database |
| /games/:week | GET    | 200      | Retrieves game results for given week   |
| /table       | GET    | 200      | Calculates team standings               |

To save a football match data must be present in following format:
```json
  {
    "gameWeek": 1,
    "homeTeam": "Arsenal",
    "awayTeam": "Leicester",
    "homeGoals": 2,
    "awayGoals": 1
  }
```
The game week suppose to be between 1 and 38.   

The game resuts are ordered alphabetically by home team. If not games have been POSTed for a specified game week, an empty array is returned.
```json
[
    {
        "homeTeam": "Arsenal",
        "awayTeam": "Leicester",
        "homeGoals": 2,
        "awayGoals": 1,
        "result": "Home Win"
    }
]
```   

The table is paged therefore __offset__ and __limit__ can be included in query string, for example, `localhost:9000\table?offset=1&limit=10`. Default values is 1 for offset and 20 for limit.   
The teams gets awarded with __3 points__ winning a game and with __1 point__ for draw.  
```json
{
    "offset": 1,
    "limit": 2,
    "total": 2,
    "items": [
        {
            "position": 1,
            "team": "Arsenal",
            "gamesPlayed": 1,
            "gamesWon": 1,
            "gamesDrawn": 0,
            "gamesLost": 0,
            "goalsFor": 2,
            "goalsAgainst": 1,
            "goalDifference": 1,
            "points": 3
        },
        {
            "position": 2,
            "team": "Leicester",
            "gamesPlayed": 1,
            "gamesWon": 0,
            "gamesDrawn": 0,
            "gamesLost": 1,
            "goalsFor": 1,
            "goalsAgainst": 2,
            "goalDifference": -1,
            "points": 0
        }
    ]
}
```

## Build instructions

- Compile
```
sbt compile
```
- Build deployable binary
```
sbt dist
```
- Build a Docker image
```
sbt docker:publishLocal
```
- Run application and dependencies
```
docker-compose up -d
```

## Web interface
<sup>Premier League Table</sup>
![table](doc/images/table.png?raw=true "Premier League Table")
<sup>No content page</sup> 
![nothing](doc/images/nothing.png?raw=true "Nothing to show")
<sup>Error page</sup>
![error](doc/images/error.png?raw=true "Error page")
