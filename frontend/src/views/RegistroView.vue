<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { post, mostrarError } from '../api/http'
import { iniciarSesion } from '../stores/auth'
import { toast } from '../stores/toast'
import Campo from '../components/Campo.vue'
import { camposHuesped, huespedVacio, limpiarVacios } from '../utils/huesped'

const router = useRouter()
const form = reactive({ alias: '', contrasena: '', confirmar: '', huesped: huespedVacio() })
const errores = reactive({})
const enviando = ref(false)

async function registrar() {
  Object.keys(errores).forEach(k => delete errores[k])
  if (form.contrasena && form.contrasena !== form.confirmar) {
    errores.confirmar = 'Las contraseñas no coinciden.'
    return
  }
  enviando.value = true
  try {
    const r = await post('/auth/registro', {
      alias: form.alias || null,
      contrasena: form.contrasena || null,
      huesped: limpiarVacios(form.huesped)
    })
    iniciarSesion(r)
    toast.exito('¡Cuenta creada! Ya puedes reservar.')
    router.push('/')
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-3xl px-4 py-10">
    <RouterLink to="/login" class="text-sm text-marca-700 hover:underline">← Volver al inicio de sesión</RouterLink>
    <h1 class="mt-3 text-2xl font-bold text-slate-900">Crear cuenta</h1>
    <p class="mt-1 text-sm text-slate-500">Regístrate como huésped para reservar y dar seguimiento a tus estadías. Tu correo será el de tu cuenta.</p>

    <form class="tarjeta mt-6 space-y-6 p-6" novalidate @submit.prevent="registrar">
      <div>
        <h2 class="mb-3 font-semibold text-slate-800">Datos de acceso</h2>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <Campo v-model="form.alias" :campo="{ nombre: 'alias', etiqueta: 'Alias (usuario)', requerido: true, ayuda: 'Letras, números, punto o guion' }" :error="errores.alias" />
          <Campo v-model="form.contrasena" :campo="{ nombre: 'contrasena', etiqueta: 'Contraseña', tipo: 'password', requerido: true, ayuda: 'Mínimo 6 caracteres' }" :error="errores.contrasena" />
          <Campo v-model="form.confirmar" :campo="{ nombre: 'confirmar', etiqueta: 'Confirmar contraseña', tipo: 'password', requerido: true }" :error="errores.confirmar" />
        </div>
      </div>
      <div>
        <h2 class="mb-3 font-semibold text-slate-800">Datos personales</h2>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Campo v-for="c in camposHuesped()" :key="c.nombre" v-model="form.huesped[c.nombre]" :campo="c"
                 :error="errores['huesped.' + c.nombre]" :class="c.ancho === 'completo' ? 'sm:col-span-2' : ''" />
        </div>
      </div>
      <div class="flex justify-end">
        <button class="btn-primario" :disabled="enviando">{{ enviando ? 'Creando cuenta...' : 'Crear cuenta' }}</button>
      </div>
    </form>
  </div>
</template>
