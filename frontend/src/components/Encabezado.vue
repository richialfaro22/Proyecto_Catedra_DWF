<script setup>
defineProps({ titulo: String, subtitulo: String })
</script>

<template>
  <div class="mb-6 flex flex-wrap items-end justify-between gap-3">
    <div>
      <h1 class="text-2xl font-bold text-slate-900">{{ titulo }}</h1>
      <p v-if="subtitulo" class="mt-1 text-sm text-slate-500">{{ subtitulo }}</p>
    </div>
    <div class="flex flex-wrap gap-2"><slot /></div>
  </div>
</template>
