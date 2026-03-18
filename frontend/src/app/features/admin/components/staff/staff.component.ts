import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, DatePipe } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { CreateStaffRequest, StaffResponse } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-admin-staff',
    standalone: true,
    imports: [ReactiveFormsModule, CommonModule, DatePipe],
    templateUrl: './staff.component.html',
})
export class AdminStaffComponent implements OnInit {
    private fb = inject(FormBuilder);
    private adminService = inject(AdminService);

    staff = signal<StaffResponse[]>([]);
    staffLoading = signal(false);
    staffError = signal('');
    staffSuccess = signal('');

    staffSubTab = signal<'active' | 'inactive'>('active');
    showStaffModal = signal(false);
    staffSubmitting = signal(false);

    readonly STAFF_ROLES = [
        { key: 'UNDERWRITER', label: 'Underwriters', badgeClass: 'bg-indigo-100 text-indigo-700' },
        { key: 'CLAIMS_OFFICER', label: 'Claims Officers', badgeClass: 'bg-indigo-100 text-indigo-700' },
    ] as const;

    staffForm = this.fb.group({
        username: ['', [Validators.required, Validators.minLength(3)]],
        email: ['', [Validators.required, Validators.email]],
        password: [''],
        phoneNumber: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
        role: ['UNDERWRITER' as 'UNDERWRITER' | 'CLAIMS_OFFICER', Validators.required],
    });

    activeStaff = computed(() => this.staff().filter(s => s.active));
    inactiveStaff = computed(() => this.staff().filter(s => !s.active));

    activeStaffByRole = computed(() =>
        this.STAFF_ROLES
            .map(r => ({ ...r, members: this.activeStaff().filter(s => s.role === r.key) }))
            .filter(g => g.members.length > 0)
    );

    inactiveStaffByRole = computed(() =>
        this.STAFF_ROLES
            .map(r => ({ ...r, members: this.inactiveStaff().filter(s => s.role === r.key) }))
            .filter(g => g.members.length > 0)
    );

    // Staff Action Modals
    showDeactivateStaffModal = signal(false);
    pendingDeactivateStaffId = signal<number | null>(null);
    pendingDeactivateStaffName = signal('');

    showActivateStaffModal = signal(false);
    pendingActivateStaffId = signal<number | null>(null);
    pendingActivateStaffName = signal('');

    ngOnInit(): void {
        this.loadStaff();
    }

    loadStaff() {
        this.staffLoading.set(true);
        this.adminService.getAllStaff().subscribe({
            next: (data) => { this.staff.set(data); this.staffLoading.set(false); },
            error: () => { this.staffError.set('Failed to load staff.'); this.staffLoading.set(false); },
        });
    }

    setStaffSubTab(tab: 'active' | 'inactive') { this.staffSubTab.set(tab); }
    openStaffModal() { this.staffForm.reset({ role: 'UNDERWRITER' }); this.showStaffModal.set(true); }
    closeStaffModal() { this.showStaffModal.set(false); this.staffError.set(''); }

    submitStaff() {
        if (this.staffForm.invalid) { this.staffForm.markAllAsTouched(); return; }
        this.staffSubmitting.set(true);
        this.adminService.createStaff(this.staffForm.value as CreateStaffRequest).subscribe({
            next: () => {
                this.staffSubmitting.set(false);
                this.closeStaffModal();
                this.staffSuccess.set('Staff member created successfully!');
                this.loadStaff();
                setTimeout(() => this.staffSuccess.set(''), 3000);
            },
            error: (err) => {
                this.staffSubmitting.set(false);
                this.staffError.set(err?.error?.message ?? 'Failed to create staff member.');
            },
        });
    }

    deactivateStaff(id: number, username: string) {
        this.pendingDeactivateStaffId.set(id);
        this.pendingDeactivateStaffName.set(username);
        this.showDeactivateStaffModal.set(true);
    }

    confirmDeactivateStaff() {
        const id = this.pendingDeactivateStaffId();
        if (id === null) return;
        this.showDeactivateStaffModal.set(false);
        this.adminService.deactivateStaff(id).subscribe({
            next: (updatedStaff) => {
                this.staff.update(list => list.map(s => s.id === updatedStaff.id ? updatedStaff : s));
                this.staffSuccess.set('Staff member deactivated successfully.');
                setTimeout(() => this.staffSuccess.set(''), 3000);
            },
            error: () => this.staffError.set('Failed to deactivate staff member.'),
        });
        this.pendingDeactivateStaffId.set(null);
        this.pendingDeactivateStaffName.set('');
    }

    activateStaff(id: number, username: string) {
        this.pendingActivateStaffId.set(id);
        this.pendingActivateStaffName.set(username);
        this.showActivateStaffModal.set(true);
    }

    confirmActivateStaff() {
        const id = this.pendingActivateStaffId();
        if (id === null) return;
        this.showActivateStaffModal.set(false);
        this.adminService.activateStaff(id).subscribe({
            next: (updatedStaff) => {
                this.staff.update(list => list.map(s => s.id === updatedStaff.id ? updatedStaff : s));
                this.staffSuccess.set('Staff member activated successfully.');
                setTimeout(() => this.staffSuccess.set(''), 3000);
            },
            error: () => this.staffError.set('Failed to activate staff member.'),
        });
        this.pendingActivateStaffId.set(null);
        this.pendingActivateStaffName.set('');
    }

    getRoleBadgeClass(role: string): string {
        if (role === 'UNDERWRITER') return 'text-[10px] font-bold bg-blue-100 text-blue-700 px-3 py-1 rounded-full';
        if (role === 'CLAIMS_OFFICER') return 'text-[10px] font-bold bg-amber-100 text-amber-700 px-3 py-1 rounded-full';
        if (role === 'ADMIN') return 'text-[10px] font-bold bg-red-100 text-red-700 px-3 py-1 rounded-full';
        return 'text-[10px] font-bold bg-slate-100 text-slate-600 px-3 py-1 rounded-full';
    }
}
