import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ChangePasswordModalComponent } from '../../../shared/components/change-password-modal/change-password-modal.component';
import { NotificationIconComponent } from '../../../shared/components/notification-icon/notification-icon.component';

@Component({
    selector: 'app-customer',
    standalone: true,
    imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ChangePasswordModalComponent, NotificationIconComponent],
    templateUrl: './customer.component.html',
})
export class CustomerComponent {
    authService = inject(AuthService);
    showPasswordModal = false;

    menuItems = [
        { label: 'Dashboard', route: '/customer', icon: 'dashboard', exact: true },
        { label: 'View Products', route: '/customer/buy', icon: 'shopping_bag', exact: false },
        { label: 'My Policies', route: '/customer/policies', icon: 'description', exact: false },
        { label: 'Claims', route: '/customer/claims', icon: 'content_paste_search', exact: false }
    ];

    logout() {
        this.authService.logout();
    }
}
