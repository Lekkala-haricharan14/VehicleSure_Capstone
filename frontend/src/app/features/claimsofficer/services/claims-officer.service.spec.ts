import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClaimsOfficerService } from './claims-officer.service';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from '../../../core/interceptors/error.interceptor';
import { ToastService } from '../../../core/services/toast.service';

describe('ClaimsOfficerService', () => {
    let service: ClaimsOfficerService;
    let httpMock: HttpTestingController;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    const API_URL = 'http://localhost:8080/api/claims-officer';

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                ClaimsOfficerService,
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(ClaimsOfficerService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch assigned claims', () => {
        const mockClaims = [{ claimId: 1, claimNumber: 'CLM001' }];

        service.getAssignedClaims().subscribe(data => {
            expect(data).toEqual(mockClaims as any);
        });

        const req = httpMock.expectOne(`${API_URL}/claims`);
        expect(req.request.method).toBe('GET');
        req.flush(mockClaims);
    });

    it('should approve claim', () => {
        const payload = { billAmount: 1000 };
        service.approveClaim(1, payload).subscribe();
        
        const req = httpMock.expectOne(`${API_URL}/claims/1/approve`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(payload);
        req.flush({});
    });

    it('should handle approval error', () => {
        service.approveClaim(1, {}).subscribe({
            error: (err) => {
                expect(err.message).toBe('Claim not found');
            }
        });

        const req = httpMock.expectOne(`${API_URL}/claims/1/approve`);
        req.flush({ message: 'Claim not found' }, { status: 404, statusText: 'Not Found' });
    });
});
