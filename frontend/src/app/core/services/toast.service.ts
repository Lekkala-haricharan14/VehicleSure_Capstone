import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  toasts = signal<Toast[]>([]);
  private counter = 0;

  show(message: string, type: ToastType = 'info') {
    const id = this.counter++;
    const toast: Toast = { id, message, type };
    this.toasts.update(ts => [...ts, toast]);

    // Auto remove after 5 seconds
    setTimeout(() => this.remove(id), 5000);
  }

  success(message: string) { this.show(message, 'success'); }
  error(message: string) { this.show(message, 'error'); }
  warning(message: string) { this.show(message, 'warning'); }
  info(message: string) { this.show(message, 'info'); }

  remove(id: number) {
    this.toasts.update(ts => ts.filter(t => t.id !== id));
  }
}
