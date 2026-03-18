import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ClaimService } from './claim.service';
import { errorInterceptor } from '../../../core/interceptors/error.interceptor';
import { ToastService } from '../../../core/services/toast.service';

describe('ClaimService', () => {
    let service: ClaimService;
    let httpMock: HttpTestingController;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    const API_URL = 'http://localhost:8080/api/customer/claims';

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                ClaimService,
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(ClaimService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch customer claims', () => {
        const mockClaims = [{ claimId: 1, claimNumber: 'CLM123' }] as any;

        service.getMyClaims().subscribe(data => {
            expect(data).toEqual(mockClaims);
        });

        const req = httpMock.expectOne(`http://localhost:8080/api/customer/my-claims`);
        expect(req.request.method).toBe('GET');
        req.flush(mockClaims);
    });

    it('should submit claim', () => {
        const dto = { policyId: 1, claimType: 'THEFT' } as any;
        service.submitClaim(dto).subscribe();

        const req = httpMock.expectOne(`http://localhost:8080/api/customer/submit-claim`);
        expect(req.request.method).toBe('POST');
        req.flush({});
    });

    it('should handle submission error', () => {
        service.submitClaim({ policyId: 1 } as any).subscribe({
            error: (err) => {
                expect(err.message).toBe('Policy not found');
            }
        });

        const req = httpMock.expectOne(`http://localhost:8080/api/customer/submit-claim`);
        req.flush({ message: 'Policy not found' }, { status: 404, statusText: 'Not Found' });
    });
});
