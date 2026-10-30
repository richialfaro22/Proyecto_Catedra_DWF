<script setup>
/**
 * Campo de formulario genérico. `campo` = { nombre, etiqueta, tipo, opciones, requerido,
 * placeholder, ayuda, min, max, step }. Muestra el error enviado por la API bajo el campo.
 */
const props = defineProps({
  campo: { type: Object, required: true },
  error: String,
  requerido: Boolean
})
const modelo = defineModel()
const id = 'c-' + props.campo.nombre.replaceAll('.', '-') + '-' + Math.random().toString(36).slice(2, 7)
</script>

<template>
  <div>
    <label :for="id" class="etiqueta" :class="{ requerido: requerido || campo.requerido }">{{ campo.etiqueta }}</label>

    <select v-if="campo.tipo === 'select'" :id="id" v-model="modelo" class="entrada" :class="{ 'entrada-error': error }">
      <option value="">{{ campo.placeholder || 'Seleccione...' }}</option>
      <option v-for="o in campo.opciones" :key="o.valor" :value="o.valor">{{ o.texto }}</option>
    </select>

    <textarea v-else-if="campo.tipo === 'textarea'" :id="id" v-model="modelo" rows="3" class="entrada"
              :class="{ 'entrada-error': error }" :placeholder="campo.placeholder" />

    <input v-else :id="id" v-model="modelo" :type="campo.tipo || 'text'" class="entrada"
           :class="{ 'entrada-error': error }" :placeholder="campo.placeholder"
           :min="campo.min" :max="campo.max" :step="campo.step" autocomplete="off" />

    <p v-if="error" class="texto-error">{{ error }}</p>
    <p v-else-if="campo.ayuda" class="mt-1 text-xs text-slate-500">{{ campo.ayuda }}</p>
  </div>
</template>
