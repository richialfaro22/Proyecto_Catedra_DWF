<script setup>
import { dinero, etiqueta, fecha } from '../utils/formato'
import Insignia from './Insignia.vue'

defineProps({ r: { type: Object, required: true } })
</script>

<template>
  <div class="tarjeta overflow-hidden">
    <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-slate-50 px-5 py-4">
      <div>
        <p class="text-xs uppercase tracking-wide text-slate-500">Reserva</p>
        <p class="font-mono text-xl font-bold text-slate-900">#{{ r.codigo }}</p>
      </div>
      <Insignia :valor="r.estadoReserva" />
    </div>

    <div class="grid grid-cols-1 gap-6 p-5 md:grid-cols-2">
      <div>
        <h3 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Huésped</h3>
        <p class="font-semibold">{{ r.huesped.nombreHuesped }}</p>
        <p class="text-sm text-slate-600">{{ r.huesped.tipoDocumento }} {{ r.huesped.documento }}</p>
        <p class="text-sm text-slate-600">{{ r.huesped.telefono }} · {{ r.huesped.email }}</p>
        <p class="text-sm text-slate-600">Nacimiento: {{ fecha(r.huesped.fechaNacimiento) }}</p>
      </div>
      <div>
        <h3 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Hotel y habitación</h3>
        <p class="font-semibold">{{ r.nombreHotel }}</p>
        <p class="text-sm text-slate-600">{{ r.direccionHotel }}, {{ r.ciudad }}</p>
        <p v-for="d in r.detalles" :key="d.id" class="text-sm text-slate-600">
          Habitación {{ d.tipoHabitacion }} #{{ d.numeroHabitacion }} · {{ d.numPersonas }} huésped(es)
        </p>
      </div>
      <div>
        <h3 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Fechas</h3>
        <p class="text-sm">Entrada: <b>{{ fecha(r.fechaEntrada) }}</b> (desde 3:00 p.m.)</p>
        <p class="text-sm">Salida: <b>{{ fecha(r.fechaSalida) }}</b></p>
        <p class="text-sm">{{ r.noches }} noche(s)</p>
      </div>
      <div v-if="r.observaciones">
        <h3 class="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Solicitudes especiales</h3>
        <p class="text-sm text-slate-700">{{ r.observaciones }}</p>
      </div>
    </div>

    <div class="border-t border-slate-200 p-5">
      <table class="w-full text-sm">
        <tbody>
          <tr v-for="d in r.detalles" :key="'d' + d.id">
            <td class="py-1 text-slate-600">Habitación #{{ d.numeroHabitacion }} ({{ d.cantidadNoches }} noche(s))</td>
            <td class="py-1 text-right">{{ dinero(d.precioReserva) }}</td>
          </tr>
          <tr v-for="s in r.servicios" :key="s.servicioId">
            <td class="py-1 text-slate-600">{{ s.nombreServicio }} × {{ s.cantidad }} <span class="text-xs text-slate-400">({{ etiqueta(s.tipoCobro).toLowerCase() }})</span></td>
            <td class="py-1 text-right">{{ dinero(s.subtotal) }}</td>
          </tr>
          <tr v-if="!r.servicios.length"><td class="py-1 text-slate-600">Servicios adicionales</td><td class="py-1 text-right text-slate-400">Ninguno</td></tr>
        </tbody>
      </table>
      <div class="mt-3 flex items-center justify-between rounded-xl bg-slate-900 px-4 py-3 text-white">
        <span class="text-sm">Precio total</span>
        <span class="text-2xl font-bold">{{ dinero(r.total) }}</span>
      </div>
      <table class="mt-3 w-full text-sm">
        <tbody>
          <tr><td class="text-slate-600">Pagado</td><td class="text-right text-emerald-700">{{ dinero(r.totalPagado) }}</td></tr>
          <tr v-if="Number(r.totalReembolsado) > 0"><td class="text-slate-600">Reembolsado</td><td class="text-right text-sky-700">− {{ dinero(r.totalReembolsado) }}</td></tr>
          <tr v-if="Number(r.saldoPendiente) > 0" class="font-semibold"><td class="text-amber-700">Saldo pendiente</td><td class="text-right text-amber-700">{{ dinero(r.saldoPendiente) }}</td></tr>
        </tbody>
      </table>
      <p v-if="r.estadoReserva === 'CANCELADA'" class="mt-3 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">
        Cancelada el {{ new Date(r.fechaCancelacion).toLocaleString('es-SV') }} · Motivo: {{ r.motivoCancelacion }} ·
        Cargo por cancelación: {{ dinero(r.cargoCancelacion) }}
      </p>
      <p v-else class="mt-3 rounded-lg bg-rose-50 p-3 text-xs text-rose-700">
        Cancelaciones con menos de {{ r.horasCancelacionGratis }} horas de anticipación tienen un cargo del {{ r.porcentajePenalizacion }}% sobre el total. El resto se reembolsa al método de pago original.
      </p>
    </div>
  </div>
</template>
