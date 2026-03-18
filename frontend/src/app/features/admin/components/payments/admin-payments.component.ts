import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminPaymentsService } from '../../services/admin-payments.service';
import { Claim, Payment, ClaimsPayment } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-admin-payments',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './admin-payments.component.html',
})
export class AdminPaymentsComponent implements OnInit {
    private paymentsService = inject(AdminPaymentsService);

    // ── Payments State ────────────────────────────────────
    activeTab = signal<'PENDING' | 'RECEIVED' | 'HISTORY'>('PENDING');
    
    pendingPayments = signal<Claim[]>([]);
    receivedPayments = signal<Payment[]>([]);
    payoutHistory = signal<ClaimsPayment[]>([]);
    
    paymentsLoading = signal(false);
    paymentsError = signal('');
    paymentsSuccess = signal('');

    // Payment Modal
    showPaymentModal = signal(false);
    selectedPaymentClaim = signal<Claim | null>(null);
    paymentSubmitting = signal(false);

    ngOnInit(): void {
        this.loadAllData();
    }

    loadAllData() {
        this.loadPendingPayments();
        this.loadReceivedPayments();
        this.loadPayoutHistory();
    }

    // ── Payments Methods ──────────────────────────────
    loadPendingPayments() {
        this.paymentsLoading.set(true);
        this.paymentsService.getPendingPayments().subscribe({
            next: (data) => {
                this.pendingPayments.set(data);
                this.paymentsLoading.set(false);
            },
            error: () => {
                this.paymentsError.set('Failed to load pending payments.');
                this.paymentsLoading.set(false);
            }
        });
    }

    loadReceivedPayments() {
        this.paymentsService.getReceivedPayments().subscribe({
            next: (data) => this.receivedPayments.set(data),
            error: () => console.error('Failed to load received payments')
        });
    }

    loadPayoutHistory() {
        this.paymentsService.getClaimPayouts().subscribe({
            next: (data) => this.payoutHistory.set(data),
            error: () => console.error('Failed to load payout history')
        });
    }

    openPaymentModal(claim: Claim) {
        this.selectedPaymentClaim.set(claim);
        this.showPaymentModal.set(true);
    }

    closePaymentModal() {
        if (this.paymentSubmitting()) return;
        this.showPaymentModal.set(false);
        this.selectedPaymentClaim.set(null);
    }

    processPayment() {
        const claim = this.selectedPaymentClaim();
        if (!claim) return;

        this.paymentSubmitting.set(true);
        this.paymentsService.processClaimPayment(claim.claimId).subscribe({
            next: () => {
                this.pendingPayments.update(list => list.filter(c => c.claimId !== claim.claimId));
                this.paymentsSuccess.set(`Payment processed successfully for Claim #${claim.claimNumber}`);
                this.paymentSubmitting.set(false);
                this.closePaymentModal();
                setTimeout(() => this.paymentsSuccess.set(''), 5000);
            },
            error: () => {
                this.paymentsError.set('Failed to process payment.');
                this.paymentSubmitting.set(false);
            }
        });
    }

    formatCurrency(val: number): string {
        return '₹' + val?.toLocaleString('en-IN');
    }

    downloadReceivedExcel() {
        this.paymentsService.exportReceivedPayments().subscribe({
            next: (blob) => this.downloadFile(blob, 'received_payments.xlsx'),
            error: () => this.paymentsError.set('Failed to export received payments.')
        });
    }

    downloadPayoutExcel() {
        this.paymentsService.exportPayoutHistory().subscribe({
            next: (blob) => this.downloadFile(blob, 'payout_history.xlsx'),
            error: () => this.paymentsError.set('Failed to export payout history.')
        });
    }

    private downloadFile(blob: Blob, filename: string) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        link.click();
        window.URL.revokeObjectURL(url);
    }
}
