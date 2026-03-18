import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UnderwriterService } from '../services/underwriter.service';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationIconComponent } from '../../../shared/components/notification-icon/notification-icon.component';
import { VehicleApplication } from '../../../shared/models/policy.model';
import { ChangePasswordModalComponent } from '../../../shared/components/change-password-modal/change-password-modal.component';
import { HttpClientModule } from '@angular/common/http';

@Component({
    selector: 'app-underwriter-dashboard',
    standalone: true,
    imports: [CommonModule, HttpClientModule, FormsModule, ChangePasswordModalComponent, NotificationIconComponent],
    templateUrl: './underwriter-dashboard.component.html',
    styleUrls: ['./underwriter-dashboard.component.css']
})
export class UnderwriterDashboardComponent implements OnInit {
    private underwriterService = inject(UnderwriterService);
    public auth = inject(AuthService);

    today = new Date();

    // State
    applications = signal<VehicleApplication[]>([]);
    isLoading = signal<boolean>(true);
    activeTab = signal<'pending' | 'approved' | 'rejected'>('pending');

    filteredApplications = computed(() => {
        const apps = this.applications();
        if (this.activeTab() === 'pending') {
            return apps.filter(a => a.status === 'ASSIGNED' || a.status === 'UNDER_REVIEW');
        } else if (this.activeTab() === 'approved') {
            return apps.filter(a => a.status === 'APPROVED');
        } else {
            return apps.filter(a => a.status === 'REJECTED');
        }
    });

    pendingCount = computed(() => this.applications().filter(a => a.status === 'ASSIGNED' || a.status === 'UNDER_REVIEW').length);
    approvedCount = computed(() => this.applications().filter(a => a.status === 'APPROVED').length);
    rejectedCount = computed(() => this.applications().filter(a => a.status === 'REJECTED').length);

    // Selected App for Modal
    selectedApplication = signal<VehicleApplication | null>(null);

    // Confirmation Modal
    showConfirmModal = signal<boolean>(false);
    confirmAction = signal<'APPROVE' | 'REJECT' | null>(null);

    // Password Modal
    showPasswordModal = signal<boolean>(false);

    // Rejection
    rejectionReason = signal<string>('');
    errorStatus = signal<string | null>(null);

    ngOnInit() {
        this.loadApplications();
    }

    setTab(tab: 'pending' | 'approved' | 'rejected') {
        this.activeTab.set(tab);
    }

    loadApplications() {
        this.isLoading.set(true);
        this.underwriterService.getAssignedApplications().subscribe({
            next: (data) => {
                this.applications.set(data.sort((a, b) => {
                    const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
                    const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
                    return dateB - dateA;
                }));
                this.isLoading.set(false);
            },
            error: (err) => {
                console.error('Failed to load applications', err);
                this.isLoading.set(false);
            }
        });
    }

    openReviewModal(app: VehicleApplication) {
        this.selectedApplication.set(app);
        this.rejectionReason.set('');
    }

    closeReviewModal() {
        this.selectedApplication.set(null);
        this.rejectionReason.set('');
    }

    approveApplication() {
        this.confirmAction.set('APPROVE');
        this.showConfirmModal.set(true);
    }

    rejectApplication() {
        const reason = this.rejectionReason().trim();
        if (!reason) {
            this.errorStatus.set('You must provide a reason for rejecting the application.');
            setTimeout(() => this.errorStatus.set(null), 3000);
            return;
        }
        this.confirmAction.set('REJECT');
        this.showConfirmModal.set(true);
    }

    cancelConfirm() {
        this.showConfirmModal.set(false);
        this.confirmAction.set(null);
    }

    processConfirm() {
        const app = this.selectedApplication();
        const action = this.confirmAction();
        if (!app || !action) return;

        if (action === 'APPROVE') {
            this.underwriterService.approveApplication(app.vehicleApplicationId).subscribe({
                next: () => {
                    this.loadApplications();
                    this.closeReviewModal();
                    this.cancelConfirm();
                },
                error: (err) => {
                    console.error('Approval failed', err);
                    this.cancelConfirm();
                }
            });
        } else {
            const reason = this.rejectionReason().trim();
            this.underwriterService.rejectApplication(app.vehicleApplicationId, reason).subscribe({
                next: () => {
                    this.loadApplications();
                    this.closeReviewModal();
                    this.cancelConfirm();
                },
                error: (err) => {
                    console.error('Rejection failed', err);
                    this.cancelConfirm();
                }
            });
        }
    }

    formatDate(date: any): string {
        if (!date) return 'N/A';
        const d = new Date(date);
        return isNaN(d.getTime()) ? 'Invalid Date' : d.toLocaleDateString();
    }

    formatCurrency(amount: any): string {
        if (!amount) return '₹0';
        return '₹' + Number(amount).toLocaleString('en-IN');
    }

    getStatusBadgeClass(status: string): string {
        switch (status) {
            case 'UNDER_REVIEW': return 'bg-amber-100 text-amber-700 border-amber-200';
            case 'ASSIGNED': return 'bg-indigo-100 text-indigo-700 border-indigo-200';
            case 'APPROVED': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            case 'REJECTED': return 'bg-rose-100 text-rose-700 border-rose-200';
            default: return 'bg-slate-100 text-slate-700 border-slate-200';
        }
    }
}
