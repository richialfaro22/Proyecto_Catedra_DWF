<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import { get, mostrarError } from '../../api/http'
import { fechaHora } from '../../utils/formato'

const estados = ref([])
const empleados = ref([])
onMounted(async () => {
  try {
    [estados.value, empleados.value] = await Promise.all([get('/catalogos/estados-hotel'), get('/empleados')])
  } catch (e) { mostrarError(e) }
})

const columnas = [
  { campo: 'nombreHotel', etiqueta: 'Hotel' },
  { campo: 'ciudad', etiqueta: 'Ciudad' },
  { campo: 'estrellas', etiqueta: 'Categoría', formato: (v) => '★'.repeat(v) },
  { campo: 'telefono', etiqueta: 'Teléfono' },
  { campo: 'nombreEmpleado', etiqueta: 'Responsable', formato: (v) => v || '—' },
  { campo: 'horasCancelacionGratis', etiqueta: 'Política de cancelación',
    formato: (v, f) => `Gratis hasta ${v} h antes; luego ${f.porcentajePenalizacion}%` },
  { campo: 'fechaRegistro', etiqueta: 'Registro', formato: fechaHora },
  { campo: 'estado', etiqueta: 'Estado', insignia: true }
]

const campos = computed(() => [
  { nombre: 'nombreHotel', etiqueta: 'Nombre del hotel', requerido: true, placeholder: 'Hotel Real InterContinental' },
  { nombre: 'ciudad', etiqueta: 'Ciudad', requerido: true, placeholder: 'San Salvador' },
  { nombre: 'direccion', etiqueta: 'Dirección', requerido: true, placeholder: 'Av. La Revolución, Col. San Benito' },
  { nombre: 'telefono', etiqueta: 'Teléfono de contacto', tipo: 'tel', requerido: true, placeholder: '2244-1000' },
  { nombre: 'email', etiqueta: 'Correo', tipo: 'email' },
  { nombre: 'estrellas', etiqueta: 'Estrellas (1 a 5)', tipo: 'number', min: 1, max: 5, requerido: true },
  { nombre: 'estadoId', etiqueta: 'Estado del hotel', tipo: 'select', requerido: true,
    opciones: estados.value.map(e => ({ valor: e.id, texto: e.nombre })),
    ayuda: 'Solo los hoteles en estado Activo aparecen en las búsquedas.' },
  { nombre: 'empleadoId', etiqueta: 'Empleado responsable', tipo: 'select', placeholder: 'Sin asignar',
    opciones: empleados.value.map(e => ({ valor: e.id, texto: `${e.nombreEmpleado} (${e.tipoEmpleado})` })) },
  { nombre: 'horasCancelacionGratis', etiqueta: 'Horas de cancelación gratuita', tipo: 'number', min: 0, requerido: true,
    ayuda: 'Horas antes de la entrada en las que se puede cancelar sin cargo.' },
  { nombre: 'porcentajePenalizacion', etiqueta: '% de penalización', tipo: 'number', step: '0.01', min: 0, max: 100,
    requerido: true, ayuda: 'Porcentaje del total que se cobra si se cancela fuera de plazo.' },
  { nombre: 'descripcion', etiqueta: 'Descripción', tipo: 'textarea', ancho: 'completo' }
])

const iniciales = computed(() => ({
  estrellas: 3, horasCancelacionGratis: 48, porcentajePenalizacion: 20,
  estadoId: estados.value.find(e => e.nombre === 'Activo')?.id ?? ''
}))
</script>

<template>
  <CrudPage titulo="Tus hoteles y sucursales" subtitulo="Registro de hoteles, su estado y su política de cancelación."
            singular="Hotel" endpoint="/hoteles" :columnas="columnas" :campos="campos" :valores-iniciales="iniciales" />
</template>
