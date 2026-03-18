import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminDashboardComponent } from './dashboard.component';
import { AdminService } from '../../services/admin.service';
import { AdminPaymentsService } from '../../services/admin-payments.service';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';

describe('AdminDashboardComponent', () => {
    let component: AdminDashboardComponent;
    let fixture: ComponentFixture<AdminDashboardComponent>;
    let adminServiceSpy: jasmine.SpyObj<AdminService>;
    let paymentsServiceSpy: jasmine.SpyObj<AdminPaymentsService>;

    const mockStaff = [{ id: 1, fullName: 'Staff 1', role: 'UNDERWRITER' }];
    const mockPayments = [
        { amount: 10000, vehicleType: 'CAR', paymentDate: '2024-03-14' },
        { amount: 5000, vehicleType: 'TWO_WHEELER', paymentDate: '2024-03-14' }
    ];
    const mockPayouts = [
        { amountPaid: 2000, vehicleType: 'CAR', paymentDate: '2024-03-14' }
    ];
    const mockClaims = [
        { status: 'SETTLED' },
        { status: 'SUBMITTED' }
    ];

    beforeEach(async () => {
        adminServiceSpy = jasmine.createSpyObj('AdminService', ['getAllStaff', 'getAllPolicyPlans', 'getAllApplications', 'getAllClaims']);
        paymentsServiceSpy = jasmine.createSpyObj('AdminPaymentsService', ['getReceivedPayments', 'getClaimPayouts']);

        adminServiceSpy.getAllStaff.and.returnValue(of(mockStaff as any));
        adminServiceSpy.getAllPolicyPlans.and.returnValue(of([]));
        adminServiceSpy.getAllApplications.and.returnValue(of([]));
        adminServiceSpy.getAllClaims.and.returnValue(of(mockClaims as any));

        paymentsServiceSpy.getReceivedPayments.and.returnValue(of(mockPayments as any));
        paymentsServiceSpy.getClaimPayouts.and.returnValue(of(mockPayouts as any));

        await TestBed.configureTestingModule({
            imports: [AdminDashboardComponent],
            providers: [
                { provide: AdminService, useValue: adminServiceSpy },
                { provide: AdminPaymentsService, useValue: paymentsServiceSpy },
                provideRouter([])
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminDashboardComponent);
        component = fixture.componentInstance;
        spyOn(window, 'alert');
        fixture.detectChanges();
    });

    it('should create and load data on init', () => {
        expect(component).toBeTruthy();
        expect(adminServiceSpy.getAllStaff).toHaveBeenCalled();
        expect(paymentsServiceSpy.getReceivedPayments).toHaveBeenCalled();
        expect(component.staff().length).toBe(1);
    });

    it('should calculate financial stats correctly', () => {
        const stats = component.financialStats();
        expect(stats.totalRevenue).toBe(15000);
        expect(stats.totalExpenditure).toBe(2000);
        expect(stats.icr).toBeCloseTo(13.33, 1);
        expect(stats.csr).toBe(50);
    });

    it('should compute growth highlights (Monthly Trends)', () => {
        const trends = component.monthlyTrends();
        expect(trends.labels.length).toBe(6);
        expect(trends.revenueData.length).toBe(6);
        expect(trends.revenuePath).toContain('M'); 
    });

    it('should compute category profitability correctly', () => {
        const profitability = component.categoryProfitability();
        const carStats = profitability.find(p => p.key === 'CAR');
        
        expect(carStats).toBeDefined();
        if (carStats) {
            expect(carStats.revenue).toBe(10000);
            expect(carStats.expenditure).toBe(2000);
            expect(carStats.profit).toBe(8000);
        }
    });

    it('should handle service errors gracefully', () => {
        adminServiceSpy.getAllStaff.and.returnValue(throwError(() => new Error('Network Error')));
        component.loadStats();
        expect(adminServiceSpy.getAllStaff).toHaveBeenCalled();
    });
});
