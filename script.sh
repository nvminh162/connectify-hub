# neo4j docker
# https://docs.spring.io/spring-data/neo4j/reference/getting-started.html
# Authentication in Neo4j (default username & password: neo4j) => require replace password on first login http://localhost:7474/browser/
docker pull neo4j:latest
docker run --name neo4j --publish=7474:7474 --publish=7687:7687 -d neo4j:latest

# mysql docker
docker pull mysql:8.0.44-debian
docker run --name mysql -p 3306:3306 -v mysql_data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.44-debian
# tool browser:
docker pull dbeaver/cloudbeaver:latest
docker run --name cloudbeaver -p 8978:8978 dbeaver/cloudbeaver:latest



