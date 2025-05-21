# Usa la imagen base de OpenJDK 23
FROM openjdk:23-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR generado al contenedor
COPY target/SlushyApp-0.0.1-SNAPSHOT.jar /app/slushyapp.jar

# Expón el puerto en el que la aplicación se ejecutará
EXPOSE 8080

# Comando para ejecutar la aplicación Spring Boot
CMD ["java", "-jar", "slushyapp.jar"]
