<script setup>
import { computed } from 'vue'
import { etiqueta } from '../utils/formato'

const props = defineProps({ valor: String })

const colores = {
  verde: ['CONFIRMADA', 'APROBADO', 'RESUELTO', 'DISPONIBLE', 'ACTIVO', 'Activo'],
  azul: ['EN_CURSO', 'EN_PROCESO', 'ADMIN', 'OCUPADA', 'REEMBOLSADO'],
  ambar: ['PENDIENTE', 'ABIERTO', 'MANTENIMIENTO', 'RECEPCIONISTA', 'En mantenimiento', 'Suspendido'],
  rojo: ['CANCELADA', 'RECHAZADO', 'FUERA_DE_SERVICIO', 'INACTIVO', 'Inactivo'],
  gris: ['COMPLETADA', 'CERRADO', 'HUESPED']
}
const clases = {
  verde: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  azul: 'bg-sky-50 text-sky-700 ring-sky-600/20',
  ambar: 'bg-amber-50 text-amber-800 ring-amber-600/20',
  rojo: 'bg-rose-50 text-rose-700 ring-rose-600/20',
  gris: 'bg-slate-100 text-slate-600 ring-slate-500/20'
}
const clase = computed(() => {
  const c = Object.entries(colores).find(([, lista]) => lista.includes(props.valor))
  return clases[c ? c[0] : 'gris']
})
const textos = { ACTIVO: 'Activo', INACTIVO: 'Inactivo' }
</script>

<template>
  <span class="inline-flex items-center whitespace-nowrap rounded-full px-2 py-0.5 text-xs font-medium ring-1 ring-inset" :class="clase">
    {{ textos[valor] || (/^[A-Z_]+$/.test(valor || '') ? etiqueta(valor) : valor) }}
  </span>
</template>
