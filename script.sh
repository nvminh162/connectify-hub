# neo4j docker
# https://docs.spring.io/spring-data/neo4j/reference/getting-started.html
# Authentication in Neo4j (default username & password: neo4j) => require replace password on first login http://localhost:7474/browser/
docker pull neo4j:latest
docker run --name neo4j --publish=7474:7474 --publish=7687:7687 -d neo4j:latest

# mysql docker
docker pull mysql:8.0.44-debian
docker run --name mysql -p 3306:3306 -v mysql_data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.44-debian

# mongodb docker
# https://hub.docker.com/r/bitnami/mongodb/tags
docker pull bitnami/mongodb:latest
docker run -d --name mongodb-7.0.11 -p 27017:27017 -e MONGODB_ROOT_USER=root -e MONGODB_ROOT_PASSWORD=root bitnami/mongodb:latest