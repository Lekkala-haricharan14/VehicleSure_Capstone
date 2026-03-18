import { Component, signal, inject } from '@angular/core';
import {
    FormBuilder, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordMismatch: true };
}

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './register.component.html',
    styleUrl: './register.component.css',
})
export class RegisterComponent {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);

    isLoading = signal(false);
    errorMsg = signal('');
    successMsg = signal('');
    showPassword = signal(false);
    showConfirm = signal(false);
    currentStep = signal(1);

    form = this.fb.group(
        {
            username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
            email: ['', [Validators.required, Validators.email]],
            phoneNumber: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
            password: ['', [Validators.required, Validators.minLength(8),
            Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)]],
            confirmPassword: ['', Validators.required],
        },
        { validators: passwordMatchValidator }
    );

    get username() { return this.form.get('username')!; }
    get email() { return this.form.get('email')!; }
    get phoneNumber() { return this.form.get('phoneNumber')!; }
    get password() { return this.form.get('password')!; }
    get confirmPassword() { return this.form.get('confirmPassword')!; }

    goToStep2() {
        this.username.markAsTouched();
        this.email.markAsTouched();
        this.phoneNumber.markAsTouched();
        if (this.username.valid && this.email.valid && this.phoneNumber.valid) {
            this.currentStep.set(2);
        }
    }

    goToStep1() {
        this.currentStep.set(1);
    }

    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.isLoading.set(true);
        this.errorMsg.set('');
        this.successMsg.set('');

        this.auth.register({
            username: this.username.value!,
            email: this.email.value!,
            password: this.password.value!,
            phoneNumber: this.phoneNumber.value!,
        }).subscribe({
            next: () => {
                this.isLoading.set(false);
                this.successMsg.set('Account created! Redirecting to login...');
                setTimeout(() => this.router.navigate(['/login']), 2000);
            },
            error: (err) => {
                this.isLoading.set(false);
                this.errorMsg.set(err?.error?.message ?? 'Registration failed. Please try again.');
            },
        });
    }

    getPasswordStrength(): { label: string; pct: number; color: string } {
        const val = this.password.value ?? '';
        let score = 0;
        if (val.length >= 8) score++;
        if (/[A-Z]/.test(val)) score++;
        if (/[a-z]/.test(val)) score++;
        if (/\d/.test(val)) score++;
        if (/[^A-Za-z0-9]/.test(val)) score++;
        if (score <= 1) return { label: 'Weak', pct: 20, color: '#ef4444' };
        if (score === 2) return { label: 'Fair', pct: 40, color: '#f59e0b' };
        if (score === 3) return { label: 'Good', pct: 65, color: '#3b82f6' };
        if (score === 4) return { label: 'Strong', pct: 85, color: '#22c55e' };
        return { label: 'Very Strong', pct: 100, color: '#22c55e' };
    }
}
