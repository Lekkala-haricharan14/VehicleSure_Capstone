import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ClaimsOfficerService } from '../../services/claims-officer.service';
import { AuthService } from '../../../../core/services/auth.service';
import { Claim } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-claims-officer-dashboard',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './claims-officer-dashboard.component.html'
})
export class ClaimsOfficerDashboardComponent implements OnInit {
    private claimsOfficerService = inject(ClaimsOfficerService);
    private authService = inject(AuthService);
    private router = inject(Router);

    claims = signal<Claim[]>([]);
    isLoading = signal(true);
    error = signal('');
    success = signal('');
    isSubmitting = signal(false);

    // Computed values
    pendingClaimsCount = computed(() => this.claims().filter(c => c.status === 'ASSIGNED').length);
    processedClaimsCount = computed(() => this.claims().length - this.pendingClaimsCount());
    officerName = this.authService.username;

    // Modal State
    selectedClaim = signal<Claim | null>(null);
    showActionModal = false;
    actionType: 'APPROVE' | 'REJECT' | null = null;
    // Adjudication Inputs
    billAmountStr = '';
    exShowroomPriceStr = '';
    yearOfManufactureStr = '';
    rejectionReason = '';
    actionError = '';
    calculatedPayoutAmount: number | null = null;

    ngOnInit() {
        this.loadClaims();
    }

    loadClaims() {
        this.isLoading.set(true);
        this.error.set('');
        this.claimsOfficerService.getAssignedClaims().subscribe({
            next: (data) => {
                // Sort ASSIGNED first, then by ID descending
                const sorted = data.sort((a, b) => {
                    if (a.status === 'ASSIGNED' && b.status !== 'ASSIGNED') return -1;
                    if (a.status !== 'ASSIGNED' && b.status === 'ASSIGNED') return 1;
                    return b.claimId - a.claimId;
                });
                this.claims.set(sorted);
                this.isLoading.set(false);
            },
            error: (err) => {
                console.error('Failed to load claims:', err);
                this.error.set('Failed to load assigned claims. Please try again later.');
                this.isLoading.set(false);
            }
        });
    }

    openReviewModal(claim: Claim) {
        this.selectedClaim.set(claim);
        this.showActionModal = true;
        this.actionType = null;
        this.billAmountStr = '';
        this.exShowroomPriceStr = '';
        this.yearOfManufactureStr = '';
        this.rejectionReason = '';
        this.actionError = '';
        this.calculatedPayoutAmount = null;
    }

    closeModal() {
        if (this.isSubmitting()) return;
        this.showActionModal = false;
        this.selectedClaim.set(null);
    }

    setActionType(type: 'APPROVE' | 'REJECT') {
        this.actionType = type;
        this.actionError = '';
        this.calculatedPayoutAmount = null;
    }

    calculatePayout() {
        const claim = this.selectedClaim();
        if (!claim || this.actionType !== 'APPROVE') return;
        this.actionError = '';

        const payload: any = {};
        if (this.billAmountStr) payload.billAmount = parseFloat(this.billAmountStr);
        if (this.exShowroomPriceStr) payload.exShowroomPrice = parseFloat(this.exShowroomPriceStr);
        if (this.yearOfManufactureStr) payload.yearOfManufacture = parseInt(this.yearOfManufactureStr, 10);

        if (payload.yearOfManufacture && (payload.yearOfManufacture < 1900 || payload.yearOfManufacture > new Date().getFullYear())) {
            this.actionError = 'Please enter a valid year of manufacture.';
            return;
        }

        this.isSubmitting.set(true);
        this.claimsOfficerService.calculatePayment(claim.claimId, payload).subscribe({
            next: (amount) => {
                this.calculatedPayoutAmount = amount;
                this.isSubmitting.set(false);
            },
            error: (err) => {
                this.handleError(err);
                this.calculatedPayoutAmount = null;
            }
        });
    }

    submitAction() {
        const claim = this.selectedClaim();
        if (!claim || !this.actionType) return;

        this.actionError = '';

        if (this.actionType === 'APPROVE') {
            if (this.calculatedPayoutAmount === null) {
                this.actionError = 'Please calculate the payout amount first.';
                return;
            }

            const payload: any = {};
            if (this.billAmountStr) payload.billAmount = parseFloat(this.billAmountStr);
            if (this.exShowroomPriceStr) payload.exShowroomPrice = parseFloat(this.exShowroomPriceStr);
            if (this.yearOfManufactureStr) payload.yearOfManufacture = parseInt(this.yearOfManufactureStr, 10);

            this.isSubmitting.set(true);
            this.claimsOfficerService.approveClaim(claim.claimId, payload).subscribe({
                next: () => this.handleSuccess('Claim approved successfully.'),
                error: (err) => this.handleError(err)
            });
        } else if (this.actionType === 'REJECT') {
            if (!this.rejectionReason.trim()) {
                this.actionError = 'Reason is required for rejection.';
                return;
            }
            this.isSubmitting.set(true);
            this.claimsOfficerService.rejectClaim(claim.claimId, this.rejectionReason).subscribe({
                next: () => this.handleSuccess('Claim rejected successfully.'),
                error: (err) => this.handleError(err)
            });
        }
    }

    private handleSuccess(msg: string) {
        this.success.set(msg);
        this.isSubmitting.set(false);
        this.closeModal();
        this.loadClaims();
        setTimeout(() => this.success.set(''), 4000);
    }

    private handleError(err: any) {
        console.error('Action failed:', err);
        this.actionError = err.error?.message || 'Action failed. Please try again.';
        this.isSubmitting.set(false);
    }

    getDocUrl(path: string | undefined): string | null {
        if (!path) return null;
        return `http://localhost:8080/${path}`;
    }

    logout() {
        this.authService.logout();
    }
}
