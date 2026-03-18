import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminPaymentsComponent } from './admin-payments.component';
import { AdminPaymentsService } from '../../services/admin-payments.service';
import { AdminService } from '../../services/admin.service';
import { of } from 'rxjs';

describe('AdminPaymentsComponent', () => {
    let component: AdminPaymentsComponent;
    let fixture: ComponentFixture<AdminPaymentsComponent>;
    let paymentsServiceSpy: jasmine.SpyObj<AdminPaymentsService>;

    beforeEach(async () => {
        paymentsServiceSpy = jasmine.createSpyObj('AdminPaymentsService', [
            'getPendingPayments', 
            'getReceivedPayments', 
            'getClaimPayouts', 
            'processClaimPayment',
            'exportReceivedPayments',
            'exportPayoutHistory'
        ]);

        paymentsServiceSpy.getPendingPayments.and.returnValue(of([]));
        paymentsServiceSpy.getReceivedPayments.and.returnValue(of([
            { paymentId: 1, amount: 5000, status: 'PAID' } as any
        ]));
        paymentsServiceSpy.getClaimPayouts.and.returnValue(of([]));
        paymentsServiceSpy.processClaimPayment.and.returnValue(of({}));
        paymentsServiceSpy.exportReceivedPayments.and.returnValue(of(new Blob()));
        paymentsServiceSpy.exportPayoutHistory.and.returnValue(of(new Blob()));

        await TestBed.configureTestingModule({
            imports: [AdminPaymentsComponent],
            providers: [
                { provide: AdminPaymentsService, useValue: paymentsServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminPaymentsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should switch tabs', () => {
        component.activeTab.set('HISTORY');
        expect(component.activeTab()).toBe('HISTORY');
    });

    it('should call export service', () => {
        component.downloadReceivedExcel();
        expect(paymentsServiceSpy.exportReceivedPayments).toHaveBeenCalled();
    });
});
