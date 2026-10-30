<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { get, post, patch, mostrarError } from '../api/http'
import { esPersonal } from '../stores/auth'
import { toast } from '../stores/toast'
import { dinero, etiqueta, fechaHora, hoyISO } from '../utils/formato'
import ResumenReserva from '../components/ResumenReserva.vue'
import Cargando from '../components/Cargando.vue'
import Encabezado from '../components/Encabezado.vue'
import Modal from '../components/Modal.vue'
import Insignia from '../components/Insignia.vue'
import Campo from '../components/Campo.vue'

const props = defineProps({ id: String })
const route = useRoute()
const r = ref(null)
const cotizacion = ref(null)
const motivo = ref('')
const errores = reactive({})
const procesando = ref(false)

async function cargar() {
  try { r.value = await get(`/reservas/${props.id}`) } catch (e) { mostrarError(e) }
}
onMounted(cargar)

const activa = computed(() => r.value && ['PENDIENTE', 'CONFIRMADA'].includes(r.value.estadoReserva))
const modificable = computed(() => activa.value && r.value.fechaEntrada > hoyISO())

async function abrirCancelacion() {
  try {
    cotizacion.value = await get(`/reservas/${props.id}/cancelacion`)
    motivo.value = ''
    Object.keys(errores).forEach(k => delete errores[k])
  } catch (e) { mostrarError(e) }
}

async function cancelar() {
  procesando.value = true
  try {
    r.value = await post(`/reservas/${props.id}/cancelar`, { motivo: motivo.value || null })
    cotizacion.value = null
    toast.exito('La reserva fue cancelada.' + (Number(r.value.totalReembolsado) > 0 ? ` Reembolso: ${dinero(r.value.totalReembolsado)}.` : ''))
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    procesando.value = false
  }
}

async function cambiarEstado(accion, mensaje) {
  try {
    r.value = await patch(`/reservas/${props.id}/${accion}`)
    toast.exito(mensaje)
  } catch (e) { mostrarError(e) }
}
</script>

<template>
  <div>
    <Encabezado titulo="Gestiona tu reserva" subtitulo="¿Cambio de planes? Puedes modificar las fechas o cancelar según la política del hotel.">
      <RouterLink to="/reservas" class="btn-secundario">← Reservas</RouterLink>
    </Encabezado>
    <Cargando v-if="!r" />
    <template v-else>
      <div v-if="route.query.confirmada && r.estadoReserva === 'CONFIRMADA'" class="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 p-5 text-emerald-800">
        <p class="text-lg font-semibold">¡Reserva confirmada!</p>
        <p class="text-sm">Tu pago fue registrado. Guarda tu código <b class="font-mono">{{ r.codigo }}</b> para cualquier consulta.</p>
      </div>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div class="lg:col-span-2"><ResumenReserva :r="r" /></div>

        <aside class="space-y-4">
          <div class="tarjeta space-y-2 p-5">
            <h2 class="mb-2 font-semibold text-slate-800">Acciones</h2>
            <RouterLink v-if="r.estadoReserva === 'PENDIENTE'" :to="`/reservas/${r.id}/pago`" class="btn-primario w-full">Pagar {{ dinero(r.saldoPendiente) }}</RouterLink>
            <RouterLink v-if="modificable" :to="`/reservas/${r.id}/modificar`" class="btn-secundario w-full">Modificar reserva</RouterLink>
            <button v-if="activa" class="btn w-full bg-rose-50 text-rose-700 hover:bg-rose-100" @click="abrirCancelacion">Cancelar reserva</button>
            <template v-if="esPersonal">
              <button v-if="r.estadoReserva === 'CONFIRMADA'" class="btn-suave w-full" @click="cambiarEstado('check-in', 'Check-in registrado.')">Registrar check-in</button>
              <button v-if="r.estadoReserva === 'EN_CURSO'" class="btn-suave w-full" @click="cambiarEstado('check-out', 'Check-out registrado.')">Registrar check-out</button>
            </template>
            <RouterLink :to="{ path: '/reclamos', query: { reservaId: r.id, huespedId: r.huesped.id } }" class="btn-secundario w-full">Enviar reclamo o solicitud</RouterLink>
            <p class="pt-2 text-xs text-slate-500">Creada {{ fechaHora(r.fechaReserva) }} por {{ r.creadaPor }}</p>
          </div>

          <div class="tarjeta p-5">
            <h2 class="mb-3 font-semibold text-slate-800">Movimientos</h2>
            <p v-if="!r.pagos.length" class="text-sm text-slate-500">Aún no hay pagos registrados.</p>
            <ul class="space-y-3">
              <li v-for="p in r.pagos" :key="p.id" class="text-sm">
                <div class="flex items-center justify-between">
                  <Insignia :valor="p.estado" />
                  <b :class="p.estado === 'REEMBOLSADO' ? 'text-sky-700' : ''">{{ p.estado === 'REEMBOLSADO' ? '− ' : '' }}{{ dinero(p.totalPagar) }}</b>
                </div>
                <p class="text-xs text-slate-500">{{ etiqueta(p.metodoPago) }}{{ p.ultimosDigitos ? ' •••• ' + p.ultimosDigitos : '' }} · {{ fechaHora(p.fechaPago) }} · {{ p.referencia }}</p>
                <p v-if="p.observacion" class="text-xs text-slate-500">{{ p.observacion }}</p>
              </li>
            </ul>
          </div>
        </aside>
      </div>
    </template>

    <Modal :abierto="!!cotizacion" titulo="Cancelar reserva" ancho="max-w-lg" @cerrar="cotizacion = null">
      <template v-if="cotizacion">
        <div class="rounded-xl p-4 text-sm" :class="cotizacion.dentroDePlazoGratis ? 'bg-emerald-50 text-emerald-800' : 'bg-amber-50 text-amber-900'">
          {{ cotizacion.mensaje }}
        </div>
        <dl class="mt-4 grid grid-cols-2 gap-2 text-sm">
          <dt class="text-slate-500">Total de la reserva</dt><dd class="text-right">{{ dinero(cotizacion.total) }}</dd>
          <dt class="text-slate-500">Pagado</dt><dd class="text-right">{{ dinero(cotizacion.totalPagado) }}</dd>
          <dt class="text-slate-500">Cargo por cancelación</dt><dd class="text-right text-rose-700">{{ dinero(cotizacion.cargo) }}</dd>
          <dt class="font-semibold">Reembolso</dt><dd class="text-right font-semibold text-emerald-700">{{ dinero(cotizacion.reembolso) }}</dd>
        </dl>
        <Campo v-model="motivo" class="mt-4" :campo="{ nombre: 'motivo', etiqueta: 'Motivo de la cancelación', tipo: 'textarea', requerido: true }" :error="errores.motivo" />
      </template>
      <template #pie>
        <button class="btn-secundario" @click="cotizacion = null">Volver</button>
        <button class="btn-peligro" :disabled="procesando" @click="cancelar">{{ procesando ? 'Cancelando...' : 'Confirmar cancelación' }}</button>
      </template>
    </Modal>
  </div>
</template>
