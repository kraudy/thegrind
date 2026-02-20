import { OrdenCalendario } from './orden-calendario.model';

export interface CalendarioDiaDTO {
  date: string;                    // "2026-02-18"
  dayName: string;                 // se puede ignorar
  relativeToToday: string;
  weekLabel: string;
  orderCount: number;
  orders: OrdenCalendario[];
}