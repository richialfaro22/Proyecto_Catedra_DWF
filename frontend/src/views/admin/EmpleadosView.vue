<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import { get, mostrarError } from '../../api/http'

const estados = ref([])
onMounted(async () => {
  try { estados.value = await get('/catalogos/estados-empleado') } catch (e) { mostrarError(e) }
})

const columnas = [
  { campo: 'nombreEmpleado', etiqueta: 'Nombre' },
  { campo: 'tipoEmpleado', etiqueta: 'Cargo' },
  { campo: 'email', etiqueta: 'Correo' },
  { campo: 'telefono', etiqueta: 'Teléfono' },
  { campo: 'direccion', etiqueta: 'Dirección' },
  { campo: 'estado', etiqueta: 'Estado', insignia: true }
]
const campos = computed(() => [
  { nombre: 'nombreEmpleado', etiqueta: 'Nombre completo', requerido: true },
  { nombre: 'tipoEmpleado', etiqueta: 'Tipo de empleado', requerido: true, placeholder: 'Recepcionista, Administrador, Gerente...' },
  { nombre: 'email', etiqueta: 'Correo', tipo: 'email' },
  { nombre: 'telefono', etiqueta: 'Teléfono', tipo: 'tel', placeholder: '2222-0000' },
  { nombre: 'estadoId', etiqueta: 'Estado', tipo: 'select', requerido: true,
    opciones: estados.value.map(e => ({ valor: e.id, texto: e.nombre })) },
  { nombre: 'direccion', etiqueta: 'Dirección' }
])
const iniciales = computed(() => ({ estadoId: estados.value.find(e => e.nombre === 'Activo')?.id ?? '' }))
</script>

<template>
  <CrudPage titulo="Empleados" subtitulo="Personal del hotel. Las cuentas de administradores y recepcionistas se vinculan a un empleado."
            singular="Empleado" endpoint="/empleados" :columnas="columnas" :campos="campos" :valores-iniciales="iniciales" />
</template>
