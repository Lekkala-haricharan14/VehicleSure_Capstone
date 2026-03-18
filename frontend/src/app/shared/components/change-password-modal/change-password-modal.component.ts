import { ChangeDetectorRef, Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-change-password-modal',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './change-password-modal.component.html'
})
export class ChangePasswordModalComponent {
    @Output() close = new EventEmitter<void>();

    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    private cdr = inject(ChangeDetectorRef);

    passwordForm: FormGroup;
    isLoading = false;
    isSuccess = false;
    successMessage = '';
    errorMessage = '';

    constructor() {
        this.passwordForm = this.fb.group({
            currentPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', [Validators.required]]
        }, { validators: this.passwordMatchValidator });
    }

    passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
        const newPassword = control.get('newPassword')?.value;
        const confirmPassword = control.get('confirmPassword')?.value;
        return newPassword === confirmPassword ? null : { passwordMismatch: true };
    }

    onSubmit() {
        if (this.passwordForm.invalid) {
            this.passwordForm.markAllAsTouched();
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const currentPassword = this.passwordForm.value.currentPassword;
        const newPassword = this.passwordForm.value.newPassword;

        // Note: The backend DTO ChangePasswordDTO expects { currentPassword, newPassword }
        // The authService mapping might be `{ oldPassword, newPassword }`, so we'll adapt depending on what auth.service currently does.
        // auth.service.ts currently accepts { oldPassword, newPassword } natively according to line 89. I'll pass the adapter object.
        this.authService.changePassword({ currentPassword: currentPassword, newPassword: newPassword }).subscribe({
            next: () => {
                this.isLoading = false;
                this.isSuccess = true;
                this.successMessage = 'Password changed successfully! You can now use your new password.';
                this.passwordForm.reset();
                this.cdr.detectChanges();
                setTimeout(() => this.closeModal(), 2000);
            },
            error: (err) => {
                this.isLoading = false;
                this.errorMessage = err?.error?.message || err?.error || 'Failed to change password. Please verify your current password.';
                this.cdr.detectChanges();
            }
        });
    }

    closeModal() {
        this.close.emit();
    }
}
