import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard, adminGuard, guestGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('AuthGuards', () => {
    let authServiceSpy: jasmine.SpyObj<AuthService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(() => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'isAdmin', 'getRedirectPath']);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);

        TestBed.configureTestingModule({
            providers: [
                { provide: AuthService, useValue: authServiceSpy },
                { provide: Router, useValue: routerSpy }
            ]
        });
    });

    describe('authGuard', () => {
        it('should allow activation if authenticated', () => {
            authServiceSpy.isAuthenticated.and.returnValue(true);
            const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
            expect(result).toBeTrue();
        });

        it('should redirect to login if not authenticated', () => {
            authServiceSpy.isAuthenticated.and.returnValue(false);
            const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
            expect(result).toBeFalse();
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
        });
    });

    describe('adminGuard', () => {
        it('should allow if authenticated and admin', () => {
            authServiceSpy.isAuthenticated.and.returnValue(true);
            authServiceSpy.isAdmin.and.returnValue(true);
            const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
            expect(result).toBeTrue();
        });

        it('should block if authenticated but not admin', () => {
            authServiceSpy.isAuthenticated.and.returnValue(true);
            authServiceSpy.isAdmin.and.returnValue(false);
            const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
            expect(result).toBeFalse();
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
        });
    });

    describe('guestGuard', () => {
        it('should allow if NOT authenticated', () => {
            authServiceSpy.isAuthenticated.and.returnValue(false);
            const result = TestBed.runInInjectionContext(() => guestGuard({} as any, {} as any));
            expect(result).toBeTrue();
        });

        it('should redirect to portal if already authenticated', () => {
            authServiceSpy.isAuthenticated.and.returnValue(true);
            authServiceSpy.getRedirectPath.and.returnValue('/customer');
            const result = TestBed.runInInjectionContext(() => guestGuard({} as any, {} as any));
            expect(result).toBeFalse();
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/customer']);
        });
    });
});
