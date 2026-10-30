package com.udb.hotel.service;

import com.udb.hotel.dto.ServicioRequest;
import com.udb.hotel.dto.ServicioResponse;
import com.udb.hotel.exception.OperacionNoPermitidaException;
import com.udb.hotel.exception.RecursoDuplicadoException;
import com.udb.hotel.exception.RecursoNoEncontradoException;
import com.udb.hotel.model.ServicioAdicional;
import com.udb.hotel.repository.ReservaServicioRepository;
import com.udb.hotel.repository.ServicioAdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServicioAdicionalService {

    private final ServicioAdicionalRepository repo;
    private final ReservaServicioRepository reservaServicioRepo;

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar(boolean soloActivos) {
        var lista = soloActivos ? repo.findByActivoTrueOrderByCategoriaAscNombreServicioAsc()
                                : repo.findAllByOrderByCategoriaAscNombreServicioAsc();
        return lista.stream().map(ServicioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Integer id) {
        return ServicioResponse.de(buscar(id));
    }

    public ServicioResponse crear(ServicioRequest r) {
        validarNombre(r.nombreServicio(), null);
        ServicioAdicional s = new ServicioAdicional();
        s.setActivo(true);
        copiar(r, s);
        return ServicioResponse.de(repo.save(s));
    }

    public ServicioResponse actualizar(Integer id, ServicioRequest r) {
        ServicioAdicional s = buscar(id);
        validarNombre(r.nombreServicio(), id);
        copiar(r, s);
        return ServicioResponse.de(repo.save(s));
    }

    public ServicioResponse cambiarEstado(Integer id, boolean activo) {
        ServicioAdicional s = buscar(id);
        s.setActivo(activo);
        return ServicioResponse.de(repo.save(s));
    }

    public void eliminar(Integer id) {
        ServicioAdicional s = buscar(id);
        if (reservaServicioRepo.existsByServicioId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el servicio porque ya fue agregado en reservas. Puedes desactivarlo.");
        }
        repo.delete(s);
    }

    public ServicioAdicional buscar(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el servicio solicitado"));
    }

    private void validarNombre(String nombre, Integer idActual) {
        repo.findByNombreServicioIgnoreCase(nombre.trim()).ifPresent(otro -> {
            if (!otro.getId().equals(idActual)) {
                throw new RecursoDuplicadoException("Ya existe un servicio llamado '" + nombre.trim() + "'.");
            }
        });
    }

    private void copiar(ServicioRequest r, ServicioAdicional s) {
        s.setNombreServicio(r.nombreServicio().trim());
        s.setDescripcion(r.descripcion());
        s.setCategoria(r.categoria());
        s.setPrecio(r.precio());
        s.setTipoCobro(r.tipoCobro());
    }
}
