import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../models/user.model';
import { jwtDecode } from 'jwt-decode';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly API_URL = 'http://localhost:8080/api/auth';
    private readonly TOKEN_KEY = 'vehiclesure_token';

    private http = inject(HttpClient);
    private router = inject(Router);

    // ── Signals ──────────────────────────────────────────
    private _token = signal<string | null>(this.loadToken());

    // We only store exactly what is needed logically now
    private _username = signal<string>('');
    private _role = signal<string | null>(null);

    readonly token = this._token.asReadonly();
    readonly username = this._username.asReadonly();
    readonly userRole = this._role.asReadonly();

    readonly isAuthenticated = computed(() => !!this._token());
    readonly isAdmin = computed(() => this._role() === 'ADMIN');
    readonly isUnderwriter = computed(() => this._role() === 'UNDERWRITER');
    readonly isClaimsOfficer = computed(() => this._role() === 'CLAIMS_OFFICER');
    readonly isCustomer = computed(() => this._role() === 'CUSTOMER');

    constructor() {
        this.initAuthData();
    }

    // ── Private Helpers ───────────────────────────────────
    private loadToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    private initAuthData() {
        const token = this.loadToken();
        if (token) {
            this.decodeAndSetData(token);
        }
    }

    private decodeAndSetData(token: string) {
        if (!token || token.split('.').length !== 3) {
            // Probably a mock or invalid token, don't try to decode
            return;
        }
        try {
            const payload: any = jwtDecode(token);
            this._username.set(payload.username ?? '');
            this._role.set(payload.role?.replace('ROLE_', '') ?? 'CUSTOMER');
        } catch (e) {
            console.error('Failed to decode token:', e);
            this._token.set(null);
            this._username.set('');
            this._role.set(null);
        }
    }

    getAuthHeaders(): HttpHeaders {
        return new HttpHeaders({ Authorization: `Bearer ${this._token()}` });
    }

    // ── Public Methods ────────────────────────────────────
    login(jwtRequest: any): Observable<any> {
        return this.http.post(`${this.API_URL}/login`, jwtRequest).pipe(
            tap((response: any) => {
                //console.log('Login successful:', response);
                localStorage.setItem(this.TOKEN_KEY, response.token);
                this._token.set(response.token);
                this.decodeAndSetData(response.token);
                this.router.navigate([this.getRedirectPath()]);
            }),
            catchError((error) => {
                console.error('Login failed:', error);
                return throwError(() => error);
            })
        );
    }

    register(req: RegisterRequest): Observable<RegisterResponse> {
        return this.http.post<RegisterResponse>(`${this.API_URL}/register`, req).pipe(
            catchError((err) => throwError(() => err))
        );
    }

    changePassword(dto: { currentPassword: string; newPassword: string }): Observable<string> {
        return this.http.put(`${this.API_URL}/change-password`, dto, {
            headers: this.getAuthHeaders(),
            responseType: 'text'
        }).pipe(catchError((err) => throwError(() => err)));
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        this._token.set(null);
        this._username.set('');
        this._role.set(null);
        this.router.navigate(['/login']);
    }

    getRedirectPath(): string {
        const role = this._role();
        if (role === 'ADMIN') return '/admin';
        if (role === 'UNDERWRITER') return '/underwriter';
        if (role === 'CLAIMS_OFFICER') return '/claims-officer';
        return '/customer';
    }
}
