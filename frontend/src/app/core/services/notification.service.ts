import { Injectable, signal, inject, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Notification } from '../models/notification.model';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/notifications';

  notifications = signal<Notification[]>([]);
  unreadCount = signal<number>(0);

  unreadNotifications = computed(() => 
    this.notifications().filter(n => !n.isRead)
  );

  loadNotifications() {
    this.http.get<Notification[]>(this.apiUrl).subscribe(data => {
      this.notifications.set(data);
    });
    this.loadUnreadCount();
  }

  loadUnreadCount() {
    this.http.get<number>(`${this.apiUrl}/unread-count`).subscribe(count => {
      this.unreadCount.set(count);
    });
  }

  markAsRead(notificationId: number) {
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {}).pipe(
      tap(() => {
        // Update local state for immediate feedback
        const currentNotifications = this.notifications().map(n => 
          n.id === notificationId ? { ...n, isRead: true } : n
        );
        this.notifications.set(currentNotifications);
        this.loadUnreadCount();
      })
    ).subscribe();
  }
}
