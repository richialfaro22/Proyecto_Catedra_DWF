<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { post, mostrarError } from '../api/http'
import { iniciarSesion } from '../stores/auth'
import { toast } from '../stores/toast'
import Campo from '../components/Campo.vue'

const router = useRouter()
const route = useRoute()
const form = reactive({ usuario: '', contrasena: '' })
const errores = reactive({})
const enviando = ref(false)

async function entrar() {
  enviando.value = true
  try {
    const r = await post('/auth/login', { usuario: form.usuario || null, contrasena: form.contrasena || null })
    iniciarSesion(r)
    toast.exito(`¡Bienvenido, ${r.alias}!`)
    const destino = route.query.redirect || (r.rol === 'HUESPED' ? '/' : '/reservas')
    router.push(destino)
  } catch (e) {
    mostrarError(e, errores)
  } finally {
    enviando.value = false
  }
}

</script>

<template>
  <div class="grid min-h-screen lg:grid-cols-2">
    <div class="hidden flex-col justify-between bg-gradient-to-br from-marca-700 to-marca-800 p-12 text-white lg:flex">
      <div class="flex items-center gap-2 text-lg font-bold"><img src="/favicon.svg" class="h-9 w-9" alt="" /> Hoteles Sheraton</div>
      <div>
        <h1 class="text-4xl font-bold leading-tight">Reserva tu estadía</h1>
        <p class="mt-4 max-w-md text-marca-100">Puedes buscar disponibilidad por ciudad y fechas, agrega servicios, paga en línea y administra tus reservas</p>
      </div>
    </div>

    <div class="flex items-center justify-center p-6">
      <div class="w-full max-w-sm">
        <h2 class="text-2xl font-bold text-slate-900">Iniciar sesión</h2>
        <p class="mt-1 text-sm text-slate-500">Ingresa con tu cuenta</p>

        <form class="mt-6 space-y-4" novalidate @submit.prevent="entrar">
          <Campo v-model="form.usuario" :campo="{ nombre: 'usuario', etiqueta: 'Usuario o correo electrónico', requerido: true, placeholder: 'Ingresa tu usuario o correo' }" :error="errores.usuario" />
          <Campo v-model="form.contrasena" :campo="{ nombre: 'contrasena', etiqueta: 'Contraseña', tipo: 'password', requerido: true }" :error="errores.contrasena" />
          <button class="btn-primario w-full" :disabled="enviando">{{ enviando ? 'Ingresando...' : 'Ingresar' }}</button>
        </form>

        <p class="mt-6 text-center text-sm text-slate-600">
          ¿No tienes cuenta? <RouterLink to="/registro" class="font-semibold text-marca-700 hover:underline">Regístrate</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>
