# Usa una imagen base de Java 21 (Eclipse Temurin, una distribución popular y estable)
FROM eclipse-temurin:21-jdk-jammy

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el wrapper de Maven y el archivo pom.xml
# Esto permite a Docker cachear las dependencias si no cambian
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
COPY pom.xml .

# Descarga las dependencias del proyecto
RUN ./mvnw dependency:go-offline -B

# Copia el resto del código fuente de tu aplicación
COPY src ./src

# Compila la aplicación y la empaqueta en un archivo .jar
# Se saltean los tests para un despliegue más rápido
RUN ./mvnw package -DskipTests

# Expone el puerto en el que correrá la aplicación (Railway usa la variable PORT)
EXPOSE ${PORT:-8081}

# Comando para ejecutar la aplicación cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "target/flowfit-0.0.1-SNAPSHOT.jar"]
