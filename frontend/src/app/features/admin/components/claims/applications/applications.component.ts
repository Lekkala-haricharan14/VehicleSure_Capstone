import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../services/admin.service';
import { VehicleApplication, UpdateApplicationStatusRequest, UnderwriterWorkload } from '../../../../../shared/models/policy.model';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-admin-applications',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './applications.component.html',
})
export class AdminApplicationsComponent implements OnInit {
    private adminService = inject(AdminService);

    applications = signal<VehicleApplication[]>([]);
    appsLoading = signal(false);
    appsError = signal('');
    appsSuccess = signal('');

    underwriters = signal<UnderwriterWorkload[]>([]);
    showReviewModal = signal(false);
    selectedApplication = signal<VehicleApplication | null>(null);
    reviewSubmitting = signal(false);

    // Filtering
    statusFilter = signal<string>('ALL');

    filteredApplications = computed(() => {
        const filter = this.statusFilter();
        const apps = this.applications();
        if (filter === 'ALL') return apps;
        return apps.filter(app => app.status === filter);
    });

    statusCounts = computed(() => {
        const apps = this.applications();
        return {
            ALL: apps.length,
            UNDER_REVIEW: apps.filter(a => a.status === 'UNDER_REVIEW').length,
            ASSIGNED: apps.filter(a => a.status === 'ASSIGNED').length,
            APPROVED: apps.filter(a => a.status === 'APPROVED').length,
            REJECTED: apps.filter(a => a.status === 'REJECTED').length,
            PAID: apps.filter(a => a.status === 'PAID').length,
        };
    });

    readonly STATUS_FILTERS = [
        { key: 'ALL', label: 'All Requests', icon: 'list' },
        { key: 'UNDER_REVIEW', label: 'Pending', icon: 'schedule' },
        { key: 'ASSIGNED', label: 'Assigned', icon: 'person_search' },
        { key: 'APPROVED', label: 'Approved', icon: 'check_circle' },
        { key: 'PAID', label: 'Paid', icon: 'payments' },
        { key: 'REJECTED', label: 'Rejected', icon: 'cancel' },
    ] as const;

    ngOnInit(): void {
        this.loadApplications();
    }

    loadApplications() {
        this.appsLoading.set(true);
        this.adminService.getAllApplications().subscribe({
            next: (data) => {
                data.forEach(app => {
                    if (app.status === 'UNDER_REVIEW' && app.assignedUnderwriterName) {
                        app.status = 'ASSIGNED';
                    }
                });
                this.applications.set(data.sort((a, b) => {
                    const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
                    const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
                    return dateB - dateA;
                }));
                this.appsLoading.set(false);
            },
            error: () => {
                this.appsError.set('Failed to load applications.');
                this.appsLoading.set(false);
            }
        });
    }

    loadUnderwriters() {
        this.adminService.getUnderwritersByWorkload().subscribe({
            next: (data) => this.underwriters.set(data),
            error: (err) => console.error('Failed to load underwriters', err)
        });
    }

    openReviewModal(app: VehicleApplication) {
        this.selectedApplication.set(app);
        this.showReviewModal.set(true);
        this.loadUnderwriters();
    }

    closeReviewModal() {
        this.showReviewModal.set(false);
        this.selectedApplication.set(null);
    }


    assignToUnderwriter(underwriterIdStr: string) {
        const underwriterId = Number(underwriterIdStr);
        const app = this.selectedApplication();

        if (!app) return;

        if (!underwriterId) {
            this.appsError.set('Please select an underwriter to assign.');
            setTimeout(() => this.appsError.set(''), 3000);
            return;
        }

        this.adminService.assignApplication(app.vehicleApplicationId, underwriterId).subscribe({
            next: (updated) => {
                if (updated.status === 'UNDER_REVIEW') {
                    updated.status = 'ASSIGNED';
                }
                this.applications.update(list => list.map(a =>
                    a.vehicleApplicationId === updated.vehicleApplicationId ? updated : a
                ));
                this.selectedApplication.set(updated);
                this.appsSuccess.set('Application assigned successfully!');
                this.closeReviewModal();
                setTimeout(() => this.appsSuccess.set(''), 3000);
            },
            error: () => {
                this.appsError.set('Failed to assign application.');
                setTimeout(() => this.appsError.set(''), 3000);
            }
        });
    }

    getStatusBadgeClass(status: string): string {
        switch (status) {
            case 'UNDER_REVIEW': return 'bg-amber-100 text-amber-700 border-amber-200';
            case 'ASSIGNED': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'APPROVED': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            case 'REJECTED': return 'bg-rose-100 text-rose-700 border-rose-200';
            default: return 'bg-slate-100 text-slate-700 border-slate-200';
        }
    }

    getStatusCount(key: string): number {
        return (this.statusCounts() as any)[key] || 0;
    }

    formatCurrency(val: number): string {
        return '₹' + val?.toLocaleString('en-IN');
    }

    viewDocument(path?: string) {
        if (!path) {
            this.appsError.set('Document path not found');
            setTimeout(() => this.appsError.set(''), 3000);
            return;
        }
        const url = `http://localhost:8080/${path}`;
        window.open(url, '_blank');
    }
}
