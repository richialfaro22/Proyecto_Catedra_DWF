<script setup>
import CrudPage from '../../components/CrudPage.vue'
import { esAdmin } from '../../stores/auth'
import { fecha, fechaHora } from '../../utils/formato'
import { camposHuesped } from '../../utils/huesped'

const columnas = [
  { campo: 'nombreHuesped', etiqueta: 'Nombre' },
  { campo: 'tipoDocumento', etiqueta: 'Documento', formato: (v, f) => `${v} ${f.documento}` },
  { campo: 'fechaNacimiento', etiqueta: 'Nacimiento', formato: (v, f) => `${fecha(v)} (${f.edad} años)` },
  { campo: 'telefono', etiqueta: 'Teléfono' },
  { campo: 'email', etiqueta: 'Correo' },
  { campo: 'fechaRegistro', etiqueta: 'Registro', formato: fechaHora }
]
</script>

<template>
  <CrudPage titulo="Huéspedes" subtitulo="Registro de huéspedes del hotel." singular="Huésped" endpoint="/huespedes"
            :columnas="columnas" :campos="camposHuesped()" :puede-eliminar="esAdmin"
            :valores-iniciales="{ tipoDocumento: 'DUI', nacionalidad: 'Salvadoreña' }" />
</template>
