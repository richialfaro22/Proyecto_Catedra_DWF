package com.udb.hotel.config;

import com.udb.hotel.model.*;
import com.udb.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos iniciales al arrancar:
 * <ul>
 *   <li>Catálogos Estado_Hotel y Estado_Empleado.</li>
 *   <li>Empleados y usuarios de prueba (si no existen): admin / Admin123*, recepcion / Recep123*,
 *       huesped / Huesped123*.</li>
 *   <li>Datos de ejemplo (solo si no hay hoteles): hoteles, tipos de habitación, habitaciones,
 *       tarifas, servicios adicionales y algunas reservas para las estadísticas.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicializador implements CommandLineRunner {

    private final EstadoHotelRepository estadoHotelRepo;
    private final EstadoEmpleadoRepository estadoEmpleadoRepo;
    private final EmpleadoRepository empleadoRepo;
    private final UsuarioRepository usuarioRepo;
    private final HotelRepository hotelRepo;
    private final TipoHabitacionRepository tipoRepo;
    private final HabitacionRepository habitacionRepo;
    private final TarifaRepository tarifaRepo;
    private final ServicioAdicionalRepository servicioRepo;
    private final HuespedRepository huespedRepo;
    private final ReservaRepository reservaRepo;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        // ---------------- catálogos
        EstadoHotel activo = estadoHotel(EstadoHotel.ACTIVO);
        estadoHotel("Inactivo");
        estadoHotel("En mantenimiento");
        EstadoEmpleado empActivo = estadoEmpleado("Activo");
        estadoEmpleado("Inactivo");
        estadoEmpleado("Suspendido");

        // ---------------- empleados y usuarios
        Empleado ana = empleado("Ana Ramírez", "ana.ramirez@hotelesudb.com", "Administrador", empActivo);
        Empleado carlos = empleado("Carlos Meza", "carlos.meza@hotelesudb.com", "Recepcionista", empActivo);
        Usuario admin = usuario("admin", "Admin123*", "admin@hotelesudb.com", Rol.ADMIN, ana, null);
        usuario("recepcion", "Recep123*", "recepcion@hotelesudb.com", Rol.RECEPCIONISTA, carlos, null);

        Huesped nelly = huespedRepo.findByDocumentoIgnoreCase("04512345-6").orElseGet(() ->
                huespedRepo.save(Huesped.builder().nombreHuesped("Nelly Pérez")
                        .fechaNacimiento(LocalDate.of(1995, 4, 12)).tipoDocumento(TipoDocumento.DUI)
                        .documento("04512345-6").telefono("7000-0000").email("nelly.perez@correo.com")
                        .direccion("Col. Escalón, San Salvador").nacionalidad("Salvadoreña").build()));
        Usuario huespedUsr = usuario("huesped", "Huesped123*", "nelly.perez@correo.com", Rol.HUESPED, null, nelly);

        if (hotelRepo.count() > 0) {
            return;
        }
        log.info("Cargando datos de ejemplo del hotel...");

        // ---------------- hoteles
        Hotel intercontinental = hotelRepo.save(hotel("Hotel Real InterContinental", "San Salvador",
                "Av. La Revolución, Col. San Benito", "2244-1000", "reservas@realintercontinental.com", 5,
                "Hotel ejecutivo en la zona rosa, cerca de centros comerciales y restaurantes.", 48, "20.00",
                activo, ana));
        Hotel barcelo = hotelRepo.save(hotel("Hotel Barceló San Salvador", "San Salvador",
                "Calle El Mirador y 89 Av. Norte, Col. Escalón", "2268-4545", "reservas@barcelosansalvador.com", 4,
                "Hotel con piscina, gimnasio y salones para eventos.", 48, "20.00", activo, ana));
        Hotel cumbres = hotelRepo.save(hotel("Hostal Cumbres de El Salvador", "La Libertad",
                "Km 42 Carretera del Litoral, Playa El Tunco", "2389-1122", "hola@cumbres.com", 3,
                "Hostal frente al mar, ideal para surf y descanso.", 24, "50.00", activo, carlos));

        // ---------------- tipos de habitación
        TipoHabitacion sencilla = tipoRepo.save(tipo("Sencilla", "Habitación con una cama individual.", 1, "60.00",
                "WiFi, TV, Aire acondicionado"));
        TipoHabitacion doble = tipoRepo.save(tipo("Doble estándar", "Dos camas matrimoniales.", 2, "85.00",
                "WiFi, Desayuno, TV, Aire acondicionado"));
        TipoHabitacion junior = tipoRepo.save(tipo("Suite junior", "Sala independiente y vista a la ciudad.", 3, "120.00",
                "WiFi, Piscina, Parqueo, Desayuno"));
        TipoHabitacion presidencial = tipoRepo.save(tipo("Suite presidencial", "Jacuzzi, sala y comedor privados.", 4,
                "180.00", "WiFi, Piscina, Parqueo, Desayuno, Jacuzzi"));

        // ---------------- habitaciones (precio_base propio de cada habitación)
        List<Habitacion> habs = new ArrayList<>();
        habs.add(hab(intercontinental, sencilla, "101", 1, "60.00"));
        habs.add(hab(intercontinental, doble, "201", 2, "85.00"));
        habs.add(hab(intercontinental, doble, "202", 2, "90.00"));
        habs.add(hab(intercontinental, junior, "301", 3, "120.00"));
        habs.add(hab(intercontinental, presidencial, "401", 4, "180.00"));
        habs.add(hab(barcelo, doble, "110", 1, "80.00"));
        habs.add(hab(barcelo, junior, "210", 2, "120.00"));
        habs.add(hab(barcelo, junior, "211", 2, "125.00"));
        habs.add(hab(cumbres, sencilla, "1A", 1, "40.00"));
        habs.add(hab(cumbres, doble, "2A", 2, "55.00"));
        Habitacion mantenimiento = hab(intercontinental, sencilla, "102", 1, "60.00");
        mantenimiento.setEstado(EstadoHabitacion.MANTENIMIENTO);
        habs.add(mantenimiento);
        habitacionRepo.saveAll(habs);

        // ---------------- tarifas de temporada (fin de año)
        int anio = LocalDate.now().getYear();
        tarifaRepo.save(Tarifa.builder().nombreTarifa("Temporada alta fin de año").hotel(cumbres).tipo(doble)
                .precioNoche(new BigDecimal("80.00"))
                .fechaInicio(LocalDate.of(anio, 12, 20)).fechaFin(LocalDate.of(anio + 1, 1, 5))
                .activa(true).build());
        tarifaRepo.save(Tarifa.builder().nombreTarifa("Temporada alta fin de año").hotel(intercontinental)
                .tipo(presidencial).precioNoche(new BigDecimal("230.00"))
                .fechaInicio(LocalDate.of(anio, 12, 20)).fechaFin(LocalDate.of(anio + 1, 1, 5))
                .activa(true).build());

        // ---------------- servicios adicionales
        ServicioAdicional desayuno = servicioRepo.save(servicio("Desayuno buffet", "Desayuno por persona.",
                CategoriaServicio.ALIMENTACION, "12.00", TipoCobro.POR_NOCHE));
        servicioRepo.save(servicio("Cena romántica", "Cena para dos personas con vino.",
                CategoriaServicio.ALIMENTACION, "60.00", TipoCobro.POR_ESTADIA));
        ServicioAdicional traslado = servicioRepo.save(servicio("Traslado aeropuerto",
                "Transporte privado desde/hacia el aeropuerto.", CategoriaServicio.TRANSPORTE, "35.00",
                TipoCobro.POR_ESTADIA));
        servicioRepo.save(servicio("Lavandería express", "Lavado y planchado de una carga de ropa.",
                CategoriaServicio.LAVANDERIA, "15.00", TipoCobro.POR_ESTADIA));
        servicioRepo.save(servicio("Masaje relajante", "Sesión de 60 minutos.",
                CategoriaServicio.SPA, "45.00", TipoCobro.POR_ESTADIA));
        servicioRepo.save(servicio("Tour Ruta de las Flores", "Tour guiado de día completo.",
                CategoriaServicio.TOUR, "55.00", TipoCobro.POR_ESTADIA));
        servicioRepo.save(servicio("Parqueo", "Estacionamiento vigilado.",
                CategoriaServicio.OTRO, "5.00", TipoCobro.POR_NOCHE));

        // ---------------- reservas de ejemplo (para estadísticas)
        Huesped john = huespedRepo.save(Huesped.builder().nombreHuesped("John Smith")
                .fechaNacimiento(LocalDate.of(1988, 9, 3)).tipoDocumento(TipoDocumento.PASAPORTE)
                .documento("X1234567").telefono("+1 305 555 0101").email("john.smith@mail.com")
                .nacionalidad("Estadounidense").build());

        LocalDate hoy = LocalDate.now();
        reservaRepo.save(reservaEjemplo("R-DEMO-0001", nelly, habs.get(1), admin, hoy.minusDays(10), hoy.minusDays(7),
                2, EstadoReserva.COMPLETADA, MetodoPago.TARJETA, desayuno, 2));
        Reserva enCurso = reservaEjemplo("R-DEMO-0002", john, habs.get(6), admin, hoy.minusDays(1), hoy.plusDays(3),
                3, EstadoReserva.EN_CURSO, MetodoPago.TRANSFERENCIA, traslado, 1);
        habs.get(6).setEstado(EstadoHabitacion.OCUPADA);
        reservaRepo.save(enCurso);
        reservaRepo.save(reservaEjemplo("R-DEMO-0003", nelly, habs.get(3), huespedUsr, hoy.plusDays(15),
                hoy.plusDays(18), 2, EstadoReserva.CONFIRMADA, MetodoPago.TARJETA, desayuno, 2));

        log.info("Datos de ejemplo cargados.");
    }

    // ------------------------------------------------------------ helpers

    private EstadoHotel estadoHotel(String nombre) {
        return estadoHotelRepo.findByEstadoHotelIgnoreCase(nombre)
                .orElseGet(() -> estadoHotelRepo.save(EstadoHotel.builder().estadoHotel(nombre).build()));
    }

    private EstadoEmpleado estadoEmpleado(String nombre) {
        return estadoEmpleadoRepo.findByTipoEstadoEmpleadoIgnoreCase(nombre)
                .orElseGet(() -> estadoEmpleadoRepo.save(EstadoEmpleado.builder().tipoEstadoEmpleado(nombre).build()));
    }

    private Empleado empleado(String nombre, String email, String tipo, EstadoEmpleado estado) {
        return empleadoRepo.findByEmailIgnoreCase(email).orElseGet(() -> empleadoRepo.save(Empleado.builder()
                .nombreEmpleado(nombre).email(email).telefono("2222-0000").direccion("San Salvador")
                .tipoEmpleado(tipo).estado(estado).build()));
    }

    private Usuario usuario(String alias, String clave, String email, Rol rol, Empleado emp, Huesped hue) {
        return usuarioRepo.findByAliasIgnoreCase(alias).orElseGet(() -> usuarioRepo.save(Usuario.builder()
                .alias(alias)
                .contrasena(encoder.encode(clave))
                .email(email)
                .rol(rol)
                .empleado(emp)
                .huesped(hue)
                .activo(true)
                .build()));
    }

    private static Hotel hotel(String nombre, String ciudad, String direccion, String tel, String email,
                               int estrellas, String desc, int horas, String penalizacion, EstadoHotel estado,
                               Empleado responsable) {
        return Hotel.builder().nombreHotel(nombre).ciudad(ciudad).direccion(direccion).telefono(tel).email(email)
                .estrellas(estrellas).descripcion(desc).horasCancelacionGratis(horas)
                .porcentajePenalizacion(new BigDecimal(penalizacion)).estado(estado).empleado(responsable).build();
    }

    private static TipoHabitacion tipo(String nombre, String desc, int personas, String precio, String incluidos) {
        return TipoHabitacion.builder().tipoHabitacion(nombre).descripcion(desc).cantidadPersonas(personas)
                .precio(new BigDecimal(precio)).serviciosIncluidos(incluidos).activo(true).build();
    }

    private static Habitacion hab(Hotel hotel, TipoHabitacion tipo, String numero, int piso, String precio) {
        return Habitacion.builder().hotel(hotel).tipo(tipo).numeroHabitacion(numero).numPiso(piso)
                .precioBase(new BigDecimal(precio)).estado(EstadoHabitacion.DISPONIBLE).build();
    }

    private static ServicioAdicional servicio(String nombre, String desc, CategoriaServicio cat, String precio,
                                              TipoCobro cobro) {
        return ServicioAdicional.builder().nombreServicio(nombre).descripcion(desc).categoria(cat)
                .precio(new BigDecimal(precio)).tipoCobro(cobro).activo(true).build();
    }

    private static Reserva reservaEjemplo(String codigo, Huesped huesped, Habitacion hab, Usuario usuario,
                                          LocalDate entrada, LocalDate salida, int personas, EstadoReserva estado,
                                          MetodoPago metodo, ServicioAdicional servicio, int cantidad) {
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        BigDecimal subHab = hab.getPrecioBase().multiply(BigDecimal.valueOf(noches));
        BigDecimal subServ = servicio.getPrecio().multiply(BigDecimal.valueOf(cantidad))
                .multiply(BigDecimal.valueOf(servicio.getTipoCobro() == TipoCobro.POR_NOCHE ? noches : 1));
        BigDecimal total = subHab.add(subServ);
        LocalDateTime creada = entrada.minusDays(5).atTime(10, 0);
        if (creada.isAfter(LocalDateTime.now())) {
            creada = LocalDateTime.now();
        }

        Reserva r = Reserva.builder().codigo(codigo).huesped(huesped).hotel(hab.getHotel()).usuario(usuario)
                .fechaReserva(creada).fechaEntrada(entrada).fechaSalida(salida).estadoReserva(estado)
                .subtotalHabitaciones(subHab).subtotalServicios(subServ).total(total).build();
        r.getDetalles().add(DetalleReserva.builder().reserva(r).habitacion(hab).numPersonas(personas)
                .cantidadNoches((int) noches).precioReserva(subHab).build());
        r.getServicios().add(ReservaServicio.builder().reserva(r).servicio(servicio).cantidad(cantidad)
                .precioUnitario(servicio.getPrecio()).subtotal(subServ).build());
        r.getPagos().add(Pago.builder().reserva(r).metodoPago(metodo).totalPagar(total)
                .fechaPago(creada.plusMinutes(10)).estado(EstadoPago.APROBADO)
                .referencia("PAG-" + codigo).ultimosDigitos(metodo == MetodoPago.TARJETA ? "4521" : null)
                .build());
        return r;
    }
}
