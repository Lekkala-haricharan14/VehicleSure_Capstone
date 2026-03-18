import { Component, inject, OnInit, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CustomerService } from '../../services/customer.service';
import { Policy } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-policies',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './policies.component.html',
})
export class PoliciesComponent implements OnInit {
    customerService = inject(CustomerService);
    private cdr = inject(ChangeDetectorRef);
    policies: Policy[] = [];
    isLoading = true;

    // Custom Confirmation Modal State
    showConfirmModal = signal<boolean>(false);
    selectedPolicyForPayment = signal<any | null>(null);

    // Payment Notifications
    paymentSuccess = signal<string | null>(null);
    paymentError = signal<string | null>(null);

    ngOnInit() {
        this.loadPolicies();
    }

    loadPolicies() {
        console.log('Fetching policies for customer...');
        this.isLoading = true;

        this.customerService.getMyPolicies().subscribe({
            next: (res) => {
                console.log('Successfully fetched policies:', res);
                try {
                    this.policies = Array.isArray(res) ? res : [];
                } catch (e) {
                    console.error('Error processing policies:', e);
                } finally {
                    this.isLoading = false;
                    this.cdr.detectChanges();
                }
            },
            error: (err) => {
                console.error('Error fetching policies', err);
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'ACTIVE': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
            case 'PENDING_PAYMENT': return 'bg-amber-100 text-amber-700 border-amber-200';
            case 'EXPIRED': return 'bg-slate-100 text-slate-700 border-slate-200';
            case 'CANCELLED': return 'bg-rose-100 text-rose-700 border-rose-200';
            default: return 'bg-amber-100 text-amber-700 border-amber-200';
        }
    }

    confirmPayment(policy: any) {
        this.selectedPolicyForPayment.set(policy);
        this.showConfirmModal.set(true);
    }

    cancelPaymentConfirm() {
        this.showConfirmModal.set(false);
        this.selectedPolicyForPayment.set(null);
    }

    processPaymentConfirm() {
        const policy = this.selectedPolicyForPayment();
        if (!policy) return;

        this.isLoading = true;
        this.showConfirmModal.set(false);

        const orderRequest = {
            policyId: policy.policyId,
            amount: policy.premiumAmount
        };

        this.customerService.createRazorpayOrder(orderRequest).subscribe({
            next: (order) => {
                const options = {
                    key: order.keyId,
                    amount: order.amount * 100,
                    currency: order.currency,
                    name: 'VehicleSure',
                    description: 'Policy Premium Payment - ' + policy.policyNumber,
                    image: 'https://logos-world.net/wp-content/uploads/2020/11/Razorpay-Logo.png',
                    order_id: order.orderId,
                    handler: (response: any) => {
                        this.verifyPayment(response);
                    },
                    prefill: {
                        name: policy.customerName || '',
                        email: '',
                        contact: ''
                    },
                    notes: {
                        policy_id: policy.policyId,
                        policy_number: policy.policyNumber
                    },
                    theme: {
                        color: '#6366f1'
                    },
                    modal: {
                        ondismiss: () => {
                            this.isLoading = false;
                        }
                    }
                };

                const rzp = new (window as any).Razorpay(options);
                rzp.open();
            },
            error: (err) => {
                console.error('Order creation failed', err);
                this.paymentError.set('Failed to initialize payment. Please try again.');
                this.isLoading = false;
                setTimeout(() => this.paymentError.set(null), 5000);
            }
        });
    }

    verifyPayment(razorpayResponse: any) {
        const verificationData = {
            razorpayOrderId: razorpayResponse.razorpay_order_id,
            razorpayPaymentId: razorpayResponse.razorpay_payment_id,
            razorpaySignature: razorpayResponse.razorpay_signature,
            policyId: this.selectedPolicyForPayment()?.policyId
        };

        this.customerService.verifyRazorpayPayment(verificationData).subscribe({
            next: (res) => {
                this.paymentSuccess.set('Payment successful! Your policy is now active.');
                this.loadPolicies();
                this.selectedPolicyForPayment.set(null);
                setTimeout(() => this.paymentSuccess.set(null), 5000);
            },
            error: (err) => {
                console.error('Verification failed', err);
                this.paymentError.set('Payment verification failed. Please contact support.');
                this.isLoading = false;
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

    canDownload(status: any): boolean {
        return status === 'ACTIVE' || status === 'PAID';
    }
}
