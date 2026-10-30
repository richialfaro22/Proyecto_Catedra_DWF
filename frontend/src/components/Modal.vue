<script setup>
defineProps({
  abierto: Boolean,
  titulo: String,
  ancho: { type: String, default: 'max-w-2xl' }
})
const emit = defineEmits(['cerrar'])
</script>

<template>
  <Teleport to="body">
    <div v-if="abierto" class="fixed inset-0 z-40 flex items-start justify-center overflow-y-auto bg-slate-900/40 p-4 sm:items-center"
         @mousedown.self="emit('cerrar')">
      <div class="tarjeta my-8 w-full" :class="ancho" role="dialog" aria-modal="true">
        <div class="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <h2 class="text-lg font-semibold text-slate-800">{{ titulo }}</h2>
          <button class="text-slate-400 hover:text-slate-600" aria-label="Cerrar" @click="emit('cerrar')">✕</button>
        </div>
        <div class="px-5 py-4">
          <slot />
        </div>
        <div v-if="$slots.pie" class="flex flex-wrap justify-end gap-2 border-t border-slate-200 px-5 py-3">
          <slot name="pie" />
        </div>
      </div>
    </div>
  </Teleport>
</template>
