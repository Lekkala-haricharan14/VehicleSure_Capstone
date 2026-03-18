import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';

@Component({
    selector: 'app-payment',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './payment.component.html'
})
export class PaymentComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private customerService = inject(CustomerService);

    policyId: number = 0;
    amount: number = 0;
    isLoading = false;

    showSuccess = signal(false);
    errorMessage = signal<string | null>(null);

    cardInfo = {
        name: '',
        number: '',
        expiry: '',
        cvv: ''
    };

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            this.policyId = +params['policyId'];
            this.amount = +params['amount'];
            if (!this.policyId) {
                this.router.navigate(['/customer']);
            }
        });
    }

    pay() {
        this.isLoading = true;
        this.errorMessage.set(null);

        const orderRequest = {
            policyId: this.policyId,
            amount: this.amount
        };

        this.customerService.createRazorpayOrder(orderRequest).subscribe({
            next: (order) => {
                const options = {
                    key: order.keyId,
                    amount: order.amount * 100,
                    currency: order.currency,
                    name: 'VehicleSure',
                    description: 'Policy Premium Payment',
                    image: 'https://logos-world.net/wp-content/uploads/2020/11/Razorpay-Logo.png',
                    order_id: order.orderId,
                    handler: (response: any) => {
                        this.verifyPayment(response);
                    },
                    prefill: {
                        name: this.cardInfo.name || '',
                        email: '',
                        contact: ''
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
                this.errorMessage.set(err.error?.message || 'Failed to create payment order.');
                this.isLoading = false;
            }
        });
    }

    verifyPayment(razorpayResponse: any) {
        const verificationData = {
            razorpayOrderId: razorpayResponse.razorpay_order_id,
            razorpayPaymentId: razorpayResponse.razorpay_payment_id,
            razorpaySignature: razorpayResponse.razorpay_signature,
            policyId: this.policyId
        };

        this.customerService.verifyRazorpayPayment(verificationData).subscribe({
            next: () => {
                this.showSuccess.set(true);
                setTimeout(() => {
                    this.router.navigate(['/customer']);
                }, 2500);
            },
            error: (err) => {
                this.errorMessage.set(err.error || 'Payment verification failed.');
                this.isLoading = false;
            }
        });
    }

    cancel() {
        this.router.navigate(['/customer']);
    }
}
