<script setup>
import CrudPage from '../../components/CrudPage.vue'
import { dinero, etiqueta, fechaHora } from '../../utils/formato'

const columnas = [
  { campo: 'fechaPago', etiqueta: 'Fecha', formato: fechaHora },
  { campo: 'codigoReserva', etiqueta: 'Reserva' },
  { campo: 'nombreHuesped', etiqueta: 'Huésped' },
  { campo: 'nombreHotel', etiqueta: 'Hotel' },
  { campo: 'metodoPago', etiqueta: 'Método', formato: (v, f) => etiqueta(v) + (f.ultimosDigitos ? ` •••• ${f.ultimosDigitos}` : '') },
  { campo: 'totalPagar', etiqueta: 'Monto', formato: (v, f) => (f.estado === 'REEMBOLSADO' ? '− ' : '') + dinero(v) },
  { campo: 'estado', etiqueta: 'Estado', insignia: true },
  { campo: 'referencia', etiqueta: 'Referencia' }
]
</script>

<template>
  <CrudPage titulo="Pagos y reembolsos" subtitulo="Historial de la tabla Pagos: método, monto, fecha y estado de cada movimiento."
            singular="Pago" endpoint="/pagos" :columnas="columnas" :campos="[]"
            :puede-crear="false" :puede-editar="false" :puede-eliminar="false">
    <template #acciones="{ fila }">
      <RouterLink :to="`/reservas/${fila.reservaId}`" class="btn-suave btn-sm">Ver reserva</RouterLink>
    </template>
  </CrudPage>
</template>
