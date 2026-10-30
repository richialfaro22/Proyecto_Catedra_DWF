<script setup>
import { computed, onMounted, ref } from 'vue'
import CrudPage from '../../components/CrudPage.vue'
import { get, mostrarError } from '../../api/http'
import { fechaHora, opciones } from '../../utils/formato'

const empleados = ref([])
const huespedes = ref([])
onMounted(async () => {
  try {
    [empleados.value, huespedes.value] = await Promise.all([get('/empleados'), get('/huespedes')])
  } catch (e) { mostrarError(e) }
})

const columnas = [
  { campo: 'alias', etiqueta: 'Alias' },
  { campo: 'email', etiqueta: 'Correo' },
  { campo: 'rol', etiqueta: 'Rol', insignia: true },
  { campo: 'nombreEmpleado', etiqueta: 'Empleado / huésped', formato: (v, f) => v || f.nombreHuesped || '—' },
  { campo: 'fechaCreacion', etiqueta: 'Creado', formato: fechaHora }
]
const campos = computed(() => [
  { nombre: 'alias', etiqueta: 'Alias', requerido: true },
  { nombre: 'email', etiqueta: 'Correo electrónico', tipo: 'email', requerido: true },
  { nombre: 'contrasena', etiqueta: 'Contraseña', tipo: 'password',
    ayuda: 'Mínimo 6 caracteres. Al editar, déjala vacía para conservar la actual.' },
  { nombre: 'rol', etiqueta: 'Rol', tipo: 'select', requerido: true,
    opciones: opciones(['ADMIN', 'RECEPCIONISTA', 'HUESPED']) },
  { nombre: 'empleadoId', etiqueta: 'Empleado (Administrador / Recepcionista)', tipo: 'select', placeholder: 'Ninguno',
    opciones: empleados.value.map(e => ({ valor: e.id, texto: `${e.nombreEmpleado} (${e.tipoEmpleado})` })) },
  { nombre: 'huespedId', etiqueta: 'Huésped (rol Huésped)', tipo: 'select', placeholder: 'Ninguno',
    opciones: huespedes.value.map(h => ({ valor: h.id, texto: `${h.nombreHuesped} (${h.documento})` })) }
])
const aFormulario = (f) => ({ alias: f.alias, email: f.email, rol: f.rol, contrasena: '',
  empleadoId: f.empleadoId ?? '', huespedId: f.huespedId ?? '' })
</script>

<template>
  <CrudPage titulo="Usuarios y roles" subtitulo="Creación, actualización, activación, desactivación y eliminación de usuarios."
            singular="Usuario" endpoint="/usuarios" campo-activo="activo" :columnas="columnas" :campos="campos"
            :a-formulario="aFormulario" :valores-iniciales="{ rol: 'RECEPCIONISTA' }" />
</template>
