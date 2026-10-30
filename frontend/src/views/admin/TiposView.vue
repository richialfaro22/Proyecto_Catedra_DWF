<script setup>
import CrudPage from '../../components/CrudPage.vue'
import { dinero } from '../../utils/formato'

const columnas = [
  { campo: 'tipoHabitacion', etiqueta: 'Tipo' },
  { campo: 'cantidadPersonas', etiqueta: 'Capacidad', formato: (v) => `${v} persona(s)` },
  { campo: 'precio', etiqueta: 'Precio de referencia', formato: dinero },
  { campo: 'serviciosIncluidos', etiqueta: 'Servicios incluidos' }
]
const campos = [
  { nombre: 'tipoHabitacion', etiqueta: 'Tipo de habitación', requerido: true, placeholder: 'Suite presidencial' },
  { nombre: 'cantidadPersonas', etiqueta: 'Capacidad (personas)', tipo: 'number', min: 1, max: 10, requerido: true },
  { nombre: 'precio', etiqueta: 'Precio de referencia por noche ($)', tipo: 'number', step: '0.01', min: 1, requerido: true,
    ayuda: 'Se sugiere como precio base al crear habitaciones de este tipo.' },
  { nombre: 'serviciosIncluidos', etiqueta: 'Servicios incluidos', placeholder: 'WiFi, Desayuno, TV',
    ayuda: 'Separados por coma.' },
  { nombre: 'descripcion', etiqueta: 'Descripción', tipo: 'textarea', ancho: 'completo' }
]
</script>

<template>
  <CrudPage titulo="Tipos de habitación" subtitulo="Capacidad, precio de referencia y servicios incluidos de cada tipo."
            singular="Tipo de habitación" endpoint="/tipos-habitacion" campo-activo="activo"
            :columnas="columnas" :campos="campos" />
</template>
