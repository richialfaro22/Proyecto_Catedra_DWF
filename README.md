# Proyecto_Catedra_DWF — Sistema de reservas de hotel

Sistema de administración y reserva de habitaciones para un hotel o cadena hotelera.

| Parte | Tecnología | Carpeta |
|---|---|---|
| API REST (backend) | Java 21 · Spring Boot 4.1 · Spring Security + JWT · Spring Data JPA · MariaDB/MySQL (phpMyAdmin) · OpenAPI/Swagger | [`hotel/`](hotel) |
| Frontend | Vue 3 · Vite 7 · Tailwind CSS 4 · Vue Router | [`frontend/`](frontend) |

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 (Temurin, Oracle u OpenJDK) |
| IntelliJ IDEA | Community 2024.3 o superior (trae Maven integrado) |
| XAMPP (MariaDB + phpMyAdmin) | 8.x o superior (también sirve MySQL 8) |
| Node.js | 20.19+ o 22 LTS (incluye npm) |

> En el panel de XAMPP inicia **Apache** (para phpMyAdmin) y **MySQL** (MariaDB, puerto **3306**). La configuración por defecto usa el usuario `root` sin contraseña.

---

## 2. Base de datos

1. Abrir **phpMyAdmin** (`http://localhost/phpmyadmin`).
2. Ir a la pestaña **Importar** (o **SQL**) y ejecutar [`hotel/Base Datos/Query_base_hotel.sql`](hotel/Base%20Datos/Query_base_hotel.sql): crea la base `hotel_db` con todas las tablas del **modelo ER de la Fase 1** (`Usuario`, `Empleado`, `Hotel`, `Habitacion`, `Reservas`, `Detalle_Reserva`, `Pagos`…) más las tablas agregadas en esta fase. Se puede ejecutar más de una vez.
3. Al iniciar la API por primera vez se cargan los usuarios de prueba y los datos de ejemplo (hoteles, habitaciones, servicios, reservas). Si alguna tabla faltara, Hibernate la crea (`ddl-auto=update`).
4. Para empezar de cero ejecutar [`hotel/Base Datos/Reiniciar_tablas.sql`](hotel/Base%20Datos/Reiniciar_tablas.sql) y luego otra vez `Query_base_hotel.sql`.

La conexión se configura en `hotel/src/main/resources/application.properties` (por defecto `jdbc:mariadb://localhost:3306/hotel_db`, usuario `root`, sin contraseña). Si tu `root` tiene contraseña, escríbela en `spring.datasource.password`. Para MySQL 8 hay una URL alternativa comentada en el mismo archivo.

---

## 3. Abrir y ejecutar en IntelliJ IDEA Community

### Backend
1. **File → Open…** y seleccionar la carpeta raíz del repositorio (`Proyecto_Catedra_DWF`).
2. Si aparece el aviso *"Maven build scripts found"* pulsar **Load Maven Project**. Si no aparece: clic derecho sobre `hotel/pom.xml` → **Add as Maven Project**.
3. **File → Project Structure → Project → SDK**: seleccionar un JDK 21 (si no hay, *Add SDK → Download JDK… → versión 21*).
4. Activar Lombok: **Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing** (IntelliJ suele pedirlo automáticamente).
5. Ejecutar la configuración **Backend - HotelApplication** (ya incluida en `.run/`) o abrir `hotel/src/main/java/com/udb/hotel/HotelApplication.java` y pulsar ▶.
6. La API queda en **http://localhost:8080** y la documentación en **http://localhost:8080/swagger-ui.html**.

### Frontend
IntelliJ Community no tiene soporte para Vue/npm, pero se puede usar su terminal integrada (**View → Tool Windows → Terminal**):

```bash
cd frontend
npm install      # solo la primera vez
npm run dev
```

Abrir **http://localhost:5173**. Vite redirige las peticiones `/api/*` a `http://localhost:8080`, así que la API debe estar en ejecución.

> Para editar los archivos `.vue` con resaltado se puede usar VS Code, o bien IntelliJ como editor de texto.

Para generar la versión de producción: `npm run build` (crea `frontend/dist`).

---

## 4. Usuarios de prueba

| Usuario | Contraseña | Rol | Puede… |
|---|---|---|---|
| `admin` | `Admin123*` | ADMIN (Administrador) | Todo: hoteles, empleados, usuarios, catálogos, reservas, pagos, reclamos, estadísticas |
| `recepcion` | `Recep123*` | RECEPCIONISTA | Reservar para huéspedes, cobrar (incluye efectivo), check-in/out, huéspedes, reclamos, estadísticas |
| `huesped` | `Huesped123*` | HUESPED (Huésped) | Buscar, reservar, pagar, modificar/cancelar sus reservas, enviar reclamos |

Se puede iniciar sesión con el **alias o con el correo** (ej. `nelly.perez@correo.com`). También se puede crear una cuenta de huésped desde **Regístrate**.

**Tarjetas de prueba** (pago simulado, validación con algoritmo de Luhn):
`4111 1111 1111 1111` → aprobada · `4000 0000 0000 0002` → rechazada · vencimiento futuro (ej. `12/30`) · CVV `123`.

---

## 5. Proceso de reserva implementado

1. **Búsqueda**: ciudad, fecha de entrada/salida, número de huéspedes y tipo (opcional).
2. **Opciones disponibles**: hotel, tipo, capacidad, servicios incluidos, precio por noche, disponibilidad y precio total (con tarifas de temporada).
3. **Datos del huésped**: nombre completo, nacimiento, documento, teléfono, correo, solicitudes especiales y servicios adicionales.
4. **Verificación y resumen**: se vuelve a verificar la disponibilidad (con bloqueo de la habitación) y se genera la reserva (`Reservas` + `Detalle_Reserva`) con código.
5. **Confirmación y pago**: tarjeta, transferencia o efectivo; se registran en `Pagos` el método, monto, fecha y estado del pago, y se actualiza el estado de la reserva.
6. **Modificación / cancelación**: según la política del hotel (horas de cancelación gratuita y % de penalización) con cálculo automático de cargos y reembolsos.

Más detalle técnico (paquetes, endpoints, modelo de datos, dependencias) en [`hotel/README.md`](hotel/README.md).
