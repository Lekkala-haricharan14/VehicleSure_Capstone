import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AdminService } from '../services/admin.service';
import { ChangePasswordModalComponent } from '../../../shared/components/change-password-modal/change-password-modal.component';
import { NotificationIconComponent } from '../../../shared/components/notification-icon/notification-icon.component';

@Component({
    selector: 'app-admin-layout',
    standalone: true,
    imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ChangePasswordModalComponent, NotificationIconComponent],
    templateUrl: './admin-layout.component.html',
})
export class AdminLayoutComponent implements OnInit {
    auth = inject(AuthService);
    showPasswordModal = false;
    private adminService = inject(AdminService);

    pendingAppsCount = signal(0);

    ngOnInit(): void {
        this.loadPendingCount();
    }

    loadPendingCount() {
        this.adminService.getAllApplications().subscribe(apps => {
            const pending = apps.filter(a => a.status === 'UNDER_REVIEW' || a.status === 'ASSIGNED').length;
            this.pendingAppsCount.set(pending);
        });
    }
}
