import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ChangePasswordModalComponent } from '../../../../shared/components/change-password-modal/change-password-modal.component';
import { NotificationIconComponent } from '../../../../shared/components/notification-icon/notification-icon.component';

@Component({
    selector: 'app-claims-officer-layout',
    standalone: true,
    imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ChangePasswordModalComponent, NotificationIconComponent],
    templateUrl: './claims-officer-layout.component.html'
})
export class ClaimsOfficerLayoutComponent {
    private authService = inject(AuthService);
    officerName = this.authService.username;
    showPasswordModal = false;

    logout() {
        this.authService.logout();
    }
}
