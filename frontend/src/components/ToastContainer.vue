<script setup>
import { toasts, cerrar } from '../stores/toast'

const estilos = {
  exito: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  error: 'border-rose-200 bg-rose-50 text-rose-800',
  info: 'border-sky-200 bg-sky-50 text-sky-800'
}
const iconos = { exito: '✓', error: '!', info: 'i' }
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 flex w-[calc(100%-2rem)] max-w-sm flex-col gap-2" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="t in toasts" :key="t.id"
           class="flex items-start gap-3 rounded-xl border p-3 text-sm shadow-lg" :class="estilos[t.tipo]">
        <span class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-current/10 text-xs font-bold">{{ iconos[t.tipo] }}</span>
        <p class="flex-1">{{ t.mensaje }}</p>
        <button class="opacity-60 hover:opacity-100" aria-label="Cerrar" @click="cerrar(t.id)">✕</button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all .25s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateY(8px); }
</style>
