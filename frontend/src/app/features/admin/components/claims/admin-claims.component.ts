import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { Claim, ClaimsOfficerWorkload } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-admin-claims',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-claims.component.html'
})
export class AdminClaimsComponent implements OnInit {
    private adminService = inject(AdminService);

    claims = signal<Claim[]>([]);
    workloads = signal<ClaimsOfficerWorkload[]>([]);
    isLoading = signal(false);
    successMessage = signal('');
    errorMessage = signal('');

    // Filtering State
    statusFilter = signal<string>('ALL');
    STATUS_FILTERS = [
        { key: 'ALL', label: 'All Requests' },
        { key: 'SUBMITTED', label: 'Submitted' },
        { key: 'ASSIGNED', label: 'Assigned' },
        { key: 'APPROVED', label: 'Approved' },
        { key: 'REJECTED', label: 'Rejected' },
        { key: 'SETTLED', label: 'Settled' }
    ];

    filteredClaims = computed(() => {
        const currentFilter = this.statusFilter();
        const allClaims = this.claims();

        if (currentFilter === 'ALL') return allClaims;
        return allClaims.filter(c => c.status === currentFilter);
    });

    selectedClaim = signal<Claim | null>(null);
    selectedOfficerId: number | null = null;
    showModal = false;

    ngOnInit(): void {
        this.loadData();
    }

    getStatusCount(status: string): number {
        if (status === 'ALL') return this.claims().length;
        return this.claims().filter(c => c.status === status).length;
    }

    getDocUrl(path: string | undefined): string | null {
        if (!path) return null;
        // In dev, backend usually runs on 8080
        return `http://localhost:8080/${path}`;
    }

    loadData() {
        this.isLoading.set(true);
        this.adminService.getAllClaims().subscribe({
            next: (data) => {
                this.claims.set(data);
                this.isLoading.set(false);
            },
            error: (err) => {
                this.errorMessage.set('Failed to load claims');
                this.isLoading.set(false);
            }
        });

        this.adminService.getClaimsOfficerWorkload().subscribe({
            next: (data) => this.workloads.set(data),
            error: (err) => console.error('Failed to load workloads', err)
        });
    }

    openAssignModal(claim: Claim) {
        this.selectedClaim.set(claim);
        this.showModal = true;
        this.successMessage.set('');
        this.errorMessage.set('');
    }

    closeModal() {
        this.showModal = false;
        this.selectedClaim.set(null);
        this.selectedOfficerId = null;
    }

    assignToOfficer() {
        const claim = this.selectedClaim();
        if (!claim || !this.selectedOfficerId) return;

        this.adminService.assignClaim(claim.claimId, this.selectedOfficerId).subscribe({
            next: () => {
                this.successMessage.set('Claim assigned successfully!');
                this.loadData();
                setTimeout(() => this.closeModal(), 2000);
            },
            error: (err) => {
                this.errorMessage.set('Failed to assign claim');
            }
        });
    }

    getStatusClass(status: string) {
        switch (status) {
            case 'SUBMITTED': return 'bg-amber-100 text-amber-700';
            case 'ASSIGNED': return 'bg-indigo-100 text-indigo-700';
            case 'APPROVED': return 'bg-emerald-100 text-emerald-700';
            case 'REJECTED': return 'bg-rose-100 text-rose-700';
            case 'SETTLED': return 'bg-slate-800 text-slate-100';
            default: return 'bg-slate-100 text-slate-700';
        }
    }
}
