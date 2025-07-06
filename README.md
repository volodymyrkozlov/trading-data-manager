# Trading data manager

## Introduction

This repository contains a high-performance RESTful service designed to meet the demanding requirements of high-frequency trading systems. 
The service consumes trading data and provides real-time statistical insights per symbol. 
It leverages an efficient data structure optimized for rapid data insertion and low-latency retrieval of analytical results. <br>

## System requirements

1. Java `24` must be installed on the machine where this application is running.
2. Memory requirements:
- The application uses `double` ring buffers to store trading prices, their prefix sums, and squared prefix sums. <br>
The default maximum value of `K` is `8` and each `double` occupies `8 bytes`, approximately `800 MB` is expected per buffer according to formula `1e8 * 8 bytes = 800 MB`. `800 MB * 3 = 2400 MB`. <br>
- The application uses `2` dequeues of indices to calculate max and min values. <br>
The default maximum value of `K` is `8`, `8 k values * 2 = 16` dequeues per symbol and each int occupies `4 bytes`, `0.5MB` is expected per symbol. <br>

Total per symbol is `2.4 GB`. <br>

By default `10` symbols are allowed, the expected memory allocation is `2.4GB * 10 = 24GB`

## Build

To build **trading-data-manager** service application, run the following command: <br>
`./gradlew build`<br>

*It will run unit tests and install*
dependencies.

*Note:*
Gradle properties already contain required configurations. If more memory is required run as `./gradlew bootRun -Dorg.gradle.jvmargs="-Xms4g -Xmx26g -XX:+HeapDumpOnOutOfMemoryError"`


## Run

Navigate to **trading-data-manager** directory and run the following command: <br>
` ./gradlew bootRun` <br>

*The application will start on port `8080`*

Application properties:
- max-symbols-allowed-amount - *An amount of symbols allowed for the application. Default `10`*
- max-k-value - *Max K value. Default `8`*
- max-batch-size - *Max batch size. Default `10000`*

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


