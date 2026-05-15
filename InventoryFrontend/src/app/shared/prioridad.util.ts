export type Prioridad = 'Normal' | 'Alta' | 'Urgente';

/** Returns Tailwind classes to highlight a table row by prioridad */
export function getPrioridadRowClass(p?: string | null): string {
  switch (p) {
    case 'Urgente':
      return 'bg-red-50 border-l-4 border-red-500';
    case 'Alta':
      return 'bg-yellow-50 border-l-4 border-yellow-400';
    default:
      return '';
  }
}

/** Returns Tailwind classes to highlight a card/box by prioridad */
export function getPrioridadCardClass(p?: string | null): string {
  switch (p) {
    case 'Urgente':
      return 'ring-2 ring-red-500';
    case 'Alta':
      return 'ring-2 ring-yellow-400';
    default:
      return '';
  }
}

/** Returns Tailwind classes for a badge displaying prioridad */
export function getPrioridadBadgeClass(p?: string | null): string {
  switch (p) {
    case 'Urgente':
      return 'bg-red-100 text-red-700';
    case 'Alta':
      return 'bg-yellow-100 text-yellow-800';
    default:
      return 'bg-gray-100 text-gray-600';
  }
}
