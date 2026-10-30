<script setup>
import CrudPage from '../../components/CrudPage.vue'
import { dinero, etiqueta, opciones } from '../../utils/formato'

const columnas = [
  { campo: 'nombreServicio', etiqueta: 'Servicio' },
  { campo: 'categoria', etiqueta: 'Categoría', formato: etiqueta },
  { campo: 'precio', etiqueta: 'Precio', formato: dinero },
  { campo: 'tipoCobro', etiqueta: 'Cobro', formato: etiqueta },
  { campo: 'descripcion', etiqueta: 'Descripción' }
]
const campos = [
  { nombre: 'nombreServicio', etiqueta: 'Nombre', requerido: true },
  { nombre: 'categoria', etiqueta: 'Categoría', tipo: 'select', requerido: true,
    opciones: opciones(['ALIMENTACION', 'TRANSPORTE', 'LAVANDERIA', 'SPA', 'TOUR', 'OTRO']) },
  { nombre: 'precio', etiqueta: 'Precio ($)', tipo: 'number', step: '0.01', min: 0, requerido: true },
  { nombre: 'tipoCobro', etiqueta: 'Tipo de cobro', tipo: 'select', requerido: true,
    opciones: opciones(['POR_ESTADIA', 'POR_NOCHE']),
    ayuda: 'Por noche: se multiplica por las noches de la estadía.' },
  { nombre: 'descripcion', etiqueta: 'Descripción', tipo: 'textarea', ancho: 'completo' }
]
</script>

<template>
  <CrudPage titulo="Servicios adicionales" subtitulo="Alimentación, transporte, lavandería, spa y otros servicios."
            singular="Servicio" endpoint="/servicios" campo-activo="activo" :columnas="columnas" :campos="campos"
            :valores-iniciales="{ tipoCobro: 'POR_ESTADIA' }" />
</template>
