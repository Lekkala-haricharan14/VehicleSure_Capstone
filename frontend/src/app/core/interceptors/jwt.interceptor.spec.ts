import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';
import { signal } from '@angular/core';

describe('JwtInterceptor', () => {
    let httpMock: HttpTestingController;
    let httpClient: HttpClient;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(() => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['logout']);
        (authServiceSpy as any).token = signal<string | null>(null);

        TestBed.configureTestingModule({
            providers: [
                { provide: AuthService, useValue: authServiceSpy },
                provideHttpClient(withInterceptors([jwtInterceptor])),
                provideHttpClientTesting()
            ]
        });

        httpClient = TestBed.inject(HttpClient);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should add Authorization header if token exists', () => {
        (authServiceSpy.token as any).set('mock.jwt.token');

        httpClient.get('/test').subscribe();

        const req = httpMock.expectOne('/test');
        expect(req.request.headers.has('Authorization')).toBeTrue();
        expect(req.request.headers.get('Authorization')).toBe('Bearer mock.jwt.token');
    });

    it('should NOT add Authorization header if no token', () => {
        (authServiceSpy.token as any).set(null);

        httpClient.get('/test').subscribe();

        const req = httpMock.expectOne('/test');
        expect(req.request.headers.has('Authorization')).toBeFalse();
    });
});
