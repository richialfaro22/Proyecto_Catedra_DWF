const moneda = new Intl.NumberFormat('es-SV', { style: 'currency', currency: 'USD' })

export const dinero = (v) => (v === null || v === undefined || v === '' ? '—' : moneda.format(Number(v)))

/** 'AAAA-MM-DD' -> 'DD/MM/AAAA' sin problemas de zona horaria. */
export function fecha(iso) {
  if (!iso) return '—'
  const [a, m, d] = String(iso).substring(0, 10).split('-')
  return `${d}/${m}/${a}`
}

export function fechaHora(iso) {
  if (!iso) return '—'
  const f = new Date(iso)
  return f.toLocaleString('es-SV', { dateStyle: 'short', timeStyle: 'short' })
}

/** Fecha local de hoy en formato AAAA-MM-DD (+ días opcionales). */
export function hoyISO(dias = 0) {
  const f = new Date()
  f.setDate(f.getDate() + dias)
  const m = String(f.getMonth() + 1).padStart(2, '0')
  const d = String(f.getDate()).padStart(2, '0')
  return `${f.getFullYear()}-${m}-${d}`
}

export function sumarDias(iso, dias) {
  const [a, m, d] = iso.split('-').map(Number)
  const f = new Date(a, m - 1, d + dias)
  return `${f.getFullYear()}-${String(f.getMonth() + 1).padStart(2, '0')}-${String(f.getDate()).padStart(2, '0')}`
}

export const texto = (v) => (v ? String(v).replaceAll('_', ' ').toLowerCase().replace(/^\w/, c => c.toUpperCase()) : '—')

export const ETIQUETAS = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'En curso',
  COMPLETADA: 'Completada',
  CANCELADA: 'Cancelada',
  APROBADO: 'Aprobado',
  RECHAZADO: 'Rechazado',
  REEMBOLSADO: 'Reembolsado',
  OCUPADA: 'Ocupada',
  ABIERTO: 'Abierto',
  EN_PROCESO: 'En proceso',
  RESUELTO: 'Resuelto',
  CERRADO: 'Cerrado',
  DISPONIBLE: 'Disponible',
  MANTENIMIENTO: 'Mantenimiento',
  FUERA_DE_SERVICIO: 'Fuera de servicio',
  TARJETA: 'Tarjeta',
  TRANSFERENCIA: 'Transferencia',
  EFECTIVO: 'Efectivo',
  POR_NOCHE: 'Por noche',
  POR_ESTADIA: 'Por estadía',
  ADMIN: 'Administrador',
  RECEPCIONISTA: 'Recepcionista',
  HUESPED: 'Huésped',
  DUI: 'DUI',
  PASAPORTE: 'Pasaporte',
  OTRO: 'Otro',
  ALIMENTACION: 'Alimentación',
  TRANSPORTE: 'Transporte',
  LAVANDERIA: 'Lavandería',
  SPA: 'Spa',
  TOUR: 'Tour',
  QUEJA_DE_SERVICIO: 'Queja de servicio',
  SOLICITUD: 'Solicitud',
  SUGERENCIA: 'Sugerencia'
}

export const etiqueta = (v) => ETIQUETAS[v] || texto(v)

export const opciones = (lista) => lista.map(v => ({ valor: v, texto: etiqueta(v) }))
