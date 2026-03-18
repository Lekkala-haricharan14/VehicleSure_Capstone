import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { errorInterceptor } from '../interceptors/error.interceptor';
import { ToastService } from './toast.service';

describe('AuthService', () => {
    let service: AuthService;
    let httpMock: HttpTestingController;
    let routerSpy: jasmine.SpyObj<Router>;
    const API_URL = 'http://localhost:8080/api/auth';

    beforeEach(() => {
        const spy = jasmine.createSpyObj('Router', ['navigate']);
        const toastSpy = jasmine.createSpyObj('ToastService', ['error', 'success']);
        
        TestBed.configureTestingModule({
            providers: [
                AuthService,
                { provide: Router, useValue: spy },
                { provide: ToastService, useValue: toastSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting()
            ]
        });
        service = TestBed.inject(AuthService);
        httpMock = TestBed.inject(HttpTestingController);
        routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
        
        localStorage.clear();
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should login and store token', () => {
        const mockResponse = { token: 'mock.jwt.token' };
        const loginData = { email: 'test@test.com', password: 'password123' };

        service.login(loginData).subscribe(res => {
            expect(res).toEqual(mockResponse);
            expect(localStorage.getItem('vehiclesure_token')).toBe('mock.jwt.token');
        });

        const req = httpMock.expectOne(`${API_URL}/login`);
        expect(req.request.method).toBe('POST');
        req.flush(mockResponse);
    });

    it('should handle registration', () => {
        const registerData = { username: 'user', email: 'test@test.com', password: 'Password123', phoneNumber: '9876543210' };
        
        service.register(registerData).subscribe(res => {
            expect(res).toBeTruthy();
        });

        const req = httpMock.expectOne(`${API_URL}/register`);
        expect(req.request.method).toBe('POST');
        req.flush({ message: 'Success' });
    });

    it('should handle login error', () => {
        const loginData = { email: 'bad@test.com', password: 'wrong' };
        
        service.login(loginData).subscribe({
            error: (error) => {
                expect(error.message).toBe('Invalid credentials');
            }
        });

        const req = httpMock.expectOne(`${API_URL}/login`);
        req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should logout and clear data', () => {
        localStorage.setItem('vehiclesure_token', 'mock.jwt.token');
        service.logout();
        expect(localStorage.getItem('vehiclesure_token')).toBeNull();
        expect(service.isAuthenticated()).toBeFalse();
        expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
    });
});
