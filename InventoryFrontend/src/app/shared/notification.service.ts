import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private stompClient: Client | null = null;

  // ← NEW: Reactive stream that all components can subscribe to
  private refreshSubject = new Subject<string>();
  refreshNeeded$ = this.refreshSubject.asObservable();

  connect() {
    // Prevent duplicate connections
    if (this.stompClient?.active) return;

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      debug: (str) => console.log('STOMP: ' + str),
    });

    this.stompClient.onConnect = () => {
      console.log('✅ Connected to WebSocket');

      this.stompClient!.subscribe('/topic/ordenes-seguimiento', () => {
        console.log('🔴 Seguimiento changed → notifying subscribers');
        this.refreshSubject.next('seguimiento');
      });

      this.stompClient!.subscribe('/topic/ordenes-calendario', () => {
        this.refreshSubject.next('calendario');
      });

      this.stompClient!.subscribe('/topic/ordenes-trabajo', () => {
        this.refreshSubject.next('trabajo');
      });

      this.stompClient!.subscribe('/topic/ordenes-costo', () => {
        console.log('🔴 Ordenes Costo changed → notifying subscribers');
        this.refreshSubject.next('costo');
      });

      this.stompClient!.subscribe('/topic/ordenes-pago', () => {
        console.log('🔴 Ordenes Pago changed → notifying subscribers');
        this.refreshSubject.next('pago');
      });

      this.stompClient!.subscribe('/topic/facturas', () => {
        console.log('🔴 Facturas changed → notifying subscribers');
        this.refreshSubject.next('facturas');
      });
      
    };

    this.stompClient.activate();
  }

  disconnect() {
    this.stompClient?.deactivate();
    // Optional: this.refreshSubject.complete();
  }

}