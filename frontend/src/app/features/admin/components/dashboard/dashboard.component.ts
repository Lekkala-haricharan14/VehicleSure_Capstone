import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { AdminPaymentsService } from '../../services/admin-payments.service';
import { VEHICLE_CATEGORIES, StaffResponse, PolicyPlan, VehicleApplication, Payment, ClaimsPayment, Policy, Claim } from '../../../../shared/models/policy.model';

@Component({
    selector: 'app-admin-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './dashboard.component.html',
})
export class AdminDashboardComponent implements OnInit {
    private adminService = inject(AdminService);
    private paymentsService = inject(AdminPaymentsService);

    staff = signal<StaffResponse[]>([]);
    plans = signal<PolicyPlan[]>([]);
    applications = signal<VehicleApplication[]>([]);
    claims = signal<Claim[]>([]);
    receivedPayments = signal<Payment[]>([]);
    payoutHistory = signal<ClaimsPayment[]>([]);

    readonly vehicleCategories = VEHICLE_CATEGORIES;

    financialStats = computed(() => {
        const revenue = this.receivedPayments().reduce((acc, p) => acc + (p.amount || 0), 0);
        const expenditure = this.payoutHistory().reduce((acc, p) => acc + (p.amountPaid || 0), 0);
        const settledCount = this.claims().filter(c => c.status === 'SETTLED').length;
        const totalClaims = this.claims().length;

        // Incurred Claim Ratio (ICR) = (Total Value of Claims Paid / Total Premiums Collected) × 100
        const icr = revenue > 0 ? (expenditure / revenue) * 100 : 0;
        
        // Claim Settlement Ratio (CSR) = (Total Claims Settled / Total Claims Received) × 100
        const csr = totalClaims > 0 ? (settledCount / totalClaims) * 100 : 0;

        return {
            totalRevenue: revenue,
            totalExpenditure: expenditure,
            netRevenue: revenue - expenditure,
            icr: icr,
            csr: csr,
            settledCount: settledCount,
            totalClaims: totalClaims
        };
    });

    claimsFunnel = computed(() => {
        const c = this.claims();
        return [
            { label: 'Submitted', count: c.filter(x => x.status === 'SUBMITTED').length, color: 'bg-indigo-400', icon: 'send' },
            { label: 'Under Review', count: c.filter(x => x.status === 'ASSIGNED').length, color: 'bg-amber-400', icon: 'analytics' },
            { label: 'Approved', count: c.filter(x => x.status === 'APPROVED').length, color: 'bg-emerald-400', icon: 'verified' },
            { label: 'Settled', count: c.filter(x => x.status === 'SETTLED').length, color: 'bg-indigo-600', icon: 'task_alt' }
        ];
    });

    recentFinancialActivity = computed(() => {
        const premiums = this.receivedPayments().map(p => ({
            type: 'INCOME',
            label: 'Premium Received',
            amount: p.amount,
            date: p.paymentDate,
            subtext: p.policyNumber,
            icon: 'add_circle'
        }));

        const payouts = this.payoutHistory().map(p => ({
            type: 'EXPENSE',
            label: 'Claim Payout',
            amount: p.amountPaid,
            date: p.paymentDate,
            subtext: p.claimNumber,
            icon: 'remove_circle'
        }));

        return [...premiums, ...payouts]
            .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
            .slice(0, 4);
    });

    categoryProfitability = computed(() => {
        const revs = this.receivedPayments();
        const exps = this.payoutHistory();
        
        return this.vehicleCategories.filter(cat => cat.key !== 'ALL').map(cat => {
            const revenue = revs.filter(r => r.vehicleType === cat.key).reduce((acc, r) => acc + (r.amount || 0), 0);
            const expenditure = exps.filter(e => e.vehicleType === cat.key).reduce((acc, e) => acc + (e.amountPaid || 0), 0);
            return {
                label: cat.label,
                key: cat.key,
                color: cat.color,
                revenue,
                expenditure,
                profit: revenue - expenditure,
                profitMargin: revenue > 0 ? ((revenue - expenditure) / revenue) * 100 : 0
            };
        }).sort((a, b) => b.revenue - a.revenue);
    });

    monthlyTrends = computed(() => {
        const payments = this.receivedPayments();
        const allClaims = this.claims();
        const now = new Date();
        const last6Months = Array.from({ length: 6 }, (_, i) => {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            return {
                month: d.toLocaleString('default', { month: 'short' }),
                year: d.getFullYear(),
                key: `${d.getFullYear()}-${d.getMonth()}`
            };
        }).reverse();

        const data = last6Months.map(m => {
            const filteredPayments = payments.filter(p => {
                const pd = new Date(p.paymentDate);
                return pd.toLocaleString('default', { month: 'short' }) === m.month && pd.getFullYear() === m.year;
            });

            const filteredClaims = allClaims.filter(c => {
                // Assuming claim has a createdAt or similar date field. Checking policy.model.ts, 
                // it seems Claim doesn't have a date by default in the interface, but let's check
                // how it's being used. If no date, we might need to add it or use a fallback.
                // Wait, looking at the previous recentFinancialActivity, it uses paymentDate for payouts.
                // For regular claims, we might not have a date in the current interface.
                // Let's assume there's a field or we'll need to use a mock trend if data is missing.
                return false; // Temporary fallback
            });

            return {
                label: m.month,
                revenue: filteredPayments.reduce((acc, p) => acc + (p.amount || 0), 0),
                policies: filteredPayments.length,
                claims: allClaims.filter(c => {
                    // For now, let's try to match by some property if available, 
                    // otherwise we'll just show some data if we can find a date.
                    return false; 
                }).length
            };
        });

        // Refined logic for claims: since we don't have a date on Claim interface, 
        // let's use payoutHistory dates as a proxy for 'Settled Claims' trend, 
        // or just show a flat line if no date is available. 
        // Actually, let's check the Claim interface again in policy.model.ts.
        // Line 82: export interface Claim { ... documentPaths ... rejectionReason ... } - NO DATE.
        // However, payoutHistory has paymentDate. 
        
        const refinedData = last6Months.map(m => {
            const fPayments = payments.filter(p => {
                const pd = new Date(p.paymentDate);
                return pd.toLocaleString('default', { month: 'short' }) === m.month && pd.getFullYear() === m.year;
            });
            const fPayouts = this.payoutHistory().filter(p => {
                const pd = new Date(p.paymentDate);
                return pd.toLocaleString('default', { month: 'short' }) === m.month && pd.getFullYear() === m.year;
            });

            return {
                label: m.month,
                revenue: fPayments.reduce((acc, p) => acc + (p.amount || 0), 0),
                policies: fPayments.length,
                claims: fPayouts.length // Using payouts as a proxy for claims volume over months
            };
        });

        // Calculate SVG paths for line charts
        const getPath = (vals: number[], height: number, width: number) => {
            if (vals.length < 2) return '';
            const max = Math.max(...vals, 1);
            const stepX = width / (vals.length - 1);
            return vals.map((v, i) => `${i === 0 ? 'M' : 'L'} ${i * stepX} ${height - (v / max * height)}`).join(' ');
        };

        return {
            labels: refinedData.map(d => d.label),
            revenuePath: getPath(refinedData.map(d => d.revenue), 40, 200),
            policiesPath: getPath(refinedData.map(d => d.policies), 40, 200),
            claimsPath: getPath(refinedData.map(d => d.claims), 40, 200),
            currentRevenue: refinedData[refinedData.length - 1].revenue,
            currentPolicies: refinedData[refinedData.length - 1].policies,
            currentClaims: refinedData[refinedData.length - 1].claims,
            revenueData: refinedData.map(d => d.revenue),
            policiesData: refinedData.map(d => d.policies),
            claimsData: refinedData.map(d => d.claims)
        };
    });

    dashboardStats = computed(() => ({
        totalStaff: this.staff().length,
        totalPlans: this.plans().length,
        activePlans: this.plans().filter(p => p.active).length,
        underwriters: this.staff().filter(s => s.role === 'UNDERWRITER').length,
        claimsOfficers: this.staff().filter(s => s.role === 'CLAIMS_OFFICER').length,
        pendingApps: this.applications().filter(a => a.status === 'UNDER_REVIEW' || a.status === 'ASSIGNED').length,
        totalClaims: this.claims().length,
        newClaims: this.claims().filter(c => c.status === 'SUBMITTED').length,
    }));

    ngOnInit(): void {
        this.loadStats();
    }

    loadStats() {
        this.adminService.getAllStaff().subscribe({
            next: (data) => this.staff.set(data),
            error: (err) => console.error('Failed to load staff', err)
        });
        this.adminService.getAllPolicyPlans().subscribe({
            next: (data) => this.plans.set(data),
            error: (err) => console.error('Failed to load plans', err)
        });
        this.adminService.getAllApplications().subscribe({
            next: (data) => this.applications.set(data),
            error: (err) => console.error('Failed to load applications', err)
        });
        this.adminService.getAllClaims().subscribe({
            next: (data) => this.claims.set(data),
            error: (err) => console.error('Failed to load claims', err)
        });
        this.paymentsService.getReceivedPayments().subscribe({
            next: (data) => this.receivedPayments.set(data),
            error: (err) => console.error('Failed to load received payments', err)
        });
        this.paymentsService.getClaimPayouts().subscribe({
            next: (data) => this.payoutHistory.set(data),
            error: (err) => console.error('Failed to load payout history', err)
        });
    }

    getPlansCountByVehicle(key: string): number {
        return this.plans().filter(p => p.applicableVehicleType === key).length;
    }
}
