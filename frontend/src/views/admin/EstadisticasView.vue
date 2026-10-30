<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { get, mostrarError } from '../../api/http'
import { dinero, etiqueta, hoyISO } from '../../utils/formato'
import Encabezado from '../../components/Encabezado.vue'
import Cargando from '../../components/Cargando.vue'
import Insignia from '../../components/Insignia.vue'

const hoy = hoyISO()
const filtro = reactive({ desde: hoy.substring(0, 8) + '01', hasta: '' })
const datos = ref(null)
const cargando = ref(false)

async function cargar() {
  cargando.value = true
  try {
    datos.value = await get('/estadisticas', { desde: filtro.desde, hasta: filtro.hasta })
    filtro.hasta = datos.value.hasta
  } catch (e) {
    mostrarError(e)
  } finally {
    cargando.value = false
  }
}
onMounted(cargar)

const tarjetas = computed(() => {
  const d = datos.value
  if (!d) return []
  return [
    { titulo: 'Ocupación', valor: `${d.ocupacionPorcentaje}%`, detalle: `${d.totalHabitaciones} habitaciones operativas` },
    { titulo: 'Reservaciones', valor: d.reservasCreadas, detalle: 'creadas en el período' },
    { titulo: 'Cancelaciones', valor: d.reservasCanceladas, detalle: `${d.tasaCancelacion}% de las reservaciones` },
    { titulo: 'Ingresos netos', valor: dinero(d.ingresosNetos), detalle: `${dinero(d.ingresosBrutos)} cobrados − ${dinero(d.reembolsos)} reembolsados` }
  ]
})

const maxMes = computed(() => Math.max(1, ...(datos.value?.ingresosPorMes || []).map(m => Number(m.ingresos))))
const totalEstados = computed(() => Math.max(1, ...Object.values(datos.value?.reservasPorEstado || {})))
const nombreMes = (m) => {
  const [a, mes] = m.split('-').map(Number)
  return new Date(a, mes - 1, 1).toLocaleDateString('es-SV', { month: 'short', year: '2-digit' })
}
</script>

<template>
  <div>
    <Encabezado titulo="Cómo va tu hotel" subtitulo="Ocupación, reservaciones, cancelaciones e ingresos del período.">
      <form class="flex flex-wrap items-end gap-2" @submit.prevent="cargar">
        <div><label class="etiqueta text-xs">Desde</label><input v-model="filtro.desde" type="date" class="entrada" /></div>
        <div><label class="etiqueta text-xs">Hasta</label><input v-model="filtro.hasta" type="date" class="entrada" /></div>
        <button class="btn-primario" :disabled="cargando">Consultar</button>
      </form>
    </Encabezado>

    <Cargando v-if="cargando && !datos" />
    <template v-else-if="datos">
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div v-for="t in tarjetas" :key="t.titulo" class="tarjeta p-5">
          <p class="text-sm font-medium text-slate-500">{{ t.titulo }}</p>
          <p class="mt-2 text-3xl font-bold text-slate-900">{{ t.valor }}</p>
          <p class="mt-1 text-xs text-slate-500">{{ t.detalle }}</p>
        </div>
      </div>

      <div class="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <section class="tarjeta p-5">
          <h2 class="mb-4 font-semibold text-slate-800">Ingresos netos por mes</h2>
          <div class="flex h-48 items-end gap-3">
            <div v-for="m in datos.ingresosPorMes" :key="m.mes" class="flex flex-1 flex-col items-center gap-1">
              <span class="text-[11px] text-slate-500">{{ dinero(m.ingresos) }}</span>
              <div class="w-full rounded-t-md bg-marca-500" :style="{ height: Math.max(2, (Number(m.ingresos) / maxMes) * 140) + 'px' }"
                   :title="`${m.reservas} reservas`"></div>
              <span class="text-xs text-slate-600">{{ nombreMes(m.mes) }}</span>
            </div>
          </div>
        </section>

        <section class="tarjeta p-5">
          <h2 class="mb-4 font-semibold text-slate-800">Reservaciones por estado</h2>
          <div class="space-y-3">
            <div v-for="(n, estado) in datos.reservasPorEstado" :key="estado">
              <div class="mb-1 flex justify-between text-sm"><Insignia :valor="estado" /><span class="font-semibold">{{ n }}</span></div>
              <div class="h-2 rounded-full bg-slate-100">
                <div class="h-2 rounded-full bg-marca-500" :style="{ width: (n / totalEstados * 100) + '%' }"></div>
              </div>
            </div>
          </div>
          <h3 class="mb-2 mt-6 text-sm font-semibold text-slate-700">Reclamos y solicitudes (total)</h3>
          <div class="flex flex-wrap gap-3 text-sm">
            <span v-for="(n, estado) in datos.reclamosPorEstado" :key="estado" class="flex items-center gap-1">
              <Insignia :valor="estado" /> {{ n }}
            </span>
          </div>
        </section>
      </div>

      <section class="tarjeta mt-6 overflow-hidden">
        <h2 class="px-5 pt-5 font-semibold text-slate-800">Detalle por hotel</h2>
        <div class="overflow-x-auto p-2">
          <table class="tabla">
            <thead>
              <tr><th>Hotel</th><th>Habitaciones</th><th>Noches ocupadas</th><th>Ocupación</th><th>Reservas</th><th>Cancelaciones</th><th>Ingresos netos</th></tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="h in datos.porHotel" :key="h.hotelId">
                <td class="font-medium">{{ h.nombreHotel }}</td>
                <td>{{ h.habitaciones }}</td>
                <td>{{ h.nochesOcupadas }}</td>
                <td>
                  <div class="flex items-center gap-2">
                    <div class="h-2 w-24 rounded-full bg-slate-100"><div class="h-2 rounded-full bg-marca-500" :style="{ width: Math.min(100, h.ocupacionPorcentaje) + '%' }"></div></div>
                    {{ h.ocupacionPorcentaje }}%
                  </div>
                </td>
                <td>{{ h.reservas }}</td>
                <td>{{ h.cancelaciones }}</td>
                <td>{{ dinero(h.ingresos) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      <p class="mt-3 text-xs text-slate-500">Huéspedes registrados: {{ datos.totalHuespedes }} · Hoteles: {{ datos.totalHoteles }}</p>
    </template>
  </div>
</template>
