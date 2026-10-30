<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import { get, mostrarError } from '../../api/http'
import { esAdmin } from '../../stores/auth'
import { dinero, opciones } from '../../utils/formato'

const hoteles = ref([])
const tipos = ref([])

onMounted(async () => {
  try {
    [hoteles.value, tipos.value] = await Promise.all([get('/hoteles'), get('/tipos-habitacion')])
  } catch (e) { mostrarError(e) }
})

const columnas = [
  { campo: 'nombreHotel', etiqueta: 'Hotel' },
  { campo: 'numeroHabitacion', etiqueta: 'Número' },
  { campo: 'numPiso', etiqueta: 'Piso' },
  { campo: 'tipoHabitacion', etiqueta: 'Tipo' },
  { campo: 'cantidadPersonas', etiqueta: 'Capacidad' },
  { campo: 'precioBase', etiqueta: 'Tarifa/noche', formato: dinero },
  { campo: 'estado', etiqueta: 'Estado', insignia: true }
]
const campos = computed(() => [
  { nombre: 'hotelId', etiqueta: 'Hotel', tipo: 'select', requerido: true,
    opciones: hoteles.value.map(h => ({ valor: h.id, texto: h.nombreHotel })) },
  { nombre: 'tipoId', etiqueta: 'Tipo de habitación', tipo: 'select', requerido: true,
    opciones: tipos.value.map(t => ({ valor: t.id, texto: `${t.tipoHabitacion} (${t.cantidadPersonas} pers.)` })) },
  { nombre: 'numeroHabitacion', etiqueta: 'Número', requerido: true, placeholder: '101', ayuda: 'Ej. 101 o 12B' },
  { nombre: 'numPiso', etiqueta: 'Piso', tipo: 'number', min: 0, requerido: true },
  { nombre: 'precioBase', etiqueta: 'Precio base por noche ($)', tipo: 'number', step: '0.01', min: 1, requerido: true,
    ayuda: 'Se usa cuando no hay una tarifa de temporada vigente.' },
  { nombre: 'estado', etiqueta: 'Estado', tipo: 'select', requerido: true,
    opciones: opciones(['DISPONIBLE', 'OCUPADA', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO']) },
  { nombre: 'descripcion', etiqueta: 'Descripción', tipo: 'textarea', ancho: 'completo' }
])
</script>

<template>
  <CrudPage titulo="Habitaciones y tarifas" subtitulo="Habitaciones de cada hotel, su precio base y su estado operativo."
            singular="Habitación" endpoint="/habitaciones" :columnas="columnas" :campos="campos"
            :puede-crear="esAdmin" :puede-eliminar="esAdmin" :valores-iniciales="{ estado: 'DISPONIBLE', numPiso: 1 }" />
</template>
