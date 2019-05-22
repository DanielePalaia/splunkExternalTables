## Summary
This software is intended to show how it is possible to integrate Splunk with Greenplum database. It exercises splunk java api 
to make searches or to take all logs stored in Splunk and ingest a Greenplum table in order to do some other searches.</br>
The software is using external web table in order for Greenplum to query Splunk data. </br>
This software continues the experiment done here: </br>
https://github.com/DanielePalaia/gpss-splunk </br>
A java script is created, this script is connecting to splunk and printing in consolle splunk logs. The idea is that every host search for a different date range in order to work in parallel.
Then, the script can be embedded in a external web table definition.

## Prerequisites:
### 1. Bring some data to Splunk: 
I used a DBMS to bring some data into Splunk. I used DBConnect to bring some Greenplum/Postgresql into Splunk. Here you can follow the istructions: </br></br>
https://subscription.packtpub.com/book/big_data_and_business_intelligence/9781788835237/1/ch01lvl1sec18/getting-data-from-databases-using-db-connect</br></br>
The data I put into Splunk are of this type: </br></br>
dashboard=# select * from services limit 5; </br></br>
  id  | version | environment  |     service_level      | company_id | top_service |             name  </br>            
------+---------+--------------+------------------------+------------+-------------+------------------------------ </br>
 2052 |       0 | Production   | DC - Basic             |          7 | NO          | IE-PCAT PRODUCTION-PROD </br>
 2053 |       0 | Unknown-null | DC - Mission Critical  |         45 | NO          | ARCOR SERVERHOUSING </br>
 2054 |       0 | Production   | DC - Business Standard |         37 | NO          | DE-NSS ATOLL-BE DB SYBASE </br>
 2055 |       0 | Unknown-null | DC - Business Standard |         49 | NO          | VFUK INFRASTRUCTURE SECURITY </br>
 2056 |       0 | Unknown-null | DC - Business Standard |         42 | NO          | SHARED KPI MEASURING </br>
</br></br>
Once loaded into splunk it will start to generate events and you will start to see logs like this:
![Screenshot](./images/image1.png)

### 2. Create a Greenplum external web table:
Taking in account all informations contained in a splunk log, create an external web table like this:</br>

CREATE EXTERNAL WEB TABLE log_output
    (_bkt text, _cd text, serial text, id text, version text, environment text, service_level text, comapany_id text, top_service text, name text, splunk_server text, index text, source text, indextime text, subsecond text, linecount text, si text, hostname text, ip text, source_type text, sourceType text, time text)
    EXECUTE '/home/gpadmin/splunk_data.sh' ON HOST
    FORMAT 'CSV';
    </br>
For semplicity I put all fields as text but you can put the data type you want accodingly to the info received
</br>

    
### 3. The software is written in Java so you need a JVM installed as well as Splunk
Java needs to be installed on every host of the Greenplum distributed system </br> 
In every segment host, you need also to create a .splunkrc  in your home directory specifying connection parameters like: </br>  

host=localhost 
#Splunk admin port (default: 8089) </br> 
port=8089   </br> 
#Splunk username   
username=daniele   
#Splunk password   
password=XXXXXX   
#Access scheme (default: https)   
scheme=https  
#Splunk version number   
version=7.2.6   
 </br>
 
## Running the software:
### 1. Configuration file: </br>  
In every segment host you need to specify a segment.properties file where you specify this parameters: </br>
**earliest:2019-05-21T00:00:00**
**latest:2019-05-21T23:59:00**

The idea is to specify different time range in different hosts.

### 2. Run the jar </br>
Copy the .jar /target/splunk-0.0.1-SNAPSHOT.jar in /home/gpadmin </br>
Create a /home/gpadmin/splunk_data.sh where you simply call the .jar</br>

java -jar /home/gpadmin/splunk-0.0.1-SNAPSHOT.jar "search * | head 100"</br>
In this case will take just the first 100 elements but you can specify more


### 5. Have a look to the external table specified </br> 
Just do a SELECT * FROM log_output;
Then the /home/gpadmin/splunk_data.sh which is connecting to splunk and display csv lines will be invoked in order to see in a structured way:</br>
dashboard=# select * from log_output limit 5; </br>

                    _bkt                     |    _cd     | serial |           id            |  version  | environment |      service_level      |              comapany_id              |  top_service   |      na
me       |              splunk_server              |        index         | source | indextime | subsecond  | linecount | si |         hostname         |    ip     | source_type | sourcetype |             time  
            
---------------------------------------------+------------+--------+-------------------------+-----------+-------------+-------------------------+---------------------------------------+----------------+--------
---------+-----------------------------------------+----------------------+--------+-----------+------------+-----------+----+--------------------------+-----------+-------------+------------+-------------------
------------
 main~4~3E1FBD62-AB28-4A3C-93D0-5D509AC308D9 | 4:14430163 | 0      | 2019-05-22 10:24:13.183 |  id=10427 |  version=0  |  environment=Production |  service_level=DC - Business Premium  |  company_id=24 |  top_se
rvice=NO |  name=GSOC-LOCAL SECURITY INCIDENT (PT) | Danieles-MBP.station | main   | dahsboard | 1558513453 | .183      | 1  | Danieles-MBP.stationmain | localhost | string      | string     | 2019-05-22T10:24:1
3.183+02:00
 main~4~3E1FBD62-AB28-4A3C-93D0-5D509AC308D9 | 4:14430156 | 1      | 2019-05-22 10:24:13.183 |  id=10426 |  version=0  |  environment=Production |  service_level=AO-Business Standard   |  company_id=2  |  top_se
rvice=NO |  name=M2M REPORTING SERVER-PROD         | Danieles-MBP.station | main   | dahsboard | 1558513453 | .183      | 1  | Danieles-MBP.stationmain | localhost | string      | string     | 2019-05-22T10:24:1
3.183+02:00
 main~4~3E1FBD62-AB28-4A3C-93D0-5D509AC308D9 | 4:14430149 | 2      | 2019-05-22 10:24:13.183 |  id=10425 |  version=0  |  environment=Production |  service_level=DC - Business Premium  |  company_id=26 |  top_se
rvice=NO |  name=GSOC-LOCAL SECURITY INCIDENT (CZ) | Danieles-MBP.station | main   | dahsboard | 1558513453 | .183      | 1  | Danieles-MBP.stationmain | localhost | string      | string     | 2019-05-22T10:24:1
3.183+02:00
 main~4~3E1FBD62-AB28-4A3C-93D0-5D509AC308D9 | 4:14430143 | 3      | 2019-05-22 10:24:13.183 |  id=10424 |  version=0  |  environment=Production |  service_level=Unknown                |  company_id=19 |  top_se
rvice=NO |  name=VOCH_CALLMGMTSW                   | Danieles-MBP.station | main   | dahsboard | 1558513453 | .183      | 1  | Danieles-MBP.stationmain | localhost | string      | string     | 2019-05-22T10:24:1
3.183+02:00
 main~4~3E1FBD62-AB28-4A3C-93D0-5D509AC308D9 | 4:14430136 | 4      | 2019-05-22 10:24:13.183 |  id=10423 |  version=0  |  environment=Production |  service_level=DC - Business Standard |  company_id=24 |  top_se
rvice=NO |  name=VFPT-TOL - VIRTUAL MACHINE        | Danieles-MBP.station | main   | dahsboard | 1558513453 | .183      | 1  | Danieles-MBP.stationmain | localhost | string      | string     | 2019-05-22T10:24:1
3.183+02:00

### 6. Limitations: </br> 
Currently just one segment per host will work. Not fully tested on multiple segments.

## Compiling the software:

**If you wish to compile the software you can just mvn install on the root directory** </br>
**Jar file will be produced inside /target**
