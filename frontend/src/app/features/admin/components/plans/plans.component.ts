import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { PolicyPlan, CreatePolicyPlanRequest, VEHICLE_CATEGORIES } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-admin-plans',
    standalone: true,
    imports: [ReactiveFormsModule, CommonModule],
    templateUrl: './plans.component.html',
})
export class AdminPlansComponent implements OnInit {
    private fb = inject(FormBuilder);
    private adminService = inject(AdminService);

    plans = signal<PolicyPlan[]>([]);
    plansLoading = signal(false);
    planError = signal('');
    planSuccess = signal('');
    plansSubTab = signal<'active' | 'inactive'>('active');
    showPlanModal = signal(false);
    planSubmitting = signal(false);

    readonly vehicleCategories = VEHICLE_CATEGORIES;

    planForm = this.fb.group({
        planName: ['', Validators.required],
        policyType: ['COMPREHENSIVE', Validators.required],
        description: ['', Validators.required],
        basePremium: [0, [Validators.required, Validators.min(1)]],
        maxCoverageAmount: [0, [Validators.required, Validators.min(1)]],
        policyDurationMonths: [12, [Validators.required, Validators.min(1)]],
        deductibleAmount: [0, Validators.required],
        coversThirdParty: [true],
        coversOwnDamage: [false],
        coversTheft: [false],
        coversNaturalDisaster: [false],
        zeroDepreciationAvailable: [false],
        engineProtectionAvailable: [false],
        roadsideAssistanceAvailable: [false],
        applicableVehicleType: ['ALL', Validators.required],
    });

    activePlans = computed(() => this.plans().filter(p => p.active));
    inactivePlans = computed(() => this.plans().filter(p => !p.active));

    activePlansByVehicle = computed(() => {
        const active = this.activePlans();
        return VEHICLE_CATEGORIES
            .map(cat => ({
                ...cat,
                plans: active.filter((p: PolicyPlan) =>
                    p.applicableVehicleType === cat.key ||
                    (cat.key === 'ALL' && p.applicableVehicleType === 'ALL')
                )
            }))
            .filter(group => group.plans.length > 0);
    });

    inactivePlansByVehicle = computed(() => {
        const inactive = this.inactivePlans();
        return VEHICLE_CATEGORIES
            .map(cat => ({
                ...cat,
                plans: inactive.filter((p: PolicyPlan) =>
                    p.applicableVehicleType === cat.key ||
                    (cat.key === 'ALL' && p.applicableVehicleType === 'ALL')
                )
            }))
            .filter(group => group.plans.length > 0);
    });

    showDeactivateModal = signal(false);
    pendingDeactivateId = signal<number | null>(null);
    pendingDeactivateName = signal('');

    showActivateModal = signal(false);
    pendingActivateId = signal<number | null>(null);
    pendingActivateName = signal('');

    ngOnInit(): void {
        this.loadPlans();
    }

    loadPlans() {
        this.plansLoading.set(true);
        this.adminService.getAllPolicyPlans().subscribe({
            next: (data: PolicyPlan[]) => { this.plans.set(data); this.plansLoading.set(false); },
            error: () => { this.planError.set('Failed to load plans.'); this.plansLoading.set(false); },
        });
    }

    setPlansSubTab(tab: 'active' | 'inactive') { this.plansSubTab.set(tab); }
    openPlanModal() {
        this.planForm.reset({
            policyType: 'COMPREHENSIVE', policyDurationMonths: 12, applicableVehicleType: 'ALL',
            coversThirdParty: true, coversOwnDamage: false, coversTheft: false,
            coversNaturalDisaster: false, zeroDepreciationAvailable: false,
            engineProtectionAvailable: false, roadsideAssistanceAvailable: false,
            basePremium: 0, maxCoverageAmount: 0, deductibleAmount: 0,
        }); this.showPlanModal.set(true);
    }
    closePlanModal() { this.showPlanModal.set(false); this.planError.set(''); }

    submitPlan() {
        if (this.planForm.invalid) { this.planForm.markAllAsTouched(); return; }
        this.planSubmitting.set(true);
        this.adminService.addPolicyPlan(this.planForm.value as CreatePolicyPlanRequest).subscribe({
            next: () => {
                this.planSubmitting.set(false);
                this.closePlanModal();
                this.planSuccess.set('Policy plan created successfully!');
                this.loadPlans();
                setTimeout(() => this.planSuccess.set(''), 3000);
            },
            error: (err: any) => {
                this.planSubmitting.set(false);
                this.planError.set(err?.error?.message ?? 'Failed to create plan.');
            },
        });
    }

    deactivatePlan(id: number, name: string) {
        this.pendingDeactivateId.set(id);
        this.pendingDeactivateName.set(name);
        this.showDeactivateModal.set(true);
    }

    confirmDeactivate() {
        const id = this.pendingDeactivateId();
        if (id === null) return;
        this.showDeactivateModal.set(false);
        this.adminService.deactivatePolicyPlan(id).subscribe({
            next: (updatedPlan: PolicyPlan) => {
                this.plans.update((list: PolicyPlan[]) => list.map((p: PolicyPlan) => p.planId === updatedPlan.planId ? updatedPlan : p));
                this.planSuccess.set('Plan deactivated successfully.');
                setTimeout(() => this.planSuccess.set(''), 3000);
            },
            error: () => this.planError.set('Failed to deactivate plan.'),
        });
        this.pendingDeactivateId.set(null);
        this.pendingDeactivateName.set('');
    }

    activatePlan(id: number, name: string) {
        this.pendingActivateId.set(id);
        this.pendingActivateName.set(name);
        this.showActivateModal.set(true);
    }

    confirmActivate() {
        const id = this.pendingActivateId();
        if (id === null) return;
        this.showActivateModal.set(false);
        this.adminService.activatePolicyPlan(id).subscribe({
            next: (updatedPlan: PolicyPlan) => {
                this.plans.update((list: PolicyPlan[]) => list.map((p: PolicyPlan) => p.planId === updatedPlan.planId ? updatedPlan : p));
                this.planSuccess.set('Plan activated successfully.');
                setTimeout(() => this.planSuccess.set(''), 3000);
            },
            error: () => this.planError.set('Failed to activate plan.'),
        });
        this.pendingActivateId.set(null);
        this.pendingActivateName.set('');
    }

    formatCurrency(val: number): string {
        return '₹' + val?.toLocaleString('en-IN');
    }
}
