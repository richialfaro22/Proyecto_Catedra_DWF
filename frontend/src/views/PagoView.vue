<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, mostrarError } from '../api/http'
import { esPersonal } from '../stores/auth'
import { toast } from '../stores/toast'
import { dinero, etiqueta } from '../utils/formato'
import ResumenReserva from '../components/ResumenReserva.vue'
import Campo from '../components/Campo.vue'
import Cargando from '../components/Cargando.vue'
import Encabezado from '../components/Encabezado.vue'

const props = defineProps({ id: String })
const router = useRouter()
const reserva = ref(null)
const errores = reactive({})
const pagando = ref(false)
const form = reactive({ metodoPago: 'TARJETA', totalPagar: '', numeroTarjeta: '', titular: '', vencimiento: '', cvv: '', referenciaTransferencia: '' })

const metodos = computed(() => [
  { valor: 'TARJETA', texto: 'Tarjeta' },
  { valor: 'TRANSFERENCIA', texto: 'Transferencia' },
  { valor: 'EFECTIVO', texto: 'Efectivo', soloPersonal: true }
])
const esTarjeta = computed(() => form.metodoPago === 'TARJETA')

async function cargar() {
  try {
    reserva.value = await get(`/reservas/${props.id}`)
  } catch (e) {
    mostrarError(e)
    router.replace({ name: 'reservas' })
  }
}
onMounted(cargar)

async function pagar() {
  Object.keys(errores).forEach(k => delete errores[k])
  pagando.value = true
  const cuerpo = { metodoPago: form.metodoPago, totalPagar: form.totalPagar === '' ? null : form.totalPagar }
  if (esTarjeta.value) {
    Object.assign(cuerpo, {
      numeroTarjeta: form.numeroTarjeta.replace(/\s/g, ''), titular: form.titular,
      vencimiento: form.vencimiento, cvv: form.cvv
    })
  } else if (form.metodoPago === 'TRANSFERENCIA') {
    cuerpo.referenciaTransferencia = form.referenciaTransferencia
  }
  try {
    const p = await post(`/reservas/${props.id}/pagos`, cuerpo)
    if (p.estado === 'RECHAZADO') {
      toast.error(p.observacion || 'El pago fue rechazado. Intenta con otro método.')
      await cargar()
      return
    }
    toast.exito(`Pago aprobado por ${dinero(p.totalPagar)}. Referencia ${p.referencia}.`)
    await cargar()
    if (reserva.value.estadoReserva === 'CONFIRMADA') {
      router.push({ name: 'reserva', params: { id: props.id }, query: { confirmada: 1 } })
    }
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    pagando.value = false
  }
}
</script>

<template>
  <div>
    <Encabezado titulo="Revisa que todo esté correcto y confirma tu reserva" subtitulo="Pasos 4 y 5 · Verifica el resumen y realiza el pago." />
    <Cargando v-if="!reserva" />
    <div v-else class="grid grid-cols-1 gap-6 lg:grid-cols-5">
      <div class="lg:col-span-3"><ResumenReserva :r="reserva" /></div>

      <div class="lg:col-span-2">
        <div v-if="reserva.estadoReserva !== 'PENDIENTE'" class="tarjeta p-6 text-center">
          <p class="text-slate-600">Esta reserva no tiene pagos pendientes.</p>
          <RouterLink :to="`/reservas/${reserva.id}`" class="btn-primario mt-4">Ver reserva</RouterLink>
        </div>

        <form v-else class="tarjeta space-y-4 p-6" novalidate @submit.prevent="pagar">
          <div class="flex items-start justify-between rounded-xl bg-marca-50 p-4 text-slate-900">
            <div>
              <p class="text-sm text-slate-600">Monto a pagar</p>
              <p class="text-3xl font-bold">{{ dinero(reserva.saldoPendiente) }}</p>
            </div>
            <span class="rounded-full bg-amber-100 px-2.5 py-1 text-xs font-medium text-amber-800">Pago pendiente</span>
          </div>

          <div>
            <p class="etiqueta requerido">Método de pago</p>
            <div class="grid grid-cols-3 gap-2">
              <button v-for="m in metodos" :key="m.valor" type="button" :disabled="m.soloPersonal && !esPersonal"
                      :title="m.soloPersonal && !esPersonal ? 'El efectivo se paga en la recepción del hotel' : ''"
                      class="rounded-lg border px-3 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-40"
                      :class="form.metodoPago === m.valor ? 'border-marca-600 bg-marca-50 text-marca-700 ring-2 ring-marca-500/30' : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'"
                      @click="form.metodoPago = m.valor">{{ m.texto }}</button>
            </div>
            <p v-if="errores.metodoPago" class="texto-error">{{ errores.metodoPago }}</p>
          </div>

          <template v-if="esTarjeta">
            <Campo v-model="form.numeroTarjeta" :campo="{ nombre: 'numeroTarjeta', etiqueta: 'Número de tarjeta', requerido: true, placeholder: '4111 1111 1111 1111' }" :error="errores.numeroTarjeta" />
            <Campo v-model="form.titular" :campo="{ nombre: 'titular', etiqueta: 'Titular', requerido: true, placeholder: 'Como aparece en la tarjeta' }" :error="errores.titular" />
            <div class="grid grid-cols-2 gap-3">
              <Campo v-model="form.vencimiento" :campo="{ nombre: 'vencimiento', etiqueta: 'Vencimiento', requerido: true, placeholder: 'MM/AA' }" :error="errores.vencimiento" />
              <Campo v-model="form.cvv" :campo="{ nombre: 'cvv', etiqueta: 'CVV', tipo: 'password', requerido: true, placeholder: '123' }" :error="errores.cvv" />
            </div>
            <p class="text-xs text-slate-500">Pago simulado. Prueba: 4111 1111 1111 1111 (aprobada) · 4000 0000 0000 0002 (rechazada).</p>
          </template>

          <Campo v-if="form.metodoPago === 'TRANSFERENCIA'" v-model="form.referenciaTransferencia"
                 :campo="{ nombre: 'referenciaTransferencia', etiqueta: 'Referencia de la transferencia', requerido: true }"
                 :error="errores.referenciaTransferencia" />

          <Campo v-model="form.totalPagar" :campo="{ nombre: 'totalPagar', etiqueta: 'Monto (opcional)', tipo: 'number', step: '0.01', min: 0.01, ayuda: 'Déjalo vacío para pagar el saldo completo. La reserva se confirma al completar el pago.' }" :error="errores.totalPagar" />

          <button class="btn-primario w-full py-3" :disabled="pagando">{{ pagando ? 'Procesando pago...' : 'Confirmar y pagar' }}</button>
          <RouterLink :to="`/reservas/${reserva.id}`" class="block text-center text-sm text-slate-500 hover:underline">Pagar más tarde</RouterLink>
        </form>

        <div v-if="reserva.pagos.length" class="tarjeta mt-4 p-5">
          <h3 class="mb-2 text-sm font-semibold text-slate-700">Intentos de pago</h3>
          <ul class="space-y-1 text-sm">
            <li v-for="p in reserva.pagos" :key="p.id" class="flex justify-between">
              <span>{{ etiqueta(p.metodoPago) }} · {{ etiqueta(p.estado).toLowerCase() }}</span><span>{{ dinero(p.totalPagar) }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
