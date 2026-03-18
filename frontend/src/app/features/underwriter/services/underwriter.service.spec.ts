import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UnderwriterService } from './underwriter.service';
import { errorInterceptor } from '../../../core/interceptors/error.interceptor';
import { ToastService } from '../../../core/services/toast.service';

describe('UnderwriterService', () => {
    let service: UnderwriterService;
    let httpMock: HttpTestingController;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    const API_URL = 'http://localhost:8080/api/underwriter';

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                UnderwriterService,
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(UnderwriterService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should fetch assigned applications', () => {
        const mockApps = [{ vehicleApplicationId: 1, registrationNumber: 'ABC' }];

        service.getAssignedApplications().subscribe(data => {
            expect(data).toEqual(mockApps as any);
        });

        const req = httpMock.expectOne(`${API_URL}/applications`);
        expect(req.request.method).toBe('GET');
        req.flush(mockApps);
    });

    it('should approve application', () => {
        service.approveApplication(1).subscribe();

        const req = httpMock.expectOne(`${API_URL}/applications/1/approve`);
        expect(req.request.method).toBe('POST');
        req.flush({});
    });

    it('should reject application with reason', () => {
        const reason = 'Invalid docs';
        service.rejectApplication(1, reason).subscribe();

        const req = httpMock.expectOne(`${API_URL}/applications/1/reject`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual({ reason: reason });
        req.flush({});
    });

    it('should handle rejection error', () => {
        service.rejectApplication(1, 'Reason').subscribe({
            error: (err) => {
                expect(err.message).toBe('Application not assigned to you');
            }
        });

        const req = httpMock.expectOne(`${API_URL}/applications/1/reject`);
        req.flush({ message: 'Application not assigned to you' }, { status: 403, statusText: 'Forbidden' });
    });
});
