# SENG302 Team 500

Project split into:
- API backend using Play and Java
- Front end using Vue.js

## Project Structure for Back End
* `backend/app/` The source for the API backend
* `backend/doc/` User and design documentation for the whole project
* `backend/doc/examples/` Demo example files for use with the backend
* `backend/conf/` configuration files required to ensure the backend builds properly

## Project Structure for Front End
`// TODO: Raf to complete this` 

## How to run the back end
```bash
cd backend
sbt run
```
And open <http://localhost:9000/>

## How to run the front end
```bash
cd frontend
npm install # only initially to install dependencies
npm run serve # starts the development server (with hot reload)
```
And open <http://localhost:8080/>

### Reference
* [Play Documentation](https://playframework.com/documentation/latest/Home)
* [EBean](https://www.playframework.com/documentation/latest/JavaEbean) is a Java ORM library that uses SQL.The documentation can be found [here](https://ebean-orm.github.io/)
* [Vue.js Documentation](https://vuejs.org/v2/guide/)

