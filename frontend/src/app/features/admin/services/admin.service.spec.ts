import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { errorInterceptor } from '../../../core/interceptors/error.interceptor';
import { ToastService } from '../../../core/services/toast.service';

describe('AdminService', () => {
    let service: AdminService;
    let httpMock: HttpTestingController;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    const API_URL = 'http://localhost:8080/api/admin';

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                AdminService,
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(AdminService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should fetch all staff', () => {
        const mockStaff = [{ 
            id: 1, 
            fullName: 'John Doe', 
            role: 'UNDERWRITER', 
            username: 'john', 
            email: 'john@test.com', 
            phoneNumber: '123', 
            active: true, 
            createdAt: '2024-01-01' 
        } as any];

        service.getAllStaff().subscribe(data => {
            expect(data).toEqual(mockStaff);
        });

        const req = httpMock.expectOne(`${API_URL}/staff`);
        expect(req.request.method).toBe('GET');
        req.flush(mockStaff);
    });

    it('should create staff', () => {
        const dto = { username: 'test', email: 'test@test.com', password: '123', fullName: 'Test', role: 'UNDERWRITER' as any, phoneNumber: '123' };
        
        service.createStaff(dto).subscribe(res => {
            expect(res).toBeTruthy();
        });

        const req = httpMock.expectOne(`${API_URL}/staff`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(dto);
        req.flush({ id: 1, ...dto });
    });

    it('should assign application to underwriter', () => {
        service.assignApplication(1, 2).subscribe(res => {
            expect(res).toBeTruthy();
        });

        const req = httpMock.expectOne(`${API_URL}/applications/1/assign/2`);
        expect(req.request.method).toBe('PUT');
        req.flush({ success: true });
    });

    it('should handle errors gracefully via interceptor', () => {
        service.getAllStaff().subscribe({
            next: () => fail('should have failed with 404 error'),
            error: (err) => {
                expect(err.message).toBe('Staff not found');
            }
        });

        const req = httpMock.expectOne(`${API_URL}/staff`);
        req.flush({ message: 'Staff not found' }, { status: 404, statusText: 'Not Found' });
    });
});
