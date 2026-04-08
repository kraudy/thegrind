import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

import { ToastMessage } from './toast-message.model';

@Injectable({ providedIn: 'root' })
export class ToastService {

  private toastSource = new Subject<ToastMessage>();
  toast$ = this.toastSource.asObservable();

  showToast(type: 'success' | 'error' | 'warning' | 'info', title: string, message: string, duration = 4500) {
    const toast: ToastMessage = {
      id: Date.now(),
      type,
      title,
      message,
      duration
    };
    console.log('🚀 showToast CALLED →', toast); 
    this.toastSource.next(toast);
  }
}