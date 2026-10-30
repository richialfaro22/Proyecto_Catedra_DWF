package com.udb.hotel.service;

import com.udb.hotel.dto.HuespedRequest;
import com.udb.hotel.dto.HuespedResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.exception.ValidacionException;
import com.udb.hotel.model.Huesped;
import com.udb.hotel.model.TipoDocumento;
import com.udb.hotel.model.Usuario;
import com.udb.hotel.repository.HuespedRepository;
import com.udb.hotel.repository.ReclamoRepository;
import com.udb.hotel.repository.ReservaRepository;
import com.udb.hotel.repository.UsuarioRepository;
import com.udb.hotel.validation.Patrones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HuespedService {

    private final HuespedRepository repo;
    private final ReservaRepository reservaRepo;
    private final ReclamoRepository reclamoRepo;
    private final UsuarioRepository usuarioRepo;

    @Transactional(readOnly = true)
    public List<HuespedResponse> listar(String q) {
        String filtro = (q == null || q.isBlank()) ? null : q.trim();
        return repo.buscar(filtro).stream().map(HuespedResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public HuespedResponse obtener(Integer id) {
        return HuespedResponse.de(buscar(id));
    }

    public HuespedResponse crear(HuespedRequest r) {
        return HuespedResponse.de(crearEntidad(r, "documento"));
    }

    public HuespedResponse actualizar(Integer id, HuespedRequest r) {
        return HuespedResponse.de(actualizarEntidad(buscar(id), r, "documento"));
    }

    public void eliminar(Integer id) {
        Huesped h = buscar(id);
        if (reservaRepo.existsByHuespedId(id) || reclamoRepo.existsByHuespedId(id)
                || usuarioRepo.existsByHuespedId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el huésped porque tiene reservas, reclamos o una cuenta de usuario.");
        }
        repo.delete(h);
    }

    public Huesped buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el huésped solicitado."));
    }


    public Huesped crearEntidad(HuespedRequest r, String campoDoc) {
        String doc = normalizarDocumento(r.tipoDocumento(), r.documento());
        validarDocumento(r.tipoDocumento(), doc, campoDoc);
        if (repo.findByDocumentoIgnoreCase(doc).isPresent()) {
            throw new ValidacionException(campoDoc, "Ya existe un huésped registrado con el documento " + doc + ".");
        }
        Huesped h = new Huesped();
        copiar(r, doc, h);
        return repo.save(h);
    }

    public Huesped actualizarEntidad(Huesped h, HuespedRequest r, String campoDoc) {
        String doc = normalizarDocumento(r.tipoDocumento(), r.documento());
        validarDocumento(r.tipoDocumento(), doc, campoDoc);
        repo.findByDocumentoIgnoreCase(doc).ifPresent(otro -> {
            if (!otro.getId().equals(h.getId())) {
                throw new RecursoDuplicadoException("Ya existe otro huésped registrado con el documento " + doc + ".");
            }
        });
        copiar(r, doc, h);
        return repo.save(h);
    }


    public Huesped resolverParaReserva(HuespedRequest r, Usuario usuario, boolean esPersonal) {
        String campo = "huesped.documento";
        String doc = normalizarDocumento(r.tipoDocumento(), r.documento());
        validarDocumento(r.tipoDocumento(), doc, campo);

        Huesped h = repo.findByDocumentoIgnoreCase(doc).orElse(null);
        if (h != null && !esPersonal) {
            usuarioRepo.findFirstByHuespedId(h.getId()).ifPresent(cuenta -> {
                if (!cuenta.getId().equals(usuario.getId())) {
                    throw new ValidacionException(campo, "Ese documento pertenece a un huésped registrado con otra cuenta.");
                }
            });
        }
        if (h == null) {
            h = new Huesped();
        }
        copiar(r, doc, h);
        h = repo.save(h);

        if (!esPersonal && usuario.getHuesped() == null) {
            usuario.setHuesped(h); // primera reserva de la cuenta: queda como su ficha de huésped
            usuarioRepo.save(usuario);
        }
        return h;
    }

    private String normalizarDocumento(TipoDocumento tipo, String numero) {
        String n = numero.trim();
        return tipo == TipoDocumento.PASAPORTE ? n.toUpperCase() : n;
    }

    private void validarDocumento(TipoDocumento tipo, String numero, String campo) {
        switch (tipo) {
            case DUI -> {
                if (!numero.matches(Patrones.DUI)) {
                    throw new ValidacionException(campo, "El DUI debe tener el formato 00000000-0 (8 dígitos, guion y 1 dígito).");
                }
            }
            case PASAPORTE -> {
                if (!numero.matches(Patrones.PASAPORTE)) {
                    throw new ValidacionException(campo, "El pasaporte debe tener de 6 a 12 letras o números, sin espacios.");
                }
            }
            case OTRO -> {
                if (!numero.matches(Patrones.OTRO_DOCUMENTO)) {
                    throw new ValidacionException(campo, "El documento debe tener de 4 a 20 letras, números o guiones.");
                }
            }
        }
    }

    private void copiar(HuespedRequest r, String doc, Huesped h) {
        h.setNombreHuesped(r.nombreHuesped().trim().replaceAll("\\s+", " "));
        h.setFechaNacimiento(r.fechaNacimiento());
        h.setTipoDocumento(r.tipoDocumento());
        h.setDocumento(doc);
        h.setTelefono(r.telefono().trim());
        h.setEmail(r.email().trim().toLowerCase());
        h.setDireccion(r.direccion() == null || r.direccion().isBlank() ? null : r.direccion().trim());
        h.setNacionalidad(r.nacionalidad() == null || r.nacionalidad().isBlank() ? null : r.nacionalidad().trim());
    }
}
