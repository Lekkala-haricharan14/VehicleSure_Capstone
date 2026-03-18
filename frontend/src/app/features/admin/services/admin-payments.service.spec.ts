import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AdminPaymentsService } from './admin-payments.service';
import { errorInterceptor } from '../../../core/interceptors/error.interceptor';
import { ToastService } from '../../../core/services/toast.service';

describe('AdminPaymentsService', () => {
    let service: AdminPaymentsService;
    let httpMock: HttpTestingController;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    const API_URL = 'http://localhost:8080/api/admin';

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                AdminPaymentsService,
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(AdminPaymentsService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch received payments', () => {
        const mockPayments = [{ 
            paymentId: 1, 
            amount: 5000, 
            paymentDate: '2024-03-01', 
            status: 'SUCCESS', 
            transactionReference: 'REF1', 
            policyId: 10,
            customerName: 'John',
            customerEmail: 'j@j.com',
            policyNumber: 'POL1',
            policyType: 'COMPREHENSIVE'
        } as any];

        service.getReceivedPayments().subscribe(data => {
            expect(data).toEqual(mockPayments);
        });

        const req = httpMock.expectOne(`${API_URL}/received-payments`);
        expect(req.request.method).toBe('GET');
        req.flush(mockPayments);
    });

    it('should fetch claim payouts', () => {
        const mockPayouts = [{ 
            paymentId: 101, 
            amountPaid: 2000, 
            paymentDate: '2024-03-05',
            claimId: 1,
            claimNumber: 'CLM1',
            policyNumber: 'POL1',
            policyType: 'THIRD_PARTY',
            customerName: 'Jane',
            billAmount: 5000,
            payoutAmount: 2000,
            status: 'SETTLED',
            processedAt: '2024-03-05',
            claimsOfficerName: 'Officer',
            claimsOfficerEmail: 'o@o.com'
        } as any];

        service.getClaimPayouts().subscribe(data => {
            expect(data).toEqual(mockPayouts);
        });

        const req = httpMock.expectOne(`${API_URL}/claims/payouts`);
        expect(req.request.method).toBe('GET');
        req.flush(mockPayouts);
    });

    it('should process claim payment', () => {
        service.processClaimPayment(5).subscribe(res => {
            expect(res).toBeTruthy();
        });

        const req = httpMock.expectOne(`${API_URL}/claims/5/pay`);
        expect(req.request.method).toBe('POST');
        req.flush({ success: true });
    });
});
