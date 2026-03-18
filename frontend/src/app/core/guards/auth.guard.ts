import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated()) return true;
    router.navigate(['/login']);
    return false;
};

export const adminGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated() && auth.isAdmin()) return true;
    router.navigate(['/login']);
    return false;
};

export const underwriterGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated() && auth.isUnderwriter()) return true;
    router.navigate(['/login']);
    return false;
};

export const claimsOfficerGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated() && auth.isClaimsOfficer()) return true;
    router.navigate(['/login']);
    return false;
};

export const guestGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isAuthenticated()) return true;
    router.navigate([auth.getRedirectPath()]);
    return false;
};
