<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, mostrarError } from '../api/http'
import { esPersonal } from '../stores/auth'
import { flujoReserva } from '../stores/reserva'
import { toast } from '../stores/toast'
import { dinero, etiqueta, fecha } from '../utils/formato'
import { camposHuesped, huespedDesde, huespedVacio, limpiarVacios } from '../utils/huesped'
import Campo from '../components/Campo.vue'
import Encabezado from '../components/Encabezado.vue'

const router = useRouter()
const opcion = flujoReserva.opcion
const criterios = flujoReserva.criterios

const huesped = reactive(huespedVacio())
const solicitudes = ref('')
const numPersonas = ref(criterios.huespedes || 1)
const servicios = ref([])
const seleccion = reactive({})
const errores = reactive({})
const enviando = ref(false)

// Búsqueda de huésped existente (solo personal)
const q = ref('')
const encontrados = ref([])

onMounted(async () => {
  if (!opcion) {
    router.replace({ name: 'buscar' })
    return
  }
  try {
    servicios.value = await get('/servicios', { soloActivos: true })
    if (!esPersonal.value) {
      const perfil = await get('/auth/perfil')
      if (perfil.huesped) Object.assign(huesped, huespedDesde(perfil.huesped))
    }
  } catch (e) { mostrarError(e) }
})

async function buscarHuesped() {
  try {
    encontrados.value = await get('/huespedes', { q: q.value })
    if (!encontrados.value.length) toast.info('No se encontraron huéspedes; completa los datos para registrarlo.')
  } catch (e) { mostrarError(e) }
}

function usarHuesped(h) {
  Object.assign(huesped, huespedDesde(h))
  encontrados.value = []
  q.value = ''
}

const totalServicios = computed(() => servicios.value.reduce((t, s) => {
  const cant = seleccion[s.id] || 0
  if (!cant) return t
  return t + s.precio * cant * (s.tipoCobro === 'POR_NOCHE' ? opcion.noches : 1)
}, 0))

async function reservar() {
  Object.keys(errores).forEach(k => delete errores[k])
  enviando.value = true
  try {
    const r = await post('/reservas', {
      habitacionId: opcion.habitacionId,
      fechaEntrada: criterios.fechaEntrada,
      fechaSalida: criterios.fechaSalida,
      numPersonas: numPersonas.value === '' ? null : numPersonas.value,
      observaciones: solicitudes.value || null,
      huesped: limpiarVacios(huesped),
      servicios: Object.entries(seleccion).filter(([, c]) => c > 0).map(([id, c]) => ({ servicioId: Number(id), cantidad: c }))
    })
    toast.exito(`Reserva ${r.codigo} generada. Revisa el resumen y realiza el pago.`)
    flujoReserva.opcion = null
    flujoReserva.buscado = false
    router.push({ name: 'pago', params: { id: r.id } })
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div v-if="opcion">
    <Encabezado titulo="Cuéntanos quién se hospeda" subtitulo="Paso 3 de 5 · Completa los datos del huésped y elige servicios adicionales.">
      <RouterLink to="/" class="btn-secundario">← Cambiar habitación</RouterLink>
    </Encabezado>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <form class="space-y-6 lg:col-span-2" novalidate @submit.prevent="reservar">
        <section v-if="esPersonal" class="tarjeta p-5">
          <h2 class="mb-3 font-semibold text-slate-800">Buscar huésped registrado</h2>
          <div class="flex gap-2">
            <input v-model="q" class="entrada" placeholder="Nombre, documento o correo" @keydown.enter.prevent="buscarHuesped" />
            <button type="button" class="btn-secundario" @click="buscarHuesped">Buscar</button>
          </div>
          <ul v-if="encontrados.length" class="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-200">
            <li v-for="h in encontrados" :key="h.id" class="flex items-center justify-between px-3 py-2 text-sm">
              <span>{{ h.nombreHuesped }} · {{ h.tipoDocumento }} {{ h.documento }}</span>
              <button type="button" class="btn-suave btn-sm" @click="usarHuesped(h)">Usar</button>
            </li>
          </ul>
        </section>

        <section class="tarjeta p-5">
          <h2 class="mb-3 font-semibold text-slate-800">Huésped titular</h2>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Campo v-for="c in camposHuesped()" :key="c.nombre" v-model="huesped[c.nombre]" :campo="c"
                   :error="errores['huesped.' + c.nombre]" :class="c.ancho === 'completo' ? 'sm:col-span-2' : ''" />
          </div>
        </section>

        <section class="tarjeta p-5">
          <h2 class="mb-3 font-semibold text-slate-800">Detalles de la estadía</h2>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Campo v-model="numPersonas" :campo="{ nombre: 'numPersonas', etiqueta: 'Número de huéspedes', tipo: 'number', min: 1, max: opcion.cantidadPersonas, requerido: true, ayuda: `Máximo ${opcion.cantidadPersonas}` }"
                   :error="errores.numPersonas" />
            <Campo v-model="solicitudes" class="sm:col-span-2"
                   :campo="{ nombre: 'observaciones', etiqueta: 'Solicitudes especiales (opcional)', tipo: 'textarea', placeholder: 'Ej. cama adicional, piso alto...' }"
                   :error="errores.observaciones" />
          </div>
          <p v-if="errores.fechaEntrada || errores.fechaSalida" class="texto-error">{{ errores.fechaEntrada || errores.fechaSalida }}</p>
        </section>

        <section class="tarjeta p-5">
          <h2 class="mb-1 font-semibold text-slate-800">Servicios adicionales</h2>
          <p class="mb-4 text-sm text-slate-500">Opcional. Indica la cantidad de cada servicio que deseas agregar.</p>
          <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label v-for="s in servicios" :key="s.id"
                   class="flex items-center gap-3 rounded-xl border p-3 transition"
                   :class="seleccion[s.id] ? 'border-marca-500 bg-marca-50' : 'border-slate-200'">
              <div class="flex-1">
                <p class="text-sm font-medium text-slate-800">{{ s.nombreServicio }}</p>
                <p class="text-xs text-slate-500">{{ etiqueta(s.categoria) }} · {{ dinero(s.precio) }} {{ s.tipoCobro === 'POR_NOCHE' ? 'por noche' : 'por estadía' }}</p>
              </div>
              <input v-model.number="seleccion[s.id]" type="number" min="0" max="20" class="entrada w-20" placeholder="0" />
            </label>
          </div>
          <p v-for="(m, k) in errores" v-show="k.startsWith('servicios')" :key="k" class="texto-error">{{ m }}</p>
        </section>

        <div class="flex justify-end">
          <button class="btn-primario px-6 py-3" :disabled="enviando">{{ enviando ? 'Verificando disponibilidad...' : 'Continuar al resumen y pago →' }}</button>
        </div>
      </form>

      <aside class="tarjeta h-fit p-5 lg:sticky lg:top-24">
        <h2 class="font-semibold text-slate-800">Tu selección</h2>
        <p class="mt-3 text-sm text-amber-500">{{ '★'.repeat(opcion.estrellas) }}</p>
        <p class="font-semibold">{{ opcion.nombreHotel }}</p>
        <p class="text-sm text-slate-500">{{ opcion.ciudad }}</p>
        <dl class="mt-4 space-y-2 text-sm">
          <div class="flex justify-between"><dt class="text-slate-500">Habitación</dt><dd>{{ opcion.tipoHabitacion }} #{{ opcion.numeroHabitacion }}</dd></div>
          <div class="flex justify-between"><dt class="text-slate-500">Entrada</dt><dd>{{ fecha(criterios.fechaEntrada) }}</dd></div>
          <div class="flex justify-between"><dt class="text-slate-500">Salida</dt><dd>{{ fecha(criterios.fechaSalida) }}</dd></div>
          <div class="flex justify-between"><dt class="text-slate-500">Noches</dt><dd>{{ opcion.noches }}</dd></div>
          <div class="flex justify-between border-t border-slate-100 pt-2"><dt class="text-slate-500">Alojamiento</dt><dd>{{ dinero(opcion.precioTotal) }}</dd></div>
          <div class="flex justify-between"><dt class="text-slate-500">Servicios (estimado)</dt><dd>{{ dinero(totalServicios) }}</dd></div>
          <div class="flex justify-between border-t border-slate-200 pt-2 text-base font-bold"><dt>Total estimado</dt><dd>{{ dinero(Number(opcion.precioTotal) + totalServicios) }}</dd></div>
        </dl>
        <p class="mt-3 text-xs text-slate-500">El total final se confirma en el siguiente paso.</p>
      </aside>
    </div>
  </div>
</template>
