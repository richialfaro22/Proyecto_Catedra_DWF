<script setup>
import CrudPage from '../../components/CrudPage.vue'

const columnas = [{ campo: 'id', etiqueta: 'ID' }, { campo: 'nombre', etiqueta: 'Estado' }]
const campos = [{ nombre: 'nombre', etiqueta: 'Nombre del estado', requerido: true, ancho: 'completo' }]
</script>

<template>
  <div class="space-y-10">
    <CrudPage titulo="Estados de hotel" subtitulo="Tabla Estado_Hotel. El estado 'Activo' es el único que permite reservas."
              singular="Estado" endpoint="/catalogos/estados-hotel" :columnas="columnas" :campos="campos" />
    <CrudPage titulo="Estados de empleado" subtitulo="Tabla Estado_Empleado."
              singular="Estado" endpoint="/catalogos/estados-empleado" :columnas="columnas" :campos="campos" />
  </div>
</template>
