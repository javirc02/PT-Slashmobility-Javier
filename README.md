# Prueba Técnica – Sistema de Productos con Precios Históricos

Este proyecto es la solución a la prueba técnica alojada en el repositorio https://github.com/adriancataland/senior-java-tech-challenge.git, consta de una **API REST** desarrollada con **Spring Boot**, enfocada en la gestión de productos y sus precios asociados. Incluye operaciones de CRUD para productos y precios, con validaciones de negocio, y documentación automática mediante **Springdoc OpenAPI**.

---
## Requisitos previos

- **Java 21** instalado y configurado en el PATH.
- **Gradle** instalado para compilar y ejecutar el proyecto.
- **PostgreSQL** en ejecución con la base de datos `productsdb` creada.
- Usuario de PostgreSQL con permisos de lectura/escritura (configurado en `application.yml`).
---
## Instrucciones para compilar y ejecutar

### 1. Clona el repositorio:

```bash
git clone https://github.com/javirc02/senior-java-tech-challenge-mango
cd senior-java-tech-challenge-mango
```
### 2. Compila y ejecuta el proyecto usando Gradle:
```bash
./gradlew build
./gradlew bootRun
```
### 3. La API estará disponible en:
```bash
http://localhost:8080
```
### 4. La documentación de la api se puede consultar en:
```bash
http://localhost:8080/swagger-ui.html
```
### 5. Los tests se ejecutan con:
```bash
./gradlew test
```
Se puede ver el resultado de los tests abriendo en un navegador el archivo generado en:
```
products/build/reports/tests/test/packages/mango.challenge.products.service.html
```
---

## Tecnologías y justificación técnica

### Java 21
- Última versión LTS, lo que nos asegura mantener la aplicación con una versión de java con soporte el máximo tiempo posible.

### Spring Framework 3.5.5 + Spring Boot
- Spring Boot simplifica la configuración, arranque y despliegue, permitiendo centrarse en la lógica de negocio.

### Spring Web / Spring Data JPA / Hibernate
  - **Spring Web:** Permite exponer la lógica de negocio a través de endpoints REST de manera sencilla y estandarizada.  
  - **Spring Data JPA / Hibernate:** Facilitan la persistencia de datos, manejo de transacciones y consultas complejas de manera declarativa, reduciendo boilerplate y aumentando la mantenibilidad del código.

### PostgreSQL
- Base de datos relacional confiable, con soporte a transacciones, índices avanzados y escalabilidad.  
- Ideal para este proyecto para mantener la integridad de los datos y contar con un buen rendimiento rendimiento.

### Gradle
- Sistema de construcción flexible y rápido, que facilita la gestión de dependencias y tareas de construcción.  

### Arquitectura MVC con Service y Repository
- **Modelo:** Representa entidades de negocio (Product, Price).  
- **Vista:** Aunque no se usa frontend directo, la capa REST actúa como interfaz.  
- **Controlador:** Gestiona solicitudes HTTP y respuestas.  
- **Service:** Contiene la lógica de negocio, validaciones y reglas de negocio.  
- **Repository:** Encapsula la persistencia de datos y comunicación con la base de datos.
  
**Beneficios:** Código más limpio, testable, mantenible y fácil de escalar. Ideal para este servicio de poca embergadura ya que otro tipo de arquitecturas habrían complicado innecesariamente la implementación.

### Springdoc OpenAPI 2.8.13
- Genera documentación automática de la API, compatible con Spring Boot 3.5.5 y evitando errores de incompatibilidad.

### Flyway
- Muy útil para gestionar la evolución de la base de datos de forma automática y controlada. Permite versionar la estructura y los datos iniciales mediante scripts numerados, garantizando que todos los entornos (desarrollo, pruebas y producción) tengan la misma configuración.

### Decisiones de diseño de la API
- Validaciones de negocio robustas (fechas, solapamientos, existencia de entidad).  
- Manejo de errores mediante excepciones con mensajes claros para facilitar depuración.
- Los endpoints de consulta de productos y precios incluyen los IDs correspondientes, permitiendo al usuario de la API identificar cada recurso y utilizarlos para llamar a otros endpoints relacionados.
- Se utilizó **PATCH** para actualizar precios parcialmente sin sobrescribir otros campos.  

### Mejoras y supuestos
- Cada precio pertenece a un único producto.
- Manejo de errores centralizado y claro.
- Carga de ejemplos en la base de datos para simplificar las pruebas.
- Configuración lista para PostgreSQL con posibilidad de cambiar base de datos fácilmente.







