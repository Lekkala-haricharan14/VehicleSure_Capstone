import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CustomerService } from './customer.service';
import { errorInterceptor } from '../../../core/interceptors/error.interceptor';
import { ToastService } from '../../../core/services/toast.service';

describe('CustomerService', () => {
    let service: CustomerService;
    let httpMock: HttpTestingController;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    const API_URL = 'http://localhost:8080/api/customer';

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                CustomerService,
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(CustomerService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch customer policies', () => {
        const mockPolicies = [{ policyId: 1, policyNumber: 'POL001' }] as any;

        service.getMyPolicies().subscribe(data => {
            expect(data).toEqual(mockPolicies);
        });

        const req = httpMock.expectOne(`${API_URL}/policies`);
        expect(req.request.method).toBe('GET');
        req.flush(mockPolicies);
    });

    it('should submit vehicle application', () => {
        const dto = { make: 'Toyota', model: 'Camry' } as any;
        const file = new File([''], 'test.pdf');
        service.submitBuyPolicy(dto, file, file).subscribe();

        const req = httpMock.expectOne(`${API_URL}/buy-policy`);
        expect(req.request.method).toBe('POST');
        req.flush('Success');
    });

    it('should handle application error', () => {
        const dto = { make: 'Toyota', model: 'Camry' } as any;
        service.submitBuyPolicy(dto, null as any, null as any).subscribe({
            error: (err) => {
                expect(err.message).toBe('RC file is empty or missing');
            }
        });

        const req = httpMock.expectOne(`${API_URL}/buy-policy`);
        req.flush({ message: 'RC file is empty or missing' }, { status: 400, statusText: 'Bad Request' });
    });
});
