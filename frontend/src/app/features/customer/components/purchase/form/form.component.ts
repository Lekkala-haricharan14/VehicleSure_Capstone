import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CustomerService, PolicyPlan, QuoteResponse, QuoteRequest } from '../../../services/customer.service';

@Component({
    selector: 'app-purchase-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './form.component.html',
})
export class FormComponent implements OnInit {
    private fb = inject(FormBuilder);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private customerService = inject(CustomerService);
    private cdr = inject(ChangeDetectorRef);

    vehicleType: string = 'CAR';
    applicationForm!: FormGroup;

    availablePlans: PolicyPlan[] = [];
    quoteResponses: QuoteResponse[] = [];

    rcFile: File | null = null;
    invoiceFile: File | null = null;

    isSubmitting = false;
    submitError = '';
    submitSuccess = false;

    // 2-Step Quote Variables
    currentStep = 1;
    calculatedIdv = 0;
    selectedTenure = 1; // Default 1 year
    isLoadingQuotes = false;

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            if (params['type']) {
                this.vehicleType = params['type'];
            }
            this.initForm();
            this.loadPlans();
        });
    }

    initForm(): void {
        this.applicationForm = this.fb.group({
            vehicleOwnerName: ['', Validators.required],
            registrationNumber: ['', [
                Validators.required,
                Validators.pattern('^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$')
            ]],
            make: ['', Validators.required],
            model: ['', Validators.required],
            year: [new Date().getFullYear(), [
                Validators.required,
                Validators.min(1990),
                Validators.max(new Date().getFullYear())
            ]],
            fuelType: ['', Validators.required],
            chassisNumber: ['', [
                Validators.required,
                Validators.pattern('^[A-HJ-NPR-Z0-9]{17}$')
            ]],
            distanceDriven: [0, [
                Validators.required,
                Validators.min(0),
                Validators.pattern('^[0-9]+$')
            ]],
            exShowroomPrice: [null, [Validators.required, Validators.min(1000)]],
            transmissionType: ['', Validators.required],
            accidentsInPast: [0, [Validators.required, Validators.min(0), Validators.pattern('^[0-9]+$')]],
            planId: [null, Validators.required]
        });

        // Add real-time formatting logic for registration number
        const regCtrl = this.applicationForm.get('registrationNumber');
        regCtrl?.valueChanges.subscribe(value => {
            if (value) {
                const formatted = value.toUpperCase().replace(/\s+/g, '');
                // Ensure we don't cause an infinite loop by double setting if the value is already handled
                if (formatted !== value) {
                    regCtrl.setValue(formatted, { emitEvent: false });
                }
            }
        });

        // Add real-time formatting logic for chassis number (uppercase only)
        const chassisCtrl = this.applicationForm.get('chassisNumber');
        chassisCtrl?.valueChanges.subscribe(value => {
            if (value) {
                const formatted = value.toUpperCase();
                if (formatted !== value) {
                    chassisCtrl.setValue(formatted, { emitEvent: false });
                }
            }
        });
    }

    loadPlans(): void {
        this.customerService.getActivePlans(this.vehicleType).subscribe({
            next: (plans) => {
                // Defensive client-side filtering: Guarantee only matching vehicle plans are shown
                this.availablePlans = plans.filter(p =>
                    p.applicableVehicleType &&
                    p.applicableVehicleType.toUpperCase() === this.vehicleType.toUpperCase()
                );
            },
            error: (err) => console.error('Failed to load plans:', err)
        });
    }

    onFileSelected(event: Event, type: 'rc' | 'invoice'): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            if (type === 'rc') {
                this.rcFile = input.files[0];
            } else {
                this.invoiceFile = input.files[0];
            }
        }
    }

    // Move to Step 2 (Calculations)
    goToStep2(): void {
        console.log("Submit clicked. Validating...");
        // We need all details except planId for step 2
        const controls = this.applicationForm.controls;
        let isValid = true;
        ['vehicleOwnerName', 'registrationNumber', 'make', 'model', 'year', 'fuelType', 'chassisNumber', 'distanceDriven', 'exShowroomPrice', 'transmissionType', 'accidentsInPast'].forEach(key => {
            if (controls[key].invalid) {
                console.log(`Validation Failed: ${key} is invalid. Value:`, controls[key].value, 'Errors:', controls[key].errors);
                controls[key].markAsTouched();
                isValid = false;
            }
        });

        if (!isValid) {
            console.log("Validation Hard-Failed. Valid:", isValid);
            this.submitError = 'Please fill all required details before getting a quote.';
            return;
        }

        if (!this.rcFile || !this.invoiceFile) {
            this.submitError = 'Please upload both RC and Invoice documents.';
            return;
        }

        console.log("Validation passed. Pinging API...");
        this.submitError = '';
        this.isLoadingQuotes = true;

        const val = this.applicationForm.value;
        const request: QuoteRequest = {
            make: val.make,
            model: val.model,
            year: val.year,
            fuelType: val.fuelType,
            transmissionType: val.transmissionType?.toUpperCase(),
            accidentsInPast: val.accidentsInPast,
            distanceDriven: val.distanceDriven,
            exShowroomPrice: val.exShowroomPrice,
            vehicleType: this.vehicleType,
            // the backend only requires these fields for quotes. 
            // Others like registrationNumber, chassisNumber are ignored for quotes.
            vehicleOwnerName: val.vehicleOwnerName,
            registrationNumber: val.registrationNumber,
            chassisNumber: val.chassisNumber
        };

        this.customerService.getQuotes(request).subscribe({
            next: (quotes) => {
                console.log("API returned quotes:", quotes);
                this.quoteResponses = quotes;
                this.isLoadingQuotes = false;
                if (quotes.length > 0) {
                    this.calculatedIdv = quotes[0].idv;
                }
                this.currentStep = 2;
                this.cdr.detectChanges(); // Trigger Angular rendering pass immediately
                window.scrollTo(0, 0);
            },
            error: (err) => {
                console.error("API hit an error:", err);
                this.isLoadingQuotes = false;
                this.submitError = 'Failed to generate quotes. Please try again.';
                this.cdr.detectChanges();
            }
        });
    }

    goToStep1(): void {
        this.currentStep = 1;
        window.scrollTo(0, 0);
    }

    // Select 1, 2, or 3 year tenure
    selectTenure(years: number): void {
        this.selectedTenure = years;
    }

    getPremiumForPlan(planId: number): number {
        const quote = this.quoteResponses.find(q => q.planId === planId);
        if (!quote) return 0;
        const option = quote.options.find(o => o.tenureYears === this.selectedTenure);
        return option ? option.calculatedPremium : 0;
    }

    onSubmit(): void {
        if (this.applicationForm.invalid) {
            this.submitError = 'Please fully complete the application form.';
            return;
        }

        if (!this.rcFile || !this.invoiceFile) {
            this.submitError = 'Both RC Document and Vehicle Invoice are required to submit the application.';
            return;
        }

        this.isSubmitting = true;
        this.submitError = '';

        const formData = {
            ...this.applicationForm.value,
            vehicleType: this.vehicleType,
            idv: this.calculatedIdv,
            calculatedPremium: this.getPremiumForPlan(this.applicationForm.value.planId),
            tenureYears: this.selectedTenure
        };

        this.customerService.submitBuyPolicy(formData, this.rcFile!, this.invoiceFile!).subscribe({
            next: (response) => {
                this.isSubmitting = false;
                this.submitSuccess = true;
                setTimeout(() => this.router.navigate(['/customer']), 2000);
            },
            error: (err) => {
                this.isSubmitting = false;
                this.submitError = err.error || 'Failed to submit application. Please try again.';
            }
        });
    }
}
