<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get, mostrarError } from '../api/http'
import { esPersonal } from '../stores/auth'
import { dinero, fecha, opciones } from '../utils/formato'
import Cargando from '../components/Cargando.vue'
import Encabezado from '../components/Encabezado.vue'
import Insignia from '../components/Insignia.vue'

const router = useRouter()
const reservas = ref([])
const hoteles = ref([])
const cargando = ref(true)
const filtro = reactive({ estado: '', hotelId: '' })
const codigo = ref('')
const estados = opciones(['PENDIENTE', 'CONFIRMADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA'])

async function cargar() {
  cargando.value = true
  try {
    reservas.value = await get('/reservas', filtro)
  } catch (e) { mostrarError(e) } finally { cargando.value = false }
}

async function buscarCodigo() {
  if (!codigo.value.trim()) return
  try {
    const r = await get(`/reservas/codigo/${encodeURIComponent(codigo.value.trim())}`)
    router.push(`/reservas/${r.id}`)
  } catch (e) { mostrarError(e) }
}

onMounted(async () => {
  await cargar()
  if (esPersonal.value) {
    try { hoteles.value = await get('/hoteles') } catch { /* opcional */ }
  }
})
</script>

<template>
  <div>
    <Encabezado :titulo="esPersonal ? 'Reservas' : 'Mis reservas'"
                :subtitulo="esPersonal ? 'Todas las reservas del sistema.' : 'Consulta, paga, modifica o cancela tus reservas.'">
      <RouterLink to="/" class="btn-primario">+ Nueva reserva</RouterLink>
    </Encabezado>

    <div class="tarjeta mb-4 flex flex-wrap items-end gap-3 p-4">
      <div>
        <label class="etiqueta text-xs">Estado</label>
        <select v-model="filtro.estado" class="entrada" @change="cargar">
          <option value="">Todos</option>
          <option v-for="e in estados" :key="e.valor" :value="e.valor">{{ e.texto }}</option>
        </select>
      </div>
      <div v-if="esPersonal">
        <label class="etiqueta text-xs">Hotel</label>
        <select v-model="filtro.hotelId" class="entrada" @change="cargar">
          <option value="">Todos</option>
          <option v-for="h in hoteles" :key="h.id" :value="h.id">{{ h.nombreHotel }}</option>
        </select>
      </div>
      <form class="ml-auto flex gap-2" @submit.prevent="buscarCodigo">
        <input v-model="codigo" class="entrada w-48 font-mono uppercase" placeholder="Código de reserva" />
        <button class="btn-secundario">Buscar</button>
      </form>
    </div>

    <div class="tarjeta overflow-hidden">
      <Cargando v-if="cargando" />
      <div v-else-if="!reservas.length" class="p-10 text-center text-sm text-slate-500">
        No hay reservas para mostrar. <RouterLink to="/" class="text-marca-700 hover:underline">Haz tu primera reserva</RouterLink>.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="tabla">
          <thead>
            <tr><th>Código</th><th>Huésped</th><th>Hotel / habitación</th><th>Fechas</th><th>Total</th><th>Saldo</th><th>Estado</th><th></th></tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="r in reservas" :key="r.id" class="cursor-pointer" @click="router.push(`/reservas/${r.id}`)">
              <td class="font-mono font-semibold">{{ r.codigo }}</td>
              <td>{{ r.huesped.nombreHuesped }}</td>
              <td>{{ r.nombreHotel }}<br /><span class="text-xs text-slate-500">{{ r.tipoHabitacion }} #{{ r.numeroHabitacion }}</span></td>
              <td class="whitespace-nowrap">{{ fecha(r.fechaEntrada) }} → {{ fecha(r.fechaSalida) }}<br /><span class="text-xs text-slate-500">{{ r.noches }} noche(s)</span></td>
              <td>{{ dinero(r.total) }}</td>
              <td :class="Number(r.saldoPendiente) > 0 ? 'font-semibold text-amber-700' : 'text-slate-400'">{{ dinero(r.saldoPendiente) }}</td>
              <td><Insignia :valor="r.estadoReserva" /></td>
              <td class="text-right"><span class="text-marca-700">Ver →</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
