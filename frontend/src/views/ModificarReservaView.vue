<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get, put, mostrarError } from '../api/http'
import { toast } from '../stores/toast'
import { dinero, etiqueta, hoyISO, sumarDias } from '../utils/formato'
import Campo from '../components/Campo.vue'
import Cargando from '../components/Cargando.vue'
import Encabezado from '../components/Encabezado.vue'

const props = defineProps({ id: String })
const router = useRouter()
const r = ref(null)
const servicios = ref([])
const seleccion = reactive({})
const form = reactive({ fechaEntrada: '', fechaSalida: '', numPersonas: 1, observaciones: '' })
const errores = reactive({})
const guardando = ref(false)

onMounted(async () => {
  try {
    const [reserva, lista] = await Promise.all([get(`/reservas/${props.id}`), get('/servicios', { soloActivos: true })])
    r.value = reserva
    servicios.value = lista
    Object.assign(form, {
      fechaEntrada: reserva.fechaEntrada, fechaSalida: reserva.fechaSalida,
      numPersonas: reserva.numPersonas, observaciones: reserva.observaciones || ''
    })
    reserva.servicios.forEach(s => { seleccion[s.servicioId] = s.cantidad })
  } catch (e) { mostrarError(e) }
})

const noches = computed(() => {
  if (!form.fechaEntrada || !form.fechaSalida) return 0
  return Math.max(0, (new Date(form.fechaSalida) - new Date(form.fechaEntrada)) / 86400000)
})

function ajustarSalida() {
  if (form.fechaEntrada && form.fechaSalida <= form.fechaEntrada) form.fechaSalida = sumarDias(form.fechaEntrada, 1)
}

async function guardar() {
  guardando.value = true
  Object.keys(errores).forEach(k => delete errores[k])
  try {
    const actualizada = await put(`/reservas/${props.id}`, {
      fechaEntrada: form.fechaEntrada || null,
      fechaSalida: form.fechaSalida || null,
      numPersonas: form.numPersonas === '' ? null : form.numPersonas,
      observaciones: form.observaciones || null,
      servicios: Object.entries(seleccion).filter(([, c]) => c > 0).map(([id, c]) => ({ servicioId: Number(id), cantidad: c }))
    })
    if (actualizada.estadoReserva === 'PENDIENTE' && Number(actualizada.saldoPendiente) > 0) {
      toast.info(`Reserva modificada. Nuevo total ${dinero(actualizada.total)}; tienes un saldo de ${dinero(actualizada.saldoPendiente)}.`)
      router.push(`/reservas/${props.id}/pago`)
    } else {
      toast.exito('Reserva modificada correctamente.')
      router.push(`/reservas/${props.id}`)
    }
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    guardando.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-4xl">
    <Encabezado titulo="Modificar reserva" :subtitulo="r ? `${r.codigo} · ${r.nombreHotel} · ${r.tipoHabitacion} #${r.numeroHabitacion}` : ''">
      <RouterLink :to="`/reservas/${id}`" class="btn-secundario">← Volver</RouterLink>
    </Encabezado>
    <Cargando v-if="!r" />
    <form v-else class="space-y-6" novalidate @submit.prevent="guardar">
      <section class="tarjeta p-5">
        <p class="mb-4 rounded-lg bg-sky-50 p-3 text-sm text-sky-800">
          Si el nuevo total es menor a lo que ya pagaste, se te reembolsará la diferencia. Si es mayor, deberás pagar el saldo para mantener la reserva confirmada.
        </p>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <Campo v-model="form.fechaEntrada" :campo="{ nombre: 'fechaEntrada', etiqueta: 'Entrada', tipo: 'date', min: hoyISO(), requerido: true }" :error="errores.fechaEntrada" @change="ajustarSalida" />
          <Campo v-model="form.fechaSalida" :campo="{ nombre: 'fechaSalida', etiqueta: 'Salida', tipo: 'date', min: form.fechaEntrada, requerido: true, ayuda: `${noches} noche(s)` }" :error="errores.fechaSalida" />
          <Campo v-model="form.numPersonas" :campo="{ nombre: 'numPersonas', etiqueta: 'Huéspedes', tipo: 'number', min: 1, max: r.cantidadPersonas, requerido: true, ayuda: `Máximo ${r.cantidadPersonas}` }" :error="errores.numPersonas" />
          <Campo v-model="form.observaciones" class="sm:col-span-3" :campo="{ nombre: 'observaciones', etiqueta: 'Solicitudes especiales', tipo: 'textarea' }" :error="errores.observaciones" />
        </div>
      </section>
      <section class="tarjeta p-5">
        <h2 class="mb-3 font-semibold text-slate-800">Servicios adicionales</h2>
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <label v-for="s in servicios" :key="s.id" class="flex items-center gap-3 rounded-xl border p-3"
                 :class="seleccion[s.id] ? 'border-marca-500 bg-marca-50' : 'border-slate-200'">
            <div class="flex-1">
              <p class="text-sm font-medium">{{ s.nombreServicio }}</p>
              <p class="text-xs text-slate-500">{{ etiqueta(s.categoria) }} · {{ dinero(s.precio) }} {{ s.tipoCobro === 'POR_NOCHE' ? 'por noche' : 'por estadía' }}</p>
            </div>
            <input v-model.number="seleccion[s.id]" type="number" min="0" max="20" class="entrada w-20" placeholder="0" />
          </label>
        </div>
      </section>
      <div class="flex justify-end">
        <button class="btn-primario" :disabled="guardando">{{ guardando ? 'Guardando...' : 'Guardar cambios' }}</button>
      </div>
    </form>
  </div>
</template>
