import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { errorInterceptor } from './error.interceptor';
import { ToastService } from '../services/toast.service';
import { provideRouter } from '@angular/router';

describe('ErrorInterceptor', () => {
    let httpMock: HttpTestingController;
    let httpClient: HttpClient;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;

    beforeEach(() => {
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['error', 'success', 'info', 'warning']);

        TestBed.configureTestingModule({
            providers: [
                { provide: ToastService, useValue: toastServiceSpy },
                provideHttpClient(withInterceptors([errorInterceptor])),
                provideHttpClientTesting(),
                provideRouter([])
            ]
        });

        httpClient = TestBed.inject(HttpClient);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should handle 404 error and show toast', () => {
        const errorResponse = {
            status: 404,
            statusText: 'Not Found',
            error: { message: 'Resource not found' }
        };

        httpClient.get('/test').subscribe({
            error: (error) => {
                expect(error.message).toBe('Resource not found');
            }
        });

        const req = httpMock.expectOne('/test');
        req.flush(errorResponse.error, { status: 404, statusText: 'Not Found' });

        expect(toastServiceSpy.error).toHaveBeenCalledWith('Resource not found');
    });

    it('should handle 409 Conflict error specifically', () => {
        const errorResponse = {
            status: 409,
            statusText: 'Conflict',
            error: { message: 'User already exists with email' }
        };

        httpClient.get('/test').subscribe({
            error: (error) => {
                expect(error.message).toBe('User already exists with email');
            }
        });

        const req = httpMock.expectOne('/test');
        req.flush(errorResponse.error, { status: 409, statusText: 'Conflict' });

        expect(toastServiceSpy.error).toHaveBeenCalledWith('User already exists with email');
    });

    it('should handle 500 error and show generic message if no message in body', () => {
        httpClient.get('/test').subscribe({
            error: (error) => {
                expect(error.message).toBe('Internal Server Error. Please try again later.');
            }
        });

        const req = httpMock.expectOne('/test');
        req.flush({}, { status: 500, statusText: 'Server Error' });

        expect(toastServiceSpy.error).toHaveBeenCalledWith('Internal Server Error. Please try again later.');
    });
});
