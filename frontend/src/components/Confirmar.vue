<script setup>
import Modal from './Modal.vue'

defineProps({
  abierto: Boolean,
  titulo: { type: String, default: '¿Estás seguro?' },
  mensaje: String,
  textoBoton: { type: String, default: 'Confirmar' },
  peligro: { type: Boolean, default: true },
  ocupado: Boolean
})
const emit = defineEmits(['cerrar', 'confirmar'])
</script>

<template>
  <Modal :abierto="abierto" :titulo="titulo" ancho="max-w-md" @cerrar="emit('cerrar')">
    <p class="text-sm text-slate-600">{{ mensaje }}</p>
    <slot />
    <template #pie>
      <button class="btn-secundario" @click="emit('cerrar')">Volver</button>
      <button :class="peligro ? 'btn-peligro' : 'btn-primario'" :disabled="ocupado" @click="emit('confirmar')">
        {{ ocupado ? 'Procesando...' : textoBoton }}
      </button>
    </template>
  </Modal>
</template>
