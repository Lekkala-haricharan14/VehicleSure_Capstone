import { Component, inject, OnInit, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { CustomerService } from '../../services/customer.service';
import { VehicleApplication } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
    authService = inject(AuthService);
    customerService = inject(CustomerService);
    private cdr = inject(ChangeDetectorRef);

    username = '';
    applications: any[] = [];
    isLoading = true;

    // Custom Confirmation Modal State
    showConfirmModal = signal<boolean>(false);
    selectedAppForPayment = signal<any | null>(null);

    // Payment Notifications
    paymentSuccess = signal<string | null>(null);
    paymentError = signal<string | null>(null);

    ngOnInit() {
        console.log('DashboardComponent initialized - Version 2.0 (Robust Fixes)');
        this.username = this.authService.username();
        this.loadApplications();
    }

    loadApplications() {
        console.log('Fetching applications for customer dashboard...');
        this.isLoading = true;

        // Fail-safe to ensure spinner disappears even if something hangs
        setTimeout(() => {
            if (this.isLoading) {
                console.warn('Dashboard loading timed out. Forcing spinner off.');
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        }, 3000);

        this.customerService.getMyApplications().subscribe({
            next: (res) => {
                console.log('Successfully fetched applications:', res);
                try {
                    if (res && Array.isArray(res)) {
                        // Create a new array to avoid mutating read-only responses if applicable
                        const sortedApps = [...res];
                        sortedApps.sort((a, b) => {
                            try {
                                const getTime = (val: any) => {
                                    if (!val) return 0;
                                    // Handle array format [year, month, day...]
                                    if (Array.isArray(val)) {
                                        return new Date(val[0], val[1] - 1, val[2], val[3] || 0, val[4] || 0).getTime();
                                    }
                                    const d = new Date(val);
                                    return isNaN(d.getTime()) ? 0 : d.getTime();
                                };
                                return getTime(b.createdAt) - getTime(a.createdAt);
                            } catch (e) {
                                return 0;
                            }
                        });
                        this.applications = sortedApps;
                    } else {
                        this.applications = [];
                    }
                } catch (err) {
                    console.error('Error processing applications data:', err);
                    this.applications = Array.isArray(res) ? res : [];
                } finally {
                    this.isLoading = false;
                    this.cdr.detectChanges();
                    console.log('Dashboard loading finished. Total apps:', this.applications.length);
                }
            },
            error: (err) => {
                console.error('Error fetching applications on dashboard load:', err);
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    getStatusBadgeClass(status: string): string {
        switch (status) {
            case 'UNDER_REVIEW': return 'bg-amber-100 text-amber-700 border-amber-200';
            case 'APPROVED': return 'bg-emerald-50 text-emerald-600 border-emerald-100';
            case 'REJECTED': return 'bg-rose-50 text-rose-600 border-rose-100';
            case 'PAID': return 'bg-indigo-50 text-indigo-600 border-indigo-100';
            default: return 'bg-slate-50 text-slate-500 border-slate-100';
        }
    }

    isPaid(status: any): boolean {
        return status === 'PAID';
    }

    confirmPayment(app: any) {
        this.selectedAppForPayment.set(app);
        this.showConfirmModal.set(true);
    }

    cancelPaymentConfirm() {
        this.showConfirmModal.set(false);
        this.selectedAppForPayment.set(null);
    }

    processPaymentConfirm() {
        const app = this.selectedAppForPayment();
        if (!app) return;

        const paymentRequest = {
            policyId: app.policyId,
            amount: app.calculatedPremium,
            transactionReference: 'INSTANT-' + Math.random().toString(36).substring(7).toUpperCase()
        };

        this.customerService.processPayment(paymentRequest).subscribe({
            next: (res) => {
                this.paymentSuccess.set('Payment successful! Your documents have been generated.');
                this.loadApplications();
                this.cancelPaymentConfirm();
                setTimeout(() => this.paymentSuccess.set(null), 5000);
            },
            error: (err) => {
                console.error('Payment failed', err);
                this.paymentError.set('Payment failed. Please try again.');
                this.cancelPaymentConfirm();
                setTimeout(() => this.paymentError.set(null), 5000);
            }
        });
    }

    downloadInvoice(policyId: number) {
        this.customerService.downloadInvoice(policyId).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `invoice_${policyId}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            },
            error: (err) => console.error('Error downloading invoice:', err)
        });
    }

    downloadPolicy(policyId: number) {
        this.customerService.downloadPolicyDocument(policyId).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `policy_document_${policyId}.pdf`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            },
            error: (err) => console.error('Error downloading policy:', err)
        });
    }
}
