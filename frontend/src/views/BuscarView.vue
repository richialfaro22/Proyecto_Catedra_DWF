<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get, mostrarError } from '../api/http'
import { flujoReserva } from '../stores/reserva'
import { dinero, hoyISO, sumarDias } from '../utils/formato'
import Cargando from '../components/Cargando.vue'

const router = useRouter()
const c = flujoReserva.criterios
if (!c.fechaEntrada) {
  c.fechaEntrada = hoyISO(1)
  c.fechaSalida = hoyISO(3)
}
const ciudades = ref([])
const tipos = ref([])
const errores = reactive({})
const buscando = ref(false)

onMounted(async () => {
  try {
    [ciudades.value, tipos.value] = await Promise.all([get('/hoteles/ciudades'), get('/tipos-habitacion', { soloActivos: true })])
  } catch (e) { mostrarError(e) }
})

function ajustarSalida() {
  if (c.fechaEntrada && (!c.fechaSalida || c.fechaSalida <= c.fechaEntrada)) c.fechaSalida = sumarDias(c.fechaEntrada, 1)
}

async function buscar() {
  Object.keys(errores).forEach(k => delete errores[k])
  buscando.value = true
  try {
    flujoReserva.resultados = await get('/disponibilidad', {
      ciudad: c.ciudad, fechaEntrada: c.fechaEntrada, fechaSalida: c.fechaSalida,
      huespedes: c.huespedes, tipoId: c.tipoId
    })
    flujoReserva.buscado = true
  } catch (e) {
    flujoReserva.resultados = []
    mostrarError(e, errores)
  } finally {
    buscando.value = false
  }
}

const seleccionHab = reactive({})
const clave = (o) => o.hotelId + '-' + o.tipoId

/** Habitación elegida en la opción (por defecto la de menor precio). */
function habitacionDe(o) {
  return o.habitaciones.find(h => h.id === seleccionHab[clave(o)]) || o.habitaciones[0]
}

function elegir(o) {
  const h = habitacionDe(o)
  flujoReserva.opcion = { ...o, habitacionId: h.id, numeroHabitacion: h.numeroHabitacion, precioTotal: h.precioTotal,
    precioPorNoche: (Number(h.precioTotal) / o.noches).toFixed(2) }
  router.push({ name: 'reservar' })
}
</script>

<template>
  <div>
    <section class="rounded-3xl bg-gradient-to-br from-marca-700 to-marca-800 p-6 text-white shadow-lg sm:p-8">
      <p class="text-xs font-semibold uppercase tracking-widest text-marca-100">Empecemos tu viaje</p>
      <h1 class="mt-1 text-2xl font-bold sm:text-3xl">¿A dónde te gustaría ir?</h1>
      <p class="mt-1 text-marca-100">Cuéntanos qué estás buscando y te mostramos las opciones disponibles.</p>

      <form class="mt-6 grid grid-cols-1 gap-3 rounded-2xl bg-white p-4 text-slate-800 sm:grid-cols-2 lg:grid-cols-6" novalidate @submit.prevent="buscar">
        <div class="lg:col-span-2">
          <label class="etiqueta">Ciudad o ubicación</label>
          <input v-model="c.ciudad" list="lista-ciudades" class="entrada" placeholder="Todas las ciudades" />
          <datalist id="lista-ciudades"><option v-for="ci in ciudades" :key="ci" :value="ci" /></datalist>
        </div>
        <div>
          <label class="etiqueta requerido">Entrada</label>
          <input v-model="c.fechaEntrada" type="date" :min="hoyISO()" class="entrada" :class="{ 'entrada-error': errores.fechaEntrada }" @change="ajustarSalida" />
          <p v-if="errores.fechaEntrada" class="texto-error">{{ errores.fechaEntrada }}</p>
        </div>
        <div>
          <label class="etiqueta requerido">Salida</label>
          <input v-model="c.fechaSalida" type="date" :min="c.fechaEntrada" class="entrada" :class="{ 'entrada-error': errores.fechaSalida }" />
          <p v-if="errores.fechaSalida" class="texto-error">{{ errores.fechaSalida }}</p>
        </div>
        <div>
          <label class="etiqueta requerido">Huéspedes</label>
          <input v-model.number="c.huespedes" type="number" min="1" max="10" class="entrada" />
        </div>
        <div>
          <label class="etiqueta">Tipo (opcional)</label>
          <select v-model="c.tipoId" class="entrada">
            <option value="">Cualquiera</option>
            <option v-for="t in tipos" :key="t.id" :value="t.id">{{ t.nombre }}</option>
          </select>
        </div>
        <div class="sm:col-span-2 lg:col-span-6">
          <button class="btn-primario w-full sm:w-auto" :disabled="buscando">{{ buscando ? 'Buscando...' : 'Buscar disponibilidad' }}</button>
        </div>
      </form>
    </section>

    <Cargando v-if="buscando">Buscando habitaciones disponibles...</Cargando>

    <section v-else-if="flujoReserva.buscado" class="mt-8">
      <h2 class="mb-4 text-lg font-semibold text-slate-800">
        {{ flujoReserva.resultados.length }} opción(es) disponible(s)
      </h2>
      <div v-if="!flujoReserva.resultados.length" class="tarjeta p-10 text-center text-slate-500">
        No hay habitaciones disponibles con esos criterios. Prueba con otras fechas, otra ciudad o menos huéspedes.
      </div>

      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <article v-for="o in flujoReserva.resultados" :key="clave(o)" class="tarjeta flex flex-col p-5">
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm text-amber-500">{{ '★'.repeat(o.estrellas) }}</p>
              <h3 class="text-lg font-semibold text-slate-900">{{ o.nombreHotel }}</h3>
              <p class="text-sm text-slate-500">{{ o.ciudad }} · {{ o.direccion }}</p>
            </div>
            <span class="rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700">
              {{ o.habitacionesDisponibles }} disponible(s)
            </span>
          </div>

          <div class="mt-4 rounded-xl bg-slate-50 p-4">
            <p class="font-semibold text-slate-800">Habitación {{ o.tipoHabitacion }} <span class="font-normal text-slate-500">· capacidad {{ o.cantidadPersonas }}</span></p>
            <p v-if="o.descripcionTipo" class="mt-1 text-sm text-slate-600">{{ o.descripcionTipo }}</p>
            <div class="mt-3 flex flex-wrap gap-1.5">
              <span v-for="s in o.serviciosIncluidos" :key="s" class="rounded-full bg-white px-2 py-0.5 text-xs text-slate-600 ring-1 ring-slate-200">{{ s }}</span>
            </div>
          </div>

          <p class="mt-3 text-xs text-slate-500">
            Cancelación gratuita hasta {{ o.horasCancelacionGratis }} h antes de la entrada; después se cobra el {{ o.porcentajePenalizacion }}%.
          </p>

          <div class="mt-auto flex flex-wrap items-end justify-between gap-3 pt-4">
            <div>
              <p class="text-sm text-slate-500">{{ dinero(Number(habitacionDe(o).precioTotal) / o.noches) }} / noche · {{ o.noches }} noche(s)</p>
              <p class="text-2xl font-bold text-slate-900">{{ dinero(habitacionDe(o).precioTotal) }} <span class="text-sm font-normal text-slate-500">total</span></p>
            </div>
            <div class="flex items-center gap-2">
              <select v-model="seleccionHab[clave(o)]" class="entrada w-40 py-1.5" title="Habitación">
                <option :value="undefined">Hab. {{ o.habitaciones[0].numeroHabitacion }} (piso {{ o.habitaciones[0].numPiso }})</option>
                <option v-for="h in o.habitaciones.slice(1)" :key="h.id" :value="h.id">Hab. {{ h.numeroHabitacion }} (piso {{ h.numPiso }})</option>
              </select>
              <button class="btn-primario" @click="elegir(o)">Seleccionar</button>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
