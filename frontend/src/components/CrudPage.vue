<script setup>
/**
 * Pantalla CRUD reutilizable: tabla con búsqueda, formulario en modal (crear/editar),
 * activar/desactivar y eliminar. Los errores de validación del servidor se muestran
 * debajo de cada campo.
 *
 * columnas: [{ campo, etiqueta, formato?(valor, fila), insignia?: bool }]
 * campos:   [{ nombre, etiqueta, tipo, opciones, requerido, soloCrear, ayuda, ancho: 'completo' }]
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { get, post, put, patch, del, mostrarError } from '../api/http'
import { toast } from '../stores/toast'
import Modal from './Modal.vue'
import Campo from './Campo.vue'
import Insignia from './Insignia.vue'
import Cargando from './Cargando.vue'
import Confirmar from './Confirmar.vue'
import Encabezado from './Encabezado.vue'

const props = defineProps({
  titulo: String,
  subtitulo: String,
  singular: String,
  endpoint: String,
  columnas: Array,
  campos: Array,
  valoresIniciales: { type: Object, default: () => ({}) },
  campoActivo: { type: String, default: null },
  puedeCrear: { type: Boolean, default: true },
  puedeEditar: { type: Boolean, default: true },
  puedeEliminar: { type: Boolean, default: true },
  /** Convierte una fila de la tabla en los valores del formulario. */
  aFormulario: { type: Function, default: (fila) => ({ ...fila }) },
  /** Convierte el formulario en el cuerpo enviado a la API. */
  aPeticion: { type: Function, default: (form) => form }
})
const emit = defineEmits(['cambio'])

const filas = ref([])
const cargando = ref(true)
const busqueda = ref('')
const modal = ref(false)
const editandoId = ref(null)
const guardando = ref(false)
const form = reactive({})
const errores = reactive({})
const aEliminar = ref(null)
const eliminando = ref(false)

const filtradas = computed(() => {
  const q = busqueda.value.trim().toLowerCase()
  if (!q) return filas.value
  return filas.value.filter(f => JSON.stringify(Object.values(f)).toLowerCase().includes(q))
})

const camposVisibles = computed(() => props.campos.filter(c => !(c.soloCrear && editandoId.value)))

async function cargar() {
  cargando.value = true
  try {
    filas.value = await get(props.endpoint)
  } catch (e) {
    mostrarError(e)
  } finally {
    cargando.value = false
  }
}

function limpiar() {
  Object.keys(form).forEach(k => delete form[k])
  Object.keys(errores).forEach(k => delete errores[k])
}

function nuevo() {
  limpiar()
  props.campos.forEach(c => { form[c.nombre] = '' })
  Object.assign(form, props.valoresIniciales)
  editandoId.value = null
  modal.value = true
}

function editar(fila) {
  limpiar()
  props.campos.forEach(c => { form[c.nombre] = '' })
  const valores = props.aFormulario(fila)
  props.campos.forEach(c => {
    if (valores[c.nombre] !== undefined && valores[c.nombre] !== null) form[c.nombre] = valores[c.nombre]
  })
  editandoId.value = fila.id
  modal.value = true
}

async function guardar() {
  guardando.value = true
  Object.keys(errores).forEach(k => delete errores[k])
  const cuerpo = {}
  Object.entries(props.aPeticion({ ...form })).forEach(([k, v]) => { cuerpo[k] = v === '' ? null : v })
  try {
    if (editandoId.value) {
      await put(`${props.endpoint}/${editandoId.value}`, cuerpo)
      toast.exito(`${props.singular} actualizado correctamente.`)
    } else {
      await post(props.endpoint, cuerpo)
      toast.exito(`${props.singular} creado correctamente.`)
    }
    modal.value = false
    await cargar()
    emit('cambio')
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    guardando.value = false
  }
}

async function alternar(fila) {
  const activo = fila[props.campoActivo]
  try {
    await patch(`${props.endpoint}/${fila.id}/${activo ? 'desactivar' : 'activar'}`)
    toast.exito(`${props.singular} ${activo ? 'desactivado' : 'activado'}.`)
    await cargar()
  } catch (e) {
    mostrarError(e)
  }
}

async function eliminar() {
  eliminando.value = true
  try {
    const r = await del(`${props.endpoint}/${aEliminar.value.id}`)
    toast.exito(r?.mensaje || 'Registro eliminado.')
    aEliminar.value = null
    await cargar()
  } catch (e) {
    mostrarError(e)
  } finally {
    eliminando.value = false
  }
}

function valor(fila, col) {
  const v = fila[col.campo]
  return col.formato ? col.formato(v, fila) : (v ?? '—')
}

onMounted(cargar)
defineExpose({ cargar })
</script>

<template>
  <div>
    <Encabezado :titulo="titulo" :subtitulo="subtitulo">
      <input v-model="busqueda" type="search" class="entrada w-56" placeholder="Buscar..." />
      <button v-if="puedeCrear" class="btn-primario" @click="nuevo">+ Nuevo</button>
    </Encabezado>

    <div class="tarjeta overflow-hidden">
      <Cargando v-if="cargando" />
      <div v-else-if="!filtradas.length" class="py-12 text-center text-sm text-slate-500">
        No hay registros{{ busqueda ? ' que coincidan con la búsqueda' : '' }}.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="tabla">
          <thead>
            <tr>
              <th v-for="c in columnas" :key="c.campo">{{ c.etiqueta }}</th>
              <th v-if="campoActivo">Estado</th>
              <th class="text-right">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="f in filtradas" :key="f.id">
              <td v-for="c in columnas" :key="c.campo">
                <Insignia v-if="c.insignia" :valor="f[c.campo]" />
                <span v-else>{{ valor(f, c) }}</span>
              </td>
              <td v-if="campoActivo"><Insignia :valor="f[campoActivo] ? 'ACTIVO' : 'INACTIVO'" /></td>
              <td class="whitespace-nowrap text-right">
                <slot name="acciones" :fila="f" :recargar="cargar" />
                <button v-if="puedeEditar" class="btn-suave btn-sm ml-1" @click="editar(f)">Editar</button>
                <button v-if="campoActivo && puedeEditar" class="btn-secundario btn-sm ml-1" @click="alternar(f)">
                  {{ f[campoActivo] ? 'Desactivar' : 'Activar' }}
                </button>
                <button v-if="puedeEliminar" class="btn-sm ml-1 btn bg-rose-50 text-rose-700 hover:bg-rose-100"
                        @click="aEliminar = f">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal :abierto="modal" :titulo="(editandoId ? 'Editar ' : 'Nuevo ') + singular.toLowerCase()" @cerrar="modal = false">
      <form id="form-crud" class="grid grid-cols-1 gap-4 sm:grid-cols-2" novalidate @submit.prevent="guardar">
        <Campo v-for="c in camposVisibles" :key="c.nombre" v-model="form[c.nombre]" :campo="c"
               :error="errores[c.nombre]" :class="c.ancho === 'completo' ? 'sm:col-span-2' : ''" />
      </form>
      <p class="mt-4 text-xs text-slate-500">Los campos con <span class="text-rose-500">*</span> son obligatorios.</p>
      <template #pie>
        <button class="btn-secundario" @click="modal = false">Cancelar</button>
        <button class="btn-primario" type="submit" form="form-crud" :disabled="guardando">
          {{ guardando ? 'Guardando...' : 'Guardar' }}
        </button>
      </template>
    </Modal>

    <Confirmar :abierto="!!aEliminar" :ocupado="eliminando" textoBoton="Eliminar"
               :mensaje="`Se eliminará este registro de forma permanente. Esta acción no se puede deshacer.`"
               @cerrar="aEliminar = null" @confirmar="eliminar" />
  </div>
</template>
