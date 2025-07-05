# Trading data manager

## Introduction

This repository contains a high-performance RESTful service designed to meet the demanding requirements of high-frequency trading systems. 
The service consumes trading data and provides real-time statistical insights per symbol. 
It leverages an efficient data structure optimized for rapid data insertion and low-latency retrieval of analytical results. <br>

## Build

To build **trading-data-manager** service application, run the following command. <br>
`./gradlew build`<br>

*It will run unit tests and install*
dependencies.

## Run

Navigate to **trading-data-manager** directory and run the following command: <br>
` ./gradlew bootRun` <br>

*The application will start on port `8080`*

## Usage

Trading data manager contains 2 endpoints

1. *Add symbol data* <br>

`POST /add_batch` <br>

Example: 
```
{
    "symbol": "UAH",
    "values": [97.26,36.78,28.01,45.12,3.57,79.49,71.65,25.93,87.76,93.41,
               68.14,90.03,58.69,43.25,32.17,60.04,89.33,6.98,16.22,55.37]
}
```
Response: 
```
Status: 200 OK
```

2. *Get symbol stats* <br>

`GET /stats/{symbol}/{k}` <br>

`symbol`: a financial instrument's identifier e.g `PLN`, `UAH`, `EUR` <br>
`k`:  an integer from `1` to `8`, specifying the number of last `1e{k}` data points to analyze <br>

Response:
```
Status: 200 OK
Body: {
    "min": 0.27,
    "max": 97.26,
    "last": 61.4,
    "avg": 46.46199999999998,
    "var": 815.9876520000025
}
```

## Tests
The repository includes test cases in `src/test/resources` for verifying each type of calculation: average, last, maximum, minimum, and variance.
These files can be modified to test custom input values if needed.


