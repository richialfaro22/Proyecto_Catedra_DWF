<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import { get, mostrarError } from '../../api/http'
import { dinero, fecha } from '../../utils/formato'

const hoteles = ref([])
const tipos = ref([])
onMounted(async () => {
  try {
    [hoteles.value, tipos.value] = await Promise.all([get('/hoteles'), get('/tipos-habitacion')])
  } catch (e) { mostrarError(e) }
})

const columnas = [
  { campo: 'nombreTarifa', etiqueta: 'Tarifa' },
  { campo: 'nombreHotel', etiqueta: 'Hotel' },
  { campo: 'tipoHabitacion', etiqueta: 'Tipo' },
  { campo: 'precioNoche', etiqueta: 'Precio/noche', formato: dinero },
  { campo: 'fechaInicio', etiqueta: 'Vigencia', formato: (v, f) => `${fecha(v)} – ${fecha(f.fechaFin)}` }
]
const campos = computed(() => [
  { nombre: 'nombreTarifa', etiqueta: 'Nombre', requerido: true, placeholder: 'Temporada alta', ancho: 'completo' },
  { nombre: 'hotelId', etiqueta: 'Hotel', tipo: 'select', requerido: true,
    opciones: hoteles.value.map(h => ({ valor: h.id, texto: h.nombreHotel })) },
  { nombre: 'tipoId', etiqueta: 'Tipo de habitación', tipo: 'select', requerido: true,
    opciones: tipos.value.map(t => ({ valor: t.id, texto: t.tipoHabitacion })) },
  { nombre: 'precioNoche', etiqueta: 'Precio por noche ($)', tipo: 'number', step: '0.01', min: 1, requerido: true },
  { nombre: 'fechaInicio', etiqueta: 'Desde', tipo: 'date', requerido: true },
  { nombre: 'fechaFin', etiqueta: 'Hasta', tipo: 'date', requerido: true }
])
</script>

<template>
  <CrudPage titulo="Tarifas de temporada"
            subtitulo="Precio por noche de un tipo de habitación en un hotel durante un período. Fuera de esas fechas se usa el precio base de la habitación."
            singular="Tarifa" endpoint="/tarifas" campo-activo="activa" :columnas="columnas" :campos="campos" />
</template>
