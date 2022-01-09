# Vastumbot

## Server
The data directory consists of a list of subdirectories, each containing the extracted snaphost of the trash (image.png) and the additional information (info.json). The name of each subdirectory represents the id of the object.

To launch the backend.py script on port 8008:
```py backend.py```

## GET Requests
To access all the information about all objects: ```http://localhost:8008```

To access all the information about one object: ```http://localhost:8008/<id>```

To access the specific information about one object (path, lat, lon, timestamp, type, status, id_user): ```http://localhost:8008/<id>/<info>```

To access the picture of one object: ```http://localhost:8008/<id>/image.png```

## POST Requests
To add a new object: ```curl --max-time 5 --data "<JSON>" --header "Content-Type: application/json" http://localhost:8008```

## PUT Requests
To update the status of an object: ```curl -X PUT --max-time 5 --data "{\"status\":\"<status>\"}" --header "Content-Type: application/json" http://localhost:8008/<id>```
