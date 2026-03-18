import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ChangePasswordModalComponent } from './change-password-modal.component';
import { AuthService } from '../../../core/services/auth.service';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';

describe('ChangePasswordModalComponent', () => {
    let component: ChangePasswordModalComponent;
    let fixture: ComponentFixture<ChangePasswordModalComponent>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['changePassword']);

        await TestBed.configureTestingModule({
            imports: [ChangePasswordModalComponent, ReactiveFormsModule],
            providers: [
                { provide: AuthService, useValue: authServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ChangePasswordModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should validate password mismatch', () => {
        component.passwordForm.patchValue({
            currentPassword: 'old',
            newPassword: 'newPassword123',
            confirmPassword: 'wrongPassword'
        });
        expect(component.passwordForm.errors).toEqual({ passwordMismatch: true });
    });

    it('should handle successful password change', fakeAsync(() => {
        component.passwordForm.patchValue({
            currentPassword: 'oldPassword',
            newPassword: 'newPassword123',
            confirmPassword: 'newPassword123'
        });
        authServiceSpy.changePassword.and.returnValue(of('Success'));
        spyOn(component.close, 'emit');

        component.onSubmit();

        expect(component.successMessage).toBe('Password changed successfully! You can now use your new password.');
        tick(2000);
        expect(component.close.emit).toHaveBeenCalled();
    }));

    it('should show error on failure', () => {
        component.passwordForm.patchValue({
            currentPassword: 'wrongOldPassword',
            newPassword: 'newPassword123',
            confirmPassword: 'newPassword123'
        });
        authServiceSpy.changePassword.and.returnValue(throwError(() => ({ error: 'Invalid current password' })));

        component.onSubmit();

        expect(component.errorMessage).toBe('Invalid current password');
        expect(component.isLoading).toBeFalse();
    });
});
