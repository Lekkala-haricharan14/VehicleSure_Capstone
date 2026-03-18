import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerService } from '../../services/customer.service';
import { ClaimService, CreateClaimRequest } from '../../services/claim.service';
import { AuthService } from '../../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-claims',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './claims.component.html',
    styleUrl: './claims.component.css'
})
export class ClaimsComponent implements OnInit {
    private customerService = inject(CustomerService);
    private claimService = inject(ClaimService);
    private authService = inject(AuthService);

    policies = signal<any[]>([]);
    myClaims = signal<any[]>([]);
    isLoading = signal(false);
    isSubmitting = signal(false);
    error = signal<string | null>(null);
    successMessage = signal<string | null>(null);

    // Modal / Filing State
    showFilingModal = signal(false);
    selectedPolicy = signal<any | null>(null);
    claimType = signal<'THEFT' | 'DAMAGE' | 'THIRD_PARTY'>('DAMAGE');
    description = signal('');

    // Bank Details
    bankAccountNumber = signal('');
    ifscCode = signal('');
    accountHolderName = signal('');

    // Files
    doc1 = signal<File | null>(null);
    doc2 = signal<File | null>(null);
    doc3 = signal<File | null>(null);

    // Labels based on type
    doc1Label = signal('Document 1');
    doc2Label = signal('Document 2');
    doc3Label = signal('Document 3');

    ngOnInit() {
        this.loadPolicies();
        this.loadMyClaims();
    }

    loadPolicies() {
        this.isLoading.set(true);
        this.customerService.getMyPolicies().subscribe({
            next: (data) => {
                // Filter policies so only 'ACTIVE' ones are visible for claims
                const activePolicies = data.filter((p: any) => p.status === 'ACTIVE');
                this.policies.set(activePolicies);
                this.isLoading.set(false);
            },
            error: () => {
                this.error.set('Failed to load policies.');
                this.isLoading.set(false);
            }
        });
    }

    loadMyClaims() {
        this.claimService.getMyClaims().subscribe({
            next: (data) => this.myClaims.set(data),
            error: () => console.error('Failed to load existing claims')
        });
    }

    onFileClaim(policy: any) {
        this.selectedPolicy.set(policy);

        // Robust check for Third Party only policies
        const isTP = policy.policyType?.toUpperCase().includes('THIRD') ||
            (policy.coversThirdParty && !policy.coversTheft && !policy.coversOwnDamage);

        if (isTP) {
            this.claimType.set('THIRD_PARTY');
        } else {
            this.claimType.set('DAMAGE');
        }

        this.showFilingModal.set(true);
        this.updateLabels();
    }

    closeModal() {
        this.showFilingModal.set(false);
        this.resetForm();
    }

    resetForm() {
        this.selectedPolicy.set(null);
        this.claimType.set('DAMAGE');
        this.description.set('');
        this.doc1.set(null);
        this.doc2.set(null);
        this.doc3.set(null);
        this.bankAccountNumber.set('');
        this.ifscCode.set('');
        this.accountHolderName.set('');
        this.error.set(null);
        this.successMessage.set(null);
    }

    updateLabels() {
        const type = this.claimType();
        if (type === 'THEFT') {
            this.doc1Label.set('FIR Copy');
            this.doc2Label.set('Vehicle Invoice');
            this.doc3Label.set('Policy Document');
        } else if (type === 'DAMAGE') {
            this.doc1Label.set('Repair Bill');
            this.doc2Label.set('Vehicle Invoice');
            this.doc3Label.set('Policy Document');
        } else {
            this.doc1Label.set('Other Vehicle Invoice');
            this.doc2Label.set('Repair Bill');
            this.doc3Label.set('Policy Document');
        }
    }

    onFileChange(event: any, docNum: number) {
        const file = event.target.files[0];
        if (file) {
            if (docNum === 1) this.doc1.set(file);
            if (docNum === 2) this.doc2.set(file);
            if (docNum === 3) this.doc3.set(file);
        }
    }

    submitClaim() {
        const policy = this.selectedPolicy();
        if (!policy) return;

        if (!this.doc1() || !this.doc2() || !this.doc3()) {
            this.error.set('Please upload all required documents.');
            return;
        }

        if (!this.bankAccountNumber() || !this.ifscCode() || !this.accountHolderName()) {
            this.error.set('Please provide complete bank details for claim settlement.');
            return;
        }

        this.isSubmitting.set(true);
        this.error.set(null);

        const request: any = {
            policyId: policy.policyId,
            claimType: this.claimType(),
            description: this.description(),
            bankAccountNumber: this.bankAccountNumber(),
            ifscCode: this.ifscCode(),
            accountHolderName: this.accountHolderName()
        };

        this.claimService.submitClaim(request, this.doc1()!, this.doc2()!, this.doc3()!).subscribe({
            next: () => {
                this.successMessage.set('Claim submitted successfully! Our claims officer will review it shortly.');
                this.isSubmitting.set(false);
                this.loadMyClaims();
                setTimeout(() => this.closeModal(), 3000);
            },
            error: (err) => {
                this.error.set(err.error?.message || 'Failed to submit claim.');
                this.isSubmitting.set(false);
            }
        });
    }

    getStatusClass(status: string) {
        switch (status) {
            case 'SUBMITTED': return 'bg-amber-100 text-amber-700';
            case 'APPROVED': return 'bg-emerald-100 text-emerald-700';
            case 'REJECTED': return 'bg-rose-100 text-rose-700';
            default: return 'bg-slate-100 text-slate-700';
        }
    }
}
