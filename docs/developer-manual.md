# Developer Manual

## 1. Starting the application

To start the application, execute this command:

```bash
./gradlew clean bootRun
```

## 2. Send files to the /documents endpoint
You can either use Postman or copy this code snippet here
```bash
curl -i --location --request POST 'http://localhost:8080/documents' \
--header 'Content-Type: application/xml' \
--data-raw '<?xml version="1.0" encoding="UTF-8"?>
<items>
    <news-item id="0A01-5215-5B25-124E">
        <date>2014-03-14</date>
        <title>US ends BP contract ban2</title>
        <source>Digital Journal</source>
        <author></author>
        <text><![CDATA[ bp ]]></text>
    </news-item>
    <news-item id="0A01-5215-5B25-124E">
        <date>2014-03-14</date>
        <title>US ends BP contract ban2</title>
        <source>Digital Journal</source>
        <author></author>
        <text> bp </text>
    </news-item>
</items>'
```
## 3. Fetch Documents by companyId
```bash
curl -i --location --request GET 'http://localhost:8080/documents?companyIds=2&companyIds=26' \
--header 'Content-Type: application/x-www-form-urlencoded'
```

## 4. Executing the test suite
To execute the test suite, type this command: 
```bash 
./gradlew cleanTest test
```

