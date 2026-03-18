import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';

describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);

        await TestBed.configureTestingModule({
            imports: [RegisterComponent, ReactiveFormsModule],
            providers: [
                { provide: AuthService, useValue: authServiceSpy },
                provideRouter([])
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        const router = TestBed.inject(Router);
        routerSpy = spyOn(router, 'navigate') as any;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should go to step 2 when step 1 is valid', () => {
        component.form.patchValue({
            username: 'testuser',
            email: 'test@test.com',
            phoneNumber: '9876543210'
        });
        component.goToStep2();
        expect(component.currentStep()).toBe(2);
    });

    it('should handle registration success', fakeAsync(() => {
        component.form.patchValue({
            username: 'testuser',
            email: 'test@test.com',
            phoneNumber: '9876543210',
            password: 'Password123',
            confirmPassword: 'Password123'
        });
        authServiceSpy.register.and.returnValue(of({} as any));

        component.onSubmit();

        expect(component.successMsg()).toBe('Account created! Redirecting to login...');
        tick(2000);
        expect(routerSpy).toHaveBeenCalledWith(['/login']);
    }));

    it('should show error on registration failure', () => {
        component.form.patchValue({
            username: 'testuser',
            email: 'test@test.com',
            phoneNumber: '9876543210',
            password: 'Password123',
            confirmPassword: 'Password123'
        });
        authServiceSpy.register.and.returnValue(throwError(() => ({ error: { message: 'Existing User' } })));

        component.onSubmit();

        expect(component.errorMsg()).toBe('Existing User');
        expect(component.isLoading()).toBeFalse();
    });

    it('should handle service errors gracefully', () => {
        component.form.patchValue({
            username: 'testuser',
            email: 'test@test.com',
            phoneNumber: '9876543210',
            password: 'Password123',
            confirmPassword: 'Password123'
        });
        authServiceSpy.register.and.returnValue(throwError(() => ({ error: { message: 'Network Error' } })));

        component.onSubmit();

        expect(component.errorMsg()).toBe('Network Error'); // Assuming component extracts message from generic Error
        expect(component.isLoading()).toBeFalse();
    });
});
