import { opciones } from './formato'

/** Campos del formulario de huésped (tabla Huesped de la Fase 1 + datos del paso 3 de la reserva). */
export function camposHuesped(prefijo = '') {
  return [
    { nombre: prefijo + 'nombreHuesped', etiqueta: 'Nombre completo', requerido: true, placeholder: 'Nicole Méndez' },
    { nombre: prefijo + 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', tipo: 'date', requerido: true,
      ayuda: 'El titular debe ser mayor de edad.' },
    { nombre: prefijo + 'tipoDocumento', etiqueta: 'Tipo de documento', tipo: 'select', requerido: true,
      opciones: opciones(['DUI', 'PASAPORTE', 'OTRO']) },
    { nombre: prefijo + 'documento', etiqueta: 'Documento de identidad / pasaporte', requerido: true,
      placeholder: '04512345-6', ayuda: 'DUI: 00000000-0 · Pasaporte: 6 a 12 letras/números' },
    { nombre: prefijo + 'telefono', etiqueta: 'Teléfono', tipo: 'tel', requerido: true, placeholder: '7000-0000' },
    { nombre: prefijo + 'email', etiqueta: 'Correo electrónico', tipo: 'email', requerido: true, placeholder: 'nombre@correo.com' },
    { nombre: prefijo + 'nacionalidad', etiqueta: 'Nacionalidad', placeholder: 'Salvadoreña' },
    { nombre: prefijo + 'direccion', etiqueta: 'Dirección', placeholder: 'Colonia, ciudad' }
  ]
}

export const huespedVacio = () => ({
  nombreHuesped: '', fechaNacimiento: '', tipoDocumento: 'DUI', documento: '',
  telefono: '', email: '', nacionalidad: 'Salvadoreña', direccion: ''
})

export function huespedDesde(h) {
  if (!h) return huespedVacio()
  const v = huespedVacio()
  Object.keys(v).forEach(k => { if (h[k] !== null && h[k] !== undefined) v[k] = h[k] })
  return v
}

/** Convierte '' en null para que la API muestre el mensaje de campo obligatorio. */
export const limpiarVacios = (obj) => Object.fromEntries(Object.entries(obj).map(([k, v]) => [k, v === '' ? null : v]))
