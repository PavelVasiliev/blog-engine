## Table of contents
* [General info](#general-info)
* [Dependencies](#dependencies)
* [Setup](#setup)

## General info
This is Blog project with the following features: 
* registering/initiating/updating Users with different roles;
* Blog settings for prohibiting registration, for displaying and creating posts;
* full set of CRUD methods for Posts, sorting;
* post drafts and the possibility of their delayed launch;
* adding comments, voting to posts

## Dependencies
Created with:
* Maven
* Spring (Boot, Web, Security)
* MySQL
* Hibernate
* Heroku
* Lombok
* Liquibase
* Log4j
* Jinq
* Cloudinary
* Cage (Captcha Generator)

## Setup
Set variables:
* JAWSDB_URL with mysql user, password, host, port, database name or use local base;
* CLOUDINARY_URL with registered Cloudinary API Environment variable. (cloudinary://{API Key}:{API Secret}@{Cloud name})
```
$ mvn install
$ cd target
$ java -jar blog-plov-1.0.jar
```