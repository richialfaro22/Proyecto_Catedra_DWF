<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { get, post, put, del, mostrarError } from '../api/http'
import { esAdmin, esPersonal } from '../stores/auth'
import { toast } from '../stores/toast'
import { etiqueta, fechaHora, opciones } from '../utils/formato'
import Campo from '../components/Campo.vue'
import Cargando from '../components/Cargando.vue'
import Encabezado from '../components/Encabezado.vue'
import Insignia from '../components/Insignia.vue'
import Modal from '../components/Modal.vue'

const route = useRoute()
const reclamos = ref([])
const cargando = ref(true)
const filtroEstado = ref('')
const misReservas = ref([])
const huespedes = ref([])

const nuevo = ref(false)
const form = reactive({ tipo: 'QUEJA_DE_SERVICIO', asunto: '', descripcion: '', reservaId: '', huespedId: '' })
const errores = reactive({})
const enviando = ref(false)

const respondiendo = ref(null)
const respuesta = reactive({ estado: 'EN_PROCESO', respuesta: '' })
const erroresResp = reactive({})

async function cargar() {
  cargando.value = true
  try { reclamos.value = await get('/reclamos', { estado: filtroEstado.value }) }
  catch (e) { mostrarError(e) } finally { cargando.value = false }
}

onMounted(async () => {
  await cargar()
  try {
    misReservas.value = await get('/reservas')
    if (esPersonal.value) huespedes.value = await get('/huespedes')
  } catch { /* opcional */ }
  if (route.query.reservaId) {
    form.reservaId = Number(route.query.reservaId)
    if (route.query.huespedId) form.huespedId = Number(route.query.huespedId)
    nuevo.value = true
  }
})

async function enviar() {
  enviando.value = true
  Object.keys(errores).forEach(k => delete errores[k])
  try {
    await post('/reclamos', {
      tipo: form.tipo || null, asunto: form.asunto || null, descripcion: form.descripcion || null,
      reservaId: form.reservaId || null, huespedId: form.huespedId || null
    })
    toast.exito('Tu solicitud fue registrada. Te responderemos pronto.')
    nuevo.value = false
    Object.assign(form, { tipo: 'QUEJA_DE_SERVICIO', asunto: '', descripcion: '', reservaId: '', huespedId: '' })
    await cargar()
  } catch (e) { mostrarError(e, errores) } finally { enviando.value = false }
}

function abrirRespuesta(c) {
  respondiendo.value = c
  Object.assign(respuesta, { estado: c.estado === 'ABIERTO' ? 'EN_PROCESO' : c.estado, respuesta: c.respuesta || '' })
  Object.keys(erroresResp).forEach(k => delete erroresResp[k])
}

async function responder() {
  try {
    await put(`/reclamos/${respondiendo.value.id}/respuesta`, { estado: respuesta.estado || null, respuesta: respuesta.respuesta || null })
    toast.exito('Respuesta registrada.')
    respondiendo.value = null
    await cargar()
  } catch (e) { mostrarError(e, erroresResp) }
}

async function eliminar(c) {
  if (!window.confirm('¿Eliminar este reclamo?')) return
  try { await del(`/reclamos/${c.id}`); toast.exito('Reclamo eliminado.'); await cargar() } catch (e) { mostrarError(e) }
}
</script>

<template>
  <div>
    <Encabezado titulo="Reclamos y solicitudes"
                :subtitulo="esPersonal ? 'Atiende los reclamos y solicitudes de los huéspedes.' : 'Envía reclamos, solicitudes o sugerencias y da seguimiento a las respuestas.'">
      <select v-model="filtroEstado" class="entrada w-44" @change="cargar">
        <option value="">Todos los estados</option>
        <option v-for="e in opciones(['ABIERTO', 'EN_PROCESO', 'RESUELTO', 'CERRADO'])" :key="e.valor" :value="e.valor">{{ e.texto }}</option>
      </select>
      <button class="btn-primario" @click="nuevo = true">+ Nuevo</button>
    </Encabezado>

    <Cargando v-if="cargando" />
    <div v-else-if="!reclamos.length" class="tarjeta p-10 text-center text-sm text-slate-500">No hay reclamos registrados.</div>
    <div v-else class="space-y-3">
      <article v-for="c in reclamos" :key="c.id" class="tarjeta p-5">
        <div class="flex flex-wrap items-start justify-between gap-2">
          <div>
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ etiqueta(c.tipo) }}</span>
              <Insignia :valor="c.estado" />
            </div>
            <h3 class="mt-1 font-semibold text-slate-900">{{ c.asunto }}</h3>
            <p class="text-xs text-slate-500">
              {{ c.nombreHuesped }} · {{ fechaHora(c.fechaReclamo) }}
              <template v-if="c.codigoReserva"> · Reserva <RouterLink :to="`/reservas/${c.reservaId}`" class="font-mono text-marca-700 hover:underline">{{ c.codigoReserva }}</RouterLink></template>
            </p>
          </div>
          <div v-if="esPersonal" class="flex gap-1">
            <button class="btn-suave btn-sm" @click="abrirRespuesta(c)">Responder</button>
            <button v-if="esAdmin" class="btn btn-sm bg-rose-50 text-rose-700 hover:bg-rose-100" @click="eliminar(c)">Eliminar</button>
          </div>
        </div>
        <p class="mt-3 whitespace-pre-line text-sm text-slate-700">{{ c.descripcion }}</p>
        <div v-if="c.respuesta" class="mt-3 rounded-xl bg-slate-50 p-3 text-sm">
          <p class="text-xs font-semibold text-slate-500">Respuesta de {{ c.atendidoPor }} · {{ fechaHora(c.fechaRespuesta) }}</p>
          <p class="mt-1 whitespace-pre-line text-slate-700">{{ c.respuesta }}</p>
        </div>
      </article>
    </div>

    <Modal :abierto="nuevo" titulo="Nuevo reclamo o solicitud" @cerrar="nuevo = false">
      <form id="form-reclamo" class="grid grid-cols-1 gap-4 sm:grid-cols-2" novalidate @submit.prevent="enviar">
        <Campo v-model="form.tipo" :campo="{ nombre: 'tipo', etiqueta: 'Tipo', tipo: 'select', requerido: true, opciones: opciones(['QUEJA_DE_SERVICIO', 'SOLICITUD', 'SUGERENCIA']) }" :error="errores.tipo" />
        <Campo v-model="form.reservaId" :campo="{ nombre: 'reservaId', etiqueta: 'Reserva relacionada', tipo: 'select', placeholder: 'Ninguna', opciones: misReservas.map(r => ({ valor: r.id, texto: `${r.codigo} · ${r.huesped.nombreHuesped} · ${r.nombreHotel}` })) }" :error="errores.reservaId" />
        <Campo v-if="esPersonal" v-model="form.huespedId" class="sm:col-span-2" :campo="{ nombre: 'huespedId', etiqueta: 'Huésped', tipo: 'select', requerido: true, opciones: huespedes.map(h => ({ valor: h.id, texto: `${h.nombreHuesped} (${h.documento})` })) }" :error="errores.huespedId" />
        <Campo v-model="form.asunto" class="sm:col-span-2" :campo="{ nombre: 'asunto', etiqueta: 'Asunto', requerido: true }" :error="errores.asunto" />
        <Campo v-model="form.descripcion" class="sm:col-span-2" :campo="{ nombre: 'descripcion', etiqueta: 'Descripción', tipo: 'textarea', requerido: true, ayuda: 'Mínimo 10 caracteres.' }" :error="errores.descripcion" />
      </form>
      <template #pie>
        <button class="btn-secundario" @click="nuevo = false">Cancelar</button>
        <button class="btn-primario" type="submit" form="form-reclamo" :disabled="enviando">{{ enviando ? 'Enviando...' : 'Enviar' }}</button>
      </template>
    </Modal>

    <Modal :abierto="!!respondiendo" titulo="Responder reclamo" @cerrar="respondiendo = null">
      <template v-if="respondiendo">
        <p class="mb-4 text-sm text-slate-600"><b>{{ respondiendo.asunto }}</b> — {{ respondiendo.descripcion }}</p>
        <div class="space-y-4">
          <Campo v-model="respuesta.estado" :campo="{ nombre: 'estado', etiqueta: 'Nuevo estado', tipo: 'select', requerido: true, opciones: opciones(['EN_PROCESO', 'RESUELTO', 'CERRADO']) }" :error="erroresResp.estado" />
          <Campo v-model="respuesta.respuesta" :campo="{ nombre: 'respuesta', etiqueta: 'Respuesta', tipo: 'textarea', requerido: true }" :error="erroresResp.respuesta" />
        </div>
      </template>
      <template #pie>
        <button class="btn-secundario" @click="respondiendo = null">Cancelar</button>
        <button class="btn-primario" @click="responder">Guardar respuesta</button>
      </template>
    </Modal>
  </div>
</template>
