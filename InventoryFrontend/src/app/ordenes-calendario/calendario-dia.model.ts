import { OrdenCalendario } from './orden-calendario.model';

export interface CalendarioDiaDTO {
  date: string;                    // "2026-02-18"
  dayName: string;                 // (you can ignore this — we use locale below)
  relativeToToday: string;
  weekLabel: string;
  orderCount: number;
  orders: OrdenCalendario[];
}