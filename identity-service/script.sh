# Cách 1: Sử dụng Maven đã cài đặt sẵn trên hệ thống
# maven thực hiện các step để build project (skip unit test)
./mvnw package -DskipTests

# run
#D:\Workspace\course\project\connectify-hub\identity-service\target>
java -jar identity-service-0.0.1-SNAPSHOT.jar

# clean
./mvnw clean



# Cách 2:
mvn package -DskipTests



# format spotless
mvn spotless:apply