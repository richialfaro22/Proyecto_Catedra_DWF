import { reactive } from 'vue'

/** Estado temporal del proceso de reserva (criterios de búsqueda y opción elegida). */
export const flujoReserva = reactive({
  criterios: {
    ciudad: '',
    fechaEntrada: '',
    fechaSalida: '',
    huespedes: 1,
    tipoId: ''
  },
  resultados: [],
  buscado: false,
  opcion: null
})
