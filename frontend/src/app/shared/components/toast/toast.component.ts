import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-4 right-4 z-[9999] flex flex-col gap-3 pointer-events-none">
      <div *ngFor="let toast of toastService.toasts()" 
           class="pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl shadow-2xl border min-w-[300px] animate-slide-in"
           [ngClass]="{
             'bg-emerald-50 border-emerald-100 text-emerald-800': toast.type === 'success',
             'bg-rose-50 border-rose-100 text-rose-800': toast.type === 'error',
             'bg-amber-50 border-amber-100 text-amber-800': toast.type === 'warning',
             'bg-indigo-50 border-indigo-100 text-indigo-800': toast.type === 'info'
           }">
        
        <span class="material-icons text-xl" [ngClass]="{
             'text-emerald-500': toast.type === 'success',
             'text-rose-500': toast.type === 'error',
             'text-amber-500': toast.type === 'warning',
             'text-indigo-500': toast.type === 'info'
           }">
          {{ getIcon(toast.type) }}
        </span>
        
        <p class="text-sm font-bold flex-1">{{ toast.message }}</p>
        
        <button (click)="toastService.remove(toast.id)" class="text-slate-400 hover:text-slate-600 transition-colors">
          <span class="material-icons !text-lg">close</span>
        </button>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slide-in {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    .animate-slide-in {
      animation: slide-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);

  getIcon(type: string): string {
    switch (type) {
      case 'success': return 'check_circle';
      case 'error': return 'error';
      case 'warning': return 'warning';
      default: return 'info';
    }
  }
}
