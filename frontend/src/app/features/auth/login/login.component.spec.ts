import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

        await TestBed.configureTestingModule({
            imports: [LoginComponent, ReactiveFormsModule],
            providers: [
                { provide: AuthService, useValue: authServiceSpy },
                provideRouter([])
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        const router = TestBed.inject(Router);
        spyOn(router, 'navigate');
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should show error on invalid credentials', () => {
        component.form.setValue({ email: 'test@test.com', password: 'password123' });
        authServiceSpy.login.and.returnValue(throwError(() => ({ status: 401 })));

        component.onSubmit();

        expect(component.errorMsg()).toBe('Invalid credentials');
        expect(component.isLoading()).toBeFalse();
    });

    it('should set loading to true on submit', () => {
        component.form.setValue({ email: 'test@test.com', password: 'password123' });
        authServiceSpy.login.and.returnValue(of({}));

        component.onSubmit();

        expect(authServiceSpy.login).toHaveBeenCalled();
        expect(component.isLoading()).toBeFalse(); // Becomes false after next()
    });

    it('should toggle password visibility', () => {
        expect(component.showPassword()).toBeFalse();
        component.togglePassword();
        expect(component.showPassword()).toBeTrue();
    });
});
