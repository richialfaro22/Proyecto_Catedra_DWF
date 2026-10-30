
CREATE DATABASE IF NOT EXISTS hotel_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hotel_db;


CREATE TABLE IF NOT EXISTS Estado_Empleado (
    id_estadoempleado INT NOT NULL AUTO_INCREMENT,
    tipo_estadoempleado VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_estadoempleado),
    UNIQUE KEY uk_estado_empleado_tipo_estadoempleado (tipo_estadoempleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Empleado (
    id_empleado INT NOT NULL AUTO_INCREMENT,
    nombre_empleado VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NULL,
    telefono VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    tipo_empleado VARCHAR(50) NOT NULL,
    id_estadoempleado INT NOT NULL,
    PRIMARY KEY (id_empleado),
    UNIQUE KEY uk_empleado_email (email),
    CONSTRAINT fk_empleado_id_estadoempleado FOREIGN KEY (id_estadoempleado) REFERENCES Estado_Empleado (id_estadoempleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Estado_Hotel (
    id_estadohotel INT NOT NULL AUTO_INCREMENT,
    estado_hotel VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_estadohotel),
    UNIQUE KEY uk_estado_hotel_estado_hotel (estado_hotel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Huesped (
    id_huesped INT NOT NULL AUTO_INCREMENT,
    nombre_huesped VARCHAR(150) NOT NULL,
    email VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    telefono VARCHAR(20) NULL,
    documento VARCHAR(30) NOT NULL,
    fecha_registro DATETIME(6) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    tipo_documento VARCHAR(15) NOT NULL,
    nacionalidad VARCHAR(50) NULL,
    PRIMARY KEY (id_huesped),
    UNIQUE KEY uk_huesped_documento (documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    alias VARCHAR(50) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL,
    id_empleado INT NULL,
    id_huesped INT NULL,
    activo BIT(1) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_alias (alias),
    UNIQUE KEY uk_usuario_email (email),
    CONSTRAINT fk_usuario_id_empleado FOREIGN KEY (id_empleado) REFERENCES Empleado (id_empleado),
    CONSTRAINT fk_usuario_id_huesped FOREIGN KEY (id_huesped) REFERENCES Huesped (id_huesped)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Hotel (
    id_hotel INT NOT NULL AUTO_INCREMENT,
    nombre_hotel VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    descripcion VARCHAR(1000) NULL,
    fecha_registro DATETIME(6) NOT NULL,
    id_empleado INT NULL,
    id_estadohotel INT NOT NULL,
    email VARCHAR(100) NULL,
    estrellas INT NOT NULL,
    horas_cancelacion_gratis INT NOT NULL,
    porcentaje_penalizacion DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_hotel),
    UNIQUE KEY uk_hotel_nombre_hotel (nombre_hotel),
    CONSTRAINT fk_hotel_id_empleado FOREIGN KEY (id_empleado) REFERENCES Empleado (id_empleado),
    CONSTRAINT fk_hotel_id_estadohotel FOREIGN KEY (id_estadohotel) REFERENCES Estado_Hotel (id_estadohotel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Tipo_Habitacion (
    id_tipohabitacion INT NOT NULL AUTO_INCREMENT,
    tipohabitacion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(1000) NULL,
    precio DECIMAL(10,2) NOT NULL,
    cantidad_personas INT NOT NULL,
    servicios_incluidos VARCHAR(300) NULL,
    activo BIT(1) NOT NULL,
    PRIMARY KEY (id_tipohabitacion),
    UNIQUE KEY uk_tipo_habitacion_tipohabitacion (tipohabitacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Habitacion (
    id_habitacion INT NOT NULL AUTO_INCREMENT,
    numerohabitacion VARCHAR(10) NOT NULL,
    descripcion VARCHAR(1000) NULL,
    estado VARCHAR(30) NOT NULL,
    precio_base DECIMAL(10,2) NOT NULL,
    numpiso INT NOT NULL,
    id_hotel INT NOT NULL,
    id_tipohabitacion INT NOT NULL,
    PRIMARY KEY (id_habitacion),
    UNIQUE KEY uk_habitacion_hotel_numero (id_hotel, numerohabitacion),
    CONSTRAINT fk_habitacion_id_hotel FOREIGN KEY (id_hotel) REFERENCES Hotel (id_hotel),
    CONSTRAINT fk_habitacion_id_tipohabitacion FOREIGN KEY (id_tipohabitacion) REFERENCES Tipo_Habitacion (id_tipohabitacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Tarifa (
    id_tarifa INT NOT NULL AUTO_INCREMENT,
    nombre_tarifa VARCHAR(80) NOT NULL,
    id_hotel INT NOT NULL,
    id_tipohabitacion INT NOT NULL,
    precio_noche DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activa BIT(1) NOT NULL,
    PRIMARY KEY (id_tarifa),
    CONSTRAINT fk_tarifa_id_hotel FOREIGN KEY (id_hotel) REFERENCES Hotel (id_hotel),
    CONSTRAINT fk_tarifa_id_tipohabitacion FOREIGN KEY (id_tipohabitacion) REFERENCES Tipo_Habitacion (id_tipohabitacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Servicio_Adicional (
    id_servicio INT NOT NULL AUTO_INCREMENT,
    nombre_servicio VARCHAR(80) NOT NULL,
    descripcion VARCHAR(300) NULL,
    categoria VARCHAR(20) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    tipo_cobro VARCHAR(20) NOT NULL,
    activo BIT(1) NOT NULL,
    PRIMARY KEY (id_servicio),
    UNIQUE KEY uk_servicio_adicional_nombre_servicio (nombre_servicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Reservas (
    id_reserva INT NOT NULL AUTO_INCREMENT,
    fecha_reserva DATETIME(6) NOT NULL,
    fecha_entrada DATE NOT NULL,
    fecha_salida DATE NOT NULL,
    estadoreserva VARCHAR(30) NOT NULL,
    observaciones VARCHAR(1000) NULL,
    id_hotel INT NOT NULL,
    id_huesped INT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    id_usuario INT NOT NULL,
    subtotal_habitaciones DECIMAL(12,2) NOT NULL,
    subtotal_servicios DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    fecha_cancelacion DATETIME(6) NULL,
    motivo_cancelacion VARCHAR(300) NULL,
    cargo_cancelacion DECIMAL(12,2) NULL,
    PRIMARY KEY (id_reserva),
    CONSTRAINT fk_reservas_id_hotel FOREIGN KEY (id_hotel) REFERENCES Hotel (id_hotel),
    CONSTRAINT fk_reservas_id_huesped FOREIGN KEY (id_huesped) REFERENCES Huesped (id_huesped),
    UNIQUE KEY uk_reservas_codigo (codigo),
    CONSTRAINT fk_reservas_id_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Detalle_Reserva (
    id_detallereserva INT NOT NULL AUTO_INCREMENT,
    PrecioReserva DECIMAL(10,2) NOT NULL,
    cantidad_noches INT NOT NULL,
    num_personas INT NOT NULL,
    id_reserva INT NOT NULL,
    id_habitacion INT NOT NULL,
    PRIMARY KEY (id_detallereserva),
    CONSTRAINT fk_detalle_reserva_id_reserva FOREIGN KEY (id_reserva) REFERENCES Reservas (id_reserva),
    CONSTRAINT fk_detalle_reserva_id_habitacion FOREIGN KEY (id_habitacion) REFERENCES Habitacion (id_habitacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Reserva_Servicio (
    id_reservaservicio INT NOT NULL AUTO_INCREMENT,
    id_reserva INT NOT NULL,
    id_servicio INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id_reservaservicio),
    CONSTRAINT fk_reserva_servicio_id_reserva FOREIGN KEY (id_reserva) REFERENCES Reservas (id_reserva),
    CONSTRAINT fk_reserva_servicio_id_servicio FOREIGN KEY (id_servicio) REFERENCES Servicio_Adicional (id_servicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Pagos (
    id_pago INT NOT NULL AUTO_INCREMENT,
    totalpagar DECIMAL(10,2) NOT NULL,
    fecha_pago DATETIME(6) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    id_reserva INT NOT NULL,
    referencia VARCHAR(30) NOT NULL,
    ultimos_digitos VARCHAR(4) NULL,
    observacion VARCHAR(200) NULL,
    PRIMARY KEY (id_pago),
    CONSTRAINT fk_pagos_id_reserva FOREIGN KEY (id_reserva) REFERENCES Reservas (id_reserva),
    UNIQUE KEY uk_pagos_referencia (referencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Reclamo (
    id_reclamo INT NOT NULL AUTO_INCREMENT,
    id_huesped INT NOT NULL,
    id_reserva INT NULL,
    tipo VARCHAR(20) NOT NULL,
    asunto VARCHAR(120) NOT NULL,
    descripcion VARCHAR(1000) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    respuesta VARCHAR(1000) NULL,
    fecha_reclamo DATETIME(6) NOT NULL,
    fecha_respuesta DATETIME(6) NULL,
    atendido_por VARCHAR(50) NULL,
    PRIMARY KEY (id_reclamo),
    CONSTRAINT fk_reclamo_id_huesped FOREIGN KEY (id_huesped) REFERENCES Huesped (id_huesped),
    CONSTRAINT fk_reclamo_id_reserva FOREIGN KEY (id_reserva) REFERENCES Reservas (id_reserva)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT IGNORE INTO Estado_Hotel (estado_hotel) VALUES ('Activo'), ('Inactivo'), ('En mantenimiento');
INSERT IGNORE INTO Estado_Empleado (tipo_estadoempleado) VALUES ('Activo'), ('Inactivo'), ('Suspendido');
