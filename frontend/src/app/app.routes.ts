import { Routes } from '@angular/router';
import { authGuard, adminGuard, underwriterGuard, claimsOfficerGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
    { path: 'buy', redirectTo: 'customer/buy', pathMatch: 'full' },
    {
        path: '',
        loadComponent: () => import('./features/landing/landing/landing.component').then(m => m.LandingComponent),
    },
    {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
        canActivate: [guestGuard],
    },
    {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent),
        canActivate: [guestGuard],
    },
    {
        path: 'admin',
        loadComponent: () => import('./features/admin/components/admin-layout.component').then(m => m.AdminLayoutComponent),
        canActivate: [adminGuard],
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', loadComponent: () => import('./features/admin/components/dashboard/dashboard.component').then(m => m.AdminDashboardComponent) },
            { path: 'staff', loadComponent: () => import('./features/admin/components/staff/staff.component').then(m => m.AdminStaffComponent) },
            { path: 'plans', loadComponent: () => import('./features/admin/components/plans/plans.component').then(m => m.AdminPlansComponent) },
            { path: 'applications', loadComponent: () => import('./features/admin/components/claims/applications/applications.component').then(m => m.AdminApplicationsComponent) },
            { path: 'claims', loadComponent: () => import('./features/admin/components/claims/admin-claims.component').then(m => m.AdminClaimsComponent) },
            { path: 'payments', loadComponent: () => import('./features/admin/components/payments/admin-payments.component').then(m => m.AdminPaymentsComponent) },
        ]
    },
    {
        path: 'customer',
        loadComponent: () => import('./features/customer/components/customer.component').then(m => m.CustomerComponent),
        canActivate: [authGuard],
        children: [
            { path: '', loadComponent: () => import('./features/customer/components/dashboard/dashboard.component').then(m => m.DashboardComponent) },
            { path: 'buy', loadComponent: () => import('./features/customer/components/purchase/purchase.component').then(m => m.PurchaseComponent) },
            { path: 'buy/form', loadComponent: () => import('./features/customer/components/purchase/form/form.component').then(m => m.FormComponent) },
            { path: 'policies', loadComponent: () => import('./features/customer/components/policies/policies.component').then(m => m.PoliciesComponent) },
            { path: 'payment', loadComponent: () => import('./features/customer/components/payment/payment.component').then(m => m.PaymentComponent) },
            { path: 'claims', loadComponent: () => import('./features/customer/components/claims/claims.component').then(m => m.ClaimsComponent) },
            { path: 'profile', loadComponent: () => import('./features/customer/components/dashboard/dashboard.component').then(m => m.DashboardComponent) }
        ]
    },
    {
        path: 'underwriter',
        loadComponent: () => import('./features/underwriter/components/underwriter-dashboard.component').then(m => m.UnderwriterDashboardComponent),
        canActivate: [underwriterGuard]
    },
    {
        path: 'claims-officer',
        loadComponent: () => import('./features/claimsofficer/components/layout/claims-officer-layout.component').then(m => m.ClaimsOfficerLayoutComponent),
        canActivate: [claimsOfficerGuard],
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', loadComponent: () => import('./features/claimsofficer/components/dashboard/claims-officer-dashboard.component').then(m => m.ClaimsOfficerDashboardComponent) },
            { path: 'history', loadComponent: () => import('./features/claimsofficer/components/history/claims-officer-history.component').then(m => m.ClaimsOfficerHistoryComponent) }
        ]
    },
    {
        path: '**',
        redirectTo: '',
    },
];
