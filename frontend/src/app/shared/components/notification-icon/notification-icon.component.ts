import { Component, inject, OnInit, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-notification-icon',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-icon.component.html',
  styleUrls: ['./notification-icon.component.css'],
  providers: [DatePipe]
})
export class NotificationIconComponent implements OnInit {
  notificationService = inject(NotificationService);
  private eRef = inject(ElementRef);
  private router = inject(Router);
  private auth = inject(AuthService);
  
  isOpen = false;

  ngOnInit() {
    this.notificationService.loadNotifications();
  }

  toggleDropdown() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.notificationService.loadNotifications();
    }
  }

  @HostListener('document:click', ['$event'])
  clickout(event: any) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }

  markAsRead(id: number, event: Event) {
    event.stopPropagation();
    this.notificationService.markAsRead(id);
  }

  onNotificationClick(notif: any) {
    if (!notif.isRead) {
      this.notificationService.markAsRead(notif.id);
    }
    this.isOpen = false;

    const role = this.auth.userRole();
    const type = notif.type;

    if (role === 'ADMIN') {
      if (type.includes('APPLICATION') || type.includes('POLICY')) this.router.navigate(['/admin/applications']);
      else if (type.includes('CLAIM')) this.router.navigate(['/admin/claims']);
      else if (type.includes('PAYMENT') || type.includes('PREMIUM')) this.router.navigate(['/admin/payments']);
    } else if (role === 'CUSTOMER') {
      if (type.includes('POLICY')) this.router.navigate(['/customer/policies']);
      else if (type.includes('CLAIM')) this.router.navigate(['/customer/claims']);
      else if (type.includes('PAYMENT')) this.router.navigate(['/customer/payment']);
    } else if (role === 'UNDERWRITER') {
      this.router.navigate(['/underwriter']);
    } else if (role === 'CLAIMS_OFFICER') {
      this.router.navigate(['/claims-officer/dashboard']);
    }
  }

  formatDate(date: string): string {
    const d = new Date(date);
    return d.toLocaleString();
  }

  getIconForType(type: string): string {
    switch(type) {
      case 'POLICY_APPROVED': return 'check_circle';
      case 'POLICY_REJECTED': return 'cancel';
      case 'CLAIM_APPROVED': return 'fact_check';
      case 'CLAIM_REJECTED': return 'gavel';
      case 'NEW_ASSIGNMENT': return 'assignment_ind';
      case 'NEW_APPLICATION_SUBMITTED': return 'add_shopping_cart';
      case 'NEW_CLAIM_SUBMITTED': return 'report_problem';
      case 'PREMIUM_PAID': return 'payments';
      case 'PAYMENT_SUCCESS': return 'account_balance_wallet';
      default: return 'notifications';
    }
  }
}
