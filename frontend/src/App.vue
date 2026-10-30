<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auth, autenticado, cerrarSesion, esAdmin, esPersonal } from './stores/auth'
import { etiqueta } from './utils/formato'
import ToastContainer from './components/ToastContainer.vue'

const route = useRoute()
const router = useRouter()
const menuAbierto = ref(false)
watch(() => route.fullPath, () => { menuAbierto.value = false })

const enlaces = computed(() => {
  const l = [
    { to: '/', texto: 'Reservar' },
    { to: '/reservas', texto: esPersonal.value ? 'Reservas' : 'Mis reservas' },
    { to: '/reclamos', texto: 'Reclamos' }
  ]
  if (esPersonal.value) {
    l.push({ to: '/admin/huespedes', texto: 'Huéspedes' })
    l.push({ to: '/admin/habitaciones', texto: 'Habitaciones' })
    l.push({ to: '/admin/pagos', texto: 'Pagos' })
    l.push({ to: '/admin/estadisticas', texto: 'Estadísticas' })
  }
  return l
})

const enlacesAdmin = [
  { to: '/admin/hoteles', texto: 'Hoteles y sucursales' },
  { to: '/admin/tipos', texto: 'Tipos de habitación' },
  { to: '/admin/tarifas', texto: 'Tarifas' },
  { to: '/admin/servicios', texto: 'Servicios adicionales' },
  { to: '/admin/empleados', texto: 'Empleados' },
  { to: '/admin/usuarios', texto: 'Usuarios y roles' },
  { to: '/admin/catalogos', texto: 'Estados (catálogos)' }
]

const mostrarBarra = computed(() => autenticado.value && !route.meta.publico)

function salir() {
  cerrarSesion()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="min-h-screen">
    <header v-if="mostrarBarra" class="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div class="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3">
        <RouterLink to="/" class="flex items-center gap-2 font-bold text-marca-700">
          <img src="/favicon.svg" alt="" class="h-8 w-8" />
          <span class="hidden sm:inline">Hoteles Sheraton</span>
        </RouterLink>

        <nav class="hidden flex-1 items-center gap-1 lg:flex">
          <RouterLink v-for="e in enlaces" :key="e.to" :to="e.to"
                      class="rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                      exact-active-class="!bg-marca-50 !text-marca-700">{{ e.texto }}</RouterLink>
          <div v-if="esAdmin" class="group relative">
            <button class="rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">Administración ▾</button>
            <div class="invisible absolute left-0 top-full z-40 w-56 rounded-xl border border-slate-200 bg-white p-1 opacity-0 shadow-lg transition group-hover:visible group-hover:opacity-100 group-focus-within:visible group-focus-within:opacity-100">
              <RouterLink v-for="e in enlacesAdmin" :key="e.to" :to="e.to"
                          class="block rounded-lg px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">{{ e.texto }}</RouterLink>
            </div>
          </div>
        </nav>

        <div class="ml-auto hidden items-center gap-3 lg:flex">
          <RouterLink to="/perfil" class="text-right text-sm leading-tight hover:text-marca-700">
            <div class="font-semibold text-slate-800">{{ auth.alias }}</div>
            <div class="text-xs text-slate-500">{{ etiqueta(auth.rol) }}</div>
          </RouterLink>
          <button class="btn-secundario btn-sm" @click="salir">Salir</button>
        </div>

        <button class="btn-secundario btn-sm ml-auto lg:hidden" @click="menuAbierto = !menuAbierto">☰ Menú</button>
      </div>

      <div v-if="menuAbierto" class="border-t border-slate-200 bg-white px-4 py-3 lg:hidden">
        <RouterLink v-for="e in [...enlaces, ...(esAdmin ? enlacesAdmin : [])]" :key="e.to" :to="e.to"
                    class="block rounded-lg px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">{{ e.texto }}</RouterLink>
        <RouterLink to="/perfil" class="block rounded-lg px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">
          Mi perfil ({{ auth.alias }})
        </RouterLink>
        <button class="btn-secundario btn-sm mt-2 w-full" @click="salir">Cerrar sesión</button>
      </div>
    </header>

    <main :class="mostrarBarra ? 'mx-auto max-w-7xl px-4 py-6' : ''">
      <RouterView />
    </main>

    <ToastContainer />
  </div>
</template>
