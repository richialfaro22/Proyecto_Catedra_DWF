<script setup>
import { onMounted, reactive, ref } from 'vue'
import { get, put, mostrarError } from '../api/http'
import { toast } from '../stores/toast'
import { etiqueta, fechaHora } from '../utils/formato'
import { camposHuesped, huespedDesde, limpiarVacios } from '../utils/huesped'
import Campo from '../components/Campo.vue'
import Cargando from '../components/Cargando.vue'
import Encabezado from '../components/Encabezado.vue'

const perfil = ref(null)
const form = reactive({})
const errores = reactive({})
const guardando = ref(false)

onMounted(async () => {
  try {
    perfil.value = await get('/auth/perfil')
    Object.assign(form, huespedDesde(perfil.value.huesped))
  } catch (e) { mostrarError(e) }
})

async function guardar() {
  guardando.value = true
  try {
    perfil.value = await put('/auth/perfil', limpiarVacios(form))
    Object.keys(errores).forEach(k => delete errores[k])
    toast.exito('Perfil actualizado.')
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    guardando.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-3xl">
    <Encabezado titulo="Mi perfil" subtitulo="Tus datos se usan para completar automáticamente tus reservas." />
    <Cargando v-if="!perfil" />
    <template v-else>
      <div class="tarjeta mb-6 flex flex-wrap gap-6 p-5 text-sm">
        <div><span class="text-slate-500">Usuario</span><p class="font-semibold">{{ perfil.usuario.alias }}</p></div>
        <div><span class="text-slate-500">Rol</span><p class="font-semibold">{{ etiqueta(perfil.usuario.rol) }}</p></div>
        <div><span class="text-slate-500">Correo</span><p class="font-semibold">{{ perfil.usuario.email }}</p></div>
        <div v-if="perfil.usuario.nombreEmpleado"><span class="text-slate-500">Empleado</span><p class="font-semibold">{{ perfil.usuario.nombreEmpleado }}</p></div>
        <div><span class="text-slate-500">Miembro desde</span><p class="font-semibold">{{ fechaHora(perfil.usuario.fechaCreacion) }}</p></div>
      </div>
      <form class="tarjeta p-6" novalidate @submit.prevent="guardar">
        <h2 class="mb-1 font-semibold text-slate-800">Ficha de huésped</h2>
        <p v-if="!perfil.huesped" class="mb-4 text-sm text-amber-700">Aún no has completado tu ficha de huésped.</p>
        <div class="mt-3 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Campo v-for="c in camposHuesped()" :key="c.nombre" v-model="form[c.nombre]" :campo="c" :error="errores[c.nombre]" :class="c.ancho === 'completo' ? 'sm:col-span-2' : ''" />
        </div>
        <div class="mt-6 flex justify-end">
          <button class="btn-primario" :disabled="guardando">{{ guardando ? 'Guardando...' : 'Guardar cambios' }}</button>
        </div>
      </form>
    </template>
  </div>
</template>
