import { Component, signal, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css',
})
export class LoginComponent {
    private fb = inject(FormBuilder);
    private auth = inject(AuthService);
    private router = inject(Router);

    isLoading = signal(false);
    errorMsg = signal('');
    showPassword = signal(false);

    form = this.fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
    });

    get email() { return this.form.get('email')!; }
    get password() { return this.form.get('password')!; }

    togglePassword() {
        this.showPassword.update(v => !v);
    }

    onSubmit() {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.isLoading.set(true);
        this.errorMsg.set('');

        this.auth.login({
            email: this.email.value!,
            password: this.password.value!,
        }).subscribe({
            next: () => {
                this.isLoading.set(false);
            },
            error: (err) => {
                this.isLoading.set(false);
                if (err.status === 401 || err.status === 403 || err.status === 404) {
                    this.errorMsg.set('Invalid credentials');
                } else {
                    this.errorMsg.set('Login failed. Please try again.');
                }
            }
        });
    }
}
