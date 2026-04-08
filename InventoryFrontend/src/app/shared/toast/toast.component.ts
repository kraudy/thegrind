import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../notification.service';
import { ToastService } from './toast.service';
import { ToastMessage } from './toast-message.model';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngFor="let toast of toasts; let i = index" 
      class="fixed bottom-6 right-6 flex items-center gap-3 px-6 py-4 rounded-2xl shadow-2xl text-white max-w-md transition-all duration-300"
      [ngClass]="{
        'bg-emerald-600': toast.type === 'success',
        'bg-red-600': toast.type === 'error',
        'bg-amber-600': toast.type === 'warning',
        'bg-blue-600': toast.type === 'info'
      }"
      [style.bottom.px]="24 + (i * 80)"
      (click)="remove(toast.id)">
      
      <span class="text-2xl">{{ getIcon(toast.type) }}</span>
      
      <div class="flex-1">
        <p class="font-semibold">{{ toast.title }}</p>
        <p class="text-sm opacity-90">{{ toast.message }}</p>
      </div>

      <button (click)="remove(toast.id); $event.stopImmediatePropagation()" 
        class="text-white/70 hover:text-white text-3xl leading-none">
        ×
      </button>
    </div>
  `
})
export class ToastComponent implements OnInit {
  toasts: ToastMessage[] = [];

  constructor(
    private toastService: ToastService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.toastService.toast$.subscribe(toast => {
      console.log('🎉 Toast RECEIVED in ToastComponent →', toast); 

      this.toasts.push(toast);
      this.cd.detectChanges(); 
      // Auto remove
      setTimeout(() => this.remove(toast.id), toast.duration || 4500);
    });
  }

  remove(id: number) {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.cd.detectChanges();
  }

  getIcon(type: 'success' | 'error' | 'warning' | 'info'): string {
    switch (type) {
      case 'success': return '✅';
      case 'error': return '❌';
      case 'warning': return '⚠️';
      case 'info': return 'ℹ️';
      default: return '📌';
    }
  }
}